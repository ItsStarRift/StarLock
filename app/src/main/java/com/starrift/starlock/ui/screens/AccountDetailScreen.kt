package com.starrift.starlock.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.starrift.starlock.R
import androidx.compose.ui.unit.dp
import com.starrift.starlock.data.AccountField
import com.starrift.starlock.ui.components.AddFieldDialog
import com.starrift.starlock.util.fieldIconFor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountDetailScreen(
    viewModel: AccountDetailViewModel,
    onBackClick: () -> Unit
) {
    val fields by viewModel.fields.collectAsState()
    val selectedIds by viewModel.selectedIds.collectAsState()
    val selectionMode by viewModel.isSelectionMode.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showArchiveConfirm by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            if (selectionMode) {
                TopAppBar(
                    title = { Text(stringResource(R.string.field_selected_count, selectedIds.size)) },
                    navigationIcon = {
                        IconButton(onClick = { viewModel.exitSelectionMode() }) {
                            Icon(Icons.Default.Close, contentDescription = stringResource(R.string.cd_close))
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { showEditDialog = true },
                            enabled = selectedIds.size == 1
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.cd_edit))
                        }
                        IconButton(onClick = { showArchiveConfirm = true }) {
                            Icon(Icons.Default.Archive, contentDescription = stringResource(R.string.cd_archive_field))
                        }
                        IconButton(onClick = { showDeleteConfirm = true }) {
                            Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete_confirm), tint = MaterialTheme.colorScheme.error)
                        }
                    }
                )
            } else {
                TopAppBar(
                    title = { Text(stringResource(R.string.account_detail_title)) },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.cd_back))
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            if (!selectionMode) {
                FloatingActionButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.cd_add_field))
                }
            }
        }
    ) { padding ->
        if (fields.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.detail_empty), style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(
                contentPadding = padding,
                modifier = Modifier.fillMaxSize()
            ) {
                itemsIndexed(fields, key = { _, field -> field.id }) { index, field ->
                    FieldItemCard(
                        field = field,
                        selectionMode = selectionMode,
                        isSelected = field.id in selectedIds,
                        canMoveUp = index > 0,
                        canMoveDown = index < fields.size - 1,
                        onToggleSelect = { viewModel.toggleSelect(field.id) },
                        onEnterSelection = { viewModel.enterSelectionMode() },
                        onMoveUp = { viewModel.moveField(field.id, -1) },
                        onMoveDown = { viewModel.moveField(field.id, 1) }
                    )
                }
            }
        }

        if (showAddDialog) {
            AddFieldDialog(
                onDismiss = { showAddDialog = false },
                onConfirm = { label, value, isCustom ->
                    viewModel.addField(label, value, isCustom)
                    showAddDialog = false
                }
            )
        }

        if (showEditDialog) {
            val editingField = fields.find { it.id in selectedIds }
            if (editingField != null) {
                AddFieldDialog(
                    onDismiss = { showEditDialog = false },
                    onConfirm = { label, value, isCustom ->
                        viewModel.editField(editingField.id, label, value, isCustom)
                        showEditDialog = false
                    },
                    existingField = editingField
                )
            } else {
                showEditDialog = false
            }
        }

        if (showDeleteConfirm) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirm = false },
                title = { Text(stringResource(R.string.field_selected_count, selectedIds.size)) },
                text = { Text(stringResource(R.string.delete_field_text)) },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.deleteSelectedFields()
                        showDeleteConfirm = false
                    }) { Text(stringResource(R.string.delete_confirm), color = MaterialTheme.colorScheme.error) }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirm = false }) { Text(stringResource(R.string.cancel)) }
                }
            )
        }

        if (showArchiveConfirm) {
            AlertDialog(
                onDismissRequest = { showArchiveConfirm = false },
                title = { Text(stringResource(R.string.archive_confirm_title)) },
                text = { Text(stringResource(R.string.archive_confirm_text)) },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.archiveSelectedFields()
                        showArchiveConfirm = false
                    }) { Text(stringResource(R.string.archive)) }
                },
                dismissButton = {
                    TextButton(onClick = { showArchiveConfirm = false }) { Text(stringResource(R.string.cancel)) }
                }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FieldItemCard(
    field: AccountField,
    selectionMode: Boolean,
    isSelected: Boolean,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onToggleSelect: () -> Unit,
    onMoveUp: () -> Unit,
    onEnterSelection: () -> Unit,
    onMoveDown: () -> Unit
) {
    val context = LocalContext.current
    var isPasswordVisible by remember { mutableStateOf(false) }
    val isPassword = field.label.contains("Şifre", ignoreCase = true) || field.label.contains("Password", ignoreCase = true)

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
            .combinedClickable(
                onClick = { if (selectionMode) onToggleSelect() },
                onLongClick = { if (!selectionMode) { onEnterSelection(); onToggleSelect() } }
            ),
        colors = if (isSelected) CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer) else CardDefaults.cardColors(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = fieldIconFor(field.label),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = field.label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(4.dp))
                if (isPassword && !isPasswordVisible) {
                    Text(text = "••••••••", style = MaterialTheme.typography.bodyLarge)
                } else {
                    Text(text = field.value, style = MaterialTheme.typography.bodyLarge)
                }
            }
            if (selectionMode) {
                IconButton(onClick = onMoveUp, enabled = canMoveUp) {
                    Icon(Icons.Default.KeyboardArrowUp, contentDescription = stringResource(R.string.cd_move_up))
                }
                IconButton(onClick = onMoveDown, enabled = canMoveDown) {
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = stringResource(R.string.cd_move_down))
                }
                Checkbox(checked = isSelected, onCheckedChange = { onToggleSelect() })
            } else {
                if (isPassword) {
                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                        Icon(
                            imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = stringResource(R.string.cd_visibility)
                        )
                    }
                }
                IconButton(onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText(field.label, field.value)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(context, context.getString(R.string.copied_toast), Toast.LENGTH_SHORT).show()
                }) {
                    Icon(Icons.Default.ContentCopy, contentDescription = stringResource(R.string.cd_copy))
                }
            }
        }
    }
}
