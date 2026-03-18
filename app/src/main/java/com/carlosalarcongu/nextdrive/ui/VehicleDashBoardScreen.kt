// VehicleDashboardScreen.kt
package com.carlosalarcongu.nextdrive.ui

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleDashboardScreen(
    vehicleId: Long,
    viewModel: NextDriveViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToExpenses: (Long) -> Unit,
    onNavigateToEdit: (Long) -> Unit,
    onNavigateToDocuments: (Long) -> Unit,
    onNavigateToGraphs: (Long) -> Unit,
    onNavigateToAddRepostaje: (Long) -> Unit // ¡NUEVO PARÁMETRO!
) {
    val vehicle by viewModel.getVehicleById(vehicleId).collectAsState(initial = null)
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }
    var deleteValidationText by remember { mutableStateOf("") }
    var showAddKmDialog by remember { mutableStateOf(false) }
    var addKmText by remember { mutableStateOf("") }

    val photoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            // Guardamos permiso persistente para poder leer la foto tras reiniciar la app
            context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            viewModel.updateVehicle(vehicle!!.copy(imageUri = it.toString()))
        }
    }

    if (vehicle == null) return

    Scaffold(
        topBar = { TopAppBar(title = { Text(vehicle?.model?.uppercase() ?: "") }, navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "") } }, actions = { IconButton(onClick = { onNavigateToEdit(vehicleId) }) { Icon(Icons.Default.Edit, "") } }) }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {

            Box(modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(6.dp)).background(MaterialTheme.colorScheme.background), contentAlignment = Alignment.Center) {
                VehicleImageLoader(vehicle = vehicle!!, modifier = Modifier.fillMaxSize())

                FilledIconButton(onClick = { photoLauncher.launch(arrayOf("image/*")) }, modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp), colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.primary)) {
                    Icon(Icons.Default.PhotoCamera, "Cambiar Foto")
                }
            }

            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    val displayName = vehicle?.nickname?.takeIf { it.isNotBlank() } ?: "${vehicle?.brand ?: ""} ${vehicle?.model}"
                    Text(displayName.uppercase(), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

                    Spacer(modifier = Modifier.height(8.dp))
                    if (!vehicle?.licensePlate.isNullOrBlank()) Text("MATRÍCULA: ${vehicle?.licensePlate}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                    if (!vehicle?.vin.isNullOrBlank()) Text("BASTIDOR: ${vehicle?.vin}", style = MaterialTheme.typography.bodyMedium)

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("KM: ${vehicle?.currentKm ?: "0"} | ${vehicle?.fuelType?.uppercase() ?: "N/D"}", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)

                        // ¡AQUÍ ESTÁN LOS DOS BOTONES JUNTOS!
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilledTonalIconButton(onClick = { onNavigateToAddRepostaje(vehicleId) }) {
                                Icon(Icons.Default.LocalGasStation, "Añadir Repostaje")
                            }
                            FilledTonalIconButton(onClick = { showAddKmDialog = true }) {
                                Icon(Icons.Default.AddRoad, "Añadir Km")
                            }
                        }
                    }
                }
            }

            Text("PANELES", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

            DashboardAccessCard("GASTOS Y MANTENIMIENTO", Icons.Default.Receipt) { onNavigateToExpenses(vehicleId) }
            DashboardAccessCard("DOCUMENTACIÓN", Icons.Default.Folder) { onNavigateToDocuments(vehicleId) }
            DashboardAccessCard("ESTADÍSTICAS Y GRÁFICOS", Icons.Default.BarChart) { onNavigateToGraphs(vehicleId) }

            Spacer(modifier = Modifier.weight(1f))

            Button(onClick = { showDeleteDialog = true }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                Icon(Icons.Default.Delete, "", modifier = Modifier.padding(end = 8.dp))
                Text("ELIMINAR VEHÍCULO")
            }
        }

        if (showAddKmDialog) {
            AlertDialog(
                onDismissRequest = { showAddKmDialog = false; addKmText = "" }, title = { Text("AÑADIR KILÓMETROS") },
                text = { OutlinedTextField(value = addKmText, onValueChange = { addKmText = it.filter { char -> char.isDigit() } }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), label = { Text("Km a sumar") }, singleLine = true) },
                confirmButton = { Button(onClick = { val km = addKmText.toIntOrNull() ?: 0; if(km > 0) { viewModel.updateVehicle(vehicle!!.copy(currentKm = (vehicle!!.currentKm ?: 0) + km)) }; showAddKmDialog = false; addKmText = "" }) { Text("SUMAR") } },
                dismissButton = { TextButton(onClick = { showAddKmDialog = false; addKmText = "" }) { Text("CANCELAR") } }
            )
        }

        if (showDeleteDialog) {
            AlertDialog(onDismissRequest = { showDeleteDialog = false; deleteValidationText = "" }, title = { Text("¿ELIMINAR?") }, text = { Column { Text("Escribe ELIMINAR para confirmar."); OutlinedTextField(value = deleteValidationText, onValueChange = { deleteValidationText = it }, singleLine = true) } }, confirmButton = { Button(onClick = { vehicle?.let { viewModel.deleteVehicle(it) }; showDeleteDialog = false; onNavigateBack() }, enabled = deleteValidationText == "ELIMINAR", colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { Text("ELIMINAR") } }, dismissButton = { TextButton(onClick = { showDeleteDialog = false; deleteValidationText = "" }) { Text("CANCELAR") } })
        }
    }
}

@Composable
fun DashboardAccessCard(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), onClick = onClick, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, "", modifier = Modifier.size(32.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(16.dp))
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}