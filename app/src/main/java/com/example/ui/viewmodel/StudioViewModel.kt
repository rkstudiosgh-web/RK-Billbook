package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.Booking
import com.example.data.model.CameramanRecord
import com.example.data.model.CameramanSummary
import com.example.data.model.EditingSummary
import com.example.data.model.Rasam
import com.example.data.model.VcrBill
import com.example.data.model.VideoEditingOrder
import com.example.data.repository.StudioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CalendarEventItem(
    val bookingId: Long,
    val bookingNumber: String,
    val customerName: String,
    val customerMobile: String,
    val rasamName: String,
    val date: String,
    val time: String,
    val venue: String,
    val cameras: List<String>,
    val routeNote: String
)

data class CalendarSummary(
    val totalEvents: Int,
    val bookedDates: Int,
    val doubleBookedDays: Int
)

class StudioViewModel(private val repository: StudioRepository) : ViewModel() {

    // Tab state (0: Bookings, 1: Calendar, 2: Cameraman, 3: VCR, 4: Editing)
    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab

    fun setTab(index: Int) {
        _selectedTab.value = index
    }

    // Bookings and search
    private val _bookingSearchQuery = MutableStateFlow("")
    val bookingSearchQuery: StateFlow<String> = _bookingSearchQuery

    fun setBookingSearchQuery(query: String) {
        _bookingSearchQuery.value = query
    }

    val allBookings: StateFlow<List<Booking>> = repository.allBookings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val filteredBookings: StateFlow<List<Booking>> = combine(
        allBookings,
        _bookingSearchQuery
    ) { bookings, query ->
        if (query.isBlank()) {
            bookings
        } else {
            val q = query.trim().lowercase()
            bookings.filter {
                it.customerName.lowercase().contains(q) ||
                    it.customerMobile.lowercase().contains(q) ||
                    it.bookingNumber.lowercase().contains(q) ||
                    it.address.lowercase().contains(q)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Calendar sync
    val calendarEventsByDate: StateFlow<Map<String, List<CalendarEventItem>>> = allBookings.combine(
        MutableStateFlow(Unit)
    ) { bookings, _ ->
        val map = mutableMapOf<String, MutableList<CalendarEventItem>>()
        for (booking in bookings) {
            for (rasam in booking.rasams) {
                if (rasam.date.isNotBlank()) {
                    val item = CalendarEventItem(
                        bookingId = booking.id,
                        bookingNumber = booking.bookingNumber,
                        customerName = booking.customerName,
                        customerMobile = booking.customerMobile,
                        rasamName = rasam.name,
                        date = rasam.date,
                        time = rasam.time,
                        venue = rasam.venue,
                        cameras = rasam.cameras,
                        routeNote = rasam.routeNote
                    )
                    map.getOrPut(rasam.date) { mutableListOf() }.add(item)
                }
            }
        }
        map
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val calendarSummary: StateFlow<CalendarSummary> = calendarEventsByDate.combine(
        MutableStateFlow(Unit)
    ) { eventsMap, _ ->
        val totalEvents = eventsMap.values.sumOf { it.size }
        val bookedDates = eventsMap.keys.size
        val doubleBooked = eventsMap.values.count { it.size >= 2 }
        CalendarSummary(
            totalEvents = totalEvents,
            bookedDates = bookedDates,
            doubleBookedDays = doubleBooked
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CalendarSummary(0, 0, 0))

    // Cameraman records
    val allCameramen: StateFlow<List<CameramanRecord>> = repository.allCameramen.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _cameramanSearchQuery = MutableStateFlow("")
    val cameramanSearchQuery: StateFlow<String> = _cameramanSearchQuery

    fun setCameramanSearchQuery(query: String) {
        _cameramanSearchQuery.value = query
    }

    val filteredCameramen: StateFlow<List<CameramanRecord>> = combine(
        allCameramen,
        _cameramanSearchQuery
    ) { records, query ->
        if (query.isBlank()) {
            records
        } else {
            val q = query.trim().lowercase()
            records.filter {
                it.cameramanName.lowercase().contains(q) ||
                    it.customerName.lowercase().contains(q) ||
                    it.rasamName.lowercase().contains(q) ||
                    it.cameraType.lowercase().contains(q) ||
                    it.eventDate.contains(q)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val cameramanSummaries: StateFlow<List<CameramanSummary>> = allCameramen.combine(
        MutableStateFlow(Unit)
    ) { records, _ ->
        records.groupBy { it.cameramanName.trim() }
            .filterKeys { it.isNotBlank() }
            .map { (name, list) ->
                val phone = list.firstOrNull { it.cameramanPhone.isNotBlank() }?.cameramanPhone ?: ""
                val totalFee = list.sumOf { it.feeAmount }
                val totalPaid = list.sumOf { it.paidAmount }
                val totalBaqaya = maxOf(0.0, totalFee - totalPaid)
                CameramanSummary(
                    name = name,
                    phone = phone,
                    totalEvents = list.size,
                    totalFee = totalFee,
                    totalPaid = totalPaid,
                    totalBaqaya = totalBaqaya
                )
            }.sortedByDescending { it.totalBaqaya }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // VCR Cassette Bills
    val allVcrBills: StateFlow<List<VcrBill>> = repository.allVcrBills.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _vcrSearchQuery = MutableStateFlow("")
    val vcrSearchQuery: StateFlow<String> = _vcrSearchQuery

    fun setVcrSearchQuery(query: String) {
        _vcrSearchQuery.value = query
    }

    val filteredVcrBills: StateFlow<List<VcrBill>> = combine(
        allVcrBills,
        _vcrSearchQuery
    ) { bills, query ->
        if (query.isBlank()) {
            bills
        } else {
            val q = query.trim().lowercase()
            bills.filter {
                it.customerName.lowercase().contains(q) ||
                    it.customerPhone.lowercase().contains(q) ||
                    it.billNumber.lowercase().contains(q)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Video Editing Orders
    val allEditingOrders: StateFlow<List<VideoEditingOrder>> = repository.allEditingOrders.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _editingSearchQuery = MutableStateFlow("")
    val editingSearchQuery: StateFlow<String> = _editingSearchQuery

    fun setEditingSearchQuery(query: String) {
        _editingSearchQuery.value = query
    }

    val filteredEditingOrders: StateFlow<List<VideoEditingOrder>> = combine(
        allEditingOrders,
        _editingSearchQuery
    ) { orders, query ->
        if (query.isBlank()) {
            orders
        } else {
            val q = query.trim().lowercase()
            orders.filter {
                it.customerName.lowercase().contains(q) ||
                    it.customerPhone.lowercase().contains(q) ||
                    it.orderNumber.lowercase().contains(q) ||
                    it.status.lowercase().contains(q)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val editingSummary: StateFlow<EditingSummary> = allEditingOrders.combine(
        MutableStateFlow(Unit)
    ) { orders, _ ->
        val totalOrders = orders.size
        val totalBilling = orders.sumOf { it.totalAmount }
        val totalAdvance = orders.sumOf { it.advanceAmount }
        val totalBaqaya = maxOf(0.0, totalBilling - totalAdvance)
        EditingSummary(
            totalOrders = totalOrders,
            totalBilling = totalBilling,
            totalAdvance = totalAdvance,
            totalBaqaya = totalBaqaya
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), EditingSummary(0, 0.0, 0.0, 0.0))

    // Selected booking for direct navigation/detail
    private val _activeBookingForDetail = MutableStateFlow<Booking?>(null)
    val activeBookingForDetail: StateFlow<Booking?> = _activeBookingForDetail

    fun openBookingDetail(booking: Booking?) {
        _activeBookingForDetail.value = booking
    }

    fun openBookingById(bookingId: Long) {
        val found = allBookings.value.find { it.id == bookingId }
        if (found != null) {
            _selectedTab.value = 0 // Switch to Bookings tab
            _activeBookingForDetail.value = found
        }
    }

    init {
        viewModelScope.launch {
            repository.checkAndSeedInitialData()
        }
    }

    // CRUD - Booking
    fun saveBooking(booking: Booking) {
        viewModelScope.launch {
            if (booking.id == 0L) {
                repository.insertBooking(booking)
            } else {
                repository.updateBooking(booking)
            }
        }
    }

    fun deleteBooking(booking: Booking) {
        viewModelScope.launch {
            repository.deleteBooking(booking)
        }
    }

    fun getNextBookingNumber(): String {
        val count = allBookings.value.size + 1
        return "#" + count.toString().padStart(3, '0')
    }

    // CRUD - Cameraman
    fun saveCameramanRecord(record: CameramanRecord) {
        viewModelScope.launch {
            if (record.id == 0L) {
                repository.insertCameraman(record)
            } else {
                repository.updateCameraman(record)
            }
        }
    }

    fun deleteCameramanRecord(record: CameramanRecord) {
        viewModelScope.launch {
            repository.deleteCameraman(record)
        }
    }

    // CRUD - VCR Bill
    fun saveVcrBill(bill: VcrBill) {
        viewModelScope.launch {
            if (bill.id == 0L) {
                repository.insertVcrBill(bill)
            } else {
                repository.updateVcrBill(bill)
            }
        }
    }

    fun deleteVcrBill(bill: VcrBill) {
        viewModelScope.launch {
            repository.deleteVcrBill(bill)
        }
    }

    fun getNextVcrBillNumber(): String {
        val count = allVcrBills.value.size + 1
        return "#VCR-" + count.toString().padStart(3, '0')
    }

    // CRUD - Video Editing
    fun saveEditingOrder(order: VideoEditingOrder) {
        viewModelScope.launch {
            if (order.id == 0L) {
                repository.insertEditingOrder(order)
            } else {
                repository.updateEditingOrder(order)
            }
        }
    }

    fun deleteEditingOrder(order: VideoEditingOrder) {
        viewModelScope.launch {
            repository.deleteEditingOrder(order)
        }
    }

    fun getNextEditingOrderNumber(): String {
        val count = allEditingOrders.value.size + 1
        return "#ED-" + count.toString().padStart(3, '0')
    }
}
