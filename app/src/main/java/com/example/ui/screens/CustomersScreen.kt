package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.CustomerEntity
import com.example.data.model.CustomerLedgerEntryEntity
import com.example.data.model.PaymentMethod
import com.example.ui.common.AppStrings
import com.example.ui.common.Formatters
import com.example.ui.components.AddCustomerDialog
import com.example.ui.viewmodel.DukanViewModel

@Composable
fun CustomersScreen(
    viewModel: DukanViewModel,
    onOpenReceivePayment: () -> Unit
) {
    val isUrdu by viewModel.isUrdu.collectAsState()
    val customers by viewModel.customers.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var showAddCustomerDialog by remember { mutableStateOf(false) }
    var selectedCustomerForLedger by remember { mutableStateOf<CustomerEntity?>(null) }

    val filteredCustomers = remember(customers, searchQuery) {
        customers.filter { c ->
            searchQuery.isBlank() ||
                    c.name.contains(searchQuery, ignoreCase = true) ||
                    c.phone.contains(searchQuery, ignoreCase = true) ||
                    c.address.contains(searchQuery, ignoreCase = true)
        }
    }

    val totalOutstanding = customers.sumOf { it.currentBalance }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("customers_screen")
    ) {
        // Top Summary Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
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
                        text = if (isUrdu) "گاہکوں کا کل ادھار کھاتہ" else "Total Customer Khata Due",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = Formatters.formatPkr(totalOutstanding),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Button(
                    onClick = { showAddCustomerDialog = true },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isUrdu) "نیا گاہک" else "Add Customer")
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text(if (isUrdu) "گاہک کا نام یا فون تلاش کریں..." else "Search by customer name or phone...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // List
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredCustomers, key = { it.id }) { customer ->
                CustomerCard(
                    customer = customer,
                    isUrdu = isUrdu,
                    onViewLedger = { selectedCustomerForLedger = customer },
                    onReceivePayment = {
                        viewModel.selectedCustomer.value = customer
                        onOpenReceivePayment()
                    }
                )
            }
        }
    }

    if (showAddCustomerDialog) {
        AddCustomerDialog(
            isUrdu = isUrdu,
            onDismiss = { showAddCustomerDialog = false },
            onSave = {
                viewModel.addCustomer(it)
                showAddCustomerDialog = false
            }
        )
    }

    if (selectedCustomerForLedger != null) {
        CustomerLedgerDialog(
            customer = selectedCustomerForLedger!!,
            viewModel = viewModel,
            isUrdu = isUrdu,
            onDismiss = { selectedCustomerForLedger = null },
            onReceivePayment = {
                val c = selectedCustomerForLedger
                selectedCustomerForLedger = null
                viewModel.selectedCustomer.value = c
                onOpenReceivePayment()
            }
        )
    }
}

@Composable
private fun CustomerCard(
    customer: CustomerEntity,
    isUrdu: Boolean,
    onViewLedger: () -> Unit,
    onReceivePayment: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onViewLedger),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(customer.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                if (customer.phone.isNotBlank()) {
                    Text(customer.phone, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (customer.address.isNotBlank()) {
                    Text(customer.address, fontSize = 11.sp, color = Color.Gray, maxLines = 1)
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = Formatters.formatPkr(customer.currentBalance),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    color = if (customer.currentBalance > 0) Color(0xFFDC2626) else Color(0xFF16A34A)
                )
                Text(
                    text = if (customer.currentBalance > 0) "Udhaar Due" else "Clear",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (customer.currentBalance > 0) Color(0xFFDC2626) else Color(0xFF16A34A)
                )

                if (customer.currentBalance > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Button(
                        onClick = onReceivePayment,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Text(if (isUrdu) "وصولی" else "Receive", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun CustomerLedgerDialog(
    customer: CustomerEntity,
    viewModel: DukanViewModel,
    isUrdu: Boolean,
    onDismiss: () -> Unit,
    onReceivePayment: () -> Unit
) {
    val ledgerEntries by viewModel.repository.getCustomerLedger(customer.id)
        .collectAsState(initial = emptyList())
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .padding(8.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(customer.name, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        Text("Phone: ${customer.phone.ifEmpty { "N/A" }}", fontSize = 12.sp, color = Color.DarkGray)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                // Balance Box
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (customer.currentBalance > 0) Color(0xFFFEE2E2) else Color(0xFFDCFCE7)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isUrdu) "موجودہ کھاتہ بقایا:" else "Current Outstanding:",
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = Formatters.formatPkr(customer.currentBalance),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 17.sp,
                            color = if (customer.currentBalance > 0) Color(0xFFDC2626) else Color(0xFF16A34A)
                        )
                    }
                }

                // WhatsApp reminder button
                if (customer.currentBalance > 0 && customer.phone.isNotBlank()) {
                    OutlinedButton(
                        onClick = {
                            val msg = "محترم ${customer.name} صاحب! آپ کا دکان پر بقایا کھاتہ ${Formatters.formatPkr(customer.currentBalance)} واجب الادا ہے۔ براہ کرم جلد ادائیگی فرما کر شکریہ کا موقع دیں۔ جزاک اللہ!"
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, msg)
                            }
                            context.startActivity(Intent.createChooser(intent, "Send WhatsApp Reminder"))
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, tint = Color(0xFF16A34A))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isUrdu) "واٹس ایپ پر ادھار کا تقاضا بھیجیں" else "Send WhatsApp Reminder")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Text(
                    text = if (isUrdu) "کھاتہ رجسٹر کی تفصیلات (Ledger History)" else "Khata Ledger Transactions",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )

                Divider(modifier = Modifier.padding(vertical = 4.dp))

                // Table Header
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text("Date / Ref", fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.4f))
                    Text("Debit (بنام)", fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    Text("Credit (جمع)", fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    Text("Bal (بقایا)", fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                }

                Divider(modifier = Modifier.padding(vertical = 4.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(ledgerEntries) { entry ->
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.weight(1.4f)) {
                                Text(entry.referenceNumber, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text(Formatters.formatDate(entry.date), fontSize = 10.sp, color = Color.Gray)
                            }
                            Text(
                                text = if (entry.debit > 0) Formatters.formatPkr(entry.debit) else "-",
                                fontSize = 11.sp,
                                color = Color(0xFFDC2626),
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = if (entry.credit > 0) Formatters.formatPkr(entry.credit) else "-",
                                fontSize = 11.sp,
                                color = Color(0xFF16A34A),
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

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("Close")
                    }
                    Button(onClick = onReceivePayment, modifier = Modifier.weight(1f)) {
                        Text(if (isUrdu) "وصولی درج کریں" else "Receive Payment")
                    }
                }
            }
        }
    }
}
