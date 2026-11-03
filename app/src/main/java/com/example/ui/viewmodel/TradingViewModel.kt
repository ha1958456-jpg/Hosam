package com.example.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.TradingDatabase
import com.example.data.local.TradingRepository
import com.example.data.local.entity.CapitalTransaction
import com.example.data.local.entity.Trade
import com.example.data.local.entity.TradingAccount
import com.example.data.preferences.TradingPreferences
import com.example.domain.calculations.TradingCalculations
import com.example.domain.model.StatisticsCalculator
import com.example.domain.model.TimeFilter
import com.example.domain.model.TimePeriod
import com.example.domain.model.TradeStatistics
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

enum class TradeSortOrder(val titleArabic: String) {
    NEWEST_FIRST("الأحدث أولاً"),
    OLDEST_FIRST("الأقدم أولاً"),
    HIGHEST_PROFIT("الأعلى ربحاً"),
    BIGGEST_LOSS("الأكثر خسارة")
}

data class TradeFilterCriteria(
    val timeFilter: TimeFilter = TimeFilter(TimePeriod.ALL),
    val searchQuery: String = "",
    val category: String? = null,
    val orderType: String? = null,
    val status: String? = null,
    val sortOrder: TradeSortOrder = TradeSortOrder.NEWEST_FIRST
)

data class CapitalSummary(
    val initialBalance: Double = 10000.0,
    val totalDeposits: Double = 0.0,
    val totalWithdrawals: Double = 0.0,
    val realizedPnl: Double = 0.0,
    val currentBalance: Double = 10000.0,
    val floatingPnl: Double = 0.0,
    val equity: Double = 10000.0,
    val usedMargin: Double = 0.0,
    val freeMargin: Double = 10000.0,
    val marginLevelPercent: Double = 0.0
)

class TradingViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TradingRepository
    private val preferences: TradingPreferences = TradingPreferences(application)

    init {
        val db = TradingDatabase.getDatabase(application)
        repository = TradingRepository(
            db.accountDao(),
            db.tradeDao(),
            db.capitalDao()
        )

        viewModelScope.launch {
            val initialId = repository.seedInitialDataIfEmpty()
            val savedActiveId = preferences.activeAccountId.first()
            if (savedActiveId == null || savedActiveId == 0L) {
                preferences.setActiveAccountId(initialId)
                _activeAccountId.value = initialId
            } else {
                _activeAccountId.value = savedActiveId
            }
        }
    }

    // Active Account ID
    private val _activeAccountId = MutableStateFlow<Long?>(null)
    val activeAccountId: StateFlow<Long?> = _activeAccountId.asStateFlow()

    val accounts: StateFlow<List<TradingAccount>> = repository.accounts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Account Object
    val activeAccount: StateFlow<TradingAccount?> = combine(accounts, activeAccountId) { accts, id ->
        accts.firstOrNull { it.id == id } ?: accts.firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Raw Trades for Active Account
    private val _rawTrades = MutableStateFlow<List<Trade>>(emptyList())
    val rawTrades: StateFlow<List<Trade>> = _rawTrades.asStateFlow()

    init {
        viewModelScope.launch {
            activeAccount.collect { acct ->
                if (acct != null) {
                    repository.getTradesForAccount(acct.id).collect { trades ->
                        _rawTrades.value = trades
                    }
                }
            }
        }
    }

    // Capital Transactions
    private val _rawTransactions = MutableStateFlow<List<CapitalTransaction>>(emptyList())
    val rawTransactions: StateFlow<List<CapitalTransaction>> = _rawTransactions.asStateFlow()

    init {
        viewModelScope.launch {
            activeAccount.collect { acct ->
                if (acct != null) {
                    repository.getTransactionsForAccount(acct.id).collect { txs ->
                        _rawTransactions.value = txs
                    }
                }
            }
        }
    }

    // Filters & Sorting
    val timeFilter = MutableStateFlow(TimeFilter(TimePeriod.ALL))
    val searchQuery = MutableStateFlow("")
    val categoryFilter = MutableStateFlow<String?>(null) // FOREX, GOLD, etc.
    val orderTypeFilter = MutableStateFlow<String?>(null) // BUY, SELL
    val statusFilter = MutableStateFlow<String?>(null) // OPEN, CLOSED
    val sortOrder = MutableStateFlow(TradeSortOrder.NEWEST_FIRST)

    // Filter Criteria flow combining filter params
    private val filterCriteria = combine(
        combine(timeFilter, searchQuery, categoryFilter) { t, q, c -> Triple(t, q, c) },
        combine(orderTypeFilter, statusFilter, sortOrder) { o, s, so -> Triple(o, s, so) }
    ) { (t, q, c), (o, s, so) ->
        TradeFilterCriteria(
            timeFilter = t,
            searchQuery = q,
            category = c,
            orderType = o,
            status = s,
            sortOrder = so
        )
    }

    // Filtered Trades
    val filteredTrades: StateFlow<List<Trade>> = combine(
        _rawTrades,
        filterCriteria
    ) { trades, criteria ->
        val range = criteria.timeFilter.getRange()

        trades.filter { trade ->
            // Date filter
            val matchesDate = if (range != null) {
                val tradeTime = trade.entryDate
                tradeTime in range.first..range.second
            } else true

            // Search query
            val matchesQuery = if (criteria.searchQuery.isNotBlank()) {
                trade.symbol.contains(criteria.searchQuery, ignoreCase = true) ||
                        trade.notes.contains(criteria.searchQuery, ignoreCase = true)
            } else true

            // Category filter
            val matchesCategory = if (criteria.category != null && criteria.category != "ALL") {
                trade.category.equals(criteria.category, ignoreCase = true)
            } else true

            // Order Type filter
            val matchesOrderType = if (criteria.orderType != null && criteria.orderType != "ALL") {
                trade.orderType.equals(criteria.orderType, ignoreCase = true)
            } else true

            // Status filter
            val matchesStatus = if (criteria.status != null && criteria.status != "ALL") {
                trade.status.equals(criteria.status, ignoreCase = true)
            } else true

            matchesDate && matchesQuery && matchesCategory && matchesOrderType && matchesStatus
        }.sortedWith(
            Comparator { a, b ->
                when (criteria.sortOrder) {
                    TradeSortOrder.NEWEST_FIRST -> b.entryDate.compareTo(a.entryDate)
                    TradeSortOrder.OLDEST_FIRST -> a.entryDate.compareTo(b.entryDate)
                    TradeSortOrder.HIGHEST_PROFIT -> b.profit.compareTo(a.profit)
                    TradeSortOrder.BIGGEST_LOSS -> a.profit.compareTo(b.profit)
                }
            }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Statistics based on filtered trades
    val statistics: StateFlow<TradeStatistics> = combine(
        filteredTrades,
        activeAccount
    ) { trades, acct ->
        val initBal = acct?.initialBalance ?: 10000.0
        StatisticsCalculator.calculate(trades, initBal)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TradeStatistics())

    // Capital Summary
    val capitalSummary: StateFlow<CapitalSummary> = combine(
        activeAccount,
        _rawTrades,
        _rawTransactions
    ) { acct, trades, txs ->
        val initial = acct?.initialBalance ?: 10000.0
        val deposits = txs.filter { it.type.equals("DEPOSIT", ignoreCase = true) }.sumOf { it.amount }
        val withdrawals = txs.filter { it.type.equals("WITHDRAWAL", ignoreCase = true) }.sumOf { it.amount }

        val closedTrades = trades.filter { it.status.equals("CLOSED", ignoreCase = true) }
        val openTrades = trades.filter { it.status.equals("OPEN", ignoreCase = true) }

        val realizedPnl = closedTrades.sumOf { it.profit }
        val currentBal = initial + deposits - withdrawals + realizedPnl

        val floatingPnl = openTrades.sumOf { it.profit }
        val equity = currentBal + floatingPnl

        val usedMargin = openTrades.sumOf { it.marginUsed }
        val freeMargin = equity - usedMargin
        val marginLevel = if (usedMargin > 0.0) (equity / usedMargin) * 100.0 else 0.0

        CapitalSummary(
            initialBalance = (initial * 100).roundToInt() / 100.0,
            totalDeposits = (deposits * 100).roundToInt() / 100.0,
            totalWithdrawals = (withdrawals * 100).roundToInt() / 100.0,
            realizedPnl = (realizedPnl * 100).roundToInt() / 100.0,
            currentBalance = (currentBal * 100).roundToInt() / 100.0,
            floatingPnl = (floatingPnl * 100).roundToInt() / 100.0,
            equity = (equity * 100).roundToInt() / 100.0,
            usedMargin = (usedMargin * 100).roundToInt() / 100.0,
            freeMargin = (freeMargin * 100).roundToInt() / 100.0,
            marginLevelPercent = (marginLevel * 10).roundToInt() / 10.0
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CapitalSummary())

    // Theme & PIN State
    val isDarkMode: StateFlow<Boolean> = preferences.isDarkMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val isPinEnabled: StateFlow<Boolean> = preferences.isPinEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val pinCode: StateFlow<String> = preferences.pinCode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    private val _isUnlocked = MutableStateFlow(false)
    val isUnlocked: StateFlow<Boolean> = _isUnlocked.asStateFlow()

    fun unlockApp() {
        _isUnlocked.value = true
    }

    fun lockApp() {
        _isUnlocked.value = false
    }

    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            preferences.setDarkMode(enabled)
        }
    }

    fun setPin(enabled: Boolean, pin: String) {
        viewModelScope.launch {
            preferences.setPin(enabled, pin)
            if (enabled) {
                _isUnlocked.value = true
            }
        }
    }

    // Account Actions
    fun switchAccount(accountId: Long) {
        viewModelScope.launch {
            preferences.setActiveAccountId(accountId)
            _activeAccountId.value = accountId
        }
    }

    fun createAccount(name: String, currency: String, initialBalance: Double) {
        viewModelScope.launch {
            val id = repository.insertAccount(
                TradingAccount(
                    name = name,
                    currency = currency,
                    initialBalance = initialBalance
                )
            )
            switchAccount(id)
        }
    }

    fun updateAccount(account: TradingAccount) {
        viewModelScope.launch {
            repository.updateAccount(account)
        }
    }

    fun deleteAccount(account: TradingAccount) {
        viewModelScope.launch {
            repository.deleteAccount(account)
            val first = repository.getFirstAccount()
            if (first != null) {
                switchAccount(first.id)
            }
        }
    }

    // Trade Actions
    fun saveTrade(trade: Trade) {
        viewModelScope.launch {
            val accountId = activeAccountId.value ?: repository.getFirstAccount()?.id ?: 1L
            val tradeToSave = trade.copy(accountId = accountId)
            if (trade.id == 0L) {
                repository.insertTrade(tradeToSave)
            } else {
                repository.updateTrade(tradeToSave)
            }
        }
    }

    fun deleteTrade(trade: Trade) {
        viewModelScope.launch {
            repository.deleteTrade(trade)
        }
    }

    // Capital Transactions
    fun addDeposit(amount: Double, notes: String = "") {
        viewModelScope.launch {
            val accountId = activeAccountId.value ?: return@launch
            repository.insertCapitalTransaction(
                CapitalTransaction(
                    accountId = accountId,
                    type = "DEPOSIT",
                    amount = amount,
                    notes = notes.ifBlank { "إيداع نقدي" }
                )
            )
        }
    }

    fun addWithdrawal(amount: Double, notes: String = "") {
        viewModelScope.launch {
            val accountId = activeAccountId.value ?: return@launch
            repository.insertCapitalTransaction(
                CapitalTransaction(
                    accountId = accountId,
                    type = "WITHDRAWAL",
                    amount = amount,
                    notes = notes.ifBlank { "سحب أرباح" }
                )
            )
        }
    }

    fun deleteCapitalTransaction(tx: CapitalTransaction) {
        viewModelScope.launch {
            repository.deleteCapitalTransaction(tx)
        }
    }

    // Export Trades to CSV
    fun exportTradesToCsv(uri: Uri): Boolean {
        return try {
            val context = getApplication<Application>()
            val trades = _rawTrades.value
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                OutputStreamWriter(outputStream).use { writer ->
                    writer.write("ID,Symbol,Category,Type,EntryPrice,ExitPrice,Lots,PnL,Status,EntryDate,Notes\n")
                    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
                    for (t in trades) {
                        writer.write(
                            "${t.id},${t.symbol},${t.category},${t.orderType},${t.entryPrice},${t.exitPrice ?: ""},${t.lotSize},${t.profit},${t.status},${sdf.format(Date(t.entryDate))},\"${t.notes.replace("\"", "\"\"")}\"\n"
                        )
                    }
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // Import Trades from CSV
    fun importTradesFromCsv(uri: Uri): Int {
        return try {
            val context = getApplication<Application>()
            val accountId = activeAccountId.value ?: 1L
            val imported = mutableListOf<Trade>()
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    var line = reader.readLine() // Header
                    while (reader.readLine().also { line = it } != null) {
                        val tokens = line!!.split(",")
                        if (tokens.size >= 8) {
                            val symbol = tokens[1].trim()
                            val category = tokens[2].trim()
                            val orderType = tokens[3].trim()
                            val entryPrice = tokens[4].toDoubleOrNull() ?: 0.0
                            val exitPrice = tokens[5].toDoubleOrNull()
                            val lots = tokens[6].toDoubleOrNull() ?: 0.1
                            val pnl = tokens[7].toDoubleOrNull() ?: 0.0
                            val status = if (tokens.size > 8) tokens[8].trim() else "CLOSED"

                            imported.add(
                                Trade(
                                    accountId = accountId,
                                    symbol = symbol,
                                    category = category,
                                    orderType = orderType,
                                    entryDate = System.currentTimeMillis(),
                                    entryPrice = entryPrice,
                                    exitPrice = exitPrice,
                                    lotSize = lots,
                                    profit = pnl,
                                    status = status
                                )
                            )
                        }
                    }
                }
            }
            if (imported.isNotEmpty()) {
                viewModelScope.launch {
                    repository.insertAllTrades(imported)
                }
            }
            imported.size
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }
    }
}
