package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "users",
    indices = [Index(value = ["username"], unique = true)]
)
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val username: String,
    val name: String,
    val pin: String,
    val role: UserRole,
    val permissionsJson: String = "",
    val phone: String = "",
    val isActive: Boolean = true
)

@Entity(
    tableName = "products",
    indices = [Index(value = ["barcode"]), Index(value = ["sku"]), Index(value = ["category"])]
)
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val nameUrdu: String = "",
    val sku: String = "",
    val barcode: String = "",
    val category: String = "General",
    val brand: String = "",
    val purchasePrice: Double = 0.0,
    val salePrice: Double = 0.0,
    val wholesalePrice: Double = 0.0,
    val currentStock: Double = 0.0,
    val minStock: Double = 5.0,
    val unit: String = "Pcs",
    val supplierId: Long? = null,
    val taxPercent: Double = 0.0,
    val imageRes: String = "",
    val description: String = "",
    val variationsJson: String = "",
    val isArchived: Boolean = false
)

@Entity(
    tableName = "customers",
    indices = [Index(value = ["phone"])]
)
data class CustomerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String = "",
    val address: String = "",
    val email: String = "",
    val openingBalance: Double = 0.0,
    val currentBalance: Double = 0.0,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "customer_ledger",
    indices = [Index(value = ["customerId"]), Index(value = ["date"])]
)
data class CustomerLedgerEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val customerId: Long,
    val date: Long = System.currentTimeMillis(),
    val type: LedgerEntryType,
    val referenceNumber: String = "",
    val debit: Double = 0.0,  // Increases customer's debt to shop
    val credit: Double = 0.0, // Payment made by customer, reduces debt
    val runningBalance: Double = 0.0,
    val notes: String = ""
)

@Entity(
    tableName = "suppliers",
    indices = [Index(value = ["phone"])]
)
data class SupplierEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String = "",
    val address: String = "",
    val company: String = "",
    val openingBalance: Double = 0.0,
    val currentBalance: Double = 0.0, // Amount we owe to supplier
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "supplier_ledger",
    indices = [Index(value = ["supplierId"]), Index(value = ["date"])]
)
data class SupplierLedgerEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val supplierId: Long,
    val date: Long = System.currentTimeMillis(),
    val type: LedgerEntryType,
    val referenceNumber: String = "",
    val debit: Double = 0.0,  // Payment paid to supplier (reduces payable)
    val credit: Double = 0.0, // Stock purchase from supplier (increases payable)
    val runningBalance: Double = 0.0,
    val notes: String = ""
)

@Entity(
    tableName = "sales",
    indices = [Index(value = ["invoiceNumber"], unique = true), Index(value = ["customerId"]), Index(value = ["timestamp"])]
)
data class SaleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val invoiceNumber: String,
    val customerId: Long? = null,
    val customerName: String = "Walk-in Cash Customer",
    val customerPhone: String = "",
    val subtotal: Double = 0.0,
    val discount: Double = 0.0,
    val tax: Double = 0.0,
    val grandTotal: Double = 0.0,
    val paidAmount: Double = 0.0,
    val remainingAmount: Double = 0.0,
    val paymentMethodSummary: String = "CASH",
    val paymentStatus: PaymentStatus = PaymentStatus.PAID,
    val totalCost: Double = 0.0, // For profit calculation: grandTotal - totalCost - discount
    val notes: String = "",
    val cashierName: String = "Owner",
    val cashierId: Long = 1,
    val timestamp: Long = System.currentTimeMillis(),
    val isReturned: Boolean = false
)

@Entity(
    tableName = "sale_items",
    indices = [Index(value = ["saleId"]), Index(value = ["productId"])]
)
data class SaleItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val saleId: Long,
    val productId: Long,
    val productName: String,
    val quantity: Double,
    val unitPrice: Double,
    val costPrice: Double = 0.0,
    val discount: Double = 0.0,
    val total: Double
)

@Entity(
    tableName = "payments",
    indices = [Index(value = ["saleId"]), Index(value = ["customerId"]), Index(value = ["date"])]
)
data class PaymentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val saleId: Long? = null,
    val customerId: Long? = null,
    val supplierId: Long? = null,
    val type: String, // SALE, RECOVERY, SUPPLIER_PAYMENT, EXPENSE
    val paymentMethod: PaymentMethod,
    val amount: Double,
    val date: Long = System.currentTimeMillis(),
    val referenceNumber: String = "",
    val notes: String = ""
)

@Entity(
    tableName = "purchases",
    indices = [Index(value = ["purchaseNumber"], unique = true), Index(value = ["supplierId"])]
)
data class PurchaseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val purchaseNumber: String,
    val supplierId: Long,
    val supplierName: String,
    val subtotal: Double = 0.0,
    val discount: Double = 0.0,
    val tax: Double = 0.0,
    val grandTotal: Double = 0.0,
    val paidAmount: Double = 0.0,
    val remainingAmount: Double = 0.0,
    val paymentMethod: PaymentMethod = PaymentMethod.CASH,
    val timestamp: Long = System.currentTimeMillis(),
    val notes: String = ""
)

@Entity(
    tableName = "purchase_items",
    indices = [Index(value = ["purchaseId"]), Index(value = ["productId"])]
)
data class PurchaseItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val purchaseId: Long,
    val productId: Long,
    val productName: String,
    val quantity: Double,
    val unitCost: Double,
    val total: Double
)

@Entity(
    tableName = "expenses",
    indices = [Index(value = ["category"]), Index(value = ["date"])]
)
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val category: String, // Rent, Electricity, Salary, Transport, Maintenance, Internet, Purchases, Other
    val amount: Double,
    val paymentAccount: String = "CASH",
    val date: Long = System.currentTimeMillis(),
    val notes: String = ""
)

@Entity(
    tableName = "cash_accounts"
)
data class CashAccountEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val nameUrdu: String,
    val accountType: PaymentMethod,
    val openingBalance: Double = 0.0,
    val currentBalance: Double = 0.0,
    val accountNumber: String = ""
)

@Entity(
    tableName = "cash_transactions",
    indices = [Index(value = ["accountId"]), Index(value = ["timestamp"])]
)
data class CashTransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val accountId: Long,
    val type: String, // IN, OUT, TRANSFER_IN, TRANSFER_OUT
    val amount: Double,
    val sourceOrDestination: String = "",
    val referenceType: String = "", // SALE, RECOVERY, EXPENSE, PURCHASE, TRANSFER
    val referenceId: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val notes: String = ""
)

@Entity(
    tableName = "sale_returns",
    indices = [Index(value = ["originalInvoiceNumber"])]
)
data class SaleReturnEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val returnNumber: String,
    val originalInvoiceNumber: String,
    val customerId: Long? = null,
    val customerName: String = "",
    val returnDate: Long = System.currentTimeMillis(),
    val totalRefundAmount: Double = 0.0,
    val refundMethod: PaymentMethod = PaymentMethod.CASH,
    val returnReason: String = "",
    val notes: String = ""
)

@Entity(
    tableName = "sale_return_items",
    indices = [Index(value = ["returnId"]), Index(value = ["productId"])]
)
data class SaleReturnItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val returnId: Long,
    val productId: Long,
    val productName: String,
    val quantity: Double,
    val refundUnitPrice: Double,
    val totalRefund: Double
)

@Entity(
    tableName = "inventory_movements",
    indices = [Index(value = ["productId"]), Index(value = ["timestamp"])]
)
data class InventoryMovementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val productId: Long,
    val type: StockMovementType,
    val quantityChange: Double, // positive or negative
    val remainingStockAfter: Double,
    val timestamp: Long = System.currentTimeMillis(),
    val notes: String = ""
)

@Entity(
    tableName = "shop_settings"
)
data class ShopSettingsEntity(
    @PrimaryKey val id: Long = 1,
    val shopName: String = "Al-Madina General Store",
    val shopNameUrdu: String = "المدینہ جنرل اسٹور",
    val address: String = "Shop # 14, Main Commercial Market, Lahore",
    val phone: String = "0300-1234567",
    val email: String = "almadina.store@gmail.com",
    val currencySymbol: String = "Rs.",
    val taxPercent: Double = 0.0,
    val receiptSize: ReceiptSize = ReceiptSize.THERMAL_80MM,
    val receiptFooter: String = "Thank you for your business! Please visit again.",
    val invoicePrefix: String = "INV-",
    val language: AppLanguage = AppLanguage.ENGLISH,
    val isDarkMode: Boolean = false,
    val autoCloudSync: Boolean = true,
    val lastSyncTimestamp: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "audit_logs",
    indices = [Index(value = ["timestamp"])]
)
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long = 1,
    val userName: String = "Owner",
    val action: String,
    val details: String,
    val timestamp: Long = System.currentTimeMillis()
)
