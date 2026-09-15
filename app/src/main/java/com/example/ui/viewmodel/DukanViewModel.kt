package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.DukanDatabase
import com.example.data.database.InitialDataSeeder
import com.example.data.model.*
import com.example.data.repository.CartItem
import com.example.data.repository.DukanRepository
import com.example.data.repository.PaymentSplitItem
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

enum class AppScreen(val iconName: String) {
    DASHBOARD("dashboard"),
    POS("point_of_sale"),
    INVOICES("receipt_long"),
    RECEIVE_PAYMENT("payments"),
    CUSTOMERS("people"),
    PRODUCTS("inventory_2"),
    INVENTORY("warehouse"),
    SUPPLIERS("local_shipping"),
    PURCHASES("shopping_bag"),
    EXPENSES("account_balance_wallet"),
    CASH_BANK("account_balance"),
    REPORTS("analytics"),
    STAFF_USERS("badge"),
    SETTINGS("settings"),
    RETURNS("assignment_return")
}

enum class DateFilterPeriod {
    TODAY,
    YESTERDAY,
    THIS_WEEK,
    THIS_MONTH,
    ALL_TIME
}

data class HeldBill(
    val id: String,
    val items: List<CartItem>,
    val customer: CustomerEntity?,
    val overallDiscount: Double,
    val taxPercent: Double,
    val timestamp: Long = System.currentTimeMillis()
)

class DukanViewModel(application: Application) : AndroidViewModel(application) {
    private val db = DukanDatabase.getDatabase(application)
    val repository = DukanRepository(db)

    // Global UI state
    val currentScreen = MutableStateFlow(AppScreen.DASHBOARD)
    val isUrdu = MutableStateFlow(false)
    val isDarkMode = MutableStateFlow(false)
    val toastMessage = MutableStateFlow<String?>(null)

    // Current active logged-in user
    val currentUser = MutableStateFlow(
        UserEntity(
            id = 1,
            username = "owner",
            name = "Muhammad Aslam",
            pin = "1234",
            role = UserRole.OWNER,
            phone = "0300-1234567"
        )
    )

    // Cloud sync simulation state
    val isSyncing = MutableStateFlow(false)
    val lastSyncTime = MutableStateFlow(System.currentTimeMillis())

    // Database reactive streams
    val products = repository.products.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val lowStockProducts = repository.lowStockProducts.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val outOfStockProducts = repository.outOfStockProducts.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val categories = repository.categories.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val customers = repository.customers.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val suppliers = repository.suppliers.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sales = repository.sales.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val purchases = repository.purchases.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val expenses = repository.expenses.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val cashAccounts = repository.cashAccounts.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val cashTransactions = repository.cashTransactions.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val saleReturns = repository.saleReturns.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val settings = repository.settings.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val auditLogs = repository.auditLogs.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val users = repository.users.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Dashboard Filter
    val dateFilter = MutableStateFlow(DateFilterPeriod.TODAY)

    // POS Cart State
    val cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val selectedCustomer = MutableStateFlow<CustomerEntity?>(null)
    val overallDiscount = MutableStateFlow(0.0)
    val taxPercent = MutableStateFlow(0.0)
    val posNotes = MutableStateFlow("")
    val posSearchQuery = MutableStateFlow("")
    val selectedCategory = MutableStateFlow<String?>(null)

    // Held bills
    val heldBills = MutableStateFlow<List<HeldBill>>(emptyList())

    // Active Receipt for Dialog
    val activeReceiptSale = MutableStateFlow<SaleEntity?>(null)
    val activeReceiptItems = MutableStateFlow<List<SaleItemEntity>>(emptyList())

    init {
        viewModelScope.launch {
            InitialDataSeeder.seedDatabaseIfEmpty(db)
            settings.collect { s ->
                if (s != null) {
                    isUrdu.value = (s.language == AppLanguage.URDU)
                    isDarkMode.value = s.isDarkMode
                }
            }
        }
    }

    fun showToast(msg: String) {
        toastMessage.value = msg
    }

    fun clearToast() {
        toastMessage.value = null
    }

    fun navigateTo(screen: AppScreen) {
        currentScreen.value = screen
    }

    fun toggleLanguage() {
        val newVal = !isUrdu.value
        isUrdu.value = newVal
        viewModelScope.launch {
            val s = settings.value
            if (s != null) {
                repository.updateSettings(
                    s.copy(language = if (newVal) AppLanguage.URDU else AppLanguage.ENGLISH)
                )
            }
        }
    }

    fun toggleDarkMode() {
        val newVal = !isDarkMode.value
        isDarkMode.value = newVal
        viewModelScope.launch {
            val s = settings.value
            if (s != null) {
                repository.updateSettings(s.copy(isDarkMode = newVal))
            }
        }
    }

    fun switchUser(user: UserEntity) {
        currentUser.value = user
        showToast("Switched active user to ${user.name} (${user.role.name})")
    }

    fun triggerCloudSync() {
        viewModelScope.launch {
            isSyncing.value = true
            kotlinx.coroutines.delay(1200)
            lastSyncTime.value = System.currentTimeMillis()
            isSyncing.value = false
            showToast(if (isUrdu.value) "تمام ریکارڈ کلاؤڈ پر محفوظ ہو گیا ہے" else "All data successfully synced to cloud!")
        }
    }

    // --- POS Cart Operations ---
    fun addToCart(product: ProductEntity) {
        val current = cartItems.value.toMutableList()
        val index = current.indexOfFirst { it.product.id == product.id }
        if (index >= 0) {
            val existing = current[index]
            current[index] = existing.copy(quantity = existing.quantity + 1.0)
        } else {
            current.add(CartItem(product = product, quantity = 1.0, unitPrice = product.salePrice))
        }
        cartItems.value = current
    }

    fun scanBarcode(barcode: String) {
        viewModelScope.launch {
            val p = repository.getProductByBarcode(barcode.trim())
            if (p != null) {
                addToCart(p)
                showToast("Added: ${p.name}")
            } else {
                showToast("Product not found with barcode: $barcode")
            }
        }
    }

    fun updateCartItemQuantity(productId: Long, newQty: Double) {
        if (newQty <= 0.0) {
            removeFromCart(productId)
            return
        }
        cartItems.value = cartItems.value.map {
            if (it.product.id == productId) it.copy(quantity = newQty) else it
        }
    }

    fun updateCartItemPrice(productId: Long, newPrice: Double) {
        cartItems.value = cartItems.value.map {
            if (it.product.id == productId) it.copy(unitPrice = newPrice) else it
        }
    }

    fun updateCartItemDiscount(productId: Long, itemDiscount: Double) {
        cartItems.value = cartItems.value.map {
            if (it.product.id == productId) it.copy(itemDiscount = itemDiscount) else it
        }
    }

    fun removeFromCart(productId: Long) {
        cartItems.value = cartItems.value.filter { it.product.id != productId }
    }

    fun clearCart() {
        cartItems.value = emptyList()
        selectedCustomer.value = null
        overallDiscount.value = 0.0
        taxPercent.value = 0.0
        posNotes.value = ""
    }

    fun holdBill() {
        if (cartItems.value.isEmpty()) {
            showToast("Cart is empty to hold.")
            return
        }
        val held = HeldBill(
            id = "HOLD-${System.currentTimeMillis() % 10000}",
            items = cartItems.value,
            customer = selectedCustomer.value,
            overallDiscount = overallDiscount.value,
            taxPercent = taxPercent.value
        )
        heldBills.value = heldBills.value + held
        clearCart()
        showToast("Bill held safely. You can recall it anytime.")
    }

    fun recallHeldBill(held: HeldBill) {
        cartItems.value = held.items
        selectedCustomer.value = held.customer
        overallDiscount.value = held.overallDiscount
        taxPercent.value = held.taxPercent
        heldBills.value = heldBills.value.filter { it.id != held.id }
        showToast("Held bill restored to cart.")
    }

    fun checkoutSale(splits: List<PaymentSplitItem>, onComplete: (SaleEntity) -> Unit) {
        if (cartItems.value.isEmpty()) {
            showToast("Cart is empty.")
            return
        }
        viewModelScope.launch {
            try {
                val created = repository.completeSale(
                    items = cartItems.value,
                    customer = selectedCustomer.value,
                    overallDiscount = overallDiscount.value,
                    taxPercent = taxPercent.value,
                    splits = splits,
                    notes = posNotes.value,
                    currentUser = currentUser.value
                )
                val itemsSync = repository.getSaleItemsSync(created.id)
                activeReceiptSale.value = created
                activeReceiptItems.value = itemsSync
                clearCart()
                showToast("Sale ${created.invoiceNumber} completed successfully!")
                onComplete(created)
            } catch (e: Exception) {
                showToast("Error completing sale: ${e.message}")
            }
        }
    }

    fun viewReceipt(sale: SaleEntity) {
        viewModelScope.launch {
            val items = repository.getSaleItemsSync(sale.id)
            activeReceiptSale.value = sale
            activeReceiptItems.value = items
        }
    }

    fun closeReceipt() {
        activeReceiptSale.value = null
        activeReceiptItems.value = emptyList()
    }

    // --- Customer Operations ---
    fun addCustomer(customer: CustomerEntity) {
        viewModelScope.launch {
            repository.insertCustomer(customer)
            showToast("Customer ${customer.name} added successfully.")
        }
    }

    fun updateCustomer(customer: CustomerEntity) {
        viewModelScope.launch {
            repository.updateCustomer(customer)
            showToast("Customer updated.")
        }
    }

    fun receiveCustomerPayment(
        customer: CustomerEntity,
        amount: Double,
        method: PaymentMethod,
        refNo: String,
        notes: String
    ) {
        viewModelScope.launch {
            repository.receiveCustomerPayment(
                customer = customer,
                amount = amount,
                method = method,
                refNumber = refNo,
                notes = notes,
                currentUser = currentUser.value
            )
            showToast("Payment of Rs. $amount received from ${customer.name}")
        }
    }

    // --- Product Operations ---
    fun addProduct(product: ProductEntity) {
        viewModelScope.launch {
            repository.insertProduct(product)
            showToast("Product ${product.name} saved.")
        }
    }

    fun updateProduct(product: ProductEntity) {
        viewModelScope.launch {
            repository.updateProduct(product)
            showToast("Product updated.")
        }
    }

    fun deleteProduct(id: Long) {
        viewModelScope.launch {
            repository.softDeleteProduct(id)
            showToast("Product archived.")
        }
    }

    fun adjustStock(product: ProductEntity, newStock: Double, isDamaged: Boolean, reason: String) {
        viewModelScope.launch {
            repository.adjustStock(product, newStock, isDamaged, reason, currentUser.value)
            showToast("Stock updated for ${product.name} to $newStock")
        }
    }

    // --- Supplier Operations ---
    fun addSupplier(supplier: SupplierEntity) {
        viewModelScope.launch {
            repository.insertSupplier(supplier)
            showToast("Supplier ${supplier.name} added.")
        }
    }

    fun paySupplier(
        supplier: SupplierEntity,
        amount: Double,
        method: PaymentMethod,
        refNo: String,
        notes: String
    ) {
        viewModelScope.launch {
            repository.paySupplier(
                supplier = supplier,
                amount = amount,
                method = method,
                refNumber = refNo,
                notes = notes,
                currentUser = currentUser.value
            )
            showToast("Paid Rs. $amount to ${supplier.name}")
        }
    }

    // --- Purchase Operations ---
    fun createPurchase(
        supplier: SupplierEntity,
        items: List<Triple<ProductEntity, Double, Double>>,
        paidAmount: Double,
        method: PaymentMethod,
        notes: String
    ) {
        viewModelScope.launch {
            repository.completePurchase(supplier, items, paidAmount, method, notes, currentUser.value)
            showToast("Purchase order completed and inventory updated!")
        }
    }

    // --- Expense Operations ---
    fun addExpense(
        title: String,
        category: String,
        amount: Double,
        method: PaymentMethod,
        notes: String
    ) {
        viewModelScope.launch {
            repository.recordExpense(title, category, amount, method, notes, currentUser.value)
            showToast("Expense Rs. $amount recorded.")
        }
    }

    // --- Cash Transfer Operations ---
    fun transferCash(fromId: Long, toId: Long, amount: Double, notes: String) {
        viewModelScope.launch {
            repository.transferFunds(fromId, toId, amount, notes, currentUser.value)
            showToast("Transferred Rs. $amount successfully.")
        }
    }

    // --- Sale Return Operations ---
    fun processSaleReturn(
        sale: SaleEntity,
        returnedItems: List<Pair<SaleItemEntity, Double>>,
        refundMethod: PaymentMethod,
        reason: String,
        notes: String
    ) {
        viewModelScope.launch {
            repository.processSaleReturn(sale, returnedItems, refundMethod, reason, notes, currentUser.value)
            showToast("Sale Return processed and stock updated.")
        }
    }

    // --- Duplicate Invoice to Cart ---
    fun duplicateInvoiceToCart(sale: SaleEntity) {
        viewModelScope.launch {
            val items = repository.getSaleItemsSync(sale.id)
            val currentProducts = products.value.associateBy { it.id }
            val newCart = items.mapNotNull { item ->
                val p = currentProducts[item.productId]
                p?.let {
                    CartItem(
                        product = it,
                        quantity = item.quantity,
                        unitPrice = item.unitPrice,
                        itemDiscount = item.discount
                    )
                }
            }
            cartItems.value = newCart
            overallDiscount.value = sale.discount
            taxPercent.value = if (sale.subtotal > 0) (sale.tax / (sale.subtotal - sale.discount)) * 100 else 0.0
            posNotes.value = "Duplicate of ${sale.invoiceNumber}"
            currentScreen.value = AppScreen.POS
            showToast("Loaded items from ${sale.invoiceNumber} into POS cart!")
        }
    }

    // --- Filtered Sales for Dashboard ---
    fun getFilteredSales(allSales: List<SaleEntity>, filter: DateFilterPeriod): List<SaleEntity> {
        val cal = Calendar.getInstance()
        val now = cal.timeInMillis

        return when (filter) {
            DateFilterPeriod.TODAY -> {
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                val start = cal.timeInMillis
                allSales.filter { it.timestamp >= start }
            }
            DateFilterPeriod.YESTERDAY -> {
                cal.add(Calendar.DAY_OF_YEAR, -1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                val start = cal.timeInMillis
                cal.set(Calendar.HOUR_OF_DAY, 23)
                cal.set(Calendar.MINUTE, 59)
                cal.set(Calendar.SECOND, 59)
                val end = cal.timeInMillis
                allSales.filter { it.timestamp in start..end }
            }
            DateFilterPeriod.THIS_WEEK -> {
                cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                val start = cal.timeInMillis
                allSales.filter { it.timestamp >= start }
            }
            DateFilterPeriod.THIS_MONTH -> {
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                val start = cal.timeInMillis
                allSales.filter { it.timestamp >= start }
            }
            DateFilterPeriod.ALL_TIME -> allSales
        }
    }
}
