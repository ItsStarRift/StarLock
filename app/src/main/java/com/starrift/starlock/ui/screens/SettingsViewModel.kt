package com.starrift.starlock.ui.screens

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.starrift.starlock.data.AppRepository
import com.starrift.starlock.util.BackupCryptoManager
import com.starrift.starlock.util.WebDavClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SettingsViewModel(private val repository: AppRepository) : ViewModel() {

    suspend fun exportTo(context: Context, uri: Uri): Boolean = withContext(Dispatchers.IO) {
        try {
            val json = repository.exportAllDataAsJson()
            context.contentResolver.openOutputStream(uri)?.use { output ->
                output.write(json.toByteArray())
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun importFrom(context: Context, uri: Uri): Boolean = withContext(Dispatchers.IO) {
        try {
            val json = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                ?: return@withContext false
            repository.importAllDataFromJson(context, json)
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun exportEncryptedTo(context: Context, uri: Uri, password: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val json = repository.exportAllDataAsJson()
            val encrypted = BackupCryptoManager.encrypt(json, password)
            context.contentResolver.openOutputStream(uri)?.use { output ->
                output.write(encrypted.toByteArray())
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun importEncryptedFrom(context: Context, uri: Uri, password: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val encrypted = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                ?: return@withContext false
            val json = BackupCryptoManager.decrypt(encrypted, password) ?: return@withContext false
            repository.importAllDataFromJson(context, json)
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun exportEncryptedToWebDav(
        context: Context,
        url: String,
        username: String,
        password: String,
        backupPassword: String
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val json = repository.exportAllDataAsJson()
            val encrypted = BackupCryptoManager.encrypt(json, backupPassword)
            val result = WebDavClient.upload(url, username, password, encrypted.toByteArray())
            result.isSuccess
        } catch (e: Exception) {
            false
        }
    }

    suspend fun importEncryptedFromWebDav(
        context: Context,
        url: String,
        username: String,
        password: String,
        backupPassword: String
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val downloadResult = WebDavClient.download(url, username, password)
            val bytes = downloadResult.getOrNull() ?: return@withContext false
            val encrypted = String(bytes)
            val json = BackupCryptoManager.decrypt(encrypted, backupPassword) ?: return@withContext false
            repository.importAllDataFromJson(context, json)
            true
        } catch (e: Exception) {
            false
        }
    }
}

class SettingsViewModelFactory(private val repository: AppRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            return SettingsViewModel(repository) as T
        }
        throw IllegalArgumentException("Bilinmeyen ViewModel sınıfı: ${modelClass.name}")
    }
}
