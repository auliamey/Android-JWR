package com.example.pbd_jwr.converters

import androidx.room.TypeConverter
import com.example.pbd_jwr.data.model.Category

class CategoryConverter {
    @TypeConverter
    fun fromString(value: String): Category {
        return when (value) {
            "INCOME" -> Category.INCOME
            "EXPENSE" -> Category.EXPENSE
            else -> throw IllegalArgumentException("Unknown category: $value")
        }
    }

    @TypeConverter
    fun toString(category: Category): String {
        return category.name
    }
}