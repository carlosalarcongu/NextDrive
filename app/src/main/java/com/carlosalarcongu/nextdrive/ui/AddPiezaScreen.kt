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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPiezaScreen(vehicleId: Long, expenseId: Long? = null, viewModel: NextDriveViewModel, onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var cost by remember { mutableStateOf("") }

    Scaffold(topBar = { TopAppBar(title = { Text("COMPRA DE PIEZA") }, navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "") } }) }) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).padding(16.dp).fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Nombre de la Pieza/Accesorio") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = cost, onValueChange = { cost = it }, label = { Text("Coste Total (€)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())

            Spacer(modifier = Modifier.weight(1f))
            Button(onClick = {
                val t = cost.toDoubleOrNull()
                if (title.isNotBlank() && t != null) {
                    val exp = Expense(id = expenseId ?: 0, vehicleId = vehicleId, title = title, dateMillis = System.currentTimeMillis(), totalCost = t, category = "Pieza", iconName = "Herramientas")
                    if (expenseId == null) viewModel.addExpense(exp) else viewModel.updateExpense(exp)
                    onNavigateBack()
                } else Toast.makeText(context, "Faltan datos", Toast.LENGTH_SHORT).show()
            }, modifier = Modifier.fillMaxWidth()) { Text("GUARDAR") }
        }
    }
}