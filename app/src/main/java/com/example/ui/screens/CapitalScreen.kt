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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.CapitalTransaction
import com.example.domain.calculations.TradingCalculations
import com.example.ui.components.StatCard
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.LossRedBright
import com.example.ui.theme.ProfitGreenBright
import com.example.ui.viewmodel.TradingViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CapitalScreen(
    viewModel: TradingViewModel,
    modifier: Modifier = Modifier
) {
    val summary by viewModel.capitalSummary.collectAsStateWithLifecycle()
    val activeAccount by viewModel.activeAccount.collectAsStateWithLifecycle()
    val transactions by viewModel.rawTransactions.collectAsStateWithLifecycle()
    val currency = activeAccount?.currency ?: "$"

    var showDepositDialog by remember { mutableStateOf(false) }
    var showWithdrawDialog by remember { mutableStateOf(false) }
    var amountText by remember { mutableStateOf("") }
    var notesText by remember { mutableStateOf("") }

    val dateFormatter = remember { SimpleDateFormat("yyyy/MM/dd  HH:mm", Locale.US) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("capital_screen")
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Hero Balance & Equity Card
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("capital_hero_card"),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.4f)),
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
                                text = "السيولة الإجمالية (Equity)",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${TradingCalculations.formatNumber(summary.equity)} $currency",
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                                color = GoldPrimary
                            )
                        }

                        // Deposit / Withdraw Action Buttons
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    amountText = ""
                                    notesText = ""
                                    showDepositDialog = true
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = ProfitGreenBright.copy(alpha = 0.2f), contentColor = ProfitGreenBright),
                                modifier = Modifier.testTag("btn_add_deposit")
                            ) {
                                Icon(Icons.Default.ArrowDownward, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("إيداع", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }

                            Button(
                                onClick = {
                                    amountText = ""
                                    notesText = ""
                                    showWithdrawDialog = true
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = LossRedBright.copy(alpha = 0.2f), contentColor = LossRedBright),
                                modifier = Modifier.testTag("btn_add_withdrawal")
                            ) {
                                Icon(Icons.Default.ArrowUpward, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("سحب", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("الرصيد الفعلي (Balance)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${TradingCalculations.formatNumber(summary.currentBalance)} $currency", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("الأرباح العائمة (Floating)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                "${if (summary.floatingPnl >= 0) "+" else ""}${TradingCalculations.formatNumber(summary.floatingPnl)} $currency",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall,
                                color = if (summary.floatingPnl >= 0) ProfitGreenBright else LossRedBright
                            )
                        }
                    }
                }
            }
        }

        // Section: Margin & Capital Management Metrics
        item {
            Text(
                text = "إدارة الهامش ورأس المال",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // Row 1: Initial Balance & Realized PnL
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "رأس المال الابتدائي",
                    value = "${TradingCalculations.formatNumber(summary.initialBalance)} $currency",
                    icon = Icons.Default.Savings,
                    iconTint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "الأرباح المحققة",
                    value = "${if (summary.realizedPnl >= 0) "+" else ""}${TradingCalculations.formatNumber(summary.realizedPnl)} $currency",
                    icon = Icons.Default.Payments,
                    iconTint = if (summary.realizedPnl >= 0) ProfitGreenBright else LossRedBright,
                    valueColor = if (summary.realizedPnl >= 0) ProfitGreenBright else LossRedBright,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Row 2: Used Margin & Free Margin
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "Used Margin (الهامش المستخدم)",
                    value = "${TradingCalculations.formatNumber(summary.usedMargin)} $currency",
                    icon = Icons.Default.Security,
                    iconTint = GoldPrimary,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Free Margin (الهامش المتاح)",
                    value = "${TradingCalculations.formatNumber(summary.freeMargin)} $currency",
                    icon = Icons.Default.AccountBalance,
                    iconTint = ProfitGreenBright,
                    valueColor = ProfitGreenBright,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Row 3: Margin Level % & Deposits/Withdrawals
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "Margin Level (مستوى الهامش)",
                    value = if (summary.usedMargin > 0) "${String.format(Locale.US, "%.1f%%", summary.marginLevelPercent)}" else "100%",
                    subValue = if (summary.usedMargin > 0) "نسبة أمان المحفظة" else "لا توجد مراكز مفتوحة",
                    icon = Icons.Default.Security,
                    iconTint = if (summary.marginLevelPercent > 200 || summary.usedMargin == 0.0) ProfitGreenBright else LossRedBright,
                    valueColor = if (summary.marginLevelPercent > 200 || summary.usedMargin == 0.0) ProfitGreenBright else LossRedBright,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "الإيداعات / السحوبات",
                    value = "+${TradingCalculations.formatNumber(summary.totalDeposits)}",
                    subValue = "سحوبات: -${TradingCalculations.formatNumber(summary.totalWithdrawals)} $currency",
                    icon = Icons.Default.Payments,
                    iconTint = GoldPrimary,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Capital Transactions History
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "سجل الإيداعات والسحوبات",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "${transactions.size} عمليات",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (transactions.isEmpty()) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ) {
                    Box(modifier = Modifier.padding(20.dp), contentAlignment = Alignment.Center) {
                        Text("لا توجد عمليات إيداع أو سحب مسجلة", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        } else {
            items(transactions.size, key = { transactions[it].id }) { index ->
                val tx = transactions[index]
                val isDeposit = tx.type.equals("DEPOSIT", ignoreCase = true)
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isDeposit) ProfitGreenBright.copy(alpha = 0.15f) else LossRedBright.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isDeposit) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                                    contentDescription = null,
                                    tint = if (isDeposit) ProfitGreenBright else LossRedBright,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (isDeposit) "إيداع نقدي" else "سحب أرباح",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(
                                    text = tx.notes.ifBlank { dateFormatter.format(Date(tx.date)) },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${if (isDeposit) "+" else "-"}${TradingCalculations.formatNumber(tx.amount)} $currency",
                                fontWeight = FontWeight.Bold,
                                color = if (isDeposit) ProfitGreenBright else LossRedBright
                            )
                            IconButton(onClick = { viewModel.deleteCapitalTransaction(tx) }) {
                                Icon(Icons.Default.Delete, contentDescription = "حذف", tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(70.dp))
        }
    }

    // Deposit Dialog
    if (showDepositDialog) {
        AlertDialog(
            onDismissRequest = { showDepositDialog = false },
            title = { Text("إيداع رأس مال جديد") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("ملاحظة: الإيداع يزيد رصيد الحساب فقط ولا يُحسب كربح تداول.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it },
                        label = { Text("مبلغ الإيداع ($currency)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth().testTag("input_deposit_amount")
                    )
                    OutlinedTextField(
                        value = notesText,
                        onValueChange = { notesText = it },
                        label = { Text("ملاحظة (اختياري)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = amountText.toDoubleOrNull() ?: 0.0
                        if (amount > 0) {
                            viewModel.addDeposit(amount, notesText)
                        }
                        showDepositDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ProfitGreenBright)
                ) {
                    Text("تأكيد الإيداع", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDepositDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }

    // Withdrawal Dialog
    if (showWithdrawDialog) {
        AlertDialog(
            onDismissRequest = { showWithdrawDialog = false },
            title = { Text("سحب من رأس المال أو الأرباح") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("ملاحظة: السحب ينقص رصيد الحساب فقط ولا يُحسب كخسارة تداول.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it },
                        label = { Text("مبلغ السحب ($currency)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth().testTag("input_withdraw_amount")
                    )
                    OutlinedTextField(
                        value = notesText,
                        onValueChange = { notesText = it },
                        label = { Text("ملاحظة (اختياري)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = amountText.toDoubleOrNull() ?: 0.0
                        if (amount > 0) {
                            viewModel.addWithdrawal(amount, notesText)
                        }
                        showWithdrawDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LossRedBright)
                ) {
                    Text("تأكيد السحب", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showWithdrawDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}
