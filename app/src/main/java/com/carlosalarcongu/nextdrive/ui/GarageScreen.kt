package com.carlosalarcongu.nextdrive.ui

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.carlosalarcongu.nextdrive.data.Vehicle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

fun getBrandLogoResId(context: Context, brand: String?): Int {
    if (brand.isNullOrBlank()) return 0
    val sanitized = brand.lowercase().replace(" ", "_").replace("-", "_")

    // Primer intento con el prefijo "logo_"
    val logoName = "logo_$sanitized"
    var resId = context.resources.getIdentifier(logoName, "drawable", context.packageName)

    // Si el primer intento falla (resId es 0), intenta sin el prefijo
    if (resId == 0) {
        val backupName = sanitized
        resId = context.resources.getIdentifier(backupName, "drawable", context.packageName)
    }

    return resId
}

@Composable
fun VehicleImageLoader(vehicle: Vehicle, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var bitmap by remember(vehicle.imageUri) { mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null) }

    LaunchedEffect(vehicle.imageUri) {
        if (!vehicle.imageUri.isNullOrBlank()) {
            withContext(Dispatchers.IO) {
                try {
                    val inputStream = context.contentResolver.openInputStream(Uri.parse(vehicle.imageUri))
                    bitmap = BitmapFactory.decodeStream(inputStream)?.asImageBitmap()
                } catch (e: Exception) { e.printStackTrace() }
            }
        }
    }

    val brandLogoResId = getBrandLogoResId(context, vehicle.brand)

    if (bitmap != null) {
        Image(bitmap = bitmap!!, contentDescription = "Foto Vehículo", modifier = modifier, contentScale = ContentScale.Crop)
    } else if (brandLogoResId != 0) {
        Image(painter = painterResource(id = brandLogoResId), contentDescription = "Logo Marca", modifier = modifier.padding(16.dp), contentScale = ContentScale.Fit)
    } else {
        Icon(Icons.Rounded.DirectionsCar, contentDescription = null, modifier = modifier.padding(32.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GarageScreen(viewModel: NextDriveViewModel, isDarkTheme: Boolean, onThemeToggle: () -> Unit, onNavigateToAddVehicle: () -> Unit, onVehicleClick: (Long) -> Unit, onNavigateToUserGuide: () -> Unit) {
    val vehicles by viewModel.allVehicles.collectAsState()
    val context = LocalContext.current
    var showSettingsMenu by remember { mutableStateOf(false) }

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        uri?.let {
            viewModel.exportDatabaseToJson { jsonString ->
                context.contentResolver.openOutputStream(it)?.use { os ->
                    os.write(jsonString.toByteArray())
                    // Necesitamos volver al hilo principal para el Toast
                    (context as android.app.Activity).runOnUiThread {
                        Toast.makeText(context, "Copia de seguridad exportada con éxito", Toast.LENGTH_LONG).show()
                    }
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
                        else Toast.makeText(context, "Error: Archivo no válido o corrupto", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error al leer el archivo", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("MI GARAJE", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    Box {
                        IconButton(onClick = { showSettingsMenu = true }) { Icon(Icons.Default.Settings, "Opciones") }
                        DropdownMenu(expanded = showSettingsMenu, onDismissRequest = { showSettingsMenu = false }) {
                            DropdownMenuItem(text = { Text("Cambiar Tema") }, onClick = { onThemeToggle(); showSettingsMenu = false })
                            HorizontalDivider()
                            DropdownMenuItem(text = { Text("Exportar Copia de Seguridad") }, onClick = { exportLauncher.launch("NextDrive_Backup.json"); showSettingsMenu = false })
                            DropdownMenuItem(text = { Text("Restaurar Datos (Importar)") }, onClick = { importLauncher.launch(arrayOf("application/json")); showSettingsMenu = false })
                            HorizontalDivider()
                            DropdownMenuItem(text = { Text("Ver código en GitHub") }, onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/carlosalarcongu/NextDrive.git"))); showSettingsMenu = false })
                            DropdownMenuItem(text = { Text("Borrar TODOS los datos", color = MaterialTheme.colorScheme.error) }, onClick = { viewModel.deleteAllData(); showSettingsMenu = false })
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        floatingActionButton = { FloatingActionButton(onClick = onNavigateToAddVehicle, containerColor = MaterialTheme.colorScheme.primary) { Icon(Icons.Default.Add, "Añadir Vehículo") } }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (vehicles.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) { Text("Garaje vacío.\nPulsa + para registrar un vehículo.", textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            } else {
                LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(vehicles) { vehicle ->
                        Card(modifier = Modifier.fillMaxWidth().clickable { onVehicleClick(vehicle.id) }, elevation = CardDefaults.cardElevation(defaultElevation = 4.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                            Column {
                                Box(modifier = Modifier.fillMaxWidth().height(160.dp).background(MaterialTheme.colorScheme.background), contentAlignment = Alignment.Center) {
                                    VehicleImageLoader(vehicle = vehicle, modifier = Modifier.fillMaxSize())
                                }
                                Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        val displayName = vehicle.nickname?.takeIf { it.isNotBlank() } ?: "${vehicle.brand ?: ""} ${vehicle.model}"
                                        Text(text = displayName.uppercase(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                        if (!vehicle.licensePlate.isNullOrBlank()) Text(text = vehicle.licensePlate, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                                    }
                                    Text(text = "${vehicle.currentKm ?: 0} KM", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            Column(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Card(modifier = Modifier.fillMaxWidth().clickable { onNavigateToUserGuide() }, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                    Text(text = "MANTENIMIENTOS • KILOMETRAJE • RECORDATORIOS • DOCUMENTACIÓN • ESTADÍSTICAS", modifier = Modifier.padding(16.dp).fillMaxWidth(), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}