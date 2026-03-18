package com.carlosalarcongu.nextdrive.ui

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.ImportExport
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.io.BufferedReader
import java.io.InputStreamReader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: NextDriveViewModel, currentTheme: String, onThemeChange: (String) -> Unit) {
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        uri?.let {
            viewModel.exportDatabaseToJson { jsonString ->
                context.contentResolver.openOutputStream(it)?.use { os ->
                    os.write(jsonString.toByteArray())
                    (context as android.app.Activity).runOnUiThread { Toast.makeText(context, "Copia de seguridad exportada", Toast.LENGTH_LONG).show() }
                }
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            try {
                val inputStream = context.contentResolver.openInputStream(it)
                val jsonString = BufferedReader(InputStreamReader(inputStream)).use { reader -> reader.readText() }
                viewModel.importDatabaseFromJson(jsonString) { success ->
                    (context as android.app.Activity).runOnUiThread {
                        if (success) Toast.makeText(context, "Datos restaurados correctamente", Toast.LENGTH_LONG).show()
                        else Toast.makeText(context, "Error: Archivo no válido", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) { Toast.makeText(context, "Error al leer el archivo", Toast.LENGTH_SHORT).show() }
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("AJUSTES", fontWeight = FontWeight.Bold) }, colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)) }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(24.dp)) {

            // TEMA
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ColorLens, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Apariencia de la aplicación", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { onThemeChange("SYSTEM") }) {
                        RadioButton(selected = currentTheme == "SYSTEM", onClick = { onThemeChange("SYSTEM") })
                        Text("Mismo que el sistema")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { onThemeChange("DARK") }) {
                        RadioButton(selected = currentTheme == "DARK", onClick = { onThemeChange("DARK") })
                        Text("Siempre Oscuro (Vampírico)")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { onThemeChange("LIGHT") }) {
                        RadioButton(selected = currentTheme == "LIGHT", onClick = { onThemeChange("LIGHT") })
                        Text("Siempre Claro")
                    }
                }
            }

            // DATOS
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Gestión de Datos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { exportLauncher.launch("NextDrive_Backup.json") }, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.Save, "", modifier = Modifier.padding(end = 8.dp))
                        Text("Exportar Copia de Seguridad")
                    }
                    Button(onClick = { importLauncher.launch(arrayOf("application/json")) }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary, contentColor = MaterialTheme.colorScheme.onSecondary)) {
                        Icon(Icons.Default.ImportExport, "", modifier = Modifier.padding(end = 8.dp))
                        Text("Importar Datos (Restaurar)")
                    }
                }
            }

            // PELIGRO
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Zona de Peligro", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { showDeleteDialog = true }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                        Icon(Icons.Default.DeleteForever, "", modifier = Modifier.padding(end = 8.dp))
                        Text("Borrar TODOS los datos")
                    }
                }
            }

            TextButton(onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/carlosalarcongu/NextDrive.git"))) }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Text("VER CÓDIGO EN GITHUB", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false }, title = { Text("¿Estás seguro?") },
                text = { Text("Esto borrará permanentemente todos tus vehículos, gastos y documentos. No se puede deshacer.") },
                confirmButton = { Button(onClick = { viewModel.deleteAllData(); showDeleteDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { Text("BORRAR TODO") } },
                dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Cancelar") } }
            )
        }
    }
}