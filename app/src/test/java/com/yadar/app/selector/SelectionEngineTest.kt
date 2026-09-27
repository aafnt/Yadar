package com.yadar.app.selector

import com.yadar.app.domain.model.DisplayRule
import com.yadar.app.domain.model.Sentence
import com.yadar.app.domain.model.SelectionMode
import com.yadar.app.domain.selector.SelectionEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime
import kotlin.random.Random

private fun sentence(id: Long) = Sentence(id = id, text = "جمله $id", displayRule = DisplayRule.NONE)

class SelectionEngineTest {

    // سناریو ۱ (بند ۵۱): ۱۰ جمله Daily، دو بار Render در همان روز -> باید همان جمله بماند.
    @Test
    fun `daily mode returns the same sentence on repeated refresh same day`() {
        val engine = SelectionEngine(Random(42))
        val eligible = (1..10L).map { sentence(it) }
        val now = LocalDateTime.of(2026, 9, 27, 10, 0)

        val first = engine.selectForRefresh(
            mode = SelectionMode.DAILY,
            eligible = eligible,
            now = now,
            currentSentenceId = null,
            lastSelectionEpochDay = null,
            sequentialCursorSentenceId = null,
            shownHistory = emptyList()
        )
        assertNotNull(first.sentence)

        val second = engine.selectForRefresh(
            mode = SelectionMode.DAILY,
            eligible = eligible,
            now = now.plusHours(2), // همان روز، فقط ساعت دیگر (رندر مجدد)
            currentSentenceId = first.newCurrentSentenceId,
            lastSelectionEpochDay = first.newLastSelectionEpochDay,
            sequentialCursorSentenceId = first.newSequentialCursorSentenceId,
            shownHistory = emptyList()
        )

        assertEquals(first.sentence!!.id, second.sentence!!.id)
    }

    // سناریو ۲: تغییر روز -> جمله جدید (بر اساس epoch day، Deterministic).
    @Test
    fun `daily mode changes sentence on a different day`() {
        val engine = SelectionEngine(Random(42))
        val eligible = (1..10L).map { sentence(it) }
        val day1 = LocalDateTime.of(2026, 9, 27, 10, 0)
        val day2 = LocalDateTime.of(2026, 9, 28, 10, 0)

        val first = engine.selectForRefresh(
            SelectionMode.DAILY, eligible, day1, null, null, null, emptyList()
        )
        val second = engine.selectForRefresh(
            SelectionMode.DAILY, eligible, day2,
            first.newCurrentSentenceId, first.newLastSelectionEpochDay, first.newSequentialCursorSentenceId, emptyList()
        )

        // چون تعداد جمله‌ها ۱۰ است و روزها متوالی‌اند، ایندکس (epochDay % 10) قطعاً فرق می‌کند.
        assertTrue(first.sentence!!.id != second.sentence!!.id)
    }

    // سناریو ۳: Random Without Repeat با ۵ جمله؛ هر پنج باید قبل از تکرار دیده شوند.
    @Test
    fun `random without repeat shows all sentences before repeating`() {
        val engine = SelectionEngine(Random(7))
        val eligible = (1..5L).map { sentence(it) }
        val now = LocalDateTime.of(2026, 9, 27, 10, 0)

        val shown = mutableListOf<Long>()
        var history = listOf<Long>()

        repeat(5) {
            val outcome = engine.selectForRefresh(
                mode = SelectionMode.RANDOM_WITHOUT_REPEAT,
                eligible = eligible,
                now = now,
                currentSentenceId = null,
                lastSelectionEpochDay = null,
                sequentialCursorSentenceId = null,
                shownHistory = history
            )
            val id = outcome.sentence!!.id
            shown.add(id)
            history = history + id
        }

        assertEquals(5, shown.toSet().size) // همه پنج جمله متفاوت‌اند، بدون تکرار در این دور
    }

    // سناریو ۴: دو Widget مستقل با استخر جمله متفاوت؛ فراخوانی روی یکی نباید روی دیگری اثر بگذارد
    // (چون SelectionEngine خالص است و هیچ حالت مشترکی بین دو فراخوانی نگه نمی‌دارد).
    @Test
    fun `two widgets with different pools are fully independent`() {
        val engine = SelectionEngine(Random(1))
        val poolA = (1..3L).map { sentence(it) }
        val poolB = (100..103L).map { sentence(it) }
        val now = LocalDateTime.of(2026, 9, 27, 10, 0)

        val resultA = engine.selectForRefresh(SelectionMode.RANDOM, poolA, now, null, null, null, emptyList())
        val resultB = engine.selectForRefresh(SelectionMode.RANDOM, poolB, now, null, null, null, emptyList())

        assertTrue(resultA.sentence!!.id in 1..3)
        assertTrue(resultB.sentence!!.id in 100..103)
    }

    @Test
    fun `sequential mode wraps around after the last sentence`() {
        val engine = SelectionEngine()
        val eligible = (1..3L).map { sentence(it) }
        val now = LocalDateTime.of(2026, 9, 27, 10, 0)

        var cursor: Long? = null
        val order = mutableListOf<Long>()
        repeat(4) {
            val outcome = engine.selectForRefresh(SelectionMode.SEQUENTIAL, eligible, now, null, null, cursor, emptyList())
            order.add(outcome.sentence!!.id)
            cursor = outcome.newSequentialCursorSentenceId
        }

        assertEquals(listOf(1L, 2L, 3L, 1L), order)
    }
}
