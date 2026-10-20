package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CrimsonAlert
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.StudioBorderNavy
import com.example.ui.theme.StudioCardNavy
import com.example.ui.theme.StudioDarkNavy
import com.example.ui.viewmodel.CalendarEventItem
import com.example.ui.viewmodel.StudioViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun CalendarScreen(
    viewModel: StudioViewModel,
    modifier: Modifier = Modifier
) {
    val eventsByDate by viewModel.calendarEventsByDate.collectAsState()
    val summary by viewModel.calendarSummary.collectAsState()

    // Current displayed calendar month/year
    var currentCal by remember {
        mutableStateOf(Calendar.getInstance().apply {
            // Default to October 2026 (matching simulated or real current time)
            set(Calendar.YEAR, 2026)
            set(Calendar.MONTH, Calendar.OCTOBER)
            set(Calendar.DAY_OF_MONTH, 1)
        })
    }

    var selectedDateForModal by remember { mutableStateOf<String?>(null) }
    val monthTitleFormat = remember { SimpleDateFormat("MMMM yyyy", Locale.getDefault()) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 30.dp)
    ) {
        // Realtime Summary Badges
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CalendarSummaryCard(
                title = "Total Events",
                count = "${summary.totalEvents}",
                color = AmberGold,
                modifier = Modifier.weight(1f)
            )
            CalendarSummaryCard(
                title = "Booked Dates",
                count = "${summary.bookedDates}",
                color = EmeraldSuccess,
                modifier = Modifier.weight(1f)
            )
            CalendarSummaryCard(
                title = "Double-Booked",
                count = "${summary.doubleBookedDays}",
                color = CrimsonAlert,
                modifier = Modifier.weight(1f)
            )
        }

        // Calendar Month Navigation Strip
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(containerColor = StudioCardNavy),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    val prev = currentCal.clone() as Calendar
                    prev.add(Calendar.MONTH, -1)
                    currentCal = prev
                }) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Previous Month",
                        tint = AmberGold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.Event,
                        contentDescription = null,
                        tint = AmberGold,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = monthTitleFormat.format(currentCal.time),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }

                Row {
                    IconButton(onClick = {
                        val today = Calendar.getInstance()
                        today.set(Calendar.DAY_OF_MONTH, 1)
                        currentCal = today
                    }) {
                        Icon(
                            Icons.Filled.Today,
                            contentDescription = "Jump to Current",
                            tint = Color(0xFF38BDF8)
                        )
                    }

                    IconButton(onClick = {
                        val next = currentCal.clone() as Calendar
                        next.add(Calendar.MONTH, 1)
                        currentCal = next
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Next Month",
                            tint = AmberGold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Legend: ✔ Single event (Green) vs ✔✔ Double booked (Red highlight)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(EmeraldSuccess)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("✔ 1 Event (Available)", fontSize = 11.sp, color = Color(0xFF94A3B8))
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(CrimsonAlert)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("✔✔ 2+ Double-Booked", fontSize = 11.sp, color = CrimsonAlert, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Calendar Grid Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(containerColor = StudioCardNavy),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                // Day of Week Headers (Sun to Sat)
                val dayHeaders = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
                Row(modifier = Modifier.fillMaxWidth()) {
                    dayHeaders.forEach { day ->
                        Text(
                            text = day,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (day == "Sun" || day == "Fri") AmberGold else Color(0xFF94A3B8)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = StudioBorderNavy, thickness = 1.dp)
                Spacer(modifier = Modifier.height(10.dp))

                // Build Calendar Days for Current Month
                val monthCal = currentCal.clone() as Calendar
                monthCal.set(Calendar.DAY_OF_MONTH, 1)
                val firstDayOfWeek = monthCal.get(Calendar.DAY_OF_WEEK) - 1 // 0-based for Sunday
                val daysInMonth = monthCal.getActualMaximum(Calendar.DAY_OF_MONTH)

                val year = monthCal.get(Calendar.YEAR)
                val month = monthCal.get(Calendar.MONTH) + 1 // 1-based

                val totalCells = ((firstDayOfWeek + daysInMonth + 6) / 7) * 7

                var cellIndex = 0
                val totalWeeks = totalCells / 7

                for (w in 0 until totalWeeks) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        for (d in 0 until 7) {
                            val dayNumber = cellIndex - firstDayOfWeek + 1
                            if (dayNumber in 1..daysInMonth) {
                                val dateStr = "%04d-%02d-%02d".format(year, month, dayNumber)
                                val dayEvents = eventsByDate[dateStr] ?: emptyList()

                                CalendarDayCell(
                                    dayNumber = dayNumber,
                                    dateStr = dateStr,
                                    events = dayEvents,
                                    modifier = Modifier.weight(1f),
                                    onClick = { selectedDateForModal = dateStr }
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                )
                            }
                            cellIndex++
                        }
                    }
                }
            }
        }
    }

    // Interactive Date Events Modal
    if (selectedDateForModal != null) {
        val dateStr = selectedDateForModal!!
        val events = eventsByDate[dateStr] ?: emptyList()

        DateEventsModal(
            dateStr = dateStr,
            events = events,
            onDismiss = { selectedDateForModal = null },
            onOpenBooking = { bookingId ->
                selectedDateForModal = null
                viewModel.openBookingById(bookingId)
            }
        )
    }
}

@Composable
fun CalendarDayCell(
    dayNumber: Int,
    dateStr: String,
    events: List<CalendarEventItem>,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val count = events.size
    val isDoubleBooked = count >= 2
    val hasSingleEvent = count == 1

    val cellBackground = when {
        isDoubleBooked -> CrimsonAlert.copy(alpha = 0.25f)
        hasSingleEvent -> EmeraldSuccess.copy(alpha = 0.18f)
        else -> StudioDarkNavy.copy(alpha = 0.5f)
    }

    val cellBorder = when {
        isDoubleBooked -> CrimsonAlert
        hasSingleEvent -> EmeraldSuccess
        else -> StudioBorderNavy.copy(alpha = 0.3f)
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp))
            .background(cellBackground)
            .border(1.dp, cellBorder, RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(2.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "$dayNumber",
                fontSize = 12.sp,
                fontWeight = if (count > 0) FontWeight.Bold else FontWeight.Normal,
                color = when {
                    isDoubleBooked -> CrimsonAlert
                    hasSingleEvent -> Color.White
                    else -> Color(0xFFCBD5E1)
                }
            )

            // Visual Status Indicator
            if (isDoubleBooked) {
                Text(
                    text = "✔✔",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = CrimsonAlert
                )
            } else if (hasSingleEvent) {
                Text(
                    text = "✔",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldSuccess
                )
            }
        }
    }
}

@Composable
fun CalendarSummaryCard(
    title: String,
    count: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = StudioCardNavy),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, fontSize = 11.sp, color = Color(0xFF94A3B8), textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(4.dp))
            Text(count, fontSize = 18.sp, fontWeight = FontWeight.Black, color = color)
        }
    }
}

@Composable
fun DateEventsModal(
    dateStr: String,
    events: List<CalendarEventItem>,
    onDismiss: () -> Unit,
    onOpenBooking: (Long) -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            color = StudioDarkNavy
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
            ) {
                // Modal Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Booked Events for Date",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8)
                        )
                        Text(
                            text = dateStr,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = AmberGold
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Status Strip
                if (events.size >= 2) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(CrimsonAlert.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                            .border(1.dp, CrimsonAlert, RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "⚠ Double-Booked Alert: ${events.size} Events booked on this date!",
                            color = CrimsonAlert,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                if (events.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No events booked on this date. Studio is fully available!",
                            color = EmeraldSuccess,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(events) { event ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = StudioCardNavy),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = event.rasamName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = AmberGold
                                        )
                                        Text(
                                            text = event.time,
                                            fontSize = 12.sp,
                                            color = Color.White,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = "Client: ${event.customerName} (${event.bookingNumber})",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Phone: ${event.customerMobile}",
                                        fontSize = 12.sp,
                                        color = EmeraldSuccess
                                    )

                                    if (event.venue.isNotBlank()) {
                                        Text(
                                            text = "Venue: ${event.venue}",
                                            fontSize = 11.sp,
                                            color = Color(0xFFCBD5E1)
                                        )
                                    }

                                    Text(
                                        text = "Cameras: ${event.cameras.joinToString(" • ")}",
                                        fontSize = 11.sp,
                                        color = Color(0xFF38BDF8)
                                    )

                                    if (event.routeNote.isNotBlank()) {
                                        Text(
                                            text = "Route: ${event.routeNote}",
                                            fontSize = 11.sp,
                                            color = Color(0xFF94A3B8)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Button(
                                        onClick = { onOpenBooking(event.bookingId) },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(containerColor = AmberGold),
                                        contentPadding = PaddingValues(vertical = 4.dp)
                                    ) {
                                        Icon(Icons.Filled.ReceiptLong, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Open Booking Bill (${event.bookingNumber})", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close", color = Color(0xFF94A3B8))
                }
            }
        }
    }
}
