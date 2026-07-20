package com.sutec.mobile.feature.auth

import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sutec.mobile.designsystem.component.AppTextButton
import com.sutec.mobile.designsystem.component.AppTopBar
import com.sutec.mobile.designsystem.component.PrimaryButton
import com.sutec.mobile.designsystem.spacing
import com.sutec.mobile.i18n.tr
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignupScreen(
    onBack: () -> Unit,
    onSignedUp: () -> Unit,
    onLoginClick: () -> Unit,
    viewModel: SignupViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val spacing = MaterialTheme.spacing

    LaunchedEffect(uiState.success) {
        if (uiState.success) onSignedUp()
    }

    Scaffold(
        modifier = Modifier.testTag("screen_signup"),
        topBar = { AppTopBar(title = tr("登録", "Sign up"), onBack = onBack) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = spacing.screenH),
        ) {
            Spacer(Modifier.height(spacing.lg))
            OutlinedTextField(
                value = uiState.name,
                onValueChange = viewModel::onNameChange,
                label = { Text(tr("お名前", "Name")) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("field_name"),
                singleLine = true,
            )
            Spacer(Modifier.height(spacing.md))
            OutlinedTextField(
                value = uiState.email,
                onValueChange = viewModel::onEmailChange,
                label = { Text(tr("メールアドレス", "Email")) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("field_email"),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            )
            Spacer(Modifier.height(spacing.md))
            OutlinedTextField(
                value = uiState.password,
                onValueChange = viewModel::onPasswordChange,
                label = { Text(tr("パスワード", "Password")) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("field_password"),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            )
            if (uiState.error != null) {
                Spacer(Modifier.height(spacing.sm))
                Text(
                    text = tr("登録に失敗しました", "Failed to sign up"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
            Spacer(Modifier.height(spacing.lg))
            PrimaryButton(
                text = tr("登録", "Sign up"),
                onClick = viewModel::signup,
                modifier = Modifier.testTag("btn_signup"),
                loading = uiState.loading,
            )
            Spacer(Modifier.height(spacing.sm))
            AppTextButton(
                text = tr("ログインへ", "Back to log in"),
                onClick = onLoginClick,
                modifier = Modifier.testTag("btn_goto_login"),
            )
            Spacer(Modifier.height(spacing.lg))
        }
    }
}
