package com.starrift.starlock.ui.screens

import com.starrift.starlock.BuildConfig

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import com.starrift.starlock.R
import com.starrift.starlock.util.PinManager

private const val GITHUB_URL = "https://github.com/ItsStarRift/StarLock"
private const val FEEDBACK_EMAIL = "omerplt.dev@gmail.com"
private val APP_VERSION = BuildConfig.VERSION_NAME

private fun formatLastAction(context: android.content.Context, key: String): Pair<String, String>? {
    val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
    val millis = prefs.getLong(key, -1L)
    if (millis == -1L) return null
    val datePart = java.text.SimpleDateFormat("dd.MM.yyyy", java.util.Locale.getDefault())
    val timePart = java.text.SimpleDateFormat("HH:mm (z)", java.util.Locale.getDefault())
    datePart.timeZone = java.util.TimeZone.getDefault()
    timePart.timeZone = java.util.TimeZone.getDefault()
    val date = java.util.Date(millis)
    return Pair(datePart.format(date), timePart.format(date))
}

@Composable
fun SettingsScreen(viewModel: SettingsViewModel, onTrashClick: () -> Unit, onArchivedClick: () -> Unit, themeMode: String, onThemeChange: (String) -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showAboutDialog by remember { mutableStateOf(false) }
    var showImportConfirmDialog by remember { mutableStateOf(false) }
    var pendingImportUri by remember { mutableStateOf<Uri?>(null) }
    
    // Dil Seçimi Durumu
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
    val currentLangCode = prefs.getString("app_lang", "system") ?: "system"
    val currentLangText = when(currentLangCode) {
        "en" -> stringResource(R.string.lang_en)
        "tr" -> stringResource(R.string.lang_tr)
        else -> stringResource(R.string.lang_system)
    }

    val pinManager = remember { PinManager(context) }
    var isPinSet by remember { mutableStateOf(pinManager.isPinSet()) }
    var showPinWarningDialog by remember { mutableStateOf(false) }
    var showPinCreateDialog by remember { mutableStateOf(false) }
    var showPinRemoveDialog by remember { mutableStateOf(false) }

    val exportSuccessMsg = stringResource(R.string.export_success)
    val exportFailMsg = stringResource(R.string.export_fail)
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                val success = viewModel.exportTo(context, uri)
                    if (success) prefs.edit().putLong("last_export_at", System.currentTimeMillis()).apply()
                Toast.makeText(context, if (success) exportSuccessMsg else exportFailMsg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
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
        Text(
            stringResource(R.string.settings_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(20.dp))

        SettingsGroup {
            SettingsRow(
                icon = Icons.Default.Lock,
                title = stringResource(R.string.app_lock),
                subtitle = if (isPinSet) stringResource(R.string.on) else stringResource(R.string.off),
                onClick = { if (isPinSet) showPinRemoveDialog = true else showPinWarningDialog = true }
            )
            SettingsDivider()
            SettingsRow(
                icon = Icons.Default.Language,
                title = stringResource(R.string.language),
                subtitle = currentLangText,
                onClick = { showLanguageDialog = true }
            )
                SettingsDivider()
                SettingsRow(
                    icon = Icons.Default.Palette,
                    title = stringResource(R.string.theme),
                    subtitle = when (themeMode) {
                        "dark" -> stringResource(R.string.theme_dark)
                        "light" -> stringResource(R.string.theme_light)
                        else -> stringResource(R.string.theme_system)
                    },
                    onClick = { showThemeDialog = true }
                )
        }

        Spacer(Modifier.height(20.dp))

        SettingsGroup {
            SettingsRow(
                icon = Icons.Default.Upload,
                title = stringResource(R.string.export_data),
                subtitle = stringResource(R.string.export_data_sub),
                onClick = { exportLauncher.launch("StarLock-Backup.json") },
                trailingText = formatLastAction(context, "last_export_at")
            )
            SettingsDivider()
            SettingsRow(
                icon = Icons.Default.Download,
                title = stringResource(R.string.import_data),
                subtitle = stringResource(R.string.import_data_sub),
                onClick = { importLauncher.launch(arrayOf("application/json")) },
                trailingText = formatLastAction(context, "last_import_at")
            )
            SettingsDivider()
            SettingsRow(
                icon = Icons.Default.Delete,
                title = stringResource(R.string.trash),
                subtitle = stringResource(R.string.trash_sub),
                onClick = onTrashClick
            )
        SettingsDivider()

            SettingsRow(
                icon = Icons.Default.Archive,
                title = stringResource(R.string.archived),
                subtitle = stringResource(R.string.archived_sub),
                onClick = onArchivedClick
            )

        }
        Spacer(Modifier.height(20.dp))
        SettingsGroup {
            SettingsRow(
                icon = Icons.Default.Email,
                title = stringResource(R.string.feedback),
                subtitle = FEEDBACK_EMAIL,
                onClick = {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:$FEEDBACK_EMAIL")
                        putExtra(Intent.EXTRA_SUBJECT, "StarLock Feedback")
                    }
                    context.startActivity(Intent.createChooser(intent, "Send Feedback"))
                }
            )
            SettingsDivider()
            SettingsRow(
                icon = Icons.Default.Code,
                title = stringResource(R.string.github_page),
                subtitle = stringResource(R.string.github_sub),
                onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(GITHUB_URL))) }
            )
            SettingsDivider()
            SettingsRow(
                icon = Icons.Default.Info,
                title = stringResource(R.string.about_app),
                subtitle = stringResource(R.string.version, APP_VERSION),
                onClick = { showAboutDialog = true }
            )
        }
        Spacer(Modifier.height(96.dp))
    }

    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text(stringResource(R.string.language)) },
            text = {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                prefs.edit().putString("app_lang", "system").apply()
                                showLanguageDialog = false
                                (context as? Activity)?.recreate()
                            }
                            .padding(16.dp)
                    ) { Text(stringResource(R.string.lang_system), style = MaterialTheme.typography.bodyLarge) }
                    Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                prefs.edit().putString("app_lang", "tr").apply()
                                showLanguageDialog = false
                                (context as? Activity)?.recreate()
                            }
                            .padding(16.dp)
                    ) { Text(stringResource(R.string.lang_tr), style = MaterialTheme.typography.bodyLarge) }
                    Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                prefs.edit().putString("app_lang", "en").apply()
                                showLanguageDialog = false
                                (context as? Activity)?.recreate()
                            }
                            .padding(16.dp)
                    ) { Text(stringResource(R.string.lang_en), style = MaterialTheme.typography.bodyLarge) }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }
    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text(stringResource(R.string.theme)) },
            text = {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onThemeChange("system")
                                showThemeDialog = false
                            }
                            .padding(16.dp)
                    ) { Text(stringResource(R.string.theme_system), style = MaterialTheme.typography.bodyLarge) }
                    Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onThemeChange("dark")
                                showThemeDialog = false
                            }
                            .padding(16.dp)
                    ) { Text(stringResource(R.string.theme_dark), style = MaterialTheme.typography.bodyLarge) }
                    Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onThemeChange("light")
                                showThemeDialog = false
                            }
                            .padding(16.dp)
                    ) { Text(stringResource(R.string.theme_light), style = MaterialTheme.typography.bodyLarge) }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text(stringResource(R.string.app_name)) },
            text = { Text(stringResource(R.string.version, APP_VERSION) + "\n\n" + stringResource(R.string.about_text)) },
            confirmButton = { TextButton(onClick = { showAboutDialog = false }) { Text(stringResource(R.string.close)) } }
        )
    }

    val importSuccessMsg = stringResource(R.string.import_success)
    val importFailMsg = stringResource(R.string.import_fail)
    if (showImportConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showImportConfirmDialog = false },
            title = { Text(stringResource(R.string.import_title)) },
            text = { Text(stringResource(R.string.import_warning)) },
            confirmButton = {
                TextButton(onClick = {
                    val uri = pendingImportUri
                    showImportConfirmDialog = false
                    if (uri != null) {
                        scope.launch {
                            val success = viewModel.importFrom(context, uri)
                                if (success) prefs.edit().putLong("last_import_at", System.currentTimeMillis()).apply()
                            Toast.makeText(context, if (success) importSuccessMsg else importFailMsg, Toast.LENGTH_SHORT).show()
                        }
                    }
                }) { Text(stringResource(R.string.yes_replace)) }
            },
            dismissButton = { TextButton(onClick = { showImportConfirmDialog = false }) { Text(stringResource(R.string.cancel)) } }
        )
    }

    if (showPinWarningDialog) {
        var riskAccepted by remember { mutableStateOf(false) }
        AlertDialog(
            onDismissRequest = { showPinWarningDialog = false },
            title = { Text(stringResource(R.string.pin_warning_title), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error) },
            text = {
                Column {
                    Text(stringResource(R.string.pin_warning_text))
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().clickable { riskAccepted = !riskAccepted }.padding(vertical = 4.dp)
                    ) {
                        Checkbox(checked = riskAccepted, onCheckedChange = { riskAccepted = it })
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.pin_risk_accept), style = MaterialTheme.typography.bodyMedium)
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showPinWarningDialog = false; showPinCreateDialog = true },
                    enabled = riskAccepted
                ) { Text(stringResource(R.string.continue_btn)) }
            },
            dismissButton = { TextButton(onClick = { showPinWarningDialog = false }) { Text(stringResource(R.string.cancel)) } }
        )
    }

    if (showPinCreateDialog) {
        var pinInput by remember { mutableStateOf("") }
        var pinConfirm by remember { mutableStateOf("") }
        var isError by remember { mutableStateOf(false) }
        val successMsg = stringResource(R.string.pin_success)

        AlertDialog(
            onDismissRequest = { showPinCreateDialog = false },
            title = { Text(stringResource(R.string.pin_create_title)) },
            text = {
                Column {
                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = { if (it.length <= 4) { pinInput = it; isError = false } },
                        label = { Text(stringResource(R.string.pin_4_digits)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = pinConfirm,
                        onValueChange = { if (it.length <= 4) { pinConfirm = it; isError = false } },
                        label = { Text(stringResource(R.string.pin_repeat)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        visualTransformation = PasswordVisualTransformation(),
                        isError = isError,
                        singleLine = true
                    )
                    if (isError) {
                        Text(
                            stringResource(R.string.pin_mismatch),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (pinInput.length == 4 && pinInput == pinConfirm) {
                            pinManager.setPin(pinInput)
                            isPinSet = true
                            showPinCreateDialog = false
                            Toast.makeText(context, successMsg, Toast.LENGTH_SHORT).show()
                        } else {
                            isError = true
                        }
                    }
                ) { Text(stringResource(R.string.create)) }
            },
            dismissButton = { TextButton(onClick = { showPinCreateDialog = false }) { Text(stringResource(R.string.cancel)) } }
        )
    }

    if (showPinRemoveDialog) {
        var currentPin by remember { mutableStateOf("") }
        var isError by remember { mutableStateOf(false) }
        var errorMsg by remember { mutableStateOf("") }
        
        val removeSuccessMsg = stringResource(R.string.pin_remove_success)
        val incorrectPinMsg = stringResource(R.string.pin_incorrect)
        val waitMsgTemplate = stringResource(R.string.pin_too_many_attempts)

        AlertDialog(
            onDismissRequest = { showPinRemoveDialog = false },
            title = { Text(stringResource(R.string.pin_remove_title)) },
            text = {
                Column {
                    Text(stringResource(R.string.pin_remove_text))
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = currentPin,
                        onValueChange = { if (it.length <= 4) { currentPin = it; isError = false } },
                        label = { Text(stringResource(R.string.current_pin)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        visualTransformation = PasswordVisualTransformation(),
                        isError = isError,
                        singleLine = true
                    )
                    if (isError) {
                        Text(
                            errorMsg,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (pinManager.verifyPin(currentPin)) {
                            pinManager.removePin()
                            isPinSet = false
                            showPinRemoveDialog = false
                            Toast.makeText(context, removeSuccessMsg, Toast.LENGTH_SHORT).show()
                        } else {
                            isError = true
                            val lockout = pinManager.getRemainingLockoutSeconds()
                            if (lockout > 0) {
                                errorMsg = waitMsgTemplate.format(lockout)
                            } else {
                                errorMsg = incorrectPinMsg
                            }
                        }
                    }
                ) { Text(stringResource(R.string.remove)) }
            },
            dismissButton = { TextButton(onClick = { showPinRemoveDialog = false }) { Text(stringResource(R.string.cancel)) } }
        )
    }
}

@Composable
private fun SettingsGroup(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) { content() }
}

@Composable
private fun SettingsDivider() {
    Divider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
}

@Composable
private fun SettingsRow(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit, trailingText: Pair<String, String>? = null) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(36.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) { Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp)) }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
        }
        if (trailingText != null) {
            Column(horizontalAlignment = Alignment.End) {
                Text(trailingText.first, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), textAlign = TextAlign.End)
                Text(trailingText.second, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), textAlign = TextAlign.End)
            }
            Spacer(Modifier.width(8.dp))
        }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
}
}
