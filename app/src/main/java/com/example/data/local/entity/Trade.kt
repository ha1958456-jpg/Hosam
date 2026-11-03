package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "trades",
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
        Index(value = ["entryDate"]),
        Index(value = ["symbol"])
    ]
)
data class Trade(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val accountId: Long,
    val symbol: String,
    val category: String, // FOREX, GOLD, CRYPTO, INDICES, STOCKS, OTHER
    val orderType: String, // BUY, SELL
    val entryDate: Long, // timestamp millis
    val exitDate: Long? = null,
    val entryPrice: Double,
    val exitPrice: Double? = null,
    val lotSize: Double,
    val leverage: Int = 100,
    val marginUsed: Double = 0.0,
    val stopLoss: Double? = null,
    val takeProfit: Double? = null,
    val spread: Double = 0.0,
    val commission: Double = 0.0,
    val swap: Double = 0.0,
    val profit: Double = 0.0, // Realized or floating net P&L
    val status: String = "CLOSED", // OPEN, CLOSED
    val notes: String = "",
    val beforeChartUri: String? = null,
    val afterChartUri: String? = null,
    val contractSize: Double = 100000.0,
    val riskRewardRatio: Double? = null,
    val isManualPnl: Boolean = false
)
