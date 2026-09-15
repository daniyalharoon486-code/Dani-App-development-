package com.example.ui.components

import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.*
import com.example.ui.common.Formatters

@Composable
fun BarcodeScannerDialog(
    isUrdu: Boolean,
    onBarcodeScanned: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var barcodeInput by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isUrdu) "بارکوڈ اسکینر" else "Barcode Scanner Input")
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    if (isUrdu) "بارکوڈ نمبر درج کریں یا فوری اسکین بٹن دبائیں:"
                    else "Enter barcode or tap a quick scan simulation shortcut:"
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = barcodeInput,
                    onValueChange = { barcodeInput = it },
                    label = { Text("Barcode Number") },
                    modifier = Modifier.fillMaxWidth().testTag("barcode_input_field"),
                    singleLine = true,
                    trailingIcon = {
                        IconButton(onClick = {
                            if (barcodeInput.isNotBlank()) {
                                onBarcodeScanned(barcodeInput)
                                onDismiss()
                            }
                        }) {
                            Icon(Icons.Default.Check, contentDescription = "Submit")
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))
                Text("Quick Scan Shortcuts:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    AssistChip(
                        onClick = { onBarcodeScanned("89640001"); onDismiss() },
                        label = { Text("Coke 500ml") }
                    )
                    AssistChip(
                        onClick = { onBarcodeScanned("89640002"); onDismiss() },
                        label = { Text("Pepsi 500ml") }
                    )
                    AssistChip(
                        onClick = { onBarcodeScanned("89640005"); onDismiss() },
                        label = { Text("Olpers Milk") }
                    )
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    AssistChip(
                        onClick = { onBarcodeScanned("89640006"); onDismiss() },
                        label = { Text("Dawn Bread") }
                    )
                    AssistChip(
                        onClick = { onBarcodeScanned("89640009"); onDismiss() },
                        label = { Text("Dalda Oil") }
                    )
                    AssistChip(
                        onClick = { onBarcodeScanned("89640008"); onDismiss() },
                        label = { Text("Lux Soap") }
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                if (barcodeInput.isNotBlank()) {
                    onBarcodeScanned(barcodeInput)
                    onDismiss()
                }
            }) {
                Text(if (isUrdu) "شامل کریں" else "Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isUrdu) "منسوخ" else "Cancel")
            }
        }
    )
}

@Composable
fun AddCustomerDialog(
    isUrdu: Boolean,
    onDismiss: () -> Unit,
    onSave: (CustomerEntity) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var openingBalance by remember { mutableStateOf("0") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isUrdu) "نیا گاہک کھاتہ شامل کریں" else "Add New Customer") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(if (isUrdu) "گاہک کا نام *" else "Customer Name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text(if (isUrdu) "فون نمبر" else "Phone Number") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text(if (isUrdu) "پتہ" else "Address") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = openingBalance,
                    onValueChange = { openingBalance = it },
                    label = { Text(if (isUrdu) "ابتدائی بقایا (Opening Udhaar)" else "Opening Balance (Rs.)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(if (isUrdu) "تفصیل / ریمارکس" else "Notes / Remarks") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val ob = openingBalance.toDoubleOrNull() ?: 0.0
                        onSave(
                            CustomerEntity(
                                name = name.trim(),
                                phone = phone.trim(),
                                address = address.trim(),
                                email = email.trim(),
                                openingBalance = ob,
                                currentBalance = ob,
                                notes = notes.trim()
                            )
                        )
                    }
                }
            ) {
                Text(if (isUrdu) "محفوظ کریں" else "Save Customer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isUrdu) "منسوخ" else "Cancel")
            }
        }
    )
}

@Composable
fun AddProductDialog(
    isUrdu: Boolean,
    existing: ProductEntity? = null,
    onDismiss: () -> Unit,
    onSave: (ProductEntity) -> Unit
) {
    var name by remember { mutableStateOf(existing?.name ?: "") }
    var nameUrdu by remember { mutableStateOf(existing?.nameUrdu ?: "") }
    var sku by remember { mutableStateOf(existing?.sku ?: "") }
    var barcode by remember { mutableStateOf(existing?.barcode ?: "") }
    var category by remember { mutableStateOf(existing?.category ?: "Grocery") }
    var brand by remember { mutableStateOf(existing?.brand ?: "") }
    var purchasePrice by remember { mutableStateOf(existing?.purchasePrice?.toInt()?.toString() ?: "0") }
    var salePrice by remember { mutableStateOf(existing?.salePrice?.toInt()?.toString() ?: "0") }
    var wholesalePrice by remember { mutableStateOf(existing?.wholesalePrice?.toInt()?.toString() ?: "0") }
    var stock by remember { mutableStateOf(existing?.currentStock?.toInt()?.toString() ?: "10") }
    var minStock by remember { mutableStateOf(existing?.minStock?.toInt()?.toString() ?: "5") }
    var unit by remember { mutableStateOf(existing?.unit ?: "Pcs") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .padding(12.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    text = if (existing == null) (if (isUrdu) "نئی پروڈکٹ شامل کریں" else "Add New Product") else (if (isUrdu) "پروڈکٹ میں ترمیم" else "Edit Product"),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Divider(modifier = Modifier.padding(vertical = 8.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Product Name *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = nameUrdu,
                        onValueChange = { nameUrdu = it },
                        label = { Text("اردو نام (Urdu Name)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = barcode,
                            onValueChange = { barcode = it },
                            label = { Text("Barcode") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = sku,
                            onValueChange = { sku = it },
                            label = { Text("SKU") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = category,
                            onValueChange = { category = it },
                            label = { Text("Category") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = brand,
                            onValueChange = { brand = it },
                            label = { Text("Brand") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = purchasePrice,
                            onValueChange = { purchasePrice = it },
                            label = { Text("Cost Price (Rs.) *") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = salePrice,
                            onValueChange = { salePrice = it },
                            label = { Text("Sale Price (Rs.) *") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = stock,
                            onValueChange = { stock = it },
                            label = { Text("Current Stock *") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = minStock,
                            onValueChange = { minStock = it },
                            label = { Text("Min Stock Alert") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = unit,
                            onValueChange = { unit = it },
                            label = { Text("Unit (Pcs/Kg/Pack)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text(if (isUrdu) "منسوخ" else "Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = {
                        if (name.isNotBlank()) {
                            val cost = purchasePrice.toDoubleOrNull() ?: 0.0
                            val sale = salePrice.toDoubleOrNull() ?: 0.0
                            val st = stock.toDoubleOrNull() ?: 0.0
                            val mst = minStock.toDoubleOrNull() ?: 5.0
                            val product = (existing ?: ProductEntity(name = "")).copy(
                                name = name.trim(),
                                nameUrdu = nameUrdu.trim(),
                                sku = sku.trim(),
                                barcode = barcode.trim(),
                                category = category.trim(),
                                brand = brand.trim(),
                                purchasePrice = cost,
                                salePrice = sale,
                                wholesalePrice = wholesalePrice.toDoubleOrNull() ?: sale,
                                currentStock = st,
                                minStock = mst,
                                unit = unit.trim()
                            )
                            onSave(product)
                        }
                    }) {
                        Text(if (isUrdu) "محفوظ کریں" else "Save Product")
                    }
                }
            }
        }
    }
}

@Composable
fun AddExpenseDialog(
    isUrdu: Boolean,
    onDismiss: () -> Unit,
    onSave: (title: String, category: String, amount: Double, method: PaymentMethod, notes: String) -> Unit
) {
    val categories = listOf("Rent", "Electricity", "Salary", "Transport", "Shop Maintenance", "Internet", "Purchases", "Other")
    var title by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Shop Maintenance") }
    var amount by remember { mutableStateOf("") }
    var selectedMethod by remember { mutableStateOf(PaymentMethod.CASH) }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isUrdu) "خرچہ درج کریں" else "Record Shop Expense") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Expense Title *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Category:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                ScrollableTabRow(
                    selectedTabIndex = categories.indexOf(selectedCategory).coerceAtLeast(0),
                    edgePadding = 0.dp
                ) {
                    categories.forEach { cat ->
                        Tab(
                            selected = (selectedCategory == cat),
                            onClick = { selectedCategory = cat },
                            text = { Text(cat, fontSize = 11.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount (Rs.) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Deduct from Account:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(PaymentMethod.CASH, PaymentMethod.BANK, PaymentMethod.EASYPAISA, PaymentMethod.JAZZCASH).forEach { m ->
                        FilterChip(
                            selected = (selectedMethod == m),
                            onClick = { selectedMethod = m },
                            label = { Text(m.title, fontSize = 11.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes / Receipt details") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val amt = amount.toDoubleOrNull() ?: 0.0
                if (title.isNotBlank() && amt > 0) {
                    onSave(title.trim(), selectedCategory, amt, selectedMethod, notes.trim())
                }
            }) {
                Text(if (isUrdu) "خرچہ محفوظ کریں" else "Save Expense")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(if (isUrdu) "منسوخ" else "Cancel") }
        }
    )
}
