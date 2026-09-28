package com.yadar.app.data.repository

import com.yadar.app.data.local.dao.CollectionDao
import com.yadar.app.data.local.dao.DisplayRuleDao
import com.yadar.app.data.local.dao.SentenceDao
import com.yadar.app.data.local.entity.CollectionEntity
import com.yadar.app.data.local.entity.DisplayRuleEntity
import com.yadar.app.domain.model.DisplayRuleType
import com.yadar.app.data.local.entity.SentenceEntity
import com.yadar.app.domain.model.BackupConflictStrategy
import com.yadar.app.domain.repository.BackupRepository
import com.yadar.app.domain.repository.ImportResult
import com.yadar.app.util.TextNormalizer
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

/**
 * Export/Import کامل داده‌های کاربر به یک فایل JSON (بند ۲۹ و ۳۰ سند پروژه).
 * از org.json (بخشی از Android SDK) استفاده می‌شود تا نیازی به وابستگی اضافه
 * (مثل kotlinx.serialization + پلاگین آن) نباشد؛ تصمیم فنی مستندشده طبق بند ۵۸.
 */
class BackupRepositoryImpl(
    private val sentenceDao: SentenceDao,
    private val collectionDao: CollectionDao,
    private val displayRuleDao: DisplayRuleDao
) : BackupRepository {

    override suspend fun exportToJson(): String {
        val root = JSONObject()
        root.put("version", CURRENT_BACKUP_VERSION)

        val collections = JSONArray()
        collectionDao.observeAllSnapshot().forEach { c ->
            collections.put(
                JSONObject().apply {
                    put("id", c.id)
                    put("name", c.name)
                    put("description", c.description ?: JSONObject.NULL)
                    put("isActive", c.isActive)
                    put("createdAt", c.createdAt)
                }
            )
        }
        root.put("collections", collections)

        val allSentences = sentenceDao.getAllSnapshot()
        val rules = displayRuleDao.getForSentences(allSentences.map { it.id }).associateBy { it.sentenceId }

        val sentencesJson = JSONArray()
        val rulesJson = JSONArray()
        allSentences.forEach { s ->
            sentencesJson.put(
                JSONObject().apply {
                    put("id", s.id)
                    put("text", s.text)
                    put("collectionId", s.collectionId ?: JSONObject.NULL)
                    put("isActive", s.isActive)
                    put("createdAt", s.createdAt)
                    put("updatedAt", s.updatedAt)
                    put("manualSortOrder", s.manualSortOrder)
                }
            )
            rules[s.id]?.let { r ->
                rulesJson.put(
                    JSONObject().apply {
                        put("sentenceId", r.sentenceId)
                        put("type", r.type.name)
                        put("weekdays", r.weekdays ?: JSONObject.NULL)
                        put("timeStartMinute", r.timeStartMinute ?: JSONObject.NULL)
                        put("timeEndMinute", r.timeEndMinute ?: JSONObject.NULL)
                        put("startDateEpochDay", r.startDateEpochDay ?: JSONObject.NULL)
                        put("endDateEpochDay", r.endDateEpochDay ?: JSONObject.NULL)
                    }
                )
            }
        }
        root.put("sentences", sentencesJson)
        root.put("rules", rulesJson)
        root.put("settings", JSONArray()) // تنظیمات UI عمومی فعلاً بخشی از پشتیبان جمله‌ها نیست

        return root.toString(2)
    }

    override suspend fun importFromJson(json: String, strategy: BackupConflictStrategy): ImportResult {
        val root = try {
            JSONObject(json)
        } catch (e: JSONException) {
            return ImportResult.InvalidJson(e.message ?: "invalid_json")
        }

        if (!root.has("version") || !root.has("collections") || !root.has("sentences")) {
            return ImportResult.InvalidJson("missing_required_fields")
        }

        val version = root.optInt("version", -1)
        if (version != CURRENT_BACKUP_VERSION) {
            return ImportResult.UnknownVersion(version)
        }

        return try {
            if (strategy == BackupConflictStrategy.REPLACE) {
                displayRuleDao.deleteAll()
                sentenceDao.deleteAll()
                collectionDao.deleteAll()
            }

            val collectionIdMap = HashMap<Long, Long>()
            val collectionsArray = root.getJSONArray("collections")
            for (i in 0 until collectionsArray.length()) {
                val obj = collectionsArray.getJSONObject(i)
                val oldId = obj.getLong("id")
                val newId = collectionDao.insert(
                    CollectionEntity(
                        name = obj.getString("name"),
                        description = obj.optString("description", null).takeIf { obj.has("description") && !obj.isNull("description") },
                        isActive = obj.optBoolean("isActive", true),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                    )
                )
                collectionIdMap[oldId] = newId
            }

            val rulesArray = root.optJSONArray("rules") ?: JSONArray()
            val rulesBySentenceId = HashMap<Long, JSONObject>()
            for (i in 0 until rulesArray.length()) {
                val obj = rulesArray.getJSONObject(i)
                rulesBySentenceId[obj.getLong("sentenceId")] = obj
            }

            val sentencesArray = root.getJSONArray("sentences")
            var importedSentences = 0
            for (i in 0 until sentencesArray.length()) {
                val obj = sentencesArray.getJSONObject(i)
                val oldId = obj.getLong("id")
                val oldCollectionId = if (obj.isNull("collectionId")) null else obj.getLong("collectionId")
                val newCollectionId = oldCollectionId?.let { collectionIdMap[it] }
                val text = obj.getString("text")

                val newSentenceId = sentenceDao.insert(
                    SentenceEntity(
                        text = text,
                        normalizedText = TextNormalizer.forSearch(text),
                        collectionId = newCollectionId,
                        isActive = obj.optBoolean("isActive", true),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                        updatedAt = obj.optLong("updatedAt", System.currentTimeMillis()),
                        manualSortOrder = obj.optLong("manualSortOrder", 0)
                    )
                )
                importedSentences += 1

                rulesBySentenceId[oldId]?.let { r ->
                    displayRuleDao.upsert(
                        DisplayRuleEntity(
                            sentenceId = newSentenceId,
                            type = runCatching { DisplayRuleType.valueOf(r.getString("type")) }.getOrDefault(DisplayRuleType.ON_CHANGE),
                            weekdays = if (r.isNull("weekdays")) null else r.getString("weekdays"),
                            timeStartMinute = if (r.isNull("timeStartMinute")) null else r.getInt("timeStartMinute"),
                            timeEndMinute = if (r.isNull("timeEndMinute")) null else r.getInt("timeEndMinute"),
                            startDateEpochDay = if (r.isNull("startDateEpochDay")) null else r.getLong("startDateEpochDay"),
                            endDateEpochDay = if (r.isNull("endDateEpochDay")) null else r.getLong("endDateEpochDay")
                        )
                    )
                }
            }

            ImportResult.Success(sentencesImported = importedSentences, collectionsImported = collectionIdMap.size)
        } catch (e: JSONException) {
            ImportResult.InvalidJson(e.message ?: "malformed_json")
        }
    }

    companion object {
        const val CURRENT_BACKUP_VERSION = 1
    }
}
