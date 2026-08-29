package com.starrift.starlock.data

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromCategory(category: AppCategory): String = category.name

    @TypeConverter
    fun toCategory(value: String): AppCategory = AppCategory.valueOf(value)

    @TypeConverter
    fun fromFieldChangeType(type: FieldChangeType): String = type.name

    @TypeConverter
    fun toFieldChangeType(value: String): FieldChangeType = FieldChangeType.valueOf(value)
}
