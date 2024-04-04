package com.example.pbd_jwr.data.entity

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.pbd_jwr.converters.CategoryConverter
import com.example.pbd_jwr.data.model.Category

@Parcelize
@Entity(tableName = "transaction")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val email: String,
    val title: String,
    @TypeConverters(CategoryConverter::class)
    val category: Category,
    val amount: Double,
    val latitude: Double,
    val longitude: Double,
    val date: Long
): Parcelable