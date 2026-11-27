package com.viluong.business.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.viluong.business.model.Customer
import com.viluong.business.model.Order
import com.viluong.business.model.OrderStatus
import com.viluong.business.model.WorkspaceState
import com.viluong.business.ops.findDuplicatePhoneGroups
import com.viluong.business.ops.openZaloContact
import com.viluong.business.ui.theme.AmberBg
import com.viluong.business.ui.theme.AmberBorder
import com.viluong.business.ui.theme.AmberWarn
import com.viluong.business.ui.theme.EmeraldOk
import com.viluong.business.ui.theme.GlassCard
import com.viluong.business.ui.theme.GlassSubCard
import com.viluong.business.ui.theme.MutedSlate
import com.viluong.business.ui.theme.NavyInk
import com.viluong.business.ui.theme.PrimaryZaloButton
import com.viluong.business.ui.theme.SecondaryGlassButton
import com.viluong.business.util.formatVnd

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CustomersScreen(
    state: WorkspaceState,
    canUndoMerge: Boolean,
    onAddCustomer: (String, String, String, String) -> Unit,
    onMergeDuplicatePhone: (String) -> Unit,
    onUndoMerge: () -> Unit,
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var nameInput by remember { mutableStateOf("") }
    var phoneInput by remember { mutableStateOf("") }
    var addressInput by remember { mutableStateOf("") }
    var noteInput by remember { mutableStateOf("") }

    val duplicateGroups = remember(state.customers) {
        findDuplicatePhoneGroups(state.customers)
    }

    val filteredCustomers = remember(state.customers, searchQuery) {
        val q = searchQuery.trim().lowercase()
        if (q.isBlank()) {
            state.customers
        } else {
            state.customers.filter { c ->
                c.name.lowercase().contains(q) ||
                    c.phone.lowercase().contains(q) ||
                    c.workshopAddress.lowercase().contains(q)
            }
        }
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
                    text = "Khách hàng & xưởng (${state.customers.size})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = NavyInk,
                )
                if (canUndoMerge) {
                    SecondaryGlassButton(
                        text = "Hoàn tác gộp",
                        onClick = onUndoMerge,
                    )
                }
            }
        }

        if (duplicateGroups.isNotEmpty()) {
            item {
                GlassCard(containerColor = AmberBg, borderColor = AmberBorder) {
                    Text(
                        text = "Phát hiện ${duplicateGroups.size} nhóm trùng số điện thoại",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp,
                        color = AmberWarn,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    duplicateGroups.forEach { grp ->
                        GlassSubCard {
                            Text(
                                text = "SĐT ${grp.phoneDigits} (${grp.customers.size} hồ sơ)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.5.sp,
                                color = NavyInk,
                            )
                            Text(
                                text = grp.customers.joinToString(" · ") { it.name },
                                fontSize = 11.5.sp,
                                color = MutedSlate,
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            PrimaryZaloButton(
                                text = "Gộp hồ sơ trùng SĐT",
                                onClick = { onMergeDuplicatePhone(grp.phoneDigits) },
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }
            }
        }

        item {
            GlassCard {
                Text(
                    text = "Thêm khách hàng mới",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = NavyInk,
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text("Tên khách hàng / xưởng") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = phoneInput,
                    onValueChange = { phoneInput = it },
                    label = { Text("Số điện thoại / Zalo") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = addressInput,
                    onValueChange = { addressInput = it },
                    label = { Text("Địa chỉ xưởng") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = noteInput,
                    onValueChange = { noteInput = it },
                    label = { Text("Ghi chú") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(8.dp))
                PrimaryZaloButton(
                    text = "Lưu khách hàng",
                    onClick = {
                        if (nameInput.isNotBlank()) {
                            onAddCustomer(
                                nameInput.trim(),
                                phoneInput.trim(),
                                addressInput.trim(),
                                noteInput.trim(),
                            )
                            nameInput = ""
                            phoneInput = ""
                            addressInput = ""
                            noteInput = ""
                        }
                    },
                )
            }
        }

        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Tìm theo tên, SĐT hoặc địa chỉ xưởng") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        items(filteredCustomers, key = { it.id }) { cust ->
            CustomerItemCard(
                customer = cust,
                orders = state.orders,
                onOpenZalo = { openZaloContact(context, cust.phone) },
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CustomerItemCard(
    customer: Customer,
    orders: List<Order>,
    onOpenZalo: () -> Unit,
) {
    val customerOrders = remember(orders, customer.id) {
        orders.filter { !it.isDeleted && it.status != OrderStatus.CANCELLED && it.customerId == customer.id }
    }
    val totalBought = remember(customerOrders) { customerOrders.sumOf { it.totalAmount } }
    val totalDebt = remember(customerOrders) { customerOrders.sumOf { it.remainingDebt } }

    GlassCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = customer.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = NavyInk,
                )
                if (customer.phone.isNotBlank()) {
                    Text("SĐT: ${customer.phone}", fontSize = 12.sp, color = MutedSlate)
                }
                if (customer.workshopAddress.isNotBlank()) {
                    Text("Xưởng: ${customer.workshopAddress}", fontSize = 12sp, color = MutedSlate)
                }
                if (customer.notes.isNotBlank()) {
                    Text("Ghi chú: ${customer.notes}", fontSize = 11.5.sp, color = MutedSlate)
                }
            }
            SecondaryGlassButton(text = "Mở Zalo", onClick = onOpenZalo)
        }

        Spacer(modifier = Modifier.height(8.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text("Đơn mua: ${customerOrders.size}", fontSize = 12.sp, color = NavyInk)
            Text("Tổng mua: ${formatVnd(totalBought)}", fontSize = 12.sp, color = EmeraldOk)
            Text(
                text = "Còn nợ: ${formatVnd(totalDebt)}",
                fontSize = 12.sp,
                color = if (totalDebt > 0L) AmberWarn else MutedSlate,
            )
        }
    }
}
