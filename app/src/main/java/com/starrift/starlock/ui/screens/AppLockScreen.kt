@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.starrift.starlock.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.starrift.starlock.util.BiometricHelper
import com.starrift.starlock.util.StarLockPasswordManager

@Composable
fun AppLockScreen(
    passwordManager: StarLockPasswordManager,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current

    var isFingerprintEnabled by remember { mutableStateOf(passwordManager.isFingerprintEnabled()) }
    val biometricHardwareAvailable = remember { BiometricHelper.isBiometricAvailable(context) }

    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var showDisableFingerprintDialog by remember { mutableStateOf(false) }
    var showEnableFingerprintDialog by remember { mutableStateOf(false) }
    var showNoBiometricMessage by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("App Lock") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "App Lock",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Welcome to App Lock. You can change or remove your App Lock settings. Be careful when changing or removing things. Reminder that do not forget your password, if you forget your password, you can not access your data. If you have a opinion about this space or app, please share your opinion to me.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (showNoBiometricMessage) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Cihazınızda parmak izi kayıtlı değil, sistem ayarlarından parmak izi ekleyiniz",
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                AppLockOptionCard(
                    icon = Icons.Default.Password,
                    label = "Change\nPassword",
                    modifier = Modifier.weight(1f),
                    onClick = { showChangePasswordDialog = true }
                )

                AppLockOptionCard(
                    icon = Icons.Default.Fingerprint,
                    label = if (isFingerprintEnabled) "Disable\nFingerprint" else "Enable\nFingerprint",
                    modifier = Modifier.weight(1f),
                    onClick = {
                        if (!biometricHardwareAvailable) {
                            showNoBiometricMessage = true
                        } else if (isFingerprintEnabled) {
                            showDisableFingerprintDialog = true
                        } else {
                            showEnableFingerprintDialog = true
                            showNoBiometricMessage = false
                        }
                    }
                )
            }
        }
    }

    if (showChangePasswordDialog) {
        ChangePasswordDialog(
            passwordManager = passwordManager,
            onDismiss = { showChangePasswordDialog = false }
        )
    }

    if (showDisableFingerprintDialog) {
        DisableFingerprintDialog(
            passwordManager = passwordManager,
            onDismiss = { showDisableFingerprintDialog = false },
            onDisabled = {
                isFingerprintEnabled = false
                showDisableFingerprintDialog = false
            }
        )
    }

    if (showEnableFingerprintDialog) {
        EnableFingerprintDialog(
            passwordManager = passwordManager,
            onDismiss = { showEnableFingerprintDialog = false },
            onEnabled = {
                isFingerprintEnabled = true
                showEnableFingerprintDialog = false
            }
        )
    }
}

@Composable
private fun AppLockOptionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = modifier.clip(androidx.compose.foundation.shape.RoundedCornerShape(16.dp)),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = label,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
private fun ChangePasswordDialog(
    passwordManager: StarLockPasswordManager,
    onDismiss: () -> Unit
) {
    var step by remember { mutableIntStateOf(1) }
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var currentPasswordVisible by remember { mutableStateOf(false) }
    var newPasswordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (step == 1) "Mevcut Şifre" else "Yeni Şifre") },
        text = {
            Column {
                if (step == 1) {
                    OutlinedTextField(
                        value = currentPassword,
                        onValueChange = { currentPassword = it; errorMessage = "" },
                        label = { Text("Mevcut şifre") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        visualTransformation = if (currentPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { currentPasswordVisible = !currentPasswordVisible }) {
                                Icon(if (currentPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, contentDescription = null)
                            }
                        },
                        isError = errorMessage.isNotEmpty(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = {
                            if (it.length <= StarLockPasswordManager.MAX_PASSWORD_LENGTH) {
                                newPassword = it; errorMessage = ""
                            }
                        },
                        label = { Text("Yeni şifre (6-16 karakter)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        visualTransformation = if (newPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { newPasswordVisible = !newPasswordVisible }) {
                                Icon(if (newPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, contentDescription = null)
                            }
                        },
                        isError = errorMessage.isNotEmpty(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = {
                            if (it.length <= StarLockPasswordManager.MAX_PASSWORD_LENGTH) {
                                confirmPassword = it; errorMessage = ""
                            }
                        },
                        label = { Text("Yeni şifre (tekrar)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                Icon(if (confirmPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, contentDescription = null)
                            }
                        },
                        isError = errorMessage.isNotEmpty(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                if (errorMessage.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(errorMessage, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (step == 1) {
                    if (passwordManager.verifyPassword(currentPassword)) {
                        step = 2
                        errorMessage = ""
                    } else {
                        errorMessage = "Hatalı şifre"
                    }
                } else {
                    if (newPassword.length !in StarLockPasswordManager.MIN_PASSWORD_LENGTH..StarLockPasswordManager.MAX_PASSWORD_LENGTH) {
                        errorMessage = "Şifre 6-16 karakter olmalı"
                    } else if (newPassword != confirmPassword) {
                        errorMessage = "Şifreler eşleşmiyor"
                    } else {
                        passwordManager.setPassword(newPassword)
                        onDismiss()
                    }
                }
            }) {
                Text(if (step == 1) "Devam" else "Kaydet")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("İptal") }
        }
    )
}

@Composable
private fun DisableFingerprintDialog(
    passwordManager: StarLockPasswordManager,
    onDismiss: () -> Unit,
    onDisabled: () -> Unit
) {
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Parmak İzini Kapat") },
        text = {
            Column {
                Text("Devam etmek için şifrenizi girin")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it; errorMessage = "" },
                    label = { Text("Şifre") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, contentDescription = null)
                        }
                    },
                    isError = errorMessage.isNotEmpty(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (errorMessage.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(errorMessage, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (passwordManager.verifyPassword(password)) {
                    passwordManager.setFingerprintEnabled(false)
                    onDisabled()
                } else {
                    errorMessage = "Hatalı şifre"
                }
            }) {
                Text("Kapat")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("İptal") }
        }
    )
}

@Composable
private fun EnableFingerprintDialog(
    passwordManager: StarLockPasswordManager,
    onDismiss: () -> Unit,
    onEnabled: () -> Unit
) {
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Parmak İzini Aç") },
        text = {
            Column {
                Text("Devam etmek için şifrenizi girin")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it; errorMessage = "" },
                    label = { Text("Şifre") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, contentDescription = null)
                        }
                    },
                    isError = errorMessage.isNotEmpty(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (errorMessage.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(errorMessage, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (passwordManager.verifyPassword(password)) {
                    passwordManager.setFingerprintEnabled(true)
                    onEnabled()
                } else {
                    errorMessage = "Hatalı şifre"
                }
            }) {
                Text("Aç")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("İptal") }
        }
    )
}
