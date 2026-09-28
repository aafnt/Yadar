package com.yadar.app.ui.collections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.yadar.app.R
import com.yadar.app.domain.model.Collection

@Composable
fun CollectionsScreen(viewModel: CollectionsViewModel) {
    val collections by viewModel.collections.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingCollection by remember { mutableStateOf<Collection?>(null) }
    var deletingCollection by remember { mutableStateOf<Collection?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.collections_add))
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (collections.isEmpty()) {
                Text(
                    text = stringResource(R.string.collections_empty_title),
                    modifier = Modifier.align(Alignment.Center).padding(24.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                LazyColumn(
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(collections, key = { it.id }) { collection ->
                        CollectionRow(
                            collection = collection,
                            onToggleActive = { viewModel.setActive(collection, !collection.isActive) },
                            onEdit = { editingCollection = collection },
                            onDelete = { deletingCollection = collection }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        CollectionEditDialog(
            initialName = "",
            initialDescription = "",
            title = stringResource(R.string.collections_add),
            onDismiss = { showAddDialog = false },
            onConfirm = { name, desc ->
                viewModel.add(name, desc)
                showAddDialog = false
            }
        )
    }

    editingCollection?.let { collection ->
        CollectionEditDialog(
            initialName = collection.name,
            initialDescription = collection.description ?: "",
            title = stringResource(R.string.collections_edit),
            onDismiss = { editingCollection = null },
            onConfirm = { name, desc ->
                viewModel.update(collection.copy(name = name, description = desc.ifBlank { null }))
                editingCollection = null
            }
        )
    }

    deletingCollection?.let { collection ->
        DeleteCollectionDialog(
            onDismiss = { deletingCollection = null },
            onMoveToUncategorized = {
                viewModel.delete(collection.id, moveToUncategorized = true)
                deletingCollection = null
            },
            onDeleteSentences = {
                viewModel.delete(collection.id, moveToUncategorized = false)
                deletingCollection = null
            }
        )
    }
}

@Composable
private fun CollectionRow(
    collection: Collection,
    onToggleActive: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = collection.name, style = MaterialTheme.typography.titleMedium)
                collection.description?.let {
                    Text(text = it, style = MaterialTheme.typography.bodyMedium)
                }
            }
            Switch(checked = collection.isActive, onCheckedChange = { onToggleActive() })
            TextButton(onClick = onEdit) { Text(stringResource(R.string.collections_edit)) }
            TextButton(onClick = onDelete) { Text(stringResource(R.string.collections_delete)) }
        }
    }
}

@Composable
private fun CollectionEditDialog(
    initialName: String,
    initialDescription: String,
    title: String,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var description by remember { mutableStateOf(initialDescription) }
    var error by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; error = false },
                    label = { Text(stringResource(R.string.collections_name_hint)) },
                    isError = error,
                    supportingText = { if (error) Text(stringResource(R.string.error_empty_collection_name)) },
                    singleLine = true
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(stringResource(R.string.collections_description_hint)) }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (name.isBlank()) error = true else onConfirm(name.trim(), description.trim())
            }) { Text(stringResource(R.string.widget_config_save)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.collections_delete_cancel)) }
        }
    )
}

@Composable
private fun DeleteCollectionDialog(
    onDismiss: () -> Unit,
    onMoveToUncategorized: () -> Unit,
    onDeleteSentences: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.collections_delete_confirm_title)) },
        text = { Text(stringResource(R.string.collections_delete)) },
        confirmButton = {
            TextButton(onClick = onMoveToUncategorized) {
                Text(stringResource(R.string.collections_delete_move_to_uncategorized))
            }
        },
        dismissButton = {
            Column {
                TextButton(onClick = onDeleteSentences) {
                    Text(stringResource(R.string.collections_delete_remove_sentences))
                }
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.collections_delete_cancel))
                }
            }
        }
    )
}
