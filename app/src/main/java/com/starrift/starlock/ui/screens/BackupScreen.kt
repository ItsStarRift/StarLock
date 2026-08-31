package com.starrift.starlock.ui.screens

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private fun backupFileName(): String {
    val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
    return "StarLock-Backup-${sdf.format(Date())}.starlk"
}

private fun formatBackupLastAction(context: Context, key: String): Pair<String, String>? {
    val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
    val millis = prefs.getLong(key, -1L)
    if (millis == -1L) return null
    val datePart = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    val timePart = SimpleDateFormat("HH:mm (z)", Locale.getDefault())
    datePart.timeZone = TimeZone.getDefault()
    timePart.timeZone = TimeZone.getDefault()
    val date = Date(millis)
    return Pair(datePart.format(date), timePart.format(date))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupScreen(viewModel: SettingsViewModel, onBackClick: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val exportSuccessMsg = "Veriler dışa aktarıldı"
    val exportFailMsg = "Dışa aktarma başarısız oldu"
    val importSuccessMsg = "Veriler içe aktarıldı"
    val importFailMsg = "İçe aktarma başarısız oldu"

    val offlineExportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                val success = viewModel.exportTo(context, uri)
                if (success) {
                    context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
                        .edit().putLong("last_offline_export_at", System.currentTimeMillis()).apply()
                }
                Toast.makeText(context, if (success) exportSuccessMsg else exportFailMsg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    var pendingImportUri by remember { mutableStateOf<Uri?>(null) }
    var showImportConfirmDialog by remember { mutableStateOf(false) }

    val offlineImportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            pendingImportUri = uri
            showImportConfirmDialog = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(24.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Spacer(Modifier.width(8.dp))
            Text(
                "Backup",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.CloudUpload,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(Modifier.height(8.dp))
                Text("Backup", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text(
                    "Welcome to Backup. In this page you can export or import your data backups as offline or cloud. If you have a opinion about this page or app please tell me in email.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }

        Spacer(Modifier.height(24.dp))
        Text("Cloud Backup", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            BackupActionCard(
                icon = Icons.Default.CloudUpload,
                title = "Export Data",
                subtitle = "to cloud",
                lastAction = formatBackupLastAction(context, "last_cloud_export_at"),
                modifier = Modifier.weight(1f),
                onClick = {
                    Toast.makeText(context, "Yakında kullanılabilir olacak", Toast.LENGTH_SHORT).show()
                }
            )
            BackupActionCard(
                icon = Icons.Default.CloudDownload,
                title = "Import Data",
                subtitle = "from cloud",
                lastAction = formatBackupLastAction(context, "last_cloud_import_at"),
                modifier = Modifier.weight(1f),
                onClick = {
                    Toast.makeText(context, "Yakında kullanılabilir olacak", Toast.LENGTH_SHORT).show()
                }
            )
        }

        Spacer(Modifier.height(24.dp))
        Text("Offline Backup", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            BackupActionCard(
                icon = Icons.Default.Upload,
                title = "Export Data",
                subtitle = "as a .starlk file",
                lastAction = formatBackupLastAction(context, "last_offline_export_at"),
                modifier = Modifier.weight(1f),
                onClick = { offlineExportLauncher.launch(backupFileName()) }
            )
            BackupActionCard(
                icon = Icons.Default.Download,
                title = "Import Data",
                subtitle = "a .starlk file",
                lastAction = formatBackupLastAction(context, "last_offline_import_at"),
                modifier = Modifier.weight(1f),
                onClick = { offlineImportLauncher.launch(arrayOf("application/octet-stream")) }
            )
        }

        Spacer(Modifier.height(96.dp))
    }

    if (showImportConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showImportConfirmDialog = false },
            title = { Text("Import Data") },
            text = { Text("This action will replace ALL existing data on your device with the selected backup. Current data will be deleted and cannot be recovered. Continue?") },
            confirmButton = {
                TextButton(onClick = {
                    showImportConfirmDialog = false
                    val uri = pendingImportUri
                    if (uri != null) {
                        scope.launch {
                            val success = viewModel.importFrom(context, uri)
                            if (success) {
                                context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
                                    .edit().putLong("last_offline_import_at", System.currentTimeMillis()).apply()
                            }
                            Toast.makeText(context, if (success) importSuccessMsg else importFailMsg, Toast.LENGTH_SHORT).show()
                        }
                    }
                }) {
                    Text("Continue")
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun BackupActionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    lastAction: Pair<String, String>?,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(32.dp))
            Spacer(Modifier.height(8.dp))
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            if (lastAction != null) {
                Spacer(Modifier.height(6.dp))
                Text(
                    "${lastAction.first} ${lastAction.second}",
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}
