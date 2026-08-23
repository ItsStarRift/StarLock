package com.starrift.starlock.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.starrift.starlock.R
import com.starrift.starlock.data.AccountField

@Composable
fun AddFieldDialog(
    onDismiss: () -> Unit,
    onConfirm: (label: String, value: String, isCustom: Boolean) -> Unit,
    existingField: AccountField? = null
) {
    val customOptionLabel = stringResource(R.string.custom_field_option)
    val predefinedLabels = listOf(
        stringResource(R.string.preset_phone),
        stringResource(R.string.preset_email),
        stringResource(R.string.preset_username),
        stringResource(R.string.preset_password),
        customOptionLabel
    )
    var selectedLabel by remember { mutableStateOf(predefinedLabels.first()) }
    var customLabel by remember { mutableStateOf(existingField?.label ?: "") }
    var value by remember { mutableStateOf(existingField?.value ?: "") }
    var isCustomStep by remember { mutableStateOf(existingField != null) }
    val isEditMode = existingField != null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEditMode) stringResource(R.string.edit_field_title) else if (isCustomStep) stringResource(R.string.custom_field_title) else stringResource(R.string.cd_add_field)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (!isCustomStep) {
                    predefinedLabels.forEach { label ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (label == selectedLabel),
                                onClick = { selectedLabel = label }
                            )
                            Text(text = label)
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = customLabel,
                        onValueChange = { customLabel = it },
                        label = { Text(stringResource(R.string.custom_field_name_hint)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                OutlinedTextField(
                    value = value,
                    onValueChange = { value = it },
                    label = { Text(if (isCustomStep) stringResource(R.string.value_label) else stringResource(R.string.field_value_label, selectedLabel)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (!isCustomStep && selectedLabel == customOptionLabel) {
                        isCustomStep = true
                    } else {
                        val finalLabel = if (isCustomStep) customLabel else selectedLabel
                        if (finalLabel.isNotBlank() && value.isNotBlank()) {
                            onConfirm(finalLabel, value, isCustomStep)
                        }
                    }
                }
            ) {
                Text(if (!isCustomStep && selectedLabel == customOptionLabel) stringResource(R.string.next_btn) else stringResource(R.string.add_btn))
            }
        },
        dismissButton = {
            TextButton(onClick = {
                if (isCustomStep) isCustomStep = false else onDismiss()
            }) {
                Text(if (isCustomStep) stringResource(R.string.back_btn) else stringResource(R.string.cancel))
            }
        }
    )
}
