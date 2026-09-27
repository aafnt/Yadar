package com.yadar.app.backup

import com.yadar.app.data.repository.BackupRepositoryImpl
import com.yadar.app.domain.model.BackupConflictStrategy
import com.yadar.app.domain.repository.ImportResult
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test

class BackupRepositoryImplTest {

    private val repository = BackupRepositoryImpl(FakeSentenceDao(), FakeCollectionDao(), FakeDisplayRuleDao())

    @Test
    fun `malformed json is rejected without touching the database`() = runTest {
        val result = repository.importFromJson("{ این یک جیسون خراب است", BackupConflictStrategy.REPLACE)
        assertTrue(result is ImportResult.InvalidJson)
    }

    @Test
    fun `json missing required fields is rejected`() = runTest {
        val result = repository.importFromJson("""{"foo": "bar"}""", BackupConflictStrategy.REPLACE)
        assertTrue(result is ImportResult.InvalidJson)
    }

    @Test
    fun `unknown backup version is rejected`() = runTest {
        val json = """{"version": 999, "collections": [], "sentences": []}"""
        val result = repository.importFromJson(json, BackupConflictStrategy.REPLACE)
        assertTrue(result is ImportResult.UnknownVersion)
        assertTrue((result as ImportResult.UnknownVersion).version == 999)
    }
}
