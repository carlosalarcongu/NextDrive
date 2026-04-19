package com.carlosalarcongu.nextdrive.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserGuideScreen(onNavigateBack: () -> Unit) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("GUÍA DE USUARIO") }, navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver") } }, windowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp)) }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {

            item {
                Text("Bienvenido a NextDrive", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text("Descubre cómo sacar el máximo provecho a la gestión de tus vehículos con estos ejemplos prácticos.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(8.dp))
            }

            item { GuideSection(Icons.Default.Build, "1. Gestión de Mantenimientos", "Registra cualquier intervención mecánica, pieza o repostaje.", "Ejemplo: Creas un gasto llamado 'Cambio de Aceite', seleccionas la categoría 'Consumible' e indicas el precio. La app calculará este dato en tus estadísticas globales.") }

            item { GuideSection(Icons.Default.AddRoad, "2. Control de Kilometraje", "Mantén el cuentakilómetros siempre actualizado sin entrar a editar el vehículo completo.", "Ejemplo: Llegas a casa tras un viaje. Entras al panel de tu coche, pulsas el botón '+' junto a los kilómetros y sumas '450'. Fácil y rápido.") }

            item { GuideSection(Icons.Default.Event, "3. Recordatorios Inteligentes", "Que no se te pase la ITV ni el cambio de distribución.", "Ejemplo: Añades 'Renovación de Seguro'. Activas el recordatorio por tiempo (1 Año) y seleccionas 'Calendario'. La app generará un archivo .ics para que lo añadas directamente a Google Calendar o Apple Calendar.") }

            item { GuideSection(Icons.Default.InsertDriveFile, "4. Documentación Segura", "Tus papeles importantes siempre en el bolsillo.", "Ejemplo: Acabas de pasar la ITV. Le haces una foto al informe favorable (o descargas el PDF) y lo subes al apartado 'Documentación' de tu vehículo para tenerlo a mano ante cualquier control.") }

            item { GuideSection(Icons.Default.BarChart, "5. Estadísticas Visuales", "Analiza en qué se va el dinero de tu coche.", "Ejemplo: Entras a estadísticas y seleccionas el gráfico 'Circular'. De un vistazo verás que el 60% de tu gasto es en 'Repostaje' y un 40% en 'Mantenimientos'.") }

            item { GuideSection(Icons.Default.ImportExport, "6. Copias de Seguridad", "Tus datos son tuyos. Evita perderlos al cambiar de móvil.", "Ejemplo: Vas al menú (rueda dentada) en 'Mi Garaje' y pulsas 'Exportar Copia'. Se generará un archivo JSON con absolutamente todos tus coches y gastos que puedes guardar en la nube.") }
        }
    }
}

@Composable
fun GuideSection(icon: ImageVector, title: String, desc: String, example: String) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(desc, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)) {
                Text(example, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(12.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}