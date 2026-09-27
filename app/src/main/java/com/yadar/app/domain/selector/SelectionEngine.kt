package com.yadar.app.domain.selector

import com.yadar.app.domain.model.Sentence
import com.yadar.app.domain.model.SelectionMode
import java.time.LocalDateTime
import kotlin.random.Random

/**
 * موتور انتخاب جمله (بند ۱۱ و ۱۲ سند پروژه).
 *
 * این کلاس کاملاً در Domain Layer است و به هیچ UI یا Widget API‌ای وابسته نیست؛
 * هیچ UI مجاز نیست مستقیماً این منطق را دوباره پیاده‌سازی کند (بند ۱۱).
 *
 * [random] برای تست‌پذیری تزریق می‌شود (بند ۵۰: تست‌های Selection Engine).
 */
class SelectionEngine(private val random: Random = Random.Default) {

    /** خروجی مرحله فیلتر: تمام جمله‌ها -> جمله‌های فعال -> فیلتر مجموعه (بیرون از این کلاس) -> این متد. */
    fun filterEligible(pool: List<Sentence>, now: LocalDateTime): List<Sentence> {
        val minuteOfDay = now.hour * 60 + now.minute
        return pool
            .filter { RuleEvaluator.isEligible(it.displayRule, now.toLocalDate(), minuteOfDay) }
            .sortedWith(compareBy({ it.manualSortOrder }, { it.id }))
    }

    data class SelectionOutcome(
        val sentence: Sentence?,
        val newCurrentSentenceId: Long?,
        val newLastSelectionEpochDay: Long?,
        val newSequentialCursorSentenceId: Long?,
        val recordHistory: Boolean,
        val resetHistoryBeforeRecording: Boolean
    )

    /**
     * انتخاب برای یک Refresh معمولی Widget (نه Touch کاربر).
     * وضعیت فعلی Widget را می‌گیرد و بر اساس [mode] تصمیم می‌گیرد که همان جمله
     * قبلی نگه داشته شود یا جمله تازه‌ای انتخاب شود (بند ۲۳ و ۲۴: Deterministic بودن).
     */
    fun selectForRefresh(
        mode: SelectionMode,
        eligible: List<Sentence>,
        now: LocalDateTime,
        currentSentenceId: Long?,
        lastSelectionEpochDay: Long?,
        sequentialCursorSentenceId: Long?,
        shownHistory: List<Long>
    ): SelectionOutcome {
        if (eligible.isEmpty()) {
            return SelectionOutcome(null, null, lastSelectionEpochDay, sequentialCursorSentenceId, false, false)
        }
        val todayEpochDay = now.toLocalDate().toEpochDay()

        return when (mode) {
            SelectionMode.DAILY -> {
                val current = eligible.firstOrNull { it.id == currentSentenceId }
                if (lastSelectionEpochDay == todayEpochDay && current != null) {
                    // همان روز است: رندر مجدد یا Restart نباید جمله را عوض کند.
                    SelectionOutcome(current, current.id, lastSelectionEpochDay, sequentialCursorSentenceId, false, false)
                } else {
                    val index = Math.floorMod(todayEpochDay, eligible.size.toLong()).toInt()
                    val chosen = eligible[index]
                    SelectionOutcome(chosen, chosen.id, todayEpochDay, sequentialCursorSentenceId, true, false)
                }
            }

            SelectionMode.SEQUENTIAL -> {
                val next = pickNextSequential(eligible, sequentialCursorSentenceId)
                SelectionOutcome(next, next.id, lastSelectionEpochDay, next.id, true, false)
            }

            SelectionMode.RANDOM -> {
                val chosen = eligible[random.nextInt(eligible.size)]
                SelectionOutcome(chosen, chosen.id, lastSelectionEpochDay, sequentialCursorSentenceId, true, false)
            }

            SelectionMode.RANDOM_WITHOUT_REPEAT -> {
                val remaining = eligible.filterNot { it.id in shownHistory }
                val resetNeeded = remaining.isEmpty()
                val pool = if (resetNeeded) eligible else remaining
                val chosen = pool[random.nextInt(pool.size)]
                SelectionOutcome(chosen, chosen.id, lastSelectionEpochDay, sequentialCursorSentenceId, true, resetNeeded)
            }

            SelectionMode.ON_CHANGE -> {
                // بدون حافظه: هر بار Refresh یک انتخاب تازه، ولی چیزی در Room برای این حالت persist نمی‌شود.
                val chosen = eligible[random.nextInt(eligible.size)]
                SelectionOutcome(chosen, chosen.id, lastSelectionEpochDay, sequentialCursorSentenceId, false, false)
            }
        }
    }

    /** لمس Widget با عملکرد «جمله بعدی» (بند ۲۰)، فارغ از Selection Mode فعلی. */
    fun selectNextForTouch(eligible: List<Sentence>, currentSentenceId: Long?): Sentence? {
        if (eligible.isEmpty()) return null
        return pickNextSequential(eligible, currentSentenceId)
    }

    /** لمس Widget با عملکرد «جمله تصادفی» (بند ۲۰). */
    fun selectRandomForTouch(eligible: List<Sentence>, excludeId: Long?): Sentence? {
        if (eligible.isEmpty()) return null
        val pool = if (eligible.size > 1 && excludeId != null) {
            eligible.filterNot { it.id == excludeId }
        } else eligible
        return pool[random.nextInt(pool.size)]
    }

    private fun pickNextSequential(eligible: List<Sentence>, cursorId: Long?): Sentence {
        if (cursorId == null) return eligible.first()
        val index = eligible.indexOfFirst { it.id == cursorId }
        if (index == -1) return eligible.first()
        val nextIndex = (index + 1) % eligible.size
        return eligible[nextIndex]
    }
}
