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
    "Abarth" to listOf("595", "695"),
    "Alfa Romeo" to listOf("Giulia", "Stelvio", "Tonale"),
    "Aston Martin" to listOf("DB11", "DB12", "DBX", "Vantage"),
    "Audi" to listOf("A1", "A3", "A4", "A5", "A6", "A7", "A8", "Q2", "Q3", "Q5", "Q7", "Q8", "e-tron", "TT", "R8"),
    "BMW" to listOf("Serie 1", "Serie 2", "Serie 3", "Serie 4", "Serie 5", "Serie 7", "Serie 8", "X1", "X2", "X3", "X4", "X5", "X6", "X7", "i3", "i4", "iX", "M2", "M3", "M4"),
    "BYD" to listOf("Atto 3", "Dolphin", "Han", "Seal", "Tang"),
    "Citroën" to listOf("C3", "C3 Aircross", "C4", "C4 X", "C5 Aircross", "C5 X", "Berlingo"),
    "Cupra" to listOf("Ateca", "Born", "Formentor", "Leon", "Tavascan"),
    "Dacia" to listOf("Duster", "Jogger", "Logan", "Sandero", "Spring"),
    "DS Automobiles" to listOf("DS 3", "DS 4", "DS 7", "DS 9"),
    "Ferrari" to listOf("296 GTB", "812 Superfast", "Portofino M", "Roma", "Purosangue", "SF90 Stradale"),
    "Fiat" to listOf("500", "500e", "500X", "Panda", "Tipo", "Tipo Cross"),
    "Ford" to listOf("Fiesta", "Focus", "Puma", "Kuga", "Mustang", "Mustang Mach-E", "Explorer", "Bronco", "Ranger"),
    "Honda" to listOf("Civic", "CR-V", "HR-V", "Jazz", "e:Ny1", "ZR-V"),
    "Hyundai" to listOf("i10", "i20", "i30", "Kona", "Tucson", "Santa Fe", "IONIQ 5", "IONIQ 6"),
    "Jaguar" to listOf("E-PACE", "F-PACE", "I-PACE", "XE", "XF", "XJ", "F-TYPE"),
    "Jeep" to listOf("Avenger", "Compass", "Renegade", "Wrangler", "Grand Cherokee"),
    "Kia" to listOf("Picanto", "Rio", "Ceed", "Stonic", "Niro", "Sportage", "Sorento", "EV6", "EV9"),
    "Land Rover" to listOf("Defender", "Discovery", "Discovery Sport", "Range Rover", "Range Rover Evoque", "Range Rover Sport", "Range Rover Velar"),
    "Lexus" to listOf("CT", "ES", "IS", "NX", "RX", "UX", "RZ"),
    "Maserati" to listOf("Ghibli", "Levante", "Quattroporte", "Grecale", "MC20"),
    "Mazda" to listOf("Mazda2", "Mazda3", "Mazda6", "CX-30", "CX-5", "CX-60", "CX-80", "MX-30", "MX-5"),
    "Mercedes-Benz" to listOf("Clase A", "Clase B", "Clase C", "Clase CLA", "Clase E", "Clase G", "Clase GLA", "Clase GLC", "Clase GLE", "Clase S", "EQA", "EQB", "EQC", "EQE", "EQS"),
    "MG" to listOf("MG4", "MG5", "ZS", "HS", "Marvel R"),
    "MINI" to listOf("Cooper", "Clubman", "Countryman", "Aceman"),
    "Mitsubishi" to listOf("ASX", "Colt", "Eclipse Cross", "Space Star", "Outlander"),
    "Nissan" to listOf("Juke", "Leaf", "Micra", "Qashqai", "X-Trail", "Ariya"),
    "Opel" to listOf("Astra", "Corsa", "Crossland", "Grandland", "Mokka", "Zafira"),
    "Peugeot" to listOf("208", "2008", "308", "3008", "408", "508", "5008"),
    "Polestar" to listOf("Polestar 2", "Polestar 3", "Polestar 4"),
    "Porsche" to listOf("911", "Boxster", "Cayenne", "Cayman", "Macan", "Panamera", "Taycan"),
    "Renault" to listOf("Arkana", "Austral", "Captur", "Clio", "Espace", "Kangoo", "Megane", "Zoe", "Rafale"),
    "SEAT" to listOf("Arona", "Ateca", "Ibiza", "Leon", "Tarraco"),
    "Skoda" to listOf("Fabia", "Kamiq", "Karoq", "Kodiaq", "Octavia", "Scala", "Superb", "Enyaq"),
    "Smart" to listOf("Fortwo", "Forfour", "#1", "#3"),
    "Subaru" to listOf("Crosstrek", "Forester", "Impreza", "Outback", "Solterra"),
    "Suzuki" to listOf("Ignis", "Swift", "Vitara", "S-Cross", "Swace", "Across"),
    "Tesla" to listOf("Model 3", "Model Y", "Model S", "Model X"),
    "Toyota" to listOf("Aygo X", "bZ4X", "C-HR", "Corolla", "Highlander", "Hilux", "Land Cruiser", "Prius", "RAV4", "Supra", "Yaris", "Yaris Cross"),
    "Volkswagen" to listOf("Arteon", "Golf", "ID.3", "ID.4", "ID.5", "ID.7", "Passat", "Polo", "T-Cross", "T-Roc", "Tiguan", "Touareg", "Touran"),
    "Volvo" to listOf("C40", "EX30", "EX90", "S60", "S90", "V60", "V90", "XC40", "XC60", "XC90")
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

    var nickname by remember { mutableStateOf("") }
    var brand by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var engineName by remember { mutableStateOf("") }
    var horsepower by remember { mutableStateOf("") }
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
            nickname = vehicleToEdit!!.nickname ?: ""
            brand = vehicleToEdit!!.brand ?: ""
            model = vehicleToEdit!!.model
            engineName = vehicleToEdit!!.engineName ?: ""
            horsepower = vehicleToEdit!!.horsepower?.toString() ?: ""
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
        topBar = { TopAppBar(title = { Text(if (vehicleId == null) "AÑADIR VEHÍCULO" else "EDITAR VEHÍCULO", fontWeight = FontWeight.Bold) }, navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "") } }) }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {

            OutlinedTextField(value = nickname, onValueChange = { nickname = it }, label = { Text("Apodo del vehículo (Opcional)") }, modifier = Modifier.fillMaxWidth())

            ExposedDropdownMenuBox(expanded = expandedBrand, onExpandedChange = { expandedBrand = !expandedBrand }) {
                OutlinedTextField(value = brand, onValueChange = { brand = it; expandedBrand = true; if (!carDatabaseMock.containsKey(it)) model = "" }, label = { Text("Marca") }, modifier = Modifier.menuAnchor().fillMaxWidth(), trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedBrand) })
                if (filteredBrands.isNotEmpty() && brand.isNotBlank()) {
                    DropdownMenu(expanded = expandedBrand, onDismissRequest = { expandedBrand = false }, modifier = Modifier.exposedDropdownSize(), properties = PopupProperties(focusable = false)) {
                        filteredBrands.forEach { option -> DropdownMenuItem(text = { Text(option) }, onClick = { brand = option; expandedBrand = false }) }
                    }
                }
            }

            ExposedDropdownMenuBox(expanded = expandedModel, onExpandedChange = { expandedModel = !expandedModel }) {
                OutlinedTextField(value = model, onValueChange = { model = it; expandedModel = true }, label = { Text("Modelo*") }, modifier = Modifier.menuAnchor().fillMaxWidth(), isError = model.isBlank(), trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedModel) })
                if (filteredModels.isNotEmpty() && model.isNotBlank()) {
                    DropdownMenu(expanded = expandedModel, onDismissRequest = { expandedModel = false }, modifier = Modifier.exposedDropdownSize(), properties = PopupProperties(focusable = false)) {
                        filteredModels.forEach { option -> DropdownMenuItem(text = { Text(option) }, onClick = { model = option; expandedModel = false }) }
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = engineName, onValueChange = { engineName = it }, label = { Text("Motorización") }, modifier = Modifier.weight(1f))
                OutlinedTextField(value = horsepower, onValueChange = { horsepower = it.filter { char -> char.isDigit() } }, label = { Text("CV") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(0.5f))
            }

            GradientDivider()

            OutlinedTextField(value = licensePlate, onValueChange = { licensePlate = it.filter { char -> char.isLetterOrDigit() }.uppercase() }, label = { Text("Matrícula") }, keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters), modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = vin, onValueChange = { vin = it.filter { char -> char.isLetterOrDigit() }.uppercase() }, label = { Text("Bastidor") }, keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters), modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = year, onValueChange = { year = it.filter { char -> char.isDigit() } }, label = { Text("Año") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(value = currentKm, onValueChange = { currentKm = it.filter { char -> char.isDigit() } }, label = { Text("Kilómetros") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
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

            OutlinedTextField(value = acquisitionCost, onValueChange = { acquisitionCost = it.filter { char -> char.isDigit() || char == '.' || char == ',' } }, label = { Text("Coste (€)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (model.isNotBlank()) {
                        val savedVehicle = Vehicle(
                            id = vehicleId ?: 0, nickname = nickname.takeIf { it.isNotBlank() }, brand = brand.takeIf { it.isNotBlank() }, model = model,
                            engineName = engineName.takeIf { it.isNotBlank() }, horsepower = horsepower.toIntOrNull(), year = year.toIntOrNull(), currentKm = currentKm.toIntOrNull(),
                            fuelType = fuelType, acquisitionCost = acquisitionCost.toDoubleOrNull(), isDailyUse = isDailyUse, isSecondHand = isSecondHand,
                            licensePlate = licensePlate.takeIf { it.isNotBlank() }, vin = vin.takeIf { it.isNotBlank() }, imageUri = vehicleToEdit?.imageUri
                        )
                        if (vehicleId == null) viewModel.addVehicle(savedVehicle) else viewModel.updateVehicle(savedVehicle)
                        Toast.makeText(context, "Guardado", Toast.LENGTH_SHORT).show()
                        onNavigateBack()
                    } else { Toast.makeText(context, "Modelo obligatorio", Toast.LENGTH_SHORT).show() }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text(if (vehicleId == null) "GUARDAR" else "ACTUALIZAR", modifier = Modifier.padding(8.dp), fontSize = MaterialTheme.typography.titleMedium.fontSize) }
        }

        if (showCustomFuelDialog) {
            AlertDialog(onDismissRequest = { showCustomFuelDialog = false }, title = { Text("Combustible") }, text = { OutlinedTextField(value = customFuelText, onValueChange = { customFuelText = it }, singleLine = true) }, confirmButton = { TextButton(onClick = { if (customFuelText.isNotBlank()) fuelType = customFuelText; showCustomFuelDialog = false }) { Text("Aceptar") } }, dismissButton = { TextButton(onClick = { showCustomFuelDialog = false }) { Text("Cancelar") } })
        }
    }
}