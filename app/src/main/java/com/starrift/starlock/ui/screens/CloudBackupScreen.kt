package com.starrift.starlock.ui.screens

import android.app.Activity
import android.view.WindowManager
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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

enum class CloudBackupMode {
    EXPORT, IMPORT
}

enum class CloudProvider {
    GOOGLE_DRIVE, WEBDAV
}

@Composable
fun CloudBackupScreen(mode: CloudBackupMode, onBackClick: () -> Unit) {
    val context = LocalContext.current
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

    var selectedProvider by remember { mutableStateOf<CloudProvider?>(null) }

    var webdavUrl by remember { mutableStateOf("") }
    var webdavUsername by remember { mutableStateOf("") }
    var webdavPassword by remember { mutableStateOf("") }
    var webdavPasswordVisible by remember { mutableStateOf(false) }
    var saveCredentials by remember { mutableStateOf(false) }

    val savedShortcuts = remember { mutableStateListOf<Pair<String, String>>() }
    var showSavedShortcuts by remember { mutableStateOf(false) }

    val screenTitle = when (mode) {
        CloudBackupMode.EXPORT -> "Cloud Export"
        CloudBackupMode.IMPORT -> "Cloud Import"
    }
    val actionLabel = when (mode) {
        CloudBackupMode.EXPORT -> "Export"
        CloudBackupMode.IMPORT -> "Import"
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
            Text(screenTitle, style = MaterialTheme.typography.titleLarge)
        }
        Spacer(Modifier.height(16.dp))

        if (selectedProvider == null) {
            Text(
                "Choose where to $actionLabel your data",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(16.dp))

            ProviderCard(
                icon = Icons.Default.Cloud,
                title = "Google Drive",
                subtitle = "Stored privately in your app's hidden Drive folder",
                onClick = { selectedProvider = CloudProvider.GOOGLE_DRIVE }
            )
            Spacer(Modifier.height(12.dp))
            ProviderCard(
                icon = Icons.Default.Storage,
                title = "WebDAV (Custom cloud server)",
                subtitle = "Connect to your own server using a URL, username, and password",
                onClick = { selectedProvider = CloudProvider.WEBDAV }
            )
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = { selectedProvider = null }) {
                    Text("Change provider")
                }
            }
            Spacer(Modifier.height(8.dp))

            when (selectedProvider) {
                CloudProvider.GOOGLE_DRIVE -> {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text("Google Drive", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Google Drive ${'$'}actionLabel is coming soon.",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
                CloudProvider.WEBDAV -> {
                    if (savedShortcuts.isNotEmpty()) {
                        OutlinedButton(
                            onClick = { showSavedShortcuts = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Saved Login Shortcuts")
                        }
                        Spacer(Modifier.height(16.dp))
                    }

                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text("WebDAV Server", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(16.dp))

                            OutlinedTextField(
                                value = webdavUrl,
                                onValueChange = { webdavUrl = it },
                                label = { Text("Server URL") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(Modifier.height(12.dp))
                            OutlinedTextField(
                                value = webdavUsername,
                                onValueChange = { webdavUsername = it },
                                label = { Text("Username") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(Modifier.height(12.dp))
                            OutlinedTextField(
                                value = webdavPassword,
                                onValueChange = { webdavPassword = it },
                                label = { Text("Password") },
                                visualTransformation = if (webdavPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                trailingIcon = {
                                    IconButton(onClick = { webdavPasswordVisible = !webdavPasswordVisible }) {
                                        Icon(
                                            if (webdavPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = if (webdavPasswordVisible) "Hide password" else "Show password"
                                        )
                                    }
                                },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(Modifier.height(12.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = saveCredentials,
                                    onCheckedChange = { saveCredentials = it }
                                )
                                Text(
                                    "Bu bilgilerimi sonraki islemler icin kaydet",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }

                            Spacer(Modifier.height(16.dp))
                            Button(
                                onClick = { },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(actionLabel)
                            }
                        }
                    }
                }
                else -> {}
            }
        }

        Spacer(Modifier.height(48.dp))
    }

    if (showSavedShortcuts) {
        AlertDialog(
            onDismissRequest = { showSavedShortcuts = false },
            title = { Text("Saved Login Shortcuts") },
            text = {
                Column {
                    savedShortcuts.forEach { (url, username) ->
                        Text("${'$'}username @ ${'$'}url", style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(8.dp))
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSavedShortcuts = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
private fun ProviderCard(
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
