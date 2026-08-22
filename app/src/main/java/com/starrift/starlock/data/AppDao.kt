package com.starrift.starlock.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

data class AppWithAccountCount(
    val id: Long,
    val name: String,
    val category: AppCategory,
    val iconPath: String?,
    val accountCount: Int,
    val isFavorite: Boolean
)

@Dao
interface AppDao {

    @Query(
        """
        SELECT apps.id AS id, apps.name AS name, apps.category AS category, apps.iconPath AS iconPath,
               (SELECT COUNT(*) FROM accounts WHERE accounts.appId = apps.id AND accounts.isDeleted = 0) AS accountCount,
                apps.isFavorite AS isFavorite
        FROM apps
        WHERE apps.isDeleted = 0 AND apps.isArchived = 0
        ORDER BY apps.name COLLATE NOCASE ASC
        """
    )
    fun getAllAppsWithCount(): Flow<List<AppWithAccountCount>>

    @Query("SELECT * FROM apps WHERE id = :appId AND isDeleted = 0")
    fun getAppById(appId: Long): Flow<AppItem?>

    @Insert
    suspend fun insertApp(app: AppItem): Long

    @Update
    suspend fun updateApp(app: AppItem)

    @Delete
    suspend fun deleteApp(app: AppItem)

    @Query("SELECT * FROM apps")
    suspend fun getAllAppsOnce(): List<AppItem>

    @Insert
    suspend fun insertApps(apps: List<AppItem>)

    @Query("DELETE FROM apps")
    suspend fun clearApps()

    @Query("SELECT * FROM apps WHERE isDeleted = 1 ORDER BY deletedAt DESC")
    fun getDeletedApps(): Flow<List<AppItem>>

    @Query("UPDATE apps SET isDeleted = 1, deletedAt = :deletedAt WHERE id = :appId")
    suspend fun softDeleteApp(appId: Long, deletedAt: Long)

    @Query("UPDATE apps SET isDeleted = 0, deletedAt = NULL WHERE id = :appId")
    suspend fun restoreApp(appId: Long)

    @Query("DELETE FROM apps WHERE id = :appId")
    suspend fun permanentlyDeleteApp(appId: Long)

    @Query("UPDATE apps SET isFavorite = :isFavorite WHERE id = :appId")
    suspend fun setFavorite(appId: Long, isFavorite: Boolean)

    @Query("SELECT * FROM apps WHERE isArchived = 1 AND isDeleted = 0 ORDER BY archivedAt DESC")
    fun getArchivedApps(): Flow<List<AppItem>>

    @Query("UPDATE apps SET isArchived = 1, archivedAt = :archivedAt WHERE id = :appId")
    suspend fun archiveApp(appId: Long, archivedAt: Long)

    @Query("UPDATE apps SET isArchived = 0 WHERE id = :appId")
    suspend fun unarchiveApp(appId: Long)
}
