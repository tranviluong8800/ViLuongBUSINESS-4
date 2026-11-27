package com.viluong.business.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.viluong.business.model.AssistantMode
import com.viluong.business.model.CashflowType
import com.viluong.business.model.OrderStatus
import com.viluong.business.model.VoiceWakeConfig
import com.viluong.business.model.WorkspaceState
import com.viluong.business.ui.theme.GlassSubCard
import com.viluong.business.ui.theme.MutedSlate
import com.viluong.business.ui.theme.NavyInk
import com.viluong.business.ui.theme.PrimaryZaloButton
import com.viluong.business.ui.theme.SecondaryGlassButton
import com.viluong.business.ui.theme.ZaloBlueDark
import com.viluong.business.util.formatVnd

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VoiceAssistantSheetDialog(
    mode: AssistantMode,
    state: WorkspaceState,
    voiceConfig: VoiceWakeConfig,
    onDismiss: () -> Unit,
    onUpdateVoiceConfig: (VoiceWakeConfig) -> Unit,
) {
    var selectedMode by remember(mode) { mutableStateOf(mode) }
    var promptText by remember { mutableStateOf("") }

    val activeOrders = remember(state.orders) {
        state.orders.filter { !it.isDeleted && it.status != OrderStatus.CANCELLED }
    }
    val totalRevenue = remember(activeOrders) { activeOrders.sumOf { it.totalAmount } }
    val totalDebt = remember(activeOrders) { activeOrders.sumOf { it.remainingDebt } }
    val profitEstimate = remember(activeOrders, state.machines, state.cashflows) {
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

    val summaryAnswer = remember(promptText, totalRevenue, totalDebt, profitEstimate, voiceConfig) {
        val q = promptText.trim().lowercase()
        when {
            q.contains("lợi nhuận") || q.contains("lãi") ->
                "Lợi nhuận ước tính hiện tại: ${formatVnd(profitEstimate)}."
            q.contains("nợ") || q.contains("công nợ") ->
                "Tổng công nợ khách hàng còn lại: ${formatVnd(totalDebt)}."
            q.contains("doanh thu") ->
                "Tổng doanh thu đơn hiệu lực: ${formatVnd(totalRevenue)}."
            else ->
                "Doanh thu ${formatVnd(totalRevenue)} · Còn nợ ${formatVnd(totalDebt)} · Lợi nhuận ước tính ${formatVnd(profitEstimate)}. Từ khóa đánh thức: \"${voiceConfig.wakeWord}\"."
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Trợ lý VĨ LƯƠNG BUSINESS",
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
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    AssistantMode.entries.forEach { m ->
                        SecondaryGlassButton(
                            text = m.label,
                            textColor = if (selectedMode == m) ZaloBlueDark else NavyInk,
                            onClick = { selectedMode = m },
                        )
                    }
                }

                OutlinedTextField(
                    value = promptText,
                    onValueChange = { promptText = it },
                    label = { Text("Nhập câu hỏi hoặc lệnh nhanh") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                GlassSubCard {
                    Text(
                        text = summaryAnswer,
                        fontSize = 12.5.sp,
                        color = NavyInk,
                    )
                }

                Text(
                    text = "Chế độ phản hồi giọng nói: ${if (voiceConfig.speakReply) "Bật" else "Tắt"}",
                    fontSize = 11.5.sp,
                    color = MutedSlate,
                )
                PrimaryZaloButton(
                    text = if (voiceConfig.speakReply) "Tắt đọc phản hồi" else "Bật đọc phản hồi",
                    onClick = {
                        onUpdateVoiceConfig(voiceConfig.copy(speakReply = !voiceConfig.speakReply))
                    },
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Đóng")
            }
        },
    )
}
