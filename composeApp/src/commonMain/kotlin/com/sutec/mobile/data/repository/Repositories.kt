package com.sutec.mobile.data.repository

import com.sutec.mobile.data.model.Address
import com.sutec.mobile.data.model.Category
import com.sutec.mobile.data.model.CartItem
import com.sutec.mobile.data.model.Order
import com.sutec.mobile.data.model.OrderTotals
import com.sutec.mobile.data.model.PaymentMethod
import com.sutec.mobile.data.model.Product
import com.sutec.mobile.data.model.Review
import com.sutec.mobile.data.model.User
import kotlinx.coroutines.flow.StateFlow

// ===== カタログ =====
// 全 suspend 関数は実装側で delay(200-600ms) を挟み通信を擬似する(モック方針)。
interface ProductRepository {
    suspend fun getCategories(): List<Category>
    suspend fun getFeatured(): List<Product>
    suspend fun getProducts(query: SearchQuery): List<Product>
    suspend fun getProductsByCategory(categoryId: String): List<Product>
    suspend fun getProduct(id: String): Product?
    suspend fun getProductsByIds(ids: List<String>): List<Product>
    suspend fun getReviews(productId: String): List<Review>
    suspend fun getRelated(productId: String): List<Product>
}

// ===== カート(セッション内メモリ, 全画面同期) =====
interface CartRepository {
    val items: StateFlow<List<CartItem>>
    val count: StateFlow<Int>     // 合計数量。下タブのバッジ用。
    val totals: StateFlow<OrderTotals>
    fun add(product: Product, quantity: Int = 1)
    fun setQuantity(productId: String, quantity: Int)   // 0 以下で remove
    fun remove(productId: String)
    fun clear()
}

// ===== お気に入り =====
interface WishlistRepository {
    val productIds: StateFlow<Set<String>>
    fun toggle(productId: String)
    fun remove(productId: String)
}

// ===== 注文 =====
interface OrderRepository {
    val orders: StateFlow<List<Order>>
    // サーバーから注文一覧を再取得。失敗時は例外を投げる(画面側で error 表示/再試行)。
    suspend fun refresh()
    // 金額・items・住所ラベルはサーバーがカートから権威決定する。client は選択IDのみ渡す。
    suspend fun placeOrder(addressId: String, paymentMethodId: String): Order
    fun getOrder(id: String): Order?
}

// ===== 認証(モック: 任意の資格情報で成功) =====
interface AuthRepository {
    val currentUser: StateFlow<User?>
    suspend fun login(email: String, password: String): Result<User>
    suspend fun signup(name: String, email: String, password: String): Result<User>
    fun logout()
}

// ===== 住所・支払い方法 =====
interface AccountRepository {
    val addresses: StateFlow<List<Address>>
    val paymentMethods: StateFlow<List<PaymentMethod>>
    // id が空文字なら新規採番、それ以外は更新。
    fun upsertAddress(address: Address)
    fun deleteAddress(id: String)
    fun setDefaultAddress(id: String)
    fun getAddress(id: String): Address?
    fun upsertPayment(method: PaymentMethod)
    fun deletePayment(id: String)
    fun setDefaultPayment(id: String)
    fun getPayment(id: String): PaymentMethod?
}
