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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Navigation
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
import kotlin.math.abs

// --- MODELO Y FUNCIONES VISUALES ---

data class GasStationData(
    val name: String, val brand: String,
    val lat: Double, val lon: Double,
    val price95: Double, val price98: Double, val priceDiesel: Double,
    val distanceMeters: Float
)

// Función para obtener el color corporativo de la gasolinera
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
        upper.contains("CARREFOUR") -> Color(0xFF1565C0)
        upper.contains("PETRONOR") -> Color(0xFF009688)
        upper.contains("AVIA") -> Color(0xFFE53935)
        else -> Color.LightGray
    }
}

// Función para interpolar el color del precio (Verde -> Amarillo -> Rojo)
fun getPriceColor(price: Double): Color {
    if (price <= 0.0) return Color.Gray
    val minPrice = 1.20 // Tope barato (Verde)
    val maxPrice = 1.80 // Tope caro (Rojo)

    val ratio = ((price - minPrice) / (maxPrice - minPrice)).coerceIn(0.0, 1.0).toFloat()

    val r = (ratio * 255).toInt().coerceIn(0, 255)
    val g = ((1f - ratio) * 200 + 55).toInt().coerceIn(0, 255)

    return Color(r, g, 0)
}

// Genera el bocadillo del mapa con precio más pequeño y borde corporativo
fun createPriceMarkerDrawable(context: Context, priceText: String, priceValue: Double, brandColor: Color): Drawable {
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    paint.textSize = 34f // Texto ligeramente más pequeño
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
    val path = Path().apply {
        moveTo(width / 2f - 12f, height.toFloat())
        lineTo(width / 2f + 12f, height.toFloat())
        lineTo(width / 2f, height.toFloat() + arrowHeight.toFloat())
        close()
    }

    // 1. Dibujar el FONDO (Color del termómetro de precio)
    paint.style = Paint.Style.FILL
    paint.color = if (priceValue > 0) getPriceColor(priceValue).toArgb() else android.graphics.Color.DKGRAY
    canvas.drawRoundRect(rect, 12f, 12f, paint)
    canvas.drawPath(path, paint)

    // 2. Dibujar el BORDE FINO (Color corporativo)
    paint.style = Paint.Style.STROKE
    paint.strokeWidth = 3.5f
    paint.color = brandColor.toArgb()
    canvas.drawRoundRect(rect, 12f, 12f, paint)
    canvas.drawPath(path, paint)

    // 3. Dibujar el TEXTO
    paint.style = Paint.Style.FILL
    paint.color = if (priceValue > 1.45) android.graphics.Color.WHITE else android.graphics.Color.BLACK
    canvas.drawText(priceText, width / 2f, height / 2f + textBounds.height() / 2f - 2f, paint)

    return BitmapDrawable(context.resources, bitmap)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FuelPricesScreen() {
    val context = LocalContext.current
    var viewMode by remember { mutableStateOf("LISTA") }
    var sortBy by remember { mutableStateOf("Gasolina 95") }
    var mapFuelType by remember { mutableStateOf("Gasolina 95") }

    var stations by remember { mutableStateOf<List<GasStationData>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // --- GESTIÓN DE LOCALIZACIÓN ---
    var userLocation by remember { mutableStateOf<Location?>(null) }
    var hasLocationPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
    }

    val locationManager = remember { context.getSystemService(Context.LOCATION_SERVICE) as LocationManager }
    val locationListener = remember { LocationListener { location -> userLocation = location } }

    val permisoGPSLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { concedido ->
            hasLocationPermission = concedido
            if (!concedido) Toast.makeText(context, "Permisos necesarios para buscar gasolineras", Toast.LENGTH_LONG).show()
        }
    )

    LaunchedEffect(Unit) {
        Configuration.getInstance().userAgentValue = context.packageName
        if (!hasLocationPermission) permisoGPSLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    DisposableEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            try {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000L, 10f, locationListener)
                userLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            } catch (e: SecurityException) { e.printStackTrace() }
        }
        onDispose { locationManager.removeUpdates(locationListener) }
    }

    // --- FETCH DE LA API DEL GOBIERNO ---
    LaunchedEffect(userLocation) {
        val refLat = userLocation?.latitude ?: 43.4623
        val refLon = userLocation?.longitude ?: -3.8100

        withContext(Dispatchers.IO) {
            try {
                isLoading = true
                val url = "https://sedeaplicaciones.minetur.gob.es/ServiciosRESTCarburantes/PreciosCarburantes/EstacionesTerrestres/"
                val jsonString = URL(url).readText()
                val jsonObject = JSONObject(jsonString)
                val array = jsonObject.getJSONArray("ListaEESSPrecio")

                val fetchedStations = mutableListOf<GasStationData>()
                val results = FloatArray(1)

                for (i in 0 until array.length()) {
                    val item = array.getJSONObject(i)
                    val lat = item.getString("Latitud").replace(",", ".").toDoubleOrNull() ?: continue
                    val lon = item.getString("Longitud (WGS84)").replace(",", ".").toDoubleOrNull() ?: continue

                    // Filtro de caja rápida (~50km)
                    if (abs(lat - refLat) > 0.5 || abs(lon - refLon) > 0.5) continue

                    Location.distanceBetween(refLat, refLon, lat, lon, results)
                    val distance = results[0]

                    if (distance <= 15000f) {
                        val name = item.getString("Rótulo")
                        val price95 = item.getString("Precio Gasolina 95 E5").replace(",", ".").toDoubleOrNull() ?: 0.0
                        val price98 = item.getString("Precio Gasolina 98 E5").replace(",", ".").toDoubleOrNull() ?: 0.0
                        val priceDiesel = item.getString("Precio Gasoleo A").replace(",", ".").toDoubleOrNull() ?: 0.0

                        fetchedStations.add(GasStationData(name, name, lat, lon, price95, price98, priceDiesel, distance))
                    }
                }
                stations = fetchedStations
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    val sortedList = remember(sortBy, stations) {
        val filtered = when (sortBy) {
            "Gasolina 98" -> stations.filter { it.price98 > 0.0 }
            "Diésel" -> stations.filter { it.priceDiesel > 0.0 }
            else -> stations.filter { it.price95 > 0.0 }
        }
        filtered.sortedBy {
            when(sortBy) { "Gasolina 98" -> it.price98; "Diésel" -> it.priceDiesel; else -> it.price95 }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("GASOLINERAS CERCANAS", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {

            TabRow(selectedTabIndex = if (viewMode == "LISTA") 0 else 1, containerColor = MaterialTheme.colorScheme.surfaceVariant) {
                Tab(selected = viewMode == "LISTA", onClick = { viewMode = "LISTA" }, text = { Text("LISTADO", fontWeight = FontWeight.Bold) }, icon = { Icon(Icons.Default.FormatListBulleted, "") })
                Tab(selected = viewMode == "MAPA", onClick = { viewMode = "MAPA" }, text = { Text("MAPA", fontWeight = FontWeight.Bold) }, icon = { Icon(Icons.Default.Map, "") })
            }

            Box(modifier = Modifier.weight(1f).fillMaxWidth().clipToBounds()) {
                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Sincronizando con el Ministerio...", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                } else if (viewMode == "LISTA") {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Ordenar precios por:", style = MaterialTheme.typography.labelLarge)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FilterChip(selected = sortBy == "Gasolina 95", onClick = { sortBy = "Gasolina 95" }, label = { Text("95") })
                                FilterChip(selected = sortBy == "Diésel", onClick = { sortBy = "Diésel" }, label = { Text("Diésel") })
                            }
                        }
                        LazyColumn(contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(sortedList) { station -> GasStationCard(station, sortBy) }
                        }
                    }
                } else {
                    // VISTA DE MAPA REAL (OSMDroid)
                    Box(modifier = Modifier.fillMaxSize().clipToBounds()) {
                        AndroidView(
                            factory = { ctx ->
                                MapView(ctx).apply {
                                    setMultiTouchControls(true)
                                    controller.setZoom(14.0)
                                    // Desactivar botones de zoom para interfaz más limpia
                                    zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)

                                    val inverseMatrix = ColorMatrix(floatArrayOf(
                                        -1f, 0f, 0f, 0f, 255f,
                                        0f, -1f, 0f, 0f, 255f,
                                        0f, 0f, -1f, 0f, 255f,
                                        0f, 0f, 0f, 1f, 0f
                                    ))
                                    val grayscaleMatrix = ColorMatrix().apply { setSaturation(0f) }
                                    grayscaleMatrix.postConcat(inverseMatrix)
                                    overlayManager.tilesOverlay.setColorFilter(ColorMatrixColorFilter(grayscaleMatrix))
                                }
                            },
                            update = { map ->
                                map.overlays.clear()

                                val centerPoint = GeoPoint(userLocation?.latitude ?: 43.4623, userLocation?.longitude ?: -3.8100)
                                map.controller.setCenter(centerPoint)

                                if (userLocation != null) {
                                    val userMarker = Marker(map).apply {
                                        position = centerPoint
                                        title = "Estás aquí"
                                        icon = ContextCompat.getDrawable(context, android.R.drawable.ic_menu_mylocation)
                                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                                    }
                                    map.overlays.add(userMarker)
                                }

                                stations.forEach { station ->
                                    val currentPrice = when(mapFuelType) { "Diésel" -> station.priceDiesel; else -> station.price95 }
                                    if (currentPrice > 0.0) {
                                        val stationMarker = Marker(map).apply {
                                            position = GeoPoint(station.lat, station.lon)
                                            title = station.name
                                            val priceText = "${currentPrice}€"
                                            val brandColor = getBrandColor(station.brand)

                                            // Aplicamos el borde con el color de la gasolinera
                                            icon = createPriceMarkerDrawable(context, priceText, currentPrice, brandColor)
                                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)

                                            setOnMarkerClickListener { _, _ ->
                                                val gmmIntentUri = Uri.parse("google.navigation:q=${station.lat},${station.lon}")
                                                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply { setPackage("com.google.android.apps.maps") }
                                                try {
                                                    context.startActivity(mapIntent)
                                                } catch (e: ActivityNotFoundException) {
                                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://maps.google.com/?daddr=${station.lat},${station.lon}")))
                                                }
                                                true
                                            }
                                        }
                                        map.overlays.add(stationMarker)
                                    }
                                }
                                map.invalidate()
                            },
                            modifier = Modifier.fillMaxSize()
                        )

                        // SELECTOR FLOTANTE EN EL MAPA PARA 95/DIÉSEL
                        Surface(
                            modifier = Modifier.align(Alignment.TopCenter).padding(16.dp),
                            shape = RoundedCornerShape(24.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f),
                            shadowElevation = 4.dp
                        ) {
                            Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FilterChip(
                                    selected = mapFuelType == "Gasolina 95",
                                    onClick = { mapFuelType = "Gasolina 95" },
                                    label = { Text("Gasolina 95", fontWeight = FontWeight.Bold) },
                                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primary)
                                )
                                FilterChip(
                                    selected = mapFuelType == "Diésel",
                                    onClick = { mapFuelType = "Diésel" },
                                    label = { Text("Diésel", fontWeight = FontWeight.Bold) },
                                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primary)
                                )
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

    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)).background(brandColor.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.LocalGasStation, "", tint = brandColor)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(station.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, maxLines = 1)
                    val distStr = if (station.distanceMeters > 1000) "%.1f km".format(station.distanceMeters / 1000) else "${station.distanceMeters.toInt()} m"
                    Text("A $distStr de ti", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(
                    onClick = {
                        val gmmIntentUri = Uri.parse("google.navigation:q=${station.lat},${station.lon}")
                        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply { setPackage("com.google.android.apps.maps") }
                        try { context.startActivity(mapIntent) } catch (e: ActivityNotFoundException) {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://maps.google.com/?daddr=${station.lat},${station.lon}")))
                        }
                    },
                    modifier = Modifier.background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape)
                ) {
                    Icon(Icons.Default.Navigation, "Navegar", tint = MaterialTheme.colorScheme.primary)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                if (station.price95 > 0.0) PriceTag("Gasolina 95", station.price95, highlightedSort == "Gasolina 95")
                if (station.price98 > 0.0) PriceTag("Gasolina 98", station.price98, highlightedSort == "Gasolina 98")
                if (station.priceDiesel > 0.0) PriceTag("Diésel", station.priceDiesel, highlightedSort == "Diésel")
            }
        }
    }
}

@Composable
fun PriceTag(type: String, price: Double, isHighlighted: Boolean) {
    val priceColor = getPriceColor(price)
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = if (isHighlighted) Modifier.background(priceColor.copy(alpha = 0.1f), RoundedCornerShape(8.dp)).padding(8.dp) else Modifier.padding(8.dp)) {
        Text(type, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(4.dp))
        Text("${price}€", style = MaterialTheme.typography.titleMedium, color = priceColor, fontWeight = FontWeight.ExtraBold)
    }
}