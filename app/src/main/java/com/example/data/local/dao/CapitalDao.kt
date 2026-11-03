package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.CapitalTransaction
import kotlinx.coroutines.flow.Flow

@Dao
interface CapitalDao {
    @Query("SELECT * FROM capital_transactions WHERE accountId = :accountId ORDER BY date DESC")
    fun getTransactionsForAccount(accountId: Long): Flow<List<CapitalTransaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: CapitalTransaction): Long

    @Delete
    suspend fun deleteTransaction(transaction: CapitalTransaction)

    @Query("DELETE FROM capital_transactions WHERE accountId = :accountId")
    suspend fun deleteAllForAccount(accountId: Long)
}
