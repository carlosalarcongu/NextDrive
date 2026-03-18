package com.carlosalarcongu.nextdrive.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

// Modelo de datos simulado
data class GasStationMock(
    val name: String,
    val brand: String,
    val distance: String,
    val price95: Double,
    val price98: Double,
    val priceDiesel: Double,
    val mapX: Float, // Coordenadas relativas para el radar (-1.0 a 1.0)
    val mapY: Float
)

// Datos base simulados (Cercanos a Santander)
val mockStations = listOf(
    GasStationMock("Repsol Parayas", "Repsol", "1.2 km", 1.659, 1.789, 1.549, 0.2f, -0.4f),
    GasStationMock("Cepsa Valdecilla", "Cepsa", "2.5 km", 1.635, 1.775, 1.525, -0.5f, 0.3f),
    GasStationMock("Plenoil Peñacastillo", "Plenoil", "3.1 km", 1.489, 0.0, 1.399, -0.8f, -0.6f),
    GasStationMock("Galp Centro", "Galp", "0.8 km", 1.649, 1.769, 1.539, 0.1f, 0.2f),
    GasStationMock("Ballenoil Camargo", "Ballenoil", "4.0 km", 1.495, 0.0, 1.385, 0.7f, 0.7f)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FuelPricesScreen() {
    var viewMode by remember { mutableStateOf("LISTA") } // "LISTA" o "MAPA"
    var sortBy by remember { mutableStateOf("Gasolina 95") } // "Gasolina 95", "Gasolina 98", "Diésel"

    val sortedList = remember(sortBy) {
        when (sortBy) {
            "Gasolina 98" -> mockStations.filter { it.price98 > 0 }.sortedBy { it.price98 }
            "Diésel" -> mockStations.sortedBy { it.priceDiesel }
            else -> mockStations.sortedBy { it.price95 }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ESTACIONES DE SERVICIO", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {

            // SELECTOR DE VISTA (LISTA / MAPA)
            TabRow(
                selectedTabIndex = if (viewMode == "LISTA") 0 else 1,
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Tab(
                    selected = viewMode == "LISTA",
                    onClick = { viewMode = "LISTA" },
                    text = { Text("LISTADO", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.FormatListBulleted, "") }
                )
                Tab(
                    selected = viewMode == "MAPA",
                    onClick = { viewMode = "MAPA" },
                    text = { Text("RADAR", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Map, "") }
                )
            }

            if (viewMode == "LISTA") {
                // VISTA DE LISTA
                Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Ordenar precios por:", style = MaterialTheme.typography.labelLarge)
                    ExposedDropdownMenuBox(expanded = false, onExpandedChange = {}) {
                        // En Compose puro podemos usar chips para esto más fácilmente
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(selected = sortBy == "Gasolina 95", onClick = { sortBy = "Gasolina 95" }, label = { Text("95") })
                            FilterChip(selected = sortBy == "Diésel", onClick = { sortBy = "Diésel" }, label = { Text("Diésel") })
                        }
                    }
                }

                LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(sortedList) { station ->
                        GasStationCard(station, sortBy)
                    }
                }
            } else {
                // VISTA DE MAPA (RADAR SIMULADO)
                Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant)) {
                    // Fondo tipo radar
                    val primaryColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(color = primaryColor, radius = size.minDimension / 2.5f, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f))
                        drawCircle(color = primaryColor, radius = size.minDimension / 4f, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f))
                        drawLine(color = primaryColor, start = Offset(size.width / 2, 0f), end = Offset(size.width / 2, size.height), strokeWidth = 2f)
                        drawLine(color = primaryColor, start = Offset(0f, size.height / 2), end = Offset(size.width, size.height / 2), strokeWidth = 2f)
                    }

                    // Tu ubicación
                    Box(modifier = Modifier.align(Alignment.Center).size(24.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.MyLocation, "", tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(16.dp))
                    }

                    // Puntos de gasolineras
                    mockStations.forEach { station ->
                        Box(modifier = Modifier.align(BiasAlignment(station.mapX, station.mapY))) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                // Precio flotante destacado
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                    shadowElevation = 4.dp
                                ) {
                                    val currentPrice = when(sortBy) { "Gasolina 98" -> station.price98; "Diésel" -> station.priceDiesel; else -> station.price95 }
                                    if (currentPrice > 0) {
                                        Text("${currentPrice}€", fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelMedium)
                                    } else {
                                        Text("N/D", fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelMedium)
                                    }
                                }
                                // Icono / Logo (usamos un icono genérico con el color de la marca simulado)
                                Box(modifier = Modifier.padding(top = 4.dp).size(36.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surface), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.LocalGasStation, "", tint = MaterialTheme.colorScheme.primary)
                                }
                                Text(station.brand, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GasStationCard(station: GasStationMock, highlightedSort: String) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {

            // Icono de la marca
            Box(modifier = Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.background), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.LocalGasStation, "", tint = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.width(16.dp))

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Text(station.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text(station.distance, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    PriceTag("95", station.price95, highlightedSort == "Gasolina 95")
                    if (station.price98 > 0) PriceTag("98", station.price98, highlightedSort == "Gasolina 98")
                    PriceTag("Diésel", station.priceDiesel, highlightedSort == "Diésel")
                }
            }
        }
    }
}

@Composable
fun PriceTag(type: String, price: Double, isHighlighted: Boolean) {
    val color = if (isHighlighted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
    val weight = if (isHighlighted) FontWeight.Bold else FontWeight.Normal

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(type, style = MaterialTheme.typography.labelSmall, color = color)
        Text("${price}€", style = MaterialTheme.typography.bodyMedium, color = color, fontWeight = weight)
    }
}