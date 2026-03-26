package com.carlosalarcongu.nextdrive.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.carlosalarcongu.nextdrive.data.ServiceInterval
import com.carlosalarcongu.nextdrive.data.predefinedServices

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceIntervalsScreen(vehicleId: Long, viewModel: NextDriveViewModel, onNavigateBack: () -> Unit) {
    val customIntervals by viewModel.getServiceIntervalsForVehicle(vehicleId).collectAsState(initial = emptyList())

    var editingService by remember { mutableStateOf<String?>(null) }
    var kmInput by remember { mutableStateOf("") }
    var monthsInput by remember { mutableStateOf("") }
    var currentIntervalId by remember { mutableStateOf(0L) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("PROGRAMACIONES", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "") } }
            )
        }
    ) { paddingValues ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(paddingValues), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Text("Ajusta cada cuánto tiempo o kilometraje quieres que la app te avise por defecto para este coche.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(predefinedServices) { service ->
                val custom = customIntervals.find { it.serviceName == service.name }
                val displayKm = custom?.defaultKm ?: service.defaultKm
                val displayMo = custom?.defaultMonths ?: service.defaultMonths

                Card(
                    modifier = Modifier.fillMaxWidth().clickable {
                        editingService = service.name
                        kmInput = displayKm.toString()
                        monthsInput = displayMo.toString()
                        currentIntervalId = custom?.id ?: 0L
                    },
                    colors = CardDefaults.cardColors(
                        containerColor = if (custom != null) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(service.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                            Text("Avisar cada $displayKm km o $displayMo meses", style = MaterialTheme.typography.bodyMedium)
                        }
                        Icon(Icons.Default.Edit, "Editar", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }

        if (editingService != null) {
            AlertDialog(
                onDismissRequest = { editingService = null },
                title = { Text("Personalizar Aviso") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(editingService!!, fontWeight = FontWeight.Bold)
                        OutlinedTextField(
                            value = kmInput, onValueChange = { kmInput = it.filter { c -> c.isDigit() } },
                            label = { Text("Kilómetros (0 = No avisar)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword)
                        )
                        OutlinedTextField(
                            value = monthsInput, onValueChange = { monthsInput = it.filter { c -> c.isDigit() } },
                            label = { Text("Meses (0 = No avisar)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword)
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        val km = kmInput.toIntOrNull() ?: 0
                        val mo = monthsInput.toIntOrNull() ?: 0
                        viewModel.saveServiceInterval(ServiceInterval(id = currentIntervalId, vehicleId = vehicleId, serviceName = editingService!!, defaultKm = km, defaultMonths = mo))
                        editingService = null
                    }) { Text("GUARDAR") }
                },
                dismissButton = { TextButton(onClick = { editingService = null }) { Text("Cancelar") } }
            )
        }
    }
}