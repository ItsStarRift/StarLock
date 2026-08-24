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
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.draw.graphicsLayer
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
                        IconButton(onClick = { viewModel.archiveSelectedFields() }) {
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
            var draggedFields by remember(fields) { mutableStateOf(fields) }
            var draggingIndex by remember { mutableStateOf<Int?>(null) }

            LazyColumn(
                contentPadding = padding,
                modifier = Modifier.fillMaxSize()
            ) {
                itemsIndexed(draggedFields, key = { _, field -> field.id }) { index, field ->
                    FieldItemCard(
                        field = field,
                        selectionMode = selectionMode,
                        isSelected = field.id in selectedIds,
                        isDragging = draggingIndex == index,
                        onToggleSelect = { viewModel.toggleSelect(field.id) },
                        onDragStart = {
                            if (!selectionMode) {
                                viewModel.enterSelectionMode()
                                viewModel.toggleSelect(field.id)
                            }
                            draggingIndex = index
                        },
                        onDragMove = { deltaY ->
                            val currentIndex = draggingIndex ?: return@FieldItemCard
                            val itemHeightPx = 88f
                            val targetIndex = (currentIndex + (deltaY / itemHeightPx).toInt())
                                .coerceIn(0, draggedFields.size - 1)
                            if (targetIndex != currentIndex) {
                                val mutable = draggedFields.toMutableList()
                                val moved = mutable.removeAt(currentIndex)
                                mutable.add(targetIndex, moved)
                                draggedFields = mutable
                                draggingIndex = targetIndex
                            }
                        },
                        onDragEnd = {
                            draggingIndex = null
                            viewModel.commitFieldOrder(draggedFields)
                        }
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
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FieldItemCard(
    field: AccountField,
    selectionMode: Boolean,
    isSelected: Boolean,
    isDragging: Boolean,
    onToggleSelect: () -> Unit,
    onDragStart: () -> Unit,
    onDragMove: (Float) -> Unit,
    onDragEnd: () -> Unit
) {
    val context = LocalContext.current
    var isPasswordVisible by remember { mutableStateOf(false) }
    val isPassword = field.label.contains("Şifre", ignoreCase = true) || field.label.contains("Password", ignoreCase = true)

    val scale by animateFloatAsState(
        targetValue = if (isDragging) 1.04f else 1f,
        animationSpec = tween(150),
        label = "fieldDragScale"
    )

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .combinedClickable(
                onClick = { if (selectionMode) onToggleSelect() },
                onLongClick = {}
            )
            .pointerInput(selectionMode) {
                detectDragGesturesAfterLongPress(
                    onDragStart = { onDragStart() },
                    onDragEnd = { onDragEnd() },
                    onDragCancel = { onDragEnd() },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        onDragMove(dragAmount.y)
                    }
                )
            },
        colors = if (isSelected) CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer) else CardDefaults.cardColors(),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDragging) 8.dp else 2.dp)
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
