package com.carlosalarcongu.nextdrive.ui

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddRoad
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import com.carlosalarcongu.nextdrive.data.Vehicle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

fun getBrandLogoResId(context: Context, brand: String?): Int {
    if (brand.isNullOrBlank()) return 0
    val sanitized = brand.lowercase().replace(" ", "_").replace("-", "_")
    val logoName = "logo_$sanitized"
    var resId = context.resources.getIdentifier(logoName, "drawable", context.packageName)
    if (resId == 0) resId = context.resources.getIdentifier(sanitized, "drawable", context.packageName)
    return resId
}

@Composable
fun VehicleImageLoader(vehicle: Vehicle, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var bitmap by remember(vehicle.imageUri) { mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null) }

    LaunchedEffect(vehicle.imageUri) {
        if (!vehicle.imageUri.isNullOrBlank()) {
            withContext(Dispatchers.IO) {
                try {
                    val inputStream = context.contentResolver.openInputStream(Uri.parse(vehicle.imageUri))
                    bitmap = BitmapFactory.decodeStream(inputStream)?.asImageBitmap()
                } catch (e: Exception) { e.printStackTrace() }
            }
        }
    }

    val brandLogoResId = getBrandLogoResId(context, vehicle.brand)

    if (bitmap != null) {
        Image(bitmap = bitmap!!, contentDescription = "Foto", modifier = modifier, contentScale = ContentScale.Crop)
    } else if (brandLogoResId != 0) {
        Image(painter = painterResource(id = brandLogoResId), contentDescription = "Logo", modifier = modifier.padding(16.dp), contentScale = ContentScale.Fit)
    } else {
        Icon(Icons.Rounded.DirectionsCar, contentDescription = null, modifier = modifier.padding(32.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GarageScreen(viewModel: NextDriveViewModel, onNavigateToAddVehicle: () -> Unit, onVehicleClick: (Long) -> Unit, onNavigateToUserGuide: () -> Unit) {
    val vehicles by viewModel.allVehicles.collectAsState()

    var quickKmVehicle by remember { mutableStateOf<Vehicle?>(null) }
    var quickKmTab by remember { mutableStateOf("SUMAR") }
    var quickKmInput by remember { mutableStateOf("") }

    Scaffold(
        topBar = { TopAppBar(title = { Text("MI GARAJE", fontWeight = FontWeight.Bold) }, colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)) },
        floatingActionButton = { FloatingActionButton(onClick = onNavigateToAddVehicle, containerColor = MaterialTheme.colorScheme.primary) { Icon(Icons.Default.Add, "Añadir") } }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(vehicles) { vehicle ->
                    Card(modifier = Modifier.fillMaxWidth().clickable { onVehicleClick(vehicle.id) }, elevation = CardDefaults.cardElevation(defaultElevation = 4.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                        Column {
                            // CORRECCIÓN DEL TRY-CATCH: Sacamos MaterialTheme fuera
                            val defaultBg = MaterialTheme.colorScheme.background
                            val boxColor = remember(vehicle.colorHex, defaultBg) {
                                try {
                                    if (!vehicle.colorHex.isNullOrBlank()) Color(android.graphics.Color.parseColor(vehicle.colorHex)) else defaultBg
                                } catch (e: Exception) { defaultBg }
                            }

                            Box(modifier = Modifier.fillMaxWidth().height(160.dp).background(boxColor), contentAlignment = Alignment.Center) {
                                VehicleImageLoader(vehicle = vehicle, modifier = Modifier.fillMaxSize())
                            }
                            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    val displayName = vehicle.nickname?.takeIf { it.isNotBlank() } ?: "${vehicle.brand ?: ""} ${vehicle.model}"
                                    Text(text = displayName.uppercase(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                    if (!vehicle.licensePlate.isNullOrBlank()) Text(text = vehicle.licensePlate, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = "${vehicle.currentKm ?: 0} KM", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    FilledTonalIconButton(onClick = { quickKmVehicle = vehicle }) {
                                        Icon(Icons.Default.AddRoad, "Actualizar KM")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (quickKmVehicle != null) {
            AlertDialog(
                onDismissRequest = { quickKmVehicle = null; quickKmInput = "" },
                title = { Text("Actualizar Kilometraje") },
                text = {
                    Column {
                        TabRow(selectedTabIndex = if (quickKmTab == "SUMAR") 0 else 1) {
                            Tab(selected = quickKmTab == "SUMAR", onClick = { quickKmTab = "SUMAR" }, text = { Text("+ Sumar") })
                            Tab(selected = quickKmTab == "ESTABLECER", onClick = { quickKmTab = "ESTABLECER" }, text = { Text("Establecer") })
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = quickKmInput,
                            onValueChange = { quickKmInput = it.filter { c -> c.isDigit() } },
                            label = { Text(if (quickKmTab == "SUMAR") "¿Cuántos km has hecho?" else "Nuevo cuentakilómetros") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        val input = quickKmInput.toIntOrNull() ?: 0
                        if (input > 0) {
                            val newKm = if (quickKmTab == "SUMAR") (quickKmVehicle!!.currentKm ?: 0) + input else input
                            viewModel.updateVehicle(quickKmVehicle!!.copy(currentKm = newKm))
                            quickKmVehicle = null
                            quickKmInput = ""
                        }
                    }) { Text("GUARDAR") }
                },
                dismissButton = { TextButton(onClick = { quickKmVehicle = null; quickKmInput = "" }) { Text("CANCELAR") } }
            )
        }
    }
}