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
import com.example.data.model.ProductEntity
import com.example.ui.common.AppStrings
import com.example.ui.common.Formatters
import com.example.ui.viewmodel.DukanViewModel

@Composable
fun InventoryScreen(
    viewModel: DukanViewModel
) {
    val isUrdu by viewModel.isUrdu.collectAsState()
    val products by viewModel.products.collectAsState()
    val lowStockList by viewModel.lowStockProducts.collectAsState()
    val outOfStockList by viewModel.outOfStockProducts.collectAsState()

    var filterMode by remember { mutableStateOf("ALL") } // ALL, LOW, OUT
    var productToAdjust by remember { mutableStateOf<ProductEntity?>(null) }

    // Valuations
    val totalCostValue = products.sumOf { it.currentStock * it.purchasePrice }
    val totalRetailValue = products.sumOf { it.currentStock * it.salePrice }
    val potentialProfit = totalRetailValue - totalCostValue

    val displayList = remember(products, lowStockList, outOfStockList, filterMode) {
        when (filterMode) {
            "LOW" -> lowStockList
            "OUT" -> outOfStockList
            else -> products
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("inventory_screen")
    ) {
        // Valuation Summary Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (isUrdu) "اسٹاک کی کل مالیت و تخمینہ" else "Total Inventory Valuation",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Cost Valuation:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text(Formatters.formatPkr(totalCostValue), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    Column {
                        Text("Retail Valuation:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text(Formatters.formatPkr(totalRetailValue), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Potential Profit:", fontSize = 11.sp, color = Color(0xFF15803D))
                        Text(Formatters.formatPkr(potentialProfit), fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = Color(0xFF15803D))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Tabs / Filters
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = (filterMode == "ALL"),
                onClick = { filterMode = "ALL" },
                label = { Text("All Items (${products.size})") }
            )
            FilterChip(
                selected = (filterMode == "LOW"),
                onClick = { filterMode = "LOW" },
                label = { Text("Low Stock (${lowStockList.size})") }
            )
            FilterChip(
                selected = (filterMode == "OUT"),
                onClick = { filterMode = "OUT" },
                label = { Text("Out of Stock (${outOfStockList.size})") }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(displayList, key = { it.id }) { product ->
                InventoryItemRow(
                    product = product,
                    isUrdu = isUrdu,
                    onAdjustStock = { productToAdjust = product }
                )
            }
        }
    }

    if (productToAdjust != null) {
        AdjustStockModal(
            product = productToAdjust!!,
            isUrdu = isUrdu,
            onDismiss = { productToAdjust = null },
            onConfirm = { newStock, isDamaged, reason ->
                viewModel.adjustStock(productToAdjust!!, newStock, isDamaged, reason)
                productToAdjust = null
            }
        )
    }
}

@Composable
private fun InventoryItemRow(
    product: ProductEntity,
    isUrdu: Boolean,
    onAdjustStock: () -> Unit
) {
    val isLow = product.currentStock <= product.minStock
    val isOut = product.currentStock <= 0

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
            Column(modifier = Modifier.weight(1f)) {
                Text(product.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(
                    text = "Category: ${product.category} • Cost: ${Formatters.formatPkr(product.purchasePrice)}",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
                Text(
                    text = "Min Stock Alert Level: ${product.minStock.toInt()} ${product.unit}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${product.currentStock.toInt()} ${product.unit}",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    color = when {
                        isOut -> Color(0xFFDC2626)
                        isLow -> Color(0xFFD97706)
                        else -> Color(0xFF16A34A)
                    }
                )

                Spacer(modifier = Modifier.height(4.dp))

                OutlinedButton(
                    onClick = onAdjustStock,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isUrdu) "درست کریں" else "Adjust", fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
private fun AdjustStockModal(
    product: ProductEntity,
    isUrdu: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (newStock: Double, isDamaged: Boolean, reason: String) -> Unit
) {
    var stockInput by remember { mutableStateOf(product.currentStock.toInt().toString()) }
    var isDamaged by remember { mutableStateOf(false) }
    var reason by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isUrdu) "اسٹاک کی مقدار درست کریں" else "Adjust Stock Count") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Product: ${product.name}", fontWeight = FontWeight.Bold)
                Text("Current Recorded Stock: ${product.currentStock.toInt()} ${product.unit}", color = Color.Gray, fontSize = 12.sp)

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = stockInput,
                    onValueChange = { input -> if (input.all { it.isDigit() || it == '.' }) stockInput = input },
                    label = { Text("New Physical Stock Count") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isDamaged, onCheckedChange = { isDamaged = it })
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isUrdu) "خراب / ضائع شدہ مال ہے" else "Mark difference as Damaged / Expired Stock")
                }

                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Reason (e.g., Physical count audit, damage, etc.)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val newQty = stockInput.toDoubleOrNull() ?: product.currentStock
                onConfirm(newQty, isDamaged, reason)
            }) {
                Text(if (isUrdu) "اسٹاک اپ ڈیٹ کریں" else "Update Stock")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
