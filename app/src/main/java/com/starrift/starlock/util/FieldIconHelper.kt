package com.starrift.starlock.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.Quiz
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
        FieldPreset.SECURITY_QUESTION -> Icons.Default.Quiz
        FieldPreset.CUSTOM -> Icons.Default.Label
    }
}

/** Geriye dönük uyumluluk: DB'de zaten kayıtlı eski field'lar ve custom field'lar için label metnine göre ikon bulur. */
fun fieldIconFor(label: String): ImageVector {
    val l = label.trim().lowercase()
    return when {
        l.contains("kurtarma telefon") || l.contains("recovery phone") -> Icons.Default.PhoneInTalk
        l.contains("kurtarma e-mail") || l.contains("kurtarma e-posta") || l.contains("recovery e-mail") || l.contains("recovery email") -> Icons.Default.Mail
        l.contains("telefon") || l.contains("phone") -> Icons.Default.PhoneInTalk
        l.contains("e-posta") || l.contains("email") || l.contains("e-mail") -> Icons.Default.Mail
        l.contains("kullanıcı adı") || l.contains("username") -> Icons.Default.Person
        l.contains("güvenlik sorusu") || l.contains("security question") -> Icons.Default.Quiz
        l.contains("2fa") || l.contains("gizli anahtar") || l.contains("secret key") -> Icons.Default.Key
        l.contains("şifre") || l.contains("password") -> Icons.Default.Password
        else -> Icons.Default.Label
    }
}
