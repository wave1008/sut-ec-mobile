package com.sutec.mobile.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.sutec.mobile.data.repository.CartRepository
import com.sutec.mobile.i18n.tr
import org.koin.compose.koinInject
import kotlin.reflect.KClass

// 下タブ根。順序=表示順。route はタブ切替のナビ先、matchRoutes は選択判定に使う。
private enum class Tab(
    val route: Any,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
) {
    HOME(HomeRoute, Icons.Filled.Home, Icons.Outlined.Home),
    SEARCH(SearchRoute(), Icons.Filled.Search, Icons.Outlined.Search),
    CART(CartRoute, Icons.Filled.ShoppingCart, Icons.Outlined.ShoppingCart),
    WISHLIST(WishlistRoute, Icons.Filled.Favorite, Icons.Outlined.FavoriteBorder),
    ACCOUNT(AccountRoute, Icons.Filled.Person, Icons.Outlined.Person),
}

private fun Tab.matchClass(): KClass<*> = when (this) {
    Tab.HOME -> HomeRoute::class
    Tab.SEARCH -> SearchRoute::class
    Tab.CART -> CartRoute::class
    Tab.WISHLIST -> WishlistRoute::class
    Tab.ACCOUNT -> AccountRoute::class
}

@Composable
private fun Tab.label(): String = when (this) {
    Tab.HOME -> tr("ホーム", "Home")
    Tab.SEARCH -> tr("検索", "Search")
    Tab.CART -> tr("カート", "Cart")
    Tab.WISHLIST -> tr("お気に入り", "Wishlist")
    Tab.ACCOUNT -> tr("アカウント", "Account")
}

// 現在地が下タブ根のいずれかなら true(=下タブ表示)。詳細/チェックアウト等では非表示。
fun isTabRoute(destination: NavDestination?): Boolean =
    destination?.let { d -> Tab.entries.any { d.hasRoute(it.matchClass()) } } == true

@Composable
fun AppBottomBar(navController: NavController, currentDestination: NavDestination?) {
    val cart = koinInject<CartRepository>()
    val cartCount by cart.count.collectAsState()

    NavigationBar {
        Tab.entries.forEach { tab ->
            val selected = currentDestination?.hasRoute(tab.matchClass()) == true
            NavigationBarItem(
                selected = selected,
                modifier = Modifier.testTag("tab_${tab.name.lowercase()}"),
                onClick = {
                    if (!selected) {
                        // saveState/restoreState は使わない。タブ根以外(詳細等)やタブ根への
                        // プレーン navigate が混ざったスタックが保存されると、復元でタブ根以外
                        // (例: Cart)が先頭に出て「ホームタブがカートを開く」誤遷移になる
                        // (iOS で確認した BUG-1/2 の実因)。タブは常に根へ確定着地させる。
                        navController.navigate(tab.route) {
                            popUpTo(navController.graph.findStartDestination().id) { inclusive = false }
                            launchSingleTop = true
                        }
                    }
                },
                icon = {
                    val icon = if (selected) tab.selectedIcon else tab.unselectedIcon
                    if (tab == Tab.CART && cartCount > 0) {
                        BadgedBox(badge = { Badge { Text(cartCount.toString()) } }) {
                            Icon(icon, contentDescription = null)
                        }
                    } else {
                        Icon(icon, contentDescription = null)
                    }
                },
                label = { Text(tab.label()) },
            )
        }
    }
}
