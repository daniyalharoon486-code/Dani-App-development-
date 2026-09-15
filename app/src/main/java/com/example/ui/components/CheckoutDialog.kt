package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.CustomerEntity
import com.example.data.model.PaymentMethod
import com.example.data.repository.PaymentSplitItem
import com.example.ui.common.Formatters

@Composable
fun CheckoutDialog(
    grandTotal: Double,
    customer: CustomerEntity?,
    isUrdu: Boolean,
    customersList: List<CustomerEntity>,
    onSelectCustomer: (CustomerEntity?) -> Unit,
    onDismiss: () -> Unit,
    onCompleteSale: (List<PaymentSplitItem>) -> Unit
) {
    // Payment split inputs
    var cashInput by remember { mutableStateOf(grandTotal.toInt().toString()) }
    var bankInput by remember { mutableStateOf("0") }
    var cardInput by remember { mutableStateOf("0") }
    var easypaisaInput by remember { mutableStateOf("0") }
    var jazzcashInput by remember { mutableStateOf("0") }
    var creditInput by remember { mutableStateOf("0") }

    val cashVal = cashInput.toDoubleOrNull() ?: 0.0
    val bankVal = bankInput.toDoubleOrNull() ?: 0.0
    val cardVal = cardInput.toDoubleOrNull() ?: 0.0
    val epVal = easypaisaInput.toDoubleOrNull() ?: 0.0
    val jcVal = jazzcashInput.toDoubleOrNull() ?: 0.0
    val creditVal = creditInput.toDoubleOrNull() ?: 0.0

    val paidDirect = cashVal + bankVal + cardVal + epVal + jcVal
    val totalSettled = paidDirect + creditVal
    val remaining = (grandTotal - paidDirect - creditVal)

    var customerDropdownOpen by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = 560.dp)
                .fillMaxHeight(0.95f)
                .padding(16.dp)
                .testTag("checkout_dialog"),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isUrdu) "ادائیگی و کھاتہ سیٹلمنٹ" else "Payment & Checkout",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isUrdu) "کل رقم وصول کریں یا تقسیم کریں" else "Collect payment or split across accounts",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 12.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Summary Banner
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (isUrdu) "بل کی کل رقم" else "Invoice Total",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = Formatters.formatPkr(grandTotal),
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Paid: ${Formatters.formatPkr(paidDirect)}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF16A34A)
                                )
                                if (creditVal > 0) {
                                    Text(
                                        text = "Credit: ${Formatters.formatPkr(creditVal)}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFD97706)
                                    )
                                }
                                Text(
                                    text = "Remaining: ${Formatters.formatPkr(if (remaining > 0) remaining else 0.0)}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (remaining > 0.05) Color(0xFFDC2626) else Color.DarkGray
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Customer Selection (Required if Credit or Remaining > 0)
                    Text(
                        text = if (isUrdu) "گاہک کی تفصیل (ادھار کے لیے لازمی)" else "Customer (Required for Udhaar / Credit)",
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { customerDropdownOpen = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = customer?.let { "${it.name} (Bal: ${Formatters.formatPkr(it.currentBalance)})" }
                                    ?: (if (isUrdu) "نقد گاہک (گاہک منتخب کریں)" else "Walk-in Cash Customer (Tap to select)"),
                                modifier = Modifier.weight(1f)
                            )
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }

                        DropdownMenu(
                            expanded = customerDropdownOpen,
                            onDismissRequest = { customerDropdownOpen = false },
                            modifier = Modifier.fillMaxWidth(0.9f)
                        ) {
                            DropdownMenuItem(
                                text = { Text(if (isUrdu) "واک اِن نقد گاہک" else "Walk-in Cash Customer (No Khata)") },
                                onClick = {
                                    onSelectCustomer(null)
                                    customerDropdownOpen = false
                                }
                            )
                            Divider()
                            customersList.forEach { cust ->
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(cust.name, fontWeight = FontWeight.Bold)
                                            Text("Due: ${Formatters.formatPkr(cust.currentBalance)}", color = Color(0xFFD97706))
                                        }
                                    },
                                    onClick = {
                                        onSelectCustomer(cust)
                                        customerDropdownOpen = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Quick 1-tap Payment Presets
                    Text(
                        text = if (isUrdu) "فوری ادائیگی" else "Quick 1-Tap Presets",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        PresetChip("Cash", isUrdu, onClick = {
                            cashInput = grandTotal.toInt().toString()
                            bankInput = "0"; cardInput = "0"; easypaisaInput = "0"; jazzcashInput = "0"; creditInput = "0"
                        })
                        PresetChip("Easypaisa", isUrdu, onClick = {
                            easypaisaInput = grandTotal.toInt().toString()
                            cashInput = "0"; bankInput = "0"; cardInput = "0"; jazzcashInput = "0"; creditInput = "0"
                        })
                        PresetChip("JazzCash", isUrdu, onClick = {
                            jazzcashInput = grandTotal.toInt().toString()
                            cashInput = "0"; bankInput = "0"; cardInput = "0"; easypaisaInput = "0"; creditInput = "0"
                        })
                        PresetChip("Bank", isUrdu, onClick = {
                            bankInput = grandTotal.toInt().toString()
                            cashInput = "0"; cardInput = "0"; easypaisaInput = "0"; jazzcashInput = "0"; creditInput = "0"
                        })
                        PresetChip("Credit", isUrdu, onClick = {
                            creditInput = grandTotal.toInt().toString()
                            cashInput = "0"; bankInput = "0"; cardInput = "0"; easypaisaInput = "0"; jazzcashInput = "0"
                        })
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Split payment channel inputs
                    Text(
                        text = if (isUrdu) "تقسیم ادائیگی (Split Payment Details)" else "Split Payment Inputs",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    PaymentChannelRow(
                        title = if (isUrdu) "نقد کیش (Cash)" else "Cash",
                        icon = Icons.Default.Payments,
                        value = cashInput,
                        onValueChange = { cashInput = it }
                    )
                    PaymentChannelRow(
                        title = if (isUrdu) "ایزی پیسہ (Easypaisa)" else "Easypaisa",
                        icon = Icons.Default.PhoneAndroid,
                        value = easypaisaInput,
                        onValueChange = { easypaisaInput = it }
                    )
                    PaymentChannelRow(
                        title = if (isUrdu) "جاز کیش (JazzCash)" else "JazzCash",
                        icon = Icons.Default.PhoneIphone,
                        value = jazzcashInput,
                        onValueChange = { jazzcashInput = it }
                    )
                    PaymentChannelRow(
                        title = if (isUrdu) "بینک ٹرانسفر (Bank)" else "Bank Transfer",
                        icon = Icons.Default.AccountBalance,
                        value = bankInput,
                        onValueChange = { bankInput = it }
                    )
                    PaymentChannelRow(
                        title = if (isUrdu) "کارڈ پی او ایس (Card)" else "Card POS",
                        icon = Icons.Default.CreditCard,
                        value = cardInput,
                        onValueChange = { cardInput = it }
                    )
                    PaymentChannelRow(
                        title = if (isUrdu) "گاہک کھاتہ ادھار (Credit)" else "Customer Credit / Udhaar",
                        icon = Icons.Default.Book,
                        value = creditInput,
                        onValueChange = { creditInput = it },
                        highlight = true
                    )

                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errorMessage!!,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 12.dp))

                // Bottom Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (isUrdu) "منسوخ" else "Cancel")
                    }

                    Button(
                        onClick = {
                            val totalRecorded = paidDirect + creditVal
                            if (totalRecorded < grandTotal - 0.01) {
                                // Auto place remainder into credit or require customer
                                val diff = grandTotal - paidDirect
                                if (customer == null) {
                                    errorMessage = if (isUrdu) "باقی رقم گاہک کے کھاتے میں ڈالنے کے لیے پہلے گاہک منتخب کریں۔" else "Please select a Customer to record remaining balance in Khata."
                                    return@Button
                                } else {
                                    creditInput = diff.toInt().toString()
                                }
                            }
                            if ((creditVal > 0 || remaining > 0.05) && customer == null) {
                                errorMessage = if (isUrdu) "ادھار دینے کے لیے گاہک منتخب کرنا لازمی ہے۔" else "Customer is required when giving credit (Udhaar)."
                                return@Button
                            }

                            val splits = mutableListOf<PaymentSplitItem>()
                            if (cashVal > 0) splits.add(PaymentSplitItem(PaymentMethod.CASH, cashVal))
                            if (bankVal > 0) splits.add(PaymentSplitItem(PaymentMethod.BANK, bankVal))
                            if (cardVal > 0) splits.add(PaymentSplitItem(PaymentMethod.CARD, cardVal))
                            if (epVal > 0) splits.add(PaymentSplitItem(PaymentMethod.EASYPAISA, epVal))
                            if (jcVal > 0) splits.add(PaymentSplitItem(PaymentMethod.JAZZCASH, jcVal))
                            val finalCredit = if (creditVal > 0) creditVal else if (remaining > 0) remaining else 0.0
                            if (finalCredit > 0) splits.add(PaymentSplitItem(PaymentMethod.CUSTOMER_CREDIT, finalCredit))

                            if (splits.isEmpty()) {
                                splits.add(PaymentSplitItem(PaymentMethod.CASH, grandTotal))
                            }

                            onCompleteSale(splits)
                        },
                        modifier = Modifier
                            .weight(1.6f)
                            .height(52.dp)
                            .testTag("complete_sale_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isUrdu) "فروخت مکمل و پرنٹ" else "Complete & Print Receipt",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RowScope.PresetChip(label: String, isUrdu: Boolean, onClick: () -> Unit) {
    FilledTonalButton(
        onClick = onClick,
        modifier = Modifier.weight(1f),
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
    ) {
        Text(label, fontSize = 11.sp, maxLines = 1)
    }
}

@Composable
private fun PaymentChannelRow(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    onValueChange: (String) -> Unit,
    highlight: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = if (highlight) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (highlight) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.weight(1.5f)
        )
        OutlinedTextField(
            value = value,
            onValueChange = { input ->
                if (input.all { it.isDigit() || it == '.' }) {
                    onValueChange(input)
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            prefix = { Text("Rs. ", fontSize = 12.sp) },
            singleLine = true,
            modifier = Modifier
                .weight(1.4f)
                .height(52.dp),
            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold)
        )
    }
}
