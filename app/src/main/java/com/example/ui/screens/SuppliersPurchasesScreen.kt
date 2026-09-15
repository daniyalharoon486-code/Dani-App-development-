package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.*
import com.example.ui.common.AppStrings
import com.example.ui.common.Formatters
import com.example.ui.viewmodel.DukanViewModel

@Composable
fun SuppliersScreen(
    viewModel: DukanViewModel
) {
    val isUrdu by viewModel.isUrdu.collectAsState()
    val suppliers by viewModel.suppliers.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedSupplierForPayment by remember { mutableStateOf<SupplierEntity?>(null) }
    var selectedSupplierForLedger by remember { mutableStateOf<SupplierEntity?>(null) }

    val totalPayable = suppliers.sumOf { it.currentBalance }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("suppliers_screen")
    ) {
        // Summary Header Card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isUrdu) "سپلائرز کا کل واجب الادا بیلنس" else "Total Supplier Payables",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF92400E)
                    )
                    Text(
                        text = Formatters.formatPkr(totalPayable),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFB45309)
                    )
                }

                Button(
                    onClick = { showAddDialog = true },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isUrdu) "نیا سپلائر" else "Add Supplier")
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(suppliers, key = { it.id }) { supplier ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedSupplierForLedger = supplier },
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(supplier.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text(
                                text = "${supplier.company} • ${supplier.phone}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = Formatters.formatPkr(supplier.currentBalance),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp,
                                color = if (supplier.currentBalance > 0) Color(0xFFDC2626) else Color(0xFF16A34A)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Button(
                                onClick = { selectedSupplierForPayment = supplier },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Text(if (isUrdu) "ادائیگی" else "Pay", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddSupplierDialog(
            isUrdu = isUrdu,
            onDismiss = { showAddDialog = false },
            onSave = {
                viewModel.addSupplier(it)
                showAddDialog = false
            }
        )
    }

    if (selectedSupplierForPayment != null) {
        PaySupplierDialog(
            supplier = selectedSupplierForPayment!!,
            isUrdu = isUrdu,
            onDismiss = { selectedSupplierForPayment = null },
            onPay = { amt, method, ref, notes ->
                viewModel.paySupplier(selectedSupplierForPayment!!, amt, method, ref, notes)
                selectedSupplierForPayment = null
            }
        )
    }

    if (selectedSupplierForLedger != null) {
        SupplierLedgerDialog(
            supplier = selectedSupplierForLedger!!,
            viewModel = viewModel,
            isUrdu = isUrdu,
            onDismiss = { selectedSupplierForLedger = null }
        )
    }
}

@Composable
private fun AddSupplierDialog(
    isUrdu: Boolean,
    onDismiss: () -> Unit,
    onSave: (SupplierEntity) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var company by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var openingBalance by remember { mutableStateOf("0") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isUrdu) "نیا سپلائر شامل کریں" else "Add New Supplier") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Supplier Contact Name *") }, singleLine = true)
                OutlinedTextField(value = company, onValueChange = { company = it }, label = { Text("Company / Distributor Name") }, singleLine = true)
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone") }, singleLine = true)
                OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Address / City") }, singleLine = true)
                OutlinedTextField(
                    value = openingBalance,
                    onValueChange = { openingBalance = it },
                    label = { Text("Opening Payable Balance (Rs.)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                if (name.isNotBlank()) {
                    onSave(
                        SupplierEntity(
                            name = name.trim(),
                            company = company.trim(),
                            phone = phone.trim(),
                            address = address.trim(),
                            openingBalance = openingBalance.toDoubleOrNull() ?: 0.0,
                            currentBalance = openingBalance.toDoubleOrNull() ?: 0.0
                        )
                    )
                }
            }) {
                Text(if (isUrdu) "محفوظ کریں" else "Save")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun PaySupplierDialog(
    supplier: SupplierEntity,
    isUrdu: Boolean,
    onDismiss: () -> Unit,
    onPay: (amount: Double, method: PaymentMethod, ref: String, notes: String) -> Unit
) {
    var amount by remember { mutableStateOf(if (supplier.currentBalance > 0) supplier.currentBalance.toInt().toString() else "") }
    var method by remember { mutableStateOf(PaymentMethod.BANK) }
    var ref by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pay ${supplier.name}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Current Due: ${Formatters.formatPkr(supplier.currentBalance)}", fontWeight = FontWeight.Bold, color = Color(0xFFDC2626))
                OutlinedTextField(
                    value = amount,
                    onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) amount = it },
                    label = { Text("Payment Amount (Rs.) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                Text("Paid from Account:")
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf(PaymentMethod.CASH, PaymentMethod.BANK, PaymentMethod.EASYPAISA).forEach { m ->
                        FilterChip(selected = method == m, onClick = { method = m }, label = { Text(m.title, fontSize = 11.sp) })
                    }
                }
                OutlinedTextField(value = ref, onValueChange = { ref = it }, label = { Text("Cheque / Trx ID") }, singleLine = true)
                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notes") })
            }
        },
        confirmButton = {
            Button(onClick = {
                val amt = amount.toDoubleOrNull() ?: 0.0
                if (amt > 0) onPay(amt, method, ref, notes)
            }) {
                Text(if (isUrdu) "ادائیگی ریکارڈ کریں" else "Confirm Payment")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun SupplierLedgerDialog(
    supplier: SupplierEntity,
    viewModel: DukanViewModel,
    isUrdu: Boolean,
    onDismiss: () -> Unit
) {
    val ledgerEntries by viewModel.repository.getSupplierLedger(supplier.id).collectAsState(initial = emptyList())

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().fillMaxHeight(0.85f).padding(8.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text(supplier.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(supplier.company, fontSize = 12.sp, color = Color.Gray)
                    }
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = null) }
                }

                Text(
                    text = "Current Payable: ${Formatters.formatPkr(supplier.currentBalance)}",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFDC2626),
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                Divider()

                LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(ledgerEntries) { entry ->
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Column(modifier = Modifier.weight(1.5f)) {
                                Text(entry.referenceNumber, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text(Formatters.formatDate(entry.date), fontSize = 10.sp, color = Color.Gray)
                            }
                            Text(
                                text = if (entry.debit > 0) Formatters.formatPkr(entry.debit) else "-",
                                fontSize = 11.sp,
                                color = Color(0xFF16A34A),
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = if (entry.credit > 0) Formatters.formatPkr(entry.credit) else "-",
                                fontSize = 11.sp,
                                color = Color(0xFFDC2626),
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = Formatters.formatPkr(entry.runningBalance),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                    Text("Close")
                }
            }
        }
    }
}
