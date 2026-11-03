package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trading_accounts")
data class TradingAccount(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val currency: String = "USD",
    val initialBalance: Double = 10000.0,
    val createdAt: Long = System.currentTimeMillis()
)
