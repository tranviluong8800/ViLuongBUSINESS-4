package com.viluong.business.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.viluong.business.model.Machine
import com.viluong.business.model.MachineCondition
import com.viluong.business.model.Order
import com.viluong.business.model.OrderStatus
import com.viluong.business.model.PaymentMethod
import com.viluong.business.model.WorkspaceState
import com.viluong.business.ui.theme.AmberWarn
import com.viluong.business.ui.theme.EmeraldOk
import com.viluong.business.ui.theme.GlassCard
import com.viluong.business.ui.theme.MoneyInputField
import com.viluong.business.ui.theme.MutedSlate
import com.viluong.business.ui.theme.NavyInk
import com.viluong.business.ui.theme.PrimaryZaloButton
import com.viluong.business.ui.theme.SecondaryGlassButton
import com.viluong.business.ui.theme.ZaloBlue
import com.viluong.business.ui.theme.ZaloBlueDark
import com.viluong.business.util.calculateWarrantyStatus
import com.viluong.business.util.formatDateTimeVn
import com.viluong.business.util.formatDateVn
import com.viluong.business.util.formatVnd

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OrdersScreen(
    state: WorkspaceState,
    onCreateOrder: () -> Unit,
    onRecordPayment: (Order, Long, PaymentMethod) -> Unit,
    onShareInvoiceZalo: (Order) -> Unit,
) {
    var statusFilter by remember { mutableStateOf<OrderStatus?>(null) }
    var paymentTargetOrder by remember { mutableStateOf<Order?>(null) }

    val visibleOrders = remember(state.orders, statusFilter) {
        state.orders
            .filter { !it.isDeleted }
            .filter { ord -> statusFilter == null || ord.status == statusFilter }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 8.dp, bottom = 22.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Đơn hàng (${visibleOrders.size})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = NavyInk,
                )
                PrimaryZaloButton(text = "+ Tạo đơn", onClick = onCreateOrder)
            }
        }

        item {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                SecondaryGlassButton(
                    text = "Tất cả",
                    textColor = if (statusFilter == null) ZaloBlueDark else NavyInk,
                    onClick = { statusFilter = null },
                )
                listOf(OrderStatus.DEBT, OrderStatus.COMPLETED, OrderStatus.CANCELLED).forEach { st ->
                    SecondaryGlassButton(
                        text = st.label,
                        textColor = if (statusFilter == st) ZaloBlueDark else NavyInk,
                        onClick = { statusFilter = st },
                    )
                }
            }
        }

        items(visibleOrders, key = { it.id }) { ord ->
            val firstItem = ord.items.firstOrNull()
            val warranty = firstItem?.let {
                calculateWarrantyStatus(ord.createdAtMillis, it.warrantyMonths)
            }

            GlassCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${ord.orderCode} · ${ord.customerName}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = NavyInk,
                        )
                        if (ord.customerPhone.isNotBlank()) {
                            Text("SĐT: ${ord.customerPhone}", fontSize = 12.sp, color = MutedSlate)
                        }
                        if (ord.workshopAddress.isNotBlank()) {
                            Text("Xưởng: ${ord.workshopAddress}", fontSize = 12.sp, color = MutedSlate)
                        }
                        Text(
                            text = ord.items.joinToString(", ") { "${it.machineName} (${it.model}) x${it.quantity}" },
                            fontSize = 12.sp,
                            color = NavyInk,
                        )
                        Text(
                            text = "Lập lúc: ${formatDateTimeVn(ord.createdAtMillis)} · ${ord.status.label}",
                            fontSize = 11.sp,
                            color = MutedSlate,
                        )
                        if (warranty != null) {
                            Text(
                                text = "Bảo hành đến: ${formatDateVn(warranty.expiresAtMillis)} (${warranty.remainingDays.coerceAtLeast(0L)} ngày)",
                                fontSize = 11.sp,
                                color = if (warranty.isExpired) AmberWarn else EmeraldOk,
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = formatVnd(ord.totalAmount),
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.5.sp,
                            color = NavyInk,
                        )
                        Text(
                            text = "Đã thu: ${formatVnd(ord.paidAmount)}",
                            fontSize = 11.5.sp,
                            color = EmeraldOk,
                        )
                        Text(
                            text = "Còn nợ: ${formatVnd(ord.remainingDebt)}",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (ord.remainingDebt > 0L) AmberWarn else MutedSlate,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (ord.remainingDebt > 0L && ord.status != OrderStatus.CANCELLED) {
                        PrimaryZaloButton(
                            text = "Thu tiền / công nợ",
                            onClick = { paymentTargetOrder = ord },
                        )
                    }
                    SecondaryGlassButton(
                        text = "Gửi hóa đơn Zalo",
                        onClick = { onShareInvoiceZalo(ord) },
                    )
                }
            }
        }
    }

    paymentTargetOrder?.let { target ->
        var amountInput by remember(target.id) { mutableLongStateOf(target.remainingDebt) }
        AlertDialog(
            onDismissRequest = { paymentTargetOrder = null },
            title = {
                Text(
                    text = "Thu tiền đơn ${target.orderCode}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = NavyInk,
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Khách: ${target.customerName} · Còn nợ: ${formatVnd(target.remainingDebt)}",
                        fontSize = 12.5.sp,
                        color = MutedSlate,
                    )
                    MoneyInputField(
                        value = amountInput,
                        onValueChange = { amountInput = it },
                        label = "Số tiền thu đợt này",
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { paymentTargetOrder = null }) {
                    Text("Hủy")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val safeAmount = amountInput.coerceAtLeast(0L)
                        if (safeAmount > 0L) {
                            onRecordPayment(target, safeAmount, PaymentMethod.TRANSFER)
                        }
                        paymentTargetOrder = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ZaloBlue),
                ) {
                    Text("Xác nhận thu")
                }
            },
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InventoryScreen(
    state: WorkspaceState,
    onAddMachine: (Machine) -> Unit,
    onAdjustStock: (String, Int) -> Unit,
) {
    var nameInput by remember { mutableStateOf("") }
    var modelInput by remember { mutableStateOf("") }
    var categoryInput by remember { mutableStateOf("Máy công nghiệp") }
    var conditionInput by remember { mutableStateOf(MachineCondition.NEW) }
    var sellPriceInput by remember { mutableLongStateOf(0L) }
    var costPriceInput by remember { mutableLongStateOf(0L) }
    var stockInput by remember { mutableIntStateOf(1) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 8.dp, bottom = 22.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(
                text = "Kho máy & thiết bị (${state.machines.size} dòng)",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = NavyInk,
            )
        }

        item {
            GlassCard {
                Text(
                    text = "Nhập máy mới vào kho",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = NavyInk,
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text("Tên máy") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = modelInput,
                    onValueChange = { modelInput = it },
                    label = { Text("Mã model") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = categoryInput,
                    onValueChange = { categoryInput = it },
                    label = { Text("Nhóm máy") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    MachineCondition.entries.forEach { cond ->
                        SecondaryGlassButton(
                            text = cond.label,
                            textColor = if (conditionInput == cond) ZaloBlueDark else NavyInk,
                            onClick = { conditionInput = cond },
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                MoneyInputField(
                    value = costPriceInput,
                    onValueChange = { costPriceInput = it },
                    label = "Giá vốn nhập",
                )
                MoneyInputField(
                    value = sellPriceInput,
                    onValueChange = { sellPriceInput = it },
                    label = "Giá bán đề xuất",
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    SecondaryGlassButton(
                        text = "- SL",
                        onClick = { stockInput = (stockInput - 1).coerceAtLeast(0) },
                    )
                    Text(
                        text = "Tồn kho: $stockInput",
                        fontWeight = FontWeight.Bold,
                        color = NavyInk,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                    SecondaryGlassButton(
                        text = "+ SL",
                        onClick = { stockInput += 1 },
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                PrimaryZaloButton(
                    text = "Thêm máy vào kho",
                    onClick = {
                        if (nameInput.isNotBlank()) {
                            onAddMachine(
                                Machine(
                                    id = "mac-${System.currentTimeMillis()}",
                                    name = nameInput.trim(),
                                    brand = "Vĩ Lương",
                                    model = modelInput.trim().ifBlank { "VL-NEW" },
                                    category = categoryInput.trim().ifBlank { "Máy công nghiệp" },
                                    condition = conditionInput,
                                    sellPrice = sellPriceInput.coerceAtLeast(0L),
                                    costPrice = costPriceInput.coerceAtLeast(0L),
                                    stockQty = stockInput.coerceAtLeast(0),
                                    warrantyMonths = 12,
                                ),
                            )
                            nameInput = ""
                            modelInput = ""
                            sellPriceInput = 0L
                            costPriceInput = 0L
                            stockInput = 1
                        }
                    },
                )
            }
        }

        items(state.machines, key = { it.id }) { mac ->
            GlassCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${mac.name} (${mac.model})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = NavyInk,
                        )
                        Text(
                            text = "${mac.category} · ${mac.condition.label} · BH ${mac.warrantyMonths} tháng",
                            fontSize = 12.sp,
                            color = MutedSlate,
                        )
                        Text(
                            text = "Giá bán: ${formatVnd(mac.sellPrice)} · Vốn: ${formatVnd(mac.costPrice)}",
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            color = NavyInk,
                        )
                    }
                    Text(
                        text = "Tồn: ${mac.stockQty}",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        color = if (mac.stockQty > 0) EmeraldOk else AmberWarn,
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SecondaryGlassButton(
                        text = "+1 máy",
                        onClick = { onAdjustStock(mac.id, 1) },
                    )
                    SecondaryGlassButton(
                        text = "-1 máy",
                        onClick = { onAdjustStock(mac.id, -1) },
                    )
                }
            }
        }
    }
}
