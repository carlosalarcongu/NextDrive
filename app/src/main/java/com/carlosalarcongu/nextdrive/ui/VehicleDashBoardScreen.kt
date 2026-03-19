package com.carlosalarcongu.nextdrive.ui

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun VehicleDashboardScreen(
    vehicleId: Long, viewModel: NextDriveViewModel, onNavigateBack: () -> Unit,
    onNavigateToEdit: (Long) -> Unit, onNavigateToDocuments: (Long) -> Unit,
    onNavigateToAdd: (Long, String) -> Unit, onNavigateToEditExpense: (Long, String, Long) -> Unit
) {
    val vehicle by viewModel.getVehicleById(vehicleId).collectAsState(initial = null)
    val expenses by viewModel.getExpensesForVehicle(vehicleId).collectAsState(initial = emptyList())
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    var isFabExpanded by remember { mutableStateOf(false) }

    // ESTADO PARA MULTISELECCIÓN
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
                            viewModel.deleteMultipleExpenses(selectedExpenseIds.toList())
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
            // ... (Vehicle Card y Documentos igual que antes) ...
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
                            val icon = ExpenseIconMap[expense.iconName] ?: Icons.Default.Build
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

// --- COMPONENTES VISUALES MOVIDOS DESDE EL ANTIGUO PANEL DE GASTOS ---

@Composable
fun FabMenuItem(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
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

@Composable
fun getCategoryColor(category: String): androidx.compose.ui.graphics.Color {
    val baseColor = when (category) {
        "Pieza" -> androidx.compose.ui.graphics.Color.Gray
        "Repostaje" -> androidx.compose.ui.graphics.Color(0xFF4CAF50) // Verde
        "Mantenimiento" -> androidx.compose.ui.graphics.Color(0xFFFF9800) // Naranja
        "Avería" -> MaterialTheme.colorScheme.error // Rojo
        "Trámites" -> androidx.compose.ui.graphics.Color(0xFF2196F3) // Azul
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    // Transparencia del 15% para que sea sutil
    return if (baseColor == MaterialTheme.colorScheme.surfaceVariant) baseColor else baseColor.copy(alpha = 0.15f)
}

@Composable
fun ExpenseDetailedCard(expense: com.carlosalarcongu.nextdrive.data.Expense, onClick: () -> Unit) {
    val icon = ExpenseIconMap[expense.iconName] ?: Icons.Default.Build
    val dateString = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(java.util.Date(expense.dateMillis))

    val cardColor = getCategoryColor(expense.category)

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(40.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(expense.title.uppercase(), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text("${expense.category} • $dateString", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!expense.groupName.isNullOrBlank()) {
                        SuggestionChip(onClick = {}, label = { Text(expense.groupName, style = MaterialTheme.typography.labelSmall) }, modifier = Modifier.height(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    if (!expense.comment.isNullOrBlank()) {
                        Text(text = "\"${expense.comment}\"", style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                    }
                }
            }
            Text("${expense.totalCost} €", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
    }
}