package com.sutec.mobile.feature.address

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sutec.mobile.data.model.PaymentType
import com.sutec.mobile.designsystem.component.AppFilterChip
import com.sutec.mobile.designsystem.component.AppTopBar
import com.sutec.mobile.designsystem.component.PrimaryButton
import com.sutec.mobile.designsystem.spacing
import com.sutec.mobile.i18n.tr
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentEditScreen(
    paymentId: String?,
    onBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: PaymentEditViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val spacing = MaterialTheme.spacing

    LaunchedEffect(paymentId) { viewModel.load(paymentId) }
    LaunchedEffect(uiState.saved) { if (uiState.saved) onSaved() }

    Scaffold(
        topBar = {
            AppTopBar(
                title = if (paymentId == null) tr("お支払い方法を追加", "Add payment method") else tr("お支払い方法を編集", "Edit payment method"),
                onBack = onBack,
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = spacing.screenH),
        ) {
            Spacer(Modifier.height(spacing.md))
            Row(horizontalArrangement = Arrangement.spacedBy(spacing.sm)) {
                AppFilterChip(
                    selected = uiState.type == PaymentType.CARD,
                    label = tr("カード", "Card"),
                    onClick = { viewModel.onTypeChange(PaymentType.CARD) },
                )
                AppFilterChip(
                    selected = uiState.type == PaymentType.CASH_ON_DELIVERY,
                    label = tr("代金引換", "Cash on delivery"),
                    onClick = { viewModel.onTypeChange(PaymentType.CASH_ON_DELIVERY) },
                )
            }

            if (uiState.type == PaymentType.CARD) {
                Spacer(Modifier.height(spacing.md))
                OutlinedTextField(
                    value = uiState.brand,
                    onValueChange = viewModel::onBrandChange,
                    label = { Text(tr("ブランド", "Brand")) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                Spacer(Modifier.height(spacing.sm))
                OutlinedTextField(
                    value = uiState.cardNumber,
                    onValueChange = viewModel::onCardNumberChange,
                    label = { Text(tr("カード番号", "Card number")) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
                Spacer(Modifier.height(spacing.sm))
                OutlinedTextField(
                    value = uiState.holderName,
                    onValueChange = viewModel::onHolderNameChange,
                    label = { Text(tr("名義人", "Cardholder name")) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                Spacer(Modifier.height(spacing.sm))
                Row(horizontalArrangement = Arrangement.spacedBy(spacing.sm)) {
                    OutlinedTextField(
                        value = uiState.expMonth,
                        onValueChange = viewModel::onExpMonthChange,
                        label = { Text(tr("有効期限（月）", "Exp. month")) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    )
                    OutlinedTextField(
                        value = uiState.expYear,
                        onValueChange = viewModel::onExpYearChange,
                        label = { Text(tr("有効期限（年）", "Exp. year")) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    )
                }
            }

            Spacer(Modifier.height(spacing.md))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(tr("既定のお支払い方法に設定", "Set as default payment method"), style = MaterialTheme.typography.bodyLarge)
                Switch(checked = uiState.isDefault, onCheckedChange = viewModel::onIsDefaultChange)
            }
            Spacer(Modifier.height(spacing.lg))
            PrimaryButton(text = tr("保存", "Save"), onClick = viewModel::save)
            Spacer(Modifier.height(spacing.lg))
        }
    }
}
