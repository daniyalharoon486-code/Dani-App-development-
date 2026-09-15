package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CustomerEntity
import com.example.data.model.ProductEntity
import com.example.data.model.SaleEntity
import com.example.ui.common.AppStrings
import com.example.ui.common.Formatters
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.DateFilterPeriod
import com.example.ui.viewmodel.DukanViewModel

@Composable
fun DashboardScreen(
    viewModel: DukanViewModel,
    onNavigate: (AppScreen) -> Unit,
    onOpenAddCustomer: () -> Unit,
    onOpenAddProduct: () -> Unit,
    onOpenAddExpense: () -> Unit,
    onOpenReceivePayment: () -> Unit
) {
    val isUrdu by viewModel.isUrdu.collectAsState()
    val allSales by viewModel.sales.collectAsState()
    val allExpenses by viewModel.expenses.collectAsState()
    val allProducts by viewModel.products.collectAsState()
    val allCustomers by viewModel.customers.collectAsState()
    val dateFilter by viewModel.dateFilter.collectAsState()
    val lowStockList by viewModel.lowStockProducts.collectAsState()

    val filteredSales = remember(allSales, dateFilter) {
        viewModel.getFilteredSales(allSales, dateFilter)
    }

    // Calculations
    val totalSales = filteredSales.sumOf { it.grandTotal }
    val cashReceived = filteredSales.sumOf { it.paidAmount }
    val creditSales = filteredSales.sumOf { it.remainingAmount }
    val numberOfBills = filteredSales.size

    val totalCost = filteredSales.sumOf { it.totalCost }
    val grossProfit = (totalSales - totalCost).coerceAtLeast(0.0)

    val periodExpenses = allExpenses.sumOf { it.amount }
    val netProfit = grossProfit - periodExpenses

    val totalCustomerDue = allCustomers.sumOf { it.currentBalance }
    val totalStockValue = allProducts.sumOf { it.currentStock * it.purchasePrice }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("dashboard_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 32.dp)
    ) {
        // Date Filter Pills
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DateFilterChip(
                    label = AppStrings.t("today", isUrdu),
                    selected = dateFilter == DateFilterPeriod.TODAY,
                    onClick = { viewModel.dateFilter.value = DateFilterPeriod.TODAY }
                )
                DateFilterChip(
                    label = AppStrings.t("yesterday", isUrdu),
                    selected = dateFilter == DateFilterPeriod.YESTERDAY,
                    onClick = { viewModel.dateFilter.value = DateFilterPeriod.YESTERDAY }
                )
                DateFilterChip(
                    label = AppStrings.t("this_week", isUrdu),
                    selected = dateFilter == DateFilterPeriod.THIS_WEEK,
                    onClick = { viewModel.dateFilter.value = DateFilterPeriod.THIS_WEEK }
                )
                DateFilterChip(
                    label = AppStrings.t("this_month", isUrdu),
                    selected = dateFilter == DateFilterPeriod.THIS_MONTH,
                    onClick = { viewModel.dateFilter.value = DateFilterPeriod.THIS_MONTH }
                )
                DateFilterChip(
                    label = AppStrings.t("all_time", isUrdu),
                    selected = dateFilter == DateFilterPeriod.ALL_TIME,
                    onClick = { viewModel.dateFilter.value = DateFilterPeriod.ALL_TIME }
                )
            }
        }

        // Low stock warning banner if any
        if (lowStockList.isNotEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().clickable { onNavigate(AppScreen.INVENTORY) }
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFD97706))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "⚠ ${lowStockList.size} Products Low on Stock!",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF92400E),
                                fontSize = 14.sp
                            )
                            Text(
                                text = lowStockList.take(3).joinToString(", ") { "${it.name} (${it.currentStock.toInt()} left)" },
                                fontSize = 12.sp,
                                color = Color(0xFFB45309)
                            )
                        }
                        TextButton(onClick = { onNavigate(AppScreen.INVENTORY) }) {
                            Text(if (isUrdu) "دیکھیں" else "View Stock", color = Color(0xFFB45309), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Quick Action Buttons
        item {
            Text(
                text = AppStrings.t("quick_actions", isUrdu),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickActionButton(
                    title = if (isUrdu) "نئی فروخت" else "New Sale",
                    icon = Icons.Default.PointOfSale,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f),
                    testTag = "quick_new_sale",
                    onClick = { onNavigate(AppScreen.POS) }
                )
                QuickActionButton(
                    title = if (isUrdu) "وصولی رقم" else "Receive Payment",
                    icon = Icons.Default.Payments,
                    color = Color(0xFF16A34A),
                    modifier = Modifier.weight(1f),
                    testTag = "quick_receive_payment",
                    onClick = onOpenReceivePayment
                )
                QuickActionButton(
                    title = if (isUrdu) "+ گاہک" else "+ Customer",
                    icon = Icons.Default.PersonAdd,
                    color = Color(0xFF2563EB),
                    modifier = Modifier.weight(1f),
                    testTag = "quick_add_customer",
                    onClick = onOpenAddCustomer
                )
                QuickActionButton(
                    title = if (isUrdu) "+ خرچہ" else "+ Expense",
                    icon = Icons.Default.Receipt,
                    color = Color(0xFFDC2626),
                    modifier = Modifier.weight(1f),
                    testTag = "quick_add_expense",
                    onClick = onOpenAddExpense
                )
            }
        }

        // Key Business Performance Cards
        item {
            Text(
                text = if (isUrdu) "کاروباری خلاصہ و اعداد و شمار" else "Business Performance Cards",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    KpiCard(
                        title = AppStrings.t("total_sales", isUrdu),
                        value = Formatters.formatPkr(totalSales),
                        subtitle = "$numberOfBills ${AppStrings.t("number_of_bills", isUrdu)}",
                        icon = Icons.Default.ShoppingBag,
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    KpiCard(
                        title = AppStrings.t("cash_received", isUrdu),
                        value = Formatters.formatPkr(cashReceived),
                        subtitle = if (isUrdu) "نقد و بینک وصولی" else "Paid on spot",
                        icon = Icons.Default.CheckCircle,
                        containerColor = Color(0xFFDCFCE7),
                        contentColor = Color(0xFF15803D),
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    KpiCard(
                        title = AppStrings.t("credit_sales", isUrdu),
                        value = Formatters.formatPkr(creditSales),
                        subtitle = if (isUrdu) "ادھار کھاتہ بل" else "Added to customer balance",
                        icon = Icons.Default.Book,
                        containerColor = Color(0xFFFEF3C7),
                        contentColor = Color(0xFFB45309),
                        modifier = Modifier.weight(1f)
                    )
                    KpiCard(
                        title = AppStrings.t("outstanding_balance", isUrdu),
                        value = Formatters.formatPkr(totalCustomerDue),
                        subtitle = "${allCustomers.count { it.currentBalance > 0 }} customers have dues",
                        icon = Icons.Default.AccountBalanceWallet,
                        containerColor = Color(0xFFFFEDD5),
                        contentColor = Color(0xFFC2410C),
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    KpiCard(
                        title = AppStrings.t("gross_profit", isUrdu),
                        value = Formatters.formatPkr(grossProfit),
                        subtitle = "Revenue − Product Cost",
                        icon = Icons.Default.TrendingUp,
                        containerColor = Color(0xFFE0E7FF),
                        contentColor = Color(0xFF4338CA),
                        modifier = Modifier.weight(1f)
                    )
                    KpiCard(
                        title = AppStrings.t("net_profit", isUrdu),
                        value = Formatters.formatPkr(netProfit),
                        subtitle = "Gross Profit − Expenses",
                        icon = Icons.Default.MonetizationOn,
                        containerColor = if (netProfit >= 0) Color(0xFFECFDF5) else Color(0xFFFEE2E2),
                        contentColor = if (netProfit >= 0) Color(0xFF047857) else Color(0xFFB91C1C),
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    KpiCard(
                        title = if (isUrdu) "کل اخراجات" else "Total Expenses",
                        value = Formatters.formatPkr(periodExpenses),
                        subtitle = "${allExpenses.size} expenses recorded",
                        icon = Icons.Default.ReceiptLong,
                        containerColor = Color(0xFFFEE2E2),
                        contentColor = Color(0xFFB91C1C),
                        modifier = Modifier.weight(1f)
                    )
                    KpiCard(
                        title = AppStrings.t("stock_value", isUrdu),
                        value = Formatters.formatPkr(totalStockValue),
                        subtitle = "${allProducts.size} active products",
                        icon = Icons.Default.Warehouse,
                        containerColor = Color(0xFFF3E8FF),
                        contentColor = Color(0xFF7E22CE),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Performance Visualizers (Daily Sales Trend & Best Selling Items)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isUrdu) "فروخت کا رجحان اور تجزیہ" else "Sales Trend & Analytics",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Visual Bar distribution
                    Text(
                        text = if (isUrdu) "ادائیگی کے طریقوں کا تناسب" else "Payment Breakdown (Cash vs Credit vs Digital)",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(18.dp)
                            .background(Color.LightGray, RoundedCornerShape(9.dp))
                    ) {
                        val totalVal = if (totalSales > 0) totalSales else 1.0
                        val cashWeight = (cashReceived / totalVal).toFloat().coerceIn(0.05f, 0.95f)
                        val creditWeight = (creditSales / totalVal).toFloat().coerceIn(0.05f, 0.95f)

                        Box(
                            modifier = Modifier
                                .weight(cashWeight)
                                .fillMaxHeight()
                                .background(Color(0xFF16A34A), RoundedCornerShape(topStart = 9.dp, bottomStart = 9.dp))
                        )
                        Box(
                            modifier = Modifier
                                .weight(creditWeight)
                                .fillMaxHeight()
                                .background(Color(0xFFD97706), RoundedCornerShape(topEnd = 9.dp, bottomEnd = 9.dp))
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(10.dp).background(Color(0xFF16A34A), RoundedCornerShape(2.dp)))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Cash/Digital: ${Formatters.formatPkr(cashReceived)}", fontSize = 11.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(10.dp).background(Color(0xFFD97706), RoundedCornerShape(2.dp)))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Udhaar/Credit: ${Formatters.formatPkr(creditSales)}", fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // Recent Invoices on Dashboard
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isUrdu) "حالیہ فروخت بل" else "Recent Sales Invoices",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                TextButton(onClick = { onNavigate(AppScreen.INVOICES) }) {
                    Text(if (isUrdu) "تمام دیکھیں" else "View All Invoices")
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                filteredSales.take(5).forEach { sale ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.viewReceipt(sale)
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(sale.invoiceNumber, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(sale.customerName, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(Formatters.formatDateTime(sale.timestamp), fontSize = 10.sp, color = Color.Gray)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(Formatters.formatPkr(sale.grandTotal), fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                                Text(
                                    text = sale.paymentStatus.name,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (sale.paymentStatus.name == "PAID") Color(0xFF16A34A) else Color(0xFFDC2626)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DateFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, fontSize = 12.sp) },
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
private fun QuickActionButton(
    title: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    testTag: String,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.12f)),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .height(72.dp)
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color, maxLines = 1)
        }
    }
}

@Composable
private fun KpiCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier.height(115.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = contentColor.copy(alpha = 0.85f),
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
                Icon(icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(20.dp))
            }

            Column {
                Text(
                    text = value,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = contentColor,
                    maxLines = 1
                )
                Text(
                    text = subtitle,
                    fontSize = 10.sp,
                    color = contentColor.copy(alpha = 0.75f),
                    maxLines = 1
                )
            }
        }
    }
}
