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
import com.viluong.business.model.BankAccount
import com.viluong.business.model.BankPreset
import com.viluong.business.model.CashflowRecord
import com.viluong.business.model.CashflowType
import com.viluong.business.model.PaymentMethod
import com.viluong.business.model.ReportScope
import com.viluong.business.model.VIETNAM_BANKS
import com.viluong.business.model.WorkspaceState
import com.viluong.business.ops.buildVietQrPayloadText
import com.viluong.business.ui.theme.EmeraldOk
import com.viluong.business.ui.theme.GlassCard
import com.viluong.business.ui.theme.GlassSubCard
import com.viluong.business.ui.theme.MoneyInputField
import com.viluong.business.ui.theme.MutedSlate
import com.viluong.business.ui.theme.NavyInk
import com.viluong.business.ui.theme.PrimaryZaloButton
import com.viluong.business.ui.theme.SecondaryGlassButton
import com.viluong.business.ui.theme.ZaloBlueDark
import com.viluong.business.util.formatDateTimeVn
import com.viluong.business.util.formatVnd
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BankingAndReportScreen(
    state: WorkspaceState,
    onAddBankAccount: (BankAccount) -> Unit,
    onAddCashflow: (CashflowRecord) -> Unit,
    onShareReportZalo: (ReportScope, String) -> Unit,
) {
    val defaultAccount = state.bankAccounts.firstOrNull { it.isDefault } ?: state.bankAccounts.firstOrNull()
    var selectedBank: BankPreset by remember { mutableStateOf(VIETNAM_BANKS.first()) }
    var accountNumberInput by remember { mutableStateOf(defaultAccount?.accountNumber.orEmpty()) }
    var accountNameInput by remember { mutableStateOf(defaultAccount?.accountName ?: "TRAN VI LUONG") }

    var qrAmount by remember { mutableLongStateOf(0L) }
    var qrContent by remember { mutableStateOf("THANH TOAN MAY VI LUONG") }

    var cashflowType by remember { mutableStateOf(CashflowType.EXPENSE) }
    var cashflowTitle by remember { mutableStateOf("") }
    var cashflowAmount by remember { mutableLongStateOf(0L) }

    val currentYearMonth = remember {
        SimpleDateFormat("yyyy-MM", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("Asia/Ho_Chi_Minh")
        }.format(Date())
    }

    val activeAccount = BankAccount(
        id = defaultAccount?.id ?: "acc-active",
        bankCode = selectedBank.code,
        bankName = selectedBank.name,
        bin = selectedBank.bin,
        accountNumber = accountNumberInput.trim().ifBlank { "0071009988776" },
        accountName = accountNameInput.trim().ifBlank { "TRAN VI LUONG" },
        isDefault = true,
    )
    val qrPayloadText = remember(activeAccount, qrAmount, qrContent) {
        buildVietQrPayloadText(activeAccount, qrAmount, qrContent)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 8.dp, bottom = 22.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(
                text = "Thu chi, VietQR & Báo cáo",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = NavyInk,
            )
        }

        item {
            GlassCard {
                Text(
                    text = "Tạo nội dung VietQR & Tài khoản nhận tiền",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = NavyInk,
                )
                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    VIETNAM_BANKS.forEach { bank ->
                        SecondaryGlassButton(
                            text = bank.code,
                            textColor = if (selectedBank.code == bank.code) ZaloBlueDark else NavyInk,
                            onClick = { selectedBank = bank },
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = accountNumberInput,
                    onValueChange = { accountNumberInput = it },
                    label = { Text("Số tài khoản ngân hàng") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = accountNameInput,
                    onValueChange = { accountNameInput = it },
                    label = { Text("Tên chủ tài khoản") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(8.dp))
                MoneyInputField(
                    value = qrAmount,
                    onValueChange = { qrAmount = it },
                    label = "Số tiền VietQR",
                )
                OutlinedTextField(
                    value = qrContent,
                    onValueChange = { qrContent = it },
                    label = { Text("Nội dung chuyển khoản") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(8.dp))
                GlassSubCard {
                    Text(
                        text = qrPayloadText,
                        fontSize = 11.5.sp,
                        fontFamily = FontFamily.Monospace,
                        color = NavyInk,
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PrimaryZaloButton(
                        text = "Lưu tài khoản mặc định",
                        onClick = {
                            onAddBankAccount(
                                activeAccount.copy(id = "acc-${System.currentTimeMillis()}"),
                            )
                        },
                    )
                    SecondaryGlassButton(
                        text = "Gửi báo cáo tháng Zalo",
                        onClick = { onShareReportZalo(ReportScope.OVERVIEW, currentYearMonth) },
                    )
                }
            }
        }

        item {
            GlassCard {
                Text(
                    text = "Ghi nhận thu / chi ngoài đơn",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = NavyInk,
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    CashflowType.entries.forEach { cfType ->
                        SecondaryGlassButton(
                            text = cfType.label,
                            textColor = if (cashflowType == cfType) ZaloBlueDark else NavyInk,
                            onClick = { cashflowType = cfType },
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = cashflowTitle,
                    onValueChange = { cashflowTitle = it },
                    label = { Text("Nội dung thu / chi (VD: Thuê mặt bằng, Vật tư hàn...)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(8.dp))
                MoneyInputField(
                    value = cashflowAmount,
                    onValueChange = { cashflowAmount = it },
                    label = "Số tiền",
                )
                PrimaryZaloButton(
                    text = "Lưu sổ thu chi",
                    onClick = {
                        if (cashflowTitle.isNotBlank() && cashflowAmount > 0L) {
                            onAddCashflow(
                                CashflowRecord(
                                    id = "cf-${System.currentTimeMillis()}",
                                    type = cashflowType,
                                    title = cashflowTitle.trim(),
                                    amount = cashflowAmount,
                                    method = PaymentMethod.CASH,
                                    createdAtMillis = System.currentTimeMillis(),
                                ),
                            )
                            cashflowTitle = ""
                            cashflowAmount = 0L
                        }
                    },
                )
            }
        }

        items(state.bankNotifications, key = { it.id }) { bn ->
            GlassCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${bn.bankName} ·${bn.transferContent}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = NavyInk,
                        )
                        Text(
                            text = formatDateTimeVn(bn.receivedAtMillis),
                            fontSize = 11.sp,
                            color = MutedSlate,
                        )
                    }
                    Text(
                        text = "+${formatVnd(bn.amountIn)}",
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                        color = EmeraldOk,
                    )
                }
            }
        }
    }
}
