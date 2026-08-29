@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)

package com.starrift.starlock.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.starrift.starlock.R
import com.starrift.starlock.data.AccountFieldWithAccountName
import com.starrift.starlock.data.AccountWithAppName
import com.starrift.starlock.data.AppItem
import com.starrift.starlock.util.fieldIconFor
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private fun formatDeletedAt(millis: Long?): String {
    if (millis == null) return ""
    val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm (z)", Locale.getDefault())
    sdf.timeZone = TimeZone.getDefault()
    return sdf.format(Date(millis))
}

@Composable
fun TrashScreen(viewModel: TrashViewModel, onBackClick: () -> Unit) {
    val selectedTab by viewModel.selectedTab.collectAsState()
    val isSelectionMode by viewModel.isSelectionMode.collectAsState()
    val selectedIds by viewModel.selectedIds.collectAsState()
    val deletedApps by viewModel.deletedApps.collectAsState()
    val deletedAccounts by viewModel.deletedAccounts.collectAsState()
    val deletedFields by viewModel.deletedFields.collectAsState()

    var confirmRestoreAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    var confirmDeleteAction by remember { mutableStateOf<(() -> Unit)?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Text(
                    if (isSelectionMode) stringResource(R.string.trash_selected_count, selectedIds.size)
                    else stringResource(R.string.trash)
                )
            },
            navigationIcon = {
                IconButton(onClick = { if (isSelectionMode) viewModel.exitSelectionMode() else onBackClick() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null)
                }
            },
            actions = {
                if (isSelectionMode) {
                    IconButton(onClick = { confirmRestoreAction = { viewModel.restoreSelected() } }) {
                        Icon(Icons.Default.Unarchive, contentDescription = stringResource(R.string.restore))
                    }
                    IconButton(onClick = { confirmDeleteAction = { viewModel.permanentlyDeleteSelected() } }) {
                        Icon(Icons.Default.DeleteForever, contentDescription = stringResource(R.string.delete_permanently))
                    }
                }
            }
        )

        SingleChoiceSegmentedButtonRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            SegmentedButton(
                selected = selectedTab == TrashTab.APPS,
                onClick = { viewModel.onTabChange(TrashTab.APPS) },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3)
            ) { Text(stringResource(R.string.trash_tab_apps)) }
            SegmentedButton(
                selected = selectedTab == TrashTab.ACCOUNTS,
                onClick = { viewModel.onTabChange(TrashTab.ACCOUNTS) },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3)
            ) { Text(stringResource(R.string.trash_tab_accounts)) }
            SegmentedButton(
                selected = selectedTab == TrashTab.FIELDS,
                onClick = { viewModel.onTabChange(TrashTab.FIELDS) },
                shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3)
            ) { Text(stringResource(R.string.trash_tab_fields)) }
        }

        val isEmpty = when (selectedTab) {
            TrashTab.APPS -> deletedApps.isEmpty()
            TrashTab.ACCOUNTS -> deletedAccounts.isEmpty()
            TrashTab.FIELDS -> deletedFields.isEmpty()
        }

        if (isEmpty) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    stringResource(R.string.trash_empty),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
            ) {
                when (selectedTab) {
                    TrashTab.APPS -> items(deletedApps, key = { it.id }) { app ->
                        TrashRow(
                            title = app.name,
                            subtitle = formatDeletedAt(app.deletedAt),
                            iconPath = app.iconPath,
                            isApp = true,
                            isSelectionMode = isSelectionMode,
                            isSelected = selectedIds.contains(app.id),
                            onClick = {
                                if (isSelectionMode) viewModel.toggleSelection(app.id)
                            },
                            onLongClick = { viewModel.enterSelectionMode(app.id) },
                            onRestore = { confirmRestoreAction = { viewModel.restoreSingle(app.id) } },
                            onDelete = { confirmDeleteAction = { viewModel.permanentlyDeleteSingle(app.id) } }
                        )
                    }
                    TrashTab.ACCOUNTS -> items(deletedAccounts, key = { it.id }) { account ->
                        TrashRow(
                            title = account.name,
                            subtitle = "${account.appName} • ${formatDeletedAt(account.deletedAt)}",
                            iconPath = account.iconPath,
                        isApp = false,
                            isAccount = true,
                            isSelectionMode = isSelectionMode,
                            isSelected = selectedIds.contains(account.id),
                            onClick = {
                                if (isSelectionMode) viewModel.toggleSelection(account.id)
                            },
                            onLongClick = { viewModel.enterSelectionMode(account.id) },
                            onRestore = { confirmRestoreAction = { viewModel.restoreSingle(account.id) } },
                            onDelete = { confirmDeleteAction = { viewModel.permanentlyDeleteSingle(account.id) } }
                        )
                    }
                    TrashTab.FIELDS -> items(deletedFields, key = { it.id }) { field ->
                        TrashRow(
                            title = field.label,
                        isField = true,
                            subtitle = "${field.accountName} • ${formatDeletedAt(field.deletedAt)}",
                            isSelectionMode = isSelectionMode,
                            isSelected = selectedIds.contains(field.id),
                            onClick = {
                                if (isSelectionMode) viewModel.toggleSelection(field.id)
                            },
                            onLongClick = { viewModel.enterSelectionMode(field.id) },
                            onRestore = { confirmRestoreAction = { viewModel.restoreSingle(field.id) } },
                            onDelete = { confirmDeleteAction = { viewModel.permanentlyDeleteSingle(field.id) } }
                        )
                    }
                }
            }
        }
    }

    if (confirmRestoreAction != null) {
        AlertDialog(
            onDismissRequest = { confirmRestoreAction = null },
            title = { Text(stringResource(R.string.restore_confirm_title)) },
            text = { Text(stringResource(R.string.restore_confirm_text)) },
            confirmButton = {
                TextButton(onClick = {
                    confirmRestoreAction?.invoke()
                    confirmRestoreAction = null
                }) { Text(stringResource(R.string.restore)) }
            },
            dismissButton = {
                TextButton(onClick = { confirmRestoreAction = null }) { Text(stringResource(R.string.cancel)) }
            }
        )
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
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun TrashRow(
    title: String,
    subtitle: String,
    iconPath: String? = null,
    isApp: Boolean = true,
    isAccount: Boolean = false,
    isField: Boolean = false,
    isSelectionMode: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onRestore: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
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
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        if (isSelectionMode) {
            Checkbox(checked = isSelected, onCheckedChange = { onClick() })
        } else {
            IconButton(onClick = onRestore) {
                Icon(Icons.Default.Unarchive, contentDescription = stringResource(R.string.restore))
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.DeleteForever,
                    contentDescription = stringResource(R.string.delete_permanently),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
    Spacer(modifier = Modifier.height(6.dp))
}
