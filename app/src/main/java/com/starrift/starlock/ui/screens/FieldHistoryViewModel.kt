package com.starrift.starlock.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.starrift.starlock.data.AppRepository
import com.starrift.starlock.data.FieldHistoryEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class HistoryTab {
    CURRENT,
    DELETED
}

class FieldHistoryViewModel(
    private val repository: AppRepository,
    val accountId: Long
) : ViewModel() {

    private val _selectedTab = MutableStateFlow(HistoryTab.CURRENT)
    val selectedTab: StateFlow<HistoryTab> = _selectedTab

    fun selectTab(tab: HistoryTab) {
        _selectedTab.value = tab
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val historyEntries: StateFlow<List<FieldHistoryEntry>> = _selectedTab.flatMapLatest { tab ->
        when (tab) {
            HistoryTab.CURRENT -> repository.getCurrentFieldHistory(accountId)
            HistoryTab.DELETED -> repository.getDeletedFieldHistory(accountId)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearFieldHistory(accountId)
        }
    }
}
