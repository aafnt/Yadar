package com.yadar.app.ui.schedule

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.yadar.app.R
import com.yadar.app.util.PersianDigits

@Composable
fun ScheduleScreen(viewModel: ScheduleViewModel) {
    val widgets by viewModel.widgets.collectAsState()

    if (widgets.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(stringResource(R.string.widget_empty_state), style = MaterialTheme.typography.bodyMedium)
        }
        return
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(widgets, key = { it.config.widgetId }) { item ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Widget #" + PersianDigits.toPersian(item.config.widgetId),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text("مجموعه: ${item.collectionName}", style = MaterialTheme.typography.bodyMedium)
                    Text("فونت: ${item.config.font.name}", style = MaterialTheme.typography.bodyMedium)
                    Text("روش انتخاب: ${item.config.selectionMode.name}", style = MaterialTheme.typography.bodyMedium)
                    TextButton(onClick = { viewModel.resetHistory(item.config.widgetId) }) {
                        Text(stringResource(R.string.settings_reset_manual_order))
                    }
                }
            }
        }
    }
}
