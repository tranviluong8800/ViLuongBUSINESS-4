package com.viluong.business.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.viluong.business.model.Customer
import com.viluong.business.model.Machine
import com.viluong.business.model.Order
import com.viluong.business.model.OrderItem
import com.viluong.business.model.OrderStatus
import com.viluong.business.model.WorkspaceState
import com.viluong.business.ops.suggestHcmcAddresses
import com.viluong.business.ui.theme.MoneyInputField
import com.viluong.business.ui.theme.MutedSlate
import com.viluong.business.ui.theme.NavyInk
import com.viluong.business.ui.theme.SecondaryGlassButton
import com.viluong.business.ui.theme.ZaloBlue
import com.viluong.business.ui.theme.ZaloBlueDark
import com.viluong.business.util.formatVnd

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CreateOrderDialogWithAddressSuggest(
    state: WorkspaceState,
    onDismiss: () -> Unit,
    onCreateOrder: (Order) -> Unit,
) {
    val firstCustomer: Customer? = state.customers.firstOrNull()
    val firstMachine: Machine? = state.machines.firstOrNull()

    var selectedCustomerId by remember { mutableStateOf(firstCustomer?.id.orEmpty()) }
    var customerName by remember { mutableStateOf(firstCustomer?.name.orEmpty()) }
    var customerPhone by remember { mutableStateOf(firstCustomer?.phone.orEmpty()) }
    var workshopAddress by remember { mutableStateOf(firstCustomer?.workshopAddress.orEmpty()) }

    var selectedMachine by remember { mutableStateOf(firstMachine) }
    var quantity by remember { mutableIntStateOf(1) }
    var unitPrice by remember { mutableLongStateOf(firstMachine?.sellPrice ?: 0L) }
    var shippingFee by remember { mutableLongStateOf(0L) }
    var paidAmount by remember { mutableLongStateOf(0L) }
    var notes by remember { mutableStateOf("") }

    val addressSuggestions = remember(workshopAddress) {
        suggestHcmcAddresses(workshopAddress)
    }

    val totalAmount = (unitPrice.coerceAtLeast(0L) * quantity.coerceAtLeast(1)) + shippingFee.coerceAtLeast(0L)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Tạo đơn hàng mới",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = NavyInk,
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (state.customers.isNotEmpty()) {
                    Text("Chọn khách hàng có sẵn:", fontSize = 11.5.sp, color = MutedSlate)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        state.customers.take(4).forEach { cust ->
                            val picked = cust.id == selectedCustomerId
                            SecondaryGlassButton(
                                text = cust.name,
                                textColor = if (picked) ZaloBlueDark else NavyInk,
                                onClick = {
                                    selectedCustomerId = cust.id
                                    customerName = cust.name
                                    customerPhone = cust.phone
                                    if (cust.workshopAddress.isNotBlank()) {
                                        workshopAddress = cust.workshopAddress
                                    }
                                },
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = customerName,
                    onValueChange = { customerName = it },
                    label = { Text("Tên khách hàng / xưởng") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = customerPhone,
                    onValueChange = { customerPhone = it },
                    label = { Text("Số điện thoại") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = workshopAddress,
                    onValueChange = { workshopAddress = it },
                    label = { Text("Địa chỉ xưởng (gõ tên đường TP.HCM)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                Text("Gợi ý địa chỉ TP.HCM:", fontSize = 11.sp, color = MutedSlate)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    addressSuggestions.take(3).forEach { suggestion ->
                        SecondaryGlassButton(
                            text = suggestion.substringBefore(","),
                            onClick = { workshopAddress = suggestion },
                        )
                    }
                }

                if (state.machines.isNotEmpty()) {
                    Text("Chọn máy trong kho:", fontSize = 11.5.sp, color = MutedSlate)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        state.machines.forEach { mac ->
                            val picked = mac.id == selectedMachine?.id
                            SecondaryGlassButton(
                                text = mac.model.ifBlank { mac.name },
                                textColor = if (picked) ZaloBlueDark else NavyInk,
                                onClick = {
                                    selectedMachine = mac
                                    unitPrice = mac.sellPrice
                                },
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    SecondaryGlassButton(
                        text = "- SL",
                        onClick = { quantity = (quantity - 1).coerceAtLeast(1) },
                    )
                    Text(
                        text = "Số lượng: $quantity",
                        fontWeight = FontWeight.Bold,
                        color = NavyInk,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                    SecondaryGlassButton(
                        text = "+ SL",
                        onClick = { quantity += 1 },
                    )
                }

                MoneyInputField(value = unitPrice, onValueChange = { unitPrice = it }, label = "Đơn giá bán")
                MoneyInputField(value = shippingFee, onValueChange = { shippingFee = it }, label = "Phí vận chuyển")
                MoneyInputField(value = paidAmount, onValueChange = { paidAmount = it }, label = "Đã thanh toán / cọc")

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Ghi chú đơn hàng") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Tổng tiền đơn: ${formatVnd(totalAmount)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.5.sp,
                    color = ZaloBlueDark,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy")
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cleanCustomer = customerName.trim().ifBlank { "Khách lẻ" }
                    val mac = selectedMachine
                    val item = OrderItem(
                        machineId = mac?.id.orEmpty(),
                        machineName = mac?.name ?: "Máy thiết bị công nghiệp",
                        brand = mac?.brand ?: "Vĩ Lương",
                        model = mac?.model ?: "VL-STD",
                        condition = mac?.condition ?: com.viluong.business.model.MachineCondition.NEW,
                        quantity = quantity.coerceAtLeast(1),
                        unitPrice = unitPrice.coerceAtLeast(0L),
                        warrantyMonths = mac?.warrantyMonths ?: 12,
                    )
                    val safePaid = paidAmount.coerceAtLeast(0L)
                    val computedStatus = if (safePaid >= totalAmount && totalAmount > 0L) {
                        OrderStatus.COMPLETED
                    } else {
                        OrderStatus.DEBT
                    }
                    val orderNumber = 101 + state.orders.size
                    val newOrder = Order(
                        id = "ord-${System.currentTimeMillis()}",
                        orderCode = "VL-2026-$orderNumber",
                        customerId = selectedCustomerId.ifBlank { "cust-${System.currentTimeMillis()}" },
                        customerName = cleanCustomer,
                        customerPhone = customerPhone.trim(),
                        workshopAddress = workshopAddress.trim(),
                        items = listOf(item),
                        shippingFee = shippingFee.coerceAtLeast(0L),
                        depositAmount = safePaid,
                        paidAmount = safePaid,
                        status = computedStatus,
                        notes = notes.trim(),
                        createdAtMillis = System.currentTimeMillis(),
                    )
                    onCreateOrder(newOrder)
                },
                colors = ButtonDefaults.buttonColors(containerColor = ZaloBlue),
            ) {
                Text("Lưu đơn")
            }
        },
    )
}
