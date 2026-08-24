@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)

package com.starrift.starlock.ui.screens

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Person
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.background
import androidx.compose.material3.*
import androidx.compose.ui.layout.ContentScale
import com.starrift.starlock.util.fieldIconFor
import coil.compose.AsyncImage
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchivedScreen(viewModel: ArchivedViewModel, onBackClick: () -> Unit) {
    val selectedTab by viewModel.selectedTab.collectAsState()
    val archivedApps by viewModel.archivedApps.collectAsState()
    val archivedAccounts by viewModel.archivedAccounts.collectAsState()
    val archivedFields by viewModel.archivedFields.collectAsState()
    val isSelectionMode by viewModel.isSelectionMode.collectAsState()
    val selectedIds by viewModel.selectedIds.collectAsState()
    var confirmUnarchiveAction by remember { mutableStateOf<(() -> Unit)?>(null) }
        var confirmDeleteAction by remember { mutableStateOf<(() -> Unit)?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (isSelectionMode) "${selectedIds.size} seçildi" else "Arşivlenenler")
                },
                navigationIcon = {
                    IconButton(onClick = { if (isSelectionMode) viewModel.exitSelectionMode() else onBackClick() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
                    }
                },
                actions = {
                    if (isSelectionMode) {
                        IconButton(onClick = { confirmUnarchiveAction = { viewModel.unarchiveSelected() } }) {
                            Icon(Icons.Default.Unarchive, contentDescription = "Arşivden çıkar")
                        }
                        IconButton(onClick = { confirmDeleteAction = { viewModel.permanentlyDeleteSelected() } }) {
                            Icon(Icons.Default.DeleteForever, contentDescription = stringResource(R.string.delete_permanently))
                        }
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
                        iconPath = app.iconPath,
                        isApp = true,
                                isSelectionMode = isSelectionMode,
                                isSelected = selectedIds.contains(app.id),
                                onClick = { if (isSelectionMode) viewModel.toggleSelection(app.id) },
                                onLongClick = { viewModel.enterSelectionMode(app.id) },
                                    onUnarchive = { confirmUnarchiveAction = { viewModel.unarchiveApp(app.id) } },
                        onDelete = { confirmDeleteAction = { viewModel.permanentlyDeleteSingle(app.id) } }
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
                        iconPath = account.iconPath,
                        isAccount = true,
                isApp = false,
                                isSelectionMode = isSelectionMode,
                                isSelected = selectedIds.contains(account.id),
                                onClick = { if (isSelectionMode) viewModel.toggleSelection(account.id) },
                                onLongClick = { viewModel.enterSelectionMode(account.id) },
                                    onUnarchive = { confirmUnarchiveAction = { viewModel.unarchiveAccount(account.id) } },
                        onDelete = { confirmDeleteAction = { viewModel.permanentlyDeleteSingle(account.id) } }
                                )
                            }
                        }
                    }
                }
                ArchivedTab.FIELDS -> {
                    if (archivedFields.isEmpty()) {
                        EmptyArchiveMessage("Arşivlenmiş terim yok")
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(archivedFields, key = { it.id }) { field ->
                                ArchivedRow(
                                    title = field.label,
                                    subtitle = "${field.accountName} • ${formatArchivedAt(field.archivedAt)}",
                                    iconPath = null,
                                    isField = true,
                                isSelectionMode = isSelectionMode,
                                isSelected = selectedIds.contains(field.id),
                                onClick = { if (isSelectionMode) viewModel.toggleSelection(field.id) },
                                onLongClick = { viewModel.enterSelectionMode(field.id) },
                                    onUnarchive = { confirmUnarchiveAction = { viewModel.unarchiveField(field.id) } },
                        onDelete = { confirmDeleteAction = { viewModel.permanentlyDeleteSingle(field.id) } }
                                )
                            }
                        }
                    }
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

        if (confirmDeleteAction != null) {
            AlertDialog(
                onDismissRequest = { confirmDeleteAction = null },
                title = { Text(stringResource(R.string.delete_permanently_confirm_title)) },
                text = { Text(stringResource(R.string.delete_permanently_confirm_text)) },
                confirmButton = {
                    TextButton(onClick = {
                        confirmDeleteAction?.invoke()
                        confirmDeleteAction = null
                    }) { Text(stringResource(R.string.delete_permanently)) }
                },
                dismissButton = {
                    TextButton(onClick = { confirmDeleteAction = null }) { Text(stringResource(R.string.cancel)) }
                }
            )
        }

@Composable
private fun ArchivedRow(
    title: String,
    subtitle: String?,
    iconPath: String? = null,
    isApp: Boolean = true,
    isAccount: Boolean = false,
    isField: Boolean = false,
    isSelectionMode: Boolean = false,
    isSelected: Boolean = false,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
    onUnarchive: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
        if (isField) {
            Icon(
                fieldIconFor(title),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        } else if (isApp || isAccount) {
            val iconShape = if (isApp) RoundedCornerShape(12.dp) else CircleShape
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(iconShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                if (iconPath != null) {
                    AsyncImage(
                        model = iconPath,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize().clip(iconShape),
                        contentScale = ContentScale.Crop,
                    )
                } else if (isApp) {
                    Text(
                        title.take(1).uppercase(),
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
            Spacer(Modifier.width(12.dp))
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
            if (isSelectionMode) {
                Checkbox(checked = isSelected, onCheckedChange = { onClick() })
            } else {
                IconButton(onClick = onUnarchive) {
                    Icon(Icons.Default.Unarchive, contentDescription = "Arşivden çıkar")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.DeleteForever, contentDescription = stringResource(R.string.delete_permanently), tint = MaterialTheme.colorScheme.error)
                }
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
