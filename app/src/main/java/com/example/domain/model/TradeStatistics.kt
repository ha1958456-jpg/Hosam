package com.example.domain.model

import com.example.data.local.entity.Trade
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

data class TradeStatistics(
    val totalProfits: Double = 0.0,
    val totalLosses: Double = 0.0,
    val netPnl: Double = 0.0,
    val totalTradesCount: Int = 0,
    val winningTradesCount: Int = 0,
    val losingTradesCount: Int = 0,
    val breakevenTradesCount: Int = 0,
    val winRatePercent: Double = 0.0,
    val lossRatePercent: Double = 0.0,
    val avgProfitPerTrade: Double = 0.0,
    val avgLossPerTrade: Double = 0.0,
    val bestTrade: Double = 0.0,
    val worstTrade: Double = 0.0,
    val totalLots: Double = 0.0,
    val avgRiskReward: Double = 0.0,
    val totalCommissions: Double = 0.0,
    val totalSwaps: Double = 0.0,
    val profitFactor: Double = 0.0,
    // Breakdowns
    val buyTradesCount: Int = 0,
    val buyWinRate: Double = 0.0,
    val buyNetPnl: Double = 0.0,
    val sellTradesCount: Int = 0,
    val sellWinRate: Double = 0.0,
    val sellNetPnl: Double = 0.0,
    // By Asset
    val assetPerformance: List<AssetStat> = emptyList(),
    // By Day of Week (0=Sunday to 6=Saturday)
    val dayOfWeekPerformance: Map<Int, DayStat> = emptyMap(),
    // Equity points
    val equityCurvePoints: List<EquityPoint> = emptyList()
)

data class AssetStat(
    val symbol: String,
    val tradesCount: Int,
    val winRate: Double,
    val netPnl: Double
)

data class DayStat(
    val dayIndex: Int, // Calendar.SUNDAY .. SATURDAY
    val dayNameArabic: String,
    val tradesCount: Int,
    val netPnl: Double
)

data class EquityPoint(
    val timestamp: Long,
    val label: String,
    val balance: Double,
    val pnl: Double
)

object StatisticsCalculator {

    fun calculate(
        trades: List<Trade>,
        initialBalance: Double = 10000.0
    ): TradeStatistics {
        if (trades.isEmpty()) {
            return TradeStatistics(
                equityCurvePoints = listOf(
                    EquityPoint(System.currentTimeMillis(), "البداية", initialBalance, 0.0)
                )
            )
        }

        // Only closed trades contribute to realized PnL statistics
        val closedTrades = trades.filter { it.status.equals("CLOSED", ignoreCase = true) }
            .sortedBy { it.exitDate ?: it.entryDate }

        var totalProfits = 0.0
        var totalLosses = 0.0
        var winningCount = 0
        var losingCount = 0
        var breakevenCount = 0
        var bestTrade = 0.0
        var worstTrade = 0.0
        var totalLots = 0.0
        var totalCommissions = 0.0
        var totalSwaps = 0.0
        var sumRiskReward = 0.0
        var countRiskReward = 0

        var buyCount = 0
        var buyWins = 0
        var buyPnl = 0.0

        var sellCount = 0
        var sellWins = 0
        var sellPnl = 0.0

        val assetMap = mutableMapOf<String, MutableList<Trade>>()
        val dayMap = mutableMapOf<Int, MutableList<Trade>>()

        var runningBalance = initialBalance
        val equityPoints = mutableListOf<EquityPoint>()
        val dateFormatter = SimpleDateFormat("dd/MM", Locale.US)

        equityPoints.add(
            EquityPoint(
                timestamp = trades.minOfOrNull { it.entryDate } ?: System.currentTimeMillis(),
                label = "البداية",
                balance = initialBalance,
                pnl = 0.0
            )
        )

        for (trade in closedTrades) {
            val pnl = trade.profit
            totalLots += trade.lotSize
            totalCommissions += trade.commission
            totalSwaps += trade.swap

            if (pnl > 0.001) {
                totalProfits += pnl
                winningCount++
                if (pnl > bestTrade) bestTrade = pnl
            } else if (pnl < -0.001) {
                totalLosses += abs(pnl)
                losingCount++
                if (pnl < worstTrade) worstTrade = pnl
            } else {
                breakevenCount++
            }

            trade.riskRewardRatio?.let {
                if (it > 0) {
                    sumRiskReward += it
                    countRiskReward++
                }
            }

            val isBuy = trade.orderType.equals("BUY", ignoreCase = true)
            if (isBuy) {
                buyCount++
                buyPnl += pnl
                if (pnl > 0) buyWins++
            } else {
                sellCount++
                sellPnl += pnl
                if (pnl > 0) sellWins++
            }

            // Asset grouping
            assetMap.getOrPut(trade.symbol) { mutableListOf() }.add(trade)

            // Day of week grouping
            val cal = Calendar.getInstance().apply { timeInMillis = trade.exitDate ?: trade.entryDate }
            val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
            dayMap.getOrPut(dayOfWeek) { mutableListOf() }.add(trade)

            // Equity curve
            runningBalance += pnl
            equityPoints.add(
                EquityPoint(
                    timestamp = trade.exitDate ?: trade.entryDate,
                    label = dateFormatter.format(Date(trade.exitDate ?: trade.entryDate)),
                    balance = (runningBalance * 100).roundToInt() / 100.0,
                    pnl = pnl
                )
            )
        }

        val totalClosed = closedTrades.size
        val winRate = if (totalClosed > 0) (winningCount.toDouble() / totalClosed) * 100.0 else 0.0
        val lossRate = if (totalClosed > 0) (losingCount.toDouble() / totalClosed) * 100.0 else 0.0
        val netPnl = totalProfits - totalLosses

        val avgProfit = if (winningCount > 0) totalProfits / winningCount else 0.0
        val avgLoss = if (losingCount > 0) totalLosses / losingCount else 0.0
        val avgRR = if (countRiskReward > 0) sumRiskReward / countRiskReward else 0.0
        val profitFactor = if (totalLosses > 0) totalProfits / totalLosses else if (totalProfits > 0) 99.9 else 0.0

        val assetStats = assetMap.map { (symbol, symbolTrades) ->
            val symWins = symbolTrades.count { it.profit > 0 }
            val symPnl = symbolTrades.sumOf { it.profit }
            val symWinRate = (symWins.toDouble() / symbolTrades.size) * 100.0
            AssetStat(
                symbol = symbol,
                tradesCount = symbolTrades.size,
                winRate = (symWinRate * 10).roundToInt() / 10.0,
                netPnl = (symPnl * 100).roundToInt() / 100.0
            )
        }.sortedByDescending { it.netPnl }

        val dayNames = mapOf(
            Calendar.SUNDAY to "الأحد",
            Calendar.MONDAY to "الإثنين",
            Calendar.TUESDAY to "الثلاثاء",
            Calendar.WEDNESDAY to "الأربعاء",
            Calendar.THURSDAY to "الخميس",
            Calendar.FRIDAY to "الجمعة",
            Calendar.SATURDAY to "السبت"
        )

        val dayPerformance = dayNames.mapValues { (dayIndex, name) ->
            val dayTrades = dayMap[dayIndex] ?: emptyList()
            DayStat(
                dayIndex = dayIndex,
                dayNameArabic = name,
                tradesCount = dayTrades.size,
                netPnl = (dayTrades.sumOf { it.profit } * 100).roundToInt() / 100.0
            )
        }

        return TradeStatistics(
            totalProfits = (totalProfits * 100).roundToInt() / 100.0,
            totalLosses = (totalLosses * 100).roundToInt() / 100.0,
            netPnl = (netPnl * 100).roundToInt() / 100.0,
            totalTradesCount = totalClosed,
            winningTradesCount = winningCount,
            losingTradesCount = losingCount,
            breakevenTradesCount = breakevenCount,
            winRatePercent = (winRate * 10).roundToInt() / 10.0,
            lossRatePercent = (lossRate * 10).roundToInt() / 10.0,
            avgProfitPerTrade = (avgProfit * 100).roundToInt() / 100.0,
            avgLossPerTrade = (avgLoss * 100).roundToInt() / 100.0,
            bestTrade = (bestTrade * 100).roundToInt() / 100.0,
            worstTrade = (worstTrade * 100).roundToInt() / 100.0,
            totalLots = (totalLots * 100).roundToInt() / 100.0,
            avgRiskReward = (avgRR * 100).roundToInt() / 100.0,
            totalCommissions = (totalCommissions * 100).roundToInt() / 100.0,
            totalSwaps = (totalSwaps * 100).roundToInt() / 100.0,
            profitFactor = (profitFactor * 100).roundToInt() / 100.0,
            buyTradesCount = buyCount,
            buyWinRate = if (buyCount > 0) ((buyWins.toDouble() / buyCount) * 1000).roundToInt() / 10.0 else 0.0,
            buyNetPnl = (buyPnl * 100).roundToInt() / 100.0,
            sellTradesCount = sellCount,
            sellWinRate = if (sellCount > 0) ((sellWins.toDouble() / sellCount) * 1000).roundToInt() / 10.0 else 0.0,
            sellNetPnl = (sellPnl * 100).roundToInt() / 100.0,
            assetPerformance = assetStats,
            dayOfWeekPerformance = dayPerformance,
            equityCurvePoints = equityPoints
        )
    }
}
