package com.viluong.business.ops

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.viluong.business.model.BankAccount
import com.viluong.business.model.CashflowType
import com.viluong.business.model.Customer
import com.viluong.business.model.Order
import com.viluong.business.model.OrderStatus
import com.viluong.business.model.WorkspaceState
import com.viluong.business.util.formatDateTimeVn
import com.viluong.business.util.formatVnd
import java.text.Normalizer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private val vnLocale = Locale("vi", "VN")
private val vnTimeZone = TimeZone.getTimeZone("Asia/Ho_Chi_Minh")

fun normalizePhoneDigits(raw: String): String = raw.filter { it.isDigit() }

private fun foldVietnamese(input: String): String {
    val noMarks = Normalizer.normalize(input.lowercase(vnLocale), Normalizer.Form.NFD)
        .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
        .replace('đ', 'd')
    return noMarks.replace("\\s+".toRegex(), " ").trim()
}

private val HCMC_STREET_HINTS: List<Pair<String, String>> = listOf(
    "le van quoi" to "Lê Văn Quới, P. Bình Hưng Hòa A, Q. Bình Tân, TP. Hồ Chí Minh",
    "ten lua" to "Tên Lửa, P. Bình Trị Đông B, Q. Bình Tân, TP. Hồ Chí Minh",
    "binh tri dong" to "Bình Trị Đông, Q. Bình Tân, TP. Hồ Chí Minh",
    "huong lo 2" to "Hương Lộ 2, P. Bình Trị Đông A, Q. Bình Tân, TP. Hồ Chí Minh",
    "luy ban bich" to "Lũy Bán Bích, P. Hòa Thạnh, Q. Tân Phú, TP. Hồ Chí Minh",
    "au co" to "Âu Cơ, P. Phú Trung, Q. Tân Phú, TP. Hồ Chí Minh",
    "truong chinh" to "Trường Chinh, P. Tây Thạnh, Q. Tân Phú, TP. Hồ Chí Minh",
    "quoc lo 1a" to "Quốc Lộ 1A, Q. Bình Tân, TP. Hồ Chí Minh",
)

fun suggestHcmcAddresses(query: String): List<String> {
    val folded = foldVietnamese(query)
    if (folded.isBlank()) {
        return HCMC_STREET_HINTS.take(4).map { it.second }
    }
    val matched = HCMC_STREET_HINTS.filter { (key, full) ->
        folded.contains(key) || key.contains(folded) || foldVietnamese(full).contains(folded)
    }.map { it.second }

    return if (matched.isNotEmpty()) {
        matched
    } else {
        listOf("$query, TP. Hồ Chí Minh")
    }
}

data class DuplicatePhoneGroup(
    val phoneDigits: String,
    val customers: List<Customer>,
)

fun findDuplicatePhoneGroups(customers: List<Customer>): List<DuplicatePhoneGroup> {
    return customers
        .filter { normalizePhoneDigits(it.phone).length >= 8 }
        .groupBy { normalizePhoneDigits(it.phone) }
        .filterValues { it.size > 1 }
        .map { (digits, group) -> DuplicatePhoneGroup(phoneDigits = digits, customers = group) }
}

data class MergeCustomersResult(
    val nextState: WorkspaceState,
    val backupState: WorkspaceState,
    val mergedCount: Int,
)

fun mergeDuplicateCustomersByPhone(
    state: WorkspaceState,
    phoneDigits: String,
    keepCustomerId: String? = null,
): MergeCustomersResult {
    val targetDigits = normalizePhoneDigits(phoneDigits)
    val matching = state.customers.filter { normalizePhoneDigits(it.phone) == targetDigits }
    if (matching.size <= 1) {
        return MergeCustomersResult(
            nextState = state,
            backupState = state,
            mergedCount = 0,
        )
    }

    val primary = matching.firstOrNull { it.id == keepCustomerId } ?: matching.first()
    val secondaryList = matching.filter { it.id != primary.id }
    val secondaryIds = secondaryList.map { it.id }.toSet()

    val mergedAddress = primary.workshopAddress.ifBlank {
        secondaryList.firstOrNull { it.workshopAddress.isNotBlank() }?.workshopAddress.orEmpty()
    }
    val combinedNotes = (listOf(primary.notes) + secondaryList.map { it.notes })
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .distinct()
        .joinToString(" | ")

    val updatedPrimary = primary.copy(
        workshopAddress = mergedAddress,
        notes = combinedNotes,
    )

    val nextCustomers = state.customers
        .filterNot { it.id in secondaryIds }
        .map { if (it.id == primary.id) updatedPrimary else it }

    val nextOrders = state.orders.map { ord ->
        if (ord.customerId in secondaryIds) {
            ord.copy(
                customerId = updatedPrimary.id,
                customerName = updatedPrimary.name,
                customerPhone = updatedPrimary.phone.ifBlank { ord.customerPhone },
                workshopAddress = ord.workshopAddress.ifBlank { updatedPrimary.workshopAddress },
            )
        } else {
            ord
        }
    }

    return MergeCustomersResult(
        nextState = state.copy(
            customers = nextCustomers,
            orders = nextOrders,
        ),
        backupState = state,
        mergedCount = secondaryList.size,
    )
}

fun openZaloContact(context: Context, phoneOrHandle: String) {
    val cleaned = phoneOrHandle.trim()
    if (cleaned.isEmpty()) {
        Toast.makeText(context, "Chưa có số điện thoại hoặc nick Zalo", Toast.LENGTH_SHORT).show()
        return
    }
    val digits = normalizePhoneDigits(cleaned)
    val targetUrl = if (digits.length >= 8) {
        "https://zalo.me/$digits"
    } else {
        "https://zalo.me/${cleaned.removePrefix("@")}"
    }
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(targetUrl)).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    runCatching {
        context.startActivity(intent)
    }.onFailure {
        Toast.makeText(context, "Không mở được liên kết Zalo", Toast.LENGTH_SHORT).show()
    }
}

fun buildVietQrPayloadText(
    account: BankAccount,
    amountVnd: Long,
    transferContent: String,
): String {
    val cleanContent = foldVietnamese(transferContent).uppercase(Locale.ROOT).take(45)
    return buildString {
        appendLine("NGÂN HÀNG: ${account.bankName} (${account.bankCode})")
        appendLine("BIN: ${account.bin}")
        appendLine("SỐ TK: ${account.accountNumber}")
        appendLine("CHỦ TK: ${account.accountName}")
        if (amountVnd > 0L) {
            appendLine("SỐ TIỀN: ${formatVnd(amountVnd)}")
        }
        append("NỘI DUNG CK: $cleanContent")
    }
}

fun buildOrderInvoiceText(
    storeName: String,
    storePhone: String,
    storeAddress: String,
    order: Order,
): String {
    return buildString {
        appendLine("====================================")
        appendLine(storeName.uppercase(vnLocale))
        if (storePhone.isNotBlank()) appendLine("ĐT: $storePhone")
        if (storeAddress.isNotBlank()) appendLine("ĐC: $storeAddress")
        appendLine("====================================")
        appendLine("HÓA ĐƠN BÁN HÀNG: ${order.orderCode}")
        appendLine("Ngày lập: ${formatDateTimeVn(order.createdAtMillis)}")
        appendLine("Khách hàng: ${order.customerName}")
        if (order.customerPhone.isNotBlank()) appendLine("SĐT: ${order.customerPhone}")
        if (order.workshopAddress.isNotBlank()) appendLine("Địa chỉ xưởng: ${order.workshopAddress}")
        appendLine("------------------------------------")
        order.items.forEach { item ->
            val lineTotal = item.unitPrice * item.quantity
            appendLine("- ${item.machineName} (${item.model}) x${item.quantity}")
            appendLine("  Đơn giá: ${formatVnd(item.unitPrice)} -> ${formatVnd(lineTotal)}")
        }
        if (order.shippingFee > 0L) {
            appendLine("Phí vận chuyển: ${formatVnd(order.shippingFee)}")
        }
        appendLine("------------------------------------")
        appendLine("TỔNG CỘNG: ${formatVnd(order.totalAmount)}")
        appendLine("Đã thanh toán: ${formatVnd(order.paidAmount)}")
        appendLine("Còn lại: ${formatVnd(order.remainingDebt)}")
        append("Trạng thái: ${order.status.label}")
    }
}

fun buildMonthlyReportText(
    state: WorkspaceState,
    yearMonth: String,
): String {
    val ymFormat = SimpleDateFormat("yyyy-MM", Locale.US).apply {
        timeZone = vnTimeZone
    }
    val matchingOrders = state.orders.filter { ord ->
        !ord.isDeleted &&
            ord.status != OrderStatus.CANCELLED &&
            ymFormat.format(Date(ord.createdAtMillis)) == yearMonth
    }
    val matchingCashflows = state.cashflows.filter { cf ->
        ymFormat.format(Date(cf.createdAtMillis)) == yearMonth
    }

    val costMap = state.machines.associate { it.id to it.costPrice }
    val revenue = matchingOrders.sumOf { it.totalAmount }
    val collected = matchingOrders.sumOf { it.paidAmount }
    val debt = matchingOrders.sumOf { it.remainingDebt }
    val grossProfit = matchingOrders.sumOf { ord ->
        ord.items.sumOf { item ->
            val unitCost = costMap[item.machineId] ?: item.unitPrice
            (item.unitPrice - unitCost) * item.quantity
        }
    }
    val extraIncome = matchingCashflows.filter { it.type == CashflowType.INCOME }.sumOf { it.amount }
    val extraExpense = matchingCashflows.filter { it.type == CashflowType.EXPENSE }.sumOf { it.amount }
    val netProfit = grossProfit + extraIncome - extraExpense

    return buildString {
        appendLine("BÁO CÁO KẾT QUẢ KINH DOANH - $yearMonth")
        appendLine("Đơn vị: ${state.store.storeName}")
        appendLine("------------------------------------")
        appendLine("Số đơn hiệu lực: ${matchingOrders.size}")
        appendLine("Doanh thu: ${formatVnd(revenue)}")
        appendLine("Đã thu: ${formatVnd(collected)}")
        appendLine("Còn nợ: ${formatVnd(debt)}")
        appendLine("Lợi nhuận gộp: ${formatVnd(grossProfit)}")
        appendLine("Thu ngoài đơn: ${formatVnd(extraIncome)}")
        appendLine("Chi phí xưởng: ${formatVnd(extraExpense)}")
        append("LỢI NHUẬN RÒNG ƯỚC TÍNH: ${formatVnd(netProfit)}")
    }
}
