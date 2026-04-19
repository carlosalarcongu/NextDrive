package com.carlosalarcongu.nextdrive.ui

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class DashboardModule { INFO_PARKING, DOCS, INTERVALS, EXPENSES }

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun VehicleDashboardScreen(
    vehicleId: Long, viewModel: NextDriveViewModel, onNavigateBack: () -> Unit,
    onNavigateToEdit: (Long) -> Unit, onNavigateToDocuments: (Long) -> Unit,
    onNavigateToAdd: (Long, String) -> Unit, onNavigateToEditExpense: (Long, String, Long) -> Unit,
    onNavigateToSettingsIntervals: (Long) -> Unit,
    onUpdateDashboardOrder: (String) -> Unit // NUEVO: Callback de orden
) {
    val context = LocalContext.current
    val prefs = LocalUserPrefs.current
    val vehicle by viewModel.getVehicleById(vehicleId).collectAsState(initial = null)
    val expenses by viewModel.getExpensesForVehicle(vehicleId).collectAsState(initial = emptyList())
    val haptic = LocalHapticFeedback.current

    var isFabExpanded by remember { mutableStateOf(false) }
    var selectedExpenseIds by remember { mutableStateOf(setOf<Long>()) }

    // ESTADO: Modularidad
    var isEditingLayout by remember { mutableStateOf(false) }
    var moduleOrder by remember {
        mutableStateOf(
            prefs.dashboardOrder.split(",").mapNotNull {
                try { DashboardModule.valueOf(it) } catch(e: Exception) { null }
            }.ifEmpty { DashboardModule.values().toList() }
        )
    }

    var showParkingDialog by remember { mutableStateOf(false) }
    var parkingMinutes by remember { mutableStateOf("") }

    val locationManager = remember { context.getSystemService(Context.LOCATION_SERVICE) as LocationManager }
    val alarmManager = remember { context.getSystemService(Context.ALARM_SERVICE) as AlarmManager }
    val locationPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) showParkingDialog = true else Toast.makeText(context, "Permiso denegado", Toast.LENGTH_SHORT).show()
    }

    if (vehicle == null) return

    // FUNCIÓN PARA MOVER MÓDULOS
    fun moveModule(index: Int, direction: Int) {
        val newOrder = moduleOrder.toMutableList()
        val item = newOrder.removeAt(index)
        newOrder.add(index + direction, item)
        moduleOrder = newOrder
        onUpdateDashboardOrder(newOrder.joinToString(","))
        triggerVibration(context, prefs)
    }

    Scaffold(
        topBar = {
            if (selectedExpenseIds.isNotEmpty()) {
                TopAppBar(
                    title = { Text("${selectedExpenseIds.size} seleccionados") },
                    navigationIcon = { IconButton(onClick = { selectedExpenseIds = emptySet() }) { Icon(Icons.Default.Close, "Cancelar") } },
                    actions = {
                        IconButton(onClick = {
                            viewModel.softDeleteMultipleExpenses(selectedExpenseIds.toList())
                            triggerVibration(context, prefs)
                            selectedExpenseIds = emptySet()
                        }) { Icon(Icons.Default.Delete, "Borrar", tint = MaterialTheme.colorScheme.error) }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                )
            } else if (isEditingLayout) {
                TopAppBar(
                    title = { Text("ORGANIZAR PANEL") },
                    navigationIcon = { IconButton(onClick = { isEditingLayout = false; triggerVibration(context, prefs) }) { Icon(Icons.Default.Check, "Guardar") } },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                )
            } else {
                TopAppBar(
                    title = { Text(vehicle?.model?.uppercase() ?: "") },
                    navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "") } },
                    actions = {
                        IconButton(onClick = { isEditingLayout = true; triggerVibration(context, prefs) }) { Icon(Icons.Default.DashboardCustomize, "Editar Panel") }
                        IconButton(onClick = { onNavigateToEdit(vehicleId) }) { Icon(Icons.Default.Edit, "Editar Coche") }
                    }
                )
            }
        },
        floatingActionButton = {
            if (selectedExpenseIds.isEmpty() && !isEditingLayout) {
                Column(horizontalAlignment = Alignment.End) {
                    if (isFabExpanded) {
                        FabMenuItem("Trámites", Icons.Default.Assignment) { onNavigateToAdd(vehicleId, "Trámites"); isFabExpanded = false }
                        FabMenuItem("Avería", Icons.Default.CarCrash) { onNavigateToAdd(vehicleId, "Avería"); isFabExpanded = false }
                        FabMenuItem("Mantenimiento", Icons.Default.Handyman) { onNavigateToAdd(vehicleId, "Mantenimiento"); isFabExpanded = false }
                        FabMenuItem("Repostaje", Icons.Default.LocalGasStation) { onNavigateToAdd(vehicleId, "Repostaje"); isFabExpanded = false }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    FloatingActionButton(onClick = { isFabExpanded = !isFabExpanded; triggerVibration(context, prefs) }, containerColor = MaterialTheme.colorScheme.primary) {
                        Icon(if (isFabExpanded) Icons.Default.Close else Icons.Default.Add, "Desplegar")
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(paddingValues), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {

            if (isEditingLayout) {
                item { Text("Usa las flechas para reordenar las secciones de tu panel.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                itemsIndexed(moduleOrder) { index, module ->
                    val moduleName = when(module) {
                        DashboardModule.INFO_PARKING -> "Información y Aparcamiento"
                        DashboardModule.DOCS -> "Documentos"
                        DashboardModule.INTERVALS -> "Programaciones"
                        DashboardModule.EXPENSES -> "Historial de Gastos"
                    }
                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(moduleName, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            Row {
                                IconButton(onClick = { moveModule(index, -1) }, enabled = index > 0) { Icon(Icons.Default.KeyboardArrowUp, "Subir") }
                                IconButton(onClick = { moveModule(index, 1) }, enabled = index < moduleOrder.size - 1) { Icon(Icons.Default.KeyboardArrowDown, "Bajar") }
                            }
                        }
                    }
                }
            } else {
                // RENDERIZADO DINÁMICO SEGÚN EL ORDEN
                moduleOrder.forEach { module ->
                    when (module) {
                        DashboardModule.INFO_PARKING -> {
                            item {
                                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        val displayName = vehicle?.nickname?.takeIf { it.isNotBlank() } ?: "${vehicle?.brand ?: ""} ${vehicle?.model}"
                                        Text(displayName.uppercase(), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("ODÓMETRO: ${vehicle?.currentKm ?: "0"} ${prefs.unitDist.take(2).uppercase()} | ${vehicle?.fuelType?.uppercase() ?: "N/D"}", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)

                                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                                        if (vehicle?.parkingLat != null && vehicle?.parkingLon != null) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(modifier = Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
                                                    Icon(Icons.Default.LocalParking, "", tint = MaterialTheme.colorScheme.primary)
                                                }
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text("Vehículo Aparcado", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                                                    if (vehicle?.parkingTimeMillis != null) {
                                                        val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(vehicle!!.parkingTimeMillis!!))
                                                        Text("Ticket hasta las $timeStr", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                                                    }
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(12.dp))
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                OutlinedButton(onClick = {
                                                    viewModel.updateVehicle(vehicle!!.copy(parkingLat = null, parkingLon = null, parkingTimeMillis = null))
                                                    val intent = Intent(context, ParkingReceiver::class.java)
                                                    val pendingIntent = PendingIntent.getBroadcast(context, vehicleId.toInt(), intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                                                    alarmManager.cancel(pendingIntent)
                                                    triggerVibration(context, prefs)
                                                }, modifier = Modifier.weight(1f)) { Text("Liberar") }

                                                Button(onClick = {
                                                    val uriStr = "google.navigation:q=${vehicle!!.parkingLat},${vehicle!!.parkingLon}&mode=w"
                                                    val mapIntent = Intent(Intent.ACTION_VIEW, Uri.parse(uriStr)).apply { setPackage("com.google.android.apps.maps") }
                                                    try { context.startActivity(mapIntent) } catch (e: Exception) {}
                                                }, modifier = Modifier.weight(1f)) { Icon(Icons.Default.DirectionsWalk, ""); Spacer(Modifier.width(8.dp)); Text("Ir al coche") }
                                            }
                                        } else {
                                            OutlinedButton(onClick = {
                                                if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                                    showParkingDialog = true
                                                } else {
                                                    locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                                                }
                                            }, modifier = Modifier.fillMaxWidth()) {
                                                Icon(Icons.Default.LocationOn, ""); Spacer(Modifier.width(8.dp)); Text("He aparcado aquí")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        DashboardModule.DOCS -> {
                            item {
                                Button(onClick = { onNavigateToDocuments(vehicleId) }, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(8.dp), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)) {
                                    Icon(Icons.Default.Folder, ""); Spacer(Modifier.width(8.dp)); Text("DOCUMENTACIÓN Y PAPELES", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        DashboardModule.INTERVALS -> {
                            item {
                                OutlinedButton(onClick = { onNavigateToSettingsIntervals(vehicleId) }, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(8.dp)) {
                                    Icon(Icons.Default.SettingsSuggest, ""); Spacer(Modifier.width(8.dp)); Text("Personalizar atenciones", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        DashboardModule.EXPENSES -> {
                            item { Text("HISTORIAL DE GASTOS", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp)) }
                            items(expenses) { expense ->
                                val isSelected = selectedExpenseIds.contains(expense.id)
                                val cardColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else getCategoryColor(expense.category)

                                Card(
                                    modifier = Modifier.fillMaxWidth().combinedClickable(
                                        onClick = {
                                            if (selectedExpenseIds.isNotEmpty()) {
                                                selectedExpenseIds = if (isSelected) selectedExpenseIds - expense.id else selectedExpenseIds + expense.id
                                                triggerVibration(context, prefs)
                                            } else {
                                                onNavigateToEditExpense(vehicleId, expense.category, expense.id)
                                            }
                                        },
                                        onLongClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            selectedExpenseIds = if (isSelected) selectedExpenseIds - expense.id else selectedExpenseIds + expense.id
                                        }
                                    ),
                                    elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 8.dp else 2.dp),
                                    colors = CardDefaults.cardColors(containerColor = cardColor)
                                ) {
                                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                        if (isSelected) {
                                            Icon(Icons.Default.CheckCircle, "", modifier = Modifier.size(40.dp), tint = MaterialTheme.colorScheme.primary)
                                        } else {
                                            val icon = DashboardIconMap[expense.iconName] ?: Icons.Default.Build
                                            Icon(icon, "", modifier = Modifier.size(40.dp), tint = MaterialTheme.colorScheme.primary)
                                        }
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(expense.title.uppercase(), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                            Text(expense.category, style = MaterialTheme.typography.bodySmall)
                                        }
                                        val currSymbol = if (prefs.unitCurr.contains("$")) "$" else "€"
                                        Text("${expense.totalCost} $currSymbol", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // DIÁLOGO DE APARCAMIENTO
        if (showParkingDialog) {
            AlertDialog(
                onDismissRequest = { showParkingDialog = false; parkingMinutes = "" },
                title = { Text("Guardar Aparcamiento") },
                text = {
                    Column {
                        Text("¿Has puesto ticket ORA? Introduce los minutos para que te avisemos antes de que caduque (Opcional).")
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = parkingMinutes, onValueChange = { parkingMinutes = it.filter { c -> c.isDigit() } },
                            label = { Text("Minutos (0 = Sin aviso)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword)
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        try {
                            val loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                            if (loc != null) {
                                val mins = parkingMinutes.toLongOrNull() ?: 0L
                                val expireTime = if (mins > 0) System.currentTimeMillis() + (mins * 60 * 1000) else null

                                viewModel.updateVehicle(vehicle!!.copy(parkingLat = loc.latitude, parkingLon = loc.longitude, parkingTimeMillis = expireTime))
                                triggerVibration(context, prefs)

                                if (expireTime != null) {
                                    val notifyTime = expireTime - (15 * 60 * 1000)
                                    if (notifyTime > System.currentTimeMillis()) {
                                        val intent = Intent(context, ParkingReceiver::class.java)
                                        val pendingIntent = PendingIntent.getBroadcast(context, vehicleId.toInt(), intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                                        alarmManager.setExact(AlarmManager.RTC_WAKEUP, notifyTime, pendingIntent)
                                        Toast.makeText(context, "Aparcamiento y alarma guardados", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Aparcamiento guardado (Tiempo muy corto para alarma)", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    Toast.makeText(context, "Ubicación guardada", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Toast.makeText(context, "No se pudo obtener la ubicación GPS", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: SecurityException) { Toast.makeText(context, "Falta permiso GPS", Toast.LENGTH_SHORT).show() }

                        showParkingDialog = false
                        parkingMinutes = ""
                    }) { Text("Guardar Ubicación") }
                },
                dismissButton = { TextButton(onClick = { showParkingDialog = false }) { Text("Cancelar") } }
            )
        }
    }
}

@Composable
private fun FabMenuItem(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 12.dp)) {
        Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.padding(end = 12.dp), shadowElevation = 2.dp) {
            Text(text = title, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        }
        SmallFloatingActionButton(onClick = onClick, containerColor = MaterialTheme.colorScheme.secondaryContainer) { Icon(icon, contentDescription = title) }
    }
}

private fun getCategoryColor(category: String): androidx.compose.ui.graphics.Color {
    val baseColor = when (category) {
        "Repostaje" -> androidx.compose.ui.graphics.Color(0xFF4CAF50)
        "Mantenimiento" -> androidx.compose.ui.graphics.Color(0xFFFF9800)
        "Avería" -> androidx.compose.ui.graphics.Color(0xFFF44336)
        "Trámites" -> androidx.compose.ui.graphics.Color(0xFF2196F3)
        else -> androidx.compose.ui.graphics.Color(0xFF424242)
    }
    return baseColor.copy(alpha = 0.15f)
}

private val DashboardIconMap = mapOf(
    "Herramientas" to Icons.Default.Build,
    "Reparación" to Icons.Default.CarCrash,
    "Gasolinera" to Icons.Default.LocalGasStation,
    "Documento" to Icons.Default.Assignment
)