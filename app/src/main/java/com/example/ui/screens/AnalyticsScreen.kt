package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.calculations.TradingCalculations
import com.example.ui.components.BuyVsSellCard
import com.example.ui.components.DayOfWeekBarChart
import com.example.ui.components.EquityCurveChart
import com.example.ui.components.TimeFilterBar
import com.example.ui.components.WinLossDonutChart
import com.example.ui.components.WinLossProgressBar
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.LossRedBright
import com.example.ui.theme.ProfitGreenBright
import com.example.ui.viewmodel.TradingViewModel
import java.util.Locale

@Composable
fun AnalyticsScreen(
    viewModel: TradingViewModel,
    modifier: Modifier = Modifier
) {
    val stats by viewModel.statistics.collectAsStateWithLifecycle()
    val timeFilter by viewModel.timeFilter.collectAsStateWithLifecycle()
    val activeAccount by viewModel.activeAccount.collectAsStateWithLifecycle()
    val currency = activeAccount?.currency ?: "$"

    // Best and Worst trading days calculation
    val dayStats = stats.dayOfWeekPerformance
    val bestDay = dayStats.values.maxByOrNull { it.netPnl }
    val worstDay = dayStats.values.minByOrNull { it.netPnl }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("analytics_screen")
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Time Filter Pill Strip
        item {
            Spacer(modifier = Modifier.height(4.dp))
            TimeFilterBar(
                currentFilter = timeFilter,
                onFilterChanged = { viewModel.timeFilter.value = it }
            )
        }

        // Header Title
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Analytics,
                    contentDescription = null,
                    tint = GoldPrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "التحليلات المتقدمة والرسوم البيانية",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        // 1. Win / Loss Horizontal Split Bar
        item {
            WinLossProgressBar(
                winRate = stats.winRatePercent,
                lossRate = stats.lossRatePercent,
                winningCount = stats.winningTradesCount,
                losingCount = stats.losingTradesCount
            )
        }

        // 2. Win / Loss Donut Chart
        item {
            WinLossDonutChart(
                winCount = stats.winningTradesCount,
                lossCount = stats.losingTradesCount,
                breakevenCount = stats.breakevenTradesCount
            )
        }

        // 3. Equity Curve (Account Growth)
        item {
            EquityCurveChart(
                points = stats.equityCurvePoints,
                currency = currency
            )
        }

        // 4. BUY vs SELL Performance Breakdown
        item {
            BuyVsSellCard(
                buyCount = stats.buyTradesCount,
                buyWinRate = stats.buyWinRate,
                buyPnl = stats.buyNetPnl,
                sellCount = stats.sellTradesCount,
                sellWinRate = stats.sellWinRate,
                sellPnl = stats.sellNetPnl,
                currency = currency
            )
        }

        // 5. Day of Week Chart
        item {
            DayOfWeekBarChart(
                dayStats = stats.dayOfWeekPerformance,
                currency = currency
            )
        }

        // 6. Best and Worst Trading Days Highlight Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Best Day Card
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, ProfitGreenBright.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = ProfitGreenBright, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("أفضل يوم تداول", style = MaterialTheme.typography.labelMedium, color = ProfitGreenBright, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = bestDay?.dayNameArabic ?: "غير محدد",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (bestDay != null) "${if (bestDay.netPnl >= 0) "+" else ""}${TradingCalculations.formatNumber(bestDay.netPnl)} $currency" else "0 $currency",
                            style = MaterialTheme.typography.bodySmall,
                            color = ProfitGreenBright,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Worst Day Card
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, LossRedBright.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = LossRedBright, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("أسوأ يوم تداول", style = MaterialTheme.typography.labelMedium, color = LossRedBright, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = worstDay?.dayNameArabic ?: "غير محدد",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (worstDay != null) "${TradingCalculations.formatNumber(worstDay.netPnl)} $currency" else "0 $currency",
                            style = MaterialTheme.typography.bodySmall,
                            color = LossRedBright,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // 7. Performance by Asset Table / Cards
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "تحليل الأداء حسب الأصول المالية",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    if (stats.assetPerformance.isEmpty()) {
                        Text(
                            text = "لا توجد صفقات مغلقة لتحليل الأصول",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            stats.assetPerformance.forEach { asset ->
                                val isProfit = asset.netPnl >= 0
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = asset.symbol,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "${asset.tradesCount} صفقة • نسبة النجاح: ${asset.winRate}%",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Text(
                                        text = "${if (isProfit) "+" else ""}${TradingCalculations.formatNumber(asset.netPnl)} $currency",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = if (isProfit) ProfitGreenBright else LossRedBright
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}
