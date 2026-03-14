// AddVehicleScreen.kt
package com.carlosalarcongu.nextdrive.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.SportsMotorsports
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import com.carlosalarcongu.nextdrive.data.Vehicle

val carDatabaseMock = mapOf(
    "Audi" to listOf("A3", "A4", "A6", "Q5", "TT"),
    "BMW" to listOf("Serie 1", "Serie 3", "X5", "M4"),
    "Ford" to listOf("Focus", "Fiesta", "Mustang", "Kuga"),
    "Seat" to listOf("Ibiza", "Leon", "Ateca"),
    "Toyota" to listOf("Corolla", "Yaris", "Supra", "RAV4")
)

@Composable
fun GradientDivider() {
    Box(modifier = Modifier.fillMaxWidth().height(2.dp).background(brush = Brush.horizontalGradient(colors = listOf(Color.Transparent, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), Color.Transparent))))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddVehicleScreen(vehicleId: Long? = null, viewModel: NextDriveViewModel, onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val vehicleToEdit by if (vehicleId != null) viewModel.getVehicleById(vehicleId).collectAsState(null) else remember { mutableStateOf(null) }
    var isInitialized by remember { mutableStateOf(false) }

    var brand by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var expandedBrand by remember { mutableStateOf(false) }
    var expandedModel by remember { mutableStateOf(false) }

    var licensePlate by remember { mutableStateOf("") }
    var vin by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }
    var currentKm by remember { mutableStateOf("") }
    var acquisitionCost by remember { mutableStateOf("") }
    var fuelType by remember { mutableStateOf("Gasolina") }
    var showCustomFuelDialog by remember { mutableStateOf(false) }
    var customFuelText by remember { mutableStateOf("") }
    var isDailyUse by remember { mutableStateOf(true) }
    var isSecondHand by remember { mutableStateOf(true) }

    LaunchedEffect(vehicleToEdit) {
        if (vehicleToEdit != null && !isInitialized) {
            brand = vehicleToEdit!!.brand ?: ""
            model = vehicleToEdit!!.model
            licensePlate = vehicleToEdit!!.licensePlate ?: ""
            vin = vehicleToEdit!!.vin ?: ""
            year = vehicleToEdit!!.year?.toString() ?: ""
            currentKm = vehicleToEdit!!.currentKm?.toString() ?: ""
            fuelType = vehicleToEdit!!.fuelType ?: "Gasolina"
            acquisitionCost = vehicleToEdit!!.acquisitionCost?.toString() ?: ""
            isDailyUse = vehicleToEdit!!.isDailyUse
            isSecondHand = vehicleToEdit!!.isSecondHand
            isInitialized = true
        }
    }

    val filteredBrands = carDatabaseMock.keys.filter { it.contains(brand, ignoreCase = true) }
    val availableModels = carDatabaseMock[brand] ?: emptyList()
    val filteredModels = availableModels.filter { it.contains(model, ignoreCase = true) }

    Scaffold(
        topBar = { TopAppBar(title = { Text(if (vehicleId == null) "Añadir Vehículo" else "Editar Vehículo", fontWeight = FontWeight.Bold) }, navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "") } }) }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(if (carDatabaseMock.containsKey(brand)) Icons.Rounded.DirectionsCar else Icons.Rounded.SportsMotorsports, "", modifier = Modifier.size(100.dp), tint = MaterialTheme.colorScheme.primary)

            ExposedDropdownMenuBox(expanded = expandedBrand, onExpandedChange = { expandedBrand = !expandedBrand }) {
                OutlinedTextField(
                    value = brand, onValueChange = { brand = it; expandedBrand = true; if (!carDatabaseMock.containsKey(it)) model = "" },
                    label = { Text("Marca") }, modifier = Modifier.menuAnchor().fillMaxWidth(), trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedBrand) }
                )
                if (filteredBrands.isNotEmpty() && brand.isNotBlank()) {
                    DropdownMenu(expanded = expandedBrand, onDismissRequest = { expandedBrand = false }, modifier = Modifier.exposedDropdownSize(), properties = PopupProperties(focusable = false)) {
                        filteredBrands.forEach { option -> DropdownMenuItem(text = { Text(option) }, onClick = { brand = option; expandedBrand = false }) }
                    }
                }
            }

            ExposedDropdownMenuBox(expanded = expandedModel, onExpandedChange = { expandedModel = !expandedModel }) {
                OutlinedTextField(
                    value = model, onValueChange = { model = it; expandedModel = true },
                    label = { Text("Modelo*") }, modifier = Modifier.menuAnchor().fillMaxWidth(), isError = model.isBlank(), trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedModel) }
                )
                if (filteredModels.isNotEmpty() && model.isNotBlank()) {
                    DropdownMenu(expanded = expandedModel, onDismissRequest = { expandedModel = false }, modifier = Modifier.exposedDropdownSize(), properties = PopupProperties(focusable = false)) {
                        filteredModels.forEach { option -> DropdownMenuItem(text = { Text(option) }, onClick = { model = option; expandedModel = false }) }
                    }
                }
            }

            GradientDivider()

            OutlinedTextField(value = licensePlate, onValueChange = { licensePlate = it }, label = { Text("Matrícula") }, keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters), modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = vin, onValueChange = { vin = it }, label = { Text("Bastidor") }, keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters), modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = year, onValueChange = { year = it }, label = { Text("Año") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(value = currentKm, onValueChange = { currentKm = it }, label = { Text("Kilómetros") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    IconButton(onClick = { currentKm = ((currentKm.toIntOrNull() ?: 0) + 25000).toString() }) { Icon(Icons.Default.Add, "") }
                    IconButton(onClick = { currentKm = maxOf(0, (currentKm.toIntOrNull() ?: 0) - 25000).toString() }) { Icon(Icons.Default.Remove, "") }
                }
            }

            GradientDivider()

            Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Combustible:", fontWeight = FontWeight.Bold)
                FilterChip(selected = fuelType == "Gasolina", onClick = { fuelType = "Gasolina" }, label = { Text("Gasolina") })
                FilterChip(selected = fuelType == "Diésel", onClick = { fuelType = "Diésel" }, label = { Text("Diésel") })
                FilterChip(selected = (fuelType != "Gasolina" && fuelType != "Diésel"), onClick = { showCustomFuelDialog = true }, label = { Text("Otro") })

                HorizontalDivider(modifier = Modifier.height(24.dp).width(1.dp))
                Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(checked = isDailyUse, onCheckedChange = { isDailyUse = it }); Text("Uso Diario") }
                Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(checked = isSecondHand, onCheckedChange = { isSecondHand = it }); Text("2ª Mano") }
            }

            OutlinedTextField(value = acquisitionCost, onValueChange = { acquisitionCost = it }, label = { Text("Coste (€)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (model.isNotBlank()) {
                        val savedVehicle = Vehicle(
                            id = vehicleId ?: 0, brand = brand.takeIf { it.isNotBlank() }, model = model, year = year.toIntOrNull(), currentKm = currentKm.toIntOrNull(),
                            fuelType = fuelType, acquisitionCost = acquisitionCost.toDoubleOrNull(), isDailyUse = isDailyUse, isSecondHand = isSecondHand,
                            licensePlate = licensePlate.uppercase().takeIf { it.isNotBlank() }, vin = vin.uppercase().takeIf { it.isNotBlank() }
                        )
                        if (vehicleId == null) viewModel.addVehicle(savedVehicle) else viewModel.updateVehicle(savedVehicle)
                        Toast.makeText(context, "Guardado", Toast.LENGTH_SHORT).show()
                        onNavigateBack()
                    } else {
                        Toast.makeText(context, "Modelo obligatorio", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text(if (vehicleId == null) "Guardar" else "Actualizar", modifier = Modifier.padding(8.dp), fontSize = MaterialTheme.typography.titleMedium.fontSize) }
        }

        if (showCustomFuelDialog) {
            AlertDialog(onDismissRequest = { showCustomFuelDialog = false }, title = { Text("Combustible") }, text = { OutlinedTextField(value = customFuelText, onValueChange = { customFuelText = it }, singleLine = true) }, confirmButton = { TextButton(onClick = { if (customFuelText.isNotBlank()) fuelType = customFuelText; showCustomFuelDialog = false }) { Text("Aceptar") } }, dismissButton = { TextButton(onClick = { showCustomFuelDialog = false }) { Text("Cancelar") } })
        }
    }
}