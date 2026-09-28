package com.yadar.app.data.repository

import com.yadar.app.data.local.entity.CollectionEntity
import com.yadar.app.data.local.entity.DisplayRuleEntity
import com.yadar.app.domain.model.DisplayRuleType
import com.yadar.app.data.local.entity.SentenceEntity
import com.yadar.app.data.local.entity.WidgetConfigEntity
import com.yadar.app.domain.model.Collection
import com.yadar.app.domain.model.DisplayRule
import com.yadar.app.domain.model.Sentence
import com.yadar.app.domain.model.WidgetConfig
import java.time.DayOfWeek

fun CollectionEntity.toDomain() = Collection(
    id = id,
    name = name,
    description = description,
    isActive = isActive,
    createdAt = createdAt
)

fun Collection.toEntity() = CollectionEntity(
    id = id,
    name = name,
    description = description,
    isActive = isActive,
    createdAt = createdAt
)

fun DisplayRuleEntity?.toDomain(): DisplayRule {
    if (this == null) return DisplayRule.NONE
    val days = weekdays?.split(",")
        ?.mapNotNull { runCatching { DayOfWeek.valueOf(it) }.getOrNull() }
        ?.toSet()
        ?.takeIf { it.isNotEmpty() }
    return DisplayRule(
        type = type,
        weekdays = days,
        timeStartMinute = timeStartMinute,
        timeEndMinute = timeEndMinute,
        startDateEpochDay = startDateEpochDay,
        endDateEpochDay = endDateEpochDay
    )
}

fun DisplayRule.toEntity(sentenceId: Long, existingId: Long = 0) = DisplayRuleEntity(
    id = existingId,
    sentenceId = sentenceId,
    type = type,
    weekdays = weekdays?.takeIf { it.isNotEmpty() }?.joinToString(",") { it.name },
    timeStartMinute = timeStartMinute,
    timeEndMinute = timeEndMinute,
    startDateEpochDay = startDateEpochDay,
    endDateEpochDay = endDateEpochDay
)

fun SentenceEntity.toDomain(rule: DisplayRuleEntity?) = Sentence(
    id = id,
    text = text,
    collectionId = collectionId,
    isActive = isActive,
    createdAt = createdAt,
    updatedAt = updatedAt,
    manualSortOrder = manualSortOrder,
    lastShownAt = lastShownAt,
    displayRule = rule.toDomain()
)

fun Sentence.toEntity(normalizedText: String) = SentenceEntity(
    id = id,
    text = text,
    normalizedText = normalizedText,
    collectionId = collectionId,
    isActive = isActive,
    createdAt = createdAt,
    updatedAt = updatedAt,
    manualSortOrder = manualSortOrder,
    lastShownAt = lastShownAt
)

fun WidgetConfigEntity.toDomain() = WidgetConfig(
    widgetId = widgetId,
    collectionId = collectionId,
    selectionMode = selectionMode,
    font = font,
    fontSizeSp = fontSizeSp,
    fontWeight = fontWeight,
    fontColorArgb = fontColorArgb,
    opacityPercent = opacityPercent,
    textAlignment = textAlignment,
    lineSpacingMultiplier = lineSpacingMultiplier,
    maxLines = maxLines,
    backgroundMode = backgroundMode,
    backgroundColorArgb = backgroundColorArgb,
    backgroundAlphaPercent = backgroundAlphaPercent,
    showDate = showDate,
    dateDisplay = dateDisplay,
    touchAction = touchAction,
    currentSentenceId = currentSentenceId,
    lastSelectionEpochDay = lastSelectionEpochDay,
    sequentialCursorSentenceId = sequentialCursorSentenceId,
    createdAt = createdAt
)

fun WidgetConfig.toEntity() = WidgetConfigEntity(
    widgetId = widgetId,
    collectionId = collectionId,
    selectionMode = selectionMode,
    font = font,
    fontSizeSp = fontSizeSp,
    fontWeight = fontWeight,
    fontColorArgb = fontColorArgb,
    opacityPercent = opacityPercent,
    textAlignment = textAlignment,
    lineSpacingMultiplier = lineSpacingMultiplier,
    maxLines = maxLines,
    backgroundMode = backgroundMode,
    backgroundColorArgb = backgroundColorArgb,
    backgroundAlphaPercent = backgroundAlphaPercent,
    showDate = showDate,
    dateDisplay = dateDisplay,
    touchAction = touchAction,
    currentSentenceId = currentSentenceId,
    lastSelectionEpochDay = lastSelectionEpochDay,
    sequentialCursorSentenceId = sequentialCursorSentenceId,
    createdAt = createdAt
)
