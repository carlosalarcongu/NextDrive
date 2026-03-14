// UserGuideScreen.kt
package com.carlosalarcongu.nextdrive.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserGuideScreen(onNavigateBack: () -> Unit) {
    Scaffold(topBar = { TopAppBar(title = { Text("GUÍA DE USUARIO") }, navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "") } }) }) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Bienvenido a NextDrive", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text("• MANTENIMIENTOS: Añade las piezas que cambias para llevar el control.\n• KILOMETRAJE: Suma kilómetros directamente desde el panel de tu vehículo.\n• RECORDATORIOS: Agenda futuras revisiones exportándolas a tu calendario (.ics).\n• DOCUMENTACIÓN: Guarda fotos y PDFs de tus facturas.\n• ESTADÍSTICAS: Comprueba visualmente dónde estás gastando más dinero.", style = MaterialTheme.typography.bodyLarge)
        }
    }
}