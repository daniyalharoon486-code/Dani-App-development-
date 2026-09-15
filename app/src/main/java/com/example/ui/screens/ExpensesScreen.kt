package com.example.ui.screens

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
import com.example.data.model.ExpenseEntity
import com.example.ui.common.AppStrings
import com.example.ui.common.Formatters
import com.example.ui.components.AddExpenseDialog
import com.example.ui.viewmodel.DukanViewModel

@Composable
fun ExpensesScreen(
    viewModel: DukanViewModel
) {
    val isUrdu by viewModel.isUrdu.collectAsState()
    val expenses by viewModel.expenses.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf<String?>(null) }

    val totalExpenses = expenses.sumOf { it.amount }
    val filteredExpenses = remember(expenses, selectedCategory) {
        if (selectedCategory == null) expenses else expenses.filter { it.category.equals(selectedCategory, ignoreCase = true) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("expenses_screen")
    ) {
        // Top Card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFEE2E2)),
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
                        text = if (isUrdu) "دکان کے کل اخراجات" else "Total Shop Expenses",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF991B1B)
                    )
                    Text(
                        text = Formatters.formatPkr(totalExpenses),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFB91C1C)
                    )
                }

                Button(
                    onClick = { showAddDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isUrdu) "نیا خرچہ" else "Add Expense")
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Categories
        val cats = listOf("Rent", "Electricity", "Salary", "Transport", "Shop Maintenance", "Internet")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FilterChip(
                selected = (selectedCategory == null),
                onClick = { selectedCategory = null },
                label = { Text("All", fontSize = 11.sp) }
            )
            cats.take(4).forEach { c ->
                FilterChip(
                    selected = (selectedCategory == c),
                    onClick = { selectedCategory = c },
                    label = { Text(c, fontSize = 11.sp) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredExpenses, key = { it.id }) { expense ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(expense.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(
                                text = "${expense.category} • ${expense.paymentAccount}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = Formatters.formatDateTime(expense.date),
                                fontSize = 10.sp,
                                color = Color.Gray
                            )
                        }

                        Text(
                            text = Formatters.formatPkr(expense.amount),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            color = Color(0xFFDC2626)
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddExpenseDialog(
            isUrdu = isUrdu,
            onDismiss = { showAddDialog = false },
            onSave = { title, cat, amt, method, notes ->
                viewModel.addExpense(title, cat, amt, method, notes)
                showAddDialog = false
            }
        )
    }
}
