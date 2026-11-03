package com.example

import com.example.data.local.entity.Trade
import com.example.domain.calculations.ContractSpecs
import com.example.domain.calculations.TradingCalculations
import com.example.domain.model.StatisticsCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TradingCalculationsTest {

    @Test
    fun testGoldPnlCalculation_Buy() {
        // Gold 1 lot = 100 oz. Entry: 2700, Exit: 2710 -> Diff: $10 * 1 lot * 100 = $1,000 gross
        // Minus commission $5 -> Net $995
        val pnl = TradingCalculations.calculateNetPnl(
            symbol = "XAUUSD",
            category = "GOLD",
            orderType = "BUY",
            lotSize = 1.0,
            entryPrice = 2700.0,
            exitPrice = 2710.0,
            commission = 5.0,
            swap = 0.0
        )
        assertEquals(995.0, pnl, 0.01)
    }

    @Test
    fun testGoldPnlCalculation_Sell() {
        // Gold 0.5 lot. Entry: 2750, Exit: 2740 -> Diff: $10 * 0.5 lot * 100 = $500 gross
        val pnl = TradingCalculations.calculateNetPnl(
            symbol = "XAUUSD",
            category = "GOLD",
            orderType = "SELL",
            lotSize = 0.5,
            entryPrice = 2750.0,
            exitPrice = 2740.0,
            commission = 3.0,
            swap = 0.0
        )
        assertEquals(497.0, pnl, 0.01)
    }

    @Test
    fun testForexPnlCalculation_Buy() {
        // EURUSD 1 lot = 100,000. Entry: 1.0800, Exit: 1.0850 -> Diff: 0.0050 * 100,000 = $500
        val pnl = TradingCalculations.calculateNetPnl(
            symbol = "EURUSD",
            category = "FOREX",
            orderType = "BUY",
            lotSize = 1.0,
            entryPrice = 1.0800,
            exitPrice = 1.0850,
            commission = 7.0,
            swap = 0.0
        )
        assertEquals(493.0, pnl, 0.01)
    }

    @Test
    fun testRequiredMargin() {
        // Gold 1 lot at $2700 with leverage 1:100 -> (100 * 1 * 2700) / 100 = $2700
        val margin = TradingCalculations.calculateRequiredMargin(
            symbol = "XAUUSD",
            category = "GOLD",
            lotSize = 1.0,
            entryPrice = 2700.0,
            leverage = 100
        )
        assertEquals(2700.0, margin, 0.01)
    }

    @Test
    fun testRiskRewardRatio() {
        // BUY: Entry: 100, Stop Loss: 90 (Risk: 10), Take Profit: 120 (Reward: 20) -> 2.0
        val rr = TradingCalculations.calculateRiskReward(
            orderType = "BUY",
            entryPrice = 100.0,
            stopLoss = 90.0,
            takeProfit = 120.0
        )
        assertEquals(2.0, rr!!, 0.01)
    }

    @Test
    fun testStatisticsCalculator() {
        val trades = listOf(
            Trade(
                id = 1,
                accountId = 1,
                symbol = "XAUUSD",
                category = "GOLD",
                orderType = "BUY",
                entryDate = 1000L,
                exitDate = 2000L,
                entryPrice = 2700.0,
                exitPrice = 2720.0,
                lotSize = 1.0,
                profit = 2000.0,
                status = "CLOSED"
            ),
            Trade(
                id = 2,
                accountId = 1,
                symbol = "EURUSD",
                category = "FOREX",
                orderType = "SELL",
                entryDate = 3000L,
                exitDate = 4000L,
                entryPrice = 1.0850,
                exitPrice = 1.0900,
                lotSize = 1.0,
                profit = -500.0,
                status = "CLOSED"
            )
        )

        val stats = StatisticsCalculator.calculate(trades, initialBalance = 10000.0)

        assertEquals(2, stats.totalTradesCount)
        assertEquals(1, stats.winningTradesCount)
        assertEquals(1, stats.losingTradesCount)
        assertEquals(50.0, stats.winRatePercent, 0.01)
        assertEquals(50.0, stats.lossRatePercent, 0.01)
        assertEquals(2000.0, stats.totalProfits, 0.01)
        assertEquals(500.0, stats.totalLosses, 0.01)
        assertEquals(1500.0, stats.netPnl, 0.01)
        assertEquals(4.0, stats.profitFactor, 0.01)
        assertEquals(2000.0, stats.bestTrade, 0.01)
        assertEquals(-500.0, stats.worstTrade, 0.01)
    }
}
