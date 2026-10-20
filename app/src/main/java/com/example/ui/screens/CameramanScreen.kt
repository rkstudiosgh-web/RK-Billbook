package com.example.ui.screens

import android.content.Context
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
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.CameramanRecord
import com.example.data.model.CameramanSummary
import com.example.ui.components.shareReceiptText
import com.example.ui.theme.AmberGold
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
fun CameramanScreen(
    viewModel: StudioViewModel,
    modifier: Modifier = Modifier
) {
    val records by viewModel.filteredCameramen.collectAsState()
    val allRecords by viewModel.allCameramen.collectAsState()
    val summaries by viewModel.cameramanSummaries.collectAsState()
    val searchQuery by viewModel.cameramanSearchQuery.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var recordToEdit by remember { mutableStateOf<CameramanRecord?>(null) }
    var recordToDelete by remember { mutableStateOf<CameramanRecord?>(null) }
    var selectedCameramanHisab by remember { mutableStateOf<CameramanSummary?>(null) }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {

            // Section 1: Cameraman-Wise Mini Cards Carousel
            Text(
                text = "CAMERAMEN HISAB SUMMARY CARDS",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = AmberGold,
                modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 6.dp)
            )

            if (summaries.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(summaries, key = { it.name }) { summary ->
                        CameramanMiniCard(
                            summary = summary,
                            onOpenHisab = { selectedCameramanHisab = summary }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Search Filter
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setCameramanSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                placeholder = { Text("Search by Cameraman, Customer, Rasam or Date...") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = AmberGold) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AmberGold,
                    unfocusedBorderColor = StudioBorderNavy
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Section 2: Detailed Cameraman Record Sheet
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "RECORD SHEET ENTRIES (${records.size})",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF94A3B8)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            if (records.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No cameraman records found.",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(records, key = { it.id }) { record ->
                        CameramanRecordRow(
                            record = record,
                            onEdit = {
                                recordToEdit = record
                                showAddDialog = true
                            },
                            onAddAdditionalCameraman = {
                                // Add 2nd / 3rd cameraman for the same rasam
                                recordToEdit = record.copy(
                                    id = 0L,
                                    cameramanName = "",
                                    feeAmount = 4000.0,
                                    paidAmount = 0.0,
                                    cameraType = "DSLR",
                                    notes = "Additional cameraman for ${record.rasamName}"
                                )
                                showAddDialog = true
                            },
                            onDelete = { recordToDelete = record }
                        )
                    }
                }
            }
        }

        // FAB to add new manual record
        FloatingActionButton(
            onClick = {
                recordToEdit = null
                showAddDialog = true
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
                Icon(Icons.Filled.Add, contentDescription = "Add")
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add Record", fontWeight = FontWeight.Bold)
            }
        }
    }

    // Add / Edit Cameraman Record Dialog
    if (showAddDialog) {
        AddEditCameramanDialog(
            existing = recordToEdit,
            onDismiss = { showAddDialog = false },
            onSave = { savedRecord ->
                viewModel.saveCameramanRecord(savedRecord)
                showAddDialog = false
            }
        )
    }

    // Choti Hisab Window & Printing Dialog
    if (selectedCameramanHisab != null) {
        val summary = selectedCameramanHisab!!
        val cameramanRecords = allRecords.filter { it.cameramanName.trim().equals(summary.name.trim(), ignoreCase = true) }

        CameramanHisabDialog(
            summary = summary,
            records = cameramanRecords,
            onDismiss = { selectedCameramanHisab = null }
        )
    }

    // Delete confirmation
    if (recordToDelete != null) {
        AlertDialog(
            onDismissRequest = { recordToDelete = null },
            title = { Text("Delete Record?") },
            text = { Text("Delete cameraman entry for ${recordToDelete?.cameramanName} on ${recordToDelete?.eventDate}?") },
            confirmButton = {
                Button(
                    onClick = {
                        recordToDelete?.let { viewModel.deleteCameramanRecord(it) }
                        recordToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonAlert)
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { recordToDelete = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun CameramanMiniCard(
    summary: CameramanSummary,
    onOpenHisab: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(200.dp)
            .clickable { onOpenHisab() },
        colors = CardDefaults.cardColors(containerColor = StudioCardNavy),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(AmberGold),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.Person,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = summary.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Total Events:", fontSize = 11.sp, color = Color(0xFF94A3B8))
                Text("${summary.totalEvents}", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Total Fee:", fontSize = 11.sp, color = Color(0xFF94A3B8))
                Text("Rs. ${"%,.0f".format(summary.totalFee)}", fontSize = 11.sp, color = Color.White)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Paid:", fontSize = 11.sp, color = Color(0xFF94A3B8))
                Text("Rs. ${"%,.0f".format(summary.totalPaid)}", fontSize = 11.sp, color = EmeraldSuccess)
            }
            Spacer(modifier = Modifier.height(4.dp))
            HorizontalDivider(color = StudioBorderNavy)
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Baqaya:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CrimsonAlert)
                Text(
                    "Rs. ${"%,.0f".format(summary.totalBaqaya)}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = if (summary.totalBaqaya > 0) CrimsonAlert else EmeraldSuccess
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onOpenHisab,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = StudioDarkNavy),
                contentPadding = PaddingValues(vertical = 2.dp)
            ) {
                Icon(Icons.Filled.Calculate, contentDescription = null, tint = AmberGold, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("View Hisab", color = AmberGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun CameramanRecordRow(
    record: CameramanRecord,
    onEdit: () -> Unit,
    onAddAdditionalCameraman: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = StudioCardNavy),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header: Cameraman Name, Date, Ownership Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = record.cameramanName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    // Ownership Badge
                    val isStudio = record.cameraOwnership.contains("Studio", ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .background(
                                if (isStudio) Color(0xFF1E3A8A) else Color(0xFF78350F),
                                RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (isStudio) "Studio Cam" else "Own Cam",
                            fontSize = 10.sp,
                            color = if (isStudio) Color(0xFF93C5FD) else AmberGold,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Text(
                    text = record.eventDate,
                    fontSize = 12.sp,
                    color = AmberGold,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Event & Customer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${record.rasamName} (${record.cameraType})",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF38BDF8)
                )
                if (record.customerName.isNotBlank()) {
                    Text(
                        text = "Client: ${record.customerName}",
                        fontSize = 12.sp,
                        color = Color(0xFFCBD5E1)
                    )
                }
            }

            // Barat / Drone Route: Kahan se ➜ Kahan tak
            if (record.routeNote.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Route:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF94A3B8)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = record.routeNote,
                        fontSize = 11.sp,
                        color = AmberGold,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Fees breakdown
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(StudioDarkNavy, RoundedCornerShape(6.dp))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Fee: Rs. ${"%,.0f".format(record.feeAmount)}", fontSize = 12.sp, color = Color.White)
                Text("Paid: Rs. ${"%,.0f".format(record.paidAmount)}", fontSize = 12.sp, color = EmeraldSuccess)
                Text(
                    "Baqaya: Rs. ${"%,.0f".format(record.remainingBalance())}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (record.remainingBalance() > 0) CrimsonAlert else EmeraldSuccess
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Actions: + Cameraman, Edit, Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onAddAdditionalCameraman,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, tint = AmberGold, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("＋ Cameraman", color = AmberGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Filled.Edit, contentDescription = "Edit", tint = Color(0xFF94A3B8), modifier = Modifier.size(16.dp))
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = CrimsonAlert, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
fun AddEditCameramanDialog(
    existing: CameramanRecord?,
    onDismiss: () -> Unit,
    onSave: (CameramanRecord) -> Unit
) {
    val currentDateStr = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    var cameramanName by remember { mutableStateOf(existing?.cameramanName ?: "") }
    var cameramanPhone by remember { mutableStateOf(existing?.cameramanPhone ?: "") }
    var customerName by remember { mutableStateOf(existing?.customerName ?: "") }
    var rasamName by remember { mutableStateOf(existing?.rasamName ?: "Barat") }
    var eventDate by remember { mutableStateOf(existing?.eventDate ?: currentDateStr) }
    var cameraType by remember { mutableStateOf(existing?.cameraType ?: "Video Camera") }
    var cameraOwnership by remember { mutableStateOf(existing?.cameraOwnership ?: "Studio Camera") }
    var routeNote by remember { mutableStateOf(existing?.routeNote ?: "Ghar se ➜ Shadi Hall tak") }
    var feeAmountStr by remember {
        mutableStateOf(if (existing != null) existing.feeAmount.toLong().toString() else "4000")
    }
    var paidAmountStr by remember {
        mutableStateOf(if (existing != null) existing.paidAmount.toLong().toString() else "2000")
    }
    var notes by remember { mutableStateOf(existing?.notes ?: "") }

    val cameraTypes = listOf("Video Camera", "DSLR", "Drone", "Gimbal")
    val ownerships = listOf("Studio Camera", "Cameraman Own")

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            color = StudioDarkNavy
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = if (existing == null || existing.id == 0L) "Add Cameraman Record" else "Edit Cameraman Entry",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = cameramanName,
                    onValueChange = { cameramanName = it },
                    label = { Text("Cameraman Name *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = cameramanPhone,
                    onValueChange = { cameramanPhone = it },
                    label = { Text("Cameraman Phone") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = rasamName,
                        onValueChange = { rasamName = it },
                        label = { Text("Rasam (Mehndi/Barat)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = eventDate,
                        onValueChange = { eventDate = it },
                        label = { Text("Event Date") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = customerName,
                    onValueChange = { customerName = it },
                    label = { Text("Customer / Client Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Camera Type Selection
                Text("Camera Type:", fontSize = 12.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(cameraTypes) { type ->
                        val selected = cameraType == type
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selected) AmberGold else StudioCardNavy)
                                .clickable { cameraType = type }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = type,
                                fontSize = 12.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                color = if (selected) Color.Black else Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Camera Ownership
                Text("Camera Ownership:", fontSize = 12.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ownerships.forEach { own ->
                        val selected = cameraOwnership == own
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selected) Color(0xFF38BDF8) else StudioCardNavy)
                                .clickable { cameraOwnership = own }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = own,
                                fontSize = 12.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                color = if (selected) Color.Black else Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Barat / Drone Route
                OutlinedTextField(
                    value = routeNote,
                    onValueChange = { routeNote = it },
                    label = { Text("Route: Kahan se ➜ Kahan tak") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Fee and Paid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = feeAmountStr,
                        onValueChange = { feeAmountStr = it },
                        label = { Text("Total Fee (Rs)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = paidAmountStr,
                        onValueChange = { paidAmountStr = it },
                        label = { Text("Paid Amount (Rs)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            if (cameramanName.isNotBlank()) {
                                onSave(
                                    CameramanRecord(
                                        id = existing?.id ?: 0L,
                                        cameramanName = cameramanName.trim(),
                                        cameramanPhone = cameramanPhone.trim(),
                                        bookingId = existing?.bookingId ?: 0L,
                                        customerName = customerName.trim(),
                                        rasamName = rasamName.trim(),
                                        eventDate = eventDate.trim(),
                                        cameraType = cameraType,
                                        cameraOwnership = cameraOwnership,
                                        routeNote = routeNote.trim(),
                                        feeAmount = feeAmountStr.toDoubleOrNull() ?: 0.0,
                                        paidAmount = paidAmountStr.toDoubleOrNull() ?: 0.0,
                                        notes = notes.trim(),
                                        createdAt = existing?.createdAt ?: System.currentTimeMillis()
                                    )
                                )
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = cameramanName.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = AmberGold)
                    ) {
                        Text("Save Record", color = Color.Black, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("Cancel", color = Color(0xFF94A3B8))
                    }
                }
            }
        }
    }
}

@Composable
fun CameramanHisabDialog(
    summary: CameramanSummary,
    records: List<CameramanRecord>,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    val printableStatement = buildString {
        appendLine("========================================")
        appendLine("           RK PHOTO & VIDEO STUDIO       ")
        appendLine("           CAMERAMAN HISAB STATEMENT     ")
        appendLine("========================================")
        appendLine("Cameraman: ${summary.name}")
        if (summary.phone.isNotBlank()) appendLine("Phone    : ${summary.phone}")
        appendLine("Date     : ${SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())}")
        appendLine("----------------------------------------")
        appendLine("EVENT LOG:")
        records.forEachIndexed { i, r ->
            appendLine("${i + 1}. ${r.eventDate} - ${r.rasamName} (${r.cameraType})")
            appendLine("   Client: ${r.customerName}")
            appendLine("   Route : ${r.routeNote.ifEmpty { "N/A" }}")
            appendLine("   Fee   : Rs. ${"%,.0f".format(r.feeAmount)} | Paid: Rs. ${"%,.0f".format(r.paidAmount)} | Baqaya: Rs. ${"%,.0f".format(r.remainingBalance())}")
        }
        appendLine("----------------------------------------")
        appendLine("TOTAL EVENTS : ${summary.totalEvents}")
        appendLine("TOTAL FEE    : Rs. ${"%,.0f".format(summary.totalFee)}")
        appendLine("TOTAL PAID   : Rs. ${"%,.0f".format(summary.totalPaid)}")
        appendLine("TOTAL BAQAYA : Rs. ${"%,.0f".format(summary.totalBaqaya)}")
        appendLine("========================================")
        appendLine("Cameraman Signature: __________________")
        appendLine("Studio Manager Sign: __________________")
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            color = StudioDarkNavy
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Cameraman Hisab Window",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8)
                        )
                        Text(
                            text = summary.name,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = AmberGold
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Summary Strip
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(StudioCardNavy, RoundedCornerShape(10.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Total Fee", fontSize = 11.sp, color = Color(0xFF94A3B8))
                        Text("Rs. ${"%,.0f".format(summary.totalFee)}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Column {
                        Text("Paid", fontSize = 11.sp, color = Color(0xFF94A3B8))
                        Text("Rs. ${"%,.0f".format(summary.totalPaid)}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = EmeraldSuccess)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Baqaya (Balance)", fontSize = 11.sp, color = Color(0xFF94A3B8))
                        Text(
                            "Rs. ${"%,.0f".format(summary.totalBaqaya)}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = if (summary.totalBaqaya > 0) CrimsonAlert else EmeraldSuccess
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "INDIVIDUAL EVENT BREAKDOWN (${records.size})",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(8.dp))

                records.forEach { r ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${r.eventDate} • ${r.rasamName}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = r.cameraType,
                                    fontSize = 11.sp,
                                    color = AmberGold
                                )
                            }
                            if (r.customerName.isNotBlank()) {
                                Text("Client: ${r.customerName}", fontSize = 11.sp, color = Color(0xFFCBD5E1))
                            }
                            if (r.routeNote.isNotBlank()) {
                                Text("Route: ${r.routeNote}", fontSize = 10.sp, color = Color(0xFF94A3B8))
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Fee: Rs. ${"%,.0f".format(r.feeAmount)}", fontSize = 11.sp, color = Color.White)
                                Text("Paid: Rs. ${"%,.0f".format(r.paidAmount)}", fontSize = 11.sp, color = EmeraldSuccess)
                                Text("Baqaya: Rs. ${"%,.0f".format(r.remainingBalance())}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (r.remainingBalance() > 0) CrimsonAlert else EmeraldSuccess)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Actions: Print / Share Hisab Slip & Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            shareReceiptText(
                                context,
                                "Hisab - ${summary.name}",
                                printableStatement
                            )
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = AmberGold)
                    ) {
                        Icon(Icons.Filled.Print, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Print / Share Slip", color = Color.Black, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("Close", color = Color(0xFF94A3B8))
                    }
                }
            }
        }
    }
}
