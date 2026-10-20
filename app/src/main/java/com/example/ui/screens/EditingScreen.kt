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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MovieFilter
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.VideoEditingOrder
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
fun EditingScreen(
    viewModel: StudioViewModel,
    modifier: Modifier = Modifier
) {
    val orders by viewModel.filteredEditingOrders.collectAsState()
    val summary by viewModel.editingSummary.collectAsState()
    val searchQuery by viewModel.editingSearchQuery.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var orderToEdit by remember { mutableStateOf<VideoEditingOrder?>(null) }
    var orderToDelete by remember { mutableStateOf<VideoEditingOrder?>(null) }
    var orderToPrint by remember { mutableStateOf<VideoEditingOrder?>(null) }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {

            // Auto Summary Strip
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item { MiniStatCard("Total Orders", "${summary.totalOrders}", AmberGold, "Work Orders") }
                item { MiniStatCard("Total Billing", "Rs. ${"%,.0f".format(summary.totalBilling)}", Color(0xFF38BDF8), "Editing") }
                item { MiniStatCard("Total Advance", "Rs. ${"%,.0f".format(summary.totalAdvance)}", EmeraldSuccess, "Received") }
                item { MiniStatCard("Total Baqaya", "Rs. ${"%,.0f".format(summary.totalBaqaya)}", CrimsonAlert, "Pending") }
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setEditingSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                placeholder = { Text("Search Editing Order #, Customer, Status...") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = AmberGold) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setEditingSearchQuery("") }) {
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

            // Orders list
            if (orders.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Filled.MovieFilter,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(60.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (searchQuery.isBlank()) "No video editing orders yet. Add one below!" else "No orders match '$searchQuery'",
                            color = Color.Gray,
                            fontSize = 14.sp
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
                    items(orders, key = { it.id }) { order ->
                        EditingOrderCard(
                            order = order,
                            onPrintInvoice = { orderToPrint = order },
                            onEdit = {
                                orderToEdit = order
                                showAddDialog = true
                            },
                            onDelete = { orderToDelete = order }
                        )
                    }
                }
            }
        }

        // FAB to add new Editing Order
        FloatingActionButton(
            onClick = {
                orderToEdit = null
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
                Text("New Order", fontWeight = FontWeight.Bold)
            }
        }
    }

    // Dedicated Invoice Printing Modal
    if (orderToPrint != null) {
        EditingInvoiceModal(
            order = orderToPrint!!,
            onDismiss = { orderToPrint = null }
        )
    }

    // Add / Edit Order Dialog
    if (showAddDialog) {
        AddEditEditingDialog(
            existing = orderToEdit,
            suggestedOrderNumber = viewModel.getNextEditingOrderNumber(),
            onDismiss = { showAddDialog = false },
            onSave = { savedOrder ->
                viewModel.saveEditingOrder(savedOrder)
                showAddDialog = false
            }
        )
    }

    // Delete confirmation
    if (orderToDelete != null) {
        AlertDialog(
            onDismissRequest = { orderToDelete = null },
            title = { Text("Delete Editing Order?") },
            text = { Text("Are you sure you want to delete order ${orderToDelete?.orderNumber} for ${orderToDelete?.customerName}?") },
            confirmButton = {
                Button(
                    onClick = {
                        orderToDelete?.let { viewModel.deleteEditingOrder(it) }
                        orderToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonAlert)
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { orderToDelete = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun EditingOrderCard(
    order: VideoEditingOrder,
    onPrintInvoice: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val statusColor = when (order.status) {
        "Completed", "Delivered" -> EmeraldSuccess
        "In Progress" -> AmberGold
        "Review" -> Color(0xFF38BDF8)
        else -> CrimsonAlert
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onPrintInvoice() },
        colors = CardDefaults.cardColors(containerColor = StudioCardNavy),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header
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
                        Text(order.orderNumber, color = AmberGold, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(order.customerName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                }

                Box(
                    modifier = Modifier
                        .background(statusColor.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                        .padding(horizontal = 10.dp, vertical = 3.dp)
                ) {
                    Text(order.status, color = statusColor, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Phone and Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Call, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(order.customerPhone, fontSize = 12.sp, color = EmeraldSuccess, fontWeight = FontWeight.Medium)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text("Date: ${order.date}", fontSize = 11.sp, color = Color(0xFF94A3B8))
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Specs: Duration & Data Size (e.g. 3 Hours / 128 GB)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(StudioDarkNavy, RoundedCornerShape(8.dp))
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Video Duration", fontSize = 10.sp, color = Color(0xFF94A3B8))
                    Text(order.videoDuration, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Column {
                    Text("Data Size", fontSize = 10.sp, color = Color(0xFF94A3B8))
                    Text(order.dataSize, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8))
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Delivery Media", fontSize = 10.sp, color = Color(0xFF94A3B8))
                    Text(order.deliveryMedia.joinToString(", ").ifEmpty { "USB" }, fontSize = 12.sp, color = AmberGold)
                }
            }

            if (order.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text("Note: ${order.notes}", fontSize = 11.sp, color = Color(0xFFCBD5E1))
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Financial Summary
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0B1120), RoundedCornerShape(8.dp))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Total: Rs. ${"%,.0f".format(order.totalAmount)}", fontSize = 12.sp, color = Color.White)
                Text("Advance: Rs. ${"%,.0f".format(order.advanceAmount)}", fontSize = 12.sp, color = EmeraldSuccess)
                Text(
                    "Baqaya: Rs. ${"%,.0f".format(order.remainingBalance())}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (order.remainingBalance() > 0) CrimsonAlert else EmeraldSuccess
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onPrintInvoice,
                    colors = ButtonDefaults.buttonColors(containerColor = AmberGold),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                ) {
                    Icon(Icons.Filled.Print, contentDescription = null, tint = Color.Black, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Print Receipt", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
fun AddEditEditingDialog(
    existing: VideoEditingOrder?,
    suggestedOrderNumber: String,
    onDismiss: () -> Unit,
    onSave: (VideoEditingOrder) -> Unit
) {
    val currentDateStr = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    var orderNumber by remember { mutableStateOf(existing?.orderNumber ?: suggestedOrderNumber) }
    var customerName by remember { mutableStateOf(existing?.customerName ?: "") }
    var customerPhone by remember { mutableStateOf(existing?.customerPhone ?: "") }
    var date by remember { mutableStateOf(existing?.date ?: currentDateStr) }
    var videoDuration by remember { mutableStateOf(existing?.videoDuration ?: "3 Hours") }
    var dataSize by remember { mutableStateOf(existing?.dataSize ?: "128 GB") }
    var status by remember { mutableStateOf(existing?.status ?: "In Progress") }
    var totalAmountStr by remember {
        mutableStateOf(if (existing != null) existing.totalAmount.toLong().toString() else "10000")
    }
    var advanceAmountStr by remember {
        mutableStateOf(if (existing != null) existing.advanceAmount.toLong().toString() else "5000")
    }
    var notes by remember { mutableStateOf(existing?.notes ?: "") }

    val availableMedia = listOf("Customer USB", "Studio USB", "Portable Disk", "Google Drive")
    val selectedMedia = remember {
        mutableStateListOf<String>().apply {
            if (existing != null && existing.deliveryMedia.isNotEmpty()) {
                addAll(existing.deliveryMedia)
            } else {
                add("Customer USB")
            }
        }
    }

    val statusOptions = listOf("Pending", "In Progress", "Review", "Completed", "Delivered")

    val totalAmount = totalAmountStr.toDoubleOrNull() ?: 0.0
    val advanceAmount = advanceAmountStr.toDoubleOrNull() ?: 0.0
    val baqaya = maxOf(0.0, totalAmount - advanceAmount)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 14.dp),
            shape = RoundedCornerShape(16.dp),
            color = StudioDarkNavy
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = if (existing == null) "New Video Editing Work Order" else "Edit Order ${existing.orderNumber}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = orderNumber,
                        onValueChange = { orderNumber = it },
                        label = { Text("Order #") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = date,
                        onValueChange = { date = it },
                        label = { Text("Date") },
                        modifier = Modifier.weight(1.2f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = customerName,
                    onValueChange = { customerName = it },
                    label = { Text("Customer Name *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = customerPhone,
                    onValueChange = { customerPhone = it },
                    label = { Text("Phone Number *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Duration & Data size
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = videoDuration,
                        onValueChange = { videoDuration = it },
                        label = { Text("Video Duration (e.g. 3 Hours)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = dataSize,
                        onValueChange = { dataSize = it },
                        label = { Text("Data Size (e.g. 128 GB)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Delivery Media Checklist
                Text("Delivery Media Checklist:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    availableMedia.forEach { media ->
                        val checked = selectedMedia.contains(media)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable {
                                if (checked) selectedMedia.remove(media) else selectedMedia.add(media)
                            }
                        ) {
                            Checkbox(
                                checked = checked,
                                onCheckedChange = { isChecked ->
                                    if (isChecked) selectedMedia.add(media) else selectedMedia.remove(media)
                                },
                                colors = CheckboxDefaults.colors(checkedColor = AmberGold)
                            )
                            Text(media, fontSize = 11.sp, color = if (checked) Color.White else Color(0xFF94A3B8))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Status Chips
                Text("Order Status:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(statusOptions) { opt ->
                        val sel = status == opt
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (sel) AmberGold else StudioCardNavy)
                                .clickable { status = opt }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = opt,
                                fontSize = 11.sp,
                                fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal,
                                color = if (sel) Color.Black else Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Amounts
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = totalAmountStr,
                        onValueChange = { totalAmountStr = it },
                        label = { Text("Total Amount (Rs)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = advanceAmountStr,
                        onValueChange = { advanceAmountStr = it },
                        label = { Text("Advance Paid (Rs)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Baqaya Strip
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(StudioCardNavy, RoundedCornerShape(8.dp))
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Total: Rs. ${"%,.0f".format(totalAmount)}", color = Color.White, fontWeight = FontWeight.Bold)
                    Text("Advance: Rs. ${"%,.0f".format(advanceAmount)}", color = EmeraldSuccess, fontWeight = FontWeight.Bold)
                    Text(
                        "Baqaya: Rs. ${"%,.0f".format(baqaya)}",
                        color = if (baqaya > 0) CrimsonAlert else EmeraldSuccess,
                        fontWeight = FontWeight.Black
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Editing Instructions (Effects, Music, Song Choice)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            if (customerName.isNotBlank() && customerPhone.isNotBlank()) {
                                onSave(
                                    VideoEditingOrder(
                                        id = existing?.id ?: 0L,
                                        orderNumber = orderNumber.ifBlank { suggestedOrderNumber },
                                        customerName = customerName.trim(),
                                        customerPhone = customerPhone.trim(),
                                        date = date,
                                        videoDuration = videoDuration.trim(),
                                        dataSize = dataSize.trim(),
                                        deliveryMedia = selectedMedia.toList(),
                                        status = status,
                                        totalAmount = totalAmount,
                                        advanceAmount = advanceAmount,
                                        notes = notes.trim(),
                                        createdAt = existing?.createdAt ?: System.currentTimeMillis()
                                    )
                                )
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = customerName.isNotBlank() && customerPhone.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = AmberGold)
                    ) {
                        Text("Save Order", color = Color.Black, fontWeight = FontWeight.Bold)
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
fun EditingInvoiceModal(
    order: VideoEditingOrder,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    val invoiceText = buildString {
        appendLine("========================================")
        appendLine("           RK PHOTO & VIDEO STUDIO       ")
        appendLine("         VIDEO EDITING WORK INVOICE      ")
        appendLine("========================================")
        appendLine("Order No : ${order.orderNumber}")
        appendLine("Date     : ${order.date}")
        appendLine("Customer : ${order.customerName}")
        appendLine("Phone    : ${order.customerPhone}")
        appendLine("Status   : ${order.status}")
        appendLine("----------------------------------------")
        appendLine("PROJECT SPECIFICATIONS:")
        appendLine("• Video Duration : ${order.videoDuration}")
        appendLine("• Raw Data Size  : ${order.dataSize}")
        appendLine("• Delivery Media : ${order.deliveryMedia.joinToString(", ")}")
        if (order.notes.isNotBlank()) appendLine("• Notes / Songs  : ${order.notes}")
        appendLine("----------------------------------------")
        appendLine("TOTAL BILLING : Rs. ${"%,.0f".format(order.totalAmount)}")
        appendLine("ADVANCE PAID  : Rs. ${"%,.0f".format(order.advanceAmount)}")
        appendLine("BAQAYA BALANCE: Rs. ${"%,.0f".format(order.remainingBalance())}")
        appendLine("========================================")
        appendLine("Customer Signature: ____________________")
        appendLine("Studio Editor Sign: ____________________")
        appendLine("Cinematic Post-Production by RK Studio")
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
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Video Editing Receipt", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = StudioDarkNavy)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color.Black)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(8.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "RK PHOTO & VIDEO STUDIO",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = StudioDarkNavy,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = "Cinematic Video Editing & Post-Production Department",
                            fontSize = 11.sp,
                            color = Color(0xFF475569),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = Color(0xFFE2E8F0))
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("CUSTOMER:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                                Text(order.customerName, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                Text("Phone: ${order.customerPhone}", fontSize = 12.sp, color = Color(0xFF334155))
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(order.orderNumber, fontSize = 16.sp, fontWeight = FontWeight.Black, color = AmberGold)
                                Text("Date: ${order.date}", fontSize = 11.sp, color = Color(0xFF64748B))
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF8FAFC), RoundedCornerShape(6.dp))
                                .padding(10.dp)
                        ) {
                            Text("ORDER SPECIFICATIONS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = StudioDarkNavy)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("• Video Duration: ${order.videoDuration}", fontSize = 12.sp, color = Color.Black)
                            Text("• Data Size: ${order.dataSize}", fontSize = 12.sp, color = Color.Black)
                            Text("• Delivery Media: ${order.deliveryMedia.joinToString(", ")}", fontSize = 12.sp, color = Color(0xFF0F766E))
                            Text("• Status: ${order.status}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AmberGold)
                            if (order.notes.isNotBlank()) {
                                Text("• Notes: ${order.notes}", fontSize = 11.sp, color = Color(0xFF475569))
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF1F5F9), RoundedCornerShape(6.dp))
                                .padding(10.dp)
                        ) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Total Amount:", fontSize = 12.sp, color = Color(0xFF334155))
                                Text("Rs. ${"%,.0f".format(order.totalAmount)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Advance Paid:", fontSize = 12.sp, color = Color(0xFF334155))
                                Text("Rs. ${"%,.0f".format(order.advanceAmount)}", fontSize = 12.sp, color = EmeraldSuccess, fontWeight = FontWeight.SemiBold)
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = Color(0xFFCBD5E1))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Baqaya Balance:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = CrimsonAlert)
                                Text("Rs. ${"%,.0f".format(order.remainingBalance())}", fontSize = 15.sp, fontWeight = FontWeight.Black, color = CrimsonAlert)
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(modifier = Modifier.width(120.dp).height(1.dp).background(Color.Black))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Customer Signature", fontSize = 10.sp, color = Color(0xFF334155))
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(modifier = Modifier.width(120.dp).height(1.dp).background(Color.Black))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Editor Signature", fontSize = 10.sp, color = Color(0xFF334155))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            shareReceiptText(
                                context,
                                "Editing Invoice ${order.orderNumber}",
                                invoiceText
                            )
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = AmberGold)
                    ) {
                        Icon(Icons.Filled.Share, contentDescription = null, tint = Color.Black)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Share / Print Invoice", color = Color.Black, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("Close", color = Color.Black)
                    }
                }
            }
        }
    }
}
