package com.starrift.starlock.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.starrift.starlock.data.AccountField
import com.starrift.starlock.data.AppRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class AccountDetailViewModel(
    private val repository: AppRepository,
    val accountId: Long
) : ViewModel() {

    val fields: StateFlow<List<AccountField>> = repository.getFieldsForAccount(accountId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addField(label: String, value: String, isCustomLabel: Boolean) {
        viewModelScope.launch {
            val currentSize = fields.value.size
            repository.addField(
                accountId = accountId,
                label = label,
                value = value,
                isCustomLabel = isCustomLabel,
                orderIndex = currentSize
            )
        }
    }

    fun deleteField(field: AccountField) {
        viewModelScope.launch {
            repository.softDeleteField(field.id)
        }
    }


    private val _isSelectionMode = MutableStateFlow(false)
    val isSelectionMode: StateFlow<Boolean> = _isSelectionMode

    fun enterSelectionMode() {
        _isSelectionMode.value = true
    }

    fun exitSelectionMode() {
        _isSelectionMode.value = false
        _selectedIds.value = emptySet()
    }
    private val _selectedIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedIds: StateFlow<Set<Long>> = _selectedIds

    fun toggleSelect(id: Long) {
        _selectedIds.value = if (id in _selectedIds.value) {
            _selectedIds.value - id
        } else {
            _selectedIds.value + id
        }
        if (_selectedIds.value.isEmpty()) {
            _isSelectionMode.value = false
        }
    }

    fun moveField(fieldId: Long, direction: Int) {
        val current = fields.value.toMutableList()
        val index = current.indexOfFirst { it.id == fieldId }
        val targetIndex = index + direction
        if (index !in current.indices || targetIndex !in current.indices) return
        val temp = current[index]
        current[index] = current[targetIndex]
        current[targetIndex] = temp
        commitFieldOrder(current)
    }

    fun clearSelection() {
        _selectedIds.value = emptySet()
    }

    fun archiveSelectedFields() {
        viewModelScope.launch {
            val ids = _selectedIds.value
            ids.forEach { repository.archiveField(it) }
            _selectedIds.value = emptySet()
        }
    }

    fun deleteSelectedFields() {
        viewModelScope.launch {
            val ids = _selectedIds.value
            ids.forEach { repository.softDeleteField(it) }
            _selectedIds.value = emptySet()
        }
    }

    fun editField(fieldId: Long, label: String, value: String, isCustomLabel: Boolean) {
        viewModelScope.launch {
            val current = fields.value.find { it.id == fieldId } ?: return@launch
            repository.updateField(
                current.copy(label = label.trim(), value = value.trim(), isCustomLabel = isCustomLabel)
            )
            _selectedIds.value = emptySet()
        }
    }

    fun commitFieldOrder(newOrder: List<AccountField>) {
        val reindexed = newOrder.mapIndexed { index, field -> field.copy(orderIndex = index) }
        viewModelScope.launch {
            repository.updateFieldsOrder(reindexed)
        }
    }
}
