package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val amountMinor: Long,
    val type: String, // "INCOME" or "EXPENSE"
    val categoryKey: String,
    val description: String,
    val dateTimestamp: Long,
    val currencyCode: String // "USD" or "SOS"
)
