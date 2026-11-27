package com.viluong.business.model

enum class AppTab(val label: String) {
    DASHBOARD("Tổng quan"),
    ORDERS("Đơn hàng"),
    INVENTORY("Kho máy"),
    CUSTOMERS("Khách hàng"),
    BANKING("Thu & QR"),
}

enum class AssistantMode(val label: String) {
    CHAT("Hỏi đáp & lợi nhuận"),
    VOICE_ORDER("Giọng nói"),
}

enum class ReportScope(val label: String) {
    OVERVIEW("Báo cáo tổng quan"),
    DEBT("Báo cáo công nợ"),
    WARRANTY("Báo cáo kho & bảo hành"),
}

enum class OrderStatus(val label: String) {
    PENDING("Chờ xử lý"),
    DEBT("Còn công nợ"),
    COMPLETED("Hoàn tất"),
    CANCELLED("Đã hủy"),
}

enum class PaymentMethod(val label: String) {
    CASH("Tiền mặt"),
    TRANSFER("Chuyển khoản"),
    QR("VietQR"),
}

enum class CashflowType(val label: String) {
    INCOME("Thu ngoài đơn"),
    EXPENSE("Chi phí xưởng"),
}

enum class MachineCondition(val label: String) {
    NEW("Máy mới"),
    USED("Máy đã qua sử dụng"),
}

data class BankPreset(
    val code: String,
    val name: String,
    val bin: String,
)

val VIETNAM_BANKS: List<BankPreset> = listOf(
    BankPreset("VCB", "Vietcombank", "970436"),
    BankPreset("TCB", "Techcombank", "970407"),
    BankPreset("MB", "MB Bank", "970422"),
    BankPreset("ACB", "ACB", "970416"),
    BankPreset("BIDV", "BIDV", "970418"),
    BankPreset("VPB", "VPBank", "970432"),
)

data class BankAccount(
    val id: String,
    val bankCode: String,
    val bankName: String,
    val bin: String,
    val accountNumber: String,
    val accountName: String,
    val isDefault: Boolean = false,
)

data class Machine(
    val id: String,
    val name: String,
    val brand: String,
    val model: String,
    val category: String,
    val condition: MachineCondition,
    val sellPrice: Long,
    val costPrice: Long,
    val stockQty: Int,
    val warrantyMonths: Int = 12,
)

data class Customer(
    val id: String,
    val name: String,
    val phone: String,
    val workshopAddress: String = "",
    val notes: String = "",
)

data class OrderItem(
    val machineId: String,
    val machineName: String,
    val brand: String,
    val model: String,
    val condition: MachineCondition,
    val quantity: Int,
    val unitPrice: Long,
    val warrantyMonths: Int = 12,
)

data class Order(
    val id: String,
    val orderCode: String,
    val customerId: String,
    val customerName: String,
    val customerPhone: String,
    val workshopAddress: String,
    val items: List<OrderItem>,
    val shippingFee: Long = 0L,
    val depositAmount: Long = 0L,
    val paidAmount: Long = 0L,
    val status: OrderStatus = OrderStatus.DEBT,
    val paymentAccountId: String = "",
    val notes: String = "",
    val cancelReason: String = "",
    val isDeleted: Boolean = false,
    val createdAtMillis: Long = System.currentTimeMillis(),
) {
    val totalAmount: Long
        get() = items.sumOf { it.unitPrice * it.quantity } + shippingFee

    val remainingDebt: Long
        get() = if (status == OrderStatus.CANCELLED) 0L else (totalAmount - paidAmount).coerceAtLeast(0L)
}

data class PaymentRecord(
    val id: String,
    val orderId: String,
    val orderCode: String,
    val amount: Long,
    val method: PaymentMethod,
    val note: String = "",
    val createdAtMillis: Long = System.currentTimeMillis(),
)

data class CashflowRecord(
    val id: String,
    val type: CashflowType,
    val title: String,
    val amount: Long,
    val method: PaymentMethod = PaymentMethod.CASH,
    val note: String = "",
    val createdAtMillis: Long = System.currentTimeMillis(),
)

data class BankNotification(
    val id: String,
    val bankName: String,
    val amountIn: Long,
    val transferContent: String,
    val orderCodeHint: String = "",
    val matchedOrderId: String? = null,
    val receivedAtMillis: Long = System.currentTimeMillis(),
)

data class StoreSettings(
    val ownerName: String = "Trần Vĩ Lương",
    val ownerRoleLabel: String = "Chủ cửa hàng",
    val storeName: String = "VĨ LƯƠNG BUSINESS",
    val storePhone: String = "",
    val storeAddress: String = "Bình Tân, TP. Hồ Chí Minh",
)

data class VoiceWakeConfig(
    val enabled: Boolean = true,
    val wakeWord: String = "ok trợ lý",
    val speakReply: Boolean = true,
)

data class WorkspaceState(
    val store: StoreSettings = StoreSettings(),
    val voiceConfig: VoiceWakeConfig = VoiceWakeConfig(),
    val machines: List<Machine> = emptyList(),
    val customers: List<Customer> = emptyList(),
    val orders: List<Order> = emptyList(),
    val payments: List<PaymentRecord> = emptyList(),
    val cashflows: List<CashflowRecord> = emptyList(),
    val bankAccounts: List<BankAccount> = emptyList(),
    val bankNotifications: List<BankNotification> = emptyList(),
)
