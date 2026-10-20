package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Booking
import com.example.data.model.PaymentInstallment
import com.example.data.model.Rasam
import com.example.ui.components.BookingReceiptModal
import com.example.ui.theme.AmberGold
import com.example.ui.theme.AmberGoldDark
import com.example.ui.theme.CrimsonAlert
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.StudioBorderNavy
import com.example.ui.theme.StudioCardNavy
import com.example.ui.theme.StudioDarkNavy
import com.example.ui.viewmodel.StudioViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun BookingScreen(
    viewModel: StudioViewModel,
    modifier: Modifier = Modifier
) {
    val bookings by viewModel.filteredBookings.collectAsState()
    val allBookings by viewModel.allBookings.collectAsState()
    val searchQuery by viewModel.bookingSearchQuery.collectAsState()
    val activeBookingDetail by viewModel.activeBookingForDetail.collectAsState()

    var showAddEditDialog by remember { mutableStateOf(false) }
    var bookingToEdit by remember { mutableStateOf<Booking?>(null) }
    var quickPaymentBooking by remember { mutableStateOf<Booking?>(null) }
    var bookingToDelete by remember { mutableStateOf<Booking?>(null) }

    // Summary calculations
    val totalBilling = allBookings.sumOf { it.totalAmount }
    val totalRecovered = allBookings.sumOf { it.totalPaid() }
    val totalBaqaya = allBookings.sumOf { it.remainingBalance() }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {

            // Top Financial Stats Cards
            StatsSummaryRow(
                totalBookings = allBookings.size,
                totalBilling = totalBilling,
                totalPaid = totalRecovered,
                totalBaqaya = totalBaqaya
            )

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setBookingSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Search by Customer Name or Mobile #...") },
                leadingIcon = {
                    Icon(Icons.Filled.Search, contentDescription = null, tint = AmberGold)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setBookingSearchQuery("") }) {
                            Icon(Icons.Filled.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AmberGold,
                    unfocusedBorderColor = StudioBorderNavy
                )
            )

            // Bookings List
            if (bookings.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Filled.Videocam,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (searchQuery.isBlank()) "No bookings yet. Add one below!" else "No bookings match '$searchQuery'",
                            color = Color.Gray,
                            fontSize = 15.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(bookings, key = { it.id }) { booking ->
                        BookingCard(
                            booking = booking,
                            onOpenBill = { viewModel.openBookingDetail(booking) },
                            onAddPayment = { quickPaymentBooking = booking },
                            onEdit = {
                                bookingToEdit = booking
                                showAddEditDialog = true
                            },
                            onDelete = { bookingToDelete = booking }
                        )
                    }
                }
            }
        }

        // Floating Action Button for New Booking
        FloatingActionButton(
            onClick = {
                bookingToEdit = null
                showAddEditDialog = true
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp),
            containerColor = AmberGold,
            contentColor = Color.Black
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.Add, contentDescription = "New Booking")
                Spacer(modifier = Modifier.width(6.dp))
                Text("New Booking", fontWeight = FontWeight.Bold)
            }
        }
    }

    // Detail / Receipt Modal
    if (activeBookingDetail != null) {
        BookingReceiptModal(
            booking = activeBookingDetail!!,
            onDismiss = { viewModel.openBookingDetail(null) }
        )
    }

    // Add / Edit Booking Dialog
    if (showAddEditDialog) {
        AddEditBookingDialog(
            existingBooking = bookingToEdit,
            suggestedBookingNumber = viewModel.getNextBookingNumber(),
            onDismiss = { showAddEditDialog = false },
            onSave = { booking ->
                viewModel.saveBooking(booking)
                showAddEditDialog = false
            }
        )
    }

    // Quick Jama Next / Payment Dialog
    if (quickPaymentBooking != null) {
        QuickPaymentDialog(
            booking = quickPaymentBooking!!,
            onDismiss = { quickPaymentBooking = null },
            onSavePayment = { installment ->
                val updatedPayments = quickPaymentBooking!!.payments + installment
                viewModel.saveBooking(quickPaymentBooking!!.copy(payments = updatedPayments))
                quickPaymentBooking = null
            }
        )
    }

    // Delete Confirmation Dialog
    if (bookingToDelete != null) {
        AlertDialog(
            onDismissRequest = { bookingToDelete = null },
            title = { Text("Delete Booking?") },
            text = { Text("Are you sure you want to delete booking ${bookingToDelete?.bookingNumber} for ${bookingToDelete?.customerName}?") },
            confirmButton = {
                Button(
                    onClick = {
                        bookingToDelete?.let { viewModel.deleteBooking(it) }
                        bookingToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonAlert)
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { bookingToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun StatsSummaryRow(
    totalBookings: Int,
    totalBilling: Double,
    totalPaid: Double,
    totalBaqaya: Double
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            MiniStatCard("Total Bookings", "$totalBookings", AmberGold, "Client Events")
        }
        item {
            MiniStatCard("Total Billing", "Rs. ${"%,.0f".format(totalBilling)}", Color(0xFF38BDF8), "Gross Value")
        }
        item {
            MiniStatCard("Advance / Paid", "Rs. ${"%,.0f".format(totalPaid)}", EmeraldSuccess, "Collected")
        }
        item {
            MiniStatCard("Baqaya (Balance)", "Rs. ${"%,.0f".format(totalBaqaya)}", CrimsonAlert, "Pending Jama")
        }
    }
}

@Composable
fun MiniStatCard(
    title: String,
    value: String,
    accentColor: Color,
    subtitle: String
) {
    Card(
        modifier = Modifier.width(160.dp),
        colors = CardDefaults.cardColors(containerColor = StudioCardNavy),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, fontSize = 11.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = accentColor)
            Spacer(modifier = Modifier.height(2.dp))
            Text(subtitle, fontSize = 10.sp, color = Color(0xFF64748B))
        }
    }
}

@Composable
fun BookingCard(
    booking: Booking,
    onOpenBill: () -> Unit,
    onAddPayment: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    val status = booking.paymentStatus()
    val statusColor = when (status) {
        "PAID" -> EmeraldSuccess
        "PARTIAL" -> AmberGold
        else -> CrimsonAlert
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenBill() },
        colors = CardDefaults.cardColors(containerColor = StudioCardNavy),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Booking #, Customer Name, Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(AmberGold.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = booking.bookingNumber,
                            color = AmberGold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = booking.customerName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = Color.White
                    )
                }

                // Status Badge
                Box(
                    modifier = Modifier
                        .background(statusColor.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                        .border(1.dp, statusColor.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 10.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = status,
                        color = statusColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Phone and booking date
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .clickable {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${booking.customerMobile}"))
                            context.startActivity(intent)
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Call,
                        contentDescription = "Call",
                        tint = EmeraldSuccess,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = booking.customerMobile,
                        color = EmeraldSuccess,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Booked: ${booking.bookingDate}",
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp
                )
            }

            if (booking.address.isNotBlank()) {
                Text(
                    text = "📍 ${booking.address}",
                    color = Color(0xFFCBD5E1),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = StudioBorderNavy, thickness = 1.dp)
            Spacer(modifier = Modifier.height(10.dp))

            // Rasams Strip
            Text(
                text = "RASAMS & EVENTS (${booking.rasams.size}):",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF94A3B8)
            )
            Spacer(modifier = Modifier.height(6.dp))

            booking.rasams.forEach { rasam ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp)
                        .background(StudioDarkNavy.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = rasam.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${rasam.date} (${rasam.time})",
                                fontSize = 11.sp,
                                color = AmberGold
                            )
                        }
                        if (rasam.routeNote.isNotBlank()) {
                            Text(
                                text = "Route: ${rasam.routeNote}",
                                fontSize = 10.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }

                    // Cameras badge
                    Text(
                        text = rasam.cameras.joinToString(" • "),
                        fontSize = 10.sp,
                        color = Color(0xFF38BDF8),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Financial Summary Block
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0B1120), RoundedCornerShape(8.dp))
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Total Amount", fontSize = 10.sp, color = Color(0xFF94A3B8))
                    Text(
                        "Rs. ${"%,.0f".format(booking.totalAmount)}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Column {
                    Text("Paid / Advance", fontSize = 10.sp, color = Color(0xFF94A3B8))
                    Text(
                        "Rs. ${"%,.0f".format(booking.totalPaid())}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldSuccess
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Baqaya (Balance)", fontSize = 10.sp, color = Color(0xFF94A3B8))
                    Text(
                        "Rs. ${"%,.0f".format(booking.remainingBalance())}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = if (booking.remainingBalance() > 0) CrimsonAlert else EmeraldSuccess
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Quick Actions: View Bill, Add Jama Next, Edit, Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onOpenBill,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = AmberGoldDark),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Filled.Print, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("A4 Bill", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onAddPayment,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Filled.Payments, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("+ Jama", color = EmeraldSuccess, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(Icons.Filled.Edit, contentDescription = "Edit", tint = Color(0xFF94A3B8), modifier = Modifier.size(18.dp))
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = CrimsonAlert, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
fun AddEditBookingDialog(
    existingBooking: Booking?,
    suggestedBookingNumber: String,
    onDismiss: () -> Unit,
    onSave: (Booking) -> Unit
) {
    val currentDateStr = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    var bookingNumber by remember {
        mutableStateOf(existingBooking?.bookingNumber ?: suggestedBookingNumber)
    }
    var customerName by remember { mutableStateOf(existingBooking?.customerName ?: "") }
    var customerMobile by remember { mutableStateOf(existingBooking?.customerMobile ?: "") }
    var bookingDate by remember { mutableStateOf(existingBooking?.bookingDate ?: currentDateStr) }
    var address by remember { mutableStateOf(existingBooking?.address ?: "") }
    var totalAmountStr by remember {
        mutableStateOf(if (existingBooking != null) existingBooking.totalAmount.toLong().toString() else "50000")
    }
    var advanceAmountStr by remember {
        mutableStateOf(if (existingBooking != null) existingBooking.advanceAmount.toLong().toString() else "20000")
    }
    var notes by remember { mutableStateOf(existingBooking?.notes ?: "") }

    // Multi-Rasam State
    val rasams = remember {
        mutableStateListOf<Rasam>().apply {
            if (existingBooking != null && existingBooking.rasams.isNotEmpty()) {
                addAll(existingBooking.rasams)
            } else {
                add(
                    Rasam(
                        name = "Mehndi",
                        date = currentDateStr,
                        time = "08:00 PM",
                        venue = "Dreamland Hall",
                        cameras = listOf("Video Camera", "DSLR"),
                        routeNote = "Ghar se ➜ Venue"
                    )
                )
            }
        }
    }

    // Jama Next payment rows
    val payments = remember {
        mutableStateListOf<PaymentInstallment>().apply {
            if (existingBooking != null) {
                addAll(existingBooking.payments)
            }
        }
    }

    val totalAmount = totalAmountStr.toDoubleOrNull() ?: 0.0
    val advanceAmount = advanceAmountStr.toDoubleOrNull() ?: 0.0
    val totalJamaNext = payments.sumOf { it.amount }
    val totalPaid = advanceAmount + totalJamaNext
    val baqaya = maxOf(0.0, totalAmount - totalPaid)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(16.dp),
            color = StudioDarkNavy
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = if (existingBooking == null) "New Program Booking" else "Edit Booking ${existingBooking.bookingNumber}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Customer info fields
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = bookingNumber,
                        onValueChange = { bookingNumber = it },
                        label = { Text("Booking #") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AmberGold,
                            unfocusedBorderColor = StudioBorderNavy
                        )
                    )
                    OutlinedTextField(
                        value = bookingDate,
                        onValueChange = { bookingDate = it },
                        label = { Text("Booking Date") },
                        modifier = Modifier.weight(1.2f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AmberGold,
                            unfocusedBorderColor = StudioBorderNavy
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = customerName,
                    onValueChange = { customerName = it },
                    label = { Text("Customer Name *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AmberGold,
                        unfocusedBorderColor = StudioBorderNavy
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = customerMobile,
                    onValueChange = { customerMobile = it },
                    label = { Text("Mobile Number *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AmberGold,
                        unfocusedBorderColor = StudioBorderNavy
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Customer Address / City") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AmberGold,
                        unfocusedBorderColor = StudioBorderNavy
                    )
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Multi-Rasam Setup Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "MULTI-RASAM SETUP (${rasams.size})",
                        fontWeight = FontWeight.Bold,
                        color = AmberGold,
                        fontSize = 14.sp
                    )

                    Button(
                        onClick = {
                            rasams.add(
                                Rasam(
                                    name = "Barat",
                                    date = currentDateStr,
                                    time = "09:00 PM",
                                    venue = "Royal Hall",
                                    cameras = listOf("Video Camera", "DSLR", "Drone"),
                                    routeNote = "Ghar se ➜ Shadi Hall tak"
                                )
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AmberGold),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("+ Add Rasam", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                rasams.forEachIndexed { index, rasam ->
                    RasamEditCard(
                        index = index,
                        rasam = rasam,
                        onUpdate = { updated -> rasams[index] = updated },
                        onRemove = { if (rasams.size > 1) rasams.removeAt(index) }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Payment Tracking & Auto Calculations
                Text(
                    text = "PAYMENT TRACKING & AUTO CALCULATIONS",
                    fontWeight = FontWeight.Bold,
                    color = AmberGold,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = totalAmountStr,
                        onValueChange = { totalAmountStr = it },
                        label = { Text("Total Amount (Rs)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AmberGold,
                            unfocusedBorderColor = StudioBorderNavy
                        )
                    )

                    OutlinedTextField(
                        value = advanceAmountStr,
                        onValueChange = { advanceAmountStr = it },
                        label = { Text("Advance Paid (Rs)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AmberGold,
                            unfocusedBorderColor = StudioBorderNavy
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Jama Next Installments Rows
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "JAMA NEXT INSTALLMENTS (${payments.size})",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp
                    )
                    TextButton(
                        onClick = {
                            payments.add(
                                PaymentInstallment(
                                    date = currentDateStr,
                                    amount = 5000.0,
                                    note = "Cash Jama Part ${payments.size + 1}"
                                )
                            )
                        }
                    ) {
                        Text("+ Add Jama Row", color = EmeraldSuccess, fontWeight = FontWeight.Bold)
                    }
                }

                payments.forEachIndexed { pIdx, payment ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = payment.date,
                            onValueChange = { payments[pIdx] = payment.copy(date = it) },
                            label = { Text("Date") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = payment.amount.toLong().toString(),
                            onValueChange = {
                                val amt = it.toDoubleOrNull() ?: 0.0
                                payments[pIdx] = payment.copy(amount = amt)
                            },
                            label = { Text("Rs") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        OutlinedTextField(
                            value = payment.note,
                            onValueChange = { payments[pIdx] = payment.copy(note = it) },
                            label = { Text("Note") },
                            modifier = Modifier.weight(1.2f),
                            singleLine = true
                        )
                        IconButton(
                            onClick = { payments.removeAt(pIdx) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = CrimsonAlert)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Live Auto Calculation Summary Banner
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = StudioCardNavy),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Billing:", color = Color(0xFF94A3B8), fontSize = 13.sp)
                            Text("Rs. ${"%,.0f".format(totalAmount)}", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Paid (Advance + Jama):", color = Color(0xFF94A3B8), fontSize = 13.sp)
                            Text("Rs. ${"%,.0f".format(totalPaid)}", color = EmeraldSuccess, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        HorizontalDivider(color = StudioBorderNavy)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("BAQAYA (Remaining):", color = AmberGold, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(
                                "Rs. ${"%,.0f".format(baqaya)}",
                                color = if (baqaya > 0) CrimsonAlert else EmeraldSuccess,
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Special Instructions / Package Notes") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AmberGold,
                        unfocusedBorderColor = StudioBorderNavy
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            if (customerName.isNotBlank() && customerMobile.isNotBlank()) {
                                val newBooking = Booking(
                                    id = existingBooking?.id ?: 0L,
                                    bookingNumber = bookingNumber.ifBlank { suggestedBookingNumber },
                                    customerName = customerName.trim(),
                                    customerMobile = customerMobile.trim(),
                                    bookingDate = bookingDate,
                                    address = address.trim(),
                                    totalAmount = totalAmount,
                                    advanceAmount = advanceAmount,
                                    rasams = rasams.toList(),
                                    payments = payments.toList(),
                                    notes = notes.trim(),
                                    createdAt = existingBooking?.createdAt ?: System.currentTimeMillis()
                                )
                                onSave(newBooking)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = customerName.isNotBlank() && customerMobile.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = AmberGold)
                    ) {
                        Text("Save Booking", color = Color.Black, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel", color = Color(0xFF94A3B8))
                    }
                }
            }
        }
    }
}

@Composable
fun RasamEditCard(
    index: Int,
    rasam: Rasam,
    onUpdate: (Rasam) -> Unit,
    onRemove: () -> Unit
) {
    val commonRasams = listOf("Mehndi", "Barat", "Dawat / Walima", "Paat / Haldi", "Sangeet", "Mayun", "Engagement", "Birthday")
    val availableCameras = listOf("Video Camera", "DSLR", "Drone", "Gimbal")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, StudioBorderNavy, RoundedCornerShape(10.dp)),
        colors = CardDefaults.cardColors(containerColor = StudioCardNavy),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Rasam #${index + 1}: ${rasam.name}",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 14.sp
                )
                IconButton(onClick = onRemove, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Filled.Delete, contentDescription = "Remove", tint = CrimsonAlert)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Quick Rasam Selector Chips
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(commonRasams) { name ->
                    val isSelected = rasam.name.equals(name, ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) AmberGold else StudioDarkNavy)
                            .clickable { onUpdate(rasam.copy(name = name)) }
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = name,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color.Black else Color(0xFFCBD5E1)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = rasam.date,
                    onValueChange = { onUpdate(rasam.copy(date = it)) },
                    label = { Text("Date (YYYY-MM-DD)") },
                    modifier = Modifier.weight(1.2f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = rasam.time,
                    onValueChange = { onUpdate(rasam.copy(time = it)) },
                    label = { Text("Time (e.g. 08:30 PM)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = rasam.venue,
                onValueChange = { onUpdate(rasam.copy(venue = it)) },
                label = { Text("Venue / Hall Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = rasam.routeNote,
                onValueChange = { onUpdate(rasam.copy(routeNote = it)) },
                label = { Text("Route: Kahan se ➜ Kahan tak (e.g. Ghar se ➜ Shadi Hall)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Camera Checklist
            Text(
                text = "Equipment / Camera Checklist:",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF94A3B8)
            )
            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                availableCameras.forEach { cam ->
                    val checked = rasam.cameras.contains(cam)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            val updatedCams = if (checked) {
                                rasam.cameras - cam
                            } else {
                                rasam.cameras + cam
                            }
                            onUpdate(rasam.copy(cameras = updatedCams))
                        }
                    ) {
                        Checkbox(
                            checked = checked,
                            onCheckedChange = { isChecked ->
                                val updatedCams = if (isChecked) {
                                    rasam.cameras + cam
                                } else {
                                    rasam.cameras - cam
                                }
                                onUpdate(rasam.copy(cameras = updatedCams))
                            },
                            colors = CheckboxDefaults.colors(checkedColor = AmberGold)
                        )
                        Text(
                            text = cam.replace(" Camera", ""),
                            fontSize = 11.sp,
                            color = if (checked) Color.White else Color(0xFF94A3B8)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QuickPaymentDialog(
    booking: Booking,
    onDismiss: () -> Unit,
    onSavePayment: (PaymentInstallment) -> Unit
) {
    val currentDateStr = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }
    var amountStr by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("Cash Jama Next") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Jama Next for ${booking.customerName}") },
        text = {
            Column {
                Text(
                    text = "Current Baqaya: Rs. ${"%,.0f".format(booking.remainingBalance())}",
                    fontWeight = FontWeight.Bold,
                    color = CrimsonAlert
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Jama Amount (Rs) *") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Payment Note (e.g. Cash / Bank Transfer)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountStr.toDoubleOrNull() ?: 0.0
                    if (amount > 0) {
                        onSavePayment(
                            PaymentInstallment(
                                date = currentDateStr,
                                amount = amount,
                                note = note
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess)
            ) {
                Text("Confirm Jama", color = Color.White)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
