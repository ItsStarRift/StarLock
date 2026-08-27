package com.starrift.starlock.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.ui.graphics.vector.ImageVector

/** Sabit preset kimlikleri. AddFieldDialog'daki seçim buradan gelir, label metnine bağımlı değildir. */
enum class FieldPreset {
    PHONE, EMAIL, PASSWORD, RECOVERY_PHONE, RECOVERY_EMAIL, TWO_FA_SECRET,
    USERNAME, SECURITY_QUESTION, CUSTOM
}

fun fieldIconForPreset(preset: FieldPreset): ImageVector {
    return when (preset) {
        FieldPreset.PHONE, FieldPreset.RECOVERY_PHONE -> Icons.Default.PhoneInTalk
        FieldPreset.EMAIL, FieldPreset.RECOVERY_EMAIL -> Icons.Default.Mail
        FieldPreset.PASSWORD -> Icons.Default.Password
        FieldPreset.TWO_FA_SECRET -> Icons.Default.Key
        FieldPreset.USERNAME -> Icons.Default.Person
        FieldPreset.SECURITY_QUESTION, FieldPreset.CUSTOM -> Icons.Default.Label
    }
}

/** Geriye dönük uyumluluk: DB'de zaten kayıtlı eski field'lar için label metnine göre ikon bulur. */
fun fieldIconFor(label: String): ImageVector {
    return when (label.trim().lowercase()) {
        "telefon numarası", "phone number" -> Icons.Default.PhoneInTalk
        "e-posta", "email", "e-mail" -> Icons.Default.Mail
        "kullanıcı adı", "username" -> Icons.Default.Person
        "şifre", "password" -> Icons.Default.Password
        "kurtarma telefon numarası", "recovery phone number" -> Icons.Default.PhoneInTalk
        "kurtarma e-mail", "kurtarma e-posta", "recovery e-mail", "recovery email" -> Icons.Default.Mail
        "2fa gizli anahtarı", "2fa secret key" -> Icons.Default.Key
        else -> Icons.Default.Label
    }
}
