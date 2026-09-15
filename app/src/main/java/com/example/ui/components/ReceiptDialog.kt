package com.example.ui.components

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.ReceiptSize
import com.example.data.model.SaleEntity
import com.example.data.model.SaleItemEntity
import com.example.data.model.ShopSettingsEntity
import com.example.ui.common.Formatters

@Composable
fun ReceiptDialog(
    sale: SaleEntity,
    items: List<SaleItemEntity>,
    settings: ShopSettingsEntity?,
    isUrdu: Boolean,
    onDismiss: () -> Unit
) {
    var selectedSize by remember { mutableStateOf(settings?.receiptSize ?: ReceiptSize.THERMAL_80MM) }
    var printStatus by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    val receiptWidthDp = when (selectedSize) {
        ReceiptSize.THERMAL_58MM -> 320.dp
        ReceiptSize.THERMAL_80MM -> 400.dp
        ReceiptSize.A4 -> 540.dp
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = receiptWidthDp)
                .fillMaxHeight(0.92f)
                .padding(16.dp)
                .testTag("receipt_dialog"),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header with size selector & close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isUrdu) "رسید و انوائس" else "Receipt & Invoice",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                // Size selector chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ReceiptSize.values().forEach { size ->
                        FilterChip(
                            selected = (selectedSize == size),
                            onClick = { selectedSize = size },
                            label = { Text(size.title, fontSize = 11.sp) }
                        )
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                // Scrollable receipt canvas (Thermal Paper Simulation)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(Color(0xFFFCFDFD), RoundedCornerShape(8.dp))
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Shop Name
                        Text(
                            text = settings?.shopName ?: "Bismillah Mart & Khata",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            textAlign = TextAlign.Center,
                            color = Color.Black
                        )
                        if (isUrdu || !settings?.shopNameUrdu.isNullOrBlank()) {
                            Text(
                                text = settings?.shopNameUrdu ?: "بسم اللہ مارٹ",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                textAlign = TextAlign.Center,
                                color = Color(0xFF0F5A3E)
                            )
                        }

                        Text(
                            text = settings?.address ?: "Commercial Market, Lahore",
                            fontSize = 11.sp,
                            color = Color.DarkGray,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Phone: ${settings?.phone ?: "0300-1234567"}",
                            fontSize = 11.sp,
                            color = Color.DarkGray
                        )

                        Text(
                            text = "------------------------------------------------",
                            fontFamily = FontFamily.Monospace,
                            color = Color.Gray,
                            fontSize = 11.sp
                        )

                        // Invoice & Customer Info
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Invoice: ${sale.invoiceNumber}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Black)
                            Text(Formatters.formatDateTime(sale.timestamp), fontSize = 11.sp, color = Color.DarkGray)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Customer: ${sale.customerName}", fontSize = 12.sp, color = Color.Black)
                            if (sale.customerPhone.isNotBlank()) {
                                Text(sale.customerPhone, fontSize = 11.sp, color = Color.DarkGray)
                            }
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Cashier: ${sale.cashierName}", fontSize = 11.sp, color = Color.DarkGray)
                            Text("Status: ${sale.paymentStatus.name}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (sale.paymentStatus.name == "PAID") Color(0xFF16A34A) else Color(0xFFDC2626))
                        }

                        Text(
                            text = "------------------------------------------------",
                            fontFamily = FontFamily.Monospace,
                            color = Color.Gray,
                            fontSize = 11.sp
                        )

                        // Table header
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Item", fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(2f), color = Color.Black)
                            Text("Qty", fontWeight = FontWeight.Bold, fontSize = 11.sp, textAlign = TextAlign.Center, modifier = Modifier.weight(0.8f), color = Color.Black)
                            Text("Rate", fontWeight = FontWeight.Bold, fontSize = 11.sp, textAlign = TextAlign.End, modifier = Modifier.weight(1f), color = Color.Black)
                            Text("Total", fontWeight = FontWeight.Bold, fontSize = 11.sp, textAlign = TextAlign.End, modifier = Modifier.weight(1.2f), color = Color.Black)
                        }

                        Divider(modifier = Modifier.padding(vertical = 4.dp), color = Color.LightGray)

                        // Table items
                        items.forEach { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(item.productName, fontSize = 11.sp, modifier = Modifier.weight(2f), color = Color.Black)
                                Text("${item.quantity.toInt()}", fontSize = 11.sp, textAlign = TextAlign.Center, modifier = Modifier.weight(0.8f), color = Color.Black)
                                Text("${item.unitPrice.toInt()}", fontSize = 11.sp, textAlign = TextAlign.End, modifier = Modifier.weight(1f), color = Color.Black)
                                Text(Formatters.formatPkr(item.total), fontSize = 11.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.End, modifier = Modifier.weight(1.2f), color = Color.Black)
                            }
                        }

                        Text(
                            text = "------------------------------------------------",
                            fontFamily = FontFamily.Monospace,
                            color = Color.Gray,
                            fontSize = 11.sp
                        )

                        // Calculations
                        CalculationRow("Subtotal:", Formatters.formatPkr(sale.subtotal))
                        if (sale.discount > 0) {
                            CalculationRow("Discount:", "- " + Formatters.formatPkr(sale.discount), color = Color(0xFFDC2626))
                        }
                        if (sale.tax > 0) {
                            CalculationRow("Tax:", "+ " + Formatters.formatPkr(sale.tax))
                        }

                        Divider(modifier = Modifier.padding(vertical = 4.dp), color = Color.Black)
                        CalculationRow("Grand Total:", Formatters.formatPkr(sale.grandTotal), isBold = true, fontSize = 15.sp)
                        CalculationRow("Paid Amount:", Formatters.formatPkr(sale.paidAmount), color = Color(0xFF16A34A), isBold = true)
                        if (sale.remainingAmount > 0) {
                            CalculationRow("Balance Due (Khata):", Formatters.formatPkr(sale.remainingAmount), color = Color(0xFFDC2626), isBold = true)
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Payment: ${sale.paymentMethodSummary}", fontSize = 11.sp, color = Color.DarkGray)

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = settings?.receiptFooter ?: "Thank you for your visit! Please come again.",
                            fontSize = 11.sp,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            textAlign = TextAlign.Center,
                            color = Color.DarkGray
                        )
                        Text(
                            text = "*** Powered by Dukan POS (Offline) ***",
                            fontSize = 9.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                if (printStatus != null) {
                    Text(
                        text = printStatus!!,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Action buttons: Print, Share, WhatsApp
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            printStatus = "Sent to ${selectedSize.title} printer! (Printing...)"
                        },
                        modifier = Modifier.weight(1f).testTag("print_button")
                    ) {
                        Icon(Icons.Default.Print, contentDescription = "Print")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isUrdu) "پرنٹ" else "Print")
                    }

                    OutlinedButton(
                        onClick = {
                            val shareBody = buildString {
                                appendLine("--- ${settings?.shopName ?: "Dukan"} ---")
                                appendLine("Invoice: ${sale.invoiceNumber}")
                                appendLine("Date: ${Formatters.formatDateTime(sale.timestamp)}")
                                appendLine("Customer: ${sale.customerName}")
                                appendLine("------------------")
                                items.forEach {
                                    appendLine("${it.productName} x ${it.quantity.toInt()} = Rs. ${it.total.toInt()}")
                                }
                                appendLine("------------------")
                                appendLine("Grand Total: Rs. ${sale.grandTotal.toInt()}")
                                appendLine("Paid: Rs. ${sale.paidAmount.toInt()}")
                                if (sale.remainingAmount > 0) {
                                    appendLine("Remaining Balance: Rs. ${sale.remainingAmount.toInt()}")
                                }
                                appendLine("Thank you for your visit!")
                            }
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, shareBody)
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share Receipt via WhatsApp / SMS"))
                        },
                        modifier = Modifier.weight(1f).testTag("share_button")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isUrdu) "شیئر" else "Share")
                    }
                }
            }
        }
    }
}

@Composable
private fun CalculationRow(
    title: String,
    value: String,
    color: Color = Color.Black,
    isBold: Boolean = false,
    fontSize: androidx.compose.ui.unit.TextUnit = 12.sp
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(title, fontSize = fontSize, fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal, color = color)
        Text(value, fontSize = fontSize, fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal, color = color)
    }
}
