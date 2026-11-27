package com.viluong.business.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

private val vnLocale = Locale("vi", "VN")
private val vnTimeZone = TimeZone.getTimeZone("Asia/Ho_Chi_Minh")

fun formatVnd(amount: Long): String {
    val symbols = DecimalFormatSymbols(vnLocale).apply {
        groupingSeparator = '.'
        decimalSeparator = ','
    }
    val formatter = DecimalFormat("#,###", symbols)
    return "${formatter.format(amount)} VNĐ"
}

fun formatDateVn(millis: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy", vnLocale).apply {
        timeZone = vnTimeZone
    }
    return sdf.format(Date(millis))
}

fun formatDateTimeVn(millis: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", vnLocale).apply {
        timeZone = vnTimeZone
    }
    return sdf.format(Date(millis))
}

data class WarrantyStatus(
    val expiresAtMillis: Long,
    val remainingDays: Long,
    val isExpired: Boolean,
)

fun calculateWarrantyStatus(
    startMillis: Long,
    warrantyMonths: Int,
    nowMillis: Long = System.currentTimeMillis(),
): WarrantyStatus {
    val cal = Calendar.getInstance(vnTimeZone).apply {
        timeInMillis = startMillis
        add(Calendar.MONTH, warrantyMonths.coerceAtLeast(0))
    }
    val expiresAt = cal.timeInMillis
    val diffMillis = expiresAt - nowMillis
    val remainingDays = TimeUnit.MILLISECONDS.toDays(diffMillis)
    return WarrantyStatus(
        expiresAtMillis = expiresAt,
        remainingDays = remainingDays,
        isExpired = diffMillis < 0L,
    )
}
