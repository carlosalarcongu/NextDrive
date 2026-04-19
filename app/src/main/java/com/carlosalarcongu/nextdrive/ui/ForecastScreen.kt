package com.carlosalarcongu.nextdrive.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForecastScreen(
    viewModel: NextDriveViewModel,
    onNavigateBack: () -> Unit
) {
    val allExpenses by viewModel.allExpenses.collectAsState()
    val allVehicles by viewModel.allVehicles.collectAsState()

    // Estado para guardar qué vehículos están seleccionados para el filtro (vacío = Todos)
    var selectedVehicleIds by remember { mutableStateOf(setOf<Long>()) }

    // Calculamos el forecast reactivamente cada vez que cambian los gastos o el filtro
    val forecasts = remember(allExpenses, selectedVehicleIds) {
        viewModel.calculateForecast(allExpenses, selectedVehicleIds)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ESTIMACIÓN ANUAL", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background), windowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp)
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {

            // --- SELECTOR DE VEHÍCULOS (Filtro Superior) ---
            if (allVehicles.isNotEmpty()) {
                Text(
                    "Filtrar por Vehículo:",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    color = MaterialTheme.colorScheme.primary
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Botón "Todos"
                    FilterChip(
                        selected = selectedVehicleIds.isEmpty(),
                        onClick = { selectedVehicleIds = emptySet() },
                        label = { Text("Todos los vehículos") },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primary)
                    )

                    // Botones individuales por vehículo
                    allVehicles.forEach { vehicle ->
                        val isSelected = selectedVehicleIds.contains(vehicle.id)
                        val name = vehicle.nickname?.takeIf { it.isNotBlank() } ?: vehicle.model
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedVehicleIds = if (isSelected) {
                                    selectedVehicleIds - vehicle.id
                                } else {
                                    selectedVehicleIds + vehicle.id
                                }
                            },
                            label = { Text(name) }
                        )
                    }
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }

            // --- RESULTADOS DEL FORECAST ---
            if (forecasts.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                    Text("No hay suficientes datos para estimar.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(forecasts) { forecast ->
                        ForecastCard(forecast)
                    }
                }
            }
        }
    }
}

@Composable
fun ForecastCard(forecast: YearForecast) {
    val isCurrentYear = forecast.projectedExtra > 0.0

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Cabecera: Año y Total
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Insights, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "AÑO ${forecast.year}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    "${"%.2f".format(Locale.US, forecast.totalEstimated)} €",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Barra visual de proporción
            val total = forecast.totalEstimated.toFloat()
            val actualRatio = if (total > 0f) (forecast.actualSpent.toFloat() / total) else 0f

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(actualRatio)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.primary)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Desglose de números
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Gasto Realizado", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${"%.2f".format(Locale.US, forecast.actualSpent)} €", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                }

                if (isCurrentYear) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Proyección Esperada", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("+ ${"%.2f".format(Locale.US, forecast.projectedExtra)} €", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = Color(0xFFFF9800))
                    }
                } else {
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Estado", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("AÑO CERRADO", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
                    }
                }
            }
        }
    }
}