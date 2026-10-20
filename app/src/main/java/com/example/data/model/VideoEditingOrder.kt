package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "video_editing_orders")
data class VideoEditingOrder(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val orderNumber: String, // #ED-001
    val customerName: String,
    val customerPhone: String,
    val date: String, // YYYY-MM-DD
    val videoDuration: String = "3 Hours", // e.g. 3 Hours
    val dataSize: String = "128 GB", // e.g. 128 GB
    val deliveryMedia: List<String> = listOf("Customer USB"), // Customer USB, Studio USB, Portable Disk, Google Drive
    val status: String = "In Progress", // Pending, In Progress, Review, Completed, Delivered
    val totalAmount: Double = 0.0,
    val advanceAmount: Double = 0.0,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    fun remainingBalance(): Double = maxOf(0.0, totalAmount - advanceAmount)
    fun isPaid(): Boolean = advanceAmount >= totalAmount && totalAmount > 0
}

data class EditingSummary(
    val totalOrders: Int,
    val totalBilling: Double,
    val totalAdvance: Double,
    val totalBaqaya: Double
)
