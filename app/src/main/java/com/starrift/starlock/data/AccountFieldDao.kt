package com.starrift.starlock.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

data class AccountFieldWithAccountName(
    val id: Long,
    val accountId: Long,
    val label: String,
    val value: String,
    val isCustomLabel: Boolean,
    val orderIndex: Int,
    val isDeleted: Boolean,
    val deletedAt: Long?,
    val isArchived: Boolean,
    val archivedAt: Long?,
    val accountName: String
)

@Dao
interface AccountFieldDao {

    @Query("SELECT * FROM account_fields WHERE accountId = :accountId AND isDeleted = 0 AND isArchived = 0 ORDER BY orderIndex ASC")
    fun getFieldsForAccount(accountId: Long): Flow<List<AccountField>>

    @Insert
    suspend fun insertField(field: AccountField): Long

    @Query("SELECT account_fields.*, accounts.name AS accountName FROM account_fields INNER JOIN accounts ON account_fields.accountId = accounts.id WHERE account_fields.isDeleted = 1 AND accounts.isDeleted = 0 ORDER BY account_fields.deletedAt DESC")
    fun getDeletedFields(): Flow<List<AccountFieldWithAccountName>>

    @Query("UPDATE account_fields SET isDeleted = 1, deletedAt = :deletedAt WHERE id = :fieldId")
    suspend fun softDeleteField(fieldId: Long, deletedAt: Long)

    @Query("UPDATE account_fields SET isDeleted = 1, deletedAt = :deletedAt WHERE accountId = :accountId")
    suspend fun softDeleteFieldsByAccountId(accountId: Long, deletedAt: Long)

    @Query("UPDATE account_fields SET isDeleted = 1, deletedAt = :deletedAt WHERE accountId IN (SELECT id FROM accounts WHERE appId = :appId)")
    suspend fun softDeleteFieldsByAppId(appId: Long, deletedAt: Long)

    @Query("UPDATE account_fields SET isDeleted = 0, deletedAt = NULL WHERE id = :fieldId")
    suspend fun restoreField(fieldId: Long)

    @Query("UPDATE account_fields SET isDeleted = 0, deletedAt = NULL WHERE accountId = :accountId")
    suspend fun restoreFieldsByAccountId(accountId: Long)

    @Query("UPDATE account_fields SET isDeleted = 0, deletedAt = NULL WHERE accountId IN (SELECT id FROM accounts WHERE appId = :appId)")
    suspend fun restoreFieldsByAppId(appId: Long)

    @Query("DELETE FROM account_fields WHERE id = :fieldId")
    suspend fun permanentlyDeleteField(fieldId: Long)

    @Query("UPDATE account_fields SET isArchived = 1, archivedAt = :archivedAt WHERE id = :fieldId")
    suspend fun archiveField(fieldId: Long, archivedAt: Long)

    @Query("UPDATE account_fields SET isArchived = 0, archivedAt = NULL WHERE id = :fieldId")
    suspend fun unarchiveField(fieldId: Long)

    @Query("SELECT account_fields.*, accounts.name AS accountName FROM account_fields INNER JOIN accounts ON account_fields.accountId = accounts.id WHERE account_fields.isArchived = 1 AND account_fields.isDeleted = 0 AND accounts.isDeleted = 0 ORDER BY account_fields.archivedAt DESC")
    fun getArchivedFields(): Flow<List<AccountFieldWithAccountName>>

    @Update
    suspend fun updateField(field: AccountField)

    @Update
    suspend fun updateFields(fields: List<AccountField>)

    @Delete
    suspend fun deleteField(field: AccountField)

    @Query("SELECT * FROM account_fields")
    suspend fun getAllFieldsOnce(): List<AccountField>

    @Insert
    suspend fun insertFields(fields: List<AccountField>)
}
