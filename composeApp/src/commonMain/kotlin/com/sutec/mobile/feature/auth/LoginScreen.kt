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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sutec.mobile.designsystem.component.AppTextButton
import com.sutec.mobile.designsystem.component.AppTopBar
import com.sutec.mobile.designsystem.component.PrimaryButton
import com.sutec.mobile.designsystem.extraColors
import com.sutec.mobile.designsystem.spacing
import com.sutec.mobile.i18n.tr
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onBack: () -> Unit,
    onLoggedIn: () -> Unit,
    onSignupClick: () -> Unit,
    viewModel: LoginViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val spacing = MaterialTheme.spacing

    LaunchedEffect(uiState.success) {
        if (uiState.success) onLoggedIn()
    }

    Scaffold(
        topBar = { AppTopBar(title = tr("ログイン", "Log in"), onBack = onBack) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = spacing.screenH),
        ) {
            Spacer(Modifier.height(spacing.lg))
            Text(
                text = tr("登録済みのメールアドレスとパスワードでログイン", "Log in with your registered email and password"),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.extraColors.onSurfaceFaint,
            )
            Spacer(Modifier.height(spacing.lg))
            OutlinedTextField(
                value = uiState.email,
                onValueChange = viewModel::onEmailChange,
                label = { Text(tr("メールアドレス", "Email")) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            )
            Spacer(Modifier.height(spacing.md))
            OutlinedTextField(
                value = uiState.password,
                onValueChange = viewModel::onPasswordChange,
                label = { Text(tr("パスワード", "Password")) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            )
            if (uiState.error != null) {
                Spacer(Modifier.height(spacing.sm))
                Text(
                    text = tr("ログインに失敗しました", "Failed to log in"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
            Spacer(Modifier.height(spacing.lg))
            PrimaryButton(
                text = tr("ログイン", "Log in"),
                onClick = viewModel::login,
                loading = uiState.loading,
            )
            Spacer(Modifier.height(spacing.sm))
            AppTextButton(
                text = tr("アカウントを作成", "Create account"),
                onClick = onSignupClick,
            )
            Spacer(Modifier.height(spacing.lg))
        }
    }
}
