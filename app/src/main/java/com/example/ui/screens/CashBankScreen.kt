package com.example.ui.screens

import androidx.compose.foundation.background
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
import com.example.data.model.CashAccountEntity
import com.example.ui.common.AppStrings
import com.example.ui.common.Formatters
import com.example.ui.viewmodel.DukanViewModel

@Composable
fun CashBankScreen(
    viewModel: DukanViewModel
) {
    val isUrdu by viewModel.isUrdu.collectAsState()
    val accounts by viewModel.cashAccounts.collectAsState()
    val transactions by viewModel.cashTransactions.collectAsState()

    var showTransferDialog by remember { mutableStateOf(false) }

    val totalBalance = accounts.sumOf { it.currentBalance }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("cash_bank_screen")
    ) {
        // Overall Cash & Bank Balance Card
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
                        text = if (isUrdu) "کل کیش و بینک بیلنس" else "Total Cash & Bank Balance",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = Formatters.formatPkr(totalBalance),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Button(
                    onClick = { showTransferDialog = true },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.SwapHoriz, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isUrdu) "رقم منتقل کریں" else "Transfer")
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = if (isUrdu) "تمام اکاؤنٹس کی تفصیل" else "Accounts Overview",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Accounts Grid / Row
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            accounts.forEach { acc ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val icon = when (acc.accountType.name) {
                                "CASH" -> Icons.Default.Payments
                                "BANK" -> Icons.Default.AccountBalance
                                "CARD" -> Icons.Default.CreditCard
                                "EASYPAISA" -> Icons.Default.PhoneAndroid
                                else -> Icons.Default.PhoneIphone
                            }
                            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(acc.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(
                                    text = "Account: ${acc.accountNumber.ifEmpty { "Cash Till" }}",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }
                        }

                        Text(
                            text = Formatters.formatPkr(acc.currentBalance),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = if (acc.currentBalance >= 0) MaterialTheme.colorScheme.primary else Color(0xFFDC2626)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (isUrdu) "حالیہ ٹرانزیکشنز و کیش بک" else "Recent Cash Flow Transactions",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Recent transactions list
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(transactions.take(25)) { tx ->
                val isIncome = tx.type == "IN" || tx.type == "TRANSFER_IN"
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(
                                        if (isIncome) Color(0xFFDCFCE7) else Color(0xFFFEE2E2),
                                        RoundedCornerShape(6.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    if (isIncome) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                                    contentDescription = null,
                                    tint = if (isIncome) Color(0xFF15803D) else Color(0xFFB91C1C),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(tx.notes.ifEmpty { "${tx.referenceType} #${tx.referenceId}" }, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    text = "${tx.sourceOrDestination} • ${Formatters.formatDateTime(tx.timestamp)}",
                                    fontSize = 10.sp,
                                    color = Color.Gray
                                )
                            }
                        }

                        Text(
                            text = (if (isIncome) "+ " else "- ") + Formatters.formatPkr(tx.amount),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (isIncome) Color(0xFF15803D) else Color(0xFFB91C1C)
                        )
                    }
                }
            }
        }
    }

    if (showTransferDialog) {
        TransferFundsDialog(
            accounts = accounts,
            isUrdu = isUrdu,
            onDismiss = { showTransferDialog = false },
            onTransfer = { fromId, toId, amt, notes ->
                viewModel.transferCash(fromId, toId, amt, notes)
                showTransferDialog = false
            }
        )
    }
}

@Composable
private fun TransferFundsDialog(
    accounts: List<CashAccountEntity>,
    isUrdu: Boolean,
    onDismiss: () -> Unit,
    onTransfer: (fromId: Long, toId: Long, amount: Double, notes: String) -> Unit
) {
    var fromAccount by remember { mutableStateOf(accounts.firstOrNull()) }
    var toAccount by remember { mutableStateOf(accounts.getOrNull(1)) }
    var amountInput by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isUrdu) "اکاؤنٹس کے درمیان رقم ٹرانسفر" else "Transfer Funds Between Accounts") },
        text = {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("From Account:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                ScrollableTabRow(
                    selectedTabIndex = accounts.indexOf(fromAccount).coerceAtLeast(0),
                    edgePadding = 0.dp
                ) {
                    accounts.forEach { acc ->
                        Tab(
                            selected = (fromAccount?.id == acc.id),
                            onClick = { fromAccount = acc },
                            text = { Text(acc.name, fontSize = 11.sp) }
                        )
                    }
                }

                Text("To Account:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                ScrollableTabRow(
                    selectedTabIndex = accounts.indexOf(toAccount).coerceAtLeast(0),
                    edgePadding = 0.dp
                ) {
                    accounts.forEach { acc ->
                        Tab(
                            selected = (toAccount?.id == acc.id),
                            onClick = { toAccount = acc },
                            text = { Text(acc.name, fontSize = 11.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = amountInput,
                    onValueChange = { input -> if (input.all { it.isDigit() || it == '.' }) amountInput = input },
                    label = { Text("Transfer Amount (Rs.) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Transfer Notes / Remarks") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val amt = amountInput.toDoubleOrNull() ?: 0.0
                if (fromAccount != null && toAccount != null && amt > 0 && fromAccount?.id != toAccount?.id) {
                    onTransfer(fromAccount!!.id, toAccount!!.id, amt, notes.ifEmpty { "Inter-account transfer" })
                }
            }) {
                Text(if (isUrdu) "ٹرانسفر مکمل کریں" else "Execute Transfer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
