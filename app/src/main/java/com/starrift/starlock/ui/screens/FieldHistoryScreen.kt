package com.starrift.starlock.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.starrift.starlock.R
import com.starrift.starlock.data.FieldChangeType
import com.starrift.starlock.data.FieldHistoryEntry
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FieldHistoryScreen(
    viewModel: FieldHistoryViewModel,
    onBackClick: () -> Unit
) {
    val entries by viewModel.historyEntries.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()
    var showClearConfirm by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.field_history_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.cd_back))
                    }
                },
                actions = {
                    IconButton(onClick = { showClearConfirm = true }) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = stringResource(R.string.cd_clear_history))
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                SegmentedButton(
                    selected = selectedTab == HistoryTab.CURRENT,
                    onClick = { viewModel.selectTab(HistoryTab.CURRENT) },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                ) {
                    Text(stringResource(R.string.history_current_fields))
                }
                SegmentedButton(
                    selected = selectedTab == HistoryTab.DELETED,
                    onClick = { viewModel.selectTab(HistoryTab.DELETED) },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                ) {
                    Text(stringResource(R.string.history_deleted_fields))
                }
            }

            if (entries.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.history_empty), style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(entries, key = { it.id }) { entry ->
                        HistoryEntryCard(entry = entry, isDeletedTab = selectedTab == HistoryTab.DELETED)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text(stringResource(R.string.clear_history_confirm_title)) },
            text = { Text(stringResource(R.string.clear_history_confirm_text)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearHistory()
                    showClearConfirm = false
                }) { Text(stringResource(R.string.clear_history_confirm_action), color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }
}

@Composable
private fun HistoryEntryCard(entry: FieldHistoryEntry, isDeletedTab: Boolean) {
    val dateFormat = remember { SimpleDateFormat("dd.MM.yyyy HH:mm ( 'GMT'XXX)", Locale.getDefault()) }
    val formattedDate = remember(entry.timestamp) { dateFormat.format(Date(entry.timestamp)) }

    val titleRes = when (entry.changeType) {
        FieldChangeType.CREATED -> R.string.history_created
        FieldChangeType.FIELD_ONLY -> R.string.history_field_change
        FieldChangeType.VALUE_ONLY -> R.string.history_value_change
        FieldChangeType.BOTH -> R.string.history_field_value_change
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(titleRes),
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))

                FieldOrValueRow(
                    label = stringResource(R.string.history_field_label),
                    oldText = entry.oldLabel,
                    newText = entry.newLabel
                )
                FieldOrValueRow(
                    label = stringResource(R.string.history_value_label),
                    oldText = entry.oldValue,
                    newText = entry.newValue
                )

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (isDeletedTab) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = stringResource(R.string.cd_field_deleted_warning),
                    tint = Color.Red,
                    modifier = Modifier.align(Alignment.Top)
                )
            }
        }
    }
}

@Composable
private fun FieldOrValueRow(label: String, oldText: String?, newText: String?) {
    if (oldText == null && newText == null) return

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.width(4.dp))
        if (oldText != null && newText != null) {
            Text(text = oldText, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = "→", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = newText, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
        } else {
            Text(text = newText ?: oldText ?: "", style = MaterialTheme.typography.bodyMedium)
        }
    }
}
