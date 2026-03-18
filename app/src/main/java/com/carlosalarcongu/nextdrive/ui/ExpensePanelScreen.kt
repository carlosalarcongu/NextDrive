package com.carlosalarcongu.nextdrive.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.carlosalarcongu.nextdrive.data.Expense
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpensePanelScreen(vehicleId: Long, viewModel: NextDriveViewModel, onNavigateBack: () -> Unit, onNavigateToAdd: (Long, String) -> Unit, onNavigateToEdit: (Long, String, Long) -> Unit) {
    val expenses by viewModel.getExpensesForVehicle(vehicleId).collectAsState(initial = emptyList())
    var searchQuery by remember { mutableStateOf("") }
    val totalGasto = expenses.sumOf { it.totalCost }

    Scaffold(topBar = { TopAppBar(title = { Text("GESTIÓN DE GASTOS") }, navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver") } }) }) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            // PANEL DE 5 BOTONES SUPERIOR
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp).horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                ExpenseAddButton("Pieza", Icons.Default.Build) { onNavigateToAdd(vehicleId, "Pieza") }
                ExpenseAddButton("Repostaje", Icons.Default.LocalGasStation) { onNavigateToAdd(vehicleId, "Repostaje") }
                ExpenseAddButton("Mantenimiento", Icons.Default.Handyman) { onNavigateToAdd(vehicleId, "Mantenimiento") }
                ExpenseAddButton("Avería", Icons.Default.CarCrash) { onNavigateToAdd(vehicleId, "Avería") }
                ExpenseAddButton("Trámites", Icons.Default.Assignment) { onNavigateToAdd(vehicleId, "Trámites") }
            }
            HorizontalDivider()

            Text(text = "TOTAL HISTÓRICO: ${"%.2f".format(totalGasto)} €", modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            OutlinedTextField(value = searchQuery, onValueChange = { searchQuery = it }, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), placeholder = { Text("Buscar en el historial...") }, leadingIcon = { Icon(Icons.Default.Search, "") }, singleLine = true)

            val filtered = expenses.filter { it.title.contains(searchQuery, ignoreCase=true) || it.groupName?.contains(searchQuery, ignoreCase=true) == true }
            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(filtered) { expense ->
                    ExpenseDetailedCard(expense, onClick = { onNavigateToEdit(vehicleId, expense.category, expense.id) })
                }
            }
        }
    }
}

@Composable
fun ExpenseAddButton(title: String, icon: ImageVector, onClick: () -> Unit) {
    Card(modifier = Modifier.size(100.dp).clickable { onClick() }, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.fillMaxSize().padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Icon(icon, contentDescription = title, modifier = Modifier.size(36.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ExpenseDetailedCard(expense: Expense, onClick: () -> Unit) {
    val icon = ExpenseIconMap[expense.iconName] ?: Icons.Default.Build
    val dateString = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(expense.dateMillis))
    Card(modifier = Modifier.fillMaxWidth().clickable { onClick() }, elevation = CardDefaults.cardElevation(defaultElevation = 2.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(40.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(expense.title.uppercase(), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text("${expense.category} • $dateString", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text("${expense.totalCost} €", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
    }
}