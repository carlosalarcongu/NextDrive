package com.carlosalarcongu.nextdrive.ui

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import com.carlosalarcongu.nextdrive.data.Expense
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMantenimientoScreen(vehicleId: Long, expenseId: Long? = null, categoryStr: String, viewModel: NextDriveViewModel, onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val history by viewModel.getUniqueExpensesHistory().collectAsState(emptyList())
    val expenseToEdit by if (expenseId != null) viewModel.getExpenseById(expenseId).collectAsState(null) else remember { mutableStateOf(null) }
    var isInit by remember { mutableStateOf(false) }

    var title by remember { mutableStateOf("") }
    var groupName by remember { mutableStateOf("") }
    var expandedGroup by remember { mutableStateOf(false) }
    var workshop by remember { mutableStateOf("") }
    var isItemized by remember { mutableStateOf(false) }
    var laborCost by remember { mutableStateOf("") }
    var partsCost by remember { mutableStateOf("") }
    var totalCost by remember { mutableStateOf("") }

    LaunchedEffect(expenseToEdit) {
        if (expenseToEdit != null && !isInit) {
            title = expenseToEdit!!.title
            groupName = expenseToEdit!!.groupName ?: ""
            workshop = expenseToEdit!!.workshop ?: ""
            isItemized = expenseToEdit!!.isItemized
            laborCost = expenseToEdit!!.laborCost?.toString() ?: ""
            partsCost = expenseToEdit!!.partsCost?.toString() ?: ""
            totalCost = expenseToEdit!!.totalCost.toString()
            isInit = true
        }
    }

    val pastGroups = history.mapNotNull { it.groupName }.distinct().filter { it.contains(groupName, true) }

    Scaffold(topBar = { TopAppBar(title = { Text(categoryStr.uppercase()) }, navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "") } }) }) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).padding(16.dp).fillMaxSize().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {

            ExposedDropdownMenuBox(expanded = expandedGroup, onExpandedChange = { expandedGroup = !expandedGroup }) {
                OutlinedTextField(value = groupName, onValueChange = { groupName = it; expandedGroup = true }, label = { Text("Grupo (Ej: Motor, Frenos)") }, modifier = Modifier.menuAnchor().fillMaxWidth(), trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedGroup) })
                if (pastGroups.isNotEmpty() && groupName.isNotBlank()) {
                    DropdownMenu(expanded = expandedGroup, onDismissRequest = { expandedGroup = false }, properties = PopupProperties(focusable=false)) {
                        pastGroups.forEach { g -> DropdownMenuItem(text = { Text(g) }, onClick = { groupName = g; expandedGroup = false }) }
                    }
                }
            }

            OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Recurso / Intervención Concreta*") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = workshop, onValueChange = { workshop = it }, label = { Text("Taller / Lugar (Opcional)") }, modifier = Modifier.fillMaxWidth())

            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Text("Precio Desglosado", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                        Switch(checked = isItemized, onCheckedChange = { isItemized = it })
                    }
                    if (isItemized) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(value = laborCost, onValueChange = { laborCost = it; totalCost = "%.2f".format(Locale.US, (it.toDoubleOrNull() ?: 0.0) + (partsCost.toDoubleOrNull() ?: 0.0)) }, label = { Text("Mano Obra") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                            OutlinedTextField(value = partsCost, onValueChange = { partsCost = it; totalCost = "%.2f".format(Locale.US, (it.toDoubleOrNull() ?: 0.0) + (laborCost.toDoubleOrNull() ?: 0.0)) }, label = { Text("Piezas") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                        }
                    }
                    OutlinedTextField(value = totalCost, onValueChange = { totalCost = it; if(isItemized) { laborCost=""; partsCost=""; isItemized=false } }, label = { Text("Total Factura (€)*") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth(), textStyle = MaterialTheme.typography.titleLarge)
                }
            }

            Spacer(modifier = Modifier.weight(1f))
            Button(onClick = {
                val t = totalCost.toDoubleOrNull()
                if (title.isNotBlank() && t != null) {
                    val exp = Expense(id = expenseId ?: 0, vehicleId = vehicleId, title = title, dateMillis = expenseToEdit?.dateMillis ?: System.currentTimeMillis(), totalCost = t, category = categoryStr, groupName = groupName.ifBlank{null}, workshop = workshop.ifBlank{null}, isItemized = isItemized, laborCost = laborCost.toDoubleOrNull(), partsCost = partsCost.toDoubleOrNull(), iconName = if(categoryStr=="Avería") "Reparación" else "Herramientas")
                    if (expenseId == null) viewModel.addExpense(exp) else viewModel.updateExpense(exp)
                    onNavigateBack()
                } else Toast.makeText(context, "Faltan datos", Toast.LENGTH_SHORT).show()
            }, modifier = Modifier.fillMaxWidth()) { Text("GUARDAR") }
        }
    }
}