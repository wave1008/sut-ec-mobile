package com.sutec.mobile.feature.checkout

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sutec.mobile.designsystem.component.AppTopBar
import com.sutec.mobile.designsystem.component.PrimaryButton
import com.sutec.mobile.designsystem.component.SecondaryButton
import com.sutec.mobile.designsystem.extraColors
import com.sutec.mobile.designsystem.spacing
import com.sutec.mobile.i18n.tr
import com.sutec.mobile.util.formatYen
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun OrderConfirmationScreen(
    orderId: String,
    onContinueShopping: () -> Unit,
    onViewOrder: (String) -> Unit,
    viewModel: OrderConfirmationViewModel = koinViewModel(),
) {
    val order by viewModel.order.collectAsStateWithLifecycle()

    LaunchedEffect(orderId) {
        viewModel.load(orderId)
    }

    Scaffold(
        topBar = { AppTopBar(title = tr("注文完了", "Order confirmed")) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(MaterialTheme.spacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(MaterialTheme.spacing.xxl))
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.extraColors.success,
            )
            Spacer(Modifier.height(MaterialTheme.spacing.lg))
            Text(
                text = tr("ご注文ありがとうございます", "Thank you for your order!"),
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
            )

            val currentOrder = order
            if (currentOrder != null) {
                Spacer(Modifier.height(MaterialTheme.spacing.md))
                Text(
                    text = "#${currentOrder.id}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.extraColors.onSurfaceFaint,
                )
                Spacer(Modifier.height(MaterialTheme.spacing.xs))
                Text(
                    text = formatYen(currentOrder.totals.totalYen),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(MaterialTheme.spacing.sm))
                Text(
                    text = tr("通常5〜7営業日でお届けします", "Estimated delivery in 5-7 business days"),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.extraColors.onSurfaceFaint,
                    textAlign = TextAlign.Center,
                )
            }

            Spacer(Modifier.weight(1f))

            PrimaryButton(
                text = tr("注文を確認する", "View order"),
                onClick = { onViewOrder(orderId) },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(MaterialTheme.spacing.sm))
            SecondaryButton(
                text = tr("買い物を続ける", "Continue shopping"),
                onClick = onContinueShopping,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
