package com.sutec.mobile

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil3.compose.setSingletonImageLoaderFactory
import com.sutec.mobile.designsystem.AppTheme
import com.sutec.mobile.i18n.LocalAppLanguage
import com.sutec.mobile.i18n.LocaleController
import com.sutec.mobile.navigation.AppBottomBar
import com.sutec.mobile.navigation.AppNavHost
import com.sutec.mobile.navigation.isTabRoute
import com.sutec.mobile.util.buildImageLoader
import org.koin.compose.koinInject

// ルート: Coil 登録 -> 言語 Local 供給 -> AppTheme -> 下タブ付き Scaffold + NavHost。
@Composable
fun App() {
    setSingletonImageLoaderFactory { context -> buildImageLoader(context) }

    val localeController = koinInject<LocaleController>()
    val language by localeController.language.collectAsState()

    CompositionLocalProvider(LocalAppLanguage provides language) {
        AppTheme {
            val navController = rememberNavController()
            val backStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = backStackEntry?.destination
            val showBottomBar = isTabRoute(currentDestination)

            Scaffold(
                modifier = Modifier.fillMaxSize(),
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
