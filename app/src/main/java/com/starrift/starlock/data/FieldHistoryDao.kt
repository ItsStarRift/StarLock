package com.starrift.starlock.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FieldHistoryDao {

    @Insert
    suspend fun insertEntry(entry: FieldHistoryEntry)

    @Insert
    suspend fun insertAll(entries: List<FieldHistoryEntry>)

    @Query("SELECT * FROM field_history")
    suspend fun getAllHistoryOnce(): List<FieldHistoryEntry>

    @Query("DELETE FROM field_history")
    suspend fun clearAllHistory()

    @Query("""
        SELECT field_history.* FROM field_history
        INNER JOIN account_fields ON field_history.fieldId = account_fields.id
        WHERE field_history.accountId = :accountId AND account_fields.isDeleted = 0 AND account_fields.isArchived = 0
        ORDER BY field_history.timestamp DESC
    """)
    fun getCurrentHistoryForAccount(accountId: Long): Flow<List<FieldHistoryEntry>>

    @Query("""
        SELECT field_history.* FROM field_history
        INNER JOIN account_fields ON field_history.fieldId = account_fields.id
        WHERE field_history.accountId = :accountId AND account_fields.isArchived = 1 AND account_fields.isDeleted = 0
        ORDER BY field_history.timestamp DESC
    """)
    fun getArchivedHistoryForAccount(accountId: Long): Flow<List<FieldHistoryEntry>>

    @Query("""
        SELECT field_history.* FROM field_history
        INNER JOIN account_fields ON field_history.fieldId = account_fields.id
        WHERE field_history.accountId = :accountId AND account_fields.isDeleted = 1
        ORDER BY field_history.timestamp DESC
    """)
    fun getDeletedHistoryForAccount(accountId: Long): Flow<List<FieldHistoryEntry>>

    @Query("DELETE FROM field_history WHERE accountId = :accountId")
    suspend fun clearHistoryForAccount(accountId: Long)

    @Query("""
        DELETE FROM field_history
        WHERE accountId = :accountId AND fieldId IN (
            SELECT id FROM account_fields
            WHERE account_fields.accountId = :accountId AND account_fields.isDeleted = 0 AND account_fields.isArchived = 0
        )
    """)
    suspend fun clearCurrentHistoryForAccount(accountId: Long)

    @Query("""
        DELETE FROM field_history
        WHERE accountId = :accountId AND fieldId IN (
            SELECT id FROM account_fields
            WHERE account_fields.accountId = :accountId AND account_fields.isDeleted = 1
        )
    """)
    suspend fun clearDeletedHistoryForAccount(accountId: Long)

    @Query("""
        DELETE FROM field_history
        WHERE accountId = :accountId AND fieldId IN (
            SELECT id FROM account_fields
            WHERE account_fields.accountId = :accountId AND account_fields.isArchived = 1 AND account_fields.isDeleted = 0
        )
    """)
    suspend fun clearArchivedHistoryForAccount(accountId: Long)
}
