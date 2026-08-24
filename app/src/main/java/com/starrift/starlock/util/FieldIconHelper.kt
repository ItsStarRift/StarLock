package com.starrift.starlock.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.ui.graphics.vector.ImageVector

fun fieldIconFor(label: String): ImageVector {
    return when (label.trim().lowercase()) {
        "telefon numarası", "phone number" -> Icons.Default.PhoneInTalk
        "e-posta", "email" -> Icons.Default.Mail
        "kullanıcı adı", "username" -> Icons.Default.Person
        "şifre", "password" -> Icons.Default.Password
        else -> Icons.Default.Label
    }
}
