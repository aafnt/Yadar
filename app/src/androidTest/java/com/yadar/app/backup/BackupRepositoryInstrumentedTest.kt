package com.yadar.app.backup

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.yadar.app.data.local.database.YadarDatabase
import com.yadar.app.data.local.entity.CollectionEntity
import com.yadar.app.data.local.entity.SentenceEntity
import com.yadar.app.data.repository.BackupRepositoryImpl
import com.yadar.app.domain.model.BackupConflictStrategy
import com.yadar.app.domain.repository.ImportResult
import com.yadar.app.util.TextNormalizer
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** بند ۵۰: تست Import/Export با JSON معتبر، روی یک دیتابیس واقعی In-Memory. */
@RunWith(AndroidJUnit4::class)
class BackupRepositoryInstrumentedTest {

    private lateinit var db: YadarDatabase
    private lateinit var repository: BackupRepositoryImpl

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(context, YadarDatabase::class.java).build()
        repository = BackupRepositoryImpl(db.sentenceDao(), db.collectionDao(), db.displayRuleDao())
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun exportThenImportWithReplaceRestoresData() = runTest {
        val collectionId = db.collectionDao().insert(
            CollectionEntity(name = "دعاها", createdAt = System.currentTimeMillis())
        )
        db.sentenceDao().insert(
            SentenceEntity(
                text = "رَبِّ لَا تَذَرْنِي فَرْدًا",
                normalizedText = TextNormalizer.forSearch("رَبِّ لَا تَذَرْنِي فَرْدًا"),
                collectionId = collectionId,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
        )

        val exported = repository.exportToJson()

        // شبیه‌سازی نصب تازه: همه‌چیز پاک شود، سپس از همان JSON بازیابی شود.
        db.sentenceDao().deleteAll()
        db.collectionDao().deleteAll()

        val result = repository.importFromJson(exported, BackupConflictStrategy.REPLACE)
        assertTrue(result is ImportResult.Success)
        result as ImportResult.Success
        assertEquals(1, result.sentencesImported)
        assertEquals(1, result.collectionsImported)

        val restoredSentences = db.sentenceDao().getAllSnapshot()
        assertEquals(1, restoredSentences.size)
        assertEquals("رَبِّ لَا تَذَرْنِي فَرْدًا", restoredSentences.first().text)
    }
}
