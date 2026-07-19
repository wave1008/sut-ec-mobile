package com.sutec.mobile.di

import com.sutec.mobile.data.repository.AccountRepository
import com.sutec.mobile.data.repository.AuthRepository
import com.sutec.mobile.data.repository.CartRepository
import com.sutec.mobile.data.repository.OrderRepository
import com.sutec.mobile.data.repository.ProductRepository
import com.sutec.mobile.data.repository.WishlistRepository
import com.sutec.mobile.data.remote.ApiClient
import com.sutec.mobile.data.remote.TokenStore
import com.sutec.mobile.data.repository.impl.RemoteAccountRepository
import com.sutec.mobile.data.repository.impl.RemoteAuthRepository
import com.sutec.mobile.data.repository.impl.RemoteCartRepository
import com.sutec.mobile.data.repository.impl.RemoteOrderRepository
import com.sutec.mobile.data.repository.impl.RemoteProductRepository
import com.sutec.mobile.data.repository.impl.RemoteWishlistRepository
import com.sutec.mobile.i18n.LocaleController
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

// initKoin は Android(Application) / iOS(MainViewController) 双方から1回だけ呼ぶ。
// appDeclaration に androidContext などプラットフォーム固有設定を渡す。
fun initKoin(appDeclaration: KoinAppDeclaration = {}): KoinApplication =
    startKoin {
        appDeclaration()
        modules(appModule, featureModule)
    }

// DI 登録の唯一の集約点(メインが所有)。並列実装との競合を避けるため、
// repository/viewmodel は実装クラス完成後にここへ追記する。
// 全 repository は状態(カート/お気に入り/セッション)を保持するため single。
val appModule = module {
    single { LocaleController() }

    // TokenStore は AuthRepository が set/clear、ApiClient が付与、他 Remote 実装が購読して再取得。
    single { TokenStore() }
    single { ApiClient(get()) }

    single<ProductRepository> { RemoteProductRepository(get()) }
    single<CartRepository> { RemoteCartRepository(get(), get()) }
    single<WishlistRepository> { RemoteWishlistRepository(get(), get()) }
    single<OrderRepository> { RemoteOrderRepository(get(), get()) }
    single<AccountRepository> { RemoteAccountRepository(get(), get()) }
    single<AuthRepository> { RemoteAuthRepository(get(), get()) }

    // ViewModel は featureModule に集約(initKoin で併せて読み込む)。
}
