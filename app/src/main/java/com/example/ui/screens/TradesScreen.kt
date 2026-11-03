package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.local.entity.Trade
import com.example.domain.calculations.TradingCalculations
import com.example.ui.components.TimeFilterBar
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.LossRedBright
import com.example.ui.theme.ProfitGreenBright
import com.example.ui.viewmodel.TradeSortOrder
import com.example.ui.viewmodel.TradingViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TradesScreen(
    viewModel: TradingViewModel,
    onNavigateToAddTrade: () -> Unit,
    onEditTrade: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val filteredTrades by viewModel.filteredTrades.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val categoryFilter by viewModel.categoryFilter.collectAsStateWithLifecycle()
    val orderTypeFilter by viewModel.orderTypeFilter.collectAsStateWithLifecycle()
    val statusFilter by viewModel.statusFilter.collectAsStateWithLifecycle()
    val sortOrder by viewModel.sortOrder.collectAsStateWithLifecycle()
    val timeFilter by viewModel.timeFilter.collectAsStateWithLifecycle()
    val activeAccount by viewModel.activeAccount.collectAsStateWithLifecycle()
    val currency = activeAccount?.currency ?: "$"

    var selectedTradeForDetails by remember { mutableStateOf<Trade?>(null) }
    var tradeToDelete by remember { mutableStateOf<Trade?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("trades_screen")
    ) {
        // Time Filter Pills
        TimeFilterBar(
            currentFilter = timeFilter,
            onFilterChanged = { viewModel.timeFilter.value = it }
        )

        // Search Bar & Sort Dropdown Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.searchQuery.value = it },
                placeholder = { Text("بحث عن أصل أو ملاحظة...", fontSize = 13.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "بحث",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "مسح", modifier = Modifier.size(16.dp))
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .testTag("search_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GoldPrimary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                )
            )

            // Cycle Sort Button
            Button(
                onClick = {
                    viewModel.sortOrder.value = when (sortOrder) {
                        TradeSortOrder.NEWEST_FIRST -> TradeSortOrder.OLDEST_FIRST
                        TradeSortOrder.OLDEST_FIRST -> TradeSortOrder.HIGHEST_PROFIT
                        TradeSortOrder.HIGHEST_PROFIT -> TradeSortOrder.BIGGEST_LOSS
                        TradeSortOrder.BIGGEST_LOSS -> TradeSortOrder.NEWEST_FIRST
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.height(50.dp).testTag("sort_button")
            ) {
                Icon(Icons.Default.Sort, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = sortOrder.titleArabic, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Secondary Category & Type Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 14.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Category: All
            FilterChip(
                selected = categoryFilter == null || categoryFilter == "ALL",
                onClick = { viewModel.categoryFilter.value = null },
                label = { Text("جميع الأصول", fontSize = 11.sp) }
            )
            FilterChip(
                selected = categoryFilter == "GOLD",
                onClick = { viewModel.categoryFilter.value = if (categoryFilter == "GOLD") null else "GOLD" },
                label = { Text("ذهب ومعادن", fontSize = 11.sp) }
            )
            FilterChip(
                selected = categoryFilter == "FOREX",
                onClick = { viewModel.categoryFilter.value = if (categoryFilter == "FOREX") null else "FOREX" },
                label = { Text("فوركس", fontSize = 11.sp) }
            )
            FilterChip(
                selected = categoryFilter == "CRYPTO",
                onClick = { viewModel.categoryFilter.value = if (categoryFilter == "CRYPTO") null else "CRYPTO" },
                label = { Text("كريبتو", fontSize = 11.sp) }
            )
            FilterChip(
                selected = categoryFilter == "INDICES",
                onClick = { viewModel.categoryFilter.value = if (categoryFilter == "INDICES") null else "INDICES" },
                label = { Text("مؤشرات", fontSize = 11.sp) }
            )

            // Order Type Filter
            FilterChip(
                selected = orderTypeFilter == "BUY",
                onClick = { viewModel.orderTypeFilter.value = if (orderTypeFilter == "BUY") null else "BUY" },
                label = { Text("شراء BUY", fontSize = 11.sp, color = ProfitGreenBright) }
            )
            FilterChip(
                selected = orderTypeFilter == "SELL",
                onClick = { viewModel.orderTypeFilter.value = if (orderTypeFilter == "SELL") null else "SELL" },
                label = { Text("بيع SELL", fontSize = 11.sp, color = LossRedBright) }
            )

            // Status Filter
            FilterChip(
                selected = statusFilter == "OPEN",
                onClick = { viewModel.statusFilter.value = if (statusFilter == "OPEN") null else "OPEN" },
                label = { Text("مفتوحة فقط", fontSize = 11.sp) }
            )
            FilterChip(
                selected = statusFilter == "CLOSED",
                onClick = { viewModel.statusFilter.value = if (statusFilter == "CLOSED") null else "CLOSED" },
                label = { Text("مغلقة فقط", fontSize = 11.sp) }
            )
        }

        // Trades List
        if (filteredTrades.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "لا توجد صفقات تطابق البحث أو الفلاتر",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = onNavigateToAddTrade,
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("إضافة صفقة جديدة", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredTrades.size, key = { filteredTrades[it].id }) { index ->
                    val trade = filteredTrades[index]
                    TradeCard(
                        trade = trade,
                        currency = currency,
                        onClick = { selectedTradeForDetails = trade },
                        onEdit = { onEditTrade(trade.id) },
                        onDelete = { tradeToDelete = trade }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(70.dp))
                }
            }
        }
    }

    // Trade Details Bottom Sheet
    selectedTradeForDetails?.let { trade ->
        TradeDetailsBottomSheet(
            trade = trade,
            currency = currency,
            onDismiss = { selectedTradeForDetails = null },
            onEdit = {
                selectedTradeForDetails = null
                onEditTrade(trade.id)
            },
            onDelete = {
                selectedTradeForDetails = null
                tradeToDelete = trade
            }
        )
    }

    // Delete Confirmation Dialog
    tradeToDelete?.let { trade ->
        AlertDialog(
            onDismissRequest = { tradeToDelete = null },
            title = { Text("تأكيد حذف الصفقة") },
            text = { Text("هل أنت متأكد من رغبتك في حذف صفقة ${trade.symbol} (${trade.orderType})؟ لا يمكن التراجع عن هذا الإجراء.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteTrade(trade)
                        tradeToDelete = null
                    }
                ) {
                    Text("حذف", color = LossRedBright, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { tradeToDelete = null }) {
                    Text("إلغاء", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }
}

@Composable
fun TradeCard(
    trade: Trade,
    currency: String,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isProfit = trade.profit >= 0
    val isBuy = trade.orderType.equals("BUY", ignoreCase = true)
    val dateFormatter = remember { SimpleDateFormat("yyyy/MM/dd  HH:mm", Locale.US) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag("trade_card_${trade.id}"),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (trade.status == "OPEN") GoldPrimary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
        ),
        tonalElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Symbol, Badges, PnL
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // BUY / SELL Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isBuy) ProfitGreenBright.copy(alpha = 0.2f) else LossRedBright.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (isBuy) "BUY" else "SELL",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isBuy) ProfitGreenBright else LossRedBright
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = trade.symbol,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    if (trade.status == "OPEN") {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(GoldPrimary.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "مفتوحة",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldPrimary
                            )
                        }
                    }
                }

                // Profit / Loss Display
                Text(
                    text = "${if (isProfit) "+" else ""}${TradingCalculations.formatNumber(trade.profit)} $currency",
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 17.sp),
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isProfit) ProfitGreenBright else LossRedBright
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Details row: Entry, Exit, Lots
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "الدخول: ${trade.entryPrice}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (trade.exitPrice != null) "الخروج: ${trade.exitPrice}" else "الخروج: قيد التداول",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "اللوت: ${trade.lotSize}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    trade.riskRewardRatio?.let {
                        Text(
                            text = "R:R:  1 : $it",
                            style = MaterialTheme.typography.bodySmall,
                            color = GoldPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Footer: Date and Quick Edit/Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dateFormatter.format(Date(trade.entryDate)),
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )

                Row {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(32.dp).testTag("edit_trade_${trade.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "تعديل",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp).testTag("delete_trade_${trade.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "حذف",
                            tint = LossRedBright,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TradeDetailsBottomSheet(
    trade: Trade,
    currency: String,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val isProfit = trade.profit >= 0
    val isBuy = trade.orderType.equals("BUY", ignoreCase = true)
    val dateFormatter = remember { SimpleDateFormat("yyyy/MM/dd  HH:mm", Locale.US) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Title & PnL Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${trade.symbol} (${if (isBuy) "شراء BUY" else "بيع SELL"})",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "الحالة: ${if (trade.status == "OPEN") "صفقة مفتوحة" else "صفقة مغلقة"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (trade.status == "OPEN") GoldPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = "${if (isProfit) "+" else ""}${TradingCalculations.formatNumber(trade.profit)} $currency",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isProfit) ProfitGreenBright else LossRedBright
                )
            }

            // Specs Grid
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    DetailRow("سعر الدخول:", "${trade.entryPrice}")
                    DetailRow("سعر الخروج:", "${trade.exitPrice ?: "مفتوحة"}")
                    DetailRow("حجم اللوت:", "${trade.lotSize} Lot")
                    DetailRow("الهامش المستخدم:", "${trade.marginUsed} $currency")
                    DetailRow("وقف الخسارة (SL):", "${trade.stopLoss ?: "غير محدد"}")
                    DetailRow("جني الأرباح (TP):", "${trade.takeProfit ?: "غير محدد"}")
                    DetailRow("الرافعة المالية:", "1:${trade.leverage}")
                    DetailRow("العمولة والسواب:", "عمولة: ${trade.commission} $currency | سواب: ${trade.swap} $currency")
                    trade.riskRewardRatio?.let {
                        DetailRow("نسبة العائد/المخاطرة:", "1 : $it")
                    }
                    DetailRow("تاريخ الدخول:", dateFormatter.format(Date(trade.entryDate)))
                    trade.exitDate?.let {
                        DetailRow("تاريخ الخروج:", dateFormatter.format(Date(it)))
                    }
                }
            }

            // Notes
            if (trade.notes.isNotBlank()) {
                Text(
                    text = "الملاحظات والاستراتيجية:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = trade.notes,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Chart Screenshots
            if (trade.beforeChartUri != null || trade.afterChartUri != null) {
                Text(
                    text = "صور الشارت:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    trade.beforeChartUri?.let { uri ->
                        Column(modifier = Modifier.weight(1f)) {
                            Text("قبل الدخول", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(4.dp))
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current).data(uri).build(),
                                contentDescription = "قبل الدخول",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(110.dp)
                                    .clip(RoundedCornerShape(10.dp))
                            )
                        }
                    }
                    trade.afterChartUri?.let { uri ->
                        Column(modifier = Modifier.weight(1f)) {
                            Text("بعد الخروج", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(4.dp))
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current).data(uri).build(),
                                contentDescription = "بعد الخروج",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(110.dp)
                                    .clip(RoundedCornerShape(10.dp))
                            )
                        }
                    }
                }
            }

            // Actions: Edit / Delete
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onEdit,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, tint = Color.Black)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("تعديل الصفقة", color = Color.Black, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onDelete,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = LossRedBright)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("حذف الصفقة", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
    }
}
