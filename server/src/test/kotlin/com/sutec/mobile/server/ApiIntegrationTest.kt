package com.sutec.mobile.server

import com.sutec.mobile.data.dto.AddCartItemRequest
import com.sutec.mobile.data.dto.CartDto
import com.sutec.mobile.data.dto.LoginRequest
import com.sutec.mobile.data.dto.MergeCartRequest
import com.sutec.mobile.data.dto.PageResponse
import com.sutec.mobile.data.dto.PlaceOrderRequest
import com.sutec.mobile.data.dto.SignupRequest
import com.sutec.mobile.data.dto.TokenResponse
import com.sutec.mobile.data.model.Address
import com.sutec.mobile.data.model.Category
import com.sutec.mobile.data.model.Order
import com.sutec.mobile.data.model.PaymentMethod
import com.sutec.mobile.data.model.PaymentType
import com.sutec.mobile.data.model.Product
import com.sutec.mobile.data.model.Review
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.testcontainers.DockerClientFactory
import org.testcontainers.containers.PostgreSQLContainer
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

// サーバー統合テスト。実 PostgreSQL(Flyway マイグレーション + カタログシード)に対して API を通しで検証する。
// DB は次のいずれか:
//  - TEST_DATABASE_URL が設定されていれば その外部DB(例: Apple Container の Postgres)を使う
//  - なければ Testcontainers が postgres:16 を起動(Docker 必須)
//  - どちらも無ければ assumeTrue でクラス全体をスキップ(build は緑のまま)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ApiIntegrationTest {

    private var container: PostgreSQLContainer<*>? = null
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    @BeforeAll
    fun setup() {
        val external = System.getProperty("TEST_DATABASE_URL") ?: System.getenv("TEST_DATABASE_URL")
        if (external != null) {
            System.setProperty("DATABASE_URL", external)
            System.setProperty("DB_USER", System.getenv("TEST_DB_USER") ?: "sutec")
            System.setProperty("DB_PASSWORD", System.getenv("TEST_DB_PASSWORD") ?: "sutec")
        } else {
            assumeTrue(
                DockerClientFactory.instance().isDockerAvailable(),
                "Docker も TEST_DATABASE_URL も無いため統合テストをスキップ",
            )
            val c = PostgreSQLContainer("postgres:16")
                .withDatabaseName("sutec").withUsername("sutec").withPassword("sutec")
            c.start()
            container = c
            System.setProperty("DATABASE_URL", c.jdbcUrl)
            System.setProperty("DB_USER", c.username)
            System.setProperty("DB_PASSWORD", c.password)
        }
        System.setProperty("JWT_SECRET", "test-secret")
    }

    @AfterAll
    fun teardown() {
        container?.stop()
    }

    // ---- helpers ----
    private fun runApi(block: suspend ApplicationTestBuilder.(HttpClient) -> Unit) = testApplication {
        application { module() }
        val client = createClient { install(ContentNegotiation) { json(json) } }
        block(client)
    }

    private fun HttpRequestBuilder.bearer(token: String) = header(HttpHeaders.Authorization, "Bearer $token")
    private fun HttpRequestBuilder.jsonBody(body: Any) { contentType(ContentType.Application.Json); setBody(body) }
    private fun uniqueEmail() = "it_${UUID.randomUUID()}@example.com"

    private suspend fun ApplicationTestBuilder.newUser(client: HttpClient): TokenResponse {
        val resp = client.post("/api/v1/auth/signup") {
            jsonBody(SignupRequest("統合テスト", uniqueEmail(), "secret1"))
        }
        assertEquals(HttpStatusCode.Created, resp.status)
        return resp.body()
    }

    // ===== カタログ(公開) =====
    @Test fun health_ok() = runApi { client ->
        assertEquals(HttpStatusCode.OK, client.get("/health").status)
    }

    @Test fun categories_seeded() = runApi { client ->
        val cats: List<Category> = client.get("/api/v1/categories").body()
        assertEquals(8, cats.size)
    }

    @Test fun featured_returns_twelve() = runApi { client ->
        val featured: List<Product> = client.get("/api/v1/products/featured").body()
        assertEquals(12, featured.size)
    }

    @Test fun product_detail_and_404() = runApi { client ->
        val p: Product = client.get("/api/v1/products/electronics_1").body()
        assertEquals("electronics_1", p.id)
        assertEquals(8900, p.priceYen)
        assertTrue(p.imageUrls.all { it.startsWith("images/") }, "imageUrls は相対パス")
        assertEquals(HttpStatusCode.NotFound, client.get("/api/v1/products/does_not_exist").status)
    }

    @Test fun search_filters_and_paginates() = runApi { client ->
        val page: PageResponse<Product> =
            client.get("/api/v1/products?categoryId=electronics&sort=PRICE_ASC&pageSize=5").body()
        assertTrue(page.items.all { it.categoryId == "electronics" })
        assertTrue(page.items.size <= 5)
        assertTrue(page.total >= page.items.size)
        // PRICE_ASC は昇順
        assertEquals(page.items.map { it.priceYen }.sorted(), page.items.map { it.priceYen })
    }

    @Test fun reviews_related_byIds() = runApi { client ->
        val reviews: List<Review> = client.get("/api/v1/products/electronics_1/reviews").body()
        assertTrue(reviews.all { it.productId == "electronics_1" })
        val related: List<Product> = client.get("/api/v1/products/electronics_1/related").body()
        assertTrue(related.none { it.id == "electronics_1" })
        val byIds: List<Product> = client.get("/api/v1/products/by-ids?ids=electronics_1,fashion_5").body()
        assertEquals(listOf("electronics_1", "fashion_5"), byIds.map { it.id })
    }

    // ===== 認証 =====
    @Test fun cart_requires_auth() = runApi { client ->
        assertEquals(HttpStatusCode.Unauthorized, client.get("/api/v1/cart").status)
    }

    @Test fun signup_login_and_errors() = runApi { client ->
        val email = uniqueEmail()
        val signup = client.post("/api/v1/auth/signup") { jsonBody(SignupRequest("太郎", email, "secret1")) }
        assertEquals(HttpStatusCode.Created, signup.status)
        val token = signup.body<TokenResponse>()
        assertNotNull(token.token)
        assertEquals(email, token.user.email)

        // 重複メール → 409
        val dup = client.post("/api/v1/auth/signup") { jsonBody(SignupRequest("x", email, "secret1")) }
        assertEquals(HttpStatusCode.Conflict, dup.status)

        // 正常ログイン
        assertEquals(HttpStatusCode.OK, client.post("/api/v1/auth/login") { jsonBody(LoginRequest(email, "secret1")) }.status)
        // 誤パスワード → 401
        assertEquals(HttpStatusCode.Unauthorized, client.post("/api/v1/auth/login") { jsonBody(LoginRequest(email, "wrong")) }.status)
        // 検証エラー(空メール) → 400
        assertEquals(HttpStatusCode.BadRequest, client.post("/api/v1/auth/login") { jsonBody(LoginRequest("", "secret1")) }.status)

        // /me
        val me: com.sutec.mobile.data.model.User = client.get("/api/v1/me") { bearer(token.token) }.body()
        assertEquals(email, me.email)
    }

    // ===== カート → 注文確定(サーバー権威計算) =====
    @Test fun cart_and_place_order() = runApi { client ->
        val token = newUser(client).token

        // 住所・支払いを登録
        val addresses: List<Address> = client.post("/api/v1/addresses") {
            bearer(token)
            jsonBody(Address("", "山田太郎", "150-0001", "東京都", "渋谷区", "神宮前1-2-3", "", "09012345678", true))
        }.body()
        assertEquals(1, addresses.size)
        val addressId = addresses.first().id

        val payments: List<PaymentMethod> = client.post("/api/v1/payment-methods") {
            bearer(token)
            jsonBody(PaymentMethod("", PaymentType.CASH_ON_DELIVERY, isDefault = true))
        }.body()
        val paymentId = payments.first().id

        // カート追加(electronics_1=8900 は単体で送料無料閾値3000以上)
        client.post("/api/v1/cart/items") { bearer(token); jsonBody(AddCartItemRequest("electronics_1", 1)) }
        val cart: CartDto = client.post("/api/v1/cart/items") { bearer(token); jsonBody(AddCartItemRequest("home_2", 2)) }.body()
        assertEquals(2, cart.items.size)
        assertEquals(0, cart.totals.taxYen, "税込のため tax=0")
        assertEquals(0, cart.totals.shippingYen, "3000円以上で送料無料")
        assertEquals(cart.totals.subtotalYen, cart.totals.totalYen)
        assertTrue(cart.totals.subtotalYen >= 8900)

        // 注文確定 → サーバーがカートから権威計算
        val orderResp = client.post("/api/v1/orders") {
            bearer(token); jsonBody(PlaceOrderRequest(addressId, paymentId))
        }
        assertEquals(HttpStatusCode.Created, orderResp.status)
        val order: Order = orderResp.body()
        assertEquals("PROCESSING", order.status.name)
        assertEquals(cart.totals, order.totals, "注文金額はカートの権威計算と一致")
        assertEquals("代金引換", order.paymentLabel)
        assertEquals(2, order.items.size)
        assertTrue(order.id.startsWith("ord_"))

        // カートは空になる
        val cleared: CartDto = client.get("/api/v1/cart") { bearer(token) }.body()
        assertEquals(0, cleared.items.size)

        // 注文履歴に反映
        val orders: List<Order> = client.get("/api/v1/orders") { bearer(token) }.body()
        assertTrue(orders.any { it.id == order.id })
    }

    @Test fun empty_cart_order_is_rejected() = runApi { client ->
        val token = newUser(client).token
        val addresses: List<Address> = client.post("/api/v1/addresses") {
            bearer(token)
            jsonBody(Address("", "x", "1", "東京都", "渋谷区", "l1", "", "090", true))
        }.body()
        val payments: List<PaymentMethod> = client.post("/api/v1/payment-methods") {
            bearer(token); jsonBody(PaymentMethod("", PaymentType.CASH_ON_DELIVERY, isDefault = true))
        }.body()
        val resp = client.post("/api/v1/orders") {
            bearer(token); jsonBody(PlaceOrderRequest(addresses.first().id, payments.first().id))
        }
        assertEquals(HttpStatusCode.BadRequest, resp.status)
    }

    @Test fun cart_merge_sums_quantities() = runApi { client ->
        val token = newUser(client).token
        // サーバーカートに electronics_1 x1
        client.post("/api/v1/cart/items") { bearer(token); jsonBody(AddCartItemRequest("electronics_1", 1)) }
        // ゲストカート相当をマージ: electronics_1 x2(加算→3) / home_2 x1(新規) / 不明productは無視
        val merged: CartDto = client.post("/api/v1/cart/merge") {
            bearer(token)
            jsonBody(
                MergeCartRequest(
                    listOf(
                        AddCartItemRequest("electronics_1", 2),
                        AddCartItemRequest("home_2", 1),
                        AddCartItemRequest("nonexistent_product", 5),
                    ),
                ),
            )
        }.body()
        val qty = merged.items.associate { it.product.id to it.quantity }
        assertEquals(3, qty["electronics_1"], "既存は加算される")
        assertEquals(1, qty["home_2"], "新規は挿入される")
        assertTrue(!qty.containsKey("nonexistent_product"), "不明productは無視")
    }

    // ===== お気に入り =====
    @Test fun wishlist_add_list_remove() = runApi { client ->
        val token = newUser(client).token
        val added: List<String> = client.put("/api/v1/wishlist/electronics_1") { bearer(token) }.body()
        assertTrue(added.contains("electronics_1"))
        val listed: List<String> = client.get("/api/v1/wishlist") { bearer(token) }.body()
        assertTrue(listed.contains("electronics_1"))
        val afterRemove: List<String> = client.delete("/api/v1/wishlist/electronics_1") { bearer(token) }.body()
        assertTrue(!afterRemove.contains("electronics_1"))
    }
}
