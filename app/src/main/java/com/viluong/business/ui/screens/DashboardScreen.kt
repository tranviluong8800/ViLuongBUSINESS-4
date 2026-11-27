package com.viluong.business.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.viluong.business.model.AppTab
import com.viluong.business.model.AssistantMode
import com.viluong.business.model.CashflowType
import com.viluong.business.model.Order
import com.viluong.business.model.OrderStatus
import com.viluong.business.model.ReportScope
import com.viluong.business.model.WorkspaceState
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
import com.viluong.business.ui.theme.ZaloBlueDark
import com.viluong.business.util.formatDateTimeVn
import com.viluong.business.util.formatVnd

@Composable
fun DashboardScreen(
    state: WorkspaceState,
    onCreateOrder: () -> Unit,
    onOpenAssistant: (AssistantMode) -> Unit,
    onShareReportZalo: (ReportScope) -> Unit,
    onSelectOrder: (Order) -> Unit,
    onNavigateTab: (AppTab) -> Unit,
    onOpenSettings: () -> Unit,
) {
    val activeOrders = remember(state.orders) {
        state.orders.filter { !it.isDeleted && it.status != OrderStatus.CANCELLED }
    }
    val totalRevenue = remember(activeOrders) { activeOrders.sumOf { it.totalAmount } }
    val totalPaid = remember(activeOrders) { activeOrders.sumOf { it.paidAmount } }
    val totalDebt = remember(activeOrders) { activeOrders.sumOf { it.remainingDebt } }

    val netProfitEstimate = remember(activeOrders, state.machines, state.cashflows) {
        val costMap = state.machines.associate { it.id to it.costPrice }
        val gross = activeOrders.sumOf { ord ->
            ord.items.sumOf { item ->
                val unitCost = costMap[item.machineId] ?: item.unitPrice
                (item.unitPrice - unitCost) * item.quantity
            }
        }
        val inc = state.cashflows.filter { it.type == CashflowType.INCOME }.sumOf { it.amount }
        val exp = state.cashflows.filter { it.type == CashflowType.EXPENSE }.sumOf { it.amount }
        gross + inc - exp
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 8.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            GlassCard {
                Text(
                    text = "Tổng quan kinh doanh",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    color = NavyInk,
                )
                Text(
                    text = "Chủ cửa hàng: ${state.store.ownerName} · ${state.store.storeAddress}",
                    fontSize = 12.sp,
                    color = MutedSlate,
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    GlassSubCard(modifier = Modifier.weight(1f)) {
                        Text("Doanh thu", fontSize = 11.5.sp, color = MutedSlate)
                        Text(
                            text = formatVnd(totalRevenue),
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp,
                            color = NavyInk,
                        )
                    }
                    GlassSubCard(modifier = Modifier.weight(1f)) {
                        Text("Đã thu", fontSize = 11.5.sp, color = MutedSlate)
                        Text(
                            text = formatVnd(totalPaid),
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp,
                            color = EmeraldOk,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    GlassSubCard(modifier = Modifier.weight(1f)) {
                        Text("Còn nợ", fontSize = 11.5.sp, color = MutedSlate)
                        Text(
                            text = formatVnd(totalDebt),
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp,
                            color = AmberWarn,
                        )
                    }
                    GlassSubCard(modifier = Modifier.weight(1f)) {
                        Text("Lợi nhuận ước tính", fontSize = 11.5.sp, color = MutedSlate)
                        Text(
                            text = formatVnd(netProfitEstimate),
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp,
                            color = ZaloBlueDark,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PrimaryZaloButton(text = "+ Tạo đơn nhanh", onClick = onCreateOrder)
                    SecondaryGlassButton(
                        text = "Trợ lý giọng nói",
                        onClick = { onOpenAssistant(AssistantMode.VOICE_ORDER) },
                    )
                    SecondaryGlassButton(
                        text = "Báo cáo Zalo",
                        onClick = { onShareReportZalo(ReportScope.OVERVIEW) },
                    )
                }
            }
        }

        if (state.bankNotifications.isNotEmpty()) {
            item {
                GlassCard(containerColor = AmberBg, borderColor = AmberBorder) {
                    Text(
                        text = "Có ${state.bankNotifications.size} biến động số dư chờ đối soát",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp,
                        color = AmberWarn,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    SecondaryGlassButton(
                        text = "Mở Thu & QR đối soát",
                        onClick = { onNavigateTab(AppTab.BANKING) },
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Đơn hàng gần đây (${activeOrders.size})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = NavyInk,
                )
                SecondaryGlassButton(
                    text = "Xem tất cả",
                    onClick = { onNavigateTab(AppTab.ORDERS) },
                )
            }
        }

        items(activeOrders.take(5), key = { it.id }) { ord ->
            GlassCard(modifier = Modifier.clickable { onSelectOrder(ord) }) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${ord.orderCode} · ${ord.customerName}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp,
                            color = NavyInk,
                        )
                        Text(
                            text = ord.items.joinToString(", ") { "${it.machineName} x${it.quantity}" },
                            fontSize = 12.sp,
                            color = MutedSlate,
                        )
                        Text(
                            text = formatDateTimeVn(ord.createdAtMillis),
                            fontSize = 11.sp,
                            color = MutedSlate,
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = formatVnd(ord.totalAmount),
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp,
                            color = NavyInk,
                        )
                        Text(
                            text = if (ord.remainingDebt > 0L) "Nợ ${formatVnd(ord.remainingDebt)}" else "Đã thu đủ",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (ord.remainingDebt > 0L) AmberWarn else EmeraldOk,
                        )
                    }
                }
            }
        }
    }
}
