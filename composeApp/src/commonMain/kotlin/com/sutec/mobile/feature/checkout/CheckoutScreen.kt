package com.sutec.mobile.feature.checkout

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sutec.mobile.data.model.Address
import com.sutec.mobile.data.model.CartItem
import com.sutec.mobile.data.model.PaymentMethod
import com.sutec.mobile.designsystem.component.AppTopBar
import com.sutec.mobile.designsystem.component.AsyncProductImage
import com.sutec.mobile.designsystem.component.KeyValueRow
import com.sutec.mobile.designsystem.component.PrimaryButton
import com.sutec.mobile.designsystem.component.SecondaryButton
import com.sutec.mobile.designsystem.component.SectionHeader
import com.sutec.mobile.designsystem.extraColors
import com.sutec.mobile.designsystem.spacing
import com.sutec.mobile.i18n.AppLanguage
import com.sutec.mobile.i18n.LocalAppLanguage
import com.sutec.mobile.i18n.tr
import com.sutec.mobile.util.formatYen
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    onBack: () -> Unit,
    onPlaced: (String) -> Unit,
    onManageAddresses: () -> Unit,
    onManagePayments: () -> Unit,
    viewModel: CheckoutViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lang = LocalAppLanguage.current

    LaunchedEffect(uiState.placedOrderId) {
        uiState.placedOrderId?.let(onPlaced)
    }

    Scaffold(
        topBar = { AppTopBar(title = tr("ご注文手続き", "Checkout"), onBack = onBack) },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(MaterialTheme.spacing.md),
            ) {
                PrimaryButton(
                    text = tr("注文を確定する", "Place order"),
                    onClick = viewModel::placeOrder,
                    loading = uiState.placing,
                    enabled = !uiState.placing &&
                        uiState.selectedAddressId != null &&
                        uiState.selectedPaymentId != null,
                )
            }
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(
                horizontal = MaterialTheme.spacing.screenH,
                vertical = MaterialTheme.spacing.md,
            ),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
        ) {
            item {
                SectionHeader(
                    title = tr("お届け先", "Shipping address"),
                    actionLabel = tr("管理", "Manage"),
                    onAction = onManageAddresses,
                )
            }
            if (uiState.addresses.isEmpty()) {
                item {
                    NoOptionRow(
                        message = tr("お届け先が登録されていません", "No shipping address yet"),
                        actionLabel = tr("住所を追加", "Add address"),
                        onAction = onManageAddresses,
                    )
                }
            } else {
                items(uiState.addresses, key = { it.id }) { address ->
                    AddressOptionRow(
                        address = address,
                        selected = address.id == uiState.selectedAddressId,
                        onSelect = { viewModel.selectAddress(address.id) },
                    )
                }
            }

            item { Spacer(Modifier.height(MaterialTheme.spacing.md)) }

            item {
                SectionHeader(
                    title = tr("お支払い方法", "Payment"),
                    actionLabel = tr("管理", "Manage"),
                    onAction = onManagePayments,
                )
            }
            if (uiState.payments.isEmpty()) {
                item {
                    NoOptionRow(
                        message = tr("お支払い方法が登録されていません", "No payment method yet"),
                        actionLabel = tr("支払い方法を追加", "Add payment method"),
                        onAction = onManagePayments,
                    )
                }
            } else {
                items(uiState.payments, key = { it.id }) { payment ->
                    PaymentOptionRow(
                        payment = payment,
                        selected = payment.id == uiState.selectedPaymentId,
                        onSelect = { viewModel.selectPayment(payment.id) },
                    )
                }
            }

            item { Spacer(Modifier.height(MaterialTheme.spacing.md)) }

            item { SectionHeader(title = tr("注文内容", "Order items")) }
            items(uiState.items, key = { it.product.id }) { cartItem ->
                OrderItemRow(item = cartItem, lang = lang)
            }

            item { Spacer(Modifier.height(MaterialTheme.spacing.md)) }

            item {
                Column {
                    KeyValueRow(label = tr("小計", "Subtotal"), value = formatYen(uiState.totals.subtotalYen))
                    Spacer(Modifier.height(MaterialTheme.spacing.xs))
                    KeyValueRow(
                        label = tr("送料", "Shipping"),
                        value = if (uiState.totals.shippingYen == 0) tr("無料", "Free") else formatYen(uiState.totals.shippingYen),
                    )
                    Spacer(Modifier.height(MaterialTheme.spacing.xs))
                    KeyValueRow(label = tr("合計", "Total"), value = formatYen(uiState.totals.totalYen), emphasize = true)
                }
            }
        }
    }
}

@Composable
private fun NoOptionRow(message: String, actionLabel: String, onAction: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = MaterialTheme.spacing.xs)) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.extraColors.onSurfaceFaint,
        )
        Spacer(Modifier.height(MaterialTheme.spacing.sm))
        SecondaryButton(text = actionLabel, onClick = onAction, fillMaxWidth = false)
    }
}

@Composable
private fun AddressOptionRow(address: Address, selected: Boolean, onSelect: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect)
            .padding(vertical = MaterialTheme.spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = onSelect)
        Spacer(Modifier.width(MaterialTheme.spacing.sm))
        Column {
            Text(text = address.fullName, style = MaterialTheme.typography.titleSmall)
            val line2 = if (address.line2.isNotBlank()) " ${address.line2}" else ""
            Text(
                text = "${address.postalCode} ${address.prefecture}${address.city}${address.line1}$line2",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.extraColors.onSurfaceFaint,
            )
        }
    }
}

@Composable
private fun PaymentOptionRow(payment: PaymentMethod, selected: Boolean, onSelect: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect)
            .padding(vertical = MaterialTheme.spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = onSelect)
        Spacer(Modifier.width(MaterialTheme.spacing.sm))
        Text(text = paymentDisplayLabel(payment), style = MaterialTheme.typography.titleSmall)
    }
}

@Composable
private fun OrderItemRow(item: CartItem, lang: AppLanguage) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = MaterialTheme.spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AsyncProductImage(
            url = item.product.imageUrls.firstOrNull(),
            contentDescription = item.product.name(lang),
            modifier = Modifier.width(48.dp),
            aspectRatio = 1f,
        )
        Spacer(Modifier.width(MaterialTheme.spacing.sm))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.product.name(lang),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "×${item.quantity}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.extraColors.onSurfaceFaint,
            )
        }
        Text(
            text = formatYen(item.lineTotalYen),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
        )
    }
}
