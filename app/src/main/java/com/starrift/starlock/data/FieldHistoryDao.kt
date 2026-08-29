package com.starrift.starlock.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FieldHistoryDao {

    @Insert
    suspend fun insertEntry(entry: FieldHistoryEntry)

    @Query("""
        SELECT field_history.* FROM field_history
        INNER JOIN account_fields ON field_history.fieldId = account_fields.id
        WHERE field_history.accountId = :accountId AND account_fields.isDeleted = 0
        ORDER BY field_history.timestamp DESC
    """)
    fun getCurrentHistoryForAccount(accountId: Long): Flow<List<FieldHistoryEntry>>

    @Query("""
        SELECT field_history.* FROM field_history
        INNER JOIN account_fields ON field_history.fieldId = account_fields.id
        WHERE field_history.accountId = :accountId AND account_fields.isDeleted = 1
        ORDER BY field_history.timestamp DESC
    """)
    fun getDeletedHistoryForAccount(accountId: Long): Flow<List<FieldHistoryEntry>>

    @Query("DELETE FROM field_history WHERE accountId = :accountId")
    suspend fun clearHistoryForAccount(accountId: Long)
}
