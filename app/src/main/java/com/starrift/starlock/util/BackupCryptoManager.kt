package com.starrift.starlock.util

import android.util.Base64
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * Handles encryption/decryption for StarLock .starlk backup files.
 * Uses PBKDF2WithHmacSHA256 for key derivation and AES-256-GCM for encryption.
 * Iteration count is intentionally higher than the app-lock password manager
 * since backup files may leave the device (cloud upload, file sharing).
 */
object BackupCryptoManager {

    private const val ITERATIONS = 210_000
    private const val KEY_LENGTH_BITS = 256
    private const val SALT_LENGTH_BYTES = 16
    private const val GCM_IV_LENGTH_BYTES = 12
    private const val GCM_TAG_LENGTH_BITS = 128
    private const val ALGORITHM = "AES/GCM/NoPadding"

    /**
     * Encrypts plaintext JSON with the given password.
     * Output format (all fields concatenated then Base64-encoded):
     * [salt (16 bytes)][iv (12 bytes)][ciphertext+tag]
     */
    fun encrypt(plainText: String, password: String): String {
        val salt = ByteArray(SALT_LENGTH_BYTES)
        SecureRandom().nextBytes(salt)

        val iv = ByteArray(GCM_IV_LENGTH_BYTES)
        SecureRandom().nextBytes(iv)

        val keyBytes = deriveKey(password, salt)
        val secretKey = SecretKeySpec(keyBytes, "AES")

        val cipher = Cipher.getInstance(ALGORITHM)
        val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec)

        val cipherText = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))

        val combined = salt + iv + cipherText
        return Base64.encodeToString(combined, Base64.NO_WRAP)
    }

    /**
     * Decrypts a .starlk backup payload with the given password.
     * Returns null if the password is wrong or the data is corrupted/tampered.
     */
    fun decrypt(encodedPayload: String, password: String): String? {
        return try {
            val combined = Base64.decode(encodedPayload, Base64.NO_WRAP)
            if (combined.size < SALT_LENGTH_BYTES + GCM_IV_LENGTH_BYTES) return null

            val salt = combined.copyOfRange(0, SALT_LENGTH_BYTES)
            val iv = combined.copyOfRange(SALT_LENGTH_BYTES, SALT_LENGTH_BYTES + GCM_IV_LENGTH_BYTES)
            val cipherText = combined.copyOfRange(SALT_LENGTH_BYTES + GCM_IV_LENGTH_BYTES, combined.size)

            val keyBytes = deriveKey(password, salt)
            val secretKey = SecretKeySpec(keyBytes, "AES")

            val cipher = Cipher.getInstance(ALGORITHM)
            val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec)

            val plainBytes = cipher.doFinal(cipherText)
            String(plainBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            null
        }
    }

    private fun deriveKey(password: String, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH_BITS)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        return factory.generateSecret(spec).encoded
    }
}
