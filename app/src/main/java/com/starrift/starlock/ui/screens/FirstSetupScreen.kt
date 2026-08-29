package com.starrift.starlock.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.starrift.starlock.util.BiometricHelper
import com.starrift.starlock.util.StarLockPasswordManager

@Composable
fun FirstSetupScreen(
    passwordManager: StarLockPasswordManager,
    onSetupComplete: () -> Unit
) {
    val context = LocalContext.current
    val biometricHardwareAvailable = remember { BiometricHelper.isBiometricAvailable(context) }

    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var fingerprintChecked by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var showNoBiometricMessage by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "StarLock'a Hoş Geldiniz",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Devam etmek için bir şifre belirleyin",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = password,
            onValueChange = {
                if (it.length <= StarLockPasswordManager.MAX_PASSWORD_LENGTH) {
                    password = it; errorMessage = ""
                }
            },
            label = { Text("Şifre (6-16 karakter)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = null
                    )
                }
            },
            isError = errorMessage.isNotEmpty(),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(0.9f)
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = {
                if (it.length <= StarLockPasswordManager.MAX_PASSWORD_LENGTH) {
                    confirmPassword = it; errorMessage = ""
                }
            },
            label = { Text("Şifre (tekrar)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                    Icon(
                        if (confirmPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = null
                    )
                }
            },
            isError = errorMessage.isNotEmpty(),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(0.9f)
        )

        if (errorMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(errorMessage, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .clickable {
                    if (!biometricHardwareAvailable) {
                        showNoBiometricMessage = true
                    } else {
                        fingerprintChecked = !fingerprintChecked
                        showNoBiometricMessage = false
                    }
                }
        ) {
            Checkbox(
                checked = fingerprintChecked,
                onCheckedChange = {
                    if (!biometricHardwareAvailable) {
                        showNoBiometricMessage = true
                    } else {
                        fingerprintChecked = it
                        showNoBiometricMessage = false
                    }
                }
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("Parmak izi ile açmayı etkinleştir", style = MaterialTheme.typography.bodyMedium)
        }

        if (showNoBiometricMessage) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Cihazınızda parmak izi kayıtlı değil, sistem ayarlarından parmak izi ekleyiniz",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        Button(
            onClick = {
                if (password.length !in StarLockPasswordManager.MIN_PASSWORD_LENGTH..StarLockPasswordManager.MAX_PASSWORD_LENGTH) {
                    errorMessage = "Şifre 6-16 karakter olmalı"
                } else if (password != confirmPassword) {
                    errorMessage = "Şifreler eşleşmiyor"
                } else {
                    val success = passwordManager.setPassword(password)
                    if (success) {
                        if (fingerprintChecked && biometricHardwareAvailable) {
                            passwordManager.setFingerprintEnabled(true)
                        }
                        onSetupComplete()
                    } else {
                        errorMessage = "Şifre 6-16 karakter olmalı"
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            Text("Kur")
        }
    }
}
