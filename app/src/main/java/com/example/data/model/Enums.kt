package com.example.data.model

enum class UserRole(val displayName: String, val displayNameUrdu: String) {
    OWNER("Owner", "مالک"),
    ADMIN("Admin", "ایڈمن"),
    MANAGER("Manager", "منیجر"),
    CASHIER("Cashier", "کیشیئر")
}

enum class Permission(val code: String, val title: String, val titleUrdu: String) {
    CREATE_SALE("create_sale", "Create Sale", "نئی فروخت کرنا"),
    EDIT_SALE("edit_sale", "Edit Sale", "فروخت میں ترمیم"),
    DELETE_SALE("delete_sale", "Delete Sale", "فروخت ڈیلیٹ کرنا"),
    GIVE_DISCOUNT("give_discount", "Give Discount", "رعایت دینا"),
    CHANGE_PRODUCT_PRICE("change_price", "Change Product Price", "قیمت تبدیل کرنا"),
    VIEW_PROFIT("view_profit", "View Profit", "نفع دیکھنا"),
    VIEW_REPORTS("view_reports", "View Reports", "رپورٹس دیکھنا"),
    RECEIVE_PAYMENT("receive_payment", "Receive Payment", "وصولی رقم"),
    MANAGE_CUSTOMERS("manage_customers", "Manage Customers", "گاہکوں کا انتظام"),
    MANAGE_INVENTORY("manage_inventory", "Manage Inventory", "اسٹاک کا انتظام"),
    MANAGE_EXPENSES("manage_expenses", "Manage Expenses", "اخراجات کا انتظام"),
    PROCESS_RETURNS("process_returns", "Process Returns", "واپسی لینا"),
    MANAGE_USERS("manage_users", "Manage Staff/Users", "اسٹاف کا انتظام"),
    CHANGE_SETTINGS("change_settings", "Change Settings", "سیٹنگز تبدیل کرنا")
}

enum class PaymentMethod(val code: String, val title: String, val titleUrdu: String) {
    CASH("CASH", "Cash", "نقد کیش"),
    BANK("BANK", "Bank Transfer", "بینک ٹرانسفر"),
    CARD("CARD", "Card / POS", "کارڈ"),
    EASYPAISA("EASYPAISA", "Easypaisa", "ایزی پیسہ"),
    JAZZCASH("JAZZCASH", "JazzCash", "جاز کیش"),
    CUSTOMER_CREDIT("CUSTOMER_CREDIT", "Customer Credit / Udhaar", "گاہک ادھار")
}

enum class PaymentStatus(val title: String, val titleUrdu: String) {
    PAID("Paid", "مکمل ادا شدہ"),
    PARTIAL("Partial", "جزوی ادائیگی"),
    UNPAID("Unpaid", "غیر ادا شدہ")
}

enum class StockMovementType(val title: String, val titleUrdu: String) {
    SALE("Sale", "فروخت"),
    PURCHASE("Purchase", "خریداری"),
    SALE_RETURN("Sale Return", "گاہک سے واپسی"),
    PURCHASE_RETURN("Purchase Return", "سپلائر کو واپسی"),
    MANUAL_ADJUSTMENT("Manual Adjustment", "دستی درستگی"),
    DAMAGED_STOCK("Damaged Stock", "خراب یا ضائع مال")
}

enum class LedgerEntryType(val title: String, val titleUrdu: String) {
    OPENING("Opening Balance", "ابتدائی بقایا"),
    SALE("Sale / Bill", "فروخت بل"),
    PAYMENT_RECEIVED("Payment Received", "وصولی رقم"),
    RETURN("Sale Return", "فروخت واپسی"),
    PURCHASE("Stock Purchase", "مال خریداری"),
    PAYMENT_MADE("Payment to Supplier", "سپلائر کو ادائیگی")
}

enum class ReceiptSize(val title: String, val widthMm: Int) {
    THERMAL_58MM("58mm Thermal", 58),
    THERMAL_80MM("80mm Thermal", 80),
    A4("A4 Standard", 210)
}

enum class AppLanguage(val code: String, val label: String) {
    ENGLISH("en", "English"),
    URDU("ur", "اردو")
}
