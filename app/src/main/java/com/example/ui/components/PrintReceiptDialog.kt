package com.example.ui.components

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Booking
import com.example.data.model.CameramanRecord
import com.example.data.model.VcrBill
import com.example.data.model.VideoEditingOrder
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CrimsonAlert
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.StudioDarkNavy

fun shareReceiptText(context: Context, title: String, text: String) {
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TITLE, title)
        putExtra(Intent.EXTRA_TEXT, text)
        type = "text/plain"
    }
    val shareIntent = Intent.createChooser(sendIntent, "Print / Share $title")
    context.startActivity(shareIntent)
}

@Composable
fun BookingReceiptModal(
    booking: Booking,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val formattedReceipt = buildString {
        appendLine("========================================")
        appendLine("           RK PHOTO & VIDEO STUDIO       ")
        appendLine("       Program Booking & Payment Receipt  ")
        appendLine("========================================")
        appendLine("Booking No : ${booking.bookingNumber}")
        appendLine("Date       : ${booking.bookingDate}")
        appendLine("Client Name: ${booking.customerName}")
        appendLine("Phone      : ${booking.customerMobile}")
        if (booking.address.isNotBlank()) {
            appendLine("Address    : ${booking.address}")
        }
        appendLine("----------------------------------------")
        appendLine("EVENTS / RASAMS:")
        booking.rasams.forEachIndexed { i, r ->
            appendLine("${i + 1}. ${r.name} - Date: ${r.date} (${r.time})")
            if (r.venue.isNotBlank()) appendLine("   Venue: ${r.venue}")
            appendLine("   Cameras: ${r.cameras.joinToString(", ")}")
            if (r.routeNote.isNotBlank()) appendLine("   Route: ${r.routeNote}")
        }
        appendLine("----------------------------------------")
        appendLine("PAYMENT BREAKDOWN:")
        appendLine("Total Amount    : Rs. ${"%,.0f".format(booking.totalAmount)}")
        appendLine("Advance Received: Rs. ${"%,.0f".format(booking.advanceAmount)}")
        if (booking.payments.isNotEmpty()) {
            appendLine("Additional Jama Next:")
            booking.payments.forEach { p ->
                appendLine("   • ${p.date}: Rs. ${"%,.0f".format(p.amount)} (${p.note})")
            }
        }
        appendLine("Total Paid      : Rs. ${"%,.0f".format(booking.totalPaid())}")
        appendLine("Baqaya (Balance): Rs. ${"%,.0f".format(booking.remainingBalance())}")
        appendLine("Status          : ${booking.paymentStatus()}")
        appendLine("========================================")
        appendLine("Customer Signature: ____________________")
        appendLine("Studio Signature  : ____________________")
        appendLine("Thank you for choosing RK Studio!")
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .background(Color.Transparent),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Modal Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "A4 Printable Bill Preview",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Printable A4 Styled Sheet Container
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(8.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(8.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        // Studio Header
                        Text(
                            text = "RK PHOTO & VIDEO STUDIO",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = StudioDarkNavy,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = "Professional Wedding & Event Cinematography",
                            fontSize = 11.sp,
                            color = Color(0xFF475569),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = "Phone: 0300-0000000 | Email: rkstudiosgh@gmail.com",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 1.dp)
                        Spacer(modifier = Modifier.height(10.dp))

                        // Bill meta info
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "BILL TO:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF64748B)
                                )
                                Text(
                                    text = booking.customerName,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                                Text(
                                    text = "Mobile: ${booking.customerMobile}",
                                    fontSize = 12.sp,
                                    color = Color(0xFF334155)
                                )
                                if (booking.address.isNotBlank()) {
                                    Text(
                                        text = booking.address,
                                        fontSize = 11.sp,
                                        color = Color(0xFF475569)
                                    )
                                }
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "BOOKING NUMBER",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF64748B)
                                )
                                Text(
                                    text = booking.bookingNumber,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = AmberGold
                                )
                                Text(
                                    text = "Date: ${booking.bookingDate}",
                                    fontSize = 12.sp,
                                    color = Color(0xFF334155)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Rasams list
                        Text(
                            text = "PROGRAM EVENTS & RASAM SCHEDULE",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = StudioDarkNavy
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        booking.rasams.forEachIndexed { idx, rasam ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .background(Color(0xFFF8FAFC), RoundedCornerShape(6.dp))
                                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(6.dp))
                                    .padding(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${idx + 1}. ${rasam.name}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = Color.Black
                                    )
                                    Text(
                                        text = "${rasam.date} • ${rasam.time}",
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 12.sp,
                                        color = Color(0xFF0F766E)
                                    )
                                }
                                if (rasam.venue.isNotBlank()) {
                                    Text(
                                        text = "Venue: ${rasam.venue}",
                                        fontSize = 12.sp,
                                        color = Color(0xFF334155)
                                    )
                                }
                                Text(
                                    text = "Cameras: ${rasam.cameras.joinToString(" • ")}",
                                    fontSize = 11.sp,
                                    color = AmberGold,
                                    fontWeight = FontWeight.SemiBold
                                )
                                if (rasam.routeNote.isNotBlank()) {
                                    Text(
                                        text = "Route: ${rasam.routeNote}",
                                        fontSize = 11.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Financial summary box
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF1F5F9), RoundedCornerShape(8.dp))
                                .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Total Booking Amount:", fontSize = 13.sp, color = Color(0xFF334155))
                                Text(
                                    "Rs. ${"%,.0f".format(booking.totalAmount)}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Advance Paid:", fontSize = 13.sp, color = Color(0xFF334155))
                                Text(
                                    "Rs. ${"%,.0f".format(booking.advanceAmount)}",
                                    fontSize = 13.sp,
                                    color = EmeraldSuccess,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            booking.payments.forEach { p ->
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Jama Next (${p.date} - ${p.note}):", fontSize = 12.sp, color = Color(0xFF64748B))
                                    Text(
                                        "Rs. ${"%,.0f".format(p.amount)}",
                                        fontSize = 12.sp,
                                        color = EmeraldSuccess,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            HorizontalDivider(color = Color(0xFFCBD5E1))
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "BAQAYA (Remaining Balance):",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (booking.remainingBalance() > 0) CrimsonAlert else EmeraldSuccess
                                )
                                Text(
                                    "Rs. ${"%,.0f".format(booking.remainingBalance())}",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (booking.remainingBalance() > 0) CrimsonAlert else EmeraldSuccess
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Signature Lines
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .width(130.dp)
                                        .height(1.dp)
                                        .background(Color.Black)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Customer Signature", fontSize = 11.sp, color = Color(0xFF334155))
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .width(130.dp)
                                        .height(1.dp)
                                        .background(Color.Black)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Authorized RK Studio", fontSize = 11.sp, color = Color(0xFF334155))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            shareReceiptText(
                                context,
                                "Booking Bill ${booking.bookingNumber}",
                                formattedReceipt
                            )
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = AmberGold)
                    ) {
                        Icon(Icons.Filled.Share, contentDescription = null, tint = Color.Black)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Share / Print Bill", color = Color.Black, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Close")
                    }
                }
            }
        }
    }
}
