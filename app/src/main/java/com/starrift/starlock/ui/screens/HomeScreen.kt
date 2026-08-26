package com.starrift.starlock.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.starrift.starlock.R
import coil.compose.AsyncImage
import com.starrift.starlock.data.AppCategory
import com.starrift.starlock.data.AppWithAccountCount
import com.starrift.starlock.ui.components.AddAppDialog
import com.starrift.starlock.ui.theme.AccentOrange
import androidx.compose.foundation.layout.safeDrawingPadding

private enum class BottomTab { UYGULAMALAR, AYARLAR }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onAppClick: (Long) -> Unit,
    settingsContent: @Composable () -> Unit
) {
    val groups by viewModel.alphabeticalGroups.collectAsState()
    val favoriteApps by viewModel.favoriteApps.collectAsState()
    val categoryFilter by viewModel.categoryFilter.collectAsState()
    val sortOption by viewModel.sortOption.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val query by viewModel.searchQuery.collectAsState()
    val isSearchActive by viewModel.isSearchActive.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showSortDialog by remember { mutableStateOf(false) }
    var selectedTab by rememberSaveable { mutableStateOf(BottomTab.UYGULAMALAR) }
    var confirmDeleteAppsAction by remember { mutableStateOf<(() -> Unit)?>(null) }
        var confirmArchiveAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    var selectedIds by remember { mutableStateOf(setOf<Long>()) }

    val allAppsFlat = remember(groups, favoriteApps) { favoriteApps + groups.flatMap { it.second } }
    val selectedAppsAllFavorite = remember(selectedIds, allAppsFlat) {
        selectedIds.isNotEmpty() && allAppsFlat.filter { it.id in selectedIds }.all { it.isFavorite }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize().safeDrawingPadding(),
        floatingActionButton = {
            if (selectedTab == BottomTab.UYGULAMALAR && !isSearchActive && selectedIds.isEmpty()) {
                val rotation by animateFloatAsState(
                    targetValue = if (showAddDialog) 45f else 0f,
                    animationSpec = tween(200),
                    label = "fab-rotation"
                )
                FloatingActionButton(onClick = { showAddDialog = true }) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = stringResource(R.string.cd_add_app),
                        modifier = Modifier.rotate(rotation)
                    )
                }
            }
        },
        bottomBar = {
            if (selectedIds.isEmpty()) {
                FloatingBottomNav(
                    selected = selectedTab,
                    onSelect = { selectedTab = it }
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (selectedTab) {
                BottomTab.UYGULAMALAR -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        if (selectedIds.isEmpty()) {
                            SearchBar(
                                query = query,
                                onQueryChange = viewModel::onSearchQueryChange,
                                onSearch = {},
                                active = isSearchActive,
                                onActiveChange = viewModel::onSearchActiveChange,
                                placeholder = { Text(stringResource(R.string.search_placeholder)) },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                SearchResultsList(
                                    results = searchResults,
                                    query = query,
                                    onAppClick = {
                                        viewModel.onSearchActiveChange(false)
                                        onAppClick(it)
                                    },
                                    selectedIds = selectedIds,
                                    onToggleSelect = { id ->
                                        selectedIds = if (id in selectedIds) selectedIds - id else selectedIds + id
                                    },
                                    onEnterSelection = { id ->
                                        selectedIds = setOf(id)
                                    }
                                )
                            }

                            if (!isSearchActive) {
                                CategoryFilterRow(
                                    selected = categoryFilter,
                        onSelect = viewModel::onCategoryFilterChange,
                        onSortClick = { showSortDialog = true }
                                )
                                AppGroupedList(
                                    groups = groups,
                                    favoriteApps = favoriteApps,
                                    onAppClick = onAppClick,
                                    selectedIds = selectedIds,
                                    onToggleSelect = { id ->
                                        selectedIds = if (id in selectedIds) selectedIds - id else selectedIds + id
                                    },
                                    onEnterSelection = { id ->
                                        selectedIds = setOf(id)
                                    }
                                )
                            }
                        } else {
                            SelectionBar(
                                count = selectedIds.size,
                                allFavorite = selectedAppsAllFavorite,
                                onClose = { selectedIds = emptySet() },
                                onDelete = {
                                    confirmDeleteAppsAction = {
                                        allAppsFlat.filter { it.id in selectedIds }
                                            .forEach { viewModel.deleteApp(it) }
                                        selectedIds = emptySet()
                                    }
                                },
                onArchive = {
                            confirmArchiveAction = {
                    val ids = selectedIds
                    ids.forEach { viewModel.archiveApp(setOf(it)) }
                    selectedIds = emptySet()
                            }
                },
                                onEdit = { showEditDialog = true },
                                onToggleFavorite = {
                                    viewModel.toggleFavorite(selectedIds, makeFavorite = !selectedAppsAllFavorite)
                                    selectedIds = emptySet()
                                }
                            )

                            if (isSearchActive) {
                                SearchResultsList(
                                    results = searchResults,
                                    query = query,
                                    onAppClick = { },
                                    selectedIds = selectedIds,
                                    onToggleSelect = { id ->
                                        selectedIds = if (id in selectedIds) selectedIds - id else selectedIds + id
                                    },
                                    onEnterSelection = { id ->
                                        selectedIds = setOf(id)
                                    }
                                )
                            } else {
                                AppGroupedList(
                                    groups = groups,
                                    favoriteApps = favoriteApps,
                                    onAppClick = onAppClick,
                                    selectedIds = selectedIds,
                                    onToggleSelect = { id ->
                                        selectedIds = if (id in selectedIds) selectedIds - id else selectedIds + id
                                    },
                                    onEnterSelection = { id ->
                                        selectedIds = setOf(id)
                                    }
                                )
                            }
                        }
                    }
                }
                BottomTab.AYARLAR -> {
                    settingsContent()
                }
            }
        }
    }

    if (showAddDialog) {
        AddAppDialog(
            onDismiss = { showAddDialog = false },
            onSave = { name, category, iconPath ->
                viewModel.addApp(name, category, iconPath)
            }
        )
    }

    if (showEditDialog) {
        val editingApp = allAppsFlat.find { it.id in selectedIds }
        if (editingApp != null) {
            AddAppDialog(
                onDismiss = { showEditDialog = false },
                onSave = { name, category, iconPath ->
                    viewModel.updateApp(editingApp.id, name, category, iconPath)
                    selectedIds = emptySet()
                },
                existingName = editingApp.name,
                existingCategory = editingApp.category,
                existingIconPath = editingApp.iconPath
            )
        }
    }

    if (confirmDeleteAppsAction != null) {
        AlertDialog(
            onDismissRequest = { confirmDeleteAppsAction = null },
            title = { Text(stringResource(R.string.delete_permanently_confirm_title)) },
            text = { Text(stringResource(R.string.delete_app_text)) },
            confirmButton = {
                TextButton(onClick = {
                    confirmDeleteAppsAction?.invoke()
                    confirmDeleteAppsAction = null
                }) { Text(stringResource(R.string.delete_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { confirmDeleteAppsAction = null }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }

        if (confirmArchiveAction != null) {
            AlertDialog(
                onDismissRequest = { confirmArchiveAction = null },
                title = { Text(stringResource(R.string.archive_confirm_title)) },
                text = { Text(stringResource(R.string.archive_confirm_text)) },
                confirmButton = {
                    TextButton(onClick = {
                        confirmArchiveAction?.invoke()
                        confirmArchiveAction = null
                    }) { Text(stringResource(R.string.archive)) }
                },
                dismissButton = {
                    TextButton(onClick = { confirmArchiveAction = null }) { Text(stringResource(R.string.cancel)) }
                }
            )
        }

    if (showSortDialog) {
        SortOptionDialog(
            current = sortOption,
            onSelect = viewModel::onSortOptionChange,
            onDismiss = { showSortDialog = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryFilterRow(
    selected: CategoryFilter,
    onSelect: (CategoryFilter) -> Unit,
    onSortClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier.weight(1f)
        ) {
            SegmentedButton(
                selected = selected == CategoryFilter.ALL,
                onClick = { onSelect(CategoryFilter.ALL) },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3)
            ) {
                Text(stringResource(R.string.filter_all))
            }
            SegmentedButton(
                selected = selected == CategoryFilter.APPS,
                onClick = { onSelect(CategoryFilter.APPS) },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3)
            ) {
                Text(stringResource(R.string.filter_apps))
            }
            SegmentedButton(
                selected = selected == CategoryFilter.GAMES,
                onClick = { onSelect(CategoryFilter.GAMES) },
                shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3)
            ) {
                Text(stringResource(R.string.filter_games))
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        IconButton(onClick = onSortClick) {
            Icon(
                Icons.Filled.FilterList,
                contentDescription = stringResource(R.string.cd_sort)
            )
        }
    }
}

@Composable
private fun SelectionBar(
    count: Int,
    allFavorite: Boolean,
    onClose: () -> Unit,
    onDelete: () -> Unit,
    onArchive: () -> Unit,
    onEdit: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onClose) {
            Icon(Icons.Default.Close, contentDescription = stringResource(R.string.cancel))
        }
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.weight(1f))
        IconButton(onClick = onToggleFavorite) {
            Icon(
                if (allFavorite) Icons.Default.Star else Icons.Outlined.StarBorder,
                contentDescription = stringResource(R.string.favorite)
            )
        }
        IconButton(onClick = onEdit, enabled = count == 1) {
            Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.edit))
        }
        IconButton(onClick = onArchive) {
            Icon(Icons.Default.Archive, contentDescription = "Arşivle")
        }
        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete_confirm))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SortOptionDialog(
    current: com.starrift.starlock.data.SortOption,
    onSelect: (com.starrift.starlock.data.SortOption) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.sort_title)) },
        text = {
            Column {
                val options = listOf(
                    com.starrift.starlock.data.SortOption.ALPHA_ASC to stringResource(R.string.sort_alpha_asc),
                    com.starrift.starlock.data.SortOption.ALPHA_DESC to stringResource(R.string.sort_alpha_desc),
                    com.starrift.starlock.data.SortOption.LAST_UPDATED to stringResource(R.string.sort_last_updated)
                )
                options.forEach { (option, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(option); onDismiss() }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = current == option, onClick = { onSelect(option); onDismiss() })
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(label)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_ok)) }
        }
    )
}

@Composable
private fun formatSortGroupHeader(key: com.starrift.starlock.data.SortGroupKey): String {
    return when (key) {
        is com.starrift.starlock.data.SortGroupKey.Letter -> key.char.toString()
        is com.starrift.starlock.data.SortGroupKey.DateGroup -> {
            val zone = java.time.ZoneId.systemDefault()
            val today = java.time.LocalDate.now(zone).toEpochDay()
            val date = java.time.LocalDate.ofEpochDay(key.epochDay)
            when {
                key.itemCount == 1 && key.singleUpdatedAtMillis != null -> {
                    val instant = java.time.Instant.ofEpochMilli(key.singleUpdatedAtMillis)
                    val zdt = instant.atZone(zone)
                    val datePart = zdt.format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy"))
                    val timePart = zdt.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))
                    val offset = zdt.format(java.time.format.DateTimeFormatter.ofPattern("xxx"))
                    "$datePart $timePart (UTC$offset)"
                }
                key.epochDay == today -> stringResource(R.string.sort_date_today)
                key.epochDay == today - 1 -> stringResource(R.string.sort_date_yesterday)
                else -> date.format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy"))
            }
        }
    }
}

@Composable
private fun AppGroupedList(
    groups: List<Pair<com.starrift.starlock.data.SortGroupKey, List<AppWithAccountCount>>>,
    favoriteApps: List<AppWithAccountCount>,
    onAppClick: (Long) -> Unit,
    selectedIds: Set<Long>,
    onToggleSelect: (Long) -> Unit,
    onEnterSelection: (Long) -> Unit
) {
    if (groups.isEmpty() && favoriteApps.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                stringResource(R.string.home_empty),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    val selectionMode = selectedIds.isNotEmpty()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
    ) {
        if (favoriteApps.isNotEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.favorites_header),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 8.dp, top = 12.dp, bottom = 4.dp)
                )
            }
            items(favoriteApps, key = { "fav_${it.id}" }) { app ->
                AppRow(
                    app = app,
                    selectionMode = selectionMode,
                    isSelected = app.id in selectedIds,
                    onClick = {
                        if (selectionMode) onToggleSelect(app.id) else onAppClick(app.id)
                    },
                    onLongClick = { onEnterSelection(app.id) },
                    modifier = Modifier
                )
                Spacer(Modifier.height(8.dp))
            }
        }
        groups.forEach { (groupKey, apps) ->
            item {
                Text(
                    text = formatSortGroupHeader(groupKey),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 8.dp, top = 12.dp, bottom = 4.dp)
                )
            }
            items(apps, key = { it.id }) { app ->
                AppRow(
                    app = app,
                    selectionMode = selectionMode,
                    isSelected = app.id in selectedIds,
                    onClick = {
                        if (selectionMode) onToggleSelect(app.id) else onAppClick(app.id)
                    },
                    onLongClick = { onEnterSelection(app.id) },
                    modifier = Modifier
                )
                Spacer(Modifier.height(8.dp))
            }
        }
        item { Spacer(Modifier.height(96.dp)) }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SearchResultsList(
    results: List<AppWithAccountCount>,
    query: String,
    onAppClick: (Long) -> Unit,
    selectedIds: Set<Long>,
    onToggleSelect: (Long) -> Unit,
    onEnterSelection: (Long) -> Unit
) {
    val selectionMode = selectedIds.isNotEmpty()
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(results, key = { it.id }) { app ->
            HighlightedAppRow(
                app = app,
                query = query,
                selectionMode = selectionMode,
                isSelected = app.id in selectedIds,
                onClick = {
                    if (selectionMode) onToggleSelect(app.id) else onAppClick(app.id)
                },
                onLongClick = { onEnterSelection(app.id) }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AppRow(
    app: AppWithAccountCount,
    selectionMode: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppIconCircle(app)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(app.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = subtitleFor(app),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
        if (selectionMode) {
            Checkbox(checked = isSelected, onCheckedChange = { onClick() })
        }
    }
}

@Composable
private fun subtitleFor(app: AppWithAccountCount): String {
    val categoryLabel = if (app.category == AppCategory.OYUN) {
        stringResource(R.string.category_label_game)
    } else {
        stringResource(R.string.category_label_app)
    }
    return stringResource(R.string.account_count, app.accountCount) + "  •  " + categoryLabel
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HighlightedAppRow(
    app: AppWithAccountCount,
    query: String,
    selectionMode: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppIconCircle(app)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = highlightMatch(app.name, query), style = MaterialTheme.typography.bodyLarge)
            Text(
                text = subtitleFor(app),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
        if (selectionMode) {
            Checkbox(checked = isSelected, onCheckedChange = { onClick() })
        }
    }
}

private fun highlightMatch(text: String, query: String): AnnotatedString {
    if (query.isBlank()) return AnnotatedString(text)
    val index = text.indexOf(query, ignoreCase = true)
    if (index < 0) return AnnotatedString(text)
    return buildAnnotatedString {
        append(text.substring(0, index))
        withStyle(SpanStyle(color = AccentOrange, fontWeight = FontWeight.Bold)) {
            append(text.substring(index, index + query.length))
        }
        append(text.substring(index + query.length))
    }
}

@Composable
private fun AppIconCircle(app: AppWithAccountCount) {
    val iconShape = RoundedCornerShape(12.dp)
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(iconShape)
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center
    ) {
        if (app.iconPath != null) {
            AsyncImage(
                model = app.iconPath,
                contentDescription = null,
                modifier = Modifier.fillMaxSize().clip(iconShape),
                contentScale = ContentScale.Crop
            )
        } else {
            Text(
                app.name.take(1).uppercase(),
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun FloatingBottomNav(
    selected: BottomTab,
    onSelect: (BottomTab) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(32.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 24.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(40.dp)
        ) {
            NavItem(
                icon = Icons.Default.Apps,
                label = stringResource(R.string.nav_apps),
                selected = selected == BottomTab.UYGULAMALAR,
                onClick = { onSelect(BottomTab.UYGULAMALAR) }
            )
            NavItem(
                icon = Icons.Default.Settings,
                label = stringResource(R.string.settings_title),
                selected = selected == BottomTab.AYARLAR,
                onClick = { onSelect(BottomTab.AYARLAR) }
            )
        }
    }
}

@Composable
private fun NavItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Icon(icon, contentDescription = label, tint = tint)
        Spacer(Modifier.height(2.dp))
        Text(label, style = MaterialTheme.typography.bodySmall, color = tint)
    }
}
