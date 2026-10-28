package com.example.data.repository

import com.example.data.dao.AppSettingDao
import com.example.data.dao.TransactionDao
import com.example.data.entity.AppSettingEntity
import com.example.data.entity.TransactionEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class TransactionRepository(
    private val transactionDao: TransactionDao,
    private val appSettingDao: AppSettingDao
) {
    val allTransactions: Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()

    suspend fun insertTransaction(transaction: TransactionEntity): Long = withContext(Dispatchers.IO) {
        transactionDao.insertTransaction(transaction)
    }

    suspend fun updateTransaction(transaction: TransactionEntity) = withContext(Dispatchers.IO) {
        transactionDao.updateTransaction(transaction)
    }

    suspend fun deleteTransaction(transaction: TransactionEntity) = withContext(Dispatchers.IO) {
        transactionDao.deleteTransaction(transaction)
    }

    suspend fun deleteTransactionById(id: Long) = withContext(Dispatchers.IO) {
        transactionDao.deleteTransactionById(id)
    }

    suspend fun deleteAllData() = withContext(Dispatchers.IO) {
        transactionDao.deleteAllTransactions()
        appSettingDao.deleteAllSettings()
    }

    fun getSettingFlow(key: String, defaultValue: String): Flow<String> {
        return appSettingDao.getSettingFlow(key).map { it?.value ?: defaultValue }
    }

    suspend fun getSettingValue(key: String, defaultValue: String): String = withContext(Dispatchers.IO) {
        appSettingDao.getSettingValue(key) ?: defaultValue
    }

    suspend fun setSetting(key: String, value: String) = withContext(Dispatchers.IO) {
        appSettingDao.setSetting(AppSettingEntity(key = key, value = value))
    }

    /**
     * Exports all transactions and settings into a JSON string for local backup.
     */
    suspend fun exportJson(): String = withContext(Dispatchers.IO) {
        val root = JSONObject()
        root.put("version", 1)
        root.put("exportedAt", System.currentTimeMillis())

        val transactions = transactionDao.getAllTransactionsList()
        val txArray = JSONArray()
        for (tx in transactions) {
            val obj = JSONObject().apply {
                put("id", tx.id)
                put("amountMinor", tx.amountMinor)
                put("type", tx.type)
                put("categoryKey", tx.categoryKey)
                put("description", tx.description)
                put("dateTimestamp", tx.dateTimestamp)
                put("currencyCode", tx.currencyCode)
            }
            txArray.put(obj)
        }
        root.put("transactions", txArray)

        val settings = appSettingDao.getAllSettings()
        val settingsArray = JSONArray()
        for (s in settings) {
            val obj = JSONObject().apply {
                put("key", s.key)
                put("value", s.value)
            }
            settingsArray.put(obj)
        }
        root.put("settings", settingsArray)

        root.toString(2)
    }

    /**
     * Imports transactions and settings from a JSON string backup.
     * Prevents duplicate transactions and handles empty or malformed files safely.
     * Returns the count of newly restored transactions.
     */
    suspend fun importJson(jsonContent: String): Result<Int> = withContext(Dispatchers.IO) {
        try {
            if (jsonContent.isBlank()) {
                return@withContext Result.failure(IllegalArgumentException("Backup file is empty"))
            }

            val root = JSONObject(jsonContent)
            if (!root.has("transactions") && !root.has("settings")) {
                return@withContext Result.failure(IllegalArgumentException("Invalid backup format: missing transactions or settings"))
            }

            val txArray = root.optJSONArray("transactions") ?: JSONArray()
            val existingTxs = transactionDao.getAllTransactionsList()
            val existingSignatures = existingTxs.map {
                "${it.dateTimestamp}_${it.amountMinor}_${it.type}_${it.categoryKey}_${it.currencyCode}_${it.description.trim()}"
            }.toHashSet()

            val restoredList = mutableListOf<TransactionEntity>()
            for (i in 0 until txArray.length()) {
                val obj = txArray.getJSONObject(i)
                val dateTimestamp = obj.getLong("dateTimestamp")
                val amountMinor = obj.getLong("amountMinor")
                val type = obj.getString("type")
                val categoryKey = obj.getString("categoryKey")
                val description = obj.optString("description", "").trim()
                val currencyCode = obj.optString("currencyCode", "USD")

                val signature = "${dateTimestamp}_${amountMinor}_${type}_${categoryKey}_${currencyCode}_${description}"
                if (!existingSignatures.contains(signature)) {
                    restoredList.add(
                        TransactionEntity(
                            id = 0L, // Auto-generate new IDs
                            amountMinor = amountMinor,
                            type = type,
                            categoryKey = categoryKey,
                            description = description,
                            dateTimestamp = dateTimestamp,
                            currencyCode = currencyCode
                        )
                    )
                    existingSignatures.add(signature)
                }
            }

            if (restoredList.isNotEmpty()) {
                transactionDao.insertAll(restoredList)
            }

            val settingsArray = root.optJSONArray("settings")
            if (settingsArray != null) {
                val settingsList = mutableListOf<AppSettingEntity>()
                for (i in 0 until settingsArray.length()) {
                    val obj = settingsArray.getJSONObject(i)
                    settingsList.add(
                        AppSettingEntity(
                            key = obj.getString("key"),
                            value = obj.getString("value")
                        )
                    )
                }
                if (settingsList.isNotEmpty()) {
                    appSettingDao.insertAll(settingsList)
                }
            }

            Result.success(restoredList.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
