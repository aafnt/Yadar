package com.yadar.app.domain.usecase

import com.yadar.app.domain.model.Sentence
import com.yadar.app.domain.model.SelectionMode
import com.yadar.app.domain.model.WidgetConfig
import com.yadar.app.domain.model.WidgetTouchAction
import com.yadar.app.domain.repository.SentenceRepository
import com.yadar.app.domain.repository.WidgetConfigRepository
import com.yadar.app.domain.selector.SelectionEngine
import java.time.LocalDateTime

/**
 * انتخاب جمله برای یک Refresh معمولی Widget (نه لمس کاربر).
 * تمام تغییرات وضعیت Selection Engine را نیز روی همان Widget ذخیره می‌کند تا
 * رفتار در Restart/Render مجدد کاملاً Deterministic بماند (بند ۲۳).
 */
class RefreshWidgetSentenceUseCase(
    private val widgetConfigRepository: WidgetConfigRepository,
    private val sentenceRepository: SentenceRepository,
    private val selectionEngine: SelectionEngine
) {
    suspend operator fun invoke(widgetId: Int, now: LocalDateTime = LocalDateTime.now()): Sentence? {
        val config = widgetConfigRepository.getById(widgetId) ?: return null
        val pool = sentenceRepository.getActiveEligiblePool(config.collectionId)
        val eligible = selectionEngine.filterEligible(pool, now)

        val shownHistory = if (config.selectionMode == SelectionMode.RANDOM_WITHOUT_REPEAT) {
            widgetConfigRepository.getShownHistory(widgetId)
        } else emptyList()

        val outcome = selectionEngine.selectForRefresh(
            mode = config.selectionMode,
            eligible = eligible,
            now = now,
            currentSentenceId = config.currentSentenceId,
            lastSelectionEpochDay = config.lastSelectionEpochDay,
            sequentialCursorSentenceId = config.sequentialCursorSentenceId,
            shownHistory = shownHistory
        )

        if (outcome.resetHistoryBeforeRecording) {
            widgetConfigRepository.clearHistory(widgetId)
        }
        if (outcome.recordHistory && outcome.sentence != null) {
            val nowMillis = System.currentTimeMillis()
            widgetConfigRepository.recordShown(widgetId, outcome.sentence.id, nowMillis)
            sentenceRepository.markShown(outcome.sentence.id, nowMillis)
        }
        widgetConfigRepository.updateCurrentSelection(widgetId, outcome.newCurrentSentenceId, outcome.newLastSelectionEpochDay)
        if (config.selectionMode == SelectionMode.SEQUENTIAL) {
            widgetConfigRepository.updateSequentialCursor(widgetId, outcome.newSequentialCursorSentenceId)
        }
        return outcome.sentence
    }
}

/** واکنش به لمس Widget طبق تنظیم [WidgetConfig.touchAction] (بند ۲۰ سند پروژه). */
class HandleWidgetTouchUseCase(
    private val widgetConfigRepository: WidgetConfigRepository,
    private val sentenceRepository: SentenceRepository,
    private val selectionEngine: SelectionEngine
) {
    /** @return جمله جدید برای نمایش، یا null اگر عملکرد لمس چیزی را تغییر نمی‌دهد (مثلاً «هیچ کاری» یا «باز کردن برنامه»). */
    suspend operator fun invoke(widgetId: Int, now: LocalDateTime = LocalDateTime.now()): Sentence? {
        val config = widgetConfigRepository.getById(widgetId) ?: return null
        if (config.touchAction != WidgetTouchAction.NEXT_SENTENCE && config.touchAction != WidgetTouchAction.RANDOM_SENTENCE) {
            return null
        }

        val pool = sentenceRepository.getActiveEligiblePool(config.collectionId)
        val eligible = selectionEngine.filterEligible(pool, now)
        val chosen = when (config.touchAction) {
            WidgetTouchAction.NEXT_SENTENCE -> selectionEngine.selectNextForTouch(eligible, config.currentSentenceId)
            WidgetTouchAction.RANDOM_SENTENCE -> selectionEngine.selectRandomForTouch(eligible, config.currentSentenceId)
            else -> null
        } ?: return null

        val nowMillis = System.currentTimeMillis()
        widgetConfigRepository.updateCurrentSelection(widgetId, chosen.id, now.toLocalDate().toEpochDay())
        widgetConfigRepository.updateSequentialCursor(widgetId, chosen.id)
        widgetConfigRepository.recordShown(widgetId, chosen.id, nowMillis)
        sentenceRepository.markShown(chosen.id, nowMillis)
        return chosen
    }
}

class SaveWidgetConfigUseCase(private val repository: WidgetConfigRepository) {
    suspend operator fun invoke(config: WidgetConfig) = repository.save(config)
}

class DeleteWidgetConfigUseCase(private val repository: WidgetConfigRepository) {
    suspend operator fun invoke(widgetId: Int) = repository.delete(widgetId)
}

/** بند ۳۵: «بازنشانی ترتیب جمله‌ها» برای حالت تصادفی بدون تکرار. */
class ClearWidgetHistoryUseCase(private val repository: WidgetConfigRepository) {
    suspend operator fun invoke(widgetId: Int) = repository.clearHistory(widgetId)
}
