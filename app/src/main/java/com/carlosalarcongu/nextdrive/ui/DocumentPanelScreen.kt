// DocumentPanelScreen.kt
package com.carlosalarcongu.nextdrive.ui

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.carlosalarcongu.nextdrive.data.Document

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentPanelScreen(vehicleId: Long, viewModel: NextDriveViewModel, onNavigateBack: () -> Unit) {
    val documents by viewModel.getDocumentsForVehicle(vehicleId).collectAsState(initial = emptyList())
    val context = LocalContext.current

    val documentLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri?.let {
            context.contentResolver.takePersistableUriPermission(it, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            val mimeType = context.contentResolver.getType(it) ?: "application/octet-stream"
            val name = it.lastPathSegment ?: "Documento"
            viewModel.addDocument(Document(vehicleId = vehicleId, name = name, uriString = it.toString(), mimeType = mimeType))
            Toast.makeText(context, "Documento guardado", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Documentación") },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver") } }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { documentLauncher.launch(arrayOf("application/pdf", "image/*")) },
                icon = { Icon(Icons.Default.Add, "Añadir") },
                text = { Text("Añadir Documento") }
            )
        }
    ) { paddingValues ->
        if (documents.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text("No hay documentos guardados.")
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(paddingValues), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(documents) { doc ->
                    Card(modifier = Modifier.fillMaxWidth().clickable {
                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                            setDataAndType(Uri.parse(doc.uriString), doc.mimeType)
                            flags = android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                        }
                        try { context.startActivity(intent) } catch (e: Exception) { Toast.makeText(context, "No hay app para abrir esto", Toast.LENGTH_SHORT).show() }
                    }) {
                        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Icon(if (doc.mimeType.startsWith("image")) Icons.Default.Image else Icons.Default.InsertDriveFile, "", modifier = Modifier.size(40.dp))
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(doc.name, modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                            IconButton(onClick = { viewModel.deleteDocument(doc) }) { Icon(Icons.Default.Delete, "Borrar", tint = MaterialTheme.colorScheme.error) }
                        }
                    }
                }
            }
        }
    }
}