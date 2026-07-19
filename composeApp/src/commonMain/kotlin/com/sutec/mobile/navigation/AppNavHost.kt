package com.sutec.mobile.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.sutec.mobile.feature.address.AddressEditScreen
import com.sutec.mobile.feature.address.AddressesScreen
import com.sutec.mobile.feature.address.PaymentEditScreen
import com.sutec.mobile.feature.address.PaymentMethodsScreen
import com.sutec.mobile.feature.auth.LoginScreen
import com.sutec.mobile.feature.auth.SignupScreen
import com.sutec.mobile.feature.cart.CartScreen
import com.sutec.mobile.feature.catalog.CatalogScreen
import com.sutec.mobile.feature.checkout.CheckoutScreen
import com.sutec.mobile.feature.checkout.OrderConfirmationScreen
import com.sutec.mobile.feature.home.HomeScreen
import com.sutec.mobile.feature.orders.OrderDetailScreen
import com.sutec.mobile.feature.orders.OrdersScreen
import com.sutec.mobile.feature.productdetail.ProductDetailScreen
import com.sutec.mobile.feature.profile.AccountScreen
import com.sutec.mobile.feature.search.SearchScreen
import com.sutec.mobile.feature.wishlist.WishlistScreen

// 全ルートの結線。各 Screen は NavController を持たず、ここで型付きコールバックに変換する。
@Composable
fun AppNavHost(navController: NavHostController, modifier: Modifier = Modifier) {
    // 注文確定/買い物を続ける 用: スタックを畳んで Home タブへ戻す。
    val goHome: () -> Unit = {
        navController.navigate(HomeRoute) {
            popUpTo(navController.graph.findStartDestination().id) { inclusive = false }
            launchSingleTop = true
        }
    }

    NavHost(
        navController = navController,
        startDestination = HomeRoute,
        modifier = modifier,
    ) {
        composable<HomeRoute> {
            HomeScreen(
                onProductClick = { navController.navigate(ProductDetailRoute(it)) },
                onCategoryClick = { navController.navigate(CatalogRoute(it)) },
                onSearchClick = { navController.navigate(SearchRoute()) },
                onSeeAllFeatured = { navController.navigate(CatalogRoute()) },
            )
        }

        composable<CatalogRoute> { entry ->
            val route = entry.toRoute<CatalogRoute>()
            CatalogScreen(
                categoryId = route.categoryId,
                onProductClick = { navController.navigate(ProductDetailRoute(it)) },
                onBack = { navController.popBackStack() },
                onOpenSearch = { navController.navigate(SearchRoute()) },
            )
        }

        composable<SearchRoute> { entry ->
            val route = entry.toRoute<SearchRoute>()
            SearchScreen(
                initialQuery = route.initialQuery,
                onProductClick = { navController.navigate(ProductDetailRoute(it)) },
                onBack = { navController.popBackStack() },
            )
        }

        composable<ProductDetailRoute> { entry ->
            val route = entry.toRoute<ProductDetailRoute>()
            ProductDetailScreen(
                productId = route.productId,
                onBack = { navController.popBackStack() },
                onProductClick = { navController.navigate(ProductDetailRoute(it)) },
                onOpenCart = { navController.navigate(CartRoute) },
            )
        }

        composable<CartRoute> {
            CartScreen(
                onProductClick = { navController.navigate(ProductDetailRoute(it)) },
                onCheckout = { navController.navigate(CheckoutRoute) },
                onContinueShopping = goHome,
            )
        }

        composable<CheckoutRoute> {
            CheckoutScreen(
                onBack = { navController.popBackStack() },
                onPlaced = { orderId ->
                    navController.navigate(OrderConfirmationRoute(orderId)) {
                        // カート/チェックアウトを戻れないよう畳む。
                        popUpTo(CartRoute) { inclusive = true }
                    }
                },
                onManageAddresses = { navController.navigate(AddressesRoute) },
                onManagePayments = { navController.navigate(PaymentMethodsRoute) },
            )
        }

        composable<OrderConfirmationRoute> { entry ->
            val route = entry.toRoute<OrderConfirmationRoute>()
            OrderConfirmationScreen(
                orderId = route.orderId,
                onContinueShopping = goHome,
                onViewOrder = { navController.navigate(OrderDetailRoute(it)) },
            )
        }

        composable<OrdersRoute> {
            OrdersScreen(
                onOrderClick = { navController.navigate(OrderDetailRoute(it)) },
                onBrowse = goHome,
                onBack = { navController.popBackStack() },
            )
        }

        composable<OrderDetailRoute> { entry ->
            val route = entry.toRoute<OrderDetailRoute>()
            OrderDetailScreen(
                orderId = route.orderId,
                onBack = { navController.popBackStack() },
                onProductClick = { navController.navigate(ProductDetailRoute(it)) },
            )
        }

        composable<WishlistRoute> {
            WishlistScreen(
                onProductClick = { navController.navigate(ProductDetailRoute(it)) },
                onBrowse = goHome,
            )
        }

        composable<LoginRoute> {
            LoginScreen(
                onBack = { navController.popBackStack() },
                onLoggedIn = { navController.popBackStack() },
                onSignupClick = { navController.navigate(SignupRoute) },
            )
        }

        composable<SignupRoute> {
            SignupScreen(
                onBack = { navController.popBackStack() },
                onSignedUp = { navController.popBackStack(AccountRoute, inclusive = false) },
                onLoginClick = { navController.popBackStack() },
            )
        }

        composable<AccountRoute> {
            AccountScreen(
                onLogin = { navController.navigate(LoginRoute) },
                onOrders = { navController.navigate(OrdersRoute) },
                onWishlist = { navController.navigate(WishlistRoute) },
                onAddresses = { navController.navigate(AddressesRoute) },
                onPayments = { navController.navigate(PaymentMethodsRoute) },
            )
        }

        composable<AddressesRoute> {
            AddressesScreen(
                onBack = { navController.popBackStack() },
                onAddNew = { navController.navigate(AddressEditRoute()) },
                onEdit = { navController.navigate(AddressEditRoute(it)) },
            )
        }

        composable<AddressEditRoute> { entry ->
            val route = entry.toRoute<AddressEditRoute>()
            AddressEditScreen(
                addressId = route.addressId,
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() },
            )
        }

        composable<PaymentMethodsRoute> {
            PaymentMethodsScreen(
                onBack = { navController.popBackStack() },
                onAddNew = { navController.navigate(PaymentEditRoute()) },
                onEdit = { navController.navigate(PaymentEditRoute(it)) },
            )
        }

        composable<PaymentEditRoute> { entry ->
            val route = entry.toRoute<PaymentEditRoute>()
            PaymentEditScreen(
                paymentId = route.paymentId,
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() },
            )
        }
    }
}
