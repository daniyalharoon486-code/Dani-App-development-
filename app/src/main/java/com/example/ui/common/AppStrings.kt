package com.example.ui.common

object AppStrings {
    fun t(key: String, isUrdu: Boolean): String {
        return if (isUrdu) {
            urduMap[key] ?: englishMap[key] ?: key
        } else {
            englishMap[key] ?: key
        }
    }

    private val englishMap = mapOf(
        "app_title" to "Dukan POS & Khata",
        "dashboard" to "Dashboard",
        "pos" to "Sales / POS",
        "invoices" to "Invoices",
        "receive_payment" to "Receive Payment",
        "customers" to "Customers",
        "products" to "Products",
        "inventory" to "Inventory",
        "suppliers" to "Suppliers",
        "purchases" to "Purchases",
        "expenses" to "Expenses",
        "cash_bank" to "Cash & Bank",
        "reports" to "Reports",
        "staff_users" to "Staff & Users",
        "settings" to "Settings",

        // Dashboard
        "total_sales" to "Total Sales",
        "cash_received" to "Cash Received",
        "credit_sales" to "Credit Sales",
        "payments_received" to "Payments Received",
        "gross_profit" to "Gross Profit",
        "net_profit" to "Net Profit",
        "number_of_bills" to "Number of Bills",
        "outstanding_balance" to "Customer Outstanding",
        "stock_value" to "Stock Value",
        "today" to "Today",
        "yesterday" to "Yesterday",
        "this_week" to "This Week",
        "this_month" to "This Month",
        "all_time" to "All Time",
        "custom_range" to "Custom Range",
        "quick_actions" to "Quick Actions",
        "new_sale_btn" to "+ New Sale",
        "add_customer_btn" to "+ Add Customer",
        "add_product_btn" to "+ Add Product",
        "add_expense_btn" to "+ Add Expense",

        // POS
        "search_products" to "Search products or barcode...",
        "all_categories" to "All",
        "cart" to "Current Cart",
        "empty_cart" to "Cart is empty. Tap products to add.",
        "qty" to "Qty",
        "price" to "Price",
        "subtotal" to "Subtotal",
        "discount" to "Discount",
        "tax" to "Tax",
        "grand_total" to "Grand Total",
        "paid" to "Paid",
        "remaining" to "Remaining",
        "hold_bill" to "Hold Bill",
        "save_draft" to "Save Draft",
        "clear_cart" to "Clear",
        "checkout" to "Checkout",
        "select_customer" to "Select Customer (Optional)",
        "walk_in_customer" to "Walk-in Cash Customer",
        "notes" to "Notes / Remarks",

        // Payment / Checkout
        "payment_screen" to "Payment & Settlement",
        "split_payment" to "Split Payment",
        "enter_amount" to "Enter Amount",
        "complete_sale" to "Complete Sale & Print",
        "payment_method" to "Payment Method",
        "cash" to "Cash",
        "bank" to "Bank Transfer",
        "card" to "Card / POS",
        "easypaisa" to "Easypaisa",
        "jazzcash" to "JazzCash",
        "customer_credit" to "Customer Credit / Udhaar",

        // Invoices
        "invoice_details" to "Invoice Details",
        "print_receipt" to "Print Receipt",
        "share" to "Share",
        "whatsapp" to "WhatsApp",
        "reprint" to "Reprint",
        "return_sale" to "Sales Return / Refund",
        "duplicate" to "Duplicate to Cart",

        // Khata & Customers
        "customer_khata" to "Customer Ledger (کھاتہ)",
        "opening_balance" to "Opening Balance",
        "current_balance" to "Current Balance",
        "debit" to "Debit / Added (بنام)",
        "credit" to "Credit / Paid (جمع)",
        "running_balance" to "Balance (بقایا)",
        "pay_supplier" to "Pay Supplier",

        // Inventory
        "low_stock_warning" to "Low Stock Alerts",
        "current_stock" to "Current Stock",
        "min_stock" to "Min Stock",
        "adjust_stock" to "Adjust Stock",
        "damaged_stock" to "Damaged Stock",

        // Common
        "save" to "Save",
        "cancel" to "Cancel",
        "delete" to "Delete",
        "confirm" to "Confirm",
        "edit" to "Edit",
        "search" to "Search...",
        "offline_ready" to "Offline Ready",
        "cloud_synced" to "Synced",
        "language_toggle" to "اردو میں دیکھیں"
    )

    private val urduMap = mapOf(
        "app_title" to "دکان پی او ایس و کھاتہ",
        "dashboard" to "ڈیش بورڈ",
        "pos" to "نئی فروخت / پی او ایس",
        "invoices" to "انوائسز و بل",
        "receive_payment" to "وصولی رقم",
        "customers" to "گاہک و کھاتہ",
        "products" to "مصنوعات",
        "inventory" to "اسٹاک انوینٹری",
        "suppliers" to "سپلائرز",
        "purchases" to "خریداری مال",
        "expenses" to "اخراجات",
        "cash_bank" to "کیش و بینک",
        "reports" to "رپورٹس و نفع",
        "staff_users" to "اسٹاف و صارفین",
        "settings" to "سیٹنگز و بیک اپ",

        // Dashboard
        "total_sales" to "کل فروخت",
        "cash_received" to "نقد وصولی",
        "credit_sales" to "ادھار فروخت",
        "payments_received" to "کھاتہ وصولیاں",
        "gross_profit" to "مجموعی نفع",
        "net_profit" to "خالص نفع",
        "number_of_bills" to "کل بل",
        "outstanding_balance" to "گاہکوں کا بقایا (ادھار)",
        "stock_value" to "کل اسٹاک مالیت",
        "today" to "آج",
        "yesterday" to "کل",
        "this_week" to "اس ہفتے",
        "this_month" to "اس ماہ",
        "all_time" to "تمام ریکارڈ",
        "custom_range" to "مخصوص تاریخ",
        "quick_actions" to "فوری بٹن",
        "new_sale_btn" to "+ نئی فروخت",
        "add_customer_btn" to "+ نیا گاہک",
        "add_product_btn" to "+ نئی پروڈکٹ",
        "add_expense_btn" to "+ خرچہ درج کریں",

        // POS
        "search_products" to "پروڈکٹ کا نام یا بارکوڈ تلاش کریں...",
        "all_categories" to "سب",
        "cart" to "موجودہ کسٹمر کارٹ",
        "empty_cart" to "کارٹ خالی ہے۔ پروڈکٹ پر کلک کر کے شامل کریں۔",
        "qty" to "تعداد",
        "price" to "قیمت",
        "subtotal" to "سب ٹوٹل",
        "discount" to "رعایت",
        "tax" to "ٹیکس",
        "grand_total" to "کل واجب الادا",
        "paid" to "ادا شدہ",
        "remaining" to "بقایا",
        "hold_bill" to "بل ہولڈ کریں",
        "save_draft" to "ڈرافٹ",
        "clear_cart" to "صاف کریں",
        "checkout" to "چیک آؤٹ / وصولی",
        "select_customer" to "گاہک منتخب کریں",
        "walk_in_customer" to "نقد واک اِن گاہک",
        "notes" to "نوٹ / تفصیل",

        // Payment / Checkout
        "payment_screen" to "ادائیگی و کھاتہ سیٹلمنٹ",
        "split_payment" to "تقسیم ادائیگی (Split)",
        "enter_amount" to "رقم درج کریں",
        "complete_sale" to "فروخت مکمل و پرنٹ",
        "payment_method" to "طریقہ ادائیگی",
        "cash" to "نقد کیش",
        "bank" to "بینک اکاؤنٹ",
        "card" to "کارڈ / پی او ایس",
        "easypaisa" to "ایزی پیسہ",
        "jazzcash" to "جاز کیش",
        "customer_credit" to "گاہک ادھار کھاتہ",

        // Invoices
        "invoice_details" to "بل کی تفصیل",
        "print_receipt" to "رسید پرنٹ کریں",
        "share" to "شیئر کریں",
        "whatsapp" to "واٹس ایپ",
        "reprint" to "دوبارہ پرنٹ",
        "return_sale" to "فروخت واپسی / ریفنڈ",
        "duplicate" to "دوبارہ کارٹ میں لائیں",

        // Khata & Customers
        "customer_khata" to "گاہک کھاتہ رجسٹر",
        "opening_balance" to "ابتدائی بقایا",
        "current_balance" to "موجودہ بقایا",
        "debit" to "بنام (سامان لیا)",
        "credit" to "جمع (رقم دی)",
        "running_balance" to "بقایا کھاتہ",
        "pay_supplier" to "سپلائر کو ادائیگی",

        // Inventory
        "low_stock_warning" to "کم اسٹاک کی وارننگ",
        "current_stock" to "موجود اسٹاک",
        "min_stock" to "کم از کم حد",
        "adjust_stock" to "اسٹاک درست کریں",
        "damaged_stock" to "ضائع مال",

        // Common
        "save" to "محفوظ کریں",
        "cancel" to "منسوخ",
        "delete" to "ختم کریں",
        "confirm" to "تصدیق کریں",
        "edit" to "ترمیم",
        "search" to "تلاش کریں...",
        "offline_ready" to "آف لائن تیار",
        "cloud_synced" to "مطابق محفوظ",
        "language_toggle" to "Switch to English"
    )
}
