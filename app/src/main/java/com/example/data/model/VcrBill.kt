package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

data class CassetteItem(
    val id: String = UUID.randomUUID().toString(),
    val label: String, // e.g. "Family Shadi 1999"
    val type: String = "VHS Big Tape", // VHS Big Tape, Mini DV, Hi8, Audio Cassette
    val quantity: Int = 1,
    val rate: Double = 350.0,
    val amount: Double = 350.0
)

@Entity(tableName = "vcr_bills")
data class VcrBill(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val billNumber: String, // #VCR-001
    val customerName: String,
    val customerPhone: String,
    val date: String, // YYYY-MM-DD
    val cassettes: List<CassetteItem> = emptyList(),
    val transferModes: List<String> = listOf("Customer USB"), // Mobile transfer, Customer USB, Studio USB
    val tapePhotoUri: String? = null,
    val totalAmount: Double = 0.0,
    val advanceAmount: Double = 0.0,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    fun calculatedTotal(): Double {
        return if (totalAmount > 0) totalAmount else cassettes.sumOf { it.amount }
    }
    fun remainingBalance(): Double = maxOf(0.0, calculatedTotal() - advanceAmount)
    fun isPaid(): Boolean = advanceAmount >= calculatedTotal() && calculatedTotal() > 0
}
