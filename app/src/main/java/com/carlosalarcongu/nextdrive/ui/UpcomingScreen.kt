package com.carlosalarcongu.nextdrive.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpcomingScreen(viewModel: NextDriveViewModel, onAttend: (Long, String) -> Unit, onEdit: (Long, Long, String) -> Unit) {
    val allExpenses by viewModel.allExpenses.collectAsState()
    val allVehicles by viewModel.allVehicles.collectAsState()

    // Filtramos solo los que tienen recordatorio
    val reminders = remember(allExpenses) {
        allExpenses.filter { it.hasReminder }.sortedBy { it.reminderDateMillis ?: Long.MAX_VALUE }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("PRÓXIMOS MANTENIMIENTOS", fontWeight = FontWeight.Bold) }, colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)) }
    ) { padding ->
        if (reminders.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No hay mantenimientos planificados.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(reminders) { exp ->
                    val vehicle = allVehicles.find { it.id == exp.vehicleId }
                    val vName = vehicle?.nickname ?: "${vehicle?.brand ?: ""} ${vehicle?.model}"

                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Warning, "", tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(exp.title.uppercase(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                Text(vName, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Spacer(modifier = Modifier.height(8.dp))

                            val creationDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(exp.dateMillis))
                            Text("Registrado el $creationDate con ${exp.registeredKm ?: "?"} km", style = MaterialTheme.typography.bodySmall)

                            if (exp.reminderDateMillis != null) {
                                val targetDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(exp.reminderDateMillis))
                                Text("Aviso programado para: $targetDate", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            } else if (exp.reminderKm != null) {
                                Text("Aviso programado a los: ${exp.reminderKm} km", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                val remaining = exp.reminderKm - (vehicle?.currentKm ?: 0)
                                Text("Faltan $remaining km", style = MaterialTheme.typography.labelSmall)
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                                TextButton(onClick = { onEdit(exp.vehicleId, exp.id, exp.category) }) {
                                    Icon(Icons.Default.Edit, "", modifier = Modifier.size(18.dp)); Spacer(Modifier.width(4.dp)); Text("MODIFICAR")
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(onClick = { onAttend(exp.vehicleId, exp.title) }) {
                                    Icon(Icons.Default.Check, "", modifier = Modifier.size(18.dp)); Spacer(Modifier.width(4.dp)); Text("ATENDER AHORA")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}