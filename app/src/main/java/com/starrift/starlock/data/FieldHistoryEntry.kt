package com.starrift.starlock.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Bir field'a yapılan tek bir değişikliğin bağımsız kaydı.
 * fieldId'ye foreign key/cascade YOK: field soft/hard silinse bile
 * bu kayıtlar kalıcı kalır (Current/Deleted ayrımı sorgu zamanında yapılır).
 */
enum class FieldChangeType {
    CREATED,
    FIELD_ONLY,
    VALUE_ONLY,
    BOTH
}

@Entity(
    tableName = "field_history",
    indices = [Index("fieldId"), Index("accountId")]
)
data class FieldHistoryEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fieldId: Long,
    val accountId: Long,
    val changeType: FieldChangeType,
    val oldLabel: String?,
    val newLabel: String?,
    val oldValue: String?,
    val newValue: String?,
    val timestamp: Long
)
