package com.starrift.starlock.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.starrift.starlock.data.AccountWithAppName
import com.starrift.starlock.data.AccountFieldWithAccountName
import com.starrift.starlock.data.AppItem
import com.starrift.starlock.data.AppRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class ArchivedTab { APPS, ACCOUNTS, FIELDS }

class ArchivedViewModel(private val repository: AppRepository) : ViewModel() {

    private val _selectedTab = MutableStateFlow(ArchivedTab.APPS)
    val selectedTab: StateFlow<ArchivedTab> = _selectedTab

    fun onTabChange(tab: ArchivedTab) {
        _selectedTab.value = tab
    }

    val archivedApps: StateFlow<List<AppItem>> =
        repository.getArchivedApps()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val archivedAccounts: StateFlow<List<AccountWithAppName>> =
        repository.getArchivedAccounts()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun unarchiveApp(id: Long) {
        viewModelScope.launch { repository.unarchiveApp(id) }
    }

    fun unarchiveAccount(id: Long) {
        viewModelScope.launch { repository.unarchiveAccount(id) }
    }

    val archivedFields: StateFlow<List<AccountFieldWithAccountName>> =
        repository.getArchivedFields()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun unarchiveField(id: Long) {
        viewModelScope.launch { repository.unarchiveField(id) }
    }
}

class ArchivedViewModelFactory(
    private val repository: AppRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ArchivedViewModel::class.java)) {
            return ArchivedViewModel(repository) as T
        }
        throw IllegalArgumentException("Bilinmeyen ViewModel sınıfı: ${modelClass.name}")
    }
}
