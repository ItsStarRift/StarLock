package com.starrift.starlock.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.ui.graphics.vector.ImageVector

fun fieldIconFor(label: String): ImageVector {
    return when (label) {
        "Telefon Numarası", "Phone Number" -> Icons.Default.PhoneInTalk
        "E-posta", "Email" -> Icons.Default.Mail
        "Kullanıcı Adı", "Username" -> Icons.Default.Person
        "Şifre", "Password" -> Icons.Default.Password
        else -> Icons.Default.Label
    }
}
