package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CustomerEntity
import com.example.data.model.ProductEntity
import com.example.data.repository.CartItem
import com.example.ui.common.AppStrings
import com.example.ui.common.Formatters
import com.example.ui.components.BarcodeScannerDialog
import com.example.ui.components.CheckoutDialog
import com.example.ui.components.ReceiptDialog
import com.example.ui.viewmodel.DukanViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PosScreen(
    viewModel: DukanViewModel,
    onOpenAddProduct: () -> Unit,
    onOpenAddCustomer: () -> Unit
) {
    val isUrdu by viewModel.isUrdu.collectAsState()
    val products by viewModel.products.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val cartItems by viewModel.cartItems.collectAsState()
    val selectedCustomer by viewModel.selectedCustomer.collectAsState()
    val allCustomers by viewModel.customers.collectAsState()
    val heldBills by viewModel.heldBills.collectAsState()
    val settings by viewModel.settings.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var showBarcodeDialog by remember { mutableStateOf(false) }
    var showCheckoutDialog by remember { mutableStateOf(false) }
    var showHeldBillsSheet by remember { mutableStateOf(false) }

    val activeReceiptSale by viewModel.activeReceiptSale.collectAsState()
    val activeReceiptItems by viewModel.activeReceiptItems.collectAsState()

    // Filter products
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

    // Calculations
    val subtotal = cartItems.sumOf { it.total }
    val overallDiscount by viewModel.overallDiscount.collectAsState()
    val taxPercent by viewModel.taxPercent.collectAsState()
    val taxAmount = if (taxPercent > 0) (subtotal - overallDiscount) * (taxPercent / 100.0) else 0.0
    val grandTotal = (subtotal - overallDiscount + taxAmount).coerceAtLeast(0.0)

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .testTag("pos_screen")
    ) {
        val isWideScreen = maxWidth >= 768.dp

        if (isWideScreen) {
            // Tablet / Desktop Layout: 2 Columns
            Row(modifier = Modifier.fillMaxSize().padding(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                // Left Column: Catalog
                Column(modifier = Modifier.weight(1.35f).fillMaxHeight()) {
                    PosCatalogHeader(
                        searchQuery = searchQuery,
                        onSearchChange = { searchQuery = it },
                        onOpenBarcode = { showBarcodeDialog = true },
                        categories = categories,
                        selectedCategory = selectedCategory,
                        onCategorySelect = { selectedCategory = it },
                        isUrdu = isUrdu,
                        onOpenAddProduct = onOpenAddProduct
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ProductGrid(
                        products = filteredProducts,
                        onProductClick = { viewModel.addToCart(it) },
                        isUrdu = isUrdu,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Right Column: Active Cart
                Card(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    CartPaneContent(
                        cartItems = cartItems,
                        subtotal = subtotal,
                        overallDiscount = overallDiscount,
                        taxPercent = taxPercent,
                        taxAmount = taxAmount,
                        grandTotal = grandTotal,
                        selectedCustomer = selectedCustomer,
                        heldBillsCount = heldBills.size,
                        isUrdu = isUrdu,
                        onUpdateQty = { id, qty -> viewModel.updateCartItemQuantity(id, qty) },
                        onRemove = { id -> viewModel.removeFromCart(id) },
                        onClear = { viewModel.clearCart() },
                        onHold = { viewModel.holdBill() },
                        onViewHeld = { showHeldBillsSheet = true },
                        onDiscountChange = { viewModel.overallDiscount.value = it },
                        onTaxChange = { viewModel.taxPercent.value = it },
                        onOpenCheckout = { showCheckoutDialog = true },
                        onOpenAddCustomer = onOpenAddCustomer
                    )
                }
            }
        } else {
            // Mobile Layout: Catalog on Top with a Bottom Sticky Cart Summary bar
            var showMobileCartDetails by remember { mutableStateOf(false) }

            Column(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp)) {
                PosCatalogHeader(
                    searchQuery = searchQuery,
                    onSearchChange = { searchQuery = it },
                    onOpenBarcode = { showBarcodeDialog = true },
                    categories = categories,
                    selectedCategory = selectedCategory,
                    onCategorySelect = { selectedCategory = it },
                    isUrdu = isUrdu,
                    onOpenAddProduct = onOpenAddProduct
                )
                Spacer(modifier = Modifier.height(6.dp))
                ProductGrid(
                    products = filteredProducts,
                    onProductClick = { viewModel.addToCart(it) },
                    isUrdu = isUrdu,
                    modifier = Modifier.weight(1f)
                )

                // Sticky Bottom Cart Bar
                if (cartItems.isNotEmpty()) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showMobileCartDetails = true }
                            .padding(top = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "${cartItems.sumOf { it.quantity }.toInt()} Items in Cart",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = Formatters.formatPkr(grandTotal),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Button(
                                onClick = { showCheckoutDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text(if (isUrdu) "وصولی" else "Checkout")
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Default.ArrowForward, contentDescription = null)
                            }
                        }
                    }
                }
            }

            // Mobile Full Cart Sheet
            if (showMobileCartDetails) {
                ModalBottomSheet(onDismissRequest = { showMobileCartDetails = false }) {
                    CartPaneContent(
                        cartItems = cartItems,
                        subtotal = subtotal,
                        overallDiscount = overallDiscount,
                        taxPercent = taxPercent,
                        taxAmount = taxAmount,
                        grandTotal = grandTotal,
                        selectedCustomer = selectedCustomer,
                        heldBillsCount = heldBills.size,
                        isUrdu = isUrdu,
                        onUpdateQty = { id, qty -> viewModel.updateCartItemQuantity(id, qty) },
                        onRemove = { id -> viewModel.removeFromCart(id) },
                        onClear = { viewModel.clearCart() },
                        onHold = { viewModel.holdBill(); showMobileCartDetails = false },
                        onViewHeld = { showHeldBillsSheet = true },
                        onDiscountChange = { viewModel.overallDiscount.value = it },
                        onTaxChange = { viewModel.taxPercent.value = it },
                        onOpenCheckout = { showMobileCartDetails = false; showCheckoutDialog = true },
                        onOpenAddCustomer = onOpenAddCustomer
                    )
                }
            }
        }
    }

    // Barcode Scanner Dialog
    if (showBarcodeDialog) {
        BarcodeScannerDialog(
            isUrdu = isUrdu,
            onBarcodeScanned = { barcode -> viewModel.scanBarcode(barcode) },
            onDismiss = { showBarcodeDialog = false }
        )
    }

    // Checkout / Split Payment Dialog
    if (showCheckoutDialog) {
        CheckoutDialog(
            grandTotal = grandTotal,
            customer = selectedCustomer,
            isUrdu = isUrdu,
            customersList = allCustomers,
            onSelectCustomer = { viewModel.selectedCustomer.value = it },
            onDismiss = { showCheckoutDialog = false },
            onCompleteSale = { splits ->
                showCheckoutDialog = false
                viewModel.checkoutSale(splits) { /* completed */ }
            }
        )
    }

    // Held Bills Bottom Sheet
    if (showHeldBillsSheet) {
        ModalBottomSheet(onDismissRequest = { showHeldBillsSheet = false }) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Text(
                    text = if (isUrdu) "ہولڈ کیے گئے بل (Drafts)" else "Held Bills (${heldBills.size})",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(12.dp))
                if (heldBills.isEmpty()) {
                    Text(if (isUrdu) "کوئی محفوظ بل موجود نہیں ہے" else "No bills are currently on hold.")
                } else {
                    heldBills.forEach { held ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(held.id, fontWeight = FontWeight.Bold)
                                    Text("Customer: ${held.customer?.name ?: "Walk-in"}", fontSize = 12.sp)
                                    Text("${held.items.size} items • ${Formatters.formatTime(held.timestamp)}", fontSize = 11.sp, color = Color.DarkGray)
                                }
                                Button(onClick = {
                                    viewModel.recallHeldBill(held)
                                    showHeldBillsSheet = false
                                }) {
                                    Text(if (isUrdu) "بحال کریں" else "Recall")
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Receipt Dialog when Sale is finished or viewed
    if (activeReceiptSale != null) {
        ReceiptDialog(
            sale = activeReceiptSale!!,
            items = activeReceiptItems,
            settings = settings,
            isUrdu = isUrdu,
            onDismiss = { viewModel.closeReceipt() }
        )
    }
}

@Composable
private fun PosCatalogHeader(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onOpenBarcode: () -> Unit,
    categories: List<String>,
    selectedCategory: String?,
    onCategorySelect: (String?) -> Unit,
    isUrdu: Boolean,
    onOpenAddProduct: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text(AppStrings.t("search_products", isUrdu), fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier.weight(1f).testTag("pos_search_input"),
                shape = RoundedCornerShape(12.dp)
            )

            IconButton(
                onClick = onOpenBarcode,
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(12.dp))
                    .testTag("pos_barcode_button")
            ) {
                Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan Barcode", tint = MaterialTheme.colorScheme.primary)
            }

            IconButton(
                onClick = onOpenAddProduct,
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(12.dp))
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Product", tint = MaterialTheme.colorScheme.secondary)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Category Pills
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                FilterChip(
                    selected = (selectedCategory == null),
                    onClick = { onCategorySelect(null) },
                    label = { Text(AppStrings.t("all_categories", isUrdu), fontSize = 11.sp) }
                )
            }
            items(categories) { cat ->
                FilterChip(
                    selected = (selectedCategory == cat),
                    onClick = { onCategorySelect(cat) },
                    label = { Text(cat, fontSize = 11.sp) }
                )
            }
        }
    }
}

@Composable
private fun ProductGrid(
    products: List<ProductEntity>,
    onProductClick: (ProductEntity) -> Unit,
    isUrdu: Boolean,
    modifier: Modifier = Modifier
) {
    if (products.isEmpty()) {
        Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text("No products found.", color = Color.Gray)
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 130.dp),
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(products, key = { it.id }) { product ->
                ProductPosCard(product = product, isUrdu = isUrdu, onClick = { onProductClick(product) })
            }
        }
    }
}

@Composable
private fun ProductPosCard(
    product: ProductEntity,
    isUrdu: Boolean,
    onClick: () -> Unit
) {
    val isLowStock = product.currentStock <= product.minStock
    val isOut = product.currentStock <= 0

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .clickable(onClick = onClick)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Stock Badge
                val badgeColor = when {
                    isOut -> Color(0xFFEF4444)
                    isLowStock -> Color(0xFFF59E0B)
                    else -> Color(0xFF10B981)
                }
                Box(
                    modifier = Modifier
                        .background(badgeColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${product.currentStock.toInt()} ${product.unit}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = badgeColor
                    )
                }

                if (product.barcode.isNotBlank()) {
                    Text(
                        text = product.barcode.takeLast(4),
                        fontSize = 9.sp,
                        color = Color.Gray
                    )
                }
            }

            Column {
                Text(
                    text = product.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    maxLines = 2,
                    lineHeight = 16.sp
                )
                if (isUrdu && product.nameUrdu.isNotBlank()) {
                    Text(
                        text = product.nameUrdu,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = Formatters.formatPkr(product.salePrice),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
private fun CartPaneContent(
    cartItems: List<CartItem>,
    subtotal: Double,
    overallDiscount: Double,
    taxPercent: Double,
    taxAmount: Double,
    grandTotal: Double,
    selectedCustomer: CustomerEntity?,
    heldBillsCount: Int,
    isUrdu: Boolean,
    onUpdateQty: (Long, Double) -> Unit,
    onRemove: (Long) -> Unit,
    onClear: () -> Unit,
    onHold: () -> Unit,
    onViewHeld: () -> Unit,
    onDiscountChange: (Double) -> Unit,
    onTaxChange: (Double) -> Unit,
    onOpenCheckout: () -> Unit,
    onOpenAddCustomer: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        // Cart Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = AppStrings.t("cart", isUrdu),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                if (cartItems.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Badge { Text("${cartItems.size}") }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                if (heldBillsCount > 0) {
                    FilledTonalButton(onClick = onViewHeld, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)) {
                        Text("Drafts ($heldBillsCount)", fontSize = 11.sp)
                    }
                }
                if (cartItems.isNotEmpty()) {
                    IconButton(onClick = onHold) {
                        Icon(Icons.Default.Pause, contentDescription = "Hold Bill", tint = Color(0xFFD97706))
                    }
                    IconButton(onClick = onClear) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Clear", tint = Color(0xFFDC2626))
                    }
                }
            }
        }

        // Customer Banner
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = selectedCustomer?.name ?: AppStrings.t("walk_in_customer", isUrdu),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                if (selectedCustomer != null) {
                    Text(
                        text = "Bal: ${Formatters.formatPkr(selectedCustomer.currentBalance)}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (selectedCustomer.currentBalance > 0) Color(0xFFDC2626) else Color(0xFF16A34A)
                    )
                } else {
                    IconButton(onClick = onOpenAddCustomer, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.PersonAdd, contentDescription = "Add Customer", modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        Divider(modifier = Modifier.padding(vertical = 4.dp))

        // Cart Items List
        if (cartItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.RemoveShoppingCart, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = AppStrings.t("empty_cart", isUrdu),
                        fontSize = 12.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(cartItems, key = { it.product.id }) { item ->
                    CartItemRow(
                        item = item,
                        onUpdateQty = { newQty -> onUpdateQty(item.product.id, newQty) },
                        onRemove = { onRemove(item.product.id) }
                    )
                }
            }
        }

        Divider(modifier = Modifier.padding(vertical = 4.dp))

        // Financial Summary Block
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(AppStrings.t("subtotal", isUrdu), fontSize = 12.sp)
                Text(Formatters.formatPkr(subtotal), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(AppStrings.t("discount", isUrdu), fontSize = 12.sp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Rs. ", fontSize = 11.sp, color = Color.Gray)
                    var discStr by remember(overallDiscount) { mutableStateOf(if (overallDiscount > 0) overallDiscount.toInt().toString() else "") }
                    BasicNumberInput(
                        value = discStr,
                        onValueChange = {
                            discStr = it
                            onDiscountChange(it.toDoubleOrNull() ?: 0.0)
                        }
                    )
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(AppStrings.t("tax", isUrdu) + " (%)", fontSize = 12.sp)
                var taxStr by remember(taxPercent) { mutableStateOf(if (taxPercent > 0) taxPercent.toInt().toString() else "") }
                BasicNumberInput(
                    value = taxStr,
                    onValueChange = {
                        taxStr = it
                        onTaxChange(it.toDoubleOrNull() ?: 0.0)
                    }
                )
            }

            Divider(modifier = Modifier.padding(vertical = 2.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = AppStrings.t("grand_total", isUrdu),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp
                )
                Text(
                    text = Formatters.formatPkr(grandTotal),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Large Checkout Button
        Button(
            onClick = onOpenCheckout,
            enabled = cartItems.isNotEmpty(),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("pos_checkout_button"),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Payment, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "${AppStrings.t("checkout", isUrdu)} • ${Formatters.formatPkr(grandTotal)}",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun CartItemRow(
    item: CartItem,
    onUpdateQty: (Double) -> Unit,
    onRemove: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(item.product.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1)
                Text(
                    text = "${Formatters.formatPkr(item.unitPrice)} × ${item.quantity.toInt()} ${item.product.unit}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Qty Stepper
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { onUpdateQty(item.quantity - 1.0) },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Decrease", modifier = Modifier.size(16.dp))
                }

                Text(
                    text = "${item.quantity.toInt()}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    modifier = Modifier.widthIn(min = 20.dp),
                    textAlign = TextAlign.Center
                )

                IconButton(
                    onClick = { onUpdateQty(item.quantity + 1.0) },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Increase", modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = Formatters.formatPkr(item.total),
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.primary
            )

            IconButton(onClick = onRemove, modifier = Modifier.size(26.dp)) {
                Icon(Icons.Default.Close, contentDescription = "Delete", tint = Color.Gray, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun BasicNumberInput(
    value: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) onValueChange(it) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        modifier = Modifier
            .width(70.dp)
            .height(40.dp),
        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, textAlign = TextAlign.End)
    )
}
