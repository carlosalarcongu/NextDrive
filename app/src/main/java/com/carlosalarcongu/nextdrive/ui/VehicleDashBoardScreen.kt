package com.carlosalarcongu.nextdrive.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun VehicleDashboardScreen(
    vehicleId: Long, viewModel: NextDriveViewModel, onNavigateBack: () -> Unit,
    onNavigateToEdit: (Long) -> Unit, onNavigateToDocuments: (Long) -> Unit,
    onNavigateToAdd: (Long, String) -> Unit, onNavigateToEditExpense: (Long, String, Long) -> Unit
) {
    val vehicle by viewModel.getVehicleById(vehicleId).collectAsState(initial = null)
    val expenses by viewModel.getExpensesForVehicle(vehicleId).collectAsState(initial = emptyList())
    val haptic = LocalHapticFeedback.current
    var isFabExpanded by remember { mutableStateOf(false) }

    var selectedExpenseIds by remember { mutableStateOf(setOf<Long>()) }

    if (vehicle == null) return

    Scaffold(
        topBar = {
            if (selectedExpenseIds.isNotEmpty()) {
                TopAppBar(
                    title = { Text("${selectedExpenseIds.size} seleccionados") },
                    navigationIcon = { IconButton(onClick = { selectedExpenseIds = emptySet() }) { Icon(Icons.Default.Close, "Cancelar") } },
                    actions = {
                        IconButton(onClick = {
                            viewModel.softDeleteMultipleExpenses(selectedExpenseIds.toList())
                            selectedExpenseIds = emptySet()
                        }) { Icon(Icons.Default.Delete, "Borrar", tint = MaterialTheme.colorScheme.error) }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                )
            } else {
                TopAppBar(
                    title = { Text(vehicle?.model?.uppercase() ?: "") },
                    navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "") } },
                    actions = { IconButton(onClick = { onNavigateToEdit(vehicleId) }) { Icon(Icons.Default.Edit, "") } }
                )
            }
        },
        floatingActionButton = {
            if (selectedExpenseIds.isEmpty()) {
                Column(horizontalAlignment = Alignment.End) {
                    if (isFabExpanded) {
                        FabMenuItem("Trámites", Icons.Default.Assignment) { onNavigateToAdd(vehicleId, "Trámites"); isFabExpanded = false }
                        FabMenuItem("Avería", Icons.Default.CarCrash) { onNavigateToAdd(vehicleId, "Avería"); isFabExpanded = false }
                        FabMenuItem("Mantenimiento", Icons.Default.Handyman) { onNavigateToAdd(vehicleId, "Mantenimiento"); isFabExpanded = false }
                        FabMenuItem("Repostaje", Icons.Default.LocalGasStation) { onNavigateToAdd(vehicleId, "Repostaje"); isFabExpanded = false }
                        FabMenuItem("Pieza", Icons.Default.Build) { onNavigateToAdd(vehicleId, "Pieza"); isFabExpanded = false }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    FloatingActionButton(onClick = { isFabExpanded = !isFabExpanded }, containerColor = MaterialTheme.colorScheme.primary) {
                        Icon(if (isFabExpanded) Icons.Default.Close else Icons.Default.Add, "Desplegar")
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(paddingValues), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item {
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        val displayName = vehicle?.nickname?.takeIf { it.isNotBlank() } ?: "${vehicle?.brand ?: ""} ${vehicle?.model}"
                        Text(displayName.uppercase(), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("KM: ${vehicle?.currentKm ?: "0"} | ${vehicle?.fuelType?.uppercase() ?: "N/D"}", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                    }
                }
            }
            item {
                Button(onClick = { onNavigateToDocuments(vehicleId) }, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(8.dp), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)) {
                    Icon(Icons.Default.Folder, ""); Spacer(Modifier.width(8.dp)); Text("DOCUMENTACIÓN Y PAPELES", fontWeight = FontWeight.Bold)
                }
            }

            item { Text("HISTORIAL DE GASTOS", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 16.dp)) }

            items(expenses) { expense ->
                val isSelected = selectedExpenseIds.contains(expense.id)
                val cardColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else getCategoryColor(expense.category)

                Card(
                    modifier = Modifier.fillMaxWidth().combinedClickable(
                        onClick = {
                            if (selectedExpenseIds.isNotEmpty()) {
                                selectedExpenseIds = if (isSelected) selectedExpenseIds - expense.id else selectedExpenseIds + expense.id
                            } else {
                                onNavigateToEditExpense(vehicleId, expense.category, expense.id)
                            }
                        },
                        onLongClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            selectedExpenseIds = if (isSelected) selectedExpenseIds - expense.id else selectedExpenseIds + expense.id
                        }
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 8.dp else 2.dp),
                    colors = CardDefaults.cardColors(containerColor = cardColor)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        if (isSelected) {
                            Icon(Icons.Default.CheckCircle, "", modifier = Modifier.size(40.dp), tint = MaterialTheme.colorScheme.primary)
                        } else {
                            // ¡CORRECCIÓN! Usamos el nuevo nombre único
                            val icon = DashboardIconMap[expense.iconName] ?: Icons.Default.Build
                            Icon(icon, "", modifier = Modifier.size(40.dp), tint = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(expense.title.uppercase(), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Text(expense.category, style = MaterialTheme.typography.bodySmall)
                        }
                        Text("${expense.totalCost} €", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun FabMenuItem(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 12.dp)) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.padding(end = 12.dp),
            shadowElevation = 2.dp
        ) {
            Text(
                text = title,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
        }
        SmallFloatingActionButton(
            onClick = onClick,
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ) {
            Icon(icon, contentDescription = title)
        }
    }
}

private fun getCategoryColor(category: String): androidx.compose.ui.graphics.Color {
    val baseColor = when (category) {
        "Pieza" -> androidx.compose.ui.graphics.Color.Gray
        "Repostaje" -> androidx.compose.ui.graphics.Color(0xFF4CAF50)
        "Mantenimiento" -> androidx.compose.ui.graphics.Color(0xFFFF9800)
        "Avería" -> androidx.compose.ui.graphics.Color(0xFFF44336)
        "Trámites" -> androidx.compose.ui.graphics.Color(0xFF2196F3)
        else -> androidx.compose.ui.graphics.Color(0xFF424242)
    }
    return baseColor.copy(alpha = 0.15f)
}

// ¡CAMBIADO EL NOMBRE A DASHBOARD ICON MAP!
private val DashboardIconMap = mapOf(
    "Herramientas" to Icons.Default.Build,
    "Reparación" to Icons.Default.CarCrash,
    "Gasolinera" to Icons.Default.LocalGasStation,
    "Documento" to Icons.Default.Assignment
)