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
        exitSelectionMode()
    }

    private val _isSelectionMode = MutableStateFlow(false)
    val isSelectionMode: StateFlow<Boolean> = _isSelectionMode

    private val _selectedIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedIds: StateFlow<Set<Long>> = _selectedIds

    fun enterSelectionMode(initialId: Long) {
        _isSelectionMode.value = true
        _selectedIds.value = setOf(initialId)
    }

    fun exitSelectionMode() {
        _isSelectionMode.value = false
        _selectedIds.value = emptySet()
    }

    fun toggleSelection(id: Long) {
        val current = _selectedIds.value
        _selectedIds.value = if (current.contains(id)) current - id else current + id
        if (_selectedIds.value.isEmpty()) {
            _isSelectionMode.value = false
        }
    }

    fun unarchiveSelected() {
        viewModelScope.launch {
            val ids = _selectedIds.value
            when (_selectedTab.value) {
                ArchivedTab.APPS -> ids.forEach { repository.unarchiveApp(it) }
                ArchivedTab.ACCOUNTS -> ids.forEach { repository.unarchiveAccount(it) }
                ArchivedTab.FIELDS -> ids.forEach { repository.unarchiveField(it) }
            }
            exitSelectionMode()
        }
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
