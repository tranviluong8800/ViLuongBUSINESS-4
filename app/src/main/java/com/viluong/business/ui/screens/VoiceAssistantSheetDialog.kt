package com.viluong.business.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.viluong.business.model.AssistantMode
import com.viluong.business.model.WorkspaceState
import com.viluong.business.ui.theme.NavyInk
import com.viluong.business.ui.theme.ZaloBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceAssistantSheetDialog(
    mode: AssistantMode,
    state: WorkspaceState,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var promptText by remember { mutableStateOf("") }
    var replyText by remember {
        mutableStateOf(
            "Xin chào ${state.store.ownerName}. Đang hỗ trợ ${state.store.storeName} (${mode.name}).",
        )
    }
    var wakeWordEnabled by remember { mutableStateOf(true) }
    var wakeWordPhrase by remember { mutableStateOf("Hey Vĩ Lương") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "Trợ lý giọng nói & điều hành",
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = NavyInk,
            )
            Text(
                text = replyText,
                style = MaterialTheme.typography.bodyMedium,
                color = NavyInk,
            )

            OutlinedTextField(
                value = promptText,
                onValueChange = { promptText = it },
                label = { Text("Nhập câu hỏi hoặc lệnh nhanh") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = false,
            )

            Button(
                onClick = {
                    val q = promptText.trim()
                    replyText = if (q.isBlank()) {
                        "Hiện có ${state.orders.size} đơn hàng và ${state.machines.size} dòng máy."
                    } else {
                        "Đã nhận lệnh: \"$q\". Tổng đơn: ${state.orders.size}, tổng máy: ${state.machines.size}."
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ZaloBlue),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Hỏi trợ lý")
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "Kích hoạt từ khóa đánh thức",
                    fontWeight = FontWeight.Medium,
                    color = NavyInk,
                )
                Switch(
                    checked = wakeWordEnabled,
                    onCheckedChange = { checked -> wakeWordEnabled = checked },
                )
            }

            OutlinedTextField(
                value = wakeWordPhrase,
                onValueChange = { phrase -> wakeWordPhrase = phrase },
                label = { Text("Từ khóa đánh thức") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Đóng")
                }
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = ZaloBlue),
                ) {
                    Text("Lưu cấu hình")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
