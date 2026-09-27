package com.yadar.app.ui.sentences

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yadar.app.R
import com.yadar.app.data.local.entity.DisplayRuleType
import com.yadar.app.domain.usecase.SENTENCE_MAX_LENGTH
import com.yadar.app.util.PersianDate
import com.yadar.app.util.PersianDigits
import java.time.DayOfWeek
import java.time.LocalDate

private val weekdayOrder = listOf(
    DayOfWeek.SATURDAY, DayOfWeek.SUNDAY, DayOfWeek.MONDAY, DayOfWeek.TUESDAY,
    DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY
)

private fun weekdayLabelRes(day: DayOfWeek) = when (day) {
    DayOfWeek.SATURDAY -> R.string.weekday_saturday
    DayOfWeek.SUNDAY -> R.string.weekday_sunday
    DayOfWeek.MONDAY -> R.string.weekday_monday
    DayOfWeek.TUESDAY -> R.string.weekday_tuesday
    DayOfWeek.WEDNESDAY -> R.string.weekday_wednesday
    DayOfWeek.THURSDAY -> R.string.weekday_thursday
    DayOfWeek.FRIDAY -> R.string.weekday_friday
}

private fun ruleTypeLabelRes(type: DisplayRuleType) = when (type) {
    DisplayRuleType.DAILY -> R.string.display_mode_daily
    DisplayRuleType.ON_CHANGE -> R.string.display_mode_on_change
    DisplayRuleType.SEQUENTIAL -> R.string.display_mode_sequential
    DisplayRuleType.RANDOM -> R.string.display_mode_random
    DisplayRuleType.BY_WEEKDAY -> R.string.display_mode_by_weekday
    DisplayRuleType.BY_TIME -> R.string.display_mode_by_time
    DisplayRuleType.BY_DATE -> R.string.display_mode_by_date
    DisplayRuleType.COMBINED -> R.string.display_mode_combined
}

@Composable
fun AddEditSentenceScreen(viewModel: AddEditSentenceViewModel, onDone: () -> Unit) {
    val state by viewModel.uiState.collectAsState()
    val collections by viewModel.collections.collectAsState()

    LaunchedEffect(state.saved) {
        if (state.saved) onDone()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = state.text,
            onValueChange = viewModel::setText,
            label = { Text(stringResource(R.string.sentences_text_hint)) },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            maxLines = 8,
            supportingText = {
                val remaining = SENTENCE_MAX_LENGTH - state.text.length
                if (remaining < 0) {
                    Text(stringResource(R.string.sentences_text_too_long))
                } else {
                    Text(PersianDigits.toPersian(state.text.length) + " / " + PersianDigits.toPersian(SENTENCE_MAX_LENGTH))
                }
            },
            isError = state.text.length > SENTENCE_MAX_LENGTH
        )

        CollectionDropdown(
            collections = collections,
            selectedId = state.collectionId,
            onSelect = viewModel::setCollection
        )

        Text(stringResource(R.string.sentences_choose_display_rule), fontWeight = FontWeight.Medium)
        RuleTypeDropdown(selected = state.ruleType, onSelect = viewModel::setRuleType)

        if (state.ruleType == DisplayRuleType.BY_WEEKDAY || state.ruleType == DisplayRuleType.COMBINED) {
            WeekdayChips(selected = state.weekdays, onToggle = viewModel::toggleWeekday)
        }

        if (state.ruleType == DisplayRuleType.BY_TIME || state.ruleType == DisplayRuleType.COMBINED) {
            TimeRangeFields(
                startMinute = state.timeStartMinute,
                endMinute = state.timeEndMinute,
                onChange = viewModel::setTimeRange
            )
        }

        if (state.ruleType == DisplayRuleType.BY_DATE || state.ruleType == DisplayRuleType.COMBINED) {
            DateRangeFields(
                startEpochDay = state.startDateEpochDay,
                endEpochDay = state.endDateEpochDay,
                onChange = viewModel::setDateRange
            )
        }

        state.validationError?.let {
            Text(stringResource(R.string.error_save_sentence_failed), color = MaterialTheme.colorScheme.error)
        }

        Button(
            onClick = viewModel::save,
            modifier = Modifier.fillMaxWidth(),
            enabled = state.text.isNotBlank() && state.text.length <= SENTENCE_MAX_LENGTH
        ) {
            Text(stringResource(R.string.widget_config_save))
        }
    }
}

@Composable
private fun CollectionDropdown(
    collections: List<com.yadar.app.domain.model.Collection>,
    selectedId: Long?,
    onSelect: (Long?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedName = collections.firstOrNull { it.id == selectedId }?.name
        ?: stringResource(R.string.collections_uncategorized)

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selectedName,
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.sentences_choose_collection)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor()
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.collections_uncategorized)) },
                onClick = { onSelect(null); expanded = false }
            )
            collections.forEach { collection ->
                DropdownMenuItem(
                    text = { Text(collection.name) },
                    onClick = { onSelect(collection.id); expanded = false }
                )
            }
        }
    }
}

@Composable
private fun RuleTypeDropdown(selected: DisplayRuleType, onSelect: (DisplayRuleType) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = stringResource(ruleTypeLabelRes(selected)),
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor()
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DisplayRuleType.entries.forEach { type ->
                DropdownMenuItem(
                    text = { Text(stringResource(ruleTypeLabelRes(type))) },
                    onClick = { onSelect(type); expanded = false }
                )
            }
        }
    }
}

@Composable
private fun WeekdayChips(selected: Set<DayOfWeek>, onToggle: (DayOfWeek) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        weekdayOrder.forEach { day ->
            FilterChip(
                selected = day in selected,
                onClick = { onToggle(day) },
                label = { Text(stringResource(weekdayLabelRes(day))) }
            )
        }
    }
}

@Composable
private fun TimeRangeFields(startMinute: Int?, endMinute: Int?, onChange: (Int?, Int?) -> Unit) {
    var startText by remember { mutableStateOf(startMinute?.let { formatMinute(it) } ?: "") }
    var endText by remember { mutableStateOf(endMinute?.let { formatMinute(it) } ?: "") }

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = startText,
            onValueChange = { startText = it; onChange(parseMinute(it), parseMinute(endText)) },
            label = { Text("از (HH:MM)") },
            modifier = Modifier.weight(1f),
            singleLine = true
        )
        OutlinedTextField(
            value = endText,
            onValueChange = { endText = it; onChange(parseMinute(startText), parseMinute(it)) },
            label = { Text("تا (HH:MM)") },
            modifier = Modifier.weight(1f),
            singleLine = true
        )
    }
}

private fun parseMinute(text: String): Int? {
    val normalized = PersianDigits.toLatin(text.trim())
    val parts = normalized.split(":")
    if (parts.size != 2) return null
    val h = parts[0].toIntOrNull() ?: return null
    val m = parts[1].toIntOrNull() ?: return null
    if (h !in 0..23 || m !in 0..59) return null
    return h * 60 + m
}

private fun formatMinute(minute: Int): String {
    val h = minute / 60
    val m = minute % 60
    return "%02d:%02d".format(h, m)
}

@Composable
private fun DateRangeFields(startEpochDay: Long?, endEpochDay: Long?, onChange: (Long?, Long?) -> Unit) {
    var startText by remember {
        mutableStateOf(startEpochDay?.let { PersianDate.formatShort(LocalDate.ofEpochDay(it)) } ?: "")
    }
    var endText by remember {
        mutableStateOf(endEpochDay?.let { PersianDate.formatShort(LocalDate.ofEpochDay(it)) } ?: "")
    }

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = startText,
            onValueChange = {
                startText = it
                onChange(PersianDate.parseJalaliString(it)?.toEpochDay(), PersianDate.parseJalaliString(endText)?.toEpochDay())
            },
            label = { Text(stringResource(R.string.schedule_start_date) + " (۱۴۰۵/۰۷/۰۱)") },
            modifier = Modifier.weight(1f),
            singleLine = true
        )
        OutlinedTextField(
            value = endText,
            onValueChange = {
                endText = it
                onChange(PersianDate.parseJalaliString(startText)?.toEpochDay(), PersianDate.parseJalaliString(it)?.toEpochDay())
            },
            label = { Text(stringResource(R.string.schedule_end_date)) },
            modifier = Modifier.weight(1f),
            singleLine = true
        )
    }
}
