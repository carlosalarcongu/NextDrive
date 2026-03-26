package com.carlosalarcongu.nextdrive.ui

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddRoad
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun GarageScreen(
    viewModel: NextDriveViewModel,
    onNavigateToAddVehicle: () -> Unit,
    onNavigateToEditVehicle: (Long) -> Unit,
    onVehicleClick: (Long) -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val vehicles by viewModel.allVehicles.collectAsState()
    val haptic = LocalHapticFeedback.current

    var quickKmVehicle by remember { mutableStateOf<Vehicle?>(null) }
    var quickKmTab by remember { mutableStateOf("SUMAR") }
    var quickKmInput by remember { mutableStateOf("") }

    // Estado para el Long Press
    var selectedVehicleId by remember { mutableStateOf<Long?>(null) }

    Scaffold(
        topBar = {
            Surface(shadowElevation = 4.dp) { // SOMBRA AÑADIDA
                if (selectedVehicleId != null) {
                    TopAppBar(
                        title = { Text("1 seleccionado") },
                        navigationIcon = { IconButton(onClick = { selectedVehicleId = null }) { Icon(Icons.Default.Close, "Cancelar") } },
                        actions = {
                            IconButton(onClick = { onNavigateToEditVehicle(selectedVehicleId!!); selectedVehicleId = null }) { Icon(Icons.Default.Edit, "Editar") }
                            IconButton(onClick = {
                                val v = vehicles.find { it.id == selectedVehicleId }
                                if (v != null) viewModel.softDeleteVehicle(v)
                                selectedVehicleId = null
                            }) { Icon(Icons.Default.Delete, "Borrar", tint = MaterialTheme.colorScheme.error) }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    )
                } else {
                    TopAppBar(
                        title = { Text("MI GARAJE", fontWeight = FontWeight.Bold) },
                        actions = {
                            IconButton(onClick = onNavigateToAddVehicle) { Icon(Icons.Default.Add, "Añadir") }
                            IconButton(onClick = onNavigateToSettings) { Icon(Icons.Default.Settings, "Ajustes") }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
                    )
                }
            }
        }
        // FAB ELIMINADO
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(vehicles) { vehicle ->
                    val isSelected = selectedVehicleId == vehicle.id
                    val elevation = if (isSelected) 8.dp else 4.dp

                    Card(
                        modifier = Modifier.fillMaxWidth().combinedClickable(
                            onClick = {
                                if (selectedVehicleId != null) {
                                    selectedVehicleId = if (selectedVehicleId == vehicle.id) null else vehicle.id
                                } else {
                                    onVehicleClick(vehicle.id)
                                }
                            },
                            onLongClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                selectedVehicleId = vehicle.id
                            }
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
                        colors = CardDefaults.cardColors(containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column {
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