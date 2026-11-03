package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.Trade
import kotlinx.coroutines.flow.Flow

@Dao
interface TradeDao {
    @Query("SELECT * FROM trades WHERE accountId = :accountId ORDER BY entryDate DESC")
    fun getTradesForAccount(accountId: Long): Flow<List<Trade>>

    @Query("SELECT * FROM trades WHERE accountId = :accountId AND entryDate >= :startDate AND entryDate <= :endDate ORDER BY entryDate DESC")
    fun getTradesByDateRange(accountId: Long, startDate: Long, endDate: Long): Flow<List<Trade>>

    @Query("SELECT * FROM trades WHERE id = :id")
    suspend fun getTradeById(id: Long): Trade?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrade(trade: Trade): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(trades: List<Trade>): List<Long>

    @Update
    suspend fun updateTrade(trade: Trade)

    @Delete
    suspend fun deleteTrade(trade: Trade)

    @Query("DELETE FROM trades WHERE id = :id")
    suspend fun deleteTradeById(id: Long)

    @Query("DELETE FROM trades WHERE accountId = :accountId")
    suspend fun deleteAllTradesForAccount(accountId: Long)
}
