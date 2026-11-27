package com.viluong.business

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.viluong.business.data.WorkspaceJsonStore
import com.viluong.business.model.AppTab
import com.viluong.business.model.AssistantMode
import com.viluong.business.model.Customer
import com.viluong.business.model.Order
import com.viluong.business.model.OrderStatus
import com.viluong.business.model.PaymentRecord
import com.viluong.business.model.ReportScope
import com.viluong.business.model.StoreSettings
import com.viluong.business.model.WorkspaceState
import com.viluong.business.ops.buildMonthlyReportText
import com.viluong.business.ops.buildOrderInvoiceText
import com.viluong.business.ops.mergeDuplicateCustomersByPhone
import com.viluong.business.ui.screens.BankingAndReportScreen
import com.viluong.business.ui.screens.CreateOrderDialogWithAddressSuggest
import com.viluong.business.ui.screens.CustomersScreen
import com.viluong.business.ui.screens.DashboardScreen
import com.viluong.business.ui.screens.InventoryScreen
import com.viluong.business.ui.screens.OrdersScreen
import com.viluong.business.ui.screens.VoiceAssistantSheetDialog
import com.viluong.business.ui.theme.AppIcons
import com.viluong.business.ui.theme.GlassWhite
import com.viluong.business.ui.theme.MutedSlate
import com.viluong.business.ui.theme.NavyInk
import com.viluong.business.ui.theme.SecondaryGlassButton
import com.viluong.business.ui.theme.ViLuongTheme
import com.viluong.business.ui.theme.ZaloBlue
import com.viluong.business.ui.theme.ZaloBlueDark
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val store = WorkspaceJsonStore(applicationContext)
        val initialState = store.loadOrSeed()

        setContent {
            ViLuongTheme {
                ViLuongBusinessApp(
                    initialState = initialState,
                    onPersistState = { nextState -> store.save(nextState) },
                )
            }
        }
    }
}

private fun shareTextViaZaloOrChooser(context: Context, title: String, body: String) {
    val zaloIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        setPackage("com.zing.zalo")
        putExtra(Intent.EXTRA_SUBJECT, title)
        putExtra(Intent.EXTRA_TEXT, body)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    if (zaloIntent.resolveActivity(context.packageManager) != null) {
        runCatching {
            context.startActivity(zaloIntent)
        }.onFailure {
            Toast.makeText(context, "Không mở được Zalo trực tiếp", Toast.LENGTH_SHORT).show()
        }
        return
    }

    val chooser = Intent.createChooser(
        Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_TEXT, body)
        },
        title,
    ).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    runCatching {
        context.startActivity(chooser)
    }.onFailure {
        Toast.makeText(context, "Không tìm thấy ứng dụng chia sẻ phù hợp", Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun ViLuongBusinessApp(
    initialState: WorkspaceState,
    onPersistState: (WorkspaceState) -> Unit,
) {
    val context = LocalContext.current
    var state by remember { mutableStateOf(initialState) }
    var previousMergeBackup by remember { mutableStateOf<WorkspaceState?>(null) }
    var activeTab by remember { mutableStateOf(AppTab.DASHBOARD) }
    var assistantMode by remember { mutableStateOf<AssistantMode?>(null) }
    var showCreateOrder by remember { mutableStateOf(false) }
    var showStoreSettings by remember { mutableStateOf(false) }

    fun updateState(transform: (WorkspaceState) -> WorkspaceState) {
        val next = transform(state)
        state = next
        onPersistState(next)
    }

    LaunchedEffect(state) {
        onPersistState(state)
    }

    val currentYearMonth = remember {
        SimpleDateFormat("yyyy-MM", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("Asia/Ho_Chi_Minh")
        }.format(Date())
    }

    val liquidBackground = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFEAF3FF),
            Color(0xFFF6FAFF),
            Color(0xFFE5EEFF),
        ),
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(liquidBackground),
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = state.store.storeName,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = NavyInk,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = "${state.store.ownerName} •${state.store.ownerRoleLabel}",
                            fontSize = 11.5.sp,
                            color = MutedSlate,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 8.dp),
                    ) {
                        SecondaryGlassButton(
                            text = "Trợ lý",
                            onClick = { assistantMode = AssistantMode.CHAT },
                        )
                        SecondaryGlassButton(
                            text = "Cài đặt",
                            onClick = { showStoreSettings = true },
                            modifier = Modifier.padding(start = 6.dp),
                        )
                    }
                }
            },
            bottomBar = {
                NavigationBar(
                    containerColor = GlassWhite,
                    tonalElevation = 0.dp,
                ) {
                    AppTab.entries.forEach { tab ->
                        val selected = activeTab == tab
                        val icon = when (tab) {
                            AppTab.DASHBOARD -> AppIcons.NavDashboard
                            AppTab.ORDERS -> AppIcons.Nav
                            
