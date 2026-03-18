package com.carlosalarcongu.nextdrive.ui

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.carlosalarcongu.nextdrive.data.Expense
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRepostajeScreen(vehicleId: Long, expenseId: Long? = null, viewModel: NextDriveViewModel, onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val expenseToEdit by if (expenseId != null) viewModel.getExpenseById(expenseId).collectAsState(null) else remember { mutableStateOf(null) }
    var isInitialized by remember { mutableStateOf(false) }

    var title by remember { mutableStateOf("Repostaje") }
    var pricePerLiter by remember { mutableStateOf("") }
    var liters by remember { mutableStateOf("") }
    var totalCost by remember { mutableStateOf("") }

    LaunchedEffect(expenseToEdit) {
        if (expenseToEdit != null && !isInitialized) {
            title = expenseToEdit!!.title
            pricePerLiter = expenseToEdit!!.pricePerLiter?.toString() ?: ""
            liters = expenseToEdit!!.liters?.toString() ?: ""
            totalCost = expenseToEdit!!.totalCost.toString()
            isInitialized = true
        }
    }

    Scaffold(topBar = { TopAppBar(title = { Text("REPOSTAJE") }, navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "") } }) }) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).padding(16.dp).fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Nombre (Ej: Repostaje Repsol)") }, modifier = Modifier.fillMaxWidth())

            // AUTOCOMPLETADO MATEMÁTICO
            OutlinedTextField(value = pricePerLiter, onValueChange = {
                pricePerLiter = it.replace(",", ".")
                val p = pricePerLiter.toDoubleOrNull(); val l = liters.toDoubleOrNull()
                if (p != null && l != null) totalCost = "%.2f".format(Locale.US, p * l)
            }, label = { Text("Precio por Litro (€/L)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())

            OutlinedTextField(value = liters, onValueChange = {
                liters = it.replace(",", ".")
                val l = liters.toDoubleOrNull(); val p = pricePerLiter.toDoubleOrNull()
                if (l != null && p != null) totalCost = "%.2f".format(Locale.US, p * l)
            }, label = { Text("Cantidad (Litros)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())

            OutlinedTextField(value = totalCost, onValueChange = {
                totalCost = it.replace(",", ".")
                val t = totalCost.toDoubleOrNull(); val l = liters.toDoubleOrNull()
                if (t != null && l != null && l > 0) pricePerLiter = "%.3f".format(Locale.US, t / l)
            }, label = { Text("Coste Total (€)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())

            Spacer(modifier = Modifier.weight(1f))
            Button(onClick = {
                val t = totalCost.toDoubleOrNull()
                if (title.isNotBlank() && t != null) {
                    val exp = Expense(id = expenseId ?: 0, vehicleId = vehicleId, title = title, dateMillis = expenseToEdit?.dateMillis ?: System.currentTimeMillis(), totalCost = t, category = "Repostaje", pricePerLiter = pricePerLiter.toDoubleOrNull(), liters = liters.toDoubleOrNull(), iconName = "Gasolinera")
                    if (expenseId == null) viewModel.addExpense(exp) else viewModel.updateExpense(exp)
                    onNavigateBack()
                } else Toast.makeText(context, "Faltan datos", Toast.LENGTH_SHORT).show()
            }, modifier = Modifier.fillMaxWidth()) { Text("GUARDAR") }
        }
    }
}