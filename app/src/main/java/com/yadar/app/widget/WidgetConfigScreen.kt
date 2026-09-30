@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.yadar.app.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yadar.app.R
import com.yadar.app.domain.model.SelectionMode
import com.yadar.app.domain.model.WidgetBackgroundMode
import com.yadar.app.domain.model.WidgetConfig
import com.yadar.app.domain.model.WidgetDateDisplay
import com.yadar.app.domain.model.WidgetTextAlignment
import com.yadar.app.domain.model.WidgetTouchAction
import com.yadar.app.domain.model.FontChoice
import com.yadar.app.ui.theme.PickerColors
import com.yadar.app.ui.theme.fontFamilyFor
import com.yadar.app.util.PersianDate
import com.yadar.app.util.PersianDigits
import kotlinx.coroutines.launch
import java.time.LocalDate

private val fontSizePresets = listOf(12, 14, 16, 18, 20, 24, 28, 32, 36, 40)
private val fontWeightPresets = listOf(100, 200, 300, 400, 500, 600, 700, 800, 900)

@Composable
fun WidgetConfigScreen(viewModel: WidgetConfigViewModel, onSave: suspend () -> Unit) {
    val config by viewModel.config.collectAsState()
    val collections by viewModel.collections.collectAsState()
    val showWallpaper by viewModel.showWallpaperPreview.collectAsState()
    val scope = androidx.compose.runtime.rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize()) {
        // پیش‌نمایش زنده (بند ۵۳ و ۵۴): هر تغییری بلافاصله اینجا دیده می‌شود.
        WidgetLivePreview(config = config, showWallpaper = showWallpaper)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResourceCompat(R.string.widget_config_show_on_wallpaper), modifier = Modifier.weight(1f))
                Switch(checked = showWallpaper, onCheckedChange = { viewModel.showWallpaperPreview.value = it })
            }

            SectionLabel(R.string.widget_config_collection)
            CollectionPicker(collections, config.collectionId) { id -> viewModel.update { it.copy(collectionId = id) } }

            SectionLabel(R.string.widget_config_selection_mode)
            SelectionModePicker(config.selectionMode) { mode -> viewModel.update { it.copy(selectionMode = mode) } }

            SectionLabel(R.string.widget_config_font)
            FontPicker(config.font) { font -> viewModel.update { it.copy(font = font) } }

            SectionLabel(R.string.widget_config_font_size)
            PresetDropdown(fontSizePresets, config.fontSizeSp) { size -> viewModel.update { it.copy(fontSizeSp = size) } }

            SectionLabel(R.string.widget_config_font_weight)
            PresetDropdown(fontWeightPresets, config.fontWeight) { w -> viewModel.update { it.copy(fontWeight = w) } }

            SectionLabel(R.string.widget_config_font_color)
            ColorPicker(config.fontColorArgb) { argb -> viewModel.update { it.copy(fontColorArgb = argb) } }

            SectionLabel(R.string.widget_config_opacity)
            SliderRow(value = config.opacityPercent.toFloat(), range = 0f..100f) {
                viewModel.update { c -> c.copy(opacityPercent = it.toInt()) }
            }

            SectionLabel(R.string.widget_config_alignment)
            AlignmentPicker(config.textAlignment) { a -> viewModel.update { it.copy(textAlignment = a) } }

            SectionLabel(R.string.widget_config_line_spacing)
            SliderRow(value = config.lineSpacingMultiplier, range = 0.8f..2.0f) {
                viewModel.update { c -> c.copy(lineSpacingMultiplier = it) }
            }

            SectionLabel(R.string.widget_config_max_lines)
            PresetDropdown(listOf(1, 2, 3, 4, 5), config.maxLines) { m -> viewModel.update { it.copy(maxLines = m) } }

            SectionLabel(R.string.widget_config_background)
            BackgroundModePicker(config) { updated -> viewModel.update { updated } }

            SectionLabel(R.string.widget_config_show_date)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(checked = config.showDate, onCheckedChange = { checked ->
                    viewModel.update {
                        it.copy(
                            showDate = checked,
                            dateDisplay = if (checked) WidgetDateDisplay.SHORT else WidgetDateDisplay.OFF
                        )
                    }
                })
                if (config.showDate) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(start = 12.dp)) {
                        FilterChip(
                            selected = config.dateDisplay == WidgetDateDisplay.SHORT,
                            onClick = { viewModel.update { it.copy(dateDisplay = WidgetDateDisplay.SHORT) } },
                            label = { Text("کوتاه") }
                        )
                        FilterChip(
                            selected = config.dateDisplay == WidgetDateDisplay.SHORT_WITH_WEEKDAY,
                            onClick = { viewModel.update { it.copy(dateDisplay = WidgetDateDisplay.SHORT_WITH_WEEKDAY) } },
                            label = { Text("با روز هفته") }
                        )
                    }
                }
            }

            SectionLabel(R.string.widget_config_show_meaning)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(
                    checked = config.showMeaning,
                    onCheckedChange = { checked -> viewModel.update { it.copy(showMeaning = checked) } }
                )
                Text(
                    "اگر جمله معنی نداشته باشد، فقط خود جمله نمایش داده می‌شود.",
                    modifier = Modifier.padding(start = 12.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            if (config.showMeaning) {
                SectionLabel(R.string.widget_config_meaning_font)
                FontPicker(config.meaningFont) { font -> viewModel.update { it.copy(meaningFont = font) } }

                SectionLabel(R.string.widget_config_meaning_font_size)
                PresetDropdown(fontSizePresets, config.meaningFontSizeSp) { size ->
                    viewModel.update { it.copy(meaningFontSizeSp = size) }
                }
            }

            SectionLabel(R.string.widget_config_touch_action)
            TouchActionPicker(config.touchAction) { t -> viewModel.update { it.copy(touchAction = t) } }

            Button(
                onClick = { scope.launch { viewModel.save(); onSave() } },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResourceCompat(R.string.widget_config_save))
            }
        }
    }
}

@Composable
private fun WidgetLivePreview(config: WidgetConfig, showWallpaper: Boolean) {
    val background = if (showWallpaper) {
        Modifier.background(
            Brush.verticalGradient(listOf(Color(0xFF2B2350), Color(0xFF171335)))
        )
    } else {
        Modifier.background(Color(0xFF0E0E10))
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .then(background),
        contentAlignment = when (config.textAlignment) {
            WidgetTextAlignment.RIGHT -> Alignment.CenterEnd
            WidgetTextAlignment.CENTER -> Alignment.Center
            WidgetTextAlignment.LEFT -> Alignment.CenterStart
        }
    ) {
        val boxBackgroundModifier = when (config.backgroundMode) {
            WidgetBackgroundMode.TRANSPARENT -> Modifier
            WidgetBackgroundMode.SOLID -> Modifier
                .background(Color(config.backgroundColorArgb), RoundedCornerShape(12.dp))
                .padding(12.dp)
            WidgetBackgroundMode.CUSTOM_ALPHA -> Modifier
                .background(
                    Color(config.backgroundColorArgb).copy(alpha = config.backgroundAlphaPercent / 100f),
                    RoundedCornerShape(12.dp)
                )
                .padding(12.dp)
        }
        Column(modifier = boxBackgroundModifier, horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "نمونه جمله برای پیش‌نمایش زنده",
                maxLines = config.maxLines,
                style = TextStyle(
                    color = Color(config.fontColorArgb).copy(alpha = config.opacityPercent / 100f),
                    fontSize = config.fontSizeSp.sp,
                    fontWeight = FontWeight(config.fontWeight.coerceIn(100, 900)),
                    fontFamily = fontFamilyFor(config.font),
                    textAlign = when (config.textAlignment) {
                        WidgetTextAlignment.RIGHT -> TextAlign.Right
                        WidgetTextAlignment.CENTER -> TextAlign.Center
                        WidgetTextAlignment.LEFT -> TextAlign.Left
                    },
                    lineHeight = (config.fontSizeSp * config.lineSpacingMultiplier).sp
                )
            )
            if (config.showMeaning) {
                Text(
                    text = "و معنی جمله اینجا با فونت/اندازه جدا",
                    style = TextStyle(
                        color = Color(config.fontColorArgb).copy(alpha = config.opacityPercent / 100f),
                        fontSize = config.meaningFontSizeSp.sp,
                        fontFamily = fontFamilyFor(config.meaningFont),
                        textAlign = when (config.textAlignment) {
                            WidgetTextAlignment.RIGHT -> TextAlign.Right
                            WidgetTextAlignment.CENTER -> TextAlign.Center
                            WidgetTextAlignment.LEFT -> TextAlign.Left
                        }
                    )
                )
            }
            if (config.showDate && config.dateDisplay != WidgetDateDisplay.OFF) {
                val today = LocalDate.now()
                val text = if (config.dateDisplay == WidgetDateDisplay.SHORT_WITH_WEEKDAY) {
                    PersianDate.formatShortWithWeekday(today)
                } else PersianDate.formatShort(today)
                Text(
                    text = text,
                    style = TextStyle(
                        color = Color(config.fontColorArgb).copy(alpha = (config.opacityPercent * 0.7f) / 100f),
                        fontSize = (config.fontSizeSp * 0.6f).sp,
                        fontFamily = fontFamilyFor(config.font)
                    )
                )
            }
        }
    }
}

@Composable
private fun SectionLabel(res: Int) {
    Text(stringResourceCompat(res), style = MaterialTheme.typography.labelLarge)
}

@Composable
private fun stringResourceCompat(res: Int): String = androidx.compose.ui.res.stringResource(res)

@Composable
private fun CollectionPicker(collections: List<com.yadar.app.domain.model.Collection>, selectedId: Long?, onSelect: (Long?) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val name = collections.firstOrNull { it.id == selectedId }?.name ?: "همه مجموعه‌ها"
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = name, onValueChange = {}, readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor()
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(text = { Text("همه مجموعه‌ها") }, onClick = { onSelect(null); expanded = false })
            collections.forEach { c ->
                DropdownMenuItem(text = { Text(c.name) }, onClick = { onSelect(c.id); expanded = false })
            }
        }
    }
}

@Composable
private fun SelectionModePicker(selected: SelectionMode, onSelect: (SelectionMode) -> Unit) {
    val labels = mapOf(
        SelectionMode.DAILY to R.string.display_mode_daily,
        SelectionMode.ON_CHANGE to R.string.display_mode_on_change,
        SelectionMode.SEQUENTIAL to R.string.display_mode_sequential,
        SelectionMode.RANDOM to R.string.display_mode_random,
        SelectionMode.RANDOM_WITHOUT_REPEAT to R.string.display_mode_random_no_repeat
    )
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = stringResourceCompat(labels.getValue(selected)), onValueChange = {}, readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor()
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            labels.forEach { (mode, res) ->
                DropdownMenuItem(text = { Text(stringResourceCompat(res)) }, onClick = { onSelect(mode); expanded = false })
            }
        }
    }
}

@Composable
private fun FontPicker(selected: FontChoice, onSelect: (FontChoice) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selected.name, onValueChange = {}, readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor()
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            FontChoice.entries.forEach { font ->
                DropdownMenuItem(text = { Text(font.name) }, onClick = { onSelect(font); expanded = false })
            }
        }
    }
}

@Composable
private fun PresetDropdown(values: List<Int>, selected: Int, onSelect: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = PersianDigits.toPersian(selected), onValueChange = {}, readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor()
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            values.forEach { v ->
                DropdownMenuItem(text = { Text(PersianDigits.toPersian(v)) }, onClick = { onSelect(v); expanded = false })
            }
        }
    }
}

@Composable
private fun ColorPicker(selectedArgb: Int, onSelect: (Int) -> Unit) {
    var hex by remember(selectedArgb) { mutableStateOf(String.format("#%06X", selectedArgb and 0xFFFFFF)) }
    Column {
        // چند رنگ پیشنهادی برای انتخاب سریع (بند ۵۵)؛ کاربر به این رنگ‌ها محدود نیست.
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            PickerColors.forEach { (_, color) ->
                val isSelected = color.toArgb() == selectedArgb
                Box(
                    modifier = Modifier
                        .size(if (isSelected) 36.dp else 32.dp)
                        .clip(CircleShape)
                        .background(color)
                        .clickable { onSelect(color.toArgb()) }
                )
            }
        }
        OutlinedTextField(
            value = hex,
            onValueChange = { input ->
                hex = input
                runCatching { android.graphics.Color.parseColor(input) }.getOrNull()?.let(onSelect)
            },
            label = { Text("کد رنگ (Hex/RGB)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun SliderRow(value: Float, range: ClosedFloatingPointRange<Float>, onChange: (Float) -> Unit) {
    Column {
        Slider(value = value, onValueChange = onChange, valueRange = range)
        Text(PersianDigits.toPersian(String.format("%.0f", value)))
    }
}

@Composable
private fun AlignmentPicker(selected: WidgetTextAlignment, onSelect: (WidgetTextAlignment) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(selected == WidgetTextAlignment.RIGHT, { onSelect(WidgetTextAlignment.RIGHT) }, { Text(stringResourceCompat(R.string.widget_config_align_right)) })
        FilterChip(selected == WidgetTextAlignment.CENTER, { onSelect(WidgetTextAlignment.CENTER) }, { Text(stringResourceCompat(R.string.widget_config_align_center)) })
        FilterChip(selected == WidgetTextAlignment.LEFT, { onSelect(WidgetTextAlignment.LEFT) }, { Text(stringResourceCompat(R.string.widget_config_align_left)) })
    }
}

@Composable
private fun BackgroundModePicker(config: WidgetConfig, onChange: (WidgetConfig) -> Unit) {
    Column {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(config.backgroundMode == WidgetBackgroundMode.TRANSPARENT, { onChange(config.copy(backgroundMode = WidgetBackgroundMode.TRANSPARENT)) }, { Text(stringResourceCompat(R.string.widget_config_bg_transparent)) })
            FilterChip(config.backgroundMode == WidgetBackgroundMode.SOLID, { onChange(config.copy(backgroundMode = WidgetBackgroundMode.SOLID)) }, { Text(stringResourceCompat(R.string.widget_config_bg_solid)) })
            FilterChip(config.backgroundMode == WidgetBackgroundMode.CUSTOM_ALPHA, { onChange(config.copy(backgroundMode = WidgetBackgroundMode.CUSTOM_ALPHA)) }, { Text(stringResourceCompat(R.string.widget_config_bg_custom_alpha)) })
        }
        if (config.backgroundMode != WidgetBackgroundMode.TRANSPARENT) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                PickerColors.forEach { (_, color) ->
                    androidx.compose.material3.TextButton(onClick = { onChange(config.copy(backgroundColorArgb = color.toArgb())) }) {
                        Box(modifier = Modifier.size(20.dp).clip(CircleShape).background(color))
                    }
                }
            }
            if (config.backgroundMode == WidgetBackgroundMode.CUSTOM_ALPHA) {
                SliderRow(value = config.backgroundAlphaPercent.toFloat(), range = 0f..100f) {
                    onChange(config.copy(backgroundAlphaPercent = it.toInt()))
                }
            }
        }
    }
}

@Composable
private fun TouchActionPicker(selected: WidgetTouchAction, onSelect: (WidgetTouchAction) -> Unit) {
    val labels = mapOf(
        WidgetTouchAction.NEXT_SENTENCE to R.string.widget_touch_next,
        WidgetTouchAction.RANDOM_SENTENCE to R.string.widget_touch_random,
        WidgetTouchAction.OPEN_APP to R.string.widget_touch_open_app,
        WidgetTouchAction.NONE to R.string.widget_touch_none
    )
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = stringResourceCompat(labels.getValue(selected)), onValueChange = {}, readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor()
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            labels.forEach { (action, res) ->
                DropdownMenuItem(text = { Text(stringResourceCompat(res)) }, onClick = { onSelect(action); expanded = false })
            }
        }
    }
}
