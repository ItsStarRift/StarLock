package com.starrift.starlock.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Bir uygulamaya/oyuna ait tek bir hesap.
 * Örn: WhatsApp altındaki "İş Numaram" hesabı.
 */
@Entity(
    tableName = "accounts",
    foreignKeys = [
        ForeignKey(
            entity = AppItem::class,
            parentColumns = ["id"],
            childColumns = ["appId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("appId")]
)
data class AccountItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val appId: Long,
    val name: String,
    val iconPath: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null,
    val isArchived: Boolean = false,
    val archivedAt: Long? = null
)
