package com.carlosalarcongu.nextdrive.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpcomingScreen(viewModel: NextDriveViewModel, onAttend: (Long, String) -> Unit, onEdit: (Long, Long, String) -> Unit, onNavigateToSettings: () -> Unit) {
    val allExpenses by viewModel.allExpenses.collectAsState()
    val allVehicles by viewModel.allVehicles.collectAsState()

    // Filtramos los que tienen recordatorio, NO están en la papelera y NO han sido atendidos
    val reminders = remember(allExpenses) {
        allExpenses.filter { it.hasReminder && !it.isDeleted && !it.isAttended }
            .sortedBy { it.reminderDateMillis ?: Long.MAX_VALUE }
    }

    Scaffold(
        topBar = {
            Surface(shadowElevation = 4.dp) {
                TopAppBar(
                    title = { Text("PRÓXIMOS", fontWeight = FontWeight.Bold) },
                    actions = { IconButton(onClick = onNavigateToSettings) { Icon(Icons.Default.Settings, "Ajustes") } },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
                )
            }
        }
    ) { padding ->
        if (reminders.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No hay mantenimientos pendientes.", color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                            Spacer(modifier = Modifier.height(12.dp))

                            // SEMÁFOROS VISUALES (Ahora muestra ambos si existen)
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                if (exp.reminderDateMillis != null) {
                                    val remainingDays = ((exp.reminderDateMillis - System.currentTimeMillis()) / (1000 * 60 * 60 * 24)).toInt()
                                    val statusColor = when {
                                        remainingDays < 0 -> MaterialTheme.colorScheme.error // Rojo
                                        remainingDays <= 15 -> Color(0xFFFF9800) // Naranja
                                        else -> Color(0xFF4CAF50) // Verde
                                    }
                                    val statusText = if (remainingDays < 0) "VENCIDO HACE ${-remainingDays} DÍAS" else "FALTAN $remainingDays DÍAS"

                                    val targetDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(exp.reminderDateMillis))
                                    Column {
                                        Text("Aviso programado para: $targetDate", style = MaterialTheme.typography.bodyMedium)
                                        Box(modifier = Modifier.padding(top = 4.dp).clip(RoundedCornerShape(4.dp)).background(statusColor.copy(alpha = 0.2f)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                                            Text(statusText, color = statusColor, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                                        }
                                    }
                                }

                                if (exp.reminderKm != null) {
                                    val remainingKm = exp.reminderKm - (vehicle?.currentKm ?: 0)
                                    val statusColor = when {
                                        remainingKm < 0 -> MaterialTheme.colorScheme.error
                                        remainingKm <= 1000 -> Color(0xFFFF9800)
                                        else -> Color(0xFF4CAF50)
                                    }
                                    val statusText = if (remainingKm < 0) "TE HAS PASADO POR ${-remainingKm} KM" else "FALTAN $remainingKm KM"

                                    Column {
                                        Text("Aviso programado a los: ${exp.reminderKm} km", style = MaterialTheme.typography.bodyMedium)
                                        Box(modifier = Modifier.padding(top = 4.dp).clip(RoundedCornerShape(4.dp)).background(statusColor.copy(alpha = 0.2f)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                                            Text(statusText, color = statusColor, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                                        }
                                    }
                                }
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                                TextButton(onClick = { onEdit(exp.vehicleId, exp.id, exp.category) }) {
                                    Icon(Icons.Default.Edit, "", modifier = Modifier.size(18.dp)); Spacer(Modifier.width(4.dp)); Text("MODIFICAR")
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(onClick = { viewModel.markExpenseAsAttended(exp) }) {
                                    Icon(Icons.Default.Check, "", modifier = Modifier.size(18.dp)); Spacer(Modifier.width(4.dp)); Text("MARCAR ATENDIDO")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}