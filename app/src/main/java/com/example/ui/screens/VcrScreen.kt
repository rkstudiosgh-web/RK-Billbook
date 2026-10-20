package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.model.CassetteItem
import com.example.data.model.VcrBill
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
fun VcrScreen(
    viewModel: StudioViewModel,
    modifier: Modifier = Modifier
) {
    val bills by viewModel.filteredVcrBills.collectAsState()
    val allBills by viewModel.allVcrBills.collectAsState()
    val searchQuery by viewModel.vcrSearchQuery.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var billToEdit by remember { mutableStateOf<VcrBill?>(null) }
    var billToDelete by remember { mutableStateOf<VcrBill?>(null) }
    var billToPrintA4 by remember { mutableStateOf<VcrBill?>(null) }

    val totalVcrBills = allBills.size
    val totalTapes = allBills.sumOf { b -> b.cassettes.sumOf { it.quantity } }
    val totalBilling = allBills.sumOf { it.calculatedTotal() }
    val totalBaqaya = allBills.sumOf { it.remainingBalance() }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {

            // Top Summary Row
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item { MiniStatCard("Total VCR Bills", "$totalVcrBills", AmberGold, "Customers") }
                item { MiniStatCard("Total Tapes", "$totalTapes Tapes", Color(0xFF38BDF8), "Cassettes") }
                item { MiniStatCard("Total Billing", "Rs. ${"%,.0f".format(totalBilling)}", EmeraldSuccess, "Work Value") }
                item { MiniStatCard("Baqaya Balance", "Rs. ${"%,.0f".format(totalBaqaya)}", CrimsonAlert, "Pending") }
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setVcrSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                placeholder = { Text("Search VCR Customer, Phone or Bill #...") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = AmberGold) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setVcrSearchQuery("") }) {
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

            // VCR Bills List
            if (bills.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Filled.Album,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(60.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (searchQuery.isBlank()) "No VCR transfer bills yet. Add one below!" else "No bills match '$searchQuery'",
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
                    items(bills, key = { it.id }) { bill ->
                        VcrBillCard(
                            bill = bill,
                            onOpenPrint = { billToPrintA4 = bill },
                            onEdit = {
                                billToEdit = bill
                                showAddDialog = true
                            },
                            onDelete = { billToDelete = bill }
                        )
                    }
                }
            }
        }

        // FAB to add new VCR Bill
        FloatingActionButton(
            onClick = {
                billToEdit = null
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
                Text("New VCR Bill", fontWeight = FontWeight.Bold)
            }
        }
    }

    // A4 Printable VCR Bill Modal
    if (billToPrintA4 != null) {
        VcrA4BillModal(
            bill = billToPrintA4!!,
            onDismiss = { billToPrintA4 = null }
        )
    }

    // Add / Edit VCR Bill Dialog
    if (showAddDialog) {
        AddEditVcrBillDialog(
            existing = billToEdit,
            suggestedBillNumber = viewModel.getNextVcrBillNumber(),
            onDismiss = { showAddDialog = false },
            onSave = { savedBill ->
                viewModel.saveVcrBill(savedBill)
                showAddDialog = false
            }
        )
    }

    // Delete confirmation
    if (billToDelete != null) {
        AlertDialog(
            onDismissRequest = { billToDelete = null },
            title = { Text("Delete VCR Bill?") },
            text = { Text("Are you sure you want to delete bill ${billToDelete?.billNumber} for ${billToDelete?.customerName}?") },
            confirmButton = {
                Button(
                    onClick = {
                        billToDelete?.let { viewModel.deleteVcrBill(it) }
                        billToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonAlert)
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { billToDelete = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun VcrBillCard(
    bill: VcrBill,
    onOpenPrint: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenPrint() },
        colors = CardDefaults.cardColors(containerColor = StudioCardNavy),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Bill #, Customer Name, Date
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
                            text = bill.billNumber,
                            color = AmberGold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = bill.customerName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }

                Text(text = bill.date, fontSize = 12.sp, color = Color(0xFF94A3B8))
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Phone
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Call, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = bill.customerPhone, fontSize = 12.sp, color = EmeraldSuccess, fontWeight = FontWeight.Medium)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Cassettes Summary Table
            Text(
                text = "CASSETTE ITEMS (${bill.cassettes.size}):",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF94A3B8)
            )
            Spacer(modifier = Modifier.height(4.dp))

            bill.cassettes.forEach { c ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp)
                        .background(StudioDarkNavy.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${c.label} (${c.type})",
                        fontSize = 12.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${c.quantity}x @ ${c.rate.toInt()} = Rs. ${c.amount.toInt()}",
                        fontSize = 12.sp,
                        color = AmberGold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Transfer Mode Options
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Transfer Mode: ", fontSize = 11.sp, color = Color(0xFF94A3B8))
                Text(
                    text = bill.transferModes.joinToString(", ").ifEmpty { "Customer USB" },
                    fontSize = 11.sp,
                    color = Color(0xFF38BDF8),
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Photo preview if available
            if (!bill.tapePhotoUri.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model = bill.tapePhotoUri,
                        contentDescription = "Tape Photo",
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(6.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Original Cassette Photo Attached", fontSize = 11.sp, color = EmeraldSuccess)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Financial breakdown
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0B1120), RoundedCornerShape(8.dp))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Total: Rs. ${"%,.0f".format(bill.calculatedTotal())}", fontSize = 12.sp, color = Color.White)
                Text("Advance: Rs. ${"%,.0f".format(bill.advanceAmount)}", fontSize = 12.sp, color = EmeraldSuccess)
                Text(
                    "Baqaya: Rs. ${"%,.0f".format(bill.remainingBalance())}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (bill.remainingBalance() > 0) CrimsonAlert else EmeraldSuccess
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
                    onClick = onOpenPrint,
                    colors = ButtonDefaults.buttonColors(containerColor = AmberGold),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                ) {
                    Icon(Icons.Filled.Print, contentDescription = null, tint = Color.Black, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("A4 VCR Bill", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
fun AddEditVcrBillDialog(
    existing: VcrBill?,
    suggestedBillNumber: String,
    onDismiss: () -> Unit,
    onSave: (VcrBill) -> Unit
) {
    val currentDateStr = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    var billNumber by remember { mutableStateOf(existing?.billNumber ?: suggestedBillNumber) }
    var customerName by remember { mutableStateOf(existing?.customerName ?: "") }
    var customerPhone by remember { mutableStateOf(existing?.customerPhone ?: "") }
    var date by remember { mutableStateOf(existing?.date ?: currentDateStr) }
    var advanceAmountStr by remember {
        mutableStateOf(if (existing != null) existing.advanceAmount.toLong().toString() else "500")
    }
    var notes by remember { mutableStateOf(existing?.notes ?: "") }
    var tapePhotoUri by remember { mutableStateOf<String?>(existing?.tapePhotoUri) }

    // Cassette items table state
    val cassettes = remember {
        mutableStateListOf<CassetteItem>().apply {
            if (existing != null && existing.cassettes.isNotEmpty()) {
                addAll(existing.cassettes)
            } else {
                add(
                    CassetteItem(
                        label = "Family Shadi 1998",
                        type = "VHS Big Tape",
                        quantity = 2,
                        rate = 400.0,
                        amount = 800.0
                    )
                )
            }
        }
    }

    // Transfer mode checklist
    val transferModes = remember {
        mutableStateListOf<String>().apply {
            if (existing != null && existing.transferModes.isNotEmpty()) {
                addAll(existing.transferModes)
            } else {
                addAll(listOf("Customer USB", "Mobile transfer"))
            }
        }
    }

    // Photo picker launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            tapePhotoUri = uri.toString()
        }
    }

    val tapeTypes = listOf("VHS Big Tape", "Mini DV", "Hi8", "Audio Cassette", "Betamax")
    val availableModes = listOf("Customer USB", "Studio USB", "Mobile transfer")

    val calculatedTotal = cassettes.sumOf { it.amount }
    val advanceAmount = advanceAmountStr.toDoubleOrNull() ?: 0.0
    val baqaya = maxOf(0.0, calculatedTotal - advanceAmount)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
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
                    text = if (existing == null) "New VCR Cassette Transfer Bill" else "Edit VCR Bill ${existing.billNumber}",
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
                        value = billNumber,
                        onValueChange = { billNumber = it },
                        label = { Text("Bill #") },
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

                Spacer(modifier = Modifier.height(14.dp))

                // Cassette Details Table
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CASSETTE DETAILS TABLE (${cassettes.size})",
                        fontWeight = FontWeight.Bold,
                        color = AmberGold,
                        fontSize = 13.sp
                    )
                    Button(
                        onClick = {
                            cassettes.add(
                                CassetteItem(
                                    label = "Tape ${cassettes.size + 1}",
                                    type = "VHS Big Tape",
                                    quantity = 1,
                                    rate = 400.0,
                                    amount = 400.0
                                )
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AmberGold),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("+ Add Tape", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                cassettes.forEachIndexed { idx, item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = StudioCardNavy),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Tape #${idx + 1}", color = AmberGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                if (cassettes.size > 1) {
                                    IconButton(onClick = { cassettes.removeAt(idx) }, modifier = Modifier.size(24.dp)) {
                                        Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = CrimsonAlert, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            OutlinedTextField(
                                value = item.label,
                                onValueChange = { cassettes[idx] = item.copy(label = it) },
                                label = { Text("Tape Label / Title (e.g. Wedding 1999)") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            // Type chips
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(tapeTypes) { type ->
                                    val sel = item.type == type
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (sel) AmberGold else StudioDarkNavy)
                                            .clickable {
                                                val defaultRate = when (type) {
                                                    "VHS Big Tape" -> 400.0
                                                    "Mini DV" -> 450.0
                                                    "Hi8" -> 450.0
                                                    "Audio Cassette" -> 250.0
                                                    else -> 500.0
                                                }
                                                cassettes[idx] = item.copy(
                                                    type = type,
                                                    rate = defaultRate,
                                                    amount = defaultRate * item.quantity
                                                )
                                            }
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = type,
                                            fontSize = 11.sp,
                                            fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal,
                                            color = if (sel) Color.Black else Color.White
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = item.quantity.toString(),
                                    onValueChange = {
                                        val q = it.toIntOrNull() ?: 1
                                        cassettes[idx] = item.copy(quantity = q, amount = q * item.rate)
                                    },
                                    label = { Text("Qty") },
                                    modifier = Modifier.weight(1f),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = item.rate.toInt().toString(),
                                    onValueChange = {
                                        val r = it.toDoubleOrNull() ?: 0.0
                                        cassettes[idx] = item.copy(rate = r, amount = item.quantity * r)
                                    },
                                    label = { Text("Rate (Rs)") },
                                    modifier = Modifier.weight(1.2f),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = "Rs. ${item.amount.toInt()}",
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Amount") },
                                    modifier = Modifier.weight(1.2f),
                                    singleLine = true
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Transfer Mode Options Checklist
                Text("Transfer Mode Checklist:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    availableModes.forEach { mode ->
                        val checked = transferModes.contains(mode)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable {
                                if (checked) transferModes.remove(mode) else transferModes.add(mode)
                            }
                        ) {
                            Checkbox(
                                checked = checked,
                                onCheckedChange = { isChecked ->
                                    if (isChecked) transferModes.add(mode) else transferModes.remove(mode)
                                },
                                colors = CheckboxDefaults.colors(checkedColor = AmberGold)
                            )
                            Text(mode, fontSize = 11.sp, color = if (checked) Color.White else Color(0xFF94A3B8))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Tape Photo Upload / Preview
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = StudioCardNavy),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Original Cassette Photo:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text(
                                if (tapePhotoUri != null) "Photo attached successfully" else "No photo selected",
                                fontSize = 11.sp,
                                color = if (tapePhotoUri != null) EmeraldSuccess else Color(0xFF94A3B8)
                            )
                        }

                        Button(
                            onClick = { photoPickerLauncher.launch("image/*") },
                            colors = ButtonDefaults.buttonColors(containerColor = StudioDarkNavy)
                        ) {
                            Icon(Icons.Filled.AddPhotoAlternate, contentDescription = null, tint = AmberGold, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (tapePhotoUri != null) "Change Photo" else "Upload Photo", color = AmberGold, fontSize = 11.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Billing and Advance calculations
                OutlinedTextField(
                    value = advanceAmountStr,
                    onValueChange = { advanceAmountStr = it },
                    label = { Text("Advance Paid (Rs)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Calculation summary box
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(StudioCardNavy, RoundedCornerShape(8.dp))
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Total: Rs. ${"%,.0f".format(calculatedTotal)}", color = Color.White, fontWeight = FontWeight.Bold)
                    Text("Advance: Rs. ${"%,.0f".format(advanceAmount)}", color = EmeraldSuccess, fontWeight = FontWeight.Bold)
                    Text(
                        "Baqaya: Rs. ${"%,.0f".format(baqaya)}",
                        color = if (baqaya > 0) CrimsonAlert else EmeraldSuccess,
                        fontWeight = FontWeight.Black
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            if (customerName.isNotBlank() && customerPhone.isNotBlank()) {
                                onSave(
                                    VcrBill(
                                        id = existing?.id ?: 0L,
                                        billNumber = billNumber.ifBlank { suggestedBillNumber },
                                        customerName = customerName.trim(),
                                        customerPhone = customerPhone.trim(),
                                        date = date,
                                        cassettes = cassettes.toList(),
                                        transferModes = transferModes.toList(),
                                        tapePhotoUri = tapePhotoUri,
                                        totalAmount = calculatedTotal,
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
                        Text("Save VCR Bill", color = Color.Black, fontWeight = FontWeight.Bold)
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
fun VcrA4BillModal(
    bill: VcrBill,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    val printableBill = buildString {
        appendLine("========================================")
        appendLine("           RK PHOTO & VIDEO STUDIO       ")
        appendLine("       VCR CASSETTE CONVERSION INVOICE   ")
        appendLine("========================================")
        appendLine("Invoice No : ${bill.billNumber}")
        appendLine("Date       : ${bill.date}")
        appendLine("Customer   : ${bill.customerName}")
        appendLine("Phone      : ${bill.customerPhone}")
        appendLine("Transfer To: ${bill.transferModes.joinToString(", ")}")
        appendLine("----------------------------------------")
        appendLine("CASSETTES DETAILS:")
        bill.cassettes.forEachIndexed { i, c ->
            appendLine("${i + 1}. ${c.label} (${c.type})")
            appendLine("   Qty: ${c.quantity} | Rate: Rs. ${c.rate.toInt()} | Amount: Rs. ${c.amount.toInt()}")
        }
        appendLine("----------------------------------------")
        appendLine("TOTAL BILLING : Rs. ${"%,.0f".format(bill.calculatedTotal())}")
        appendLine("ADVANCE PAID  : Rs. ${"%,.0f".format(bill.advanceAmount)}")
        appendLine("BAQAYA BALANCE: Rs. ${"%,.0f".format(bill.remainingBalance())}")
        appendLine("========================================")
        appendLine("Customer Signature: ____________________")
        appendLine("Studio Signature  : ____________________")
        appendLine("Preserve Your Golden Memories with RK Studio")
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
                    Text("A4 VCR Conversion Invoice", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = StudioDarkNavy)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color.Black)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Printable A4 Layout Container
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
                            text = "Old Tape Digitalization (VHS, Mini DV, Hi8, Cassettes to USB / Mobile)",
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
                                Text(bill.customerName, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                Text("Phone: ${bill.customerPhone}", fontSize = 12.sp, color = Color(0xFF334155))
                                Text("Transfer Mode: ${bill.transferModes.joinToString(", ")}", fontSize = 11.sp, color = Color(0xFF0F766E))
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(bill.billNumber, fontSize = 16.sp, fontWeight = FontWeight.Black, color = AmberGold)
                                Text("Date: ${bill.date}", fontSize = 11.sp, color = Color(0xFF64748B))
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text("CASSETTE ITEMS LIST", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = StudioDarkNavy)
                        Spacer(modifier = Modifier.height(4.dp))

                        bill.cassettes.forEachIndexed { i, c ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                                    .background(Color(0xFFF8FAFC), RoundedCornerShape(4.dp))
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("${i + 1}. ${c.label}", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Color.Black)
                                    Text(c.type, fontSize = 10.sp, color = Color(0xFF64748B))
                                }
                                Text("${c.quantity}x @ ${c.rate.toInt()} = Rs. ${c.amount.toInt()}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Black)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Financial totals
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF1F5F9), RoundedCornerShape(6.dp))
                                .padding(10.dp)
                        ) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Total Amount:", fontSize = 12.sp, color = Color(0xFF334155))
                                Text("Rs. ${"%,.0f".format(bill.calculatedTotal())}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Advance Paid:", fontSize = 12.sp, color = Color(0xFF334155))
                                Text("Rs. ${"%,.0f".format(bill.advanceAmount)}", fontSize = 12.sp, color = EmeraldSuccess, fontWeight = FontWeight.SemiBold)
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = Color(0xFFCBD5E1))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Baqaya Balance:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = CrimsonAlert)
                                Text("Rs. ${"%,.0f".format(bill.remainingBalance())}", fontSize = 15.sp, fontWeight = FontWeight.Black, color = CrimsonAlert)
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Customer / Studio Signature Lines
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(modifier = Modifier.width(120.dp).height(1.dp).background(Color.Black))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Customer Signature", fontSize = 10.sp, color = Color(0xFF334155))
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(modifier = Modifier.width(120.dp).height(1.dp).background(Color.Black))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Studio Signature", fontSize = 10.sp, color = Color(0xFF334155))
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
                                "VCR Bill ${bill.billNumber}",
                                printableBill
                            )
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = AmberGold)
                    ) {
                        Icon(Icons.Filled.Share, contentDescription = null, tint = Color.Black)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Share / Print Bill", color = Color.Black, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("Close", color = Color.Black)
                    }
                }
            }
        }
    }
}
