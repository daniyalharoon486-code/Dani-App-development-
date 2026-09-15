package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PaymentMethod
import com.example.data.model.PaymentStatus
import com.example.data.model.SaleEntity
import com.example.data.model.SaleItemEntity
import com.example.ui.common.AppStrings
import com.example.ui.common.Formatters
import com.example.ui.components.ReceiptDialog
import com.example.ui.viewmodel.DukanViewModel

@Composable
fun InvoicesScreen(
    viewModel: DukanViewModel
) {
    val isUrdu by viewModel.isUrdu.collectAsState()
    val sales by viewModel.sales.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val activeReceiptSale by viewModel.activeReceiptSale.collectAsState()
    val activeReceiptItems by viewModel.activeReceiptItems.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedStatus by remember { mutableStateOf<PaymentStatus?>(null) }
    var returnSaleTarget by remember { mutableStateOf<SaleEntity?>(null) }

    val filteredSales = remember(sales, searchQuery, selectedStatus) {
        sales.filter { sale ->
            val matchesSearch = searchQuery.isBlank() ||
                    sale.invoiceNumber.contains(searchQuery, ignoreCase = true) ||
                    sale.customerName.contains(searchQuery, ignoreCase = true) ||
                    sale.customerPhone.contains(searchQuery, ignoreCase = true)
            val matchesStatus = selectedStatus == null || sale.paymentStatus == selectedStatus
            matchesSearch && matchesStatus
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("invoices_screen")
    ) {
        // Search & Filters
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text(if (isUrdu) "انوائس نمبر یا گاہک کا نام تلاش کریں..." else "Search by invoice # or customer...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Status filter chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FilterChip(
                selected = (selectedStatus == null),
                onClick = { selectedStatus = null },
                label = { Text("All (${sales.size})", fontSize = 11.sp) }
            )
            FilterChip(
                selected = (selectedStatus == PaymentStatus.PAID),
                onClick = { selectedStatus = PaymentStatus.PAID },
                label = { Text("Paid", fontSize = 11.sp) }
            )
            FilterChip(
                selected = (selectedStatus == PaymentStatus.PARTIAL),
                onClick = { selectedStatus = PaymentStatus.PARTIAL },
                label = { Text("Partial", fontSize = 11.sp) }
            )
            FilterChip(
                selected = (selectedStatus == PaymentStatus.UNPAID),
                onClick = { selectedStatus = PaymentStatus.UNPAID },
                label = { Text("Unpaid / Credit", fontSize = 11.sp) }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (filteredSales.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(if (isUrdu) "کوئی بل نہیں ملا" else "No invoices found.", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredSales, key = { it.id }) { sale ->
                    InvoiceListItemCard(
                        sale = sale,
                        isUrdu = isUrdu,
                        onViewReceipt = { viewModel.viewReceipt(sale) },
                        onDuplicate = { viewModel.duplicateInvoiceToCart(sale) },
                        onReturn = { returnSaleTarget = sale }
                    )
                }
            }
        }
    }

    // Receipt Dialog
    if (activeReceiptSale != null) {
        ReceiptDialog(
            sale = activeReceiptSale!!,
            items = activeReceiptItems,
            settings = settings,
            isUrdu = isUrdu,
            onDismiss = { viewModel.closeReceipt() }
        )
    }

    // Return / Refund Dialog
    if (returnSaleTarget != null) {
        SaleReturnDialog(
            sale = returnSaleTarget!!,
            viewModel = viewModel,
            isUrdu = isUrdu,
            onDismiss = { returnSaleTarget = null }
        )
    }
}

@Composable
private fun InvoiceListItemCard(
    sale: SaleEntity,
    isUrdu: Boolean,
    onViewReceipt: () -> Unit,
    onDuplicate: () -> Unit,
    onReturn: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onViewReceipt),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(sale.invoiceNumber, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    if (sale.isReturned) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Badge(containerColor = Color(0xFFFEE2E2)) {
                            Text("Returned", color = Color(0xFFDC2626), fontSize = 10.sp)
                        }
                    }
                }

                // Payment Status Badge
                val (badgeBg, badgeFg) = when (sale.paymentStatus) {
                    PaymentStatus.PAID -> Color(0xFFDCFCE7) to Color(0xFF15803D)
                    PaymentStatus.PARTIAL -> Color(0xFFFEF3C7) to Color(0xFFB45309)
                    PaymentStatus.UNPAID -> Color(0xFFFEE2E2) to Color(0xFFB91C1C)
                }
                Box(
                    modifier = Modifier
                        .background(badgeBg, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(sale.paymentStatus.name, color = badgeFg, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = sale.customerName,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = Formatters.formatDateTime(sale.timestamp),
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = "Method: ${sale.paymentMethodSummary}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = Formatters.formatPkr(sale.grandTotal),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (sale.remainingAmount > 0) {
                        Text(
                            text = "Due: ${Formatters.formatPkr(sale.remainingAmount)}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFDC2626)
                        )
                    } else {
                        Text(
                            text = "Paid in full",
                            fontSize = 11.sp,
                            color = Color(0xFF16A34A)
                        )
                    }
                }
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onViewReceipt) {
                    Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isUrdu) "رسید" else "View Receipt", fontSize = 12.sp)
                }
                TextButton(onClick = onDuplicate) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isUrdu) "دوبارہ کارٹ" else "Duplicate", fontSize = 12.sp)
                }
                if (!sale.isReturned) {
                    TextButton(onClick = onReturn) {
                        Icon(Icons.Default.AssignmentReturn, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isUrdu) "واپسی" else "Return", fontSize = 12.sp, color = Color(0xFFDC2626))
                    }
                }
            }
        }
    }
}

@Composable
private fun SaleReturnDialog(
    sale: SaleEntity,
    viewModel: DukanViewModel,
    isUrdu: Boolean,
    onDismiss: () -> Unit
) {
    var items by remember { mutableStateOf<List<SaleItemEntity>>(emptyList()) }
    var reason by remember { mutableStateOf("Customer returned item") }
    var selectedRefundMethod by remember { mutableStateOf(PaymentMethod.CASH) }
    var notes by remember { mutableStateOf("") }

    LaunchedEffect(sale.id) {
        items = viewModel.repository.getSaleItemsSync(sale.id)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Process Return for ${sale.invoiceNumber}") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Return all items and refund to customer:")
                Spacer(modifier = Modifier.height(8.dp))
                items.forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("${item.productName} × ${item.quantity.toInt()}")
                        Text(Formatters.formatPkr(item.total), fontWeight = FontWeight.Bold)
                    }
                }
                Divider(modifier = Modifier.padding(vertical = 6.dp))
                Text("Total Refund: ${Formatters.formatPkr(sale.grandTotal)}", fontWeight = FontWeight.ExtraBold)

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Reason for Return") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val returnPairs = items.map { it to it.quantity }
                    viewModel.processSaleReturn(
                        sale = sale,
                        returnedItems = returnPairs,
                        refundMethod = selectedRefundMethod,
                        reason = reason,
                        notes = notes
                    )
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
            ) {
                Text("Confirm Return & Restock")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
