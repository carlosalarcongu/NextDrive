package com.carlosalarcongu.nextdrive.ui

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.carlosalarcongu.nextdrive.data.Expense

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTramiteScreen(vehicleId: Long, expenseId: Long? = null, viewModel: NextDriveViewModel, onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var cost by remember { mutableStateOf("") }
    var uris by remember { mutableStateOf(listOf<String>()) }

    val docLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            uris = uris + it.toString()
        }
    }

    Scaffold(topBar = { TopAppBar(title = { Text("TRÁMITES Y PAPELEO") }, navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "") } }, windowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp)) }) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).padding(16.dp).fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Nombre (Ej: ITV, Seguro, Impuesto)") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = cost, onValueChange = { cost = it }, label = { Text("Coste (€)") }, modifier = Modifier.fillMaxWidth())

            Button(onClick = { docLauncher.launch(arrayOf("application/pdf", "image/*")) }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)) {
                Icon(Icons.Default.AttachFile, ""); Spacer(modifier = Modifier.width(8.dp)); Text("Adjuntar Documento")
            }
            if (uris.isNotEmpty()) Text("${uris.size} documentos adjuntos listos para guardar.", color = MaterialTheme.colorScheme.primary)

            Spacer(modifier = Modifier.weight(1f))
            Button(onClick = {
                val t = cost.toDoubleOrNull() ?: 0.0
                if (title.isNotBlank()) {
                    val exp = Expense(id = expenseId ?: 0, vehicleId = vehicleId, title = title, dateMillis = System.currentTimeMillis(), totalCost = t, category = "Trámites", attachedDocumentsUris = uris.joinToString(","), iconName = "Multa/Tasas")
                    if (expenseId == null) viewModel.addExpense(exp) else viewModel.updateExpense(exp)
                    onNavigateBack()
                } else Toast.makeText(context, "Falta Título", Toast.LENGTH_SHORT).show()
            }, modifier = Modifier.fillMaxWidth()) { Text("GUARDAR") }
        }
    }
}