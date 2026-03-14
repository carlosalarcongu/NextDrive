package com.carlosalarcongu.nextdrive.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.background
import androidx.compose.ui.Alignment

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsPanelScreen(vehicleId: Long, viewModel: NextDriveViewModel, onNavigateBack: () -> Unit) {
    val expenses by viewModel.getExpensesForVehicle(vehicleId).collectAsState(initial = emptyList())
    val total = expenses.sumOf { it.totalCost }
    val grouped = expenses.groupBy { it.category }.mapValues { it.value.sumOf { exp -> exp.totalCost } }.toList().sortedByDescending { it.second }

    val viewModes = listOf("BARRAS", "CIRCULAR", "TABLA")
    var selectedMode by remember { mutableStateOf(viewModes[0]) }
    val colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary, MaterialTheme.colorScheme.secondary)

    Scaffold(topBar = { TopAppBar(title = { Text("ESTADÍSTICAS") }, navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "") } }) }) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("COSTE TOTAL: ${"%.2f".format(total)} €", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                viewModes.forEach { mode -> FilterChip(selected = selectedMode == mode, onClick = { selectedMode = mode }, label = { Text(mode) }) }
            }

            Card(modifier = Modifier.fillMaxWidth().height(300.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                if (total == 0.0) {
                    Text("No hay datos", modifier = Modifier.padding(16.dp))
                } else {
                    when (selectedMode) {
                        "BARRAS" -> {
                            val maxVal = grouped.maxOfOrNull { it.second } ?: 1.0
                            Row(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.Bottom) {
                                grouped.forEachIndexed { index, pair ->
                                    val heightRatio = (pair.second / maxVal).toFloat()
                                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                        Text("${"%.0f".format(pair.second)}€", style = MaterialTheme.typography.labelSmall)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Box(modifier = Modifier.fillMaxWidth(0.6f).fillMaxHeight(heightRatio).background(colors[index % colors.size]))
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(pair.first.take(3), style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                        }
                        "CIRCULAR" -> {
                            Canvas(modifier = Modifier.fillMaxSize().padding(32.dp)) {
                                var startAngle = 0f
                                grouped.forEachIndexed { index, pair ->
                                    val sweepAngle = ((pair.second / total) * 360).toFloat()
                                    drawArc(color = colors[index % colors.size], startAngle = startAngle, sweepAngle = sweepAngle, useCenter = true, size = Size(size.minDimension, size.minDimension))
                                    startAngle += sweepAngle
                                }
                            }
                        }
                        "TABLA" -> {
                            Column(modifier = Modifier.padding(16.dp)) {
                                grouped.forEachIndexed { index, pair ->
                                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Row { Box(modifier = Modifier.size(16.dp).background(colors[index % colors.size])); Spacer(modifier = Modifier.width(8.dp)); Text(pair.first) }
                                        Text("${"%.2f".format(pair.second)} €", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}