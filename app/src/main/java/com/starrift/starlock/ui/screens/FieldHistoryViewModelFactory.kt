package com.starrift.starlock.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.starrift.starlock.data.AppRepository

class FieldHistoryViewModelFactory(
    private val repository: AppRepository,
    private val accountId: Long
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FieldHistoryViewModel::class.java)) {
            return FieldHistoryViewModel(repository, accountId) as T
        }
        throw IllegalArgumentException("Bilinmeyen ViewModel sınıfı: ${modelClass.name}")
    }
}
