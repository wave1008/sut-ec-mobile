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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.LocationOn
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sutec.mobile.data.model.Address
import com.sutec.mobile.designsystem.component.AppTextButton
import com.sutec.mobile.designsystem.component.AppTopBar
import com.sutec.mobile.designsystem.component.EmptyState
import com.sutec.mobile.designsystem.extraColors
import com.sutec.mobile.designsystem.spacing
import com.sutec.mobile.i18n.tr
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddressesScreen(
    onBack: () -> Unit,
    onAddNew: () -> Unit,
    onEdit: (String) -> Unit,
    viewModel: AddressesViewModel = koinViewModel(),
) {
    val addresses by viewModel.addresses.collectAsStateWithLifecycle()
    val spacing = MaterialTheme.spacing

    Scaffold(
        modifier = Modifier.testTag("screen_addresses"),
        topBar = {
            AppTopBar(
                title = tr("お届け先住所", "Addresses"),
                onBack = onBack,
                actions = {
                    IconButton(onClick = onAddNew, modifier = Modifier.testTag("btn_add_address")) {
                        Icon(Icons.Filled.Add, contentDescription = tr("追加", "Add"))
                    }
                },
            )
        },
    ) { padding ->
        if (addresses.isEmpty()) {
            Box(modifier = Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                EmptyState(
                    icon = Icons.Outlined.LocationOn,
                    title = tr("登録済みの住所がありません", "No addresses yet"),
                    message = tr("新しい住所を追加してください", "Add a new address to get started"),
                    actionLabel = tr("住所を追加", "Add address"),
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
            items(addresses, key = { it.id }) { address ->
                AddressCard(
                    address = address,
                    onEdit = { onEdit(address.id) },
                    onDelete = { viewModel.delete(address.id) },
                    onSetDefault = { viewModel.setDefault(address.id) },
                )
            }
        }
    }
}

@Composable
private fun AddressCard(
    address: Address,
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
            .padding(spacing.md)
            .testTag("address_row_${address.id}"),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = address.fullName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
            )
            if (address.isDefault) DefaultBadge()
        }
        Spacer(Modifier.height(spacing.xs))
        Text(
            text = formatAddressLines(address),
            style = MaterialTheme.typography.bodyMedium,
            color = extraColors.onSurfaceFaint,
        )
        Spacer(Modifier.height(spacing.xxs))
        Text(
            text = address.phone,
            style = MaterialTheme.typography.bodyMedium,
            color = extraColors.onSurfaceFaint,
        )
        Spacer(Modifier.height(spacing.sm))
        Row(horizontalArrangement = Arrangement.spacedBy(spacing.xs)) {
            AppTextButton(text = tr("編集", "Edit"), onClick = onEdit, modifier = Modifier.testTag("btn_edit_${address.id}"))
            AppTextButton(text = tr("削除", "Delete"), onClick = onDelete, modifier = Modifier.testTag("btn_delete_${address.id}"))
            if (!address.isDefault) {
                AppTextButton(text = tr("既定に設定", "Set as default"), onClick = onSetDefault)
            }
        }
    }
}

// 住所欄はユーザー入力のフリーテキストの連結。言語による語順変換はしない(モデルに言語別フィールドが無い)。
private fun formatAddressLines(address: Address): String {
    val line2Part = if (address.line2.isNotBlank()) " ${address.line2}" else ""
    return "${address.postalCode} ${address.prefecture}${address.city}${address.line1}$line2Part"
}
