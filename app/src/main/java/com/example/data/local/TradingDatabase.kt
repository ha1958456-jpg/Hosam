package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.AccountDao
import com.example.data.local.dao.CapitalDao
import com.example.data.local.dao.TradeDao
import com.example.data.local.entity.CapitalTransaction
import com.example.data.local.entity.Trade
import com.example.data.local.entity.TradingAccount

@Database(
    entities = [
        TradingAccount::class,
        Trade::class,
        CapitalTransaction::class
    ],
    version = 1,
    exportSchema = false
)
abstract class TradingDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun tradeDao(): TradeDao
    abstract fun capitalDao(): CapitalDao

    companion object {
        @Volatile
        private var INSTANCE: TradingDatabase? = null

        fun getDatabase(context: Context): TradingDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TradingDatabase::class.java,
                    "trading_journal_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
