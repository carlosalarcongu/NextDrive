// AddExpenseScreen.kt
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(vehicleId: Long, viewModel: NextDriveViewModel, onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val historyExpenses by viewModel.getUniqueExpensesHistory().collectAsState(emptyList())

    var title by remember { mutableStateOf("") }
    var expandedTitle by remember { mutableStateOf(false) }
    var cost by remember { mutableStateOf("") }
    var groupName by remember { mutableStateOf("") }
    var comment by remember { mutableStateOf("") }

    val categories = listOf("Pieza", "Consumible", "Repostaje")
    var selectedCategory by remember { mutableStateOf(categories[0]) }

    var hasReminder by remember { mutableStateOf(false) }
    var notifyChecked by remember { mutableStateOf(true) }
    var calendarChecked by remember { mutableStateOf(false) }

    val filteredHistory = historyExpenses.filter { it.title.contains(title, ignoreCase = true) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Nuevo Registro") }, navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "") } }) }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).padding(16.dp).fillMaxSize().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {

            ExposedDropdownMenuBox(expanded = expandedTitle, onExpandedChange = { expandedTitle = !expandedTitle }) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it; expandedTitle = true },
                    label = { Text("Nombre del Gasto*") },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTitle) }
                )
                if (filteredHistory.isNotEmpty() && title.isNotBlank()) {
                    DropdownMenu(
                        expanded = expandedTitle,
                        onDismissRequest = { expandedTitle = false },
                        modifier = Modifier.exposedDropdownSize(),
                        properties = PopupProperties(focusable = false)
                    ) {
                        filteredHistory.forEach { hist ->
                            DropdownMenuItem(
                                text = { Text(hist.title) },
                                onClick = {
                                    title = hist.title
                                    cost = hist.totalCost.toString()
                                    selectedCategory = hist.category
                                    groupName = hist.groupName ?: ""
                                    comment = hist.comment ?: ""
                                    expandedTitle = false
                                }
                            )
                        }
                    }
                }
            }

            OutlinedTextField(value = cost, onValueChange = { cost = it }, label = { Text("Coste Total (€)*") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())

            Text("Categoría:", style = MaterialTheme.typography.labelLarge)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                categories.forEach { cat -> FilterChip(selected = selectedCategory == cat, onClick = { selectedCategory = cat }, label = { Text(cat) }) }
            }

            OutlinedTextField(value = groupName, onValueChange = { groupName = it }, label = { Text("Grupo") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = comment, onValueChange = { comment = it }, label = { Text("Observaciones") }, modifier = Modifier.fillMaxWidth().height(100.dp), maxLines = 4)

            GradientDivider()

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text("¿Recordatorio?", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                Switch(checked = hasReminder, onCheckedChange = { hasReminder = it })
            }

            if (hasReminder) {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Método:", style = MaterialTheme.typography.labelLarge)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = notifyChecked, onCheckedChange = { notifyChecked = it })
                            Text("Push")
                            Spacer(modifier = Modifier.width(16.dp))
                            Checkbox(checked = calendarChecked, onCheckedChange = { calendarChecked = it })
                            Text("Calendario")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val finalCost = cost.toDoubleOrNull()
                    if (title.isNotBlank() && finalCost != null && (!hasReminder || notifyChecked || calendarChecked)) {
                        val rType = if (notifyChecked && calendarChecked) "AMBOS" else if (calendarChecked) "CALENDARIO" else "NOTIFICACION"
                        viewModel.addExpense(Expense(
                            vehicleId = vehicleId, title = title, dateMillis = System.currentTimeMillis(),
                            totalCost = finalCost, category = selectedCategory,
                            groupName = groupName.takeIf { it.isNotBlank() }, comment = comment.takeIf { it.isNotBlank() },
                            hasReminder = hasReminder, reminderType = if (hasReminder) rType else null
                        ))
                        Toast.makeText(context, "Registrado", Toast.LENGTH_SHORT).show()
                        onNavigateBack()
                    } else {
                        Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Guardar") }
        }
    }
}