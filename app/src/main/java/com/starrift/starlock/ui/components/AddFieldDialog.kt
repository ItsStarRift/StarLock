package com.starrift.starlock.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.starrift.starlock.R
import com.starrift.starlock.data.AccountField

private data class PresetOption(val label: String, val isCustom: Boolean = false)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AddFieldDialog(
    onDismiss: () -> Unit,
    onConfirm: (label: String, value: String, isCustom: Boolean, isCensored: Boolean) -> Unit,
    existingField: AccountField? = null,
) {
    val isEditMode = existingField != null

    val presetLabels = listOf(
        stringResource(R.string.preset_phone),
        stringResource(R.string.preset_email),
        stringResource(R.string.preset_password),
        stringResource(R.string.preset_recovery_phone),
        stringResource(R.string.preset_recovery_email),
        stringResource(R.string.preset_2fa_secret),
        stringResource(R.string.preset_username),
        stringResource(R.string.preset_security_question),
    )
    val customOptionLabel = stringResource(R.string.custom_field_option)
    val presetOptions = remember(presetLabels, customOptionLabel) {
        presetLabels.map { PresetOption(it) } + PresetOption(customOptionLabel, isCustom = true)
    }

    val initialSelection = remember(existingField) {
        if (existingField != null && !existingField.isCustomLabel) {
            presetOptions.find { it.label == existingField.label } ?: presetOptions.last()
        } else {
            presetOptions.first()
        }
    }

    var selectedOption by remember { mutableStateOf(initialSelection) }
    var customLabel by remember { mutableStateOf(if (existingField?.isCustomLabel == true) existingField.label else "") }
    var value by remember { mutableStateOf(existingField?.value ?: "") }
    var isCensored by remember { mutableStateOf(existingField?.isCensored ?: false) }
    var censorManuallySet by remember { mutableStateOf(isEditMode) }

    val passwordLabel = stringResource(R.string.preset_password)
    val twoFaLabel = stringResource(R.string.preset_2fa_secret)

    LaunchedEffect(selectedOption) {
        if (!censorManuallySet) {
            val label = selectedOption.label
            isCensored = label == passwordLabel || label == twoFaLabel
        }
    }

    val finalLabel = if (selectedOption.isCustom) customLabel else selectedOption.label
    val isValid = finalLabel.isNotBlank() && value.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (isEditMode) stringResource(R.string.edit_field_title)
                else stringResource(R.string.cd_add_field)
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                ) {
                    presetOptions.chunked(3).forEachIndexed { rowIndex, rowOptions ->
                        if (rowIndex > 0) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                        }
                        Row(modifier = Modifier.fillMaxWidth()) {
                            rowOptions.forEachIndexed { colIndex, option ->
                                if (colIndex > 0) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .width(1.dp)
                                            .background(MaterialTheme.colorScheme.outline)
                                    )
                                }
                                val isSelected = option == selectedOption
                                Row(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                                            else Color.Transparent
                                        )
                                        .clickable { selectedOption = option }
                                        .padding(horizontal = 8.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Text(
                                        text = option.label,
                                        maxLines = 1,
                                        overflow = TextOverflow.Clip,
                                        modifier = Modifier.basicMarquee()
                                    )
                                }
                            }
                            if (rowOptions.size < 3) {
                                repeat(3 - rowOptions.size) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }

                if (selectedOption.isCustom) {
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
                    label = {
                        Text(
                            if (selectedOption.isCustom) stringResource(R.string.value_label)
                            else stringResource(R.string.field_value_label, selectedOption.label)
                        )
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = isCensored,
                        onCheckedChange = {
                            isCensored = it
                            censorManuallySet = true
                        }
                    )
                    Text(stringResource(R.string.censor_field_value))
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (isValid) {
                        onConfirm(finalLabel, value, selectedOption.isCustom, isCensored)
                    }
                },
                enabled = isValid
            ) {
                Text(stringResource(R.string.add_btn))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
