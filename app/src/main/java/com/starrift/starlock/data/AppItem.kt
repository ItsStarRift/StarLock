package com.starrift.starlock.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Ana ekranda listelenen her bir "uygulama" ya da "oyun" satırı.
 * Örn: WhatsApp, Among Us.
 */
@Entity(tableName = "apps")
data class AppItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: AppCategory,
    /** Cihazdaki dahili depolamada saklanan ikon dosyasının tam yolu, yoksa null. */
    val iconPath: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null,
    val isArchived: Boolean = false,
    val archivedAt: Long? = null
)
