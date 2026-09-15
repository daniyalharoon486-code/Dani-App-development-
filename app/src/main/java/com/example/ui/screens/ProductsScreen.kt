package com.example.ui.screens

import androidx.compose.foundation.clickable
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
import com.example.data.model.ProductEntity
import com.example.ui.common.AppStrings
import com.example.ui.common.Formatters
import com.example.ui.components.AddProductDialog
import com.example.ui.viewmodel.DukanViewModel

@Composable
fun ProductsScreen(
    viewModel: DukanViewModel
) {
    val isUrdu by viewModel.isUrdu.collectAsState()
    val products by viewModel.products.collectAsState()
    val categories by viewModel.categories.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingProduct by remember { mutableStateOf<ProductEntity?>(null) }

    val filteredProducts = remember(products, searchQuery, selectedCategory) {
        products.filter { p ->
            val matchesCategory = selectedCategory == null || p.category.equals(selectedCategory, ignoreCase = true)
            val matchesSearch = searchQuery.isBlank() ||
                    p.name.contains(searchQuery, ignoreCase = true) ||
                    p.nameUrdu.contains(searchQuery, ignoreCase = true) ||
                    p.barcode.contains(searchQuery, ignoreCase = true) ||
                    p.sku.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("products_screen")
    ) {
        // Search and Add Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text(if (isUrdu) "پروڈکٹ کا نام یا بارکوڈ تلاش کریں..." else "Search product name, barcode, SKU...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            )

            Button(
                onClick = { showAddDialog = true },
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (isUrdu) "شامل کریں" else "Add")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Categories filter
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FilterChip(
                selected = (selectedCategory == null),
                onClick = { selectedCategory = null },
                label = { Text("All (${products.size})", fontSize = 11.sp) }
            )
            categories.forEach { cat ->
                FilterChip(
                    selected = (selectedCategory == cat),
                    onClick = { selectedCategory = cat },
                    label = { Text(cat, fontSize = 11.sp) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredProducts, key = { it.id }) { product ->
                ProductItemCard(
                    product = product,
                    isUrdu = isUrdu,
                    onEdit = { editingProduct = product },
                    onDelete = { viewModel.deleteProduct(product.id) }
                )
            }
        }
    }

    if (showAddDialog) {
        AddProductDialog(
            isUrdu = isUrdu,
            existing = null,
            onDismiss = { showAddDialog = false },
            onSave = {
                viewModel.addProduct(it)
                showAddDialog = false
            }
        )
    }

    if (editingProduct != null) {
        AddProductDialog(
            isUrdu = isUrdu,
            existing = editingProduct,
            onDismiss = { editingProduct = null },
            onSave = {
                viewModel.updateProduct(it)
                editingProduct = null
            }
        )
    }
}

@Composable
private fun ProductItemCard(
    product: ProductEntity,
    isUrdu: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val margin = if (product.salePrice > 0) ((product.salePrice - product.purchasePrice) / product.salePrice) * 100 else 0.0
    val isLowStock = product.currentStock <= product.minStock

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onEdit),
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(product.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    if (isLowStock) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Badge(containerColor = Color(0xFFFEF3C7)) {
                            Text("Low Stock", color = Color(0xFFB45309), fontSize = 10.sp)
                        }
                    }
                }
                if (isUrdu && product.nameUrdu.isNotBlank()) {
                    Text(product.nameUrdu, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                }
                Text(
                    text = "Category: ${product.category} • Barcode: ${product.barcode.ifEmpty { "N/A" }}",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
                Text(
                    text = "Cost: ${Formatters.formatPkr(product.purchasePrice)} • Margin: ${margin.toInt()}%",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = Formatters.formatPkr(product.salePrice),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Stock: ${product.currentStock.toInt()} ${product.unit}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isLowStock) Color(0xFFDC2626) else Color(0xFF16A34A)
                )

                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}
