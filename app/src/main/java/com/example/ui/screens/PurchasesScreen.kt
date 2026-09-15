package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import com.example.data.model.PaymentMethod
import com.example.data.model.ProductEntity
import com.example.data.model.PurchaseEntity
import com.example.data.model.SupplierEntity
import com.example.ui.common.AppStrings
import com.example.ui.common.Formatters
import com.example.ui.viewmodel.DukanViewModel

@Composable
fun PurchasesScreen(
    viewModel: DukanViewModel
) {
    val isUrdu by viewModel.isUrdu.collectAsState()
    val purchases by viewModel.purchases.collectAsState()
    val suppliers by viewModel.suppliers.collectAsState()
    val products by viewModel.products.collectAsState()
    var showNewPurchaseDialog by remember { mutableStateOf(false) }

    val totalPurchased = purchases.sumOf { it.grandTotal }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("purchases_screen")
    ) {
        // Summary Header Card
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
                        text = if (isUrdu) "کل خریداری مال (سٹاک پرچیز)" else "Total Stock Purchases",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = Formatters.formatPkr(totalPurchased),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Button(
                    onClick = { showNewPurchaseDialog = true },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.AddShoppingCart, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isUrdu) "مال خریدیں" else "New Purchase")
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(purchases, key = { it.id }) { purchase ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(purchase.purchaseNumber, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(Formatters.formatPkr(purchase.grandTotal), fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Supplier: ${purchase.supplierName}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(Formatters.formatDateTime(purchase.timestamp), fontSize = 10.sp, color = Color.Gray)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Paid: ${Formatters.formatPkr(purchase.paidAmount)}", fontSize = 11.sp, color = Color(0xFF16A34A))
                                if (purchase.remainingAmount > 0) {
                                    Text("Payable Due: ${Formatters.formatPkr(purchase.remainingAmount)}", fontSize = 11.sp, color = Color(0xFFDC2626), fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showNewPurchaseDialog) {
        NewPurchaseDialog(
            suppliers = suppliers,
            products = products,
            isUrdu = isUrdu,
            onDismiss = { showNewPurchaseDialog = false },
            onSave = { supplier, items, paid, method, notes ->
                viewModel.createPurchase(supplier, items, paid, method, notes)
                showNewPurchaseDialog = false
            }
        )
    }
}

@Composable
private fun NewPurchaseDialog(
    suppliers: List<SupplierEntity>,
    products: List<ProductEntity>,
    isUrdu: Boolean,
    onDismiss: () -> Unit,
    onSave: (SupplierEntity, List<Triple<ProductEntity, Double, Double>>, Double, PaymentMethod, String) -> Unit
) {
    var selectedSupplier by remember { mutableStateOf(suppliers.firstOrNull()) }
    var selectedProduct by remember { mutableStateOf(products.firstOrNull()) }
    var qtyInput by remember { mutableStateOf("10") }
    var costInput by remember { mutableStateOf(selectedProduct?.purchasePrice?.toInt()?.toString() ?: "100") }
    var paidInput by remember { mutableStateOf("0") }
    var selectedMethod by remember { mutableStateOf(PaymentMethod.CASH) }
    var notes by remember { mutableStateOf("") }

    LaunchedEffect(selectedProduct) {
        if (selectedProduct != null) {
            costInput = selectedProduct!!.purchasePrice.toInt().toString()
        }
    }

    val qty = qtyInput.toDoubleOrNull() ?: 10.0
    val cost = costInput.toDoubleOrNull() ?: 100.0
    val lineTotal = qty * cost

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().fillMaxHeight(0.9f).padding(8.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(if (isUrdu) "سپلائر سے نیا مال خریدیں" else "Purchase Stock from Supplier", fontWeight = FontWeight.Bold, fontSize = 17.sp)

                Text("1. Select Supplier:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                ScrollableTabRow(
                    selectedTabIndex = suppliers.indexOf(selectedSupplier).coerceAtLeast(0),
                    edgePadding = 0.dp
                ) {
                    suppliers.forEach { s ->
                        Tab(
                            selected = selectedSupplier?.id == s.id,
                            onClick = { selectedSupplier = s },
                            text = { Text(s.name, fontSize = 11.sp) }
                        )
                    }
                }

                Text("2. Select Product to Restock:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                ScrollableTabRow(
                    selectedTabIndex = products.indexOf(selectedProduct).coerceAtLeast(0),
                    edgePadding = 0.dp
                ) {
                    products.take(8).forEach { p ->
                        Tab(
                            selected = selectedProduct?.id == p.id,
                            onClick = { selectedProduct = p },
                            text = { Text(p.name, fontSize = 11.sp) }
                        )
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = qtyInput,
                        onValueChange = { qtyInput = it },
                        label = { Text("Quantity (${selectedProduct?.unit ?: "Pcs"})") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = costInput,
                        onValueChange = { costInput = it },
                        label = { Text("Cost Price (Rs.)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Text("Purchase Total: ${Formatters.formatPkr(lineTotal)}", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                OutlinedTextField(
                    value = paidInput,
                    onValueChange = { paidInput = it },
                    label = { Text("Paid Now (Remaining will go to Supplier Khata)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Paid via Account:")
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf(PaymentMethod.CASH, PaymentMethod.BANK, PaymentMethod.EASYPAISA).forEach { m ->
                        FilterChip(selected = selectedMethod == m, onClick = { selectedMethod = m }, label = { Text(m.title, fontSize = 11.sp) })
                    }
                }

                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notes / Gate Pass #") }, modifier = Modifier.fillMaxWidth())

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = {
                        if (selectedSupplier != null && selectedProduct != null) {
                            val items = listOf(Triple(selectedProduct!!, qty, cost))
                            val paid = paidInput.toDoubleOrNull() ?: 0.0
                            onSave(selectedSupplier!!, items, paid, selectedMethod, notes)
                        }
                    }) {
                        Text(if (isUrdu) "خریداری مکمل کریں" else "Save Purchase & Update Stock")
                    }
                }
            }
        }
    }
}
