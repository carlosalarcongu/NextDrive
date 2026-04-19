package com.carlosalarcongu.nextdrive.ui

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.carlosalarcongu.nextdrive.data.Document
import com.carlosalarcongu.nextdrive.data.DocumentFolder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentPanelScreen(vehicleId: Long, viewModel: NextDriveViewModel, onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val allDocs by viewModel.getDocumentsForVehicle(vehicleId).collectAsState(initial = emptyList())
    val allFolders by viewModel.getFoldersForVehicle(vehicleId).collectAsState(initial = emptyList())

    var currentFolderId by remember { mutableStateOf<Long?>(null) }
    var showNewFolderDialog by remember { mutableStateOf(false) }
    var folderNameText by remember { mutableStateOf("") }

    var showRenameDialog by remember { mutableStateOf<Document?>(null) }
    var docRenameText by remember { mutableStateOf("") }

    val docsToShow = allDocs.filter { it.folderId == currentFolderId }

    val documentLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri?.let {
            context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            val mimeType = context.contentResolver.getType(it) ?: "application/octet-stream"
            val name = it.lastPathSegment ?: "Nuevo Documento"
            viewModel.addDocument(Document(vehicleId = vehicleId, folderId = currentFolderId, name = name, uriString = it.toString(), mimeType = mimeType))
            Toast.makeText(context, "Guardado", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (currentFolderId == null) "DOCUMENTACIÓN" else allFolders.find{it.id == currentFolderId}?.name?.uppercase() ?: "CARPETA") },
                navigationIcon = {
                    IconButton(onClick = { if (currentFolderId != null) currentFolderId = null else onNavigateBack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "") }
                }, windowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp)
            )
        },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (currentFolderId == null) {
                    SmallFloatingActionButton(onClick = { showNewFolderDialog = true }, containerColor = MaterialTheme.colorScheme.secondaryContainer) { Icon(Icons.Default.CreateNewFolder, "Carpeta") }
                }
                ExtendedFloatingActionButton(onClick = { documentLauncher.launch(arrayOf("application/pdf", "image/*")) }, containerColor = MaterialTheme.colorScheme.primary) { Text("SUBIR ARCHIVO") }
            }
        }
    ) { paddingValues ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(paddingValues), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {

            // Si estamos en la raíz, mostramos las carpetas primero
            if (currentFolderId == null && allFolders.isNotEmpty()) {
                item { Text("Carpetas", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary) }
                items(allFolders) { folder ->
                    Card(modifier = Modifier.fillMaxWidth().clickable { currentFolderId = folder.id }, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Folder, "", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(folder.name, modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                            IconButton(onClick = { viewModel.deleteFolder(folder) }) { Icon(Icons.Default.Delete, "", tint = MaterialTheme.colorScheme.error) }
                        }
                    }
                }
                item { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)) }
            }

            if (docsToShow.isEmpty()) {
                item { Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) { Text("No hay archivos aquí.") } }
            } else {
                items(docsToShow) { doc ->
                    Card(modifier = Modifier.fillMaxWidth().clickable {
                        try { context.startActivity(Intent(Intent.ACTION_VIEW).apply { setDataAndType(Uri.parse(doc.uriString), doc.mimeType); flags = Intent.FLAG_GRANT_READ_URI_PERMISSION }) } catch (e: Exception) { Toast.makeText(context, "Sin app", Toast.LENGTH_SHORT).show() }
                    }) {
                        Row(modifier = Modifier.padding(8.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            // VISTA PREVIA DEL DOCUMENTO
                            Box(modifier = Modifier.size(60.dp).clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.surface), contentAlignment = Alignment.Center) {
                                if (doc.mimeType.startsWith("image")) {
                                    // Usamos un simple icono de imagen si es PNG/JPG (Opcionalmente puedes copiar la funcion UriImageLoader de GarageScreen aqui si quieres fotos reales en miniatura)
                                    Icon(Icons.Default.InsertDriveFile, "", tint = MaterialTheme.colorScheme.primary)
                                } else {
                                    Icon(Icons.Default.PictureAsPdf, "", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(doc.name, modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, maxLines = 2)
                            IconButton(onClick = { docRenameText = doc.name; showRenameDialog = doc }) { Icon(Icons.Default.Edit, "") }
                            IconButton(onClick = { viewModel.deleteDocument(doc) }) { Icon(Icons.Default.Delete, "", tint = MaterialTheme.colorScheme.error) }
                        }
                    }
                }
            }
        }

        if (showNewFolderDialog) {
            AlertDialog(onDismissRequest = { showNewFolderDialog = false }, title = { Text("Nueva Carpeta") }, text = { OutlinedTextField(value = folderNameText, onValueChange = { folderNameText = it }, singleLine = true) }, confirmButton = { Button(onClick = { viewModel.addFolder(DocumentFolder(vehicleId = vehicleId, name = folderNameText)); showNewFolderDialog = false; folderNameText = "" }) { Text("Crear") } }, dismissButton = { TextButton(onClick = { showNewFolderDialog = false }) { Text("Cancelar") } })
        }

        if (showRenameDialog != null) {
            AlertDialog(onDismissRequest = { showRenameDialog = null }, title = { Text("Renombrar") }, text = { OutlinedTextField(value = docRenameText, onValueChange = { docRenameText = it }, singleLine = true) }, confirmButton = { Button(onClick = { viewModel.updateDocument(showRenameDialog!!.copy(name = docRenameText)); showRenameDialog = null }) { Text("Guardar") } }, dismissButton = { TextButton(onClick = { showRenameDialog = null }) { Text("Cancelar") } })
        }
    }
}