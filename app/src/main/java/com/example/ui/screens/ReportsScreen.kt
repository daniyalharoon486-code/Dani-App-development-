package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
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
import com.example.ui.common.AppStrings
import com.example.ui.common.Formatters
import com.example.ui.viewmodel.DukanViewModel

@Composable
fun ReportsScreen(
    viewModel: DukanViewModel
) {
    val isUrdu by viewModel.isUrdu.collectAsState()
    val allSales by viewModel.sales.collectAsState()
    val allExpenses by viewModel.expenses.collectAsState()
    val allProducts by viewModel.products.collectAsState()
    val allCustomers by viewModel.customers.collectAsState()
    val context = LocalContext.current

    // Financial Metrics
    val totalRevenue = allSales.sumOf { it.grandTotal }
    val totalCogs = allSales.sumOf { it.totalCost }
    val grossProfit = (totalRevenue - totalCogs).coerceAtLeast(0.0)
    val totalExpenses = allExpenses.sumOf { it.amount }
    val netProfit = grossProfit - totalExpenses
    val netMargin = if (totalRevenue > 0) (netProfit / totalRevenue) * 100 else 0.0

    val totalCustomerDue = allCustomers.sumOf { it.currentBalance }
    val totalStockCost = allProducts.sumOf { it.currentStock * it.purchasePrice }
    val totalStockRetail = allProducts.sumOf { it.currentStock * it.salePrice }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("reports_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isUrdu) "کاروباری رپورٹس و نفع نقصان" else "Financial & Business Reports",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isUrdu) "آمدن، اخراجات، خالص منافع اور کھاتہ کی جامع رپورٹ" else "Comprehensive Profit & Loss, Balance Sheet & Khata audit",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = {
                        val reportText = buildString {
                            appendLine("===== DUKAN POS FINANCIAL REPORT =====")
                            appendLine("Total Revenue: ${Formatters.formatPkr(totalRevenue)}")
                            appendLine("Cost of Goods Sold (COGS): ${Formatters.formatPkr(totalCogs)}")
                            appendLine("Gross Profit: ${Formatters.formatPkr(grossProfit)}")
                            appendLine("Operating Expenses: ${Formatters.formatPkr(totalExpenses)}")
                            appendLine("NET PROFIT: ${Formatters.formatPkr(netProfit)} (${netMargin.toInt()}%)")
                            appendLine("---------------------------------------")
                            appendLine("Total Customer Udhaar Due: ${Formatters.formatPkr(totalCustomerDue)}")
                            appendLine("Inventory Stock Value: ${Formatters.formatPkr(totalStockCost)}")
                            appendLine("=======================================")
                        }
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, reportText)
                        }
                        context.startActivity(Intent.createChooser(intent, "Share Business Report"))
                    }
                ) {
                    Icon(Icons.Default.Share, contentDescription = "Share Report", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }

        // Profit & Loss Statement Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = if (isUrdu) "نفع و نقصان کا گوشوارہ (Profit & Loss Statement)" else "Profit & Loss Summary",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Divider()

                    ReportLine(title = "Total Sales Revenue (فروخت آمدن):", value = Formatters.formatPkr(totalRevenue), isPositive = true)
                    ReportLine(title = "Cost of Goods Sold (COGS مال کی لاگت):", value = "- " + Formatters.formatPkr(totalCogs), isNegative = true)

                    Divider(color = Color.LightGray)
                    ReportLine(title = "Gross Profit (مجموعی منافع):", value = Formatters.formatPkr(grossProfit), isBold = true)

                    ReportLine(title = "Shop Operating Expenses (دکان کے اخراجات):", value = "- " + Formatters.formatPkr(totalExpenses), isNegative = true)

                    Divider(color = Color.Black, thickness = 1.5.dp)
                    ReportLine(
                        title = "Net Profit / پاک صاف منافع:",
                        value = Formatters.formatPkr(netProfit),
                        isBold = true,
                        fontSize = 17.sp,
                        color = if (netProfit >= 0) Color(0xFF15803D) else Color(0xFFDC2626)
                    )
                    Text(
                        text = "Net Profit Margin: ${String.format("%.1f", netMargin)}%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (netProfit >= 0) Color(0xFF15803D) else Color(0xFFDC2626)
                    )
                }
            }
        }

        // Top Customer Debtors (Khata Breakdown)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isUrdu) "بڑے ادھار کھاتہ دار (Top Customer Debtors)" else "Customer Udhaar Breakdown",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    val topDebtors = allCustomers.filter { it.currentBalance > 0 }.sortedByDescending { it.currentBalance }
                    if (topDebtors.isEmpty()) {
                        Text("No outstanding dues from customers!", color = Color(0xFF16A34A), fontSize = 12.sp)
                    } else {
                        topDebtors.take(5).forEach { c ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(c.name, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                Text(Formatters.formatPkr(c.currentBalance), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFFDC2626))
                            }
                        }
                    }
                }
            }
        }

        // Stock Valuation Summary
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (isUrdu) "اسٹاک کا مالیاتی خلاصہ (Inventory Assets)" else "Inventory Valuation Report",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Divider()
                    ReportLine(title = "Inventory Valuation at Cost Price:", value = Formatters.formatPkr(totalStockCost))
                    ReportLine(title = "Expected Realization at Retail Sale Price:", value = Formatters.formatPkr(totalStockRetail))
                    ReportLine(
                        title = "Unrealized Inventory Gross Profit:",
                        value = Formatters.formatPkr(totalStockRetail - totalStockCost),
                        isBold = true,
                        color = Color(0xFF15803D)
                    )
                }
            }
        }
    }
}

@Composable
private fun ReportLine(
    title: String,
    value: String,
    isBold: Boolean = false,
    isPositive: Boolean = false,
    isNegative: Boolean = false,
    fontSize: androidx.compose.ui.unit.TextUnit = 13.sp,
    color: Color = Color.Unspecified
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, fontSize = fontSize, fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal)
        Text(
            text = value,
            fontSize = fontSize,
            fontWeight = if (isBold) FontWeight.ExtraBold else FontWeight.Medium,
            color = when {
                color != Color.Unspecified -> color
                isNegative -> Color(0xFFDC2626)
                isPositive -> Color(0xFF15803D)
                else -> MaterialTheme.colorScheme.onSurface
            }
        )
    }
}
