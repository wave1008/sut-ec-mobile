package com.sutec.mobile.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sutec.mobile.data.model.User
import com.sutec.mobile.designsystem.component.AppFilterChip
import com.sutec.mobile.designsystem.component.AppTopBar
import com.sutec.mobile.designsystem.component.PrimaryButton
import com.sutec.mobile.designsystem.component.SecondaryButton
import com.sutec.mobile.designsystem.extraColors
import com.sutec.mobile.designsystem.spacing
import com.sutec.mobile.i18n.AppLanguage
import com.sutec.mobile.i18n.tr
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(
    onLogin: () -> Unit,
    onOrders: () -> Unit,
    onWishlist: () -> Unit,
    onAddresses: () -> Unit,
    onPayments: () -> Unit,
    viewModel: AccountViewModel = koinViewModel(),
) {
    val user by viewModel.currentUser.collectAsStateWithLifecycle()
    val language by viewModel.language.collectAsStateWithLifecycle()
    val spacing = MaterialTheme.spacing

    Scaffold(
        modifier = Modifier.testTag("screen_account"),
        topBar = { AppTopBar(title = tr("アカウント", "Account")) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = spacing.screenH),
        ) {
            Spacer(Modifier.height(spacing.lg))
            AccountHeader(user = user, onLogin = onLogin)

            Spacer(Modifier.height(spacing.xl))
            MenuRow(Icons.AutoMirrored.Outlined.ReceiptLong, tr("注文履歴", "Order history"), onOrders, testTag = "btn_orders")
            MenuRow(Icons.Outlined.FavoriteBorder, tr("お気に入り", "Wishlist"), onWishlist)
            MenuRow(Icons.Outlined.LocationOn, tr("お届け先住所", "Addresses"), onAddresses, testTag = "btn_addresses")
            MenuRow(Icons.Outlined.CreditCard, tr("お支払い方法", "Payment methods"), onPayments, testTag = "btn_payments")

            Spacer(Modifier.height(spacing.xl))
            Text(
                text = tr("言語", "Language"),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.extraColors.onSurfaceFaint,
            )
            Spacer(Modifier.height(spacing.sm))
            Row(horizontalArrangement = Arrangement.spacedBy(spacing.sm)) {
                // 言語選択ラベル自体は選択対象言語の表記であり、現在表示言語(tr)には従わない。
                AppFilterChip(
                    selected = language == AppLanguage.JA,
                    label = "日本語",
                    onClick = { viewModel.setLanguage(AppLanguage.JA) },
                    modifier = Modifier.testTag("btn_toggle_language_ja"),
                )
                AppFilterChip(
                    selected = language == AppLanguage.EN,
                    label = "English",
                    onClick = { viewModel.setLanguage(AppLanguage.EN) },
                    modifier = Modifier.testTag("btn_toggle_language_en"),
                )
            }

            if (user != null) {
                Spacer(Modifier.height(spacing.xl))
                SecondaryButton(text = tr("ログアウト", "Log out"), onClick = viewModel::logout, modifier = Modifier.testTag("btn_logout"))
            }
            Spacer(Modifier.height(spacing.lg))
        }
    }
}

@Composable
private fun AccountHeader(user: User?, onLogin: () -> Unit) {
    val spacing = MaterialTheme.spacing
    if (user == null) {
        PrimaryButton(text = tr("ログイン / 登録", "Log in / Sign up"), onClick = onLogin, modifier = Modifier.testTag("btn_login"))
        return
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = user.name.firstOrNull()?.uppercase() ?: "",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimary,
            )
        }
        Spacer(Modifier.width(spacing.md))
        Column {
            Text(user.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(user.email, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.extraColors.onSurfaceFaint)
        }
    }
}

@Composable
private fun MenuRow(icon: ImageVector, label: String, onClick: () -> Unit, testTag: String? = null) {
    val spacing = MaterialTheme.spacing
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = spacing.md)
            .let { if (testTag != null) it.testTag(testTag) else it },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(spacing.md))
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = MaterialTheme.extraColors.onSurfaceFaint)
    }
}
