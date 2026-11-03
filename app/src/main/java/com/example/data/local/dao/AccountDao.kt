package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.TradingAccount
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {
    @Query("SELECT * FROM trading_accounts ORDER BY id ASC")
    fun getAllAccounts(): Flow<List<TradingAccount>>

    @Query("SELECT * FROM trading_accounts WHERE id = :id")
    suspend fun getAccountById(id: Long): TradingAccount?

    @Query("SELECT * FROM trading_accounts ORDER BY id ASC LIMIT 1")
    suspend fun getFirstAccount(): TradingAccount?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: TradingAccount): Long

    @Update
    suspend fun updateAccount(account: TradingAccount)

    @Delete
    suspend fun deleteAccount(account: TradingAccount)
}
