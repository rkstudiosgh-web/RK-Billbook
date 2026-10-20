package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cameraman_records")
data class CameramanRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val cameramanName: String,
    val cameramanPhone: String = "",
    val bookingId: Long = 0,
    val customerName: String = "",
    val rasamName: String = "",
    val eventDate: String = "", // YYYY-MM-DD
    val cameraType: String = "Video Camera", // Video Camera, DSLR, Drone, Gimbal
    val cameraOwnership: String = "Studio Camera", // Studio Camera, Cameraman Own
    val routeNote: String = "", // Kahan se ➜ Kahan tak
    val feeAmount: Double = 0.0,
    val paidAmount: Double = 0.0,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    fun remainingBalance(): Double = maxOf(0.0, feeAmount - paidAmount)
    fun isFullyPaid(): Boolean = paidAmount >= feeAmount && feeAmount > 0
}

data class CameramanSummary(
    val name: String,
    val phone: String,
    val totalEvents: Int,
    val totalFee: Double,
    val totalPaid: Double,
    val totalBaqaya: Double
)
