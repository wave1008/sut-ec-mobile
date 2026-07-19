package com.sutec.mobile.di

import com.sutec.mobile.feature.auth.LoginViewModel
import com.sutec.mobile.feature.auth.SignupViewModel
import com.sutec.mobile.feature.address.AddressEditViewModel
import com.sutec.mobile.feature.address.AddressesViewModel
import com.sutec.mobile.feature.address.PaymentEditViewModel
import com.sutec.mobile.feature.address.PaymentMethodsViewModel
import com.sutec.mobile.feature.cart.CartViewModel
import com.sutec.mobile.feature.catalog.CatalogViewModel
import com.sutec.mobile.feature.checkout.CheckoutViewModel
import com.sutec.mobile.feature.checkout.OrderConfirmationViewModel
import com.sutec.mobile.feature.home.HomeViewModel
import com.sutec.mobile.feature.orders.OrderDetailViewModel
import com.sutec.mobile.feature.orders.OrdersViewModel
import com.sutec.mobile.feature.productdetail.ProductDetailViewModel
import com.sutec.mobile.feature.profile.AccountViewModel
import com.sutec.mobile.feature.search.SearchViewModel
import com.sutec.mobile.feature.wishlist.WishlistViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

// 全機能の ViewModel 登録。koinViewModel() が型で解決する。
// コンストラクタ依存(repository/LocaleController)は appModule の single から型解決。
val featureModule = module {
    viewModelOf(::HomeViewModel)
    viewModelOf(::CatalogViewModel)
    viewModelOf(::SearchViewModel)
    viewModelOf(::ProductDetailViewModel)
    viewModelOf(::CartViewModel)
    viewModelOf(::CheckoutViewModel)
    viewModelOf(::OrderConfirmationViewModel)
    viewModelOf(::WishlistViewModel)
    viewModelOf(::OrdersViewModel)
    viewModelOf(::OrderDetailViewModel)
    viewModelOf(::LoginViewModel)
    viewModelOf(::SignupViewModel)
    viewModelOf(::AccountViewModel)
    viewModelOf(::AddressesViewModel)
    viewModelOf(::AddressEditViewModel)
    viewModelOf(::PaymentMethodsViewModel)
    viewModelOf(::PaymentEditViewModel)
}
