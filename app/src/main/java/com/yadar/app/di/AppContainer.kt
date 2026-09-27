package com.yadar.app.di

import android.content.Context
import com.yadar.app.data.local.database.YadarDatabase
import com.yadar.app.data.local.datastore.SettingsDataStore
import com.yadar.app.data.repository.BackupRepositoryImpl
import com.yadar.app.data.repository.CollectionRepositoryImpl
import com.yadar.app.data.repository.SentenceRepositoryImpl
import com.yadar.app.data.repository.SettingsRepositoryImpl
import com.yadar.app.data.repository.WidgetConfigRepositoryImpl
import com.yadar.app.domain.repository.BackupRepository
import com.yadar.app.domain.repository.CollectionRepository
import com.yadar.app.domain.repository.SentenceRepository
import com.yadar.app.domain.repository.SettingsRepository
import com.yadar.app.domain.repository.WidgetConfigRepository
import com.yadar.app.domain.selector.SelectionEngine
import com.yadar.app.domain.usecase.AddCollectionUseCase
import com.yadar.app.domain.usecase.AddSentenceUseCase
import com.yadar.app.domain.usecase.ClearWidgetHistoryUseCase
import com.yadar.app.domain.usecase.DeleteCollectionUseCase
import com.yadar.app.domain.usecase.DeleteSentenceUseCase
import com.yadar.app.domain.usecase.DeleteWidgetConfigUseCase
import com.yadar.app.domain.usecase.DuplicateSentenceUseCase
import com.yadar.app.domain.usecase.ExportBackupUseCase
import com.yadar.app.domain.usecase.GetHomeSummaryUseCase
import com.yadar.app.domain.usecase.HandleWidgetTouchUseCase
import com.yadar.app.domain.usecase.ImportBackupUseCase
import com.yadar.app.domain.usecase.MoveSentenceToCollectionUseCase
import com.yadar.app.domain.usecase.RefreshWidgetSentenceUseCase
import com.yadar.app.domain.usecase.ReorderSentencesManuallyUseCase
import com.yadar.app.domain.usecase.SaveWidgetConfigUseCase
import com.yadar.app.domain.usecase.SetCollectionActiveUseCase
import com.yadar.app.domain.usecase.ToggleSentenceActiveUseCase
import com.yadar.app.domain.usecase.UpdateCollectionUseCase
import com.yadar.app.domain.usecase.UpdateSentenceUseCase

/**
 * محل مرکزی ساخت وابستگی‌ها (بند ۲ سند پروژه: معماری نباید بیش از حد
 * پیچیده/Enterprise شود). به‌جای Hilt/Dagger، از یک Container دستی و ساده
 * استفاده می‌شود که در [com.yadar.app.YadarApplication] و در Widget/Receiver
 * (که مستقل از Activity اجرا می‌شوند) در دسترس است.
 */
class AppContainer(context: Context) {

    private val database: YadarDatabase = YadarDatabase.getInstance(context)
    private val settingsDataStore = SettingsDataStore(context)

    val sentenceRepository: SentenceRepository =
        SentenceRepositoryImpl(database.sentenceDao(), database.displayRuleDao())

    val collectionRepository: CollectionRepository =
        CollectionRepositoryImpl(database.collectionDao())

    val widgetConfigRepository: WidgetConfigRepository =
        WidgetConfigRepositoryImpl(database.widgetConfigDao(), database.selectionHistoryDao())

    val settingsRepository: SettingsRepository =
        SettingsRepositoryImpl(settingsDataStore)

    val backupRepository: BackupRepository =
        BackupRepositoryImpl(database.sentenceDao(), database.collectionDao(), database.displayRuleDao())

    val selectionEngine: SelectionEngine = SelectionEngine()

    // --- Sentence use cases ---
    val addSentenceUseCase = AddSentenceUseCase(sentenceRepository)
    val updateSentenceUseCase = UpdateSentenceUseCase(sentenceRepository)
    val deleteSentenceUseCase = DeleteSentenceUseCase(sentenceRepository)
    val duplicateSentenceUseCase = DuplicateSentenceUseCase(sentenceRepository)
    val toggleSentenceActiveUseCase = ToggleSentenceActiveUseCase(sentenceRepository)
    val moveSentenceToCollectionUseCase = MoveSentenceToCollectionUseCase(sentenceRepository)
    val reorderSentencesManuallyUseCase = ReorderSentencesManuallyUseCase(sentenceRepository)

    // --- Collection use cases ---
    val addCollectionUseCase = AddCollectionUseCase(collectionRepository)
    val updateCollectionUseCase = UpdateCollectionUseCase(collectionRepository)
    val setCollectionActiveUseCase = SetCollectionActiveUseCase(collectionRepository)
    val deleteCollectionUseCase = DeleteCollectionUseCase(collectionRepository, sentenceRepository)

    // --- Widget use cases ---
    val refreshWidgetSentenceUseCase =
        RefreshWidgetSentenceUseCase(widgetConfigRepository, sentenceRepository, selectionEngine)
    val handleWidgetTouchUseCase =
        HandleWidgetTouchUseCase(widgetConfigRepository, sentenceRepository, selectionEngine)
    val saveWidgetConfigUseCase = SaveWidgetConfigUseCase(widgetConfigRepository)
    val deleteWidgetConfigUseCase = DeleteWidgetConfigUseCase(widgetConfigRepository)
    val clearWidgetHistoryUseCase = ClearWidgetHistoryUseCase(widgetConfigRepository)

    // --- Home ---
    val getHomeSummaryUseCase =
        GetHomeSummaryUseCase(sentenceRepository, collectionRepository, widgetConfigRepository)

    // --- Backup ---
    val exportBackupUseCase = ExportBackupUseCase(backupRepository)
    val importBackupUseCase = ImportBackupUseCase(backupRepository)

    companion object {
        @Volatile
        private var instance: AppContainer? = null

        fun getInstance(context: Context): AppContainer =
            instance ?: synchronized(this) {
                instance ?: AppContainer(context.applicationContext).also { instance = it }
            }
    }
}
