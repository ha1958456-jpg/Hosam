package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.calculations.TradingCalculations
import com.example.ui.components.EquityCurveChart
import com.example.ui.components.StatCard
import com.example.ui.components.TimeFilterBar
import com.example.ui.components.WinLossProgressBar
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.LossRedBright
import com.example.ui.theme.ProfitGreenBright
import com.example.ui.viewmodel.TradingViewModel
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: TradingViewModel,
    onNavigateToAddTrade: () -> Unit,
    onNavigateToTrades: () -> Unit,
    onNavigateToReports: () -> Unit,
    modifier: Modifier = Modifier
) {
    val stats by viewModel.statistics.collectAsStateWithLifecycle()
    val timeFilter by viewModel.timeFilter.collectAsStateWithLifecycle()
    val activeAccount by viewModel.activeAccount.collectAsStateWithLifecycle()
    val currency = activeAccount?.currency ?: "$"
    val filteredTrades by viewModel.filteredTrades.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("dashboard_screen")
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Time Filter Horizontal Strip
        item {
            Spacer(modifier = Modifier.height(4.dp))
            TimeFilterBar(
                currentFilter = timeFilter,
                onFilterChanged = { viewModel.timeFilter.value = it }
            )
        }

        // 2. The Signature Split Horizontal Bar (Section Two requirement)
        item {
            WinLossProgressBar(
                winRate = stats.winRatePercent,
                lossRate = stats.lossRatePercent,
                winningCount = stats.winningTradesCount,
                losingCount = stats.losingTradesCount
            )
        }

        // 3. Hero Net P&L Card with Generate Report Button
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dashboard_hero_card"),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (stats.netPnl >= 0) ProfitGreenBright.copy(alpha = 0.4f) else LossRedBright.copy(alpha = 0.4f)
                ),
                tonalElevation = 4.dp
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "صافي الأرباح / الخسائر",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${if (stats.netPnl >= 0) "+" else ""}${TradingCalculations.formatNumber(stats.netPnl)} $currency",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 28.sp
                                ),
                                color = if (stats.netPnl >= 0) ProfitGreenBright else LossRedBright
                            )
                        }

                        // Report Image shortcut button
                        Button(
                            onClick = onNavigateToReports,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GoldPrimary.copy(alpha = 0.2f),
                                contentColor = GoldPrimary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("generate_report_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "إنشاء صورة التقرير",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "معدل الربح إلى الخسارة: ${String.format(Locale.US, "%.2f", stats.profitFactor)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "الصفقات المغلقة: ${stats.totalTradesCount}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // 4. All 13 Detailed Metrics Grid
        item {
            Text(
                text = "مؤشرات الأداء الرئيسية (13 مؤشر)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }

        // Row 1: Total Profits & Total Losses
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "1. إجمالي الأرباح",
                    value = "+${TradingCalculations.formatNumber(stats.totalProfits)} $currency",
                    icon = Icons.Default.ArrowUpward,
                    iconTint = ProfitGreenBright,
                    valueColor = ProfitGreenBright,
                    testTag = "stat_total_profits",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "2. إجمالي الخسائر",
                    value = "-${TradingCalculations.formatNumber(stats.totalLosses)} $currency",
                    icon = Icons.Default.ArrowDownward,
                    iconTint = LossRedBright,
                    valueColor = LossRedBright,
                    testTag = "stat_total_losses",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Row 2: Winning Trades & Losing Trades
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "5. صفقات رابحة",
                    value = "${stats.winningTradesCount} صفقة",
                    subValue = "${String.format(Locale.US, "%.1f%%", stats.winRatePercent)} من الإجمالي",
                    icon = Icons.Default.CheckCircle,
                    iconTint = ProfitGreenBright,
                    testTag = "stat_winning_count",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "6. صفقات خاسرة",
                    value = "${stats.losingTradesCount} صفقة",
                    subValue = "${String.format(Locale.US, "%.1f%%", stats.lossRatePercent)} من الإجمالي",
                    icon = Icons.Default.Error,
                    iconTint = LossRedBright,
                    testTag = "stat_losing_count",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Row 3: Win Rate & Total Trades
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "7. نسبة النجاح (Win Rate)",
                    value = "${String.format(Locale.US, "%.1f%%", stats.winRatePercent)}",
                    icon = Icons.Default.Percent,
                    iconTint = GoldPrimary,
                    valueColor = GoldPrimary,
                    testTag = "stat_win_rate",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "4. عدد الصفقات الكلي",
                    value = "${stats.totalTradesCount} صفقة",
                    subValue = "منها ${stats.breakevenTradesCount} نقطة الدخول",
                    icon = Icons.Default.BarChart,
                    iconTint = MaterialTheme.colorScheme.primary,
                    testTag = "stat_total_trades",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Row 4: Average Profit & Average Loss
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "8. متوسط الربح للصفقة",
                    value = "+${TradingCalculations.formatNumber(stats.avgProfitPerTrade)} $currency",
                    icon = Icons.Default.Speed,
                    iconTint = ProfitGreenBright,
                    valueColor = ProfitGreenBright,
                    testTag = "stat_avg_profit",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "9. متوسط الخسارة للصفقة",
                    value = "-${TradingCalculations.formatNumber(stats.avgLossPerTrade)} $currency",
                    icon = Icons.Default.Warning,
                    iconTint = LossRedBright,
                    valueColor = LossRedBright,
                    testTag = "stat_avg_loss",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Row 5: Best Trade & Worst Trade
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "10. أفضل صفقة",
                    value = "+${TradingCalculations.formatNumber(stats.bestTrade)} $currency",
                    icon = Icons.Default.Star,
                    iconTint = GoldPrimary,
                    valueColor = ProfitGreenBright,
                    testTag = "stat_best_trade",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "11. أسوأ صفقة",
                    value = "${TradingCalculations.formatNumber(stats.worstTrade)} $currency",
                    icon = Icons.Default.Warning,
                    iconTint = LossRedBright,
                    valueColor = LossRedBright,
                    testTag = "stat_worst_trade",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Row 6: Total Lots & Average Risk/Reward
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "12. إجمالي اللوتات",
                    value = "${String.format(Locale.US, "%.2f", stats.totalLots)} Lot",
                    icon = Icons.Default.Layers,
                    iconTint = MaterialTheme.colorScheme.secondary,
                    testTag = "stat_total_lots",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "13. متوسط العائد/المخاطرة",
                    value = if (stats.avgRiskReward > 0) "1 : ${String.format(Locale.US, "%.2f", stats.avgRiskReward)}" else "غير متاح",
                    icon = Icons.Default.AccountBalanceWallet,
                    iconTint = GoldPrimary,
                    valueColor = GoldPrimary,
                    testTag = "stat_avg_rr",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 5. Equity Curve Chart Preview
        item {
            EquityCurveChart(
                points = stats.equityCurvePoints,
                currency = currency
            )
        }

        // 6. Recent Trades Header & Quick List
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "أحدث الصفقات",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "عرض الكل (${filteredTrades.size})",
                    style = MaterialTheme.typography.labelMedium,
                    color = GoldPrimary,
                    modifier = Modifier.clickable { onNavigateToTrades() }
                )
            }
        }

        // Show up to 3 recent trades on dashboard
        val previewTrades = filteredTrades.take(3)
        if (previewTrades.isEmpty()) {
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "لا توجد صفقات مسجلة في هذه الفترة",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = onNavigateToAddTrade,
                            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("أضف أول صفقة الآن", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            items(previewTrades.size) { index ->
                val trade = previewTrades[index]
                TradePreviewRow(trade = trade, currency = currency)
            }
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
fun TradePreviewRow(
    trade: com.example.data.local.entity.Trade,
    currency: String,
    modifier: Modifier = Modifier
) {
    val isProfit = trade.profit >= 0
    val isBuy = trade.orderType.equals("BUY", ignoreCase = true)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isBuy) ProfitGreenBright.copy(alpha = 0.15f) else LossRedBright.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isBuy) "BUY" else "SELL",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isBuy) ProfitGreenBright else LossRedBright
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = trade.symbol,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${trade.lotSize} Lot • ${if (trade.status == "OPEN") "مفتوحة" else "مغلقة"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${if (isProfit) "+" else ""}${TradingCalculations.formatNumber(trade.profit)} $currency",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isProfit) ProfitGreenBright else LossRedBright
                )
                Text(
                    text = "دخول: ${trade.entryPrice}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
