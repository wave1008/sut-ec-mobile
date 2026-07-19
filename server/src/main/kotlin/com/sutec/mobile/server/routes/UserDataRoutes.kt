package com.sutec.mobile.server.routes

import com.sutec.mobile.data.dto.AddCartItemRequest
import com.sutec.mobile.data.dto.PlaceOrderRequest
import com.sutec.mobile.data.dto.SetQuantityRequest
import com.sutec.mobile.data.model.Address
import com.sutec.mobile.data.model.PaymentMethod
import com.sutec.mobile.server.ApiException
import com.sutec.mobile.server.auth.userId
import com.sutec.mobile.server.repository.AccountStore
import com.sutec.mobile.server.repository.CartStore
import com.sutec.mobile.server.repository.OrderStore
import com.sutec.mobile.server.repository.WishlistStore
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

// すべて authenticate("auth-jwt") 配下で呼ぶ前提(Routing.kt で囲む)。userId は JWT principal から。
fun Route.cartRoutes() {
    get("/cart") { call.respond(CartStore.get(call.userId())) }
    post("/cart/items") {
        val req = call.receive<AddCartItemRequest>()
        call.respond(CartStore.addItem(call.userId(), req.productId, req.quantity))
    }
    patch("/cart/items/{productId}") {
        val req = call.receive<SetQuantityRequest>()
        call.respond(CartStore.setQuantity(call.userId(), call.parameters["productId"]!!, req.quantity))
    }
    delete("/cart/items/{productId}") {
        call.respond(CartStore.remove(call.userId(), call.parameters["productId"]!!))
    }
    delete("/cart") { call.respond(CartStore.clear(call.userId())) }
}

fun Route.wishlistRoutes() {
    get("/wishlist") { call.respond(WishlistStore.list(call.userId())) }
    put("/wishlist/{productId}") {
        call.respond(WishlistStore.add(call.userId(), call.parameters["productId"]!!))
    }
    delete("/wishlist/{productId}") {
        call.respond(WishlistStore.remove(call.userId(), call.parameters["productId"]!!))
    }
}

fun Route.orderRoutes() {
    get("/orders") { call.respond(OrderStore.list(call.userId())) }
    get("/orders/{id}") {
        val order = OrderStore.get(call.userId(), call.parameters["id"]!!)
            ?: throw ApiException(HttpStatusCode.NotFound, "NOT_FOUND", "order not found")
        call.respond(order)
    }
    post("/orders") {
        val req = call.receive<PlaceOrderRequest>()
        call.respond(HttpStatusCode.Created, OrderStore.place(call.userId(), req))
    }
}

fun Route.accountRoutes() {
    get("/addresses") { call.respond(AccountStore.addresses(call.userId())) }
    post("/addresses") { call.respond(AccountStore.upsertAddress(call.userId(), call.receive<Address>().copy(id = ""))) }
    put("/addresses/{id}") {
        val body = call.receive<Address>().copy(id = call.parameters["id"]!!)
        call.respond(AccountStore.upsertAddress(call.userId(), body))
    }
    delete("/addresses/{id}") { call.respond(AccountStore.deleteAddress(call.userId(), call.parameters["id"]!!)) }
    post("/addresses/{id}/default") { call.respond(AccountStore.setDefaultAddress(call.userId(), call.parameters["id"]!!)) }

    get("/payment-methods") { call.respond(AccountStore.payments(call.userId())) }
    post("/payment-methods") { call.respond(AccountStore.upsertPayment(call.userId(), call.receive<PaymentMethod>().copy(id = ""))) }
    put("/payment-methods/{id}") {
        val body = call.receive<PaymentMethod>().copy(id = call.parameters["id"]!!)
        call.respond(AccountStore.upsertPayment(call.userId(), body))
    }
    delete("/payment-methods/{id}") { call.respond(AccountStore.deletePayment(call.userId(), call.parameters["id"]!!)) }
    post("/payment-methods/{id}/default") { call.respond(AccountStore.setDefaultPayment(call.userId(), call.parameters["id"]!!)) }
}
