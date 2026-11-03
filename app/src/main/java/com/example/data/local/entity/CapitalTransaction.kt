package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "capital_transactions",
    foreignKeys = [
        ForeignKey(
            entity = TradingAccount::class,
            parentColumns = ["id"],
            childColumns = ["accountId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["accountId"]),
        Index(value = ["date"])
    ]
)
data class CapitalTransaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val accountId: Long,
    val type: String, // DEPOSIT, WITHDRAWAL
    val amount: Double,
    val date: Long = System.currentTimeMillis(),
    val notes: String = ""
)
