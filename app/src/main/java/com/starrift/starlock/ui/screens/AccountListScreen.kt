package com.starrift.starlock.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import com.starrift.starlock.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.starrift.starlock.data.AccountItem
import com.starrift.starlock.ui.components.AddAccountDialog

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AccountListScreen(
    viewModel: AccountListViewModel,
    onBack: () -> Unit,
    onAccountClick: (Long) -> Unit
) {
    val app by viewModel.app.collectAsState()
    val accounts by viewModel.accounts.collectAsState()
    val favoriteAccounts by viewModel.favoriteAccounts.collectAsState()
    val nonFavoriteAccounts by viewModel.nonFavoriteAccounts.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val query by viewModel.searchQuery.collectAsState()
    val isSearchActive by viewModel.isSearchActive.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var selectedIds by remember { mutableStateOf(setOf<Long>()) }
    var confirmDeleteAccountsAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    var confirmArchiveAction by remember { mutableStateOf<(() -> Unit)?>(null) }

    val selectedAllFavorite = remember(selectedIds, accounts) {
        selectedIds.isNotEmpty() && accounts.filter { it.id in selectedIds }.all { it.isFavorite }
    }

    Scaffold(
        topBar = {
            if (selectedIds.isEmpty()) {
                if (isSearchActive) {
                    SearchTopBar(
                        query = query,
                        onQueryChange = viewModel::onSearchQueryChange,
                        onClose = { viewModel.onSearchActiveChange(false) }
                    )
                } else {
                    TopAppBar(
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(9.dp))
                                        .background(MaterialTheme.colorScheme.primary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (app?.iconPath != null) {
                                        AsyncImage(
                                            model = app?.iconPath,
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(9.dp))
                                        )
                                    } else {
                                        Text(
                                            text = app?.name?.take(1)?.uppercase() ?: "",
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(app?.name ?: "")
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = onBack) {
                                Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.cd_back))
                            }
                        },
                        actions = {
                            IconButton(onClick = { viewModel.onSearchActiveChange(true) }) {
                                Icon(Icons.Default.Search, contentDescription = stringResource(R.string.search_placeholder))
                            }
                        }
                    )
                }
            } else {
                SelectionTopBar(
                    count = selectedIds.size,
                    allFavorite = selectedAllFavorite,
                    onClose = { selectedIds = emptySet() },
                    onDelete = {
                        confirmDeleteAccountsAction = {
                            accounts.filter { it.id in selectedIds }
                                .forEach { viewModel.deleteAccount(it) }
                            selectedIds = emptySet()
                        }
                    },
                    onArchive = {
                            confirmArchiveAction = {
                        val ids = selectedIds
                        ids.forEach { viewModel.archiveAccount(setOf(it)) }
                        selectedIds = emptySet()
                            }
                    },
                    onEdit = { showEditDialog = true },
                    onToggleFavorite = {
                        viewModel.toggleFavorite(selectedIds, makeFavorite = !selectedAllFavorite)
                        selectedIds = emptySet()
                    }
                )
            }
        },
        floatingActionButton = {
            if (selectedIds.isEmpty() && !isSearchActive) {
                FloatingActionButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.cd_add_account))
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (isSearchActive && selectedIds.isEmpty()) {
                SelectableAccountList(
                    favoriteAccounts = emptyList(),
                    accounts = searchResults,
                    query = query,
                    selectedIds = selectedIds,
                    onAccountClick = { id ->
                        viewModel.onSearchActiveChange(false)
                        onAccountClick(id)
                    },
                    onToggleSelect = { id ->
                        selectedIds = if (id in selectedIds) selectedIds - id else selectedIds + id
                    },
                    onEnterSelection = { id -> selectedIds = setOf(id) }
                )
            } else {
                AnimatedContent(
                    targetState = accounts.isEmpty(),
                    transitionSpec = {
                        fadeIn(animationSpec = tween(250)) togetherWith fadeOut(animationSpec = tween(150))
                    },
                    label = "accounts-empty-list"
                ) { isEmpty ->
                    if (isEmpty) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                stringResource(R.string.accountlist_empty),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        SelectableAccountList(
                            favoriteAccounts = favoriteAccounts,
                            accounts = nonFavoriteAccounts,
                            query = "",
                            selectedIds = selectedIds,
                            onAccountClick = onAccountClick,
                            onToggleSelect = { id ->
                                selectedIds = if (id in selectedIds) selectedIds - id else selectedIds + id
                            },
                            onEnterSelection = { id -> selectedIds = setOf(id) }
                        )
                    }
                }
            }
        }

        if (showAddDialog) {
            AddAccountDialog(
                onDismiss = { showAddDialog = false },
                onSave = { name, iconPath ->
                    viewModel.addAccount(name, iconPath)
                    showAddDialog = false
                }
            )
        }

        if (showEditDialog) {
            val editingAccount = accounts.find { it.id in selectedIds }
            if (editingAccount != null) {
                AddAccountDialog(
                    onDismiss = { showEditDialog = false },
                    onSave = { name, iconPath ->
                        viewModel.updateAccount(editingAccount.id, name, iconPath)
                        showEditDialog = false
                        selectedIds = emptySet()
                    },
                    existingName = editingAccount.name,
                    existingIconPath = editingAccount.iconPath
                )
            }
        }
    }

    if (confirmDeleteAccountsAction != null) {
        AlertDialog(
            onDismissRequest = { confirmDeleteAccountsAction = null },
            title = { Text(stringResource(R.string.delete_permanently_confirm_title)) },
            text = { Text(stringResource(R.string.delete_account_text)) },
            confirmButton = {
                TextButton(onClick = {
                    confirmDeleteAccountsAction?.invoke()
                    confirmDeleteAccountsAction = null
                }) { Text(stringResource(R.string.delete_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { confirmDeleteAccountsAction = null }) { Text(stringResource(R.string.cancel)) }
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
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SelectableAccountList(
    favoriteAccounts: List<AccountItem>,
    accounts: List<AccountItem>,
    query: String,
    selectedIds: Set<Long>,
    onAccountClick: (Long) -> Unit,
    onToggleSelect: (Long) -> Unit,
    onEnterSelection: (Long) -> Unit
) {
    val selectionMode = selectedIds.isNotEmpty()
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        if (favoriteAccounts.isNotEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.favorites_header),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
                )
            }
            items(favoriteAccounts, key = { "fav_${it.id}" }) { account ->
                AccountRow(
                    account = account,
                    query = query,
                    selectionMode = selectionMode,
                    isSelected = account.id in selectedIds,
                    onClick = {
                        if (selectionMode) onToggleSelect(account.id) else onAccountClick(account.id)
                    },
                    onLongClick = { onEnterSelection(account.id) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
        items(accounts, key = { it.id }) { account ->
            AccountRow(
                account = account,
                query = query,
                selectionMode = selectionMode,
                isSelected = account.id in selectedIds,
                onClick = {
                    if (selectionMode) onToggleSelect(account.id) else onAccountClick(account.id)
                },
                onLongClick = { onEnterSelection(account.id) }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        item { Spacer(modifier = Modifier.height(96.dp)) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchTopBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClose: () -> Unit
) {
    TopAppBar(
        title = {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = { Text(stringResource(R.string.account_search_placeholder)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        navigationIcon = {
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = stringResource(R.string.cancel))
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectionTopBar(
    count: Int,
    allFavorite: Boolean,
    onClose: () -> Unit,
    onDelete: () -> Unit,
    onArchive: () -> Unit,
    onEdit: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = stringResource(R.string.cancel))
                }
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        actions = {
            IconButton(onClick = onToggleFavorite) {
                Icon(
                    if (allFavorite) Icons.Default.Star else Icons.Outlined.StarBorder,
                    contentDescription = stringResource(R.string.favorite)
                )
            }
            if (count == 1) {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.edit))
                }
            }
        IconButton(onClick = onArchive) {
            Icon(Icons.Default.Archive, contentDescription = "Arşivle")
        }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete_confirm))
            }
        }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AccountRow(
    account: AccountItem,
    query: String = "",
    selectionMode: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Row(
        modifier = Modifier
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
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            if (account.iconPath != null) {
                AsyncImage(
                    model = account.iconPath,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().clip(CircleShape)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = highlightAccountMatch(account.name, query),
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        if (selectionMode) {
            Checkbox(checked = isSelected, onCheckedChange = { onClick() })
        }
    }
}

private fun highlightAccountMatch(text: String, query: String): androidx.compose.ui.text.AnnotatedString {
    if (query.isBlank()) return androidx.compose.ui.text.AnnotatedString(text)
    val index = text.indexOf(query, ignoreCase = true)
    if (index < 0) return androidx.compose.ui.text.AnnotatedString(text)
    return androidx.compose.ui.text.buildAnnotatedString {
        append(text.substring(0, index))
        withStyle(androidx.compose.ui.text.SpanStyle(color = com.starrift.starlock.ui.theme.AccentOrange, fontWeight = FontWeight.Bold)) {
            append(text.substring(index, index + query.length))
        }
        append(text.substring(index + query.length))
    }
}
