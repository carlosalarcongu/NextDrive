package com.carlosalarcongu.nextdrive.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.carlosalarcongu.nextdrive.data.Vehicle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GarageScreen(
    viewModel: NextDriveViewModel,
    onNavigateToAddVehicle: () -> Unit,
    onVehicleClick: (Long) -> Unit // NUEVO: Para navegar al panel
) {
    val vehicles by viewModel.allVehicles.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Garaje", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAddVehicle) {
                Icon(Icons.Default.Add, contentDescription = "Añadir Vehículo")
            }
        }
    ) { paddingValues ->
        if (vehicles.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text("Tu garaje está vacío.\nPulsa el botón + para empezar.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(vehicles) { vehicle ->
                    VehicleCard(
                        vehicle = vehicle,
                        onClick = { onVehicleClick(vehicle.id) },
                        onFavoriteToggle = {
                            // Invertimos el valor de isFavorite y actualizamos
                            viewModel.updateVehicle(vehicle.copy(isFavorite = !vehicle.isFavorite))
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun VehicleCard(vehicle: Vehicle, onClick: () -> Unit, onFavoriteToggle: () -> Unit) {
    // Selector de iconos según el tipo de vehículo
    val vehicleIcon: ImageVector = when (vehicle.type.uppercase()) {
        "MOTO" -> Icons.Default.TwoWheeler
        "SUV", "FURGO" -> Icons.Default.AirportShuttle
        "CAMIÓN" -> Icons.Default.LocalShipping
        "DEPORTIVO" -> Icons.Default.SportsMotorsports
        else -> Icons.Default.DirectionsCar // Turismo por defecto
    }

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }, // Hace que toda la tarjeta sea pulsable
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono del vehículo
            Icon(imageVector = vehicleIcon, contentDescription = "Tipo", modifier = Modifier.size(40.dp), tint = MaterialTheme.colorScheme.primary)

            Spacer(modifier = Modifier.width(16.dp))

            // Textos
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "${vehicle.brand ?: ""} ${vehicle.model}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(text = "${vehicle.type} | Km: ${vehicle.currentKm ?: "0"}", style = MaterialTheme.typography.bodyMedium)
            }

            // Botón de Favorito (Estrella)
            IconButton(onClick = onFavoriteToggle) {
                Icon(
                    imageVector = if (vehicle.isFavorite) Icons.Default.Star else Icons.Default.StarOutline,
                    contentDescription = "Favorito",
                    tint = if (vehicle.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}