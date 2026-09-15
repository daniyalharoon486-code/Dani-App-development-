package com.example.ui.screens

import androidx.compose.foundation.background
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
import com.example.data.model.CustomerEntity
import com.example.data.model.PaymentMethod
import com.example.ui.common.AppStrings
import com.example.ui.common.Formatters
import com.example.ui.components.AddCustomerDialog
import com.example.ui.viewmodel.DukanViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceivePaymentScreen(
    viewModel: DukanViewModel
) {
    val isUrdu by viewModel.isUrdu.collectAsState()
    val customers by viewModel.customers.collectAsState()
    var selectedCustomer by remember { mutableStateOf(viewModel.selectedCustomer.value) }
    var amountInput by remember { mutableStateOf("") }
    var selectedMethod by remember { mutableStateOf(PaymentMethod.CASH) }
    var refNumber by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var customerDropdownOpen by remember { mutableStateOf(false) }
    var showAddCustomer by remember { mutableStateOf(false) }

    // Update if viewModel selectedCustomer changed
    LaunchedEffect(viewModel.selectedCustomer.value) {
        if (viewModel.selectedCustomer.value != null) {
            selectedCustomer = viewModel.selectedCustomer.value
            if (amountInput.isBlank() && (selectedCustomer?.currentBalance ?: 0.0) > 0) {
                amountInput = selectedCustomer!!.currentBalance.toInt().toString()
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("receive_payment_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = if (isUrdu) "گاہک سے ادھار کھاتہ کی رقم وصول کریں" else "Receive Customer Payment",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (isUrdu) "وصول شدہ رقم فوری طور پر گاہک کے کھاتے اور منتخب اکاؤنٹ میں جمع ہوگی۔" else "Recorded payment immediately credits customer ledger and updates cash/bank account.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Customer Selector Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isUrdu) "گاہک منتخب کریں:" else "Select Customer:",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { customerDropdownOpen = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = selectedCustomer?.let { "${it.name} (${it.phone})" }
                                    ?: (if (isUrdu) "گاہک منتخب کرنے کے لیے یہاں کلک کریں" else "Tap to choose a customer"),
                                modifier = Modifier.weight(1f)
                            )
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }

                        DropdownMenu(
                            expanded = customerDropdownOpen,
                            onDismissRequest = { customerDropdownOpen = false },
                            modifier = Modifier.fillMaxWidth(0.9f)
                        ) {
                            customers.forEach { c ->
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(c.name, fontWeight = FontWeight.Bold)
                                            Text(
                                                text = "Due: ${Formatters.formatPkr(c.currentBalance)}",
                                                color = if (c.currentBalance > 0) Color(0xFFDC2626) else Color(0xFF16A34A),
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    },
                                    onClick = {
                                        selectedCustomer = c
                                        customerDropdownOpen = false
                                        if (c.currentBalance > 0) {
                                            amountInput = c.currentBalance.toInt().toString()
                                        }
                                    }
                                )
                            }
                        }
                    }

                    if (selectedCustomer != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFFEF2F2), RoundedCornerShape(8.dp))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(if (isUrdu) "موجودہ واجب الادا بقایا:" else "Total Outstanding Balance:", fontWeight = FontWeight.SemiBold)
                            Text(
                                text = Formatters.formatPkr(selectedCustomer!!.currentBalance),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFDC2626)
                            )
                        }
                    }
                }
            }
        }

        // Amount & Method Entry Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = amountInput,
                        onValueChange = { input ->
                            if (input.all { it.isDigit() || it == '.' }) amountInput = input
                        },
                        label = { Text(if (isUrdu) "وصول شدہ رقم (Rs.) *" else "Payment Amount (Rs.) *") },
                        prefix = { Text("Rs. ", fontWeight = FontWeight.Bold) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    )

                    // Quick Chips
                    if (selectedCustomer != null && selectedCustomer!!.currentBalance > 0) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            FilledTonalButton(
                                onClick = { amountInput = selectedCustomer!!.currentBalance.toInt().toString() },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("Full Amount")
                            }
                            listOf(500, 1000, 2000, 5000).forEach { quick ->
                                AssistChip(
                                    onClick = { amountInput = quick.toString() },
                                    label = { Text("Rs. $quick") }
                                )
                            }
                        }
                    }

                    Text(
                        text = if (isUrdu) "وصولی کا طریقہ / اکاؤنٹ:" else "Payment Method / Account:",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(PaymentMethod.CASH, PaymentMethod.EASYPAISA, PaymentMethod.JAZZCASH, PaymentMethod.BANK, PaymentMethod.CARD).forEach { m ->
                            FilterChip(
                                selected = (selectedMethod == m),
                                onClick = { selectedMethod = m },
                                label = { Text(m.title, fontSize = 11.sp) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = refNumber,
                        onValueChange = { refNumber = it },
                        label = { Text(if (isUrdu) "ٹرانزیکشن ریفرنس / رسید نمبر" else "Ref / Slip # (Optional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text(if (isUrdu) "تفصیل / ریمارکس" else "Notes / Remarks") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            val cust = selectedCustomer
                            val amt = amountInput.toDoubleOrNull() ?: 0.0
                            if (cust != null && amt > 0) {
                                viewModel.receiveCustomerPayment(
                                    customer = cust,
                                    amount = amt,
                                    method = selectedMethod,
                                    refNo = refNumber,
                                    notes = notes.ifEmpty { "Cash Recovery" }
                                )
                                amountInput = ""
                                refNumber = ""
                                notes = ""
                            } else {
                                viewModel.showToast("Please select a customer and enter a valid amount.")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("submit_receive_payment_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isUrdu) "وصولی محفوظ کریں" else "Confirm & Save Recovery",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    }

    if (showAddCustomer) {
        AddCustomerDialog(
            isUrdu = isUrdu,
            onDismiss = { showAddCustomer = false },
            onSave = {
                viewModel.addCustomer(it)
                showAddCustomer = false
            }
        )
    }
}
