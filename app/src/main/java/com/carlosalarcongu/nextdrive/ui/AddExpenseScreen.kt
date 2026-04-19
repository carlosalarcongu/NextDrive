package com.carlosalarcongu.nextdrive.ui

import android.content.Intent
import android.provider.CalendarContract
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(vehicleId: Long, expenseId: Long? = null, defaultCategory: String, viewModel: NextDriveViewModel, onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val historyExpenses by viewModel.getUniqueExpensesHistory().collectAsState(emptyList())
    val expenseToEdit by if (expenseId != null) viewModel.getExpenseById(expenseId).collectAsState(null) else remember { mutableStateOf(null) }
    var isInitialized by remember { mutableStateOf(false) }

    var title by remember { mutableStateOf("") }
    var expandedTitle by remember { mutableStateOf(false) }
    var cost by remember { mutableStateOf("") }
    var groupName by remember { mutableStateOf("") }
    var comment by remember { mutableStateOf("") }
    var selectedIcon by remember { mutableStateOf(if (defaultCategory == "Repostaje") "Gasolinera" else "Herramientas") }

    val categories = listOf("Pieza", "Consumible", "Repostaje")
    var selectedCategory by remember { mutableStateOf(defaultCategory) }

    var hasReminder by remember { mutableStateOf(false) }
    var notifyChecked by remember { mutableStateOf(true) }
    var calendarChecked by remember { mutableStateOf(false) }
    var reminderMode by remember { mutableStateOf("KM") }
    var reminderKm by remember { mutableStateOf("") }
    var reminderTimePeriod by remember { mutableStateOf("") }
    val timeUnits = listOf("Días", "Meses", "Años")
    var selectedTimeUnit by remember { mutableStateOf(timeUnits[1]) }
    var expandedTimeUnit by remember { mutableStateOf(false) }

    var icsContentToExport by remember { mutableStateOf("") }
    val exportIcsLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/calendar")) { uri ->
        uri?.let {
            context.contentResolver.openOutputStream(it)?.use { os -> os.write(icsContentToExport.toByteArray()) }
            Toast.makeText(context, "Archivo .ics guardado", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(expenseToEdit) {
        if (expenseToEdit != null && !isInitialized) {
            title = expenseToEdit!!.title
            cost = expenseToEdit!!.totalCost.toString()
            groupName = expenseToEdit!!.groupName ?: ""
            comment = expenseToEdit!!.comment ?: ""
            selectedCategory = expenseToEdit!!.category
            selectedIcon = expenseToEdit!!.iconName
            hasReminder = expenseToEdit!!.hasReminder
            notifyChecked = expenseToEdit!!.reminderType == "NOTIFICACION" || expenseToEdit!!.reminderType == "AMBOS"
            calendarChecked = expenseToEdit!!.reminderType == "CALENDARIO" || expenseToEdit!!.reminderType == "AMBOS"
            reminderKm = expenseToEdit!!.reminderKm?.toString() ?: ""
            if (expenseToEdit!!.reminderTimePeriod != null) {
                reminderMode = "TIEMPO"
                reminderTimePeriod = expenseToEdit!!.reminderTimePeriod.toString()
                selectedTimeUnit = expenseToEdit!!.reminderTimeUnit ?: "Meses"
            }
            isInitialized = true
        }
    }

    val filteredHistory = historyExpenses.filter { it.title.contains(title, ignoreCase = true) }

    fun calculateFutureDate(): Long {
        if (reminderMode == "KM") return System.currentTimeMillis()
        val period = reminderTimePeriod.toIntOrNull() ?: 0
        val cal = Calendar.getInstance()
        when(selectedTimeUnit) { "Días" -> cal.add(Calendar.DAY_OF_YEAR, period); "Meses" -> cal.add(Calendar.MONTH, period); "Años" -> cal.add(Calendar.YEAR, period) }
        return cal.timeInMillis
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text(if (expenseId == null) "NUEVO REGISTRO" else "EDITAR REGISTRO") }, navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "") } }, windowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp)) }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).padding(16.dp).fillMaxSize().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {

            ExposedDropdownMenuBox(expanded = expandedTitle, onExpandedChange = { expandedTitle = !expandedTitle }) {
                OutlinedTextField(
                    value = title, onValueChange = { title = it; expandedTitle = true }, label = { Text("Nombre del Gasto*") },
                    modifier = Modifier.menuAnchor().fillMaxWidth(), trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTitle) }
                )
                if (filteredHistory.isNotEmpty() && title.isNotBlank() && expenseId == null) {
                    DropdownMenu(expanded = expandedTitle, onDismissRequest = { expandedTitle = false }, modifier = Modifier.exposedDropdownSize(), properties = PopupProperties(focusable = false)) {
                        filteredHistory.forEach { hist -> DropdownMenuItem(text = { Text(hist.title) }, onClick = { title = hist.title; cost = hist.totalCost.toString(); selectedCategory = hist.category; groupName = hist.groupName ?: ""; comment = hist.comment ?: ""; selectedIcon = hist.iconName; expandedTitle = false }) }
                    }
                }
            }

            OutlinedTextField(value = cost, onValueChange = { cost = it }, label = { Text("Coste Total (€)*") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())

            Text("Categoría:", style = MaterialTheme.typography.labelLarge)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) { categories.forEach { cat -> FilterChip(selected = selectedCategory == cat, onClick = { selectedCategory = cat }, label = { Text(cat) }) } }

            Text("Icono Visual:", style = MaterialTheme.typography.labelLarge)
            Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ExpenseIconMap.forEach { (name, icon) ->
                    FilterChip(selected = selectedIcon == name, onClick = { selectedIcon = name }, label = { Icon(icon, name) })
                }
            }

            OutlinedTextField(value = groupName, onValueChange = { groupName = it }, label = { Text("Grupo (Ej: Suspensión)") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = comment, onValueChange = { comment = it }, label = { Text("Observaciones") }, modifier = Modifier.fillMaxWidth().height(100.dp), maxLines = 4)

            // ... [Resto del código idéntico al anterior] ...
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text("¿RECORDATORIO?", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                Switch(checked = hasReminder, onCheckedChange = { hasReminder = it })
            }

            if (hasReminder) {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(selected = reminderMode == "KM", onClick = { reminderMode = "KM" }, label = { Text("Por Kilómetros") })
                            FilterChip(selected = reminderMode == "TIEMPO", onClick = { reminderMode = "TIEMPO" }, label = { Text("Por Tiempo") })
                        }
                        if (reminderMode == "KM") {
                            OutlinedTextField(value = reminderKm, onValueChange = { reminderKm = it }, label = { Text("Avisar a los X km") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                        } else {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(value = reminderTimePeriod, onValueChange = { reminderTimePeriod = it }, label = { Text("Cantidad") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
                                ExposedDropdownMenuBox(expanded = expandedTimeUnit, onExpandedChange = { expandedTimeUnit = !expandedTimeUnit }, modifier = Modifier.weight(1f)) {
                                    OutlinedTextField(value = selectedTimeUnit, onValueChange = {}, readOnly = true, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTimeUnit) }, modifier = Modifier.menuAnchor())
                                    DropdownMenu(expanded = expandedTimeUnit, onDismissRequest = { expandedTimeUnit = false }, modifier = Modifier.exposedDropdownSize()) {
                                        timeUnits.forEach { unit -> DropdownMenuItem(text = { Text(unit) }, onClick = { selectedTimeUnit = unit; expandedTimeUnit = false }) }
                                    }
                                }
                            }
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.horizontalScroll(rememberScrollState())) {
                            Checkbox(checked = notifyChecked, onCheckedChange = { notifyChecked = it })
                            Text("App Push")
                            Spacer(modifier = Modifier.width(16.dp))
                            Checkbox(checked = calendarChecked, onCheckedChange = { calendarChecked = it })
                            Text("Calendario")
                        }
                        Button(onClick = {
                            val dt = calculateFutureDate()
                            icsContentToExport = generateIcs("Revisión: $title", comment, dt)
                            exportIcsLauncher.launch("${title.replace(" ", "_")}_recordatorio.ics")
                        }, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Default.Save, "", modifier = Modifier.padding(end = 8.dp)); Text("Exportar a .ics") }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val finalCost = cost.toDoubleOrNull()
                    if (title.isNotBlank() && finalCost != null) {
                        val rType = if (notifyChecked && calendarChecked) "AMBOS" else if (calendarChecked) "CALENDARIO" else "NOTIFICACION"
                        val targetDate = if (hasReminder) calculateFutureDate() else null
                        val expense = Expense(
                            id = expenseId ?: 0, vehicleId = vehicleId, title = title, dateMillis = expenseToEdit?.dateMillis ?: System.currentTimeMillis(),
                            totalCost = finalCost, category = selectedCategory, groupName = groupName.takeIf { it.isNotBlank() }, comment = comment.takeIf { it.isNotBlank() },
                            hasReminder = hasReminder, reminderType = if (hasReminder) rType else null, reminderKm = if (reminderMode == "KM") reminderKm.toIntOrNull() else null,
                            reminderDateMillis = targetDate, reminderTimePeriod = if (reminderMode == "TIEMPO") reminderTimePeriod.toIntOrNull() else null, reminderTimeUnit = if (reminderMode == "TIEMPO") selectedTimeUnit else null,
                            iconName = selectedIcon
                        )
                        if (expenseId == null) viewModel.addExpense(expense) else viewModel.updateExpense(expense)

                        if (hasReminder && calendarChecked) {
                            val intent = Intent(Intent.ACTION_INSERT).apply {
                                data = CalendarContract.Events.CONTENT_URI
                                putExtra(CalendarContract.Events.TITLE, "Mantenimiento: $title")
                                putExtra(CalendarContract.Events.DESCRIPTION, "Notas: $comment")
                                if (targetDate != null) putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, targetDate)
                            }
                            context.startActivity(intent)
                        }
                        Toast.makeText(context, "Guardado", Toast.LENGTH_SHORT).show()
                        onNavigateBack()
                    } else { Toast.makeText(context, "Faltan campos", Toast.LENGTH_SHORT).show() }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text(if (expenseId == null) "GUARDAR REGISTRO" else "ACTUALIZAR REGISTRO") }
        }
    }
}