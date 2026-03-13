package com.carlosalarcongu.nextdrive.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.carlosalarcongu.nextdrive.data.Expense
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpensePanelScreen(
    vehicleId: Long,
    viewModel: NextDriveViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToAddExpense: (Long) -> Unit
) {
    val expenses by viewModel.getExpensesForVehicle(vehicleId).collectAsState(initial = emptyList())

    // Variables de Búsqueda y Ordenación
    var searchQuery by remember { mutableStateOf("") }
    val sortOptions = listOf("Fecha", "Precio (+ a -)", "Precio (- a +)", "Grupo")
    var selectedSort by remember { mutableStateOf(sortOptions[0]) }

    // Procesado mágico en tiempo real: Filtra primero y luego ordena
    val filteredAndSortedExpenses = remember(expenses, searchQuery, selectedSort) {
        val filtered = if (searchQuery.isBlank()) expenses else {
            expenses.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                        it.groupName?.contains(searchQuery, ignoreCase = true) == true ||
                        it.comment?.contains(searchQuery, ignoreCase = true) == true
            }
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
                title = { Text("Listado de Gastos") },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver") } }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { onNavigateToAddExpense(vehicleId) },
                icon = { Icon(Icons.Default.Add, "Añadir") },
                text = { Text("Nuevo Gasto") }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            // Resumen Total
            Text(
                text = "Gasto Listado: ${"%.2f".format(totalGasto)} €",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            // Barra de Búsqueda
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                placeholder = { Text("Buscar gasto, grupo o comentario...") },
                leadingIcon = { Icon(Icons.Default.Search, "Buscar") },
                singleLine = true
            )

            // Chips de Ordenación
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp).horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Ordenar por:", modifier = Modifier.align(Alignment.CenterVertically), style = MaterialTheme.typography.labelLarge)
                sortOptions.forEach { option ->
                    FilterChip(
                        selected = selectedSort == option,
                        onClick = { selectedSort = option },
                        label = { Text(option) }
                    )
                }
            }

            Divider()

            // Lista renderizada
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredAndSortedExpenses) { expense ->
                    ExpenseDetailedCard(expense)
                }
            }
        }
    }
}

@Composable
fun ExpenseDetailedCard(expense: Expense) {
    val icon = when(expense.category) {
        "Repostaje" -> Icons.Default.LocalGasStation
        "Consumible" -> Icons.Default.Opacity
        else -> Icons.Default.Build
    }

    // Formateador de fecha nativo
    val dateString = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(expense.dateMillis))

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(40.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Título y Fecha
                Text(expense.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text(dateString, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                Spacer(modifier = Modifier.height(4.dp))

                // Grupo y Comentario (Trunkado si es muy largo)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SuggestionChip(
                        onClick = {},
                        label = { Text(expense.groupName ?: "General", style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier.height(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    if (!expense.comment.isNullOrBlank()) {
                        Text(
                            text = "\"${expense.comment}\"",
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1, // Solo muestra la primera línea
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Precio destacado
            Text("${expense.totalCost} €", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
    }
}