package com.starrift.starlock.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
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
import androidx.fragment.app.FragmentActivity
import com.starrift.starlock.util.BiometricHelper
import com.starrift.starlock.util.StarLockPasswordManager
import kotlinx.coroutines.delay

@Composable
fun LockScreen(
    passwordManager: StarLockPasswordManager,
    onUnlocked: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity

    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isError by remember { mutableStateOf(false) }
    var remainingLockout by remember { mutableStateOf(passwordManager.getRemainingLockoutSeconds()) }

    val fingerprintAvailable = remember {
        passwordManager.isFingerprintEnabled() && BiometricHelper.isBiometricAvailable(context)
    }

    LaunchedEffect(remainingLockout) {
        if (remainingLockout > 0) {
            delay(1000)
            remainingLockout = passwordManager.getRemainingLockoutSeconds()
        }
    }

    fun attemptUnlock() {
        if (remainingLockout > 0) return
        if (passwordManager.verifyPassword(password)) {
            onUnlocked()
        } else {
            isError = true
            password = ""
            remainingLockout = passwordManager.getRemainingLockoutSeconds()
        }
    }

    fun triggerBiometric() {
        if (activity == null) return
        BiometricHelper.showPrompt(
            activity = activity,
            title = "Parmak İzi ile Aç",
            subtitle = "Devam etmek için parmak izinizi doğrulayın",
            negativeButtonText = "İptal",
            onSuccess = { onUnlocked() },
            onError = { /* kullanıcı iptal etti veya hata oluştu, sessizce yoksay */ }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Hoş Geldiniz",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = if (remainingLockout > 0) "Çok fazla hatalı deneme. ${remainingLockout}s bekleyin."
                   else if (isError) "Hatalı şifre, tekrar deneyin."
                   else "Devam etmek için şifrenizi girin",
            color = if (isError || remainingLockout > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = password,
            onValueChange = {
                if (it.length <= StarLockPasswordManager.MAX_PASSWORD_LENGTH) {
                    password = it
                    isError = false
                }
            },
            label = { Text("Şifre") },
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
            isError = isError,
            singleLine = true,
            enabled = remainingLockout == 0L,
            modifier = Modifier.fillMaxWidth(0.85f)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = { attemptUnlock() },
            enabled = remainingLockout == 0L && password.isNotEmpty(),
            modifier = Modifier.fillMaxWidth(0.85f)
        ) {
            Text("Giriş Yap")
        }

        if (fingerprintAvailable) {
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(
                onClick = { triggerBiometric() },
                enabled = remainingLockout == 0L,
                modifier = Modifier.fillMaxWidth(0.85f)
            ) {
                Icon(Icons.Default.Fingerprint, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Parmak İzi ile Aç")
            }
        }
    }

    LaunchedEffect(fingerprintAvailable) {
        if (fingerprintAvailable && remainingLockout == 0L) {
            triggerBiometric()
        }
    }
}
