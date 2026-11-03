package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.local.entity.Trade
import com.example.domain.calculations.AssetCategory
import com.example.domain.calculations.ContractSpecs
import com.example.domain.calculations.TradingCalculations
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.LossRedBright
import com.example.ui.theme.ProfitGreenBright
import com.example.ui.viewmodel.TradingViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTradeScreen(
    viewModel: TradingViewModel,
    existingTradeId: Long? = null,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeAccount by viewModel.activeAccount.collectAsStateWithLifecycle()
    val currency = activeAccount?.currency ?: "$"
    val allTrades by viewModel.rawTrades.collectAsStateWithLifecycle()
    val existingTrade = remember(existingTradeId, allTrades) {
        allTrades.firstOrNull { it.id == existingTradeId }
    }

    // Form States
    var symbol by remember { mutableStateOf(existingTrade?.symbol ?: "XAUUSD") }
    var category by remember { mutableStateOf(existingTrade?.category ?: "GOLD") }
    var orderType by remember { mutableStateOf(existingTrade?.orderType ?: "BUY") } // BUY or SELL
    var entryPriceText by remember { mutableStateOf(existingTrade?.entryPrice?.toString() ?: "2730.00") }
    var exitPriceText by remember { mutableStateOf(existingTrade?.exitPrice?.toString() ?: "2745.00") }
    var lotSizeText by remember { mutableStateOf(existingTrade?.lotSize?.toString() ?: "0.50") }
    var leverage by remember { mutableIntStateOf(existingTrade?.leverage ?: 100) }
    var stopLossText by remember { mutableStateOf(existingTrade?.stopLoss?.toString() ?: "") }
    var takeProfitText by remember { mutableStateOf(existingTrade?.takeProfit?.toString() ?: "") }
    var commissionText by remember { mutableStateOf(existingTrade?.commission?.toString() ?: "3.00") }
    var swapText by remember { mutableStateOf(existingTrade?.swap?.toString() ?: "0.0") }
    var spreadText by remember { mutableStateOf(existingTrade?.spread?.toString() ?: "0.0") }
    var status by remember { mutableStateOf(existingTrade?.status ?: "CLOSED") } // OPEN or CLOSED
    var notes by remember { mutableStateOf(existingTrade?.notes ?: "") }
    var manualPnlText by remember { mutableStateOf(if (existingTrade?.isManualPnl == true) existingTrade.profit.toString() else "") }
    var isManualPnlMode by remember { mutableStateOf(existingTrade?.isManualPnl ?: false) }

    var beforeChartUri by remember { mutableStateOf<String?>(existingTrade?.beforeChartUri) }
    var afterChartUri by remember { mutableStateOf<String?>(existingTrade?.afterChartUri) }

    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Photo Pickers (Zero-permission Android Photo Picker)
    val beforeImagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri: Uri? ->
            if (uri != null) {
                beforeChartUri = uri.toString()
            }
        }
    )

    val afterImagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri: Uri? ->
            if (uri != null) {
                afterChartUri = uri.toString()
            }
        }
    )

    // Live Calculations Engine
    val entryPrice = entryPriceText.toDoubleOrNull() ?: 0.0
    val exitPrice = exitPriceText.toDoubleOrNull() ?: 0.0
    val lotSize = lotSizeText.toDoubleOrNull() ?: 0.0
    val stopLoss = stopLossText.toDoubleOrNull()
    val takeProfit = takeProfitText.toDoubleOrNull()
    val commission = commissionText.toDoubleOrNull() ?: 0.0
    val swap = swapText.toDoubleOrNull() ?: 0.0
    val spread = spreadText.toDoubleOrNull() ?: 0.0

    val calculatedPnl = remember(symbol, category, orderType, lotSize, entryPrice, exitPrice, commission, swap) {
        if (exitPrice > 0.0) {
            TradingCalculations.calculateNetPnl(
                symbol = symbol,
                category = category,
                orderType = orderType,
                lotSize = lotSize,
                entryPrice = entryPrice,
                exitPrice = exitPrice,
                commission = commission,
                swap = swap
            )
        } else 0.0
    }

    val finalProfit = if (isManualPnlMode) (manualPnlText.toDoubleOrNull() ?: calculatedPnl) else calculatedPnl

    val calculatedMargin = remember(symbol, category, lotSize, entryPrice, leverage) {
        TradingCalculations.calculateRequiredMargin(
            symbol = symbol,
            category = category,
            lotSize = lotSize,
            entryPrice = entryPrice,
            leverage = leverage
        )
    }

    val calculatedRR = remember(orderType, entryPrice, stopLoss, takeProfit) {
        TradingCalculations.calculateRiskReward(
            orderType = orderType,
            entryPrice = entryPrice,
            stopLoss = stopLoss,
            takeProfit = takeProfit
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (existingTrade == null) "إضافة صفقة جديدة" else "تعديل الصفقة",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("back_button")) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "رجوع",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    if (existingTrade != null) {
                        IconButton(
                            onClick = {
                                viewModel.deleteTrade(existingTrade)
                                onBack()
                            },
                            modifier = Modifier.testTag("delete_trade_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "حذف الصفقة",
                                tint = LossRedBright
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = {
                        val tradeToSave = Trade(
                            id = existingTrade?.id ?: 0L,
                            accountId = activeAccount?.id ?: 1L,
                            symbol = symbol.ifBlank { "XAUUSD" }.uppercase(),
                            category = category,
                            orderType = orderType,
                            entryDate = existingTrade?.entryDate ?: System.currentTimeMillis(),
                            exitDate = if (status == "CLOSED") (existingTrade?.exitDate ?: System.currentTimeMillis()) else null,
                            entryPrice = entryPrice,
                            exitPrice = if (exitPrice > 0.0) exitPrice else null,
                            lotSize = if (lotSize > 0.0) lotSize else 0.01,
                            leverage = leverage,
                            marginUsed = calculatedMargin,
                            stopLoss = stopLoss,
                            takeProfit = takeProfit,
                            spread = spread,
                            commission = commission,
                            swap = swap,
                            profit = finalProfit,
                            status = status,
                            notes = notes,
                            beforeChartUri = beforeChartUri,
                            afterChartUri = afterChartUri,
                            contractSize = ContractSpecs.getContractSize(symbol, category),
                            riskRewardRatio = calculatedRR,
                            isManualPnl = isManualPnlMode
                        )
                        viewModel.saveTrade(tradeToSave)
                        onBack()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(52.dp)
                        .testTag("save_trade_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.Black
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (existingTrade == null) "حفظ الصفقة في الدفتر" else "حفظ التعديلات",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Live Real-Time Result Box
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("live_calculation_card"),
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (finalProfit >= 0) ProfitGreenBright.copy(alpha = 0.5f) else LossRedBright.copy(alpha = 0.5f)
                ),
                tonalElevation = 3.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "الربح / الخسارة المتوقعة (صافي)",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${if (finalProfit >= 0) "+" else ""}${TradingCalculations.formatNumber(finalProfit)} $currency",
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                                color = if (finalProfit >= 0) ProfitGreenBright else LossRedBright
                            )
                        }

                        // Toggle manual override
                        FilterChip(
                            selected = isManualPnlMode,
                            onClick = { isManualPnlMode = !isManualPnlMode },
                            label = { Text(if (isManualPnlMode) "يدوي" else "تلقائي", fontSize = 11.sp) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Calculate,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "الهامش المستخدم: ${TradingCalculations.formatNumber(calculatedMargin)} $currency",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (calculatedRR != null) "العائد للمخاطرة: 1 : $calculatedRR" else "R:R: غير محدد",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = GoldPrimary
                        )
                    }
                }
            }

            // Quick Preset Symbol Selector
            Column {
                Text(
                    text = "الأصول المالية الشائعة",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val presets = listOf("XAUUSD", "EURUSD", "GBPUSD", "USDJPY", "BTCUSD", "ETHUSD", "US30", "NAS100", "AAPL")
                    presets.forEach { pre ->
                        FilterChip(
                            selected = symbol.equals(pre, ignoreCase = true),
                            onClick = {
                                symbol = pre
                                val p = ContractSpecs.findPreset(pre)
                                if (p != null) {
                                    category = p.category.name
                                }
                            },
                            label = { Text(pre, fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                        )
                    }
                }
            }

            // Symbol and Category
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = symbol,
                    onValueChange = { symbol = it },
                    label = { Text("اسم الأصل أو الزوج") },
                    placeholder = { Text("مثال: XAUUSD") },
                    singleLine = true,
                    modifier = Modifier
                        .weight(1.2f)
                        .testTag("input_symbol"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldPrimary
                    )
                )

                // Category Selector
                OutlinedTextField(
                    value = when (category) {
                        "GOLD" -> "ذهب ومعادن"
                        "FOREX" -> "فوركس"
                        "CRYPTO" -> "كريبتو"
                        "INDICES" -> "مؤشرات"
                        "STOCKS" -> "أسهم"
                        else -> "أخرى"
                    },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("النوع") },
                    trailingIcon = {
                        Text(
                            text = "تبديل",
                            color = GoldPrimary,
                            fontSize = 11.sp,
                            modifier = Modifier
                                .clickable {
                                    category = when (category) {
                                        "GOLD" -> "FOREX"
                                        "FOREX" -> "CRYPTO"
                                        "CRYPTO" -> "INDICES"
                                        "INDICES" -> "STOCKS"
                                        else -> "GOLD"
                                    }
                                }
                                .padding(end = 8.dp)
                        )
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            // Order Type (BUY / SELL) & Status (OPEN / CLOSED)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // BUY / SELL switch
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (orderType == "BUY") ProfitGreenBright else Color.Transparent)
                            .clickable { orderType = "BUY" }
                            .padding(vertical = 10.dp)
                            .testTag("button_type_buy"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "شراء BUY",
                            fontWeight = FontWeight.Bold,
                            color = if (orderType == "BUY") Color.Black else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (orderType == "SELL") LossRedBright else Color.Transparent)
                            .clickable { orderType = "SELL" }
                            .padding(vertical = 10.dp)
                            .testTag("button_type_sell"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "بيع SELL",
                            fontWeight = FontWeight.Bold,
                            color = if (orderType == "SELL") Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Status switch (OPEN / CLOSED)
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (status == "CLOSED") GoldPrimary else Color.Transparent)
                            .clickable { status = "CLOSED" }
                            .padding(vertical = 10.dp)
                            .testTag("status_closed"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "مغلقة",
                            fontWeight = FontWeight.Bold,
                            color = if (status == "CLOSED") Color.Black else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (status == "OPEN") MaterialTheme.colorScheme.secondary else Color.Transparent)
                            .clickable { status = "OPEN" }
                            .padding(vertical = 10.dp)
                            .testTag("status_open"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "مفتوحة",
                            fontWeight = FontWeight.Bold,
                            color = if (status == "OPEN") Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Entry Price & Exit Price
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = entryPriceText,
                    onValueChange = { entryPriceText = it },
                    label = { Text("سعر الدخول") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("input_entry_price"),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary)
                )
                OutlinedTextField(
                    value = exitPriceText,
                    onValueChange = { exitPriceText = it },
                    label = { Text("سعر الخروج (اختياري)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("input_exit_price"),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary)
                )
            }

            // Lot Size & Leverage
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = lotSizeText,
                    onValueChange = { lotSizeText = it },
                    label = { Text("حجم اللوت (Lot)") },
                    placeholder = { Text("0.10") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("input_lot_size"),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary)
                )

                OutlinedTextField(
                    value = "1:$leverage",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("الرافعة المالية") },
                    trailingIcon = {
                        Text(
                            text = "تغيير",
                            color = GoldPrimary,
                            fontSize = 11.sp,
                            modifier = Modifier
                                .clickable {
                                    leverage = when (leverage) {
                                        50 -> 100
                                        100 -> 200
                                        200 -> 500
                                        500 -> 1000
                                        else -> 50
                                    }
                                }
                                .padding(end = 8.dp)
                        )
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            // Stop Loss & Take Profit
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = stopLossText,
                    onValueChange = { stopLossText = it },
                    label = { Text("وقف الخسارة (SL)") },
                    placeholder = { Text("اختياري") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("input_stop_loss"),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = LossRedBright)
                )
                OutlinedTextField(
                    value = takeProfitText,
                    onValueChange = { takeProfitText = it },
                    label = { Text("جني الأرباح (TP)") },
                    placeholder = { Text("اختياري") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("input_take_profit"),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ProfitGreenBright)
                )
            }

            // Commission & Swap
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = commissionText,
                    onValueChange = { commissionText = it },
                    label = { Text("العمولة ($currency)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary)
                )
                OutlinedTextField(
                    value = swapText,
                    onValueChange = { swapText = it },
                    label = { Text("السواب ($currency)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary)
                )
            }

            // Manual Profit/Loss Field if manual mode enabled
            if (isManualPnlMode) {
                OutlinedTextField(
                    value = manualPnlText,
                    onValueChange = { manualPnlText = it },
                    label = { Text("الربح أو الخسارة الفعلي ($currency)") },
                    placeholder = { Text("أدخل القيمة يدوياً من كشف الحساب") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_manual_pnl"),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary)
                )
            }

            // Notes / Strategy
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("ملاحظات واستراتيجية الصفقة") },
                placeholder = { Text("سبب الدخول، خطة الخروج، الحالة النفسية...") },
                minLines = 3,
                maxLines = 5,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_notes"),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary)
            )

            // Screenshots Section (Before & After)
            Text(
                text = "صور الشارت (قبل الدخول وبعد الإغلاق)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Before Chart Image
                ChartImagePickerBox(
                    title = "شارت قبل الدخول",
                    uriString = beforeChartUri,
                    onPickImage = {
                        beforeImagePicker.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    onRemove = { beforeChartUri = null },
                    modifier = Modifier.weight(1f)
                )

                // After Chart Image
                ChartImagePickerBox(
                    title = "شارت بعد الخروج",
                    uriString = afterChartUri,
                    onPickImage = {
                        afterImagePicker.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    onRemove = { afterChartUri = null },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun ChartImagePickerBox(
    title: String,
    uriString: String?,
    onPickImage: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(130.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onPickImage),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
    ) {
        if (uriString != null) {
            Box(modifier = Modifier.fillMaxSize()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(uriString)
                        .crossfade(true)
                        .build(),
                    contentDescription = title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(28.dp)
                        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "حذف الصورة",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AddPhotoAlternate,
                    contentDescription = title,
                    tint = GoldPrimary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
