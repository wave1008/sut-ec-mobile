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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sutec.mobile.designsystem.component.AppTopBar
import com.sutec.mobile.designsystem.component.PrimaryButton
import com.sutec.mobile.designsystem.spacing
import com.sutec.mobile.i18n.tr
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddressEditScreen(
    addressId: String?,
    onBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: AddressEditViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val spacing = MaterialTheme.spacing

    LaunchedEffect(addressId) { viewModel.load(addressId) }
    LaunchedEffect(uiState.saved) { if (uiState.saved) onSaved() }

    Scaffold(
        modifier = Modifier.testTag("screen_address_edit"),
        topBar = {
            AppTopBar(
                title = if (addressId == null) tr("住所を追加", "Add address") else tr("住所を編集", "Edit address"),
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
            OutlinedTextField(
                value = uiState.fullName,
                onValueChange = viewModel::onFullNameChange,
                label = { Text(tr("氏名", "Full name")) },
                modifier = Modifier.fillMaxWidth().testTag("field_full_name"),
                singleLine = true,
            )
            Spacer(Modifier.height(spacing.sm))
            OutlinedTextField(
                value = uiState.postalCode,
                onValueChange = viewModel::onPostalCodeChange,
                label = { Text(tr("郵便番号", "Postal code")) },
                modifier = Modifier.fillMaxWidth().testTag("field_postal_code"),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )
            Spacer(Modifier.height(spacing.sm))
            OutlinedTextField(
                value = uiState.prefecture,
                onValueChange = viewModel::onPrefectureChange,
                label = { Text(tr("都道府県", "Prefecture")) },
                modifier = Modifier.fillMaxWidth().testTag("field_prefecture"),
                singleLine = true,
            )
            Spacer(Modifier.height(spacing.sm))
            OutlinedTextField(
                value = uiState.city,
                onValueChange = viewModel::onCityChange,
                label = { Text(tr("市区町村", "City")) },
                modifier = Modifier.fillMaxWidth().testTag("field_city"),
                singleLine = true,
            )
            Spacer(Modifier.height(spacing.sm))
            OutlinedTextField(
                value = uiState.line1,
                onValueChange = viewModel::onLine1Change,
                label = { Text(tr("番地・建物名", "Address line 1")) },
                modifier = Modifier.fillMaxWidth().testTag("field_address_line"),
                singleLine = true,
            )
            Spacer(Modifier.height(spacing.sm))
            OutlinedTextField(
                value = uiState.line2,
                onValueChange = viewModel::onLine2Change,
                label = { Text(tr("部屋番号など（任意）", "Address line 2 (optional)")) },
                modifier = Modifier.fillMaxWidth().testTag("field_address_line2"),
                singleLine = true,
            )
            Spacer(Modifier.height(spacing.sm))
            OutlinedTextField(
                value = uiState.phone,
                onValueChange = viewModel::onPhoneChange,
                label = { Text(tr("電話番号", "Phone number")) },
                modifier = Modifier.fillMaxWidth().testTag("field_phone"),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            )
            Spacer(Modifier.height(spacing.md))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(tr("既定の住所に設定", "Set as default address"), style = MaterialTheme.typography.bodyLarge)
                Switch(checked = uiState.isDefault, onCheckedChange = viewModel::onIsDefaultChange)
            }
            Spacer(Modifier.height(spacing.lg))
            PrimaryButton(text = tr("保存", "Save"), onClick = viewModel::save, modifier = Modifier.testTag("btn_save"))
            Spacer(Modifier.height(spacing.lg))
        }
    }
}
