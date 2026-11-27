package com.viluong.business.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.viluong.business.util.formatVnd

val GlassWhite = Color(0xF2FFFFFF)
val GlassSubWhite = Color(0xD9F7FBFF)
val GlassBorder = Color(0x99FFFFFF)
val NavyInk = Color(0xFF0B1B32)
val MutedSlate = Color(0xFF56677D)
val ZaloBlue = Color(0xFF0068FF)
val ZaloBlueDark = Color(0xFF0047B3)

val EmeraldOk = Color(0xFF0F8A5F)
val EmeraldBg = Color(0xFFE5F7F0)
val AmberWarn = Color(0xFFB45309)
val AmberBg = Color(0xFFFEF3C7)
val AmberBorder = Color(0xFFFCD34D)
val RedAlert = Color(0xFFB91C1C)
val RedBg = Color(0xFFFEE2E2)

object AppIcons {
    val Sparkles: ImageVector = Icons.Outlined.AutoAwesome
    val NavDashboard: ImageVector = Icons.Outlined.Dashboard
    val NavOrders: ImageVector = Icons.Outlined.ReceiptLong
    val NavInventory: ImageVector = Icons.Outlined.Inventory2
    val NavCustomers: ImageVector = Icons.Outlined.People
    val NavQr: ImageVector = Icons.Outlined.AccountBalance
}

private val LightLiquidScheme = lightColorScheme(
    primary = ZaloBlue,
    onPrimary = Color.White,
    secondary = ZaloBlueDark,
    background = Color(0xFFF0F6FF),
    surface = GlassWhite,
    onSurface = NavyInk,
)

@Composable
fun ViLuongTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightLiquidScheme,
        content = content,
    )
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    containerColor: Color = GlassWhite,
    borderColor: Color = GlassBorder,
    contentPadding: PaddingValues = PaddingValues(14.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = containerColor,
        border = BorderStroke(1.dp, borderColor),
        shadowElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(contentPadding),
            content = content,
        )
    }
}

@Composable
fun GlassSubCard(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(10.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = GlassSubWhite,
        border = BorderStroke(1.dp, GlassBorder),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(contentPadding),
            content = content,
        )
    }
}

@Composable
fun PrimaryZaloButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = ZaloBlue,
            contentColor = Color.White,
        ),
    ) {
        Text(text = text, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun SecondaryGlassButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    textColor: Color = NavyInk,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, GlassBorder),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 7.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = GlassWhite,
            contentColor = textColor,
        ),
    ) {
        Text(text = text, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun MoneyInputField(
    value: Long,
    onValueChange: (Long) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
) {
    val displayText = if (value == 0L) "" else value.toString()
    OutlinedTextField(
        value = displayText,
        onValueChange = { raw ->
            val digitsOnly = raw.filter { it.isDigit() }
            onValueChange(digitsOnly.toLongOrNull() ?: 0L)
        },
        label = { Text(label) },
        supportingText = {
            Text(text = formatVnd(value.coerceAtLeast(0L)), fontSize = 11.sp, color = MutedSlate)
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = modifier.fillMaxWidth(),
    )
}
