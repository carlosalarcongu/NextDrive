// VehicleDashboardScreen.kt
package com.carlosalarcongu.nextdrive.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleDashboardScreen(
    vehicleId: Long,
    viewModel: NextDriveViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToExpenses: (Long) -> Unit,
    onNavigateToEdit: (Long) -> Unit,
    onNavigateToDocuments: (Long) -> Unit = {},
    onNavigateToGraphs: (Long) -> Unit = {}
) {
    val vehicle by viewModel.getVehicleById(vehicleId).collectAsState(initial = null)
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }
    var deleteValidationText by remember { mutableStateOf("") }

    if (vehicle == null) return

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(vehicle?.model ?: "") },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "") } },
                actions = {
                    IconButton(onClick = { onNavigateToEdit(vehicleId) }) {
                        Icon(Icons.Default.Edit, "")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.DirectionsCar, "", modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.primary)
                FilledIconButton(
                    onClick = { Toast.makeText(context, "Próximamente", Toast.LENGTH_SHORT).show() },
                    modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp)
                ) {
                    Icon(Icons.Default.PhotoCamera, "")
                }
            }

            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("${vehicle?.brand ?: ""} ${vehicle?.model}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    if (!vehicle?.licensePlate.isNullOrBlank()) Text("Matrícula: ${vehicle?.licensePlate}", style = MaterialTheme.typography.titleMedium)
                    if (!vehicle?.vin.isNullOrBlank()) Text("Bastidor: ${vehicle?.vin}", style = MaterialTheme.typography.bodyMedium)
                    Text("Km: ${vehicle?.currentKm ?: "0"} | Combustible: ${vehicle?.fuelType ?: "N/D"}")
                }
            }

            Text("Paneles", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

            DashboardAccessCard("Gastos y Mantenimiento", Icons.Default.Receipt) { onNavigateToExpenses(vehicleId) }
            DashboardAccessCard("Documentación", Icons.Default.Folder) { onNavigateToDocuments(vehicleId) }
            DashboardAccessCard("Gráficos y Consumos", Icons.Default.BarChart) { onNavigateToGraphs(vehicleId) }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { showDeleteDialog = true }, modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.Default.Delete, "", modifier = Modifier.padding(end = 8.dp))
                Text("Eliminar Vehículo")
            }
        }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false; deleteValidationText = "" },
                title = { Text("¿Eliminar?") },
                text = {
                    Column {
                        Text("Escribe ELIMINAR para confirmar.")
                        OutlinedTextField(value = deleteValidationText, onValueChange = { deleteValidationText = it }, singleLine = true)
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            vehicle?.let { viewModel.deleteVehicle(it) }
                            showDeleteDialog = false
                            onNavigateBack()
                        },
                        enabled = deleteValidationText == "ELIMINAR",
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) { Text("Eliminar") }
                },
                dismissButton = { TextButton(onClick = { showDeleteDialog = false; deleteValidationText = "" }) { Text("Cancelar") } }
            )
        }
    }
}

@Composable
fun DashboardAccessCard(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, "", modifier = Modifier.size(32.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(16.dp))
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}