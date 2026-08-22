package com.starrift.starlock.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import androidx.compose.ui.res.stringResource
import com.starrift.starlock.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun formatArchivedAt(millis: Long?): String {
    if (millis == null) return ""
    val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm (z)", Locale.getDefault())
    sdf.timeZone = TimeZone.getDefault()
    return sdf.format(Date(millis))
}

fun ArchivedScreen(viewModel: ArchivedViewModel, onBackClick: () -> Unit) {
    val selectedTab by viewModel.selectedTab.collectAsState()
    val archivedApps by viewModel.archivedApps.collectAsState()
    val archivedAccounts by viewModel.archivedAccounts.collectAsState()
    var confirmUnarchiveAction by remember { mutableStateOf<(() -> Unit)?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Arşivlenenler") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                SegmentedButton(
                    selected = selectedTab == ArchivedTab.APPS,
                    onClick = { viewModel.onTabChange(ArchivedTab.APPS) },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3)
                ) {
                    Text("Uygulamalar")
                }
                SegmentedButton(
                    selected = selectedTab == ArchivedTab.ACCOUNTS,
                    onClick = { viewModel.onTabChange(ArchivedTab.ACCOUNTS) },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3)
                ) {
                    Text("Hesaplar")
                }
                SegmentedButton(
                    selected = selectedTab == ArchivedTab.FIELDS,
                    onClick = { viewModel.onTabChange(ArchivedTab.FIELDS) },
                    shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3)
                ) {
                    Text("Terimler")
                }
            }

            when (selectedTab) {
                ArchivedTab.APPS -> {
                    if (archivedApps.isEmpty()) {
                        EmptyArchiveMessage("Arşivlenmiş uygulama yok")
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(archivedApps, key = { it.id }) { app ->
                                ArchivedRow(
                                    title = app.name,
                                    subtitle = formatArchivedAt(app.archivedAt),
                                    onUnarchive = { confirmUnarchiveAction = { viewModel.unarchiveApp(app.id) } }
                                )
                            }
                        }
                    }
                }
                ArchivedTab.ACCOUNTS -> {
                    if (archivedAccounts.isEmpty()) {
                        EmptyArchiveMessage("Arşivlenmiş hesap yok")
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(archivedAccounts, key = { it.id }) { account ->
                                ArchivedRow(
                                    title = account.name,
                                    subtitle = "${account.appName} • ${formatArchivedAt(account.archivedAt)}",
                                    onUnarchive = { confirmUnarchiveAction = { viewModel.unarchiveAccount(account.id) } }
                                )
                            }
                        }
                    }
                }
                ArchivedTab.FIELDS -> {
                    EmptyArchiveMessage("Bu bölüm yakında eklenecek")
                }
            }
        }
    }


    if (confirmUnarchiveAction != null) {
        AlertDialog(
            onDismissRequest = { confirmUnarchiveAction = null },
            title = { Text(stringResource(R.string.restore_confirm_title)) },
            text = { Text(stringResource(R.string.restore_confirm_text)) },
            confirmButton = {
                TextButton(onClick = {
                    confirmUnarchiveAction?.invoke()
                    confirmUnarchiveAction = null
                }) { Text(stringResource(R.string.restore)) }
            },
            dismissButton = {
                TextButton(onClick = { confirmUnarchiveAction = null }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }
}

@Composable
private fun ArchivedRow(
    title: String,
    subtitle: String?,
    onUnarchive: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.SemiBold)
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            IconButton(onClick = onUnarchive) {
                Icon(Icons.Default.Unarchive, contentDescription = "Arşivden çıkar")
            }
        }
    }
}

@Composable
private fun EmptyArchiveMessage(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
