package com.yadar.app.domain.usecase

import com.yadar.app.domain.model.Sentence
import com.yadar.app.domain.repository.CollectionRepository
import com.yadar.app.domain.repository.SentenceRepository
import com.yadar.app.domain.repository.WidgetConfigRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

data class HomeSummary(
    val activeSentenceCount: Int,
    val collectionCount: Int,
    val widgetCount: Int,
    val previewSentence: Sentence?
)

/** خلاصه صفحه خانه (بند ۷ سند پروژه). */
class GetHomeSummaryUseCase(
    private val sentenceRepository: SentenceRepository,
    private val collectionRepository: CollectionRepository,
    private val widgetConfigRepository: WidgetConfigRepository
) {
    operator fun invoke(): Flow<HomeSummary> = combine(
        sentenceRepository.observeActiveCount(),
        collectionRepository.observeCount(),
        widgetConfigRepository.observeAll()
    ) { activeCount, collectionCount, widgets ->
        val mostRecentWidget = widgets.maxByOrNull { it.createdAt }
        val previewSentence = mostRecentWidget?.currentSentenceId?.let { sentenceRepository.getById(it) }
        HomeSummary(
            activeSentenceCount = activeCount,
            collectionCount = collectionCount,
            widgetCount = widgets.size,
            previewSentence = previewSentence
        )
    }
}
