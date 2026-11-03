package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.AssetStat
import com.example.domain.model.DayStat
import com.example.domain.model.EquityPoint
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.LossRed
import com.example.ui.theme.LossRedBright
import com.example.ui.theme.ProfitGreen
import com.example.ui.theme.ProfitGreenBright
import java.util.Locale
import kotlin.math.abs

/**
 * 1. Equity Curve Line Chart with gradient fill
 */
@Composable
fun EquityCurveChart(
    points: List<EquityPoint>,
    currency: String = "$",
    modifier: Modifier = Modifier
) {
    if (points.isEmpty()) return

    val minBalance = points.minOf { it.balance }
    val maxBalance = points.maxOf { it.balance }
    val range = if (maxBalance == minBalance) 1.0 else (maxBalance - minBalance)

    val isProfitable = (points.last().balance >= points.first().balance)
    val lineColor = if (isProfitable) ProfitGreenBright else LossRedBright
    val fillGradient = Brush.verticalGradient(
        colors = listOf(
            lineColor.copy(alpha = 0.35f),
            lineColor.copy(alpha = 0.0f)
        )
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "منحنى تطور رأس المال (Equity Curve)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "${String.format(Locale.US, "%,.2f", points.last().balance)} $currency",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = lineColor
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 16.dp)
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val width = size.width
                val height = size.height

                // Draw background horizontal grid lines
                val gridColor = Color.White.copy(alpha = 0.06f)
                val steps = 3
                for (i in 0..steps) {
                    val y = height * (i.toFloat() / steps)
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, y),
                        end = Offset(width, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                if (points.size == 1) {
                    drawCircle(
                        color = lineColor,
                        radius = 6.dp.toPx(),
                        center = Offset(width / 2f, height / 2f)
                    )
                    return@Canvas
                }

                val path = Path()
                val fillPath = Path()

                val stepX = width / (points.size - 1)

                points.forEachIndexed { index, pt ->
                    val x = index * stepX
                    val normY = ((pt.balance - minBalance) / range).toFloat()
                    val y = height - (normY * (height - 20.dp.toPx()) + 10.dp.toPx())

                    if (index == 0) {
                        path.moveTo(x, y)
                        fillPath.moveTo(x, height)
                        fillPath.lineTo(x, y)
                    } else {
                        // Smooth bezier curve
                        val prevPt = points[index - 1]
                        val prevX = (index - 1) * stepX
                        val prevNormY = ((prevPt.balance - minBalance) / range).toFloat()
                        val prevY = height - (prevNormY * (height - 20.dp.toPx()) + 10.dp.toPx())

                        val cx1 = (prevX + x) / 2f
                        path.cubicTo(cx1, prevY, cx1, y, x, y)
                        fillPath.cubicTo(cx1, prevY, cx1, y, x, y)
                    }

                    if (index == points.size - 1) {
                        fillPath.lineTo(x, height)
                        fillPath.close()
                    }
                }

                // Draw gradient under curve
                drawPath(fillPath, brush = fillGradient)

                // Draw main curve stroke
                drawPath(
                    path = path,
                    color = lineColor,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )

                // Highlight last point
                val lastPt = points.last()
                val lastX = width
                val lastNormY = ((lastPt.balance - minBalance) / range).toFloat()
                val lastY = height - (lastNormY * (height - 20.dp.toPx()) + 10.dp.toPx())

                drawCircle(
                    color = lineColor,
                    radius = 5.dp.toPx(),
                    center = Offset(lastX, lastY)
                )
                drawCircle(
                    color = Color.White,
                    radius = 2.5.dp.toPx(),
                    center = Offset(lastX, lastY)
                )
            }
        }
    }
}

/**
 * 2. Win / Loss Donut Chart
 */
@Composable
fun WinLossDonutChart(
    winCount: Int,
    lossCount: Int,
    breakevenCount: Int = 0,
    modifier: Modifier = Modifier
) {
    val total = winCount + lossCount + breakevenCount
    val winAngle = if (total > 0) (winCount.toFloat() / total) * 360f else 180f
    val lossAngle = if (total > 0) (lossCount.toFloat() / total) * 360f else 180f
    val beAngle = if (total > 0) (breakevenCount.toFloat() / total) * 360f else 0f

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        Box(
            modifier = Modifier.size(110.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val stroke = 22.dp.toPx()
                val arcSize = Size(size.width - stroke, size.height - stroke)
                val topLeft = Offset(stroke / 2f, stroke / 2f)

                if (total == 0) {
                    drawArc(
                        color = Color.Gray.copy(alpha = 0.3f),
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(stroke)
                    )
                } else {
                    var currentAngle = -90f

                    // Win arc
                    if (winAngle > 0) {
                        drawArc(
                            color = ProfitGreenBright,
                            startAngle = currentAngle,
                            sweepAngle = winAngle,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(stroke)
                        )
                        currentAngle += winAngle
                    }

                    // Loss arc
                    if (lossAngle > 0) {
                        drawArc(
                            color = LossRedBright,
                            startAngle = currentAngle,
                            sweepAngle = lossAngle,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(stroke)
                        )
                        currentAngle += lossAngle
                    }

                    // Breakeven arc
                    if (beAngle > 0) {
                        drawArc(
                            color = GoldPrimary,
                            startAngle = currentAngle,
                            sweepAngle = beAngle,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(stroke)
                        )
                    }
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                val winPct = if (total > 0) (winCount.toFloat() / total) * 100f else 0f
                Text(
                    text = String.format(Locale.US, "%.0f%%", winPct),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "نجاح",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ChartLegendRow("صفقات رابحة", winCount, ProfitGreenBright)
            ChartLegendRow("صفقات خاسرة", lossCount, LossRedBright)
            if (breakevenCount > 0) {
                ChartLegendRow("نقطة الدخول", breakevenCount, GoldPrimary)
            }
        }
    }
}

@Composable
private fun ChartLegendRow(label: String, count: Int, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "$label: $count",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * 3. Day of Week Performance Chart
 */
@Composable
fun DayOfWeekBarChart(
    dayStats: Map<Int, DayStat>,
    currency: String = "$",
    modifier: Modifier = Modifier
) {
    val days = listOf("الأحد", "الإثنين", "الثلاثاء", "الأربعاء", "الخميس", "الجمعة", "السبت")
    val pnlList = (1..7).map { dayIndex ->
        dayStats[dayIndex]?.netPnl ?: 0.0
    }
    val maxAbs = maxOf(10.0, pnlList.maxOf { abs(it) })

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "تحليل الأرباح والخسائر حسب أيام الأسبوع",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                days.forEachIndexed { index, dayName ->
                    val pnl = pnlList[index]
                    val isProfit = pnl >= 0
                    val fraction = (abs(pnl) / maxAbs).toFloat().coerceIn(0.06f, 1f)
                    val barColor = if (pnl == 0.0) Color.Gray.copy(alpha = 0.3f) else if (isProfit) ProfitGreenBright else LossRedBright

                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Text(
                            text = if (pnl != 0.0) String.format(Locale.US, "%.0f", pnl) else "0",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = barColor
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .width(16.dp)
                                .height((80 * fraction).dp)
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                .background(barColor)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = dayName.take(3),
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

/**
 * 4. BUY vs SELL Performance Breakdown
 */
@Composable
fun BuyVsSellCard(
    buyCount: Int,
    buyWinRate: Double,
    buyPnl: Double,
    sellCount: Int,
    sellWinRate: Double,
    sellPnl: Double,
    currency: String = "$",
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "مقارنة صفقات الشراء (BUY) مقابل البيع (SELL)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // BUY Section
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(ProfitGreen.copy(alpha = 0.1f))
                        .border(1.dp, ProfitGreen.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "صفقات الشراء BUY",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = ProfitGreenBright
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "العدد: $buyCount",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "نسبة النجاح: ${String.format(Locale.US, "%.1f%%", buyWinRate)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${if (buyPnl >= 0) "+" else ""}${String.format(Locale.US, "%,.2f", buyPnl)} $currency",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (buyPnl >= 0) ProfitGreenBright else LossRedBright
                    )
                }

                // SELL Section
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(LossRed.copy(alpha = 0.1f))
                        .border(1.dp, LossRed.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "صفقات البيع SELL",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = LossRedBright
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "العدد: $sellCount",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "نسبة النجاح: ${String.format(Locale.US, "%.1f%%", sellWinRate)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${if (sellPnl >= 0) "+" else ""}${String.format(Locale.US, "%,.2f", sellPnl)} $currency",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (sellPnl >= 0) ProfitGreenBright else LossRedBright
                    )
                }
            }
        }
    }
}
