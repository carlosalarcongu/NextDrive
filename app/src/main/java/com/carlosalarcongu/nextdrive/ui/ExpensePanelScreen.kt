package com.carlosalarcongu.nextdrive.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.carlosalarcongu.nextdrive.data.Expense
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Función para asignar un color suave/transparente a cada tipo de gasto
@Composable
fun getCategoryColor(category: String): Color {
    val baseColor = when (category) {
        "Pieza" -> Color.Gray
        "Repostaje" -> Color(0xFF4CAF50) // Verde
        "Mantenimiento" -> Color(0xFFFF9800) // Naranja
        "Avería" -> MaterialTheme.colorScheme.error // Rojo
        "Trámites" -> Color(0xFF2196F3) // Azul
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    // Aplicamos una opacidad muy baja (15%) para que no sature la vista
    return if (baseColor == MaterialTheme.colorScheme.surfaceVariant) baseColor else baseColor.copy(alpha = 0.15f)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpensePanelScreen(vehicleId: Long, viewModel: NextDriveViewModel, onNavigateBack: () -> Unit, onNavigateToAdd: (Long, String) -> Unit, onNavigateToEdit: (Long, String, Long) -> Unit) {
    val expenses by viewModel.getExpensesForVehicle(vehicleId).collectAsState(initial = emptyList())
    var searchQuery by remember { mutableStateOf("") }
    val sortOptions = listOf("Fecha", "Precio (+ a -)", "Precio (- a +)", "Grupo")
    var selectedSort by remember { mutableStateOf(sortOptions[0]) }

    // Estado para el menú flotante
    var isFabExpanded by remember { mutableStateOf(false) }

    val filteredAndSortedExpenses = remember(expenses, searchQuery, selectedSort) {
        val filtered = if (searchQuery.isBlank()) expenses else {
            expenses.filter { it.title.contains(searchQuery, ignoreCase = true) || it.groupName?.contains(searchQuery, ignoreCase = true) == true || it.comment?.contains(searchQuery, ignoreCase = true) == true }
        }
        when (selectedSort) {
            "Fecha" -> filtered.sortedByDescending { it.dateMillis }
            "Precio (+ a -)" -> filtered.sortedByDescending { it.totalCost }
            "Precio (- a +)" -> filtered.sortedBy { it.totalCost }
            "Grupo" -> filtered.sortedBy { it.groupName ?: "ZZZ" }
            else -> filtered
        }
    }

    val totalGasto = filteredAndSortedExpenses.sumOf { it.totalCost }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("GESTIÓN DE GASTOS") },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver") } }
            )
        },
        floatingActionButton = {
            // MENÚ FLOTANTE DESPLEGABLE (Abajo a la derecha)
            Column(horizontalAlignment = Alignment.End) {
                if (isFabExpanded) {
                    FabMenuItem("Trámites", Icons.Default.Assignment) { onNavigateToAdd(vehicleId, "Trámites"); isFabExpanded = false }
                    FabMenuItem("Avería", Icons.Default.CarCrash) { onNavigateToAdd(vehicleId, "Avería"); isFabExpanded = false }
                    FabMenuItem("Mantenimiento", Icons.Default.Handyman) { onNavigateToAdd(vehicleId, "Mantenimiento"); isFabExpanded = false }
                    FabMenuItem("Repostaje", Icons.Default.LocalGasStation) { onNavigateToAdd(vehicleId, "Repostaje"); isFabExpanded = false }
                    FabMenuItem("Pieza", Icons.Default.Build) { onNavigateToAdd(vehicleId, "Pieza"); isFabExpanded = false }
                    Spacer(modifier = Modifier.height(16.dp))
                }
                FloatingActionButton(
                    onClick = { isFabExpanded = !isFabExpanded },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        imageVector = if (isFabExpanded) Icons.Default.Close else Icons.Default.Add,
                        contentDescription = "Desplegar menú"
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            Text(text = "TOTAL HISTÓRICO: ${"%.2f".format(totalGasto)} €", modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            OutlinedTextField(value = searchQuery, onValueChange = { searchQuery = it }, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), placeholder = { Text("Buscar en el historial...") }, leadingIcon = { Icon(Icons.Default.Search, "") }, singleLine = true)

            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp).horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Ordenar:", modifier = Modifier.align(Alignment.CenterVertically), style = MaterialTheme.typography.labelLarge)
                sortOptions.forEach { option -> FilterChip(selected = selectedSort == option, onClick = { selectedSort = option }, label = { Text(option) }) }
            }
            HorizontalDivider()

            LazyColumn(contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 80.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(filteredAndSortedExpenses) { expense ->
                    ExpenseDetailedCard(expense, onClick = { onNavigateToEdit(vehicleId, expense.category, expense.id) })
                }
            }
        }
    }
}

// Sub-componente para cada botón del menú flotante
@Composable
fun FabMenuItem(title: String, icon: ImageVector, onClick: () -> Unit) {
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
fun ExpenseDetailedCard(expense: Expense, onClick: () -> Unit) {
    val icon = ExpenseIconMap[expense.iconName] ?: Icons.Default.Build
    val dateString = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(expense.dateMillis))

    // Obtenemos el color translúcido de la categoría
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
                        Text(text = "\"${expense.comment}\"", style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
            Text("${expense.totalCost} €", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
    }
}