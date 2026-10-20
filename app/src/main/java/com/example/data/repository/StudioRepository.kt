package com.example.data.repository

import com.example.data.local.StudioDao
import com.example.data.model.Booking
import com.example.data.model.CameramanRecord
import com.example.data.model.CassetteItem
import com.example.data.model.PaymentInstallment
import com.example.data.model.Rasam
import com.example.data.model.VcrBill
import com.example.data.model.VideoEditingOrder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class StudioRepository(private val dao: StudioDao) {

    val allBookings: Flow<List<Booking>> = dao.getAllBookings()
    val allCameramen: Flow<List<CameramanRecord>> = dao.getAllCameramanRecords()
    val allVcrBills: Flow<List<VcrBill>> = dao.getAllVcrBills()
    val allEditingOrders: Flow<List<VideoEditingOrder>> = dao.getAllEditingOrders()

    // Bookings
    suspend fun insertBooking(booking: Booking): Long {
        val id = dao.insertBooking(booking)
        // Auto sync cameraman entries from booking checklist
        syncCameramenForBooking(booking.copy(id = id))
        return id
    }

    suspend fun updateBooking(booking: Booking) {
        dao.updateBooking(booking)
        syncCameramenForBooking(booking)
    }

    suspend fun deleteBooking(booking: Booking) {
        dao.deleteBooking(booking)
    }

    // Auto-sync cameraman assignments from booking checklist
    private suspend fun syncCameramenForBooking(booking: Booking) {
        val existingCameramen = dao.getAllCameramanRecords().first()
        val linked = existingCameramen.filter { it.bookingId == booking.id }

        // If no cameramen yet for this booking, generate placeholder entries based on rasams & cameras
        if (linked.isEmpty() && booking.rasams.isNotEmpty()) {
            val defaultNames = listOf("Rashid Ali", "Kamran Photo", "Waseem Drone", "Shahid Video")
            var nameIdx = 0
            for (rasam in booking.rasams) {
                for (camera in rasam.cameras) {
                    val assignedName = defaultNames[nameIdx % defaultNames.size]
                    nameIdx++
                    val defaultFee = when (camera) {
                        "Drone" -> 6000.0
                        "Gimbal" -> 4500.0
                        "DSLR" -> 5000.0
                        else -> 4000.0
                    }
                    val record = CameramanRecord(
                        cameramanName = assignedName,
                        cameramanPhone = "0300-123456$nameIdx",
                        bookingId = booking.id,
                        customerName = booking.customerName,
                        rasamName = rasam.name,
                        eventDate = rasam.date,
                        cameraType = camera,
                        cameraOwnership = if (camera == "Drone") "Cameraman Own" else "Studio Camera",
                        routeNote = rasam.routeNote.ifEmpty { "Ghar se ➜ Venue tak" },
                        feeAmount = defaultFee,
                        paidAmount = defaultFee * 0.5,
                        notes = "Auto-synced from Booking ${booking.bookingNumber}"
                    )
                    dao.insertCameramanRecord(record)
                }
            }
        }
    }

    // Cameramen
    suspend fun insertCameraman(record: CameramanRecord): Long = dao.insertCameramanRecord(record)
    suspend fun updateCameraman(record: CameramanRecord) = dao.updateCameramanRecord(record)
    suspend fun deleteCameraman(record: CameramanRecord) = dao.deleteCameramanRecord(record)

    // VCR Bills
    suspend fun insertVcrBill(bill: VcrBill): Long = dao.insertVcrBill(bill)
    suspend fun updateVcrBill(bill: VcrBill) = dao.updateVcrBill(bill)
    suspend fun deleteVcrBill(bill: VcrBill) = dao.deleteVcrBill(bill)

    // Editing Orders
    suspend fun insertEditingOrder(order: VideoEditingOrder): Long = dao.insertEditingOrder(order)
    suspend fun updateEditingOrder(order: VideoEditingOrder) = dao.updateEditingOrder(order)
    suspend fun deleteEditingOrder(order: VideoEditingOrder) = dao.deleteEditingOrder(order)

    // Initial Pre-population if empty
    suspend fun checkAndSeedInitialData() {
        val currentBookings = dao.getAllBookings().first()
        if (currentBookings.isEmpty()) {
            // Seed Sample Bookings
            val b1 = Booking(
                bookingNumber = "#001",
                customerName = "Muhammad Bilal",
                customerMobile = "0301-7654321",
                bookingDate = "2026-10-15",
                address = "Gulshan-e-Iqbal, Block 13-D, Karachi",
                totalAmount = 65000.0,
                advanceAmount = 25000.0,
                rasams = listOf(
                    Rasam(
                        name = "Mehndi",
                        date = "2026-10-24",
                        time = "08:30 PM",
                        venue = "Dreamland Banquet, Hall A",
                        cameras = listOf("Video Camera", "DSLR"),
                        routeNote = "Ghar se ➜ Dreamland Lawn"
                    ),
                    Rasam(
                        name = "Barat",
                        date = "2026-10-26",
                        time = "09:00 PM",
                        venue = "Royal Palm Marquee",
                        cameras = listOf("Video Camera", "DSLR", "Drone", "Gimbal"),
                        routeNote = "Dulha House Gulshan ➜ Royal Palm Shadi Hall"
                    ),
                    Rasam(
                        name = "Dawat / Walima",
                        date = "2026-10-28",
                        time = "08:30 PM",
                        venue = "Carlton Hotel, Main Ballroom",
                        cameras = listOf("Video Camera", "DSLR"),
                        routeNote = "Direct Hotel Carlton"
                    )
                ),
                payments = listOf(
                    PaymentInstallment(date = "2026-10-18", amount = 15000.0, note = "Cash Jama Part 1")
                ),
                notes = "Full HD Wedding Movie + 2 Photobooks + Drone Coverage"
            )

            val b2 = Booking(
                bookingNumber = "#002",
                customerName = "Tariq Mehmood",
                customerMobile = "0321-9876543",
                bookingDate = "2026-10-16",
                address = "DHA Phase 6, Lahore",
                totalAmount = 85000.0,
                advanceAmount = 30000.0,
                rasams = listOf(
                    Rasam(
                        name = "Paat / Haldi",
                        date = "2026-10-26", // Double-booked with b1 Barat! Shows double-booking indicator!
                        time = "04:00 PM",
                        venue = "Family Lawn, Phase 6",
                        cameras = listOf("DSLR", "Gimbal"),
                        routeNote = "Home Event"
                    ),
                    Rasam(
                        name = "Mehndi",
                        date = "2026-10-30",
                        time = "08:00 PM",
                        venue = "Grand Majestic Palace",
                        cameras = listOf("Video Camera", "DSLR", "Drone"),
                        routeNote = "House 14 ➜ Grand Majestic"
                    )
                ),
                payments = listOf(
                    PaymentInstallment(date = "2026-10-17", amount = 20000.0, note = "Online Bank Transfer")
                ),
                notes = "Teaser Video + Instagram Reels package requested"
            )

            insertBooking(b1)
            insertBooking(b2)

            // Seed VCR Cassette Bills
            val vcr1 = VcrBill(
                billNumber = "#VCR-001",
                customerName = "Haji Abdul Rasheed",
                customerPhone = "0333-2145678",
                date = "2026-10-17",
                cassettes = listOf(
                    CassetteItem(label = "Bhai Shadi 1996 Video", type = "VHS Big Tape", quantity = 3, rate = 400.0, amount = 1200.0),
                    CassetteItem(label = "Hajj Tour 2004", type = "Mini DV", quantity = 2, rate = 450.0, amount = 900.0),
                    CassetteItem(label = "Old Family Songs & Speeches", type = "Audio Cassette", quantity = 2, rate = 250.0, amount = 500.0)
                ),
                transferModes = listOf("Customer USB", "Mobile transfer"),
                totalAmount = 2600.0,
                advanceAmount = 1500.0,
                notes = "Urgent delivery requested before Friday. Clean audio filter applied."
            )
            dao.insertVcrBill(vcr1)

            // Seed Video Editing Orders
            val ed1 = VideoEditingOrder(
                orderNumber = "#ED-001",
                customerName = "Zubair Siddiqui",
                customerPhone = "0345-8877665",
                date = "2026-10-16",
                videoDuration = "3 Hours 45 Mins",
                dataSize = "160 GB",
                deliveryMedia = listOf("Customer USB", "Studio USB"),
                status = "In Progress",
                totalAmount = 14000.0,
                advanceAmount = 6000.0,
                notes = "Cinematic slow motion color grading + custom Urdu title track"
            )
            val ed2 = VideoEditingOrder(
                orderNumber = "#ED-002",
                customerName = "Farhan Sheikh",
                customerPhone = "0300-5544332",
                date = "2026-10-17",
                videoDuration = "2 Hours",
                dataSize = "80 GB",
                deliveryMedia = listOf("Portable Disk"),
                status = "Completed",
                totalAmount = 8500.0,
                advanceAmount = 8500.0,
                notes = "Short highlights teaser delivered on WhatsApp"
            )
            dao.insertEditingOrder(ed1)
            dao.insertEditingOrder(ed2)
        }
    }
}
