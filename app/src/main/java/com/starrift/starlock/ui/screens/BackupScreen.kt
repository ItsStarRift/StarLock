package com.starrift.starlock.ui.screens

import android.content.Context
import android.app.Activity
import android.view.WindowManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

enum class CloudProvider {
    GOOGLE_DRIVE, WEBDAV
}

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
fun BackupScreen(
    viewModel: SettingsViewModel,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val activity = context as? Activity
    DisposableEffect(Unit) {
        activity?.window?.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
        onDispose {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }

    val exportSuccessMsg = "Veriler dışa aktarıldı"
    val exportFailMsg = "Dışa aktarma başarısız oldu"
    val importSuccessMsg = "Veriler içe aktarıldı"
    val importFailMsg = "İçe aktarma başarısız oldu"

    var showExportPasswordDialog by remember { mutableStateOf(false) }
    var exportPassword by remember { mutableStateOf("") }
    var exportPasswordConfirm by remember { mutableStateOf("") }
    var exportPasswordError by remember { mutableStateOf<String?>(null) }
    var pendingExportPassword by remember { mutableStateOf<String?>(null) }

    var showCloudExportSheet by remember { mutableStateOf(false) }
    var showCloudImportSheet by remember { mutableStateOf(false) }

    var cloudExportProvider by remember { mutableStateOf<CloudProvider?>(null) }
    var cloudImportProvider by remember { mutableStateOf<CloudProvider?>(null) }

    var webdavExportUrl by remember { mutableStateOf("") }
    var webdavExportUsername by remember { mutableStateOf("") }
    var webdavExportPassword by remember { mutableStateOf("") }
    var webdavExportPasswordVisible by remember { mutableStateOf(false) }

    var webdavImportUrl by remember { mutableStateOf("") }
    var webdavImportUsername by remember { mutableStateOf("") }
    var webdavImportPassword by remember { mutableStateOf("") }
    var webdavImportPasswordVisible by remember { mutableStateOf(false) }
    var exportPasswordVisible by remember { mutableStateOf(false) }
    var exportPasswordConfirmVisible by remember { mutableStateOf(false) }

    val offlineExportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri: Uri? ->
        val password = pendingExportPassword
        if (uri != null && password != null) {
            scope.launch {
                val success = viewModel.exportEncryptedTo(context, uri, password)
                if (success) {
                    context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
                        .edit().putLong("last_offline_export_at", System.currentTimeMillis()).apply()
                }
                Toast.makeText(context, if (success) exportSuccessMsg else exportFailMsg, Toast.LENGTH_SHORT).show()
            }
        }
        pendingExportPassword = null
    }

    var pendingImportUri by remember { mutableStateOf<Uri?>(null) }
    var showImportConfirmDialog by remember { mutableStateOf(false) }
    var importPassword by remember { mutableStateOf("") }
    var importPasswordError by remember { mutableStateOf<String?>(null) }
    var importPasswordVisible by remember { mutableStateOf(false) }

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
                onClick = { showCloudExportSheet = true }
            )
            BackupActionCard(
                icon = Icons.Default.CloudDownload,
                title = "Import Data",
                subtitle = "from cloud",
                lastAction = formatBackupLastAction(context, "last_cloud_import_at"),
                modifier = Modifier.weight(1f),
                onClick = { showCloudImportSheet = true }
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
                onClick = {
                    exportPassword = ""
                    exportPasswordConfirm = ""
                    exportPasswordError = null
                    showExportPasswordDialog = true
                }
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
            onDismissRequest = {
                showImportConfirmDialog = false
                importPassword = ""
                importPasswordError = null
            },
            title = { Text("Import Data") },
            text = {
                Column {
                    Text("This action will replace ALL existing data on your device with the selected backup. Current data will be deleted and cannot be recovered.")
                    Spacer(Modifier.height(12.dp))
                    Text("Enter the backup password used when this file was exported.")
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = importPassword,
                        onValueChange = {
                            importPassword = it
                            importPasswordError = null
                        },
                        label = { Text("Backup Password") },
                        visualTransformation = if (importPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { importPasswordVisible = !importPasswordVisible }) {
                                Icon(
                                    if (importPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (importPasswordVisible) "Hide password" else "Show password"
                                )
                            }
                        },
                        singleLine = true,
                        isError = importPasswordError != null,
                        supportingText = importPasswordError?.let { { Text(it) } },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val uri = pendingImportUri
                    val password = importPassword
                    if (uri != null && password.isNotEmpty()) {
                        scope.launch {
                            val success = viewModel.importEncryptedFrom(context, uri, password)
                            if (success) {
                                context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
                                    .edit().putLong("last_offline_import_at", System.currentTimeMillis()).apply()
                                showImportConfirmDialog = false
                                importPassword = ""
                                importPasswordError = null
                                Toast.makeText(context, importSuccessMsg, Toast.LENGTH_SHORT).show()
                            } else {
                                importPasswordError = "Incorrect password or corrupted file"
                            }
                        }
                    }
                }) {
                    Text("Continue")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showImportConfirmDialog = false
                    importPassword = ""
                    importPasswordError = null
                }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showExportPasswordDialog) {
        AlertDialog(
            onDismissRequest = {
                showExportPasswordDialog = false
                exportPassword = ""
                exportPasswordConfirm = ""
                exportPasswordError = null
            },
            title = { Text("Set Backup Password") },
            text = {
                Column {
                    Text("This password encrypts your backup file. It is separate from your app lock password and is used only for this backup file.")
                    Spacer(Modifier.height(8.dp))
                    Text("If you forget it, this backup cannot be recovered. There is no way to reset it.")
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = exportPassword,
                        onValueChange = {
                            exportPassword = it
                            exportPasswordError = null
                        },
                        label = { Text("Backup Password") },
                        visualTransformation = if (exportPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { exportPasswordVisible = !exportPasswordVisible }) {
                                Icon(
                                    if (exportPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (exportPasswordVisible) "Hide password" else "Show password"
                                )
                            }
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = exportPasswordConfirm,
                        onValueChange = {
                            exportPasswordConfirm = it
                            exportPasswordError = null
                        },
                        label = { Text("Confirm Password") },
                        visualTransformation = if (exportPasswordConfirmVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { exportPasswordConfirmVisible = !exportPasswordConfirmVisible }) {
                                Icon(
                                    if (exportPasswordConfirmVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (exportPasswordConfirmVisible) "Hide password" else "Show password"
                                )
                            }
                        },
                        singleLine = true,
                        isError = exportPasswordError != null,
                        supportingText = exportPasswordError?.let { { Text(it) } },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    when {
                        exportPassword.length < 6 || exportPassword.length > 16 -> {
                            exportPasswordError = "Password must be 6-16 characters"
                        }
                        exportPassword != exportPasswordConfirm -> {
                            exportPasswordError = "Passwords do not match"
                        }
                        else -> {
                            pendingExportPassword = exportPassword
                            showExportPasswordDialog = false
                            exportPassword = ""
                            exportPasswordConfirm = ""
                            exportPasswordError = null
                            offlineExportLauncher.launch(backupFileName())
                        }
                    }
                }) {
                    Text("Continue")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showExportPasswordDialog = false
                    exportPassword = ""
                    exportPasswordConfirm = ""
                    exportPasswordError = null
                }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showCloudExportSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showCloudExportSheet = false
                cloudExportProvider = null
                webdavExportUrl = ""
                webdavExportUsername = ""
                webdavExportPassword = ""
            }
        ) {
            Column(modifier = Modifier.padding(horizontal = 20.dp).padding(bottom = 24.dp)) {
                Text("Cloud Export", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(16.dp))

                if (cloudExportProvider == null) {
                    Text("Choose where to export your data", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(16.dp))
                    CloudProviderCard(
                        icon = Icons.Default.Cloud,
                        title = "Google Drive",
                        subtitle = "Stored privately in your app's hidden Drive folder",
                        onClick = { cloudExportProvider = CloudProvider.GOOGLE_DRIVE }
                    )
                    Spacer(Modifier.height(12.dp))
                    CloudProviderCard(
                        icon = Icons.Default.Storage,
                        title = "WebDAV (Custom cloud server)",
                        subtitle = "Connect to your own server using a URL, username, and password",
                        onClick = { cloudExportProvider = CloudProvider.WEBDAV }
                    )
                } else {
                    TextButton(onClick = { cloudExportProvider = null }) {
                        Text("Change provider")
                    }
                    Spacer(Modifier.height(8.dp))
                    when (cloudExportProvider) {
                        CloudProvider.GOOGLE_DRIVE -> {
                            Text("Google Drive Export is coming soon.", style = MaterialTheme.typography.bodyMedium)
                        }
                        CloudProvider.WEBDAV -> {
                            OutlinedTextField(
                                value = webdavExportUrl,
                                onValueChange = { webdavExportUrl = it },
                                label = { Text("Server URL") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(Modifier.height(12.dp))
                            OutlinedTextField(
                                value = webdavExportUsername,
                                onValueChange = { webdavExportUsername = it },
                                label = { Text("Username") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(Modifier.height(12.dp))
                            OutlinedTextField(
                                value = webdavExportPassword,
                                onValueChange = { webdavExportPassword = it },
                                label = { Text("Password") },
                                visualTransformation = if (webdavExportPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                trailingIcon = {
                                    IconButton(onClick = { webdavExportPasswordVisible = !webdavExportPasswordVisible }) {
                                        Icon(
                                            if (webdavExportPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = if (webdavExportPasswordVisible) "Hide password" else "Show password"
                                        )
                                    }
                                },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(Modifier.height(16.dp))
                            Button(
                                onClick = { },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Export")
                            }
                        }
                        else -> {}
                    }
                }
            }
        }
    }

    if (showCloudImportSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showCloudImportSheet = false
                cloudImportProvider = null
                webdavImportUrl = ""
                webdavImportUsername = ""
                webdavImportPassword = ""
            }
        ) {
            Column(modifier = Modifier.padding(horizontal = 20.dp).padding(bottom = 24.dp)) {
                Text("Cloud Import", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(16.dp))

                if (cloudImportProvider == null) {
                    Text("Choose where to import your data from", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(16.dp))
                    CloudProviderCard(
                        icon = Icons.Default.Cloud,
                        title = "Google Drive",
                        subtitle = "Stored privately in your app's hidden Drive folder",
                        onClick = { cloudImportProvider = CloudProvider.GOOGLE_DRIVE }
                    )
                    Spacer(Modifier.height(12.dp))
                    CloudProviderCard(
                        icon = Icons.Default.Storage,
                        title = "WebDAV (Custom cloud server)",
                        subtitle = "Connect to your own server using a URL, username, and password",
                        onClick = { cloudImportProvider = CloudProvider.WEBDAV }
                    )
                } else {
                    TextButton(onClick = { cloudImportProvider = null }) {
                        Text("Change provider")
                    }
                    Spacer(Modifier.height(8.dp))
                    when (cloudImportProvider) {
                        CloudProvider.GOOGLE_DRIVE -> {
                            Text("Google Drive Import is coming soon.", style = MaterialTheme.typography.bodyMedium)
                        }
                        CloudProvider.WEBDAV -> {
                            OutlinedTextField(
                                value = webdavImportUrl,
                                onValueChange = { webdavImportUrl = it },
                                label = { Text("Server URL") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(Modifier.height(12.dp))
                            OutlinedTextField(
                                value = webdavImportUsername,
                                onValueChange = { webdavImportUsername = it },
                                label = { Text("Username") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(Modifier.height(12.dp))
                            OutlinedTextField(
                                value = webdavImportPassword,
                                onValueChange = { webdavImportPassword = it },
                                label = { Text("Password") },
                                visualTransformation = if (webdavImportPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                trailingIcon = {
                                    IconButton(onClick = { webdavImportPasswordVisible = !webdavImportPasswordVisible }) {
                                        Icon(
                                            if (webdavImportPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = if (webdavImportPasswordVisible) "Hide password" else "Show password"
                                        )
                                    }
                                },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(Modifier.height(16.dp))
                            Button(
                                onClick = { },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Import")
                            }
                        }
                        else -> {}
                    }
                }
            }
        }
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

@Composable
private fun CloudProviderCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(32.dp))
            Spacer(Modifier.width(16.dp))
            Column {
                Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
