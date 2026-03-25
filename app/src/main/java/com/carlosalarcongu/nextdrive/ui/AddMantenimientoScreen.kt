package com.carlosalarcongu.nextdrive.ui

import android.app.DatePickerDialog
import android.content.Intent
import android.provider.CalendarContract
import android.widget.Toast
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMantenimientoScreen(vehicleId: Long, expenseId: Long? = null, initialCategoryStr: String, prefillTitle: String? = null, viewModel: NextDriveViewModel, onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val history by viewModel.getUniqueExpensesHistory().collectAsState(emptyList())
    val expenseToEdit by if (expenseId != null) viewModel.getExpenseById(expenseId).collectAsState(null) else remember { mutableStateOf(null) }
    val vehicle by viewModel.getVehicleById(vehicleId).collectAsState(null)
    var isInit by remember { mutableStateOf(false) }

    // NUEVO: Estado para cambiar la categoría dinámicamente
    var currentCategory by remember { mutableStateOf(initialCategoryStr) }
    var expandedCategoryMenu by remember { mutableStateOf(false) }

    var title by remember { mutableStateOf(prefillTitle ?: "") }
    var groupName by remember { mutableStateOf("") }
    var expandedGroup by remember { mutableStateOf(false) }
    var workshop by remember { mutableStateOf("") }
    var isItemized by remember { mutableStateOf(false) }
    var laborCost by remember { mutableStateOf("") }
    var partsCost by remember { mutableStateOf("") }
    var totalCost by remember { mutableStateOf("") }
    var registeredKm by remember { mutableStateOf("") }
    var hasReminder by remember { mutableStateOf(false) }
    var notifyChecked by remember { mutableStateOf(true) }
    var calendarChecked by remember { mutableStateOf(false) }

    val reminderOptions = listOf("+ KM", "KM Exactos", "+ Tiempo", "Fecha Exacta")
    var reminderMode by remember { mutableStateOf(reminderOptions[0]) }
    var reminderKmInput by remember { mutableStateOf("") }
    var reminderTimePeriod by remember { mutableStateOf("") }
    val timeUnits = listOf("Días", "Meses", "Años")
    var selectedTimeUnit by remember { mutableStateOf(timeUnits[1]) }
    var expandedTimeUnit by remember { mutableStateOf(false) }
    val calendar = Calendar.getInstance()
    var selectedExactDateMillis by remember { mutableStateOf<Long?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(vehicle, expenseToEdit) {
        if (!isInit && vehicle != null) {
            if (expenseToEdit != null) {
                currentCategory = expenseToEdit!!.category // Carga la categoría real
                title = expenseToEdit!!.title
                groupName = expenseToEdit!!.groupName ?: ""
                workshop = expenseToEdit!!.workshop ?: ""
                isItemized = expenseToEdit!!.isItemized
                laborCost = expenseToEdit!!.laborCost?.toString() ?: ""
                partsCost = expenseToEdit!!.partsCost?.toString() ?: ""
                totalCost = expenseToEdit!!.totalCost.toString()
                registeredKm = expenseToEdit!!.registeredKm?.toString() ?: ""
                hasReminder = expenseToEdit!!.hasReminder
                notifyChecked = expenseToEdit!!.reminderType == "NOTIFICACION" || expenseToEdit!!.reminderType == "AMBOS"
                calendarChecked = expenseToEdit!!.reminderType == "CALENDARIO" || expenseToEdit!!.reminderType == "AMBOS"
                if (expenseToEdit!!.reminderKm != null) { reminderMode = "KM Exactos"; reminderKmInput = expenseToEdit!!.reminderKm.toString() }
                if (expenseToEdit!!.reminderDateMillis != null && expenseToEdit!!.reminderTimePeriod == null) { reminderMode = "Fecha Exacta"; selectedExactDateMillis = expenseToEdit!!.reminderDateMillis }
                else if (expenseToEdit!!.reminderTimePeriod != null) { reminderMode = "+ Tiempo"; reminderTimePeriod = expenseToEdit!!.reminderTimePeriod.toString(); selectedTimeUnit = expenseToEdit!!.reminderTimeUnit ?: "Meses" }
            } else { registeredKm = vehicle!!.currentKm?.toString() ?: "" }
            isInit = true
        }
    }

    val pastGroups = history.mapNotNull { it.groupName }.distinct().filter { it.contains(groupName, true) }

    Scaffold(topBar = {
        TopAppBar(
            title = { Text(currentCategory.uppercase()) },
            navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "") } },
            actions = {
                if (expenseId != null) {
                    IconButton(onClick = {
                        expenseToEdit?.let { viewModel.softDeleteExpense(it) } // A LA PAPELERA
                        onNavigateBack()
                    }) { Icon(Icons.Default.Delete, "Borrar", tint = MaterialTheme.colorScheme.error) }
                }
            }
        )
    }) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).padding(16.dp).fillMaxSize().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {

            // NUEVO BLOQUE: CAMBIAR CATEGORÍA
            ExposedDropdownMenuBox(expanded = expandedCategoryMenu, onExpandedChange = { expandedCategoryMenu = !expandedCategoryMenu }) {
                OutlinedTextField(
                    value = currentCategory, onValueChange = {}, readOnly = true,
                    label = { Text("Tipo de Registro") },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategoryMenu) },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary)
                )
                DropdownMenu(expanded = expandedCategoryMenu, onDismissRequest = { expandedCategoryMenu = false }) {
                    listOf("Mantenimiento", "Avería", "Pieza", "Trámites").forEach { cat ->
                        DropdownMenuItem(text = { Text(cat) }, onClick = { currentCategory = cat; expandedCategoryMenu = false })
                    }
                }
            }

            Text("INFORMACIÓN PRINCIPAL", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Recurso / Intervención Concreta*") }, placeholder = { Text("p. ej. Cambio de Aceite") }, modifier = Modifier.fillMaxWidth())
            GradientDivider()

            Text("DATOS DEL SERVICIO", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = registeredKm, onValueChange = { registeredKm = it.filter { c->c.isDigit() } }, label = { Text("KM Actuales") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword), modifier = Modifier.weight(1f))
                OutlinedTextField(value = workshop, onValueChange = { workshop = it }, label = { Text("Taller / Lugar") }, modifier = Modifier.weight(1f))
            }

            ExposedDropdownMenuBox(expanded = expandedGroup, onExpandedChange = { expandedGroup = !expandedGroup }) {
                OutlinedTextField(value = groupName, onValueChange = { groupName = it; expandedGroup = true }, label = { Text("Categoría / Grupo") }, modifier = Modifier.menuAnchor().fillMaxWidth(), trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedGroup) })
                if (pastGroups.isNotEmpty() && groupName.isNotBlank()) {
                    DropdownMenu(expanded = expandedGroup, onDismissRequest = { expandedGroup = false }, properties = PopupProperties(focusable=false)) {
                        pastGroups.forEach { g -> DropdownMenuItem(text = { Text(g) }, onClick = { groupName = g; expandedGroup = false }) }
                    }
                }
            }
            GradientDivider()

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
            GradientDivider()

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text("¿PLANIFICAR RECORDATORIO?", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                Switch(checked = hasReminder, onCheckedChange = { hasReminder = it })
            }

            if (hasReminder) {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            reminderOptions.forEach { opt -> FilterChip(selected = reminderMode == opt, onClick = { reminderMode = opt }, label = { Text(opt) }) }
                        }
                        when (reminderMode) {
                            "+ KM", "KM Exactos" -> { OutlinedTextField(value = reminderKmInput, onValueChange = { reminderKmInput = it.filter { c -> c.isDigit() } }, label = { Text("Kilometraje") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword), modifier = Modifier.fillMaxWidth()) }
                            "+ Tiempo" -> {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(value = reminderTimePeriod, onValueChange = { reminderTimePeriod = it.filter { c->c.isDigit() } }, label = { Text("Cantidad") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword), modifier = Modifier.weight(1f))
                                    ExposedDropdownMenuBox(expanded = expandedTimeUnit, onExpandedChange = { expandedTimeUnit = !expandedTimeUnit }, modifier = Modifier.weight(1f)) {
                                        OutlinedTextField(value = selectedTimeUnit, onValueChange = {}, readOnly = true, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTimeUnit) }, modifier = Modifier.menuAnchor())
                                        DropdownMenu(expanded = expandedTimeUnit, onDismissRequest = { expandedTimeUnit = false }) { timeUnits.forEach { unit -> DropdownMenuItem(text = { Text(unit) }, onClick = { selectedTimeUnit = unit; expandedTimeUnit = false }) } }
                                    }
                                }
                            }
                            "Fecha Exacta" -> { OutlinedButton(onClick = { showDatePicker = true }, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Default.CalendarToday, ""); Spacer(Modifier.width(8.dp)); Text(if (selectedExactDateMillis != null) SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(selectedExactDateMillis!!)) else "Seleccionar Fecha") } }
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.horizontalScroll(rememberScrollState())) {
                            Checkbox(checked = notifyChecked, onCheckedChange = { notifyChecked = it }); Text("Notificación")
                            Spacer(modifier = Modifier.width(16.dp))
                            Checkbox(checked = calendarChecked, onCheckedChange = { calendarChecked = it }); Text("Calendario")
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            Button(onClick = {
                val t = totalCost.toDoubleOrNull()
                if (title.isNotBlank() && t != null) {
                    val exp = Expense(
                        id = expenseId ?: 0, vehicleId = vehicleId, title = title, dateMillis = expenseToEdit?.dateMillis ?: System.currentTimeMillis(),
                        totalCost = t, category = currentCategory, // Usa la categoría seleccionada
                        groupName = groupName.ifBlank{null}, workshop = workshop.ifBlank{null}, isItemized = isItemized, laborCost = laborCost.toDoubleOrNull(), partsCost = partsCost.toDoubleOrNull(), registeredKm = registeredKm.toIntOrNull(), hasReminder = hasReminder,
                        reminderType = if(hasReminder) (if (notifyChecked && calendarChecked) "AMBOS" else if (calendarChecked) "CALENDARIO" else "NOTIFICACION") else null,
                        reminderKm = if (hasReminder && (reminderMode == "+ KM" || reminderMode == "KM Exactos")) (if(reminderMode=="+ KM") (registeredKm.toIntOrNull()?:vehicle?.currentKm?:0) + (reminderKmInput.toIntOrNull()?:0) else reminderKmInput.toIntOrNull()) else null,
                        reminderDateMillis = if (hasReminder && (reminderMode == "+ Tiempo" || reminderMode == "Fecha Exacta")) (if(reminderMode=="Fecha Exacta") selectedExactDateMillis else { val cal = Calendar.getInstance(); val p = reminderTimePeriod.toIntOrNull()?:0; when(selectedTimeUnit){"Días"->cal.add(Calendar.DAY_OF_YEAR,p); "Meses"->cal.add(Calendar.MONTH,p); "Años"->cal.add(Calendar.YEAR,p)}; cal.timeInMillis }) else null,
                        iconName = if(currentCategory=="Avería") "Reparación" else "Herramientas"
                    )
                    if (expenseId == null) viewModel.addExpense(exp) else viewModel.updateExpense(exp)
                    onNavigateBack()
                } else Toast.makeText(context, "Faltan datos", Toast.LENGTH_SHORT).show()
            }, modifier = Modifier.fillMaxWidth()) { Text("GUARDAR") }
        }

        if (showDatePicker) {
            DatePickerDialog(context, { _, y, m, d -> calendar.set(y, m, d); selectedExactDateMillis = calendar.timeInMillis; showDatePicker = false }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).apply { setOnDismissListener { showDatePicker = false } }.show()
        }
    }
}