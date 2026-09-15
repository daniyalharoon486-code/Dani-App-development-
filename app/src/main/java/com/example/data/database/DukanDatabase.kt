package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.data.dao.*
import com.example.data.model.*

@Database(
    entities = [
        UserEntity::class,
        ProductEntity::class,
        CustomerEntity::class,
        CustomerLedgerEntryEntity::class,
        SupplierEntity::class,
        SupplierLedgerEntryEntity::class,
        SaleEntity::class,
        SaleItemEntity::class,
        PaymentEntity::class,
        PurchaseEntity::class,
        PurchaseItemEntity::class,
        ExpenseEntity::class,
        CashAccountEntity::class,
        CashTransactionEntity::class,
        SaleReturnEntity::class,
        SaleReturnItemEntity::class,
        InventoryMovementEntity::class,
        ShopSettingsEntity::class,
        AuditLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class DukanDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun customerDao(): CustomerDao
    abstract fun supplierDao(): SupplierDao
    abstract fun saleDao(): SaleDao
    abstract fun purchaseDao(): PurchaseDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun cashAccountDao(): CashAccountDao
    abstract fun paymentDao(): PaymentDao
    abstract fun saleReturnDao(): SaleReturnDao
    abstract fun inventoryMovementDao(): InventoryMovementDao
    abstract fun shopSettingsDao(): ShopSettingsDao
    abstract fun auditLogDao(): AuditLogDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: DukanDatabase? = null

        fun getDatabase(context: Context): DukanDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DukanDatabase::class.java,
                    "dukan_pos_database.db"
                ).fallbackToDestructiveMigration()
                 .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
