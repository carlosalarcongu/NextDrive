// GarageScreen.kt
package com.carlosalarcongu.nextdrive.ui

import android.content.Intent
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.carlosalarcongu.nextdrive.data.Vehicle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun UriImageLoader(uriString: String?, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var bitmap by remember(uriString) { mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null) }

    LaunchedEffect(uriString) {
        if (!uriString.isNullOrBlank()) {
            withContext(Dispatchers.IO) {
                try {
                    val inputStream = context.contentResolver.openInputStream(Uri.parse(uriString))
                    bitmap = BitmapFactory.decodeStream(inputStream)?.asImageBitmap()
                } catch (e: Exception) { e.printStackTrace() }
            }
        }
    }

    if (bitmap != null) {
        Image(bitmap = bitmap!!, contentDescription = "Foto Vehículo", modifier = modifier, contentScale = ContentScale.Crop)
    } else {
        Icon(Icons.Rounded.DirectionsCar, contentDescription = null, modifier = modifier.padding(32.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GarageScreen(viewModel: NextDriveViewModel, onNavigateToAddVehicle: () -> Unit, onVehicleClick: (Long) -> Unit, onNavigateToUserGuide: () -> Unit) {
    val vehicles by viewModel.allVehicles.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = { TopAppBar(title = { Text("MI GARAJE", fontWeight = FontWeight.Bold) }, colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)) },
        floatingActionButton = { FloatingActionButton(onClick = onNavigateToAddVehicle, containerColor = MaterialTheme.colorScheme.primary) { Icon(Icons.Default.Add, "Añadir Vehículo") } }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {

            // LISTA DE VEHÍCULOS
            if (vehicles.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("Garaje vacío.\nPulsa + para registrar un vehículo.", textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(vehicles) { vehicle ->
                        Card(modifier = Modifier.fillMaxWidth().clickable { onVehicleClick(vehicle.id) }, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                            Column {
                                Box(modifier = Modifier.fillMaxWidth().height(160.dp).background(MaterialTheme.colorScheme.background), contentAlignment = Alignment.Center) {
                                    UriImageLoader(uriString = vehicle.imageUri, modifier = Modifier.fillMaxSize())
                                }
                                Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = "${vehicle.brand ?: ""} ${vehicle.model}".uppercase(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                        if (!vehicle.licensePlate.isNullOrBlank()) Text(text = vehicle.licensePlate, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                                    }
                                    Text(text = "${vehicle.currentKm ?: 0} KM", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // FOOTER (Guía y Github)
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Card(modifier = Modifier.fillMaxWidth().clickable { onNavigateToUserGuide() }, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                    Text(
                        text = "Mantenimientos • Kilometraje • Recordatorios • Documentación • Estadísticas",
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        textAlign = TextAlign.Center, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/carlosalarcongu/NextDrive.git"))) }) {
                    Text("REPOSITORIO EN GITHUB", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(modifier = Modifier.height(32.dp)) // Espacio para el FAB
            }
        }
    }
}