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
import androidx.compose.material.icons.filled.*
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
fun SettingsScreen(
    viewModel: NextDriveViewModel,
    themeMode: String, colorPalette: String, fontSize: String,
    unitDist: String, unitCurr: String, unitVol: String, dateFormat: String,
    onUpdatePref: (String, String) -> Unit
) {
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        uri?.let {
            viewModel.exportDatabaseToJson { jsonString ->
                context.contentResolver.openOutputStream(it)?.use { os -> os.write(jsonString.toByteArray()) }
                Toast.makeText(context, "Copia de seguridad exportada", Toast.LENGTH_LONG).show()
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            try {
                val inputStream = context.contentResolver.openInputStream(it)
                val jsonString = BufferedReader(InputStreamReader(inputStream)).use { reader -> reader.readText() }
                viewModel.importDatabaseFromJson(jsonString) { success ->
                    if (success) Toast.makeText(context, "Datos restaurados", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) { Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show() }
        }
    }

    Scaffold(topBar = { TopAppBar(title = { Text("AJUSTES", fontWeight = FontWeight.Bold) }) }) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState())) {

            // --- DATOS ---
            SettingsSectionTitle("Datos y Privacidad", Icons.Default.Storage)
            SettingsListItem("Importar Datos", "Restaura una copia de seguridad JSON") { importLauncher.launch(arrayOf("application/json")) }
            SettingsListItem("Exportar Local", "Guarda en la memoria del dispositivo") { exportLauncher.launch("NextDrive_Backup.json") }
            SettingsListItem("Exportar a Google Drive", "Usa el selector del sistema para subir a la nube") { exportLauncher.launch("NextDrive_Backup.json") }
            SettingsListItem("Borrar Todos los Datos", "Acción destructiva e irreversible", MaterialTheme.colorScheme.error) { showDeleteDialog = true }
            SettingsListItem("Política de Privacidad", "Consulta cómo tratamos (o no) tus datos") { showPrivacyDialog = true }

            // --- TEMA DE LA APLICACIÓN ---
            SettingsSectionTitle("Tema y Apariencia", Icons.Default.ColorLens)
            SettingsDropdown("Modo", listOf("SYSTEM", "DARK", "LIGHT", "OTRO (Próximamente)"), themeMode) { onUpdatePref("theme", it) }
            SettingsDropdown("Tamaño de Letra", listOf("PEQUEÑO", "MEDIANO", "GRANDE"), fontSize) { onUpdatePref("fontSize", it) }
            SettingsDropdown("Colores", listOf("VAMPIRIC", "FRUTAL", "MONOCROMÁTICO"), colorPalette) { onUpdatePref("palette", it) }

            // --- UNIDADES ---
            SettingsSectionTitle("Unidades de Medida", Icons.Default.Straighten)
            SettingsDropdown("Distancia", listOf("Kilómetros", "Millas"), unitDist) { onUpdatePref("unitDist", it) }
            SettingsDropdown("Moneda", listOf("Euros (€)", "Dólares ($)"), unitCurr) { onUpdatePref("unitCurr", it) }
            SettingsDropdown("Volumen", listOf("Litros", "Galones"), unitVol) { onUpdatePref("unitVol", it) }
            SettingsDropdown("Formato Fecha", listOf("Sistema", "dd/mm/yyyy", "mm/dd/yyyy", "dd/mm/yy"), dateFormat) { onUpdatePref("dateFormat", it) }

            // --- COMPARTIR Y TIENDA ---
            SettingsSectionTitle("Comunidad y Mejoras", Icons.Default.Share)
            SettingsListItem("Compartir Aplicación", "Recomienda NextDrive a un amigo") {
                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                    putExtra(Intent.EXTRA_TEXT, "¡Prueba NextDrive para gestionar tu coche! https://github.com/carlosalarcongu/NextDrive.git")
                    type = "text/plain"
                }
                context.startActivity(Intent.createChooser(sendIntent, "Compartir vía..."))
            }
            SettingsListItem("Quitar Publicidad", "Próximamente", MaterialTheme.colorScheme.onSurfaceVariant) { Toast.makeText(context, "Próximamente", Toast.LENGTH_SHORT).show() }
            SettingsListItem("Comprar Plaza de Garaje", "Próximamente", MaterialTheme.colorScheme.onSurfaceVariant) { Toast.makeText(context, "Próximamente", Toast.LENGTH_SHORT).show() }
            SettingsListItem("Ayuda Personalizada VIP", "Próximamente", MaterialTheme.colorScheme.onSurfaceVariant) { Toast.makeText(context, "Próximamente", Toast.LENGTH_SHORT).show() }

            Spacer(modifier = Modifier.height(32.dp))
        }

        if (showPrivacyDialog) {
            AlertDialog(
                onDismissRequest = { showPrivacyDialog = false }, title = { Text("Política de Privacidad") },
                text = { Text("NextDrive es una aplicación de gestión local. Todos los datos que introduces (vehículos, gastos, documentos, fotos) se almacenan exclusivamente en la memoria interna de tu dispositivo. No recopilamos, transmitimos, ni vendemos información personal a servidores de terceros.\n\nLa conexión a internet se utiliza únicamente para consultar apis públicas (como el precio de la gasolina) y cargar los mapas de OSMDroid.") },
                confirmButton = { TextButton(onClick = { showPrivacyDialog = false }) { Text("Entendido") } }
            )
        }
        if (showDeleteDialog) {
            AlertDialog(onDismissRequest = { showDeleteDialog = false }, title = { Text("¿BORRAR TODO?") }, text = { Text("Se eliminará todo tu garaje y gastos. Es irreversible.") }, confirmButton = { Button(onClick = { viewModel.deleteAllData(); showDeleteDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { Text("BORRAR") } }, dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Cancelar") } })
        }
    }
}

@Composable
fun SettingsSectionTitle(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp)) {
        Icon(icon, "", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
    }
    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
}

@Composable
fun SettingsListItem(title: String, subtitle: String, color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface, onClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(horizontal = 16.dp, vertical = 12.dp)) {
        Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = color)
        Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDropdown(label: String, options: List<String>, selected: String, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Row(modifier = Modifier.fillMaxWidth().clickable { expanded = true }.padding(horizontal = 16.dp, vertical = 12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
            Text(selected, color = MaterialTheme.colorScheme.primary, modifier = Modifier.menuAnchor())
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                options.forEach { opt -> DropdownMenuItem(text = { Text(opt) }, onClick = { onSelect(opt); expanded = false }) }
            }
        }
    }
}