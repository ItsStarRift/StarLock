package com.starrift.starlock.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.starrift.starlock.data.AccountItem
import com.starrift.starlock.data.AppItem
import com.starrift.starlock.data.AppRepository
import com.starrift.starlock.data.SortGroupKey
import com.starrift.starlock.data.SortOption
import com.starrift.starlock.util.SortPreferenceManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId

class AccountListViewModel(
    private val repository: AppRepository,
    private val appId: Long,
    private val sortPreferenceManager: SortPreferenceManager
) : ViewModel() {

    val app: StateFlow<AppItem?> =
        repository.getAppById(appId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val allAccounts: StateFlow<List<AccountItem>> =
        repository.getAccountsForApp(appId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val accounts: StateFlow<List<AccountItem>> = allAccounts

    private val _sortOption = MutableStateFlow(sortPreferenceManager.getAccountSortOption())
    val sortOption: StateFlow<SortOption> = _sortOption

    fun onSortOptionChange(option: SortOption) {
        _sortOption.value = option
        sortPreferenceManager.setAccountSortOption(option)
    }

    val favoriteAccounts: StateFlow<List<AccountItem>> =
        allAccounts.combine(_sortOption) { list, sort ->
            sortFlatList(list.filter { it.isFavorite }, sort)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val groupedAccounts: StateFlow<List<Pair<SortGroupKey, List<AccountItem>>>> =
        allAccounts.combine(_sortOption) { list, sort ->
            groupNonFavorites(list.filter { !it.isFavorite }, sort)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _isSearchActive = MutableStateFlow(false)
    val isSearchActive: StateFlow<Boolean> = _isSearchActive

    val searchResults: StateFlow<List<AccountItem>> =
        allAccounts.combine(_searchQuery) { list, query ->
            if (query.isBlank()) emptyList()
            else list.filter { it.name.contains(query, ignoreCase = true) }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onSearchActiveChange(active: Boolean) {
        _isSearchActive.value = active
        if (!active) _searchQuery.value = ""
    }

    fun addAccount(name: String, iconPath: String?, tag: String? = null) {
        viewModelScope.launch {
            repository.addAccount(appId, name, iconPath, tag)
        }
    }

    fun updateAccount(id: Long, name: String, iconPath: String?, tag: String? = null) {
        viewModelScope.launch {
            val existing = allAccounts.value.find { it.id == id }
            repository.updateAccount(
                AccountItem(
                    id = id,
                    appId = appId,
                    name = name,
                    iconPath = iconPath,
                    createdAt = existing?.createdAt ?: System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis(),
                    isFavorite = existing?.isFavorite ?: false,
                    isDeleted = existing?.isDeleted ?: false,
                    deletedAt = existing?.deletedAt,
                    isArchived = existing?.isArchived ?: false,
                    archivedAt = existing?.archivedAt,
                    tag = tag?.trim()?.ifBlank { null }
                )
            )
        }
    }

    fun archiveAccount(accountIds: Set<Long>) {
        viewModelScope.launch {
            accountIds.forEach { id -> repository.archiveAccount(id) }
        }
    }

    fun deleteAccount(account: AccountItem) {
        viewModelScope.launch {
            repository.softDeleteAccount(account.id)
        }
    }

    fun toggleFavorite(accountIds: Set<Long>, makeFavorite: Boolean) {
        viewModelScope.launch {
            accountIds.forEach { id -> repository.setAccountFavorite(id, makeFavorite) }
        }
    }

    private fun sortFlatList(list: List<AccountItem>, sort: SortOption): List<AccountItem> = when (sort) {
        SortOption.ALPHA_ASC -> list.sortedBy { it.name.lowercase() }
        SortOption.ALPHA_DESC -> list.sortedByDescending { it.name.lowercase() }
        SortOption.LAST_UPDATED -> list.sortedByDescending { it.updatedAt }
    }

    private fun groupNonFavorites(
        list: List<AccountItem>,
        sort: SortOption
    ): List<Pair<SortGroupKey, List<AccountItem>>> {
        return when (sort) {
            SortOption.ALPHA_ASC -> list
                .groupBy { it.name.first().uppercaseChar() }
                .toSortedMap()
                .map { (letter, items) -> SortGroupKey.Letter(letter) as SortGroupKey to items.sortedBy { it.name.lowercase() } }

            SortOption.ALPHA_DESC -> list
                .groupBy { it.name.first().uppercaseChar() }
                .toSortedMap(reverseOrder())
                .map { (letter, items) -> SortGroupKey.Letter(letter) as SortGroupKey to items.sortedByDescending { it.name.lowercase() } }

            SortOption.LAST_UPDATED -> {
                val zone = ZoneId.systemDefault()
                list
                    .groupBy { Instant.ofEpochMilli(it.updatedAt).atZone(zone).toLocalDate().toEpochDay() }
                    .toSortedMap(reverseOrder())
                    .map { (epochDay, items) ->
                        val sortedItems = items.sortedByDescending { it.updatedAt }
                        val key = SortGroupKey.DateGroup(
                            epochDay = epochDay,
                            itemCount = sortedItems.size,
                            singleUpdatedAtMillis = if (sortedItems.size == 1) sortedItems.first().updatedAt else null
                        )
                        key as SortGroupKey to sortedItems
                    }
            }
        }
    }
}

class AccountListViewModelFactory(
    private val repository: AppRepository,
    private val appId: Long,
    private val sortPreferenceManager: SortPreferenceManager
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AccountListViewModel::class.java)) {
            return AccountListViewModel(repository, appId, sortPreferenceManager) as T
        }
        throw IllegalArgumentException("Bilinmeyen ViewModel sınıfı: ${modelClass.name}")
    }
}
