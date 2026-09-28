package com.yadar.app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.yadar.app.R
import com.yadar.app.util.PersianDigits

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onAddSentence: () -> Unit,
    onManageWidgets: () -> Unit
) {
    val summary by viewModel.summary.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            text = stringResource(R.string.app_tagline),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )

        // پیش‌نمایش جمله فعلی، شبیه Widget واقعی (بند ۷: باید شبیه Widget واقعی باشد)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.home_current_sentence),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = summary?.previewSentence?.text ?: stringResource(R.string.sentences_empty_title),
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                modifier = Modifier,
                label = stringResource(R.string.home_active_sentences_count),
                value = PersianDigits.toPersian(summary?.activeSentenceCount ?: 0)
            )
            StatCard(
                modifier = Modifier,
                label = stringResource(R.string.home_collections_count),
                value = PersianDigits.toPersian(summary?.collectionCount ?: 0)
            )
            StatCard(
                modifier = Modifier,
                label = stringResource(R.string.home_widgets_count),
                value = PersianDigits.toPersian(summary?.widgetCount ?: 0)
            )
        }

        Text(
            text = stringResource(R.string.home_widget_status),
            style = MaterialTheme.typography.labelLarge
        )
        Text(
            text = if ((summary?.widgetCount ?: 0) > 0) {
                stringResource(R.string.home_widget_status_ok)
            } else {
                stringResource(R.string.home_widget_status_none)
            },
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(4.dp))

        Button(onClick = onAddSentence, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.home_add_sentence_shortcut))
        }
        OutlinedButton(onClick = onManageWidgets, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.home_manage_widgets_shortcut))
        }
    }
}

@Composable
private fun StatCard(modifier: Modifier = Modifier, label: String, value: String) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 16.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = value, style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}
