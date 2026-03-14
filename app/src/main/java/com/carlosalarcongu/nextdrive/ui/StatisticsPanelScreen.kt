// StatisticsPanelScreen.kt
package com.carlosalarcongu.nextdrive.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsPanelScreen(vehicleId: Long, viewModel: NextDriveViewModel, onNavigateBack: () -> Unit) {
    val expenses by viewModel.getExpensesForVehicle(vehicleId).collectAsState(initial = emptyList())
    val total = expenses.sumOf { it.totalCost }
    val grouped = expenses.groupBy { it.category }.mapValues { it.value.sumOf { exp -> exp.totalCost } }

    Scaffold(topBar = { TopAppBar(title = { Text("ESTADÍSTICAS") }, navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "") } }) }) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Coste Total: ${"%.2f".format(total)} €", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            HorizontalDivider()
            Text("Gasto por Categoría:", style = MaterialTheme.typography.titleMedium)
            grouped.forEach { (cat, cost) ->
                val progress = if (total > 0) (cost / total).toFloat() else 0f
                Column {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(cat)
                        Text("${"%.2f".format(cost)} €")
                    }
                    LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().height(8.dp), color = MaterialTheme.colorScheme.primary, trackColor = MaterialTheme.colorScheme.surfaceVariant)
                }
            }
        }
    }
}