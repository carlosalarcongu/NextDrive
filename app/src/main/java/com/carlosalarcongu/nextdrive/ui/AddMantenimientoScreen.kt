package com.carlosalarcongu.nextdrive.ui

import android.app.DatePickerDialog
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.SettingsSuggest
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.carlosalarcongu.nextdrive.data.Expense
import com.carlosalarcongu.nextdrive.data.ServiceDef
import com.carlosalarcongu.nextdrive.data.ServiceInterval
import com.carlosalarcongu.nextdrive.data.predefinedServices
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMantenimientoScreen(vehicleId: Long, expenseId: Long? = null, initialCategoryStr: String, prefillTitle: String? = null, viewModel: NextDriveViewModel, onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val expenseToEdit by if (expenseId != null) viewModel.getExpenseById(expenseId).collectAsState(null) else remember { mutableStateOf(null) }
    val vehicle by viewModel.getVehicleById(vehicleId).collectAsState(null)

    val customIntervals by viewModel.getServiceIntervalsForVehicle(vehicleId).collectAsState(initial = emptyList())

    var isInit by remember { mutableStateOf(false) }

    var selectedService by remember { mutableStateOf<ServiceDef?>(null) }
    var customTitle by remember { mutableStateOf("") }

    var currentKm by remember { mutableStateOf("") }
    var executionDateMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    var cost by remember { mutableStateOf("") }
    var observations by remember { mutableStateOf("") }
    var uris by remember { mutableStateOf(listOf<String>()) }

    var showReminders by remember { mutableStateOf(false) }
    var remindByKm by remember { mutableStateOf(false) }
    var remindByTime by remember { mutableStateOf(false) }
    var reminderKmOffset by remember { mutableStateOf("") }
    var reminderMonthsOffset by remember { mutableStateOf("") }

    var showDatePicker by remember { mutableStateOf(false) }

    val docLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            uris = uris + it.toString()
        }
    }

    LaunchedEffect(vehicle, expenseToEdit, customIntervals) {
        if (!isInit && vehicle != null) {
            if (expenseToEdit != null) {
                val exp = expenseToEdit!!
                selectedService = predefinedServices.find { exp.title.contains(it.name, ignoreCase = true) } ?: predefinedServices.last()
                customTitle = exp.title
                currentKm = exp.registeredKm?.toString() ?: vehicle!!.currentKm?.toString() ?: ""
                executionDateMillis = exp.dateMillis
                cost = exp.totalCost.toString()
                observations = exp.comment ?: ""
                uris = if (!exp.attachedDocumentsUris.isNullOrBlank()) exp.attachedDocumentsUris!!.split(",") else emptyList()

                showReminders = exp.hasReminder
                if (exp.reminderKm != null) {
                    remindByKm = true
                    reminderKmOffset = (exp.reminderKm - (exp.registeredKm ?: 0)).coerceAtLeast(0).toString()
                }
                if (exp.reminderDateMillis != null && exp.reminderTimePeriod != null) {
                    remindByTime = true
                    reminderMonthsOffset = exp.reminderTimePeriod.toString()
                }
            } else {
                currentKm = vehicle!!.currentKm?.toString() ?: ""
                if (prefillTitle != null) {
                    val found = predefinedServices.find { it.name.equals(prefillTitle, ignoreCase = true) }
                    if (found != null) {
                        selectedService = found
                        customTitle = found.name

                        val custom = customIntervals.find { it.serviceName == found.name }
                        val defKm = custom?.defaultKm ?: found.defaultKm
                        val defMo = custom?.defaultMonths ?: found.defaultMonths

                        remindByKm = defKm > 0
                        remindByTime = defMo > 0
                        reminderKmOffset = if (defKm > 0) defKm.toString() else ""
                        reminderMonthsOffset = if (defMo > 0) defMo.toString() else ""

                        // APAGADO POR DEFECTO AUNQUE HAYA DATOS
                        showReminders = false
                    }
                }
            }
            isInit = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (selectedService == null) "NUEVO SERVICIO" else "REGISTRAR SERVICIO") },
                navigationIcon = {
                    IconButton(onClick = {
                        if (selectedService != null && expenseId == null && prefillTitle == null) selectedService = null else onNavigateBack()
                    }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "") }
                },
                actions = {
                    if (expenseId != null) {
                        IconButton(onClick = {
                            expenseToEdit?.let { viewModel.softDeleteExpense(it) }
                            onNavigateBack()
                        }) { Icon(Icons.Default.Delete, "Borrar", tint = MaterialTheme.colorScheme.error) }
                    }
                }, windowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp)
            )
        }
    ) { paddingValues ->
        if (selectedService == null) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize().padding(paddingValues).padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(predefinedServices) { service ->
                    Card(
                        modifier = Modifier.fillMaxWidth().aspectRatio(1.2f).clickable {
                            selectedService = service
                            customTitle = service.name

                            val custom = customIntervals.find { it.serviceName == service.name }
                            val defKm = custom?.defaultKm ?: service.defaultKm
                            val defMo = custom?.defaultMonths ?: service.defaultMonths

                            remindByKm = defKm > 0
                            remindByTime = defMo > 0
                            reminderKmOffset = if (defKm > 0) defKm.toString() else ""
                            reminderMonthsOffset = if (defMo > 0) defMo.toString() else ""

                            // APAGADO POR DEFECTO AUNQUE HAYA DATOS
                            showReminders = false
                        },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.fillMaxSize().padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                            Box(modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.SettingsSuggest, "", tint = MaterialTheme.colorScheme.primary)
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(service.name, style = MaterialTheme.typography.labelMedium, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, maxLines = 2)
                        }
                    }
                }
            }
        } else {
            Column(modifier = Modifier.padding(paddingValues).padding(16.dp).fillMaxSize().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {

                Card(
                    modifier = Modifier.fillMaxWidth().clickable { if (expenseId == null) selectedService = null },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.SettingsSuggest, "", tint = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(selectedService!!.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            if (expenseId == null) Text("Toca para cambiar de servicio", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                OutlinedTextField(
                    value = customTitle, onValueChange = { customTitle = it },
                    label = { Text("Nombre del registro") }, modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = currentKm, onValueChange = { currentKm = it.filter { c -> c.isDigit() } },
                        label = { Text("KM Actuales*") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword), modifier = Modifier.weight(1f)
                    )

                    val dateStr = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(executionDateMillis))
                    OutlinedTextField(
                        value = dateStr, onValueChange = {}, readOnly = true,
                        label = { Text("Fecha*") }, modifier = Modifier.weight(1f).clickable { showDatePicker = true },
                        enabled = false, colors = OutlinedTextFieldDefaults.colors(disabledTextColor = MaterialTheme.colorScheme.onSurface, disabledBorderColor = MaterialTheme.colorScheme.outline, disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }

                OutlinedTextField(value = cost, onValueChange = { cost = it }, label = { Text("Coste Total (€)*") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())

                // OBSERVACIONES MÁS PEQUEÑAS
                OutlinedTextField(
                    value = observations, onValueChange = { observations = it },
                    label = { Text("Observaciones") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 3
                )

                Button(onClick = { docLauncher.launch(arrayOf("application/pdf", "image/*")) }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary), modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.AttachFile, ""); Spacer(modifier = Modifier.width(8.dp)); Text("Adjuntar Documentación")
                }
                if (uris.isNotEmpty()) Text("${uris.size} documentos adjuntos.", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall)

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Text("SISTEMA DE RECORDATORIOS", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    Switch(checked = showReminders, onCheckedChange = { showReminders = it })
                }

                if (showReminders) {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {

                            OutlinedButton(onClick = { Toast.makeText(context, "Funcionalidad en desarrollo...", Toast.LENGTH_SHORT).show() }, modifier = Modifier.fillMaxWidth()) {
                                Icon(Icons.Default.AutoAwesome, ""); Spacer(Modifier.width(8.dp)); Text("Rellenar según marca", maxLines = 1)
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(checked = remindByKm, onCheckedChange = { remindByKm = it })
                                Text("Avisar por Kilometraje", fontWeight = FontWeight.Bold)
                            }
                            if (remindByKm) {
                                OutlinedTextField(
                                    value = reminderKmOffset, onValueChange = { reminderKmOffset = it.filter { c -> c.isDigit() } },
                                    label = { Text("Dentro de X km") }, suffix = { Text("km") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword), modifier = Modifier.fillMaxWidth().padding(start = 48.dp)
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(checked = remindByTime, onCheckedChange = { remindByTime = it })
                                Text("Avisar por Tiempo", fontWeight = FontWeight.Bold)
                            }
                            if (remindByTime) {
                                OutlinedTextField(
                                    value = reminderMonthsOffset, onValueChange = { reminderMonthsOffset = it.filter { c -> c.isDigit() } },
                                    label = { Text("Dentro de X meses") }, suffix = { Text("meses") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword), modifier = Modifier.fillMaxWidth().padding(start = 48.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))
                Button(onClick = {
                    val finalCost = cost.toDoubleOrNull()
                    val km = currentKm.toIntOrNull()

                    if (customTitle.isNotBlank() && finalCost != null && km != null) {

                        val targetKm = if (showReminders && remindByKm && reminderKmOffset.isNotBlank() && reminderKmOffset.toInt() > 0) {
                            km + reminderKmOffset.toInt()
                        } else null

                        val targetDate = if (showReminders && remindByTime && reminderMonthsOffset.isNotBlank() && reminderMonthsOffset.toInt() > 0) {
                            val cal = Calendar.getInstance()
                            cal.timeInMillis = executionDateMillis
                            cal.add(Calendar.MONTH, reminderMonthsOffset.toInt())
                            cal.timeInMillis
                        } else null

                        val hasAnyReminder = targetKm != null || targetDate != null

                        val exp = Expense(
                            id = expenseId ?: 0, vehicleId = vehicleId,
                            title = customTitle,
                            dateMillis = executionDateMillis,
                            totalCost = finalCost,
                            category = initialCategoryStr,
                            comment = observations.ifBlank { null },
                            attachedDocumentsUris = uris.joinToString(",").ifBlank { null },
                            registeredKm = km,
                            hasReminder = hasAnyReminder,
                            reminderType = if (hasAnyReminder) "AMBOS" else null,
                            reminderKm = targetKm,
                            reminderDateMillis = targetDate,
                            reminderTimePeriod = if (targetDate != null) reminderMonthsOffset.toInt() else null,
                            iconName = "Herramientas"
                        )
                        if (expenseId == null) viewModel.addExpense(exp) else viewModel.updateExpense(exp)

                        if (km > (vehicle?.currentKm ?: 0)) viewModel.updateVehicle(vehicle!!.copy(currentKm = km))

                        onNavigateBack()
                    } else Toast.makeText(context, "Nombre, Kilómetros y Coste son obligatorios", Toast.LENGTH_SHORT).show()
                }, modifier = Modifier.fillMaxWidth()) { Text("GUARDAR REGISTRO") }
            }

            if (showDatePicker) {
                val cal = Calendar.getInstance().apply { timeInMillis = executionDateMillis }
                DatePickerDialog(context, { _, y, m, d -> cal.set(y, m, d); executionDateMillis = cal.timeInMillis; showDatePicker = false }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).apply { setOnDismissListener { showDatePicker = false } }.show()
            }
        }
    }
}