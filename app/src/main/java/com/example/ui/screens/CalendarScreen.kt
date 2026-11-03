package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.Trade
import com.example.domain.calculations.TradingCalculations
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.LossRedBright
import com.example.ui.theme.ProfitGreenBright
import com.example.ui.viewmodel.TradingViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class CalendarDay(
    val dayNumber: Int,
    val dateMillis: Long,
    val isCurrentMonth: Boolean,
    val trades: List<Trade> = emptyList(),
    val netPnl: Double = 0.0
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    viewModel: TradingViewModel,
    modifier: Modifier = Modifier
) {
    val allTrades by viewModel.rawTrades.collectAsStateWithLifecycle()
    val activeAccount by viewModel.activeAccount.collectAsStateWithLifecycle()
    val currency = activeAccount?.currency ?: "$"

    var currentCalendar by remember { mutableStateOf(Calendar.getInstance()) }
    var selectedDayTrades by remember { mutableStateOf<CalendarDay?>(null) }

    val monthFormatter = remember { SimpleDateFormat("MMMM yyyy", Locale("ar")) }

    // Prepare calendar days for current month
    val daysInMonth = remember(currentCalendar, allTrades) {
        val cal = currentCalendar.clone() as Calendar
        cal.set(Calendar.DAY_OF_MONTH, 1)

        val month = cal.get(Calendar.MONTH)
        val year = cal.get(Calendar.YEAR)

        // Day of week for first day (1=Sunday, 7=Saturday)
        val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        val maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

        val list = mutableListOf<CalendarDay>()

        // Preceding days padding
        for (i in 1 until firstDayOfWeek) {
            list.add(CalendarDay(0, 0L, false))
        }

        // Real days
        for (day in 1..maxDays) {
            cal.set(Calendar.DAY_OF_MONTH, day)
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            val dayStart = cal.timeInMillis

            cal.set(Calendar.HOUR_OF_DAY, 23)
            cal.set(Calendar.MINUTE, 59)
            cal.set(Calendar.SECOND, 59)
            val dayEnd = cal.timeInMillis

            val dayTrades = allTrades.filter { t ->
                val tradeTime = t.exitDate ?: t.entryDate
                tradeTime in dayStart..dayEnd && t.status == "CLOSED"
            }
            val netPnl = dayTrades.sumOf { it.profit }

            list.add(
                CalendarDay(
                    dayNumber = day,
                    dateMillis = dayStart,
                    isCurrentMonth = true,
                    trades = dayTrades,
                    netPnl = netPnl
                )
            )
        }
        list
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("calendar_screen")
            .padding(14.dp)
    ) {
        // Month Selector Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    val newCal = currentCalendar.clone() as Calendar
                    newCal.add(Calendar.MONTH, -1)
                    currentCalendar = newCal
                }
            ) {
                // In RTL, ChevronRight moves to previous
                Icon(Icons.Default.ChevronRight, contentDescription = "الشهر السابق")
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = monthFormatter.format(currentCalendar.time),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            IconButton(
                onClick = {
                    val newCal = currentCalendar.clone() as Calendar
                    newCal.add(Calendar.MONTH, 1)
                    currentCalendar = newCal
                }
            ) {
                // In RTL, ChevronLeft moves to next
                Icon(Icons.Default.ChevronLeft, contentDescription = "الشهر التالي")
            }
        }

        // Day of Week Header Row (Sun to Sat)
        val dayHeaders = listOf("أحد", "إثنين", "ثلاثاء", "أربعاء", "خميس", "جمعة", "سبت")
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            dayHeaders.forEach { name ->
                Text(
                    text = name,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Calendar Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(daysInMonth.size) { index ->
                val day = daysInMonth[index]
                if (!day.isCurrentMonth) {
                    Spacer(modifier = Modifier.aspectRatio(1f))
                } else {
                    val hasTrades = day.trades.isNotEmpty()
                    val isProfit = day.netPnl > 0.001
                    val isLoss = day.netPnl < -0.001

                    val cellBg = when {
                        !hasTrades -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                        isProfit -> ProfitGreenBright.copy(alpha = 0.2f)
                        isLoss -> LossRedBright.copy(alpha = 0.2f)
                        else -> GoldPrimary.copy(alpha = 0.15f)
                    }

                    val borderColor = when {
                        !hasTrades -> MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                        isProfit -> ProfitGreenBright.copy(alpha = 0.7f)
                        isLoss -> LossRedBright.copy(alpha = 0.7f)
                        else -> GoldPrimary.copy(alpha = 0.7f)
                    }

                    Box(
                        modifier = Modifier
                            .aspectRatio(0.85f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(cellBg)
                            .border(1.dp, borderColor, RoundedCornerShape(10.dp))
                            .clickable(enabled = hasTrades) {
                                selectedDayTrades = day
                            }
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "${day.dayNumber}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (hasTrades) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${if (isProfit) "+" else ""}${String.format(Locale.US, "%.0f", day.netPnl)}",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isProfit) ProfitGreenBright else if (isLoss) LossRedBright else GoldPrimary
                                )
                                Text(
                                    text = "${day.trades.size} صفقات",
                                    fontSize = 8.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // Legend Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            LegendPill("يوم رابح", ProfitGreenBright)
            LegendPill("يوم خاسر", LossRedBright)
            LegendPill("بدون صفقات", Color.Gray.copy(alpha = 0.4f))
        }
    }

    // Selected Day Trades Bottom Sheet
    selectedDayTrades?.let { day ->
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        val dateFormatter = remember { SimpleDateFormat("EEEE, d MMMM yyyy", Locale("ar")) }

        ModalBottomSheet(
            onDismissRequest = { selectedDayTrades = null },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = dateFormatter.format(Date(day.dateMillis)),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "عدد الصفقات: ${day.trades.size}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Text(
                        text = "${if (day.netPnl >= 0) "+" else ""}${TradingCalculations.formatNumber(day.netPnl)} $currency",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (day.netPnl >= 0) ProfitGreenBright else LossRedBright
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(day.trades.size) { idx ->
                        val trade = day.trades[idx]
                        TradePreviewRow(trade = trade, currency = currency)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun LegendPill(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = label, style = MaterialTheme.typography.labelSmall)
    }
}
