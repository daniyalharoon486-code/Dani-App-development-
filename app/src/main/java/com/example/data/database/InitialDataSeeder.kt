package com.example.data.database

import com.example.data.model.*

object InitialDataSeeder {
    suspend fun seedDatabaseIfEmpty(db: DukanDatabase) {
        val userCount = db.userDao().getUserByUsername("owner")
        if (userCount != null) return

        // 1. Settings
        db.shopSettingsDao().insertOrUpdate(
            ShopSettingsEntity(
                id = 1,
                shopName = "Bismillah Mart & Khata",
                shopNameUrdu = "بسم اللہ جنرل مارٹ و کھاتہ",
                address = "Shop # 12, Commercial Area, Main Market, Lahore",
                phone = "0300-1234567",
                email = "bismillah.mart@gmail.com",
                currencySymbol = "Rs.",
                taxPercent = 0.0,
                receiptSize = ReceiptSize.THERMAL_80MM,
                receiptFooter = "Thank you for your visit! جزاکم اللہ خیرا",
                invoicePrefix = "INV-",
                language = AppLanguage.ENGLISH,
                isDarkMode = false,
                autoCloudSync = true,
                lastSyncTimestamp = System.currentTimeMillis()
            )
        )

        // 2. Users
        val users = listOf(
            UserEntity(
                username = "owner",
                name = "Muhammad Aslam (Owner)",
                pin = "1234",
                role = UserRole.OWNER,
                phone = "0300-1234567"
            ),
            UserEntity(
                username = "manager",
                name = "Hamza Khan (Manager)",
                pin = "2222",
                role = UserRole.MANAGER,
                phone = "0301-7654321"
            ),
            UserEntity(
                username = "cashier",
                name = "Bilal Tariq (Cashier)",
                pin = "0000",
                role = UserRole.CASHIER,
                phone = "0322-9876543"
            )
        )
        db.userDao().insertUsers(users)

        // 3. Cash Accounts
        val accounts = listOf(
            CashAccountEntity(
                name = "Cash in Till",
                nameUrdu = "دکان میں نقد کیش",
                accountType = PaymentMethod.CASH,
                openingBalance = 25000.0,
                currentBalance = 32500.0,
                accountNumber = "Till #1"
            ),
            CashAccountEntity(
                name = "Meezan Islamic Bank",
                nameUrdu = "میزان بینک اکاؤنٹ",
                accountType = PaymentMethod.BANK,
                openingBalance = 150000.0,
                currentBalance = 178000.0,
                accountNumber = "PK45MEZN000123456789"
            ),
            CashAccountEntity(
                name = "Easypaisa Merchant",
                nameUrdu = "ایزی پیسہ مرچنٹ",
                accountType = PaymentMethod.EASYPAISA,
                openingBalance = 20000.0,
                currentBalance = 28500.0,
                accountNumber = "0300-1234567"
            ),
            CashAccountEntity(
                name = "JazzCash Business",
                nameUrdu = "جاز کیش بزنس",
                accountType = PaymentMethod.JAZZCASH,
                openingBalance = 15000.0,
                currentBalance = 21400.0,
                accountNumber = "0301-7654321"
            ),
            CashAccountEntity(
                name = "Card POS Terminal",
                nameUrdu = "کارڈ مشین",
                accountType = PaymentMethod.CARD,
                openingBalance = 5000.0,
                currentBalance = 14200.0,
                accountNumber = "HBL POS-9912"
            )
        )
        db.cashAccountDao().insertAccounts(accounts)

        // 4. Products (Mandatory prompt items + groceries + low stock demo)
        val products = listOf(
            ProductEntity(
                name = "Coca Cola 500ml",
                nameUrdu = "کوکا کولا 500 ملی",
                sku = "CC-500",
                barcode = "89640001",
                category = "Beverages",
                brand = "Coca Cola",
                purchasePrice = 75.0,
                salePrice = 100.0,
                wholesalePrice = 90.0,
                currentStock = 48.0,
                minStock = 12.0,
                unit = "Bottle",
                description = "Cold chilled 500ml pet bottle"
            ),
            ProductEntity(
                name = "Pepsi 500ml",
                nameUrdu = "پیپسی 500 ملی",
                sku = "PEP-500",
                barcode = "89640002",
                category = "Beverages",
                brand = "PepsiCo",
                purchasePrice = 75.0,
                salePrice = 100.0,
                wholesalePrice = 90.0,
                currentStock = 50.0,
                minStock = 12.0,
                unit = "Bottle",
                description = "Chilled 500ml soft drink"
            ),
            ProductEntity(
                name = "Mineral Water 1.5L",
                nameUrdu = "منرل واٹر 1.5 لیٹر",
                sku = "AQU-15",
                barcode = "89640003",
                category = "Beverages",
                brand = "Aquafina",
                purchasePrice = 70.0,
                salePrice = 110.0,
                wholesalePrice = 95.0,
                currentStock = 36.0,
                minStock = 10.0,
                unit = "Bottle",
                description = "Purified mineral drinking water"
            ),
            ProductEntity(
                name = "Biscuits (Super / Tuk)",
                nameUrdu = "بسکٹ (سپر / ٹک)",
                sku = "BSC-SUP",
                barcode = "89640004",
                category = "Bakery & Snacks",
                brand = "Peek Freans",
                purchasePrice = 38.0,
                salePrice = 50.0,
                wholesalePrice = 45.0,
                currentStock = 60.0,
                minStock = 15.0,
                unit = "Pack",
                description = "Fresh egg & milk crispy tea biscuits"
            ),
            ProductEntity(
                name = "Milk (Olper's 1L)",
                nameUrdu = "اولپرز خالص دودھ 1 لیٹر",
                sku = "OLP-1L",
                barcode = "89640005",
                category = "Dairy",
                brand = "Engro Foods",
                purchasePrice = 255.0,
                salePrice = 290.0,
                wholesalePrice = 275.0,
                currentStock = 24.0,
                minStock = 8.0,
                unit = "Pack",
                description = "Full cream UHT processed milk"
            ),
            ProductEntity(
                name = "Bread (Dawn Large)",
                nameUrdu = "ڈان بریڈ بڑی",
                sku = "DWN-BRD",
                barcode = "89640006",
                category = "Bakery & Snacks",
                brand = "Dawn",
                purchasePrice = 135.0,
                salePrice = 160.0,
                wholesalePrice = 150.0,
                currentStock = 14.0,
                minStock = 5.0,
                unit = "Pcs",
                description = "Daily fresh sliced white bread"
            ),
            ProductEntity(
                name = "Shampoo (Sunsilk 180ml)",
                nameUrdu = "سن سلک شیمپو 180 ملی",
                sku = "SNS-180",
                barcode = "89640007",
                category = "Personal Care",
                brand = "Unilever",
                purchasePrice = 390.0,
                salePrice = 480.0,
                wholesalePrice = 440.0,
                currentStock = 16.0,
                minStock = 6.0,
                unit = "Bottle",
                description = "Black shine nutritive conditioner shampoo"
            ),
            ProductEntity(
                name = "Soap (Lux 140g)",
                nameUrdu = "لکس صابن 140 گرام",
                sku = "LUX-140",
                barcode = "89640008",
                category = "Personal Care",
                brand = "Lux",
                purchasePrice = 115.0,
                salePrice = 150.0,
                wholesalePrice = 135.0,
                currentStock = 45.0,
                minStock = 10.0,
                unit = "Bar",
                description = "Rose & vitamin E beauty soap"
            ),
            ProductEntity(
                name = "Dalda Cooking Oil 1L",
                nameUrdu = "ڈالڈا کوکنگ آئل 1 لیٹر",
                sku = "DLD-1L",
                barcode = "89640009",
                category = "Grocery & Staples",
                brand = "Dalda",
                purchasePrice = 520.0,
                salePrice = 580.0,
                wholesalePrice = 550.0,
                currentStock = 4.0, // LOW STOCK ALERT
                minStock = 10.0,
                unit = "Pouch",
                description = "Pure refined premium cooking oil"
            ),
            ProductEntity(
                name = "Tapal Danedar Tea 430g",
                nameUrdu = "ٹپال دانے دار چائے 430 گرام",
                sku = "TPL-430",
                barcode = "89640010",
                category = "Grocery & Staples",
                brand = "Tapal",
                purchasePrice = 640.0,
                salePrice = 720.0,
                wholesalePrice = 680.0,
                currentStock = 18.0,
                minStock = 8.0,
                unit = "Pack",
                description = "Strong aroma black blend tea"
            ),
            ProductEntity(
                name = "Basmati Super Rice 5kg",
                nameUrdu = "سپر باسمتی چاول 5 کلو",
                sku = "RCE-5KG",
                barcode = "89640011",
                category = "Grocery & Staples",
                brand = "Guard",
                purchasePrice = 1650.0,
                salePrice = 1900.0,
                wholesalePrice = 1800.0,
                currentStock = 9.0,
                minStock = 15.0, // LOW STOCK
                unit = "Bag",
                description = "Aged extra long grain fragrant rice"
            ),
            ProductEntity(
                name = "Shan Biryani Masala",
                nameUrdu = "شان بریانی مصالحہ",
                sku = "SHN-BRY",
                barcode = "89640012",
                category = "Grocery & Staples",
                brand = "Shan",
                purchasePrice = 95.0,
                salePrice = 120.0,
                wholesalePrice = 110.0,
                currentStock = 35.0,
                minStock = 10.0,
                unit = "Pack",
                description = "Special bombay biryani spice mix"
            )
        )
        db.productDao().insertProducts(products)

        // 5. Customers (Requested: Ahmed, Ali, Usman)
        val customers = listOf(
            CustomerEntity(
                name = "Ahmed Raza",
                phone = "0301-2345678",
                address = "House # 45, Street 8, Sector F, Lahore",
                email = "ahmed.raza@gmail.com",
                openingBalance = 5000.0,
                currentBalance = 7000.0,
                notes = "Long-time neighborhood customer. Pays monthly."
            ),
            CustomerEntity(
                name = "Ali Hassan",
                phone = "0312-3456789",
                address = "Plot 12, Johar Town, Lahore",
                email = "ali.hassan@yahoo.com",
                openingBalance = 2500.0,
                currentBalance = 3800.0,
                notes = "Local restaurant caterer, regular buyer."
            ),
            CustomerEntity(
                name = "Usman Farooq",
                phone = "0321-4567890",
                address = "Block C, Gulberg III, Lahore",
                email = "usman.f@gmail.com",
                openingBalance = 0.0,
                currentBalance = 1200.0,
                notes = "Occasional credit customer, clears within 10 days."
            ),
            CustomerEntity(
                name = "Tariq Mahmood",
                phone = "0333-5678901",
                address = "Model Town, Lahore",
                email = "tariq.m@hotmail.com",
                openingBalance = 0.0,
                currentBalance = 0.0,
                notes = "Always pays cash / Easypaisa on spot."
            )
        )
        db.customerDao().insertCustomers(customers)

        // Customer Ledger Entries for Ahmed (Matching the prompt example: Opening 5000, New Sale 8000, Paid 6000 -> Current 7000)
        val ahmedEntries = listOf(
            CustomerLedgerEntryEntity(
                customerId = 1,
                date = System.currentTimeMillis() - 86400000L * 5,
                type = LedgerEntryType.OPENING,
                referenceNumber = "OPN-001",
                debit = 5000.0,
                credit = 0.0,
                runningBalance = 5000.0,
                notes = "Opening Khata Balance"
            ),
            CustomerLedgerEntryEntity(
                customerId = 1,
                date = System.currentTimeMillis() - 86400000L * 3,
                type = LedgerEntryType.SALE,
                referenceNumber = "INV-1001",
                debit = 8000.0,
                credit = 0.0,
                runningBalance = 13000.0,
                notes = "Monthly Grocery Bill"
            ),
            CustomerLedgerEntryEntity(
                customerId = 1,
                date = System.currentTimeMillis() - 86400000L * 1,
                type = LedgerEntryType.PAYMENT_RECEIVED,
                referenceNumber = "REC-501",
                debit = 0.0,
                credit = 6000.0,
                runningBalance = 7000.0,
                notes = "Partial Payment Received via Easypaisa"
            )
        )
        db.customerDao().insertLedgerEntries(ahmedEntries)

        // 6. Suppliers
        val suppliers = listOf(
            SupplierEntity(
                name = "Lahore Central FMCG Distributors",
                phone = "0300-8877665",
                company = "Nestlé & Unilever Agency",
                address = "Badami Bagh, Lahore",
                openingBalance = 35000.0,
                currentBalance = 45000.0,
                notes = "Weekly delivery on Mondays"
            ),
            SupplierEntity(
                name = "Karachi Beverage Wholesale",
                phone = "0321-9988776",
                company = "Pepsi & Coke Depo",
                address = "Gulberg Industrial, Lahore",
                openingBalance = 15000.0,
                currentBalance = 24000.0,
                notes = "Delivers 2 times a week"
            )
        )
        db.supplierDao().insertSuppliers(suppliers)

        // 7. Expenses
        val expenses = listOf(
            ExpenseEntity(
                title = "Shop Rent (Month)",
                category = "Rent",
                amount = 25000.0,
                paymentAccount = "BANK",
                date = System.currentTimeMillis() - 86400000L * 4,
                notes = "Plaza Shop # 12 rent paid via Meezan Bank"
            ),
            ExpenseEntity(
                title = "LESCO Electricity Bill",
                category = "Electricity",
                amount = 8400.0,
                paymentAccount = "JAZZCASH",
                date = System.currentTimeMillis() - 86400000L * 3,
                notes = "Commercial meter bill"
            ),
            ExpenseEntity(
                title = "Helper Salary (Adnan)",
                category = "Salary",
                amount = 12000.0,
                paymentAccount = "CASH",
                date = System.currentTimeMillis() - 86400000L * 2,
                notes = "Monthly shop assistant stipend"
            ),
            ExpenseEntity(
                title = "Tea & Shop Maintenance",
                category = "Shop Maintenance",
                amount = 1450.0,
                paymentAccount = "CASH",
                date = System.currentTimeMillis() - 86400000L * 1,
                notes = "Customer hospitality and cleaning supplies"
            )
        )
        db.expenseDao().insertExpenses(expenses)

        // 8. Sample Sales for immediate dashboard charts and reporting
        val now = System.currentTimeMillis()
        val s1 = SaleEntity(
            invoiceNumber = "INV-1001",
            customerId = 1,
            customerName = "Ahmed Raza",
            customerPhone = "0301-2345678",
            subtotal = 8000.0,
            discount = 0.0,
            tax = 0.0,
            grandTotal = 8000.0,
            paidAmount = 0.0,
            remainingAmount = 8000.0,
            paymentMethodSummary = "CUSTOMER_CREDIT",
            paymentStatus = PaymentStatus.UNPAID,
            totalCost = 6500.0,
            cashierName = "Owner",
            timestamp = now - 86400000L * 3
        )
        val s2 = SaleEntity(
            invoiceNumber = "INV-1002",
            customerId = 4,
            customerName = "Tariq Mahmood",
            customerPhone = "0333-5678901",
            subtotal = 2450.0,
            discount = 50.0,
            tax = 0.0,
            grandTotal = 2400.0,
            paidAmount = 2400.0,
            remainingAmount = 0.0,
            paymentMethodSummary = "EASYPAISA",
            paymentStatus = PaymentStatus.PAID,
            totalCost = 1850.0,
            cashierName = "Cashier",
            timestamp = now - 86400000L * 2
        )
        val s3 = SaleEntity(
            invoiceNumber = "INV-1003",
            customerId = null,
            customerName = "Walk-in Cash Customer",
            subtotal = 1580.0,
            discount = 0.0,
            tax = 0.0,
            grandTotal = 1580.0,
            paidAmount = 1580.0,
            remainingAmount = 0.0,
            paymentMethodSummary = "CASH",
            paymentStatus = PaymentStatus.PAID,
            totalCost = 1200.0,
            cashierName = "Owner",
            timestamp = now - 86400000L * 1
        )
        val s4 = SaleEntity(
            invoiceNumber = "INV-1004",
            customerId = 2,
            customerName = "Ali Hassan",
            customerPhone = "0312-3456789",
            subtotal = 4800.0,
            discount = 100.0,
            tax = 0.0,
            grandTotal = 4700.0,
            paidAmount = 3000.0,
            remainingAmount = 1700.0,
            paymentMethodSummary = "SPLIT: CASH + CREDIT",
            paymentStatus = PaymentStatus.PARTIAL,
            totalCost = 3700.0,
            cashierName = "Manager",
            timestamp = now - 3600000L * 4
        )
        val s5 = SaleEntity(
            invoiceNumber = "INV-1005",
            customerId = null,
            customerName = "Walk-in Customer",
            subtotal = 890.0,
            discount = 0.0,
            tax = 0.0,
            grandTotal = 890.0,
            paidAmount = 890.0,
            remainingAmount = 0.0,
            paymentMethodSummary = "JAZZCASH",
            paymentStatus = PaymentStatus.PAID,
            totalCost = 680.0,
            cashierName = "Cashier",
            timestamp = now - 1800000L
        )
        db.saleDao().insertSales(listOf(s1, s2, s3, s4, s5))

        // Sale items
        val items = listOf(
            SaleItemEntity(saleId = 1, productId = 10, productName = "Tapal Danedar Tea 430g", quantity = 4.0, unitPrice = 720.0, costPrice = 640.0, discount = 0.0, total = 2880.0),
            SaleItemEntity(saleId = 1, productId = 11, productName = "Basmati Super Rice 5kg", quantity = 2.0, unitPrice = 1900.0, costPrice = 1650.0, discount = 0.0, total = 3800.0),
            SaleItemEntity(saleId = 1, productId = 5, productName = "Milk (Olper's 1L)", quantity = 4.0, unitPrice = 290.0, costPrice = 255.0, discount = 0.0, total = 1160.0),
            SaleItemEntity(saleId = 1, productId = 4, productName = "Biscuits (Super / Tuk)", quantity = 3.0, unitPrice = 50.0, costPrice = 38.0, discount = 0.0, total = 150.0),

            SaleItemEntity(saleId = 2, productId = 7, productName = "Shampoo (Sunsilk 180ml)", quantity = 2.0, unitPrice = 480.0, costPrice = 390.0, discount = 0.0, total = 960.0),
            SaleItemEntity(saleId = 2, productId = 8, productName = "Soap (Lux 140g)", quantity = 6.0, unitPrice = 150.0, costPrice = 115.0, discount = 50.0, total = 850.0),
            SaleItemEntity(saleId = 2, productId = 1, productName = "Coca Cola 500ml", quantity = 6.0, unitPrice = 100.0, costPrice = 75.0, discount = 0.0, total = 600.0),

            SaleItemEntity(saleId = 3, productId = 5, productName = "Milk (Olper's 1L)", quantity = 4.0, unitPrice = 290.0, costPrice = 255.0, discount = 0.0, total = 1160.0),
            SaleItemEntity(saleId = 3, productId = 6, productName = "Bread (Dawn Large)", quantity = 2.0, unitPrice = 160.0, costPrice = 135.0, discount = 0.0, total = 320.0),
            SaleItemEntity(saleId = 3, productId = 4, productName = "Biscuits (Super / Tuk)", quantity = 2.0, unitPrice = 50.0, costPrice = 38.0, discount = 0.0, total = 100.0),

            SaleItemEntity(saleId = 4, productId = 9, productName = "Dalda Cooking Oil 1L", quantity = 5.0, unitPrice = 580.0, costPrice = 520.0, discount = 100.0, total = 2800.0),
            SaleItemEntity(saleId = 4, productId = 11, productName = "Basmati Super Rice 5kg", quantity = 1.0, unitPrice = 1900.0, costPrice = 1650.0, discount = 0.0, total = 1900.0),

            SaleItemEntity(saleId = 5, productId = 2, productName = "Pepsi 500ml", quantity = 4.0, unitPrice = 100.0, costPrice = 75.0, discount = 0.0, total = 400.0),
            SaleItemEntity(saleId = 5, productId = 3, productName = "Mineral Water 1.5L", quantity = 3.0, unitPrice = 110.0, costPrice = 70.0, discount = 0.0, total = 330.0),
            SaleItemEntity(saleId = 5, productId = 6, productName = "Bread (Dawn Large)", quantity = 1.0, unitPrice = 160.0, costPrice = 135.0, discount = 0.0, total = 160.0)
        )
        db.saleDao().insertSaleItems(items)

        // 9. Initial Audit Log
        db.auditLogDao().insertLog(
            AuditLogEntity(
                userId = 1,
                userName = "System",
                action = "INITIALIZE_DATABASE",
                details = "Dukan POS system initialized with Pakistan grocery catalog, accounts, and sample ledger.",
                timestamp = now
            )
        )
    }
}
