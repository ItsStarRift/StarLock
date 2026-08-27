package com.starrift.starlock.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Bir hesabın içindeki tek bir "terim = değer" satırı.
 * Örn: "Email" = "ornek@gmail.com" ya da özel terim "Google ile giriş" = "ornek@gmail.com".
 */
@Entity(
    tableName = "account_fields",
    foreignKeys = [
        ForeignKey(
            entity = AccountItem::class,
            parentColumns = ["id"],
            childColumns = ["accountId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("accountId")]
)
data class AccountField(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val accountId: Long,
    /** Örn: "Email", "Şifre" veya kullanıcının yazdığı özel terim "Google ile giriş" */
    val label: String,
    val value: String,
    val isCustomLabel: Boolean,
    val orderIndex: Int,
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null,
    val isArchived: Boolean = false,
    val archivedAt: Long? = null,
    val isCensored: Boolean = false
)
