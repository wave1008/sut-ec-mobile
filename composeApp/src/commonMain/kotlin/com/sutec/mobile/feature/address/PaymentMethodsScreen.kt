package com.sutec.mobile.feature.address

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sutec.mobile.data.model.PaymentMethod
import com.sutec.mobile.data.model.PaymentType
import com.sutec.mobile.designsystem.component.AppTextButton
import com.sutec.mobile.designsystem.component.AppTopBar
import com.sutec.mobile.designsystem.component.EmptyState
import com.sutec.mobile.designsystem.extraColors
import com.sutec.mobile.designsystem.spacing
import com.sutec.mobile.i18n.tr
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentMethodsScreen(
    onBack: () -> Unit,
    onAddNew: () -> Unit,
    onEdit: (String) -> Unit,
    viewModel: PaymentMethodsViewModel = koinViewModel(),
) {
    val paymentMethods by viewModel.paymentMethods.collectAsStateWithLifecycle()
    val spacing = MaterialTheme.spacing

    Scaffold(
        topBar = {
            AppTopBar(
                title = tr("お支払い方法", "Payment methods"),
                onBack = onBack,
                actions = {
                    IconButton(onClick = onAddNew) {
                        Icon(Icons.Filled.Add, contentDescription = tr("追加", "Add"))
                    }
                },
            )
        },
    ) { padding ->
        if (paymentMethods.isEmpty()) {
            Box(modifier = Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                EmptyState(
                    icon = Icons.Outlined.CreditCard,
                    title = tr("登録済みのお支払い方法がありません", "No payment methods yet"),
                    message = tr("新しいお支払い方法を追加してください", "Add a payment method to get started"),
                    actionLabel = tr("お支払い方法を追加", "Add payment method"),
                    onAction = onAddNew,
                )
            }
            return@Scaffold
        }
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(horizontal = spacing.screenH, vertical = spacing.md),
            verticalArrangement = Arrangement.spacedBy(spacing.md),
        ) {
            items(paymentMethods, key = { it.id }) { payment ->
                PaymentCard(
                    payment = payment,
                    onEdit = { onEdit(payment.id) },
                    onDelete = { viewModel.delete(payment.id) },
                    onSetDefault = { viewModel.setDefault(payment.id) },
                )
            }
        }
    }
}

@Composable
private fun PaymentCard(
    payment: PaymentMethod,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onSetDefault: () -> Unit,
) {
    val spacing = MaterialTheme.spacing
    val extraColors = MaterialTheme.extraColors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, extraColors.cardBorder, MaterialTheme.shapes.large)
            .padding(spacing.md),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (payment.type == PaymentType.CARD) Icons.Outlined.CreditCard else Icons.Outlined.Payments,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.width(spacing.sm))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (payment.type == PaymentType.CARD) {
                        "${payment.brand} •••• ${payment.last4}"
                    } else {
                        tr("代金引換", "Cash on delivery")
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                if (payment.type == PaymentType.CARD) {
                    Text(
                        text = "${payment.holderName}  ${payment.expMonth.toString().padStart(2, '0')}/${payment.expYear}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = extraColors.onSurfaceFaint,
                    )
                }
            }
            if (payment.isDefault) DefaultBadge()
        }
        Spacer(Modifier.height(spacing.sm))
        Row(horizontalArrangement = Arrangement.spacedBy(spacing.xs)) {
            AppTextButton(text = tr("編集", "Edit"), onClick = onEdit)
            AppTextButton(text = tr("削除", "Delete"), onClick = onDelete)
            if (!payment.isDefault) {
                AppTextButton(text = tr("既定に設定", "Set as default"), onClick = onSetDefault)
            }
        }
    }
}
