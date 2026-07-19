package com.sutec.mobile

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil3.compose.setSingletonImageLoaderFactory
import com.sutec.mobile.designsystem.AppTheme
import com.sutec.mobile.i18n.AppLanguage
import com.sutec.mobile.i18n.LocalAppLanguage
import com.sutec.mobile.i18n.LocaleController
import com.sutec.mobile.navigation.AppBottomBar
import com.sutec.mobile.navigation.AppNavHost
import com.sutec.mobile.navigation.isTabRoute
import com.sutec.mobile.util.AppMessages
import com.sutec.mobile.util.buildImageLoader
import org.koin.compose.koinInject

// ルート: Coil 登録 -> 言語 Local 供給 -> AppTheme -> 下タブ付き Scaffold + NavHost。
@Composable
fun App() {
    setSingletonImageLoaderFactory { context -> buildImageLoader(context) }

    val localeController = koinInject<LocaleController>()
    val language by localeController.language.collectAsState()

    val appMessages = koinInject<AppMessages>()
    val snackbarHostState = remember { SnackbarHostState() }
    // 通知バスを購読し、発火時点の言語でトースト表示(セッション失効・操作失敗など)。
    LaunchedEffect(Unit) {
        appMessages.messages.collect { msg ->
            val text = if (localeController.language.value == AppLanguage.JA) msg.ja else msg.en
            snackbarHostState.showSnackbar(text)
        }
    }

    CompositionLocalProvider(LocalAppLanguage provides language) {
        AppTheme {
            val navController = rememberNavController()
            val backStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = backStackEntry?.destination
            val showBottomBar = isTabRoute(currentDestination)

            Scaffold(
                modifier = Modifier.fillMaxSize(),
                snackbarHost = { SnackbarHost(snackbarHostState) },
                bottomBar = {
                    if (showBottomBar) {
                        AppBottomBar(navController, currentDestination)
                    }
                },
            ) { padding ->
                AppNavHost(
                    navController = navController,
                    modifier = Modifier.fillMaxSize().padding(padding),
                )
            }
        }
    }
}
