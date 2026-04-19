package com.carlosalarcongu.nextdrive.ui

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricalServices
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

// --- MODELO Y FUNCIONES VISUALES ---

data class GasStationData(
    val name: String, val brand: String,
    val lat: Double, val lon: Double,
    val price95: Double, val price98: Double, val priceDiesel: Double, val priceElectric: Double,
    val distanceMeters: Float
)

fun getBrandColor(brand: String): Color {
    val upper = brand.uppercase()
    return when {
        upper.contains("REPSOL") -> Color(0xFFFF5722)
        upper.contains("CEPSA") -> Color(0xFFD32F2F)
        upper.contains("PLENOIL") -> Color(0xFF1976D2)
        upper.contains("BALLENOIL") -> Color(0xFF03A9F4)
        upper.contains("GALP") -> Color(0xFFFF9800)
        upper.contains("SHELL") -> Color(0xFFFFC107)
        upper.contains("BP") -> Color(0xFF4CAF50)
        upper.contains("ENDESA") || upper.contains("IBERDROLA") || upper.contains("TESLA") -> Color(0xFF00BCD4) // Eléctricas
        else -> Color.LightGray
    }
}

fun getPriceColor(price: Double, isElectric: Boolean = false): Color {
    if (price <= 0.0) return Color.Gray
    if (isElectric) return Color(0xFF00BCD4) // Cyan para eléctrico

    val minPrice = 1.20
    val maxPrice = 1.80
    val ratio = ((price - minPrice) / (maxPrice - minPrice)).coerceIn(0.0, 1.0).toFloat()
    val r = (ratio * 255).toInt().coerceIn(0, 255)
    val g = ((1f - ratio) * 200 + 55).toInt().coerceIn(0, 255)
    return Color(r, g, 0)
}

fun createPriceMarkerDrawable(context: Context, priceText: String, priceValue: Double, brandColor: Color, isElectric: Boolean): Drawable {
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    paint.textSize = 34f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    paint.textAlign = Paint.Align.CENTER

    val textBounds = Rect()
    paint.getTextBounds(priceText, 0, priceText.length, textBounds)

    val paddingX = 20
    val paddingY = 12
    val width = textBounds.width() + paddingX * 2
    val height = textBounds.height() + paddingY * 2
    val arrowHeight = 14

    val bitmap = Bitmap.createBitmap(width, height + arrowHeight, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val rect = RectF(0f, 0f, width.toFloat(), height.toFloat())
    val path = Path().apply { moveTo(width / 2f - 12f, height.toFloat()); lineTo(width / 2f + 12f, height.toFloat()); lineTo(width / 2f, height.toFloat() + arrowHeight.toFloat()); close() }

    paint.style = Paint.Style.FILL
    paint.color = if (priceValue > 0) getPriceColor(priceValue, isElectric).toArgb() else android.graphics.Color.DKGRAY
    canvas.drawRoundRect(rect, 12f, 12f, paint)
    canvas.drawPath(path, paint)

    paint.style = Paint.Style.STROKE
    paint.strokeWidth = 3.5f
    paint.color = brandColor.toArgb()
    canvas.drawRoundRect(rect, 12f, 12f, paint)
    canvas.drawPath(path, paint)

    paint.style = Paint.Style.FILL
    paint.color = if (priceValue > 1.45 || isElectric) android.graphics.Color.WHITE else android.graphics.Color.BLACK
    canvas.drawText(priceText, width / 2f, height / 2f + textBounds.height() / 2f - 2f, paint)

    return BitmapDrawable(context.resources, bitmap)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FuelPricesScreen(onNavigateToSettings: () -> Unit) {
    val context = LocalContext.current
    var viewMode by remember { mutableStateOf("LISTA") }
    var sortBy by remember { mutableStateOf("Gasolina 95") }
    var mapFuelType by remember { mutableStateOf("Gasolina 95") }

    var stations by remember { mutableStateOf<List<GasStationData>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    var userLocation by remember { mutableStateOf<Location?>(null) }
    var hasLocationPermission by remember { mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) }

    val locationManager = remember { context.getSystemService(Context.LOCATION_SERVICE) as LocationManager }
    val locationListener = remember { LocationListener { location -> userLocation = location } }

    val permisoGPSLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission()) { concedido ->
        hasLocationPermission = concedido
    }

    LaunchedEffect(Unit) {
        Configuration.getInstance().userAgentValue = context.packageName
        if (!hasLocationPermission) permisoGPSLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    DisposableEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            try {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000L, 10f, locationListener)
                userLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            } catch (e: SecurityException) {}
        }
        onDispose { locationManager.removeUpdates(locationListener) }
    }

    LaunchedEffect(userLocation) {
        val refLat = userLocation?.latitude ?: 43.4623
        val refLon = userLocation?.longitude ?: -3.8100

        withContext(Dispatchers.IO) {
            try {
                isLoading = true
                val sharedPrefs = context.getSharedPreferences("FuelCache", Context.MODE_PRIVATE)
                val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                val cachedDate = sharedPrefs.getString("date", "")
                val cachedJson = sharedPrefs.getString("json", "")

                val jsonString = if (cachedDate == today && !cachedJson.isNullOrEmpty()) {
                    cachedJson // Carga instantánea desde memoria
                } else {
                    val url = "https://sedeaplicaciones.minetur.gob.es/ServiciosRESTCarburantes/PreciosCarburantes/EstacionesTerrestres/"
                    val freshJson = URL(url).readText()
                    sharedPrefs.edit().putString("date", today).putString("json", freshJson).apply()
                    freshJson
                }

                val jsonObject = JSONObject(jsonString)
                val array = jsonObject.getJSONArray("ListaEESSPrecio")

                val fetchedStations = mutableListOf<GasStationData>()
                val results = FloatArray(1)

                for (i in 0 until array.length()) {
                    val item = array.getJSONObject(i)
                    val lat = item.getString("Latitud").replace(",", ".").toDoubleOrNull() ?: continue
                    val lon = item.getString("Longitud (WGS84)").replace(",", ".").toDoubleOrNull() ?: continue

                    if (abs(lat - refLat) > 0.5 || abs(lon - refLon) > 0.5) continue
                    Location.distanceBetween(refLat, refLon, lat, lon, results)
                    if (results[0] <= 15000f) {
                        fetchedStations.add(GasStationData(
                            name = item.getString("Rótulo"), brand = item.getString("Rótulo"),
                            lat = lat, lon = lon,
                            price95 = item.getString("Precio Gasolina 95 E5").replace(",", ".").toDoubleOrNull() ?: 0.0,
                            price98 = item.getString("Precio Gasolina 98 E5").replace(",", ".").toDoubleOrNull() ?: 0.0,
                            priceDiesel = item.getString("Precio Gasoleo A").replace(",", ".").toDoubleOrNull() ?: 0.0,
                            priceElectric = 0.0, distanceMeters = results[0]
                        ))
                    }
                }

                // MOCKS DE ELECTROLINERAS
                fetchedStations.add(GasStationData("Supercharger Tesla", "Tesla", 43.424, -3.829, 0.0, 0.0, 0.0, 0.45, 1200f))
                fetchedStations.add(GasStationData("Iberdrola Carga Rápida", "Iberdrola", 43.455, -3.830, 0.0, 0.0, 0.0, 0.35, 2500f))

                stations = fetchedStations
            } catch (e: Exception) { e.printStackTrace() } finally { isLoading = false }
        }
    }

    val sortedList = remember(sortBy, stations) {
        val filtered = when (sortBy) {
            "Eléctrico" -> stations.filter { it.priceElectric > 0.0 }
            "Gasolina 98" -> stations.filter { it.price98 > 0.0 }
            "Diésel" -> stations.filter { it.priceDiesel > 0.0 }
            else -> stations.filter { it.price95 > 0.0 }
        }
        filtered.sortedBy { when(sortBy) { "Eléctrico"->it.priceElectric; "Gasolina 98"->it.price98; "Diésel"->it.priceDiesel; else->it.price95 } }
    }

    Scaffold(
        topBar = {
            Surface(shadowElevation = 4.dp) {
                TopAppBar(
                    title = { Text("ESTACIONES CERCANAS", fontWeight = FontWeight.Bold) },
                    actions = { IconButton(onClick = onNavigateToSettings) { Icon(Icons.Default.Settings, "Ajustes") } },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
                )
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            TabRow(selectedTabIndex = if (viewMode == "LISTA") 0 else 1) {
                Tab(selected = viewMode == "LISTA", onClick = { viewMode = "LISTA" }, text = { Text("LISTADO", fontWeight = FontWeight.Bold) }, icon = { Icon(Icons.Default.FormatListBulleted, "") })
                Tab(selected = viewMode == "MAPA", onClick = { viewMode = "MAPA" }, text = { Text("MAPA", fontWeight = FontWeight.Bold) }, icon = { Icon(Icons.Default.Map, "") })
            }

            Box(modifier = Modifier.weight(1f).fillMaxWidth().clipToBounds()) {
                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                } else if (viewMode == "LISTA") {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FilterChip(selected = sortBy == "Gasolina 95", onClick = { sortBy = "Gasolina 95" }, label = { Text("95") })
                                FilterChip(selected = sortBy == "Diésel", onClick = { sortBy = "Diésel" }, label = { Text("Diésel") })
                                FilterChip(selected = sortBy == "Eléctrico", onClick = { sortBy = "Eléctrico" }, label = { Text("Eléctrico", color = if (sortBy == "Eléctrico") Color(0xFF00BCD4) else Color.Unspecified) })
                            }
                        }
                        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(sortedList) { station -> GasStationCard(station, sortBy) }
                        }
                    }
                } else {
                    Box(modifier = Modifier.fillMaxSize().clipToBounds()) {
                        AndroidView(
                            factory = { ctx ->
                                MapView(ctx).apply {
                                    setMultiTouchControls(true)
                                    controller.setZoom(14.0)
                                    zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
                                    val inverseMatrix = ColorMatrix(floatArrayOf(-1f, 0f, 0f, 0f, 255f, 0f, -1f, 0f, 0f, 255f, 0f, 0f, -1f, 0f, 255f, 0f, 0f, 0f, 1f, 0f))
                                    val grayscaleMatrix = ColorMatrix().apply { setSaturation(0f) }
                                    grayscaleMatrix.postConcat(inverseMatrix)
                                    overlayManager.tilesOverlay.setColorFilter(ColorMatrixColorFilter(grayscaleMatrix))
                                }
                            },
                            update = { map ->
                                map.overlays.clear()
                                val centerPoint = GeoPoint(userLocation?.latitude ?: 43.4623, userLocation?.longitude ?: -3.8100)
                                map.controller.setCenter(centerPoint)
                                if (userLocation != null) map.overlays.add(Marker(map).apply { position = centerPoint; icon = ContextCompat.getDrawable(context, android.R.drawable.ic_menu_mylocation); setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER) })

                                stations.forEach { station ->
                                    val currentPrice = when(mapFuelType) { "Diésel" -> station.priceDiesel; "Eléctrico" -> station.priceElectric; else -> station.price95 }
                                    if (currentPrice > 0.0) {
                                        map.overlays.add(Marker(map).apply {
                                            position = GeoPoint(station.lat, station.lon)
                                            title = station.name
                                            val priceText = if (mapFuelType == "Eléctrico") "${currentPrice}€/kWh" else "${currentPrice}€"
                                            icon = createPriceMarkerDrawable(context, priceText, currentPrice, getBrandColor(station.brand), mapFuelType == "Eléctrico")
                                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                            setOnMarkerClickListener { _, _ ->
                                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q=${station.lat},${station.lon}")).apply { setPackage("com.google.android.apps.maps") }
                                                try { context.startActivity(intent) } catch (e: Exception) { }
                                                true
                                            }
                                        })
                                    }
                                }
                                map.invalidate()
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                        Surface(modifier = Modifier.align(Alignment.TopCenter).padding(16.dp), shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)) {
                            Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FilterChip(selected = mapFuelType == "Gasolina 95", onClick = { mapFuelType = "Gasolina 95" }, label = { Text("95") })
                                FilterChip(selected = mapFuelType == "Diésel", onClick = { mapFuelType = "Diésel" }, label = { Text("Diésel") })
                                FilterChip(selected = mapFuelType == "Eléctrico", onClick = { mapFuelType = "Eléctrico" }, label = { Text("Eléctrico", color = if (mapFuelType == "Eléctrico") Color(0xFF00BCD4) else Color.Unspecified) })
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GasStationCard(station: GasStationData, highlightedSort: String) {
    val context = LocalContext.current
    val brandColor = getBrandColor(station.brand)
    val isElectricOnly = station.priceElectric > 0 && station.price95 == 0.0

    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)).background(brandColor.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                    Icon(if (isElectricOnly) Icons.Default.ElectricalServices else Icons.Default.LocalGasStation, "", tint = brandColor)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(station.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, maxLines = 1)
                    val distStr = if (station.distanceMeters > 1000) "%.1f km".format(station.distanceMeters / 1000) else "${station.distanceMeters.toInt()} m"
                    Text("A $distStr de ti", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(
                    onClick = {
                        // NUEVO INTENT: Muestra la chincheta con el nombre en Maps en lugar de arrancar navegación
                        val uriStr = "geo:0,0?q=${station.lat},${station.lon}(${Uri.encode(station.name)})"
                        val mapIntent = Intent(Intent.ACTION_VIEW, Uri.parse(uriStr)).apply { setPackage("com.google.android.apps.maps") }
                        try { context.startActivity(mapIntent) } catch (e: Exception) {}
                    },
                    modifier = Modifier.background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape)
                ) { Icon(Icons.Default.Place, "Ver en mapa", tint = MaterialTheme.colorScheme.primary) }
            }
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                if (station.price95 > 0.0) PriceTag("Gasolina 95", "${station.price95}€", highlightedSort == "Gasolina 95", false, station.price95)
                if (station.priceDiesel > 0.0) PriceTag("Diésel", "${station.priceDiesel}€", highlightedSort == "Diésel", false, station.priceDiesel)
                if (station.priceElectric > 0.0) PriceTag("Recarga", "${station.priceElectric}€/kWh", highlightedSort == "Eléctrico", true, station.priceElectric)
            }
        }
    }
}

@Composable
fun PriceTag(type: String, priceStr: String, isHighlighted: Boolean, isElectric: Boolean, priceVal: Double) {
    val priceColor = getPriceColor(priceVal, isElectric)
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = if (isHighlighted) Modifier.background(priceColor.copy(alpha = 0.1f), RoundedCornerShape(8.dp)).padding(8.dp) else Modifier.padding(8.dp)) {
        Text(type, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(4.dp))
        Text(priceStr, style = MaterialTheme.typography.titleMedium, color = priceColor, fontWeight = FontWeight.ExtraBold)
    }
}