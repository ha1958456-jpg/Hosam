package com.example.domain.calculations

import kotlin.math.abs
import kotlin.math.roundToInt

object TradingCalculations {

    /**
     * Calculates the raw and net profit or loss based on contract specification.
     */
    fun calculateNetPnl(
        symbol: String,
        category: String,
        orderType: String,
        lotSize: Double,
        entryPrice: Double,
        exitPrice: Double,
        commission: Double = 0.0,
        swap: Double = 0.0,
        contractSizeOverride: Double? = null
    ): Double {
        if (lotSize <= 0.0 || entryPrice <= 0.0 || exitPrice <= 0.0) return 0.0

        val contractSize = contractSizeOverride ?: ContractSpecs.getContractSize(symbol, category)
        val isBuy = orderType.equals("BUY", ignoreCase = true)

        val priceDiff = if (isBuy) (exitPrice - entryPrice) else (entryPrice - exitPrice)
        val grossPnl = priceDiff * lotSize * contractSize

        val netPnl = grossPnl - commission + swap
        return roundToTwoDecimals(netPnl)
    }

    /**
     * Calculates Margin required for a position: (Lot * ContractSize * EntryPrice) / Leverage
     */
    fun calculateRequiredMargin(
        symbol: String,
        category: String,
        lotSize: Double,
        entryPrice: Double,
        leverage: Int = 100,
        contractSizeOverride: Double? = null
    ): Double {
        if (lotSize <= 0.0 || entryPrice <= 0.0 || leverage <= 0) return 0.0
        val contractSize = contractSizeOverride ?: ContractSpecs.getContractSize(symbol, category)
        val notionalValue = lotSize * contractSize * entryPrice
        val margin = notionalValue / leverage.toDouble()
        return roundToTwoDecimals(margin)
    }

    /**
     * Calculates Risk to Reward ratio (R:R).
     */
    fun calculateRiskReward(
        orderType: String,
        entryPrice: Double,
        stopLoss: Double?,
        takeProfit: Double?
    ): Double? {
        if (entryPrice <= 0.0 || stopLoss == null || takeProfit == null) return null
        val isBuy = orderType.equals("BUY", ignoreCase = true)

        val risk = if (isBuy) (entryPrice - stopLoss) else (stopLoss - entryPrice)
        val reward = if (isBuy) (takeProfit - entryPrice) else (entryPrice - takeProfit)

        if (risk <= 0.0 || reward <= 0.0) return null
        val ratio = reward / risk
        return (ratio * 100.0).roundToInt() / 100.0
    }

    fun roundToTwoDecimals(value: Double): Double {
        return (value * 100.0).roundToInt() / 100.0
    }

    fun formatMoney(amount: Double, currency: String = "$"): String {
        val sign = if (amount > 0) "+" else ""
        return "$sign${String.format(java.util.Locale.US, "%,.2f", amount)} $currency"
    }

    fun formatNumber(value: Double, decimals: Int = 2): String {
        return String.format(java.util.Locale.US, "%,.${decimals}f", value)
    }
}
