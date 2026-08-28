package com.starrift.starlock.util

import android.content.Context
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.SecureRandom
import java.util.concurrent.TimeUnit
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

class StarLockPasswordManager(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        "app_security_encrypted",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    companion object {
        private const val ITERATIONS = 120_000
        private const val KEY_LENGTH_BITS = 256
        private const val LOCKOUT_DURATION_MS = 30_000L
        private const val MAX_ATTEMPTS = 5
        const val MIN_PASSWORD_LENGTH = 6
        const val MAX_PASSWORD_LENGTH = 16
    }

    fun isPasswordSet(): Boolean = prefs.contains("password_hash")

    fun isFingerprintEnabled(): Boolean = prefs.getBoolean("fingerprint_enabled", false)

    fun setFingerprintEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("fingerprint_enabled", enabled).apply()
    }

    fun setPassword(password: String): Boolean {
        if (password.length !in MIN_PASSWORD_LENGTH..MAX_PASSWORD_LENGTH) return false
        val salt = generateSalt()
        val hash = deriveHash(password, salt)
        prefs.edit()
            .putString("password_hash", hash)
            .putString("password_salt", Base64.encodeToString(salt, Base64.NO_WRAP))
            .putInt("failed_attempts", 0)
            .putLong("lockout_time", 0)
            .apply()
        return true
    }

    fun removePassword() {
        prefs.edit()
            .remove("password_hash")
            .remove("password_salt")
            .remove("failed_attempts")
            .remove("lockout_time")
            .remove("fingerprint_enabled")
            .apply()
    }

    fun verifyPassword(password: String): Boolean {
        if (isLockoutActive()) return false

        val storedHash = prefs.getString("password_hash", null) ?: return false
        val saltString = prefs.getString("password_salt", null) ?: return false
        val salt = Base64.decode(saltString, Base64.NO_WRAP)

        val isCorrect = deriveHash(password, salt) == storedHash

        if (isCorrect) {
            prefs.edit().putInt("failed_attempts", 0).putLong("lockout_time", 0).apply()
        } else {
            val attempts = prefs.getInt("failed_attempts", 0) + 1
            prefs.edit().putInt("failed_attempts", attempts).apply()
            if (attempts >= MAX_ATTEMPTS) {
                prefs.edit().putLong("lockout_time", System.currentTimeMillis()).apply()
            }
        }
        return isCorrect
    }

    fun getRemainingLockoutSeconds(): Long {
        if (!isLockoutActive()) return 0
        val lockoutTime = prefs.getLong("lockout_time", 0)
        val elapsed = System.currentTimeMillis() - lockoutTime
        val remainingMillis = LOCKOUT_DURATION_MS - elapsed
        return if (remainingMillis > 0) TimeUnit.MILLISECONDS.toSeconds(remainingMillis) + 1 else 0
    }

    private fun isLockoutActive(): Boolean {
        val lockoutTime = prefs.getLong("lockout_time", 0)
        if (lockoutTime == 0L) return false
        val elapsed = System.currentTimeMillis() - lockoutTime
        if (elapsed > LOCKOUT_DURATION_MS) {
            prefs.edit().putInt("failed_attempts", 0).putLong("lockout_time", 0).apply()
            return false
        }
        return true
    }

    private fun generateSalt(): ByteArray {
        val salt = ByteArray(16)
        SecureRandom().nextBytes(salt)
        return salt
    }

    private fun deriveHash(password: String, salt: ByteArray): String {
        val spec = PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH_BITS)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val hashBytes = factory.generateSecret(spec).encoded
        return Base64.encodeToString(hashBytes, Base64.NO_WRAP)
    }
}
