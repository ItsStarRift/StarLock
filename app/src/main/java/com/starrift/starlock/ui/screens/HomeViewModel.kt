package com.starrift.starlock.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.starrift.starlock.data.AppCategory
import com.starrift.starlock.data.AppItem
import com.starrift.starlock.data.AppRepository
import com.starrift.starlock.data.AppWithAccountCount
import com.starrift.starlock.data.SortGroupKey
import com.starrift.starlock.data.SortOption
import com.starrift.starlock.util.SortPreferenceManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId

enum class CategoryFilter { ALL, APPS, GAMES }

class HomeViewModel(
    private val repository: AppRepository,
    private val sortPreferenceManager: SortPreferenceManager
) : ViewModel() {

    private val allApps: StateFlow<List<AppWithAccountCount>> =
        repository.getAllAppsWithCount()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _isSearchActive = MutableStateFlow(false)
    val isSearchActive: StateFlow<Boolean> = _isSearchActive

    private val _categoryFilter = MutableStateFlow(CategoryFilter.ALL)
    val categoryFilter: StateFlow<CategoryFilter> = _categoryFilter

    fun onCategoryFilterChange(filter: CategoryFilter) {
        _categoryFilter.value = filter
    }

    private val _sortOption = MutableStateFlow(sortPreferenceManager.getAppSortOption())
    val sortOption: StateFlow<SortOption> = _sortOption

    fun onSortOptionChange(option: SortOption) {
        _sortOption.value = option
        sortPreferenceManager.setAppSortOption(option)
    }

    private val filteredApps: StateFlow<List<AppWithAccountCount>> =
        allApps.combine(_categoryFilter) { apps, filter ->
            when (filter) {
                CategoryFilter.ALL -> apps
                CategoryFilter.APPS -> apps.filter { it.category == AppCategory.UYGULAMA }
                CategoryFilter.GAMES -> apps.filter { it.category == AppCategory.OYUN }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val alphabeticalGroups: StateFlow<List<Pair<SortGroupKey, List<AppWithAccountCount>>>> =
        filteredApps.combine(_sortOption) { apps, sort ->
            groupNonFavorites(apps.filter { !it.isFavorite }, sort)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteApps: StateFlow<List<AppWithAccountCount>> =
        filteredApps.combine(_sortOption) { apps, sort ->
            sortFlatList(apps.filter { it.isFavorite }, sort)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val searchResults: StateFlow<List<AppWithAccountCount>> =
        allApps.combine(_searchQuery) { apps, query ->
            if (query.isBlank()) emptyList()
            else apps.filter { it.name.contains(query, ignoreCase = true) }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onSearchActiveChange(active: Boolean) {
        _isSearchActive.value = active
        if (!active) _searchQuery.value = ""
    }

    fun addApp(name: String, category: AppCategory, iconPath: String?) {
        viewModelScope.launch {
            repository.addApp(name, category, iconPath)
        }
    }

    fun updateApp(id: Long, name: String, category: AppCategory, iconPath: String?) {
        viewModelScope.launch {
            val existing = repository.getAppById(id).first()
            repository.updateApp(
                AppItem(
                    id = id,
                    name = name,
                    category = category,
                    iconPath = iconPath,
                    createdAt = existing?.createdAt ?: System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis(),
                    isFavorite = existing?.isFavorite ?: false,
                    isDeleted = existing?.isDeleted ?: false,
                    deletedAt = existing?.deletedAt,
                    isArchived = existing?.isArchived ?: false,
                    archivedAt = existing?.archivedAt
                )
            )
        }
    }

    fun toggleFavorite(appIds: Set<Long>, makeFavorite: Boolean) {
        viewModelScope.launch {
            appIds.forEach { id -> repository.setFavorite(id, makeFavorite) }
        }
    }

    fun archiveApp(appIds: Set<Long>) {
        viewModelScope.launch {
            appIds.forEach { id -> repository.archiveApp(id) }
        }
    }

    fun deleteApp(app: AppWithAccountCount) {
        viewModelScope.launch {
            repository.softDeleteApp(app.id)
        }
    }

    private fun sortFlatList(
        apps: List<AppWithAccountCount>,
        sort: SortOption
    ): List<AppWithAccountCount> = when (sort) {
        SortOption.ALPHA_ASC -> apps.sortedBy { it.name.lowercase() }
        SortOption.ALPHA_DESC -> apps.sortedByDescending { it.name.lowercase() }
        SortOption.LAST_UPDATED -> apps.sortedByDescending { it.updatedAt }
    }

    private fun groupNonFavorites(
        apps: List<AppWithAccountCount>,
        sort: SortOption
    ): List<Pair<SortGroupKey, List<AppWithAccountCount>>> {
        return when (sort) {
            SortOption.ALPHA_ASC -> apps
                .groupBy { it.name.first().uppercaseChar() }
                .toSortedMap()
                .map { (letter, items) -> SortGroupKey.Letter(letter) as SortGroupKey to items.sortedBy { it.name.lowercase() } }

            SortOption.ALPHA_DESC -> apps
                .groupBy { it.name.first().uppercaseChar() }
                .toSortedMap(reverseOrder())
                .map { (letter, items) -> SortGroupKey.Letter(letter) as SortGroupKey to items.sortedByDescending { it.name.lowercase() } }

            SortOption.LAST_UPDATED -> {
                val zone = ZoneId.systemDefault()
                apps
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
