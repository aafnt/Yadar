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
import android.appwidget.AppWidgetManager
import android.content.Intent
import androidx.compose.ui.platform.LocalContext
import com.yadar.app.widget.WidgetConfigActivity
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.yadar.app.R
import com.yadar.app.util.PersianDigits

@Composable
fun ScheduleScreen(viewModel: ScheduleViewModel) {
    val widgets by viewModel.widgets.collectAsState()
    val context = LocalContext.current

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
                    val eligibleColor = if (item.eligibleNowCount <= 1) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                    Text(
                        "جمله‌های واجد شرایط الان: ${PersianDigits.toPersian(item.eligibleNowCount)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = eligibleColor
                    )
                    if (item.eligibleNowCount <= 1) {
                        Text(
                            "اگر عدد بالا ۱ یا صفر است، لمس Widget اثر محسوسی نخواهد داشت — " +
                                "یا مجموعه انتخابی این Widget را به «همه مجموعه‌ها» تغییر بده، یا " +
                                "قوانین روز/ساعت جمله‌های دیگر را بررسی کن.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    TextButton(onClick = {
                        val intent = Intent(context, WidgetConfigActivity::class.java)
                            .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, item.config.widgetId)
                        context.startActivity(intent)
                    }) { Text("ویرایش ظاهر و تنظیمات Widget") }
                    TextButton(onClick = { viewModel.resetHistory(item.config.widgetId) }) {
                        Text(stringResource(R.string.settings_reset_manual_order))
                    }
                }
            }
        }
    }
}
