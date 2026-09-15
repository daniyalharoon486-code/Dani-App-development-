package com.example.data.database

import androidx.room.TypeConverter
import com.example.data.model.AppLanguage
import com.example.data.model.LedgerEntryType
import com.example.data.model.PaymentMethod
import com.example.data.model.PaymentStatus
import com.example.data.model.ReceiptSize
import com.example.data.model.StockMovementType
import com.example.data.model.UserRole

class Converters {
    @TypeConverter
    fun fromUserRole(role: UserRole?): String? = role?.name

    @TypeConverter
    fun toUserRole(value: String?): UserRole? = value?.let { enumValueOf<UserRole>(it) }

    @TypeConverter
    fun fromPaymentMethod(method: PaymentMethod?): String? = method?.name

    @TypeConverter
    fun toPaymentMethod(value: String?): PaymentMethod? = value?.let { enumValueOf<PaymentMethod>(it) }

    @TypeConverter
    fun fromPaymentStatus(status: PaymentStatus?): String? = status?.name

    @TypeConverter
    fun toPaymentStatus(value: String?): PaymentStatus? = value?.let { enumValueOf<PaymentStatus>(it) }

    @TypeConverter
    fun fromStockMovementType(type: StockMovementType?): String? = type?.name

    @TypeConverter
    fun toStockMovementType(value: String?): StockMovementType? = value?.let { enumValueOf<StockMovementType>(it) }

    @TypeConverter
    fun fromLedgerEntryType(type: LedgerEntryType?): String? = type?.name

    @TypeConverter
    fun toLedgerEntryType(value: String?): LedgerEntryType? = value?.let { enumValueOf<LedgerEntryType>(it) }

    @TypeConverter
    fun fromReceiptSize(size: ReceiptSize?): String? = size?.name

    @TypeConverter
    fun toReceiptSize(value: String?): ReceiptSize? = value?.let { enumValueOf<ReceiptSize>(it) }

    @TypeConverter
    fun fromAppLanguage(lang: AppLanguage?): String? = lang?.name

    @TypeConverter
    fun toAppLanguage(value: String?): AppLanguage? = value?.let { enumValueOf<AppLanguage>(it) }
}
