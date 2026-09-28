package com.yadar.app.ui.sentences

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import com.yadar.app.domain.model.Sentence
import com.yadar.app.domain.model.SentenceSortOrder

@Composable
fun SentencesScreen(
    viewModel: SentencesViewModel,
    onAddSentence: () -> Unit,
    onEditSentence: (Long) -> Unit
) {
    val sentences by viewModel.sentences.collectAsState()
    val collections by viewModel.collections.collectAsState()
    val query by viewModel.searchQuery.collectAsState()
    val sortOrder by viewModel.sortOrder.collectAsState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onAddSentence) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.sentences_add))
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp, 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = query,
                    onValueChange = viewModel::setSearchQuery,
                    label = { Text(stringResource(R.string.sentences_search_hint)) },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                SortMenuButton(current = sortOrder, onSelect = viewModel::setSortOrder)
            }

            if (sentences.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = stringResource(R.string.sentences_empty_title),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp, 0.dp, 16.dp, 96.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(sentences, key = { it.id }) { sentence ->
                        val collectionName = collections.firstOrNull { it.id == sentence.collectionId }?.name
                            ?: stringResource(R.string.collections_uncategorized)
                        SentenceRow(
                            sentence = sentence,
                            collectionName = collectionName,
                            onClick = { onEditSentence(sentence.id) },
                            onToggleActive = { viewModel.toggleActive(sentence) },
                            onDuplicate = { viewModel.duplicate(sentence) },
                            onDelete = { viewModel.delete(sentence) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SentenceRow(
    sentence: Sentence,
    collectionName: String,
    onClick: () -> Unit,
    onToggleActive: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            ) {
                Text(
                    text = sentence.text,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = collectionName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(Icons.Filled.MoreVert, contentDescription = null)
                }
                DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                    DropdownMenuItem(
                        text = { Text(stringResource(if (sentence.isActive) R.string.sentences_deactivate else R.string.sentences_activate)) },
                        onClick = { menuExpanded = false; onToggleActive() }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.sentences_duplicate)) },
                        onClick = { menuExpanded = false; onDuplicate() }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.sentences_edit)) },
                        onClick = { menuExpanded = false; onClick() }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.sentences_delete)) },
                        onClick = { menuExpanded = false; onDelete() }
                    )
                }
            }
        }
    }
}

@Composable
private fun SortMenuButton(current: SentenceSortOrder, onSelect: (SentenceSortOrder) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val labelRes = when (current) {
        SentenceSortOrder.NEWEST -> R.string.sentences_sort_newest
        SentenceSortOrder.OLDEST -> R.string.sentences_sort_oldest
        SentenceSortOrder.ALPHABETIC -> R.string.sentences_sort_alphabetic
        SentenceSortOrder.LAST_SHOWN -> R.string.sentences_sort_last_shown
        SentenceSortOrder.MANUAL -> R.string.sentences_sort_manual
    }
    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(Icons.Filled.MoreVert, contentDescription = stringResource(labelRes))
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            listOf(
                SentenceSortOrder.NEWEST to R.string.sentences_sort_newest,
                SentenceSortOrder.OLDEST to R.string.sentences_sort_oldest,
                SentenceSortOrder.ALPHABETIC to R.string.sentences_sort_alphabetic,
                SentenceSortOrder.LAST_SHOWN to R.string.sentences_sort_last_shown,
                SentenceSortOrder.MANUAL to R.string.sentences_sort_manual
            ).forEach { (order, res) ->
                DropdownMenuItem(text = { Text(stringResource(res)) }, onClick = { expanded = false; onSelect(order) })
            }
        }
    }
}
