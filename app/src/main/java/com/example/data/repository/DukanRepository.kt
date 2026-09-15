package com.example.data.repository

import androidx.room.withTransaction
import com.example.data.database.DukanDatabase
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import org.json.JSONArray
import org.json.JSONObject

data class CartItem(
    val product: ProductEntity,
    val quantity: Double,
    val unitPrice: Double,
    val itemDiscount: Double = 0.0
) {
    val total: Double get() = (quantity * unitPrice) - itemDiscount
    val totalCost: Double get() = quantity * product.purchasePrice
}

data class PaymentSplitItem(
    val method: PaymentMethod,
    val amount: Double
)

class DukanRepository(private val db: DukanDatabase) {

    // Reactive Flows
    val products: Flow<List<ProductEntity>> = db.productDao().getAllProducts()
    val lowStockProducts: Flow<List<ProductEntity>> = db.productDao().getLowStockProducts()
    val outOfStockProducts: Flow<List<ProductEntity>> = db.productDao().getOutOfStockProducts()
    val categories: Flow<List<String>> = db.productDao().getAllCategories()

    val customers: Flow<List<CustomerEntity>> = db.customerDao().getAllCustomers()
    val customersWithDue: Flow<List<CustomerEntity>> = db.customerDao().getCustomersWithOutstanding()

    val suppliers: Flow<List<SupplierEntity>> = db.supplierDao().getAllSuppliers()

    val sales: Flow<List<SaleEntity>> = db.saleDao().getAllSales()
    val purchases: Flow<List<PurchaseEntity>> = db.purchaseDao().getAllPurchases()
    val expenses: Flow<List<ExpenseEntity>> = db.expenseDao().getAllExpenses()
    val cashAccounts: Flow<List<CashAccountEntity>> = db.cashAccountDao().getAllAccounts()
    val cashTransactions: Flow<List<CashTransactionEntity>> = db.cashAccountDao().getAllTransactions()
    val saleReturns: Flow<List<SaleReturnEntity>> = db.saleReturnDao().getAllReturns()
    val settings: Flow<ShopSettingsEntity?> = db.shopSettingsDao().getSettings()
    val auditLogs: Flow<List<AuditLogEntity>> = db.auditLogDao().getRecentLogs()
    val users: Flow<List<UserEntity>> = db.userDao().getAllUsers()

    fun searchProducts(query: String) = db.productDao().searchProducts(query)
    suspend fun getProductByBarcode(barcode: String) = db.productDao().getProductByBarcode(barcode)
    fun getSaleItems(saleId: Long) = db.saleDao().getItemsForSale(saleId)
    fun getCustomerLedger(customerId: Long) = db.customerDao().getLedgerForCustomer(customerId)
    fun getSupplierLedger(supplierId: Long) = db.supplierDao().getLedgerForSupplier(supplierId)
    fun getPurchasedItems(purchaseId: Long) = db.purchaseDao().getItemsForPurchase(purchaseId)

    suspend fun findSaleByInvoice(invoiceNumber: String): SaleEntity? {
        return db.saleDao().getSaleByInvoiceNumber(invoiceNumber.trim())
    }

    suspend fun getSaleItemsSync(saleId: Long): List<SaleItemEntity> {
        return db.saleDao().getItemsForSaleSync(saleId)
    }

    // 1. Complete Sale Transaction
    suspend fun completeSale(
        items: List<CartItem>,
        customer: CustomerEntity?,
        overallDiscount: Double,
        taxPercent: Double,
        splits: List<PaymentSplitItem>,
        notes: String,
        currentUser: UserEntity
    ): SaleEntity = db.withTransaction {
        val currentSettings = db.shopSettingsDao().getSettingsSync()
        val count = db.saleDao().getSaleCount()
        val prefix = currentSettings?.invoicePrefix ?: "INV-"
        val invoiceNo = "$prefix${1001 + count}"

        val subtotal = items.sumOf { it.total }
        val taxAmount = if (taxPercent > 0) (subtotal - overallDiscount) * (taxPercent / 100.0) else 0.0
        val grandTotal = (subtotal - overallDiscount + taxAmount).coerceAtLeast(0.0)
        val totalCost = items.sumOf { it.totalCost }

        val paidTotal = splits.filter { it.method != PaymentMethod.CUSTOMER_CREDIT }.sumOf { it.amount }
        val creditAmount = splits.find { it.method == PaymentMethod.CUSTOMER_CREDIT }?.amount ?: (grandTotal - paidTotal).coerceAtLeast(0.0)
        val remainingAmount = (grandTotal - paidTotal).coerceAtLeast(0.0)

        val paymentStatus = when {
            remainingAmount <= 0.0 -> PaymentStatus.PAID
            paidTotal > 0.0 -> PaymentStatus.PARTIAL
            else -> PaymentStatus.UNPAID
        }

        val methodSummary = if (splits.size == 1) {
            splits.first().method.title
        } else {
            splits.joinToString(" + ") { "${it.method.name}: Rs. ${it.amount.toInt()}" }
        }

        val customerName = customer?.name ?: "Walk-in Cash Customer"
        val customerPhone = customer?.phone ?: ""

        val sale = SaleEntity(
            invoiceNumber = invoiceNo,
            customerId = customer?.id,
            customerName = customerName,
            customerPhone = customerPhone,
            subtotal = subtotal,
            discount = overallDiscount,
            tax = taxAmount,
            grandTotal = grandTotal,
            paidAmount = paidTotal,
            remainingAmount = remainingAmount,
            paymentMethodSummary = methodSummary,
            paymentStatus = paymentStatus,
            totalCost = totalCost,
            notes = notes,
            cashierName = currentUser.name,
            cashierId = currentUser.id,
            timestamp = System.currentTimeMillis()
        )

        val saleId = db.saleDao().insertSale(sale)

        // Insert Sale Items & update inventory
        val saleItemEntities = items.map { cartItem ->
            val p = cartItem.product
            val newStock = (p.currentStock - cartItem.quantity).coerceAtLeast(0.0)
            db.productDao().updateStock(p.id, -cartItem.quantity)

            db.inventoryMovementDao().insertMovement(
                InventoryMovementEntity(
                    productId = p.id,
                    type = StockMovementType.SALE,
                    quantityChange = -cartItem.quantity,
                    remainingStockAfter = newStock,
                    notes = "Sale #$invoiceNo"
                )
            )

            SaleItemEntity(
                saleId = saleId,
                productId = p.id,
                productName = p.name,
                quantity = cartItem.quantity,
                unitPrice = cartItem.unitPrice,
                costPrice = p.purchasePrice,
                discount = cartItem.itemDiscount,
                total = cartItem.total
            )
        }
        db.saleDao().insertSaleItems(saleItemEntities)

        // Process payments and accounts
        for (split in splits) {
            if (split.amount <= 0.0) continue
            if (split.method != PaymentMethod.CUSTOMER_CREDIT) {
                // Find matching cash account
                val account = db.cashAccountDao().getAccountByType(split.method)
                if (account != null) {
                    db.cashAccountDao().updateBalance(account.id, split.amount)
                    db.cashAccountDao().insertTransaction(
                        CashTransactionEntity(
                            accountId = account.id,
                            type = "IN",
                            amount = split.amount,
                            sourceOrDestination = customerName,
                            referenceType = "SALE",
                            referenceId = invoiceNo,
                            notes = "Sale payment via ${split.method.title}"
                        )
                    )
                }
                // Record Payment entity
                db.paymentDao().insertPayment(
                    PaymentEntity(
                        saleId = saleId,
                        customerId = customer?.id,
                        type = "SALE",
                        paymentMethod = split.method,
                        amount = split.amount,
                        referenceNumber = invoiceNo,
                        notes = "Payment for $invoiceNo"
                    )
                )
            }
        }

        // Customer Ledger if customer selected or credit
        if (customer != null) {
            val oldBalance = customer.currentBalance
            val newBalance = oldBalance + remainingAmount
            db.customerDao().updateCustomerBalance(customer.id, remainingAmount)

            // Ledger entry for total bill
            db.customerDao().insertLedgerEntry(
                CustomerLedgerEntryEntity(
                    customerId = customer.id,
                    type = LedgerEntryType.SALE,
                    referenceNumber = invoiceNo,
                    debit = grandTotal,
                    credit = paidTotal,
                    runningBalance = newBalance,
                    notes = "Invoice $invoiceNo (Total: $grandTotal, Paid: $paidTotal)"
                )
            )
        }

        // Audit Log
        db.auditLogDao().insertLog(
            AuditLogEntity(
                userId = currentUser.id,
                userName = currentUser.name,
                action = "CREATE_SALE",
                details = "Created $invoiceNo for $customerName. Total: Rs. $grandTotal, Paid: Rs. $paidTotal, Balance: Rs. $remainingAmount"
            )
        )

        sale.copy(id = saleId)
    }

    // 2. Receive Customer Payment
    suspend fun receiveCustomerPayment(
        customer: CustomerEntity,
        amount: Double,
        method: PaymentMethod,
        refNumber: String,
        notes: String,
        currentUser: UserEntity
    ) = db.withTransaction {
        if (amount <= 0.0) return@withTransaction

        val oldBalance = customer.currentBalance
        val newBalance = oldBalance - amount
        db.customerDao().updateCustomerBalance(customer.id, -amount)

        // Ledger Entry
        db.customerDao().insertLedgerEntry(
            CustomerLedgerEntryEntity(
                customerId = customer.id,
                type = LedgerEntryType.PAYMENT_RECEIVED,
                referenceNumber = refNumber.ifEmpty { "REC-${System.currentTimeMillis() % 10000}" },
                debit = 0.0,
                credit = amount,
                runningBalance = newBalance,
                notes = "$notes (via ${method.title})"
            )
        )

        // Account balance increase
        val account = db.cashAccountDao().getAccountByType(method)
        if (account != null) {
            db.cashAccountDao().updateBalance(account.id, amount)
            db.cashAccountDao().insertTransaction(
                CashTransactionEntity(
                    accountId = account.id,
                    type = "IN",
                    amount = amount,
                    sourceOrDestination = customer.name,
                    referenceType = "RECOVERY",
                    referenceId = refNumber,
                    notes = "Recovery from ${customer.name} via ${method.title}"
                )
            )
        }

        // Payment record
        db.paymentDao().insertPayment(
            PaymentEntity(
                customerId = customer.id,
                type = "RECOVERY",
                paymentMethod = method,
                amount = amount,
                referenceNumber = refNumber,
                notes = notes
            )
        )

        // Audit Log
        db.auditLogDao().insertLog(
            AuditLogEntity(
                userId = currentUser.id,
                userName = currentUser.name,
                action = "RECEIVE_PAYMENT",
                details = "Received Rs. $amount from ${customer.name} via ${method.title}. New Balance: Rs. $newBalance"
            )
        )
    }

    // 3. Process Sale Return
    suspend fun processSaleReturn(
        sale: SaleEntity,
        returnedItems: List<Pair<SaleItemEntity, Double>>, // item to return quantity
        refundMethod: PaymentMethod,
        reason: String,
        notes: String,
        currentUser: UserEntity
    ) = db.withTransaction {
        val returnNo = "RET-${System.currentTimeMillis() % 100000}"
        var totalRefund = 0.0

        val returnItemEntities = mutableListOf<SaleReturnItemEntity>()

        for ((item, qty) in returnedItems) {
            if (qty <= 0) continue
            val refundPrice = item.unitPrice
            val itemRefund = qty * refundPrice
            totalRefund += itemRefund

            // Update inventory
            db.productDao().updateStock(item.productId, qty)
            val p = db.productDao().getProductById(item.productId)
            db.inventoryMovementDao().insertMovement(
                InventoryMovementEntity(
                    productId = item.productId,
                    type = StockMovementType.SALE_RETURN,
                    quantityChange = qty,
                    remainingStockAfter = p?.currentStock ?: qty,
                    notes = "Return $returnNo from ${sale.invoiceNumber}"
                )
            )

            returnItemEntities.add(
                SaleReturnItemEntity(
                    returnId = 0,
                    productId = item.productId,
                    productName = item.productName,
                    quantity = qty,
                    refundUnitPrice = refundPrice,
                    totalRefund = itemRefund
                )
            )
        }

        val returnEntity = SaleReturnEntity(
            returnNumber = returnNo,
            originalInvoiceNumber = sale.invoiceNumber,
            customerId = sale.customerId,
            customerName = sale.customerName,
            totalRefundAmount = totalRefund,
            refundMethod = refundMethod,
            returnReason = reason,
            notes = notes
        )
        val returnId = db.saleReturnDao().insertReturn(returnEntity)
        db.saleReturnDao().insertReturnItems(returnItemEntities.map { it.copy(returnId = returnId) })

        // Refund money or adjust customer balance
        if (refundMethod == PaymentMethod.CUSTOMER_CREDIT && sale.customerId != null) {
            db.customerDao().updateCustomerBalance(sale.customerId, -totalRefund)
            val c = db.customerDao().getCustomerById(sale.customerId)
            db.customerDao().insertLedgerEntry(
                CustomerLedgerEntryEntity(
                    customerId = sale.customerId,
                    type = LedgerEntryType.RETURN,
                    referenceNumber = returnNo,
                    debit = 0.0,
                    credit = totalRefund,
                    runningBalance = c?.currentBalance ?: 0.0,
                    notes = "Return against ${sale.invoiceNumber}"
                )
            )
        } else {
            val account = db.cashAccountDao().getAccountByType(refundMethod)
            if (account != null) {
                db.cashAccountDao().updateBalance(account.id, -totalRefund)
                db.cashAccountDao().insertTransaction(
                    CashTransactionEntity(
                        accountId = account.id,
                        type = "OUT",
                        amount = totalRefund,
                        sourceOrDestination = sale.customerName,
                        referenceType = "RETURN",
                        referenceId = returnNo,
                        notes = "Refund for ${sale.invoiceNumber}"
                    )
                )
            }
        }

        db.saleDao().updateSale(sale.copy(isReturned = true))

        db.auditLogDao().insertLog(
            AuditLogEntity(
                userId = currentUser.id,
                userName = currentUser.name,
                action = "PROCESS_RETURN",
                details = "Processed Return $returnNo for ${sale.invoiceNumber}. Refund: Rs. $totalRefund via ${refundMethod.title}"
            )
        )
    }

    // 4. Complete Purchase from Supplier
    suspend fun completePurchase(
        supplier: SupplierEntity,
        items: List<Triple<ProductEntity, Double, Double>>, // Product, Qty, UnitCost
        paidAmount: Double,
        paymentMethod: PaymentMethod,
        notes: String,
        currentUser: UserEntity
    ) = db.withTransaction {
        val count = db.purchaseDao().getPurchaseCount()
        val purchaseNo = "PO-${2001 + count}"
        val subtotal = items.sumOf { it.second * it.third }
        val grandTotal = subtotal
        val remaining = (grandTotal - paidAmount).coerceAtLeast(0.0)

        val purchase = PurchaseEntity(
            purchaseNumber = purchaseNo,
            supplierId = supplier.id,
            supplierName = supplier.name,
            subtotal = subtotal,
            grandTotal = grandTotal,
            paidAmount = paidAmount,
            remainingAmount = remaining,
            paymentMethod = paymentMethod,
            notes = notes
        )
        val purchaseId = db.purchaseDao().insertPurchase(purchase)

        val purchaseItems = items.map { (product, qty, cost) ->
            db.productDao().updateStock(product.id, qty)
            val updated = db.productDao().getProductById(product.id)
            db.inventoryMovementDao().insertMovement(
                InventoryMovementEntity(
                    productId = product.id,
                    type = StockMovementType.PURCHASE,
                    quantityChange = qty,
                    remainingStockAfter = updated?.currentStock ?: qty,
                    notes = "Purchase $purchaseNo"
                )
            )

            PurchaseItemEntity(
                purchaseId = purchaseId,
                productId = product.id,
                productName = product.name,
                quantity = qty,
                unitCost = cost,
                total = qty * cost
            )
        }
        db.purchaseDao().insertPurchaseItems(purchaseItems)

        // Update supplier balance
        if (remaining > 0 || paidAmount > 0) {
            val newBalance = supplier.currentBalance + remaining
            db.supplierDao().updateSupplierBalance(supplier.id, remaining)
            db.supplierDao().insertLedgerEntry(
                SupplierLedgerEntryEntity(
                    supplierId = supplier.id,
                    type = LedgerEntryType.PURCHASE,
                    referenceNumber = purchaseNo,
                    debit = paidAmount,
                    credit = grandTotal,
                    runningBalance = newBalance,
                    notes = "Stock Purchase $purchaseNo (Total: $grandTotal, Paid: $paidAmount)"
                )
            )
        }

        // Deduct paid amount from account
        if (paidAmount > 0) {
            val account = db.cashAccountDao().getAccountByType(paymentMethod)
            if (account != null) {
                db.cashAccountDao().updateBalance(account.id, -paidAmount)
                db.cashAccountDao().insertTransaction(
                    CashTransactionEntity(
                        accountId = account.id,
                        type = "OUT",
                        amount = paidAmount,
                        sourceOrDestination = supplier.name,
                        referenceType = "PURCHASE",
                        referenceId = purchaseNo,
                        notes = "Paid for Purchase $purchaseNo"
                    )
                )
            }
        }

        db.auditLogDao().insertLog(
            AuditLogEntity(
                userId = currentUser.id,
                userName = currentUser.name,
                action = "CREATE_PURCHASE",
                details = "Purchased stock $purchaseNo from ${supplier.name}. Total: Rs. $grandTotal, Paid: Rs. $paidAmount"
            )
        )
    }

    // 5. Pay Supplier
    suspend fun paySupplier(
        supplier: SupplierEntity,
        amount: Double,
        method: PaymentMethod,
        refNumber: String,
        notes: String,
        currentUser: UserEntity
    ) = db.withTransaction {
        if (amount <= 0.0) return@withTransaction

        val newBalance = supplier.currentBalance - amount
        db.supplierDao().updateSupplierBalance(supplier.id, -amount)

        db.supplierDao().insertLedgerEntry(
            SupplierLedgerEntryEntity(
                supplierId = supplier.id,
                type = LedgerEntryType.PAYMENT_MADE,
                referenceNumber = refNumber.ifEmpty { "SUP-PAY-${System.currentTimeMillis() % 10000}" },
                debit = amount,
                credit = 0.0,
                runningBalance = newBalance,
                notes = "$notes (via ${method.title})"
            )
        )

        val account = db.cashAccountDao().getAccountByType(method)
        if (account != null) {
            db.cashAccountDao().updateBalance(account.id, -amount)
            db.cashAccountDao().insertTransaction(
                CashTransactionEntity(
                    accountId = account.id,
                    type = "OUT",
                    amount = amount,
                    sourceOrDestination = supplier.name,
                    referenceType = "SUPPLIER_PAYMENT",
                    referenceId = refNumber,
                    notes = "Payment to ${supplier.name} via ${method.title}"
                )
            )
        }

        db.auditLogDao().insertLog(
            AuditLogEntity(
                userId = currentUser.id,
                userName = currentUser.name,
                action = "PAY_SUPPLIER",
                details = "Paid Rs. $amount to ${supplier.name} via ${method.title}. Remaining Payable: Rs. $newBalance"
            )
        )
    }

    // 6. Record Expense
    suspend fun recordExpense(
        title: String,
        category: String,
        amount: Double,
        paymentAccount: PaymentMethod,
        notes: String,
        currentUser: UserEntity
    ) = db.withTransaction {
        if (amount <= 0.0) return@withTransaction

        val expense = ExpenseEntity(
            title = title,
            category = category,
            amount = amount,
            paymentAccount = paymentAccount.name,
            notes = notes
        )
        val expId = db.expenseDao().insertExpense(expense)

        val account = db.cashAccountDao().getAccountByType(paymentAccount)
        if (account != null) {
            db.cashAccountDao().updateBalance(account.id, -amount)
            db.cashAccountDao().insertTransaction(
                CashTransactionEntity(
                    accountId = account.id,
                    type = "OUT",
                    amount = amount,
                    sourceOrDestination = category,
                    referenceType = "EXPENSE",
                    referenceId = "EXP-$expId",
                    notes = "$title: $notes"
                )
            )
        }

        db.auditLogDao().insertLog(
            AuditLogEntity(
                userId = currentUser.id,
                userName = currentUser.name,
                action = "ADD_EXPENSE",
                details = "Expense: $title ($category) Rs. $amount from ${paymentAccount.title}"
            )
        )
    }

    // 7. Transfer between accounts
    suspend fun transferFunds(
        fromAccountId: Long,
        toAccountId: Long,
        amount: Double,
        notes: String,
        currentUser: UserEntity
    ) = db.withTransaction {
        if (amount <= 0.0 || fromAccountId == toAccountId) return@withTransaction

        val from = db.cashAccountDao().getAccountById(fromAccountId) ?: return@withTransaction
        val to = db.cashAccountDao().getAccountById(toAccountId) ?: return@withTransaction

        db.cashAccountDao().updateBalance(fromAccountId, -amount)
        db.cashAccountDao().updateBalance(toAccountId, amount)

        val transferRef = "TRF-${System.currentTimeMillis() % 100000}"

        db.cashAccountDao().insertTransaction(
            CashTransactionEntity(
                accountId = fromAccountId,
                type = "TRANSFER_OUT",
                amount = amount,
                sourceOrDestination = to.name,
                referenceType = "TRANSFER",
                referenceId = transferRef,
                notes = "Transferred to ${to.name}: $notes"
            )
        )

        db.cashAccountDao().insertTransaction(
            CashTransactionEntity(
                accountId = toAccountId,
                type = "TRANSFER_IN",
                amount = amount,
                sourceOrDestination = from.name,
                referenceType = "TRANSFER",
                referenceId = transferRef,
                notes = "Received from ${from.name}: $notes"
            )
        )

        db.auditLogDao().insertLog(
            AuditLogEntity(
                userId = currentUser.id,
                userName = currentUser.name,
                action = "TRANSFER_FUNDS",
                details = "Transferred Rs. $amount from ${from.name} to ${to.name}"
            )
        )
    }

    // 8. Adjust Stock
    suspend fun adjustStock(
        product: ProductEntity,
        newStock: Double,
        isDamaged: Boolean,
        reason: String,
        currentUser: UserEntity
    ) = db.withTransaction {
        val delta = newStock - product.currentStock
        db.productDao().updateProduct(product.copy(currentStock = newStock))

        db.inventoryMovementDao().insertMovement(
            InventoryMovementEntity(
                productId = product.id,
                type = if (isDamaged) StockMovementType.DAMAGED_STOCK else StockMovementType.MANUAL_ADJUSTMENT,
                quantityChange = delta,
                remainingStockAfter = newStock,
                notes = reason.ifEmpty { "Manual stock count adjustment" }
            )
        )

        db.auditLogDao().insertLog(
            AuditLogEntity(
                userId = currentUser.id,
                userName = currentUser.name,
                action = "ADJUST_STOCK",
                details = "Adjusted ${product.name} stock from ${product.currentStock} to $newStock ($reason)"
            )
        )
    }

    // Standard CRUD
    suspend fun insertProduct(p: ProductEntity) = db.productDao().insertProduct(p)
    suspend fun updateProduct(p: ProductEntity) = db.productDao().updateProduct(p)
    suspend fun softDeleteProduct(id: Long) = db.productDao().softDelete(id)

    suspend fun insertCustomer(c: CustomerEntity): Long = db.withTransaction {
        val id = db.customerDao().insertCustomer(c)
        if (c.openingBalance > 0) {
            db.customerDao().insertLedgerEntry(
                CustomerLedgerEntryEntity(
                    customerId = id,
                    type = LedgerEntryType.OPENING,
                    referenceNumber = "OPN-$id",
                    debit = c.openingBalance,
                    credit = 0.0,
                    runningBalance = c.openingBalance,
                    notes = "Opening Khata Balance"
                )
            )
        }
        id
    }
    suspend fun updateCustomer(c: CustomerEntity) = db.customerDao().updateCustomer(c)

    suspend fun insertSupplier(s: SupplierEntity): Long = db.withTransaction {
        val id = db.supplierDao().insertSupplier(s)
        if (s.openingBalance > 0) {
            db.supplierDao().insertLedgerEntry(
                SupplierLedgerEntryEntity(
                    supplierId = id,
                    type = LedgerEntryType.OPENING,
                    referenceNumber = "OPN-$id",
                    debit = 0.0,
                    credit = s.openingBalance,
                    runningBalance = s.openingBalance,
                    notes = "Opening Supplier Balance"
                )
            )
        }
        id
    }
    suspend fun updateSupplier(s: SupplierEntity) = db.supplierDao().updateSupplier(s)

    suspend fun insertUser(u: UserEntity) = db.userDao().insertUser(u)
    suspend fun updateUser(u: UserEntity) = db.userDao().updateUser(u)

    suspend fun updateSettings(s: ShopSettingsEntity) = db.shopSettingsDao().insertOrUpdate(s)

    // Backup export JSON
    suspend fun exportDatabaseBackup(): String {
        val root = JSONObject()
        val productsList = db.productDao().getAllProducts().firstOrNull() ?: emptyList()
        val customersList = db.customerDao().getAllCustomers().firstOrNull() ?: emptyList()
        val salesList = db.saleDao().getAllSales().firstOrNull() ?: emptyList()

        root.put("version", "1.0")
        root.put("exportedAt", System.currentTimeMillis())
        root.put("productCount", productsList.size)
        root.put("customerCount", customersList.size)
        root.put("saleCount", salesList.size)

        val prodArr = JSONArray()
        productsList.forEach { p ->
            val obj = JSONObject()
            obj.put("name", p.name)
            obj.put("sku", p.sku)
            obj.put("barcode", p.barcode)
            obj.put("category", p.category)
            obj.put("purchasePrice", p.purchasePrice)
            obj.put("salePrice", p.salePrice)
            obj.put("currentStock", p.currentStock)
            prodArr.put(obj)
        }
        root.put("products", prodArr)

        val custArr = JSONArray()
        customersList.forEach { c ->
            val obj = JSONObject()
            obj.put("name", c.name)
            obj.put("phone", c.phone)
            obj.put("currentBalance", c.currentBalance)
            custArr.put(obj)
        }
        root.put("customers", custArr)

        return root.toString(2)
    }
}
