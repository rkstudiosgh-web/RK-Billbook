package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

data class PaymentInstallment(
    val id: String = UUID.randomUUID().toString(),
    val date: String,
    val amount: Double,
    val note: String = "Jama Next"
)

data class Rasam(
    val id: String = UUID.randomUUID().toString(),
    val name: String, // Mehndi, Barat, Dawat / Walima, Paat / Haldi, Sangeet
    val date: String, // YYYY-MM-DD
    val time: String, // 08:00 PM
    val venue: String = "",
    val cameras: List<String> = listOf("Video Camera", "DSLR"), // Video Camera, DSLR, Drone, Gimbal
    val routeNote: String = "" // Kahan se ➜ Kahan tak
)

@Entity(tableName = "bookings")
data class Booking(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val bookingNumber: String, // #001, #002...
    val customerName: String,
    val customerMobile: String,
    val bookingDate: String, // Date when booking was made
    val address: String = "",
    val totalAmount: Double = 0.0,
    val advanceAmount: Double = 0.0,
    val rasams: List<Rasam> = emptyList(),
    val payments: List<PaymentInstallment> = emptyList(),
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    fun totalPaid(): Double = advanceAmount + payments.sumOf { it.amount }
    fun remainingBalance(): Double = maxOf(0.0, totalAmount - totalPaid())
    fun isFullyPaid(): Boolean = totalPaid() >= totalAmount && totalAmount > 0
    fun paymentStatus(): String = when {
        totalPaid() >= totalAmount && totalAmount > 0 -> "PAID"
        totalPaid() > 0 -> "PARTIAL"
        else -> "UNPAID"
    }
}
