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
import androidx.compose.ui.text.style.TextAlign
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

    Scaffold(
        topBar = { TopAppBar(title = { Text("MI GARAJE", fontWeight = FontWeight.Bold) }, colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)) },
        floatingActionButton = { FloatingActionButton(onClick = onNavigateToAddVehicle, containerColor = MaterialTheme.colorScheme.primary) { Icon(Icons.Default.Add, "Añadir") } }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (vehicles.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) { Text("Garaje vacío.\nPulsa + para registrar un vehículo.", textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            } else {
                LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(vehicles) { vehicle ->
                        Card(modifier = Modifier.fillMaxWidth().clickable { onVehicleClick(vehicle.id) }, elevation = CardDefaults.cardElevation(defaultElevation = 4.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                            Column {
                                // APLICAMOS EL COLOR DEL VEHÍCULO AL FONDO DE LA IMAGEN
                                val defaultColor = MaterialTheme.colorScheme.background
                                val boxColor = remember(vehicle.colorHex, defaultColor) {
                                    try {
                                        if (!vehicle.colorHex.isNullOrBlank()) {
                                            Color(android.graphics.Color.parseColor(vehicle.colorHex))
                                        } else {
                                            defaultColor
                                        }
                                    } catch (e: Exception) {
                                        defaultColor
                                    }
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
                                    Text(text = "${vehicle.currentKm ?: 0} KM", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Card(modifier = Modifier.fillMaxWidth().clickable { onNavigateToUserGuide() }, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                    Text(text = "GUÍA DE USO Y FUNCIONES", modifier = Modifier.padding(16.dp).fillMaxWidth(), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                }
            }
        }
    }
}
