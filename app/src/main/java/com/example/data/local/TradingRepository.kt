package com.example.data.local

import com.example.data.local.dao.AccountDao
import com.example.data.local.dao.CapitalDao
import com.example.data.local.dao.TradeDao
import com.example.data.local.entity.CapitalTransaction
import com.example.data.local.entity.Trade
import com.example.data.local.entity.TradingAccount
import kotlinx.coroutines.flow.Flow

class TradingRepository(
    private val accountDao: AccountDao,
    private val tradeDao: TradeDao,
    private val capitalDao: CapitalDao
) {
    val accounts: Flow<List<TradingAccount>> = accountDao.getAllAccounts()

    fun getTradesForAccount(accountId: Long): Flow<List<Trade>> =
        tradeDao.getTradesForAccount(accountId)

    fun getTransactionsForAccount(accountId: Long): Flow<List<CapitalTransaction>> =
        capitalDao.getTransactionsForAccount(accountId)

    suspend fun getTradeById(id: Long): Trade? = tradeDao.getTradeById(id)

    suspend fun insertTrade(trade: Trade): Long = tradeDao.insertTrade(trade)

    suspend fun updateTrade(trade: Trade) = tradeDao.updateTrade(trade)

    suspend fun deleteTrade(trade: Trade) = tradeDao.deleteTrade(trade)

    suspend fun deleteTradeById(id: Long) = tradeDao.deleteTradeById(id)

    suspend fun insertAllTrades(trades: List<Trade>) = tradeDao.insertAll(trades)

    suspend fun deleteAllTradesForAccount(accountId: Long) =
        tradeDao.deleteAllTradesForAccount(accountId)

    suspend fun getFirstAccount(): TradingAccount? = accountDao.getFirstAccount()

    suspend fun getAccountById(id: Long): TradingAccount? = accountDao.getAccountById(id)

    suspend fun insertAccount(account: TradingAccount): Long = accountDao.insertAccount(account)

    suspend fun updateAccount(account: TradingAccount) = accountDao.updateAccount(account)

    suspend fun deleteAccount(account: TradingAccount) = accountDao.deleteAccount(account)

    suspend fun insertCapitalTransaction(tx: CapitalTransaction): Long =
        capitalDao.insertTransaction(tx)

    suspend fun deleteCapitalTransaction(tx: CapitalTransaction) =
        capitalDao.deleteTransaction(tx)

    suspend fun seedInitialDataIfEmpty(): Long {
        val existing = accountDao.getFirstAccount()
        if (existing != null) return existing.id

        // Create Default Trading Account
        val accountId = accountDao.insertAccount(
            TradingAccount(
                name = "حساب الذهب والعملات الأساسي",
                currency = "USD",
                initialBalance = 10000.0
            )
        )

        // Seed some realistic trades over the past 2 weeks across Gold, Forex, Crypto, Indices
        val now = System.currentTimeMillis()
        val day = 86400000L

        val initialTrades = listOf(
            Trade(
                accountId = accountId,
                symbol = "XAUUSD",
                category = "GOLD",
                orderType = "BUY",
                entryDate = now - 10 * day,
                exitDate = now - 10 * day + 3600000L * 4,
                entryPrice = 2715.50,
                exitPrice = 2732.00,
                lotSize = 0.50,
                leverage = 100,
                marginUsed = 1357.75,
                stopLoss = 2705.00,
                takeProfit = 2735.00,
                commission = 3.50,
                swap = 0.0,
                profit = 821.50,
                status = "CLOSED",
                notes = "كسر منطقة مقاومة على فريم 4 ساعات مع زخم صاعد قوي",
                contractSize = 100.0,
                riskRewardRatio = 1.85
            ),
            Trade(
                accountId = accountId,
                symbol = "EURUSD",
                category = "FOREX",
                orderType = "SELL",
                entryDate = now - 8 * day,
                exitDate = now - 8 * day + 3600000L * 2,
                entryPrice = 1.08500,
                exitPrice = 1.08920,
                lotSize = 1.00,
                leverage = 100,
                marginUsed = 1085.00,
                stopLoss = 1.08900,
                takeProfit = 1.07900,
                commission = 7.00,
                swap = -1.50,
                profit = -428.50,
                status = "CLOSED",
                notes = "ضرب وقف الخسارة بعد صدور بيانات التضخم الأمريكية",
                contractSize = 100000.0,
                riskRewardRatio = 1.50
            ),
            Trade(
                accountId = accountId,
                symbol = "BTCUSD",
                category = "CRYPTO",
                orderType = "BUY",
                entryDate = now - 6 * day,
                exitDate = now - 5 * day,
                entryPrice = 67200.0,
                exitPrice = 69850.0,
                lotSize = 0.20,
                leverage = 50,
                marginUsed = 268.80,
                stopLoss = 66000.0,
                takeProfit = 70000.0,
                commission = 5.00,
                swap = -2.00,
                profit = 523.00,
                status = "CLOSED",
                notes = "إعادة اختبار الدعم الرئيسي قبل الموجة الصاعدة",
                contractSize = 1.0,
                riskRewardRatio = 2.33
            ),
            Trade(
                accountId = accountId,
                symbol = "US30",
                category = "INDICES",
                orderType = "BUY",
                entryDate = now - 4 * day,
                exitDate = now - 4 * day + 3600000L * 3,
                entryPrice = 42800.0,
                exitPrice = 43120.0,
                lotSize = 1.00,
                leverage = 100,
                marginUsed = 428.00,
                stopLoss = 42650.0,
                takeProfit = 43200.0,
                commission = 4.00,
                swap = 0.0,
                profit = 316.00,
                status = "CLOSED",
                notes = "افتتاح جلسة نيويورك مع سيولة شرائية مرتفعة",
                contractSize = 1.0,
                riskRewardRatio = 2.67
            ),
            Trade(
                accountId = accountId,
                symbol = "GBPUSD",
                category = "FOREX",
                orderType = "SELL",
                entryDate = now - 3 * day,
                exitDate = now - 3 * day + 3600000L * 5,
                entryPrice = 1.30200,
                exitPrice = 1.29650,
                lotSize = 0.80,
                leverage = 100,
                marginUsed = 1041.60,
                stopLoss = 1.30500,
                takeProfit = 1.29500,
                commission = 5.60,
                swap = 1.20,
                profit = 435.60,
                status = "CLOSED",
                notes = "نموذج الرأس والكتفين على الإطار الزمني لساعة واحدة",
                contractSize = 100000.0,
                riskRewardRatio = 2.33
            ),
            Trade(
                accountId = accountId,
                symbol = "XAUUSD",
                category = "GOLD",
                orderType = "SELL",
                entryDate = now - 1 * day,
                exitDate = now - 1 * day + 3600000L,
                entryPrice = 2748.00,
                exitPrice = 2753.50,
                lotSize = 0.40,
                leverage = 100,
                marginUsed = 1099.20,
                stopLoss = 2753.00,
                takeProfit = 2735.00,
                commission = 2.80,
                swap = 0.0,
                profit = -222.80,
                status = "CLOSED",
                notes = "كسر كاذب للمقاومة لم يكتمل",
                contractSize = 100.0,
                riskRewardRatio = 2.60
            ),
            Trade(
                accountId = accountId,
                symbol = "XAUUSD",
                category = "GOLD",
                orderType = "BUY",
                entryDate = now - 1800000L * 3,
                exitDate = null,
                entryPrice = 2735.20,
                exitPrice = 2740.10,
                lotSize = 0.30,
                leverage = 100,
                marginUsed = 820.56,
                stopLoss = 2724.00,
                takeProfit = 2755.00,
                commission = 2.10,
                swap = 0.0,
                profit = 144.90,
                status = "OPEN",
                notes = "صفقة ذهب مفتوحة حالياً باتجاه القمة السابقة",
                contractSize = 100.0,
                riskRewardRatio = 1.77
            )
        )

        tradeDao.insertAll(initialTrades)

        // Seed an initial deposit transaction
        capitalDao.insertTransaction(
            CapitalTransaction(
                accountId = accountId,
                type = "DEPOSIT",
                amount = 10000.0,
                date = now - 15 * day,
                notes = "إيداع رأس المال الابتدائي"
            )
        )

        return accountId
    }
}
