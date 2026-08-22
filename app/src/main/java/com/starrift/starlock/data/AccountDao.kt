package com.starrift.starlock.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

data class AccountWithAppName(
    val id: Long,
    val appId: Long,
    val name: String,
    val iconPath: String?,
    val createdAt: Long,
    val isFavorite: Boolean,
    val isDeleted: Boolean,
    val deletedAt: Long?,
    val archivedAt: Long?,
    val appName: String
)

@Dao
interface AccountDao {

    @Query("SELECT * FROM accounts WHERE appId = :appId AND isDeleted = 0 AND isArchived = 0 ORDER BY name COLLATE NOCASE ASC")
    fun getAccountsForApp(appId: Long): Flow<List<AccountItem>>

    @Insert
    suspend fun insertAccount(account: AccountItem): Long

    @Update
    suspend fun updateAccount(account: AccountItem)

    @Delete
    suspend fun deleteAccount(account: AccountItem)

    @Query("SELECT * FROM accounts")
    suspend fun getAllAccountsOnce(): List<AccountItem>

    @Insert
    suspend fun insertAccounts(accounts: List<AccountItem>)

    @Query("SELECT accounts.*, apps.name AS appName FROM accounts INNER JOIN apps ON accounts.appId = apps.id WHERE accounts.isDeleted = 1 AND apps.isDeleted = 0 ORDER BY accounts.deletedAt DESC")
    fun getDeletedAccounts(): Flow<List<AccountWithAppName>>

    @Query("UPDATE accounts SET isDeleted = 1, deletedAt = :deletedAt WHERE id = :accountId")
    suspend fun softDeleteAccount(accountId: Long, deletedAt: Long)

    @Query("UPDATE accounts SET isDeleted = 1, deletedAt = :deletedAt WHERE appId = :appId")
    suspend fun softDeleteAccountsByAppId(appId: Long, deletedAt: Long)

    @Query("UPDATE accounts SET isDeleted = 0, deletedAt = NULL WHERE id = :accountId")
    suspend fun restoreAccount(accountId: Long)

    @Query("DELETE FROM accounts WHERE id = :accountId")
    suspend fun permanentlyDeleteAccount(accountId: Long)

    @Query("UPDATE accounts SET isFavorite = :isFavorite WHERE id = :accountId")
    suspend fun setFavorite(accountId: Long, isFavorite: Boolean)

    @Query("UPDATE accounts SET isDeleted = 0, deletedAt = NULL WHERE appId = :appId")
    suspend fun restoreAccountsByAppId(appId: Long)

    @Query("SELECT accounts.*, apps.name AS appName FROM accounts INNER JOIN apps ON accounts.appId = apps.id WHERE accounts.isArchived = 1 AND accounts.isDeleted = 0 ORDER BY accounts.archivedAt DESC")
    fun getArchivedAccounts(): Flow<List<AccountWithAppName>>

    @Query("UPDATE accounts SET isArchived = 1, archivedAt = :archivedAt WHERE id = :accountId")
    suspend fun archiveAccount(accountId: Long, archivedAt: Long)

    @Query("UPDATE accounts SET isArchived = 0 WHERE id = :accountId")
    suspend fun unarchiveAccount(accountId: Long)
}
