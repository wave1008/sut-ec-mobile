package com.sutec.mobile.server.repository

import com.sutec.mobile.data.model.Address
import com.sutec.mobile.data.model.PaymentMethod
import com.sutec.mobile.data.model.PaymentType
import com.sutec.mobile.server.db.Addresses
import com.sutec.mobile.server.db.PaymentMethods
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.util.UUID

// 住所・支払いはユーザー単位。upsert は id 空で新規採番、それ以外は更新(client AccountRepository 準拠)。
object AccountStore {
    private suspend fun <T> dbQuery(block: () -> T): T =
        withContext(Dispatchers.IO) { transaction { block() } }

    // ---- addresses ----
    private fun ResultRow.toAddress() = Address(
        id = this[Addresses.id],
        fullName = this[Addresses.fullName],
        postalCode = this[Addresses.postalCode],
        prefecture = this[Addresses.prefecture],
        city = this[Addresses.city],
        line1 = this[Addresses.line1],
        line2 = this[Addresses.line2],
        phone = this[Addresses.phone],
        isDefault = this[Addresses.isDefault],
    )

    private fun addressesOf(userId: UUID): List<Address> =
        Addresses.selectAll().where { Addresses.userId eq userId }
            .orderBy(Addresses.isDefault to SortOrder.DESC, Addresses.id to SortOrder.ASC)
            .map { it.toAddress() }

    suspend fun addresses(userId: UUID): List<Address> = dbQuery { addressesOf(userId) }

    suspend fun upsertAddress(userId: UUID, a: Address): List<Address> = dbQuery {
        if (a.id.isBlank()) {
            val newId = "addr_" + UUID.randomUUID().toString().take(8)
            Addresses.insert {
                it[id] = newId
                it[Addresses.userId] = userId
                it[fullName] = a.fullName
                it[postalCode] = a.postalCode
                it[prefecture] = a.prefecture
                it[city] = a.city
                it[line1] = a.line1
                it[line2] = a.line2
                it[phone] = a.phone
                it[isDefault] = a.isDefault
            }
        } else {
            Addresses.update({ (Addresses.id eq a.id) and (Addresses.userId eq userId) }) {
                it[fullName] = a.fullName
                it[postalCode] = a.postalCode
                it[prefecture] = a.prefecture
                it[city] = a.city
                it[line1] = a.line1
                it[line2] = a.line2
                it[phone] = a.phone
                it[isDefault] = a.isDefault
            }
        }
        addressesOf(userId)
    }

    suspend fun deleteAddress(userId: UUID, id: String): List<Address> = dbQuery {
        Addresses.deleteWhere { (Addresses.id eq id) and (Addresses.userId eq userId) }
        addressesOf(userId)
    }

    suspend fun setDefaultAddress(userId: UUID, id: String): List<Address> = dbQuery {
        Addresses.update({ Addresses.userId eq userId }) { it[isDefault] = false }
        Addresses.update({ (Addresses.id eq id) and (Addresses.userId eq userId) }) { it[isDefault] = true }
        addressesOf(userId)
    }

    // ---- payment methods ----
    private fun ResultRow.toPayment() = PaymentMethod(
        id = this[PaymentMethods.id],
        type = PaymentType.valueOf(this[PaymentMethods.type]),
        brand = this[PaymentMethods.brand],
        last4 = this[PaymentMethods.last4],
        holderName = this[PaymentMethods.holderName],
        expMonth = this[PaymentMethods.expMonth],
        expYear = this[PaymentMethods.expYear],
        isDefault = this[PaymentMethods.isDefault],
    )

    private fun paymentsOf(userId: UUID): List<PaymentMethod> =
        PaymentMethods.selectAll().where { PaymentMethods.userId eq userId }
            .orderBy(PaymentMethods.isDefault to SortOrder.DESC, PaymentMethods.id to SortOrder.ASC)
            .map { it.toPayment() }

    suspend fun payments(userId: UUID): List<PaymentMethod> = dbQuery { paymentsOf(userId) }

    suspend fun upsertPayment(userId: UUID, m: PaymentMethod): List<PaymentMethod> = dbQuery {
        if (m.id.isBlank()) {
            val newId = "pay_" + UUID.randomUUID().toString().take(8)
            PaymentMethods.insert {
                it[id] = newId
                it[PaymentMethods.userId] = userId
                it[type] = m.type.name
                it[brand] = m.brand
                it[last4] = m.last4
                it[holderName] = m.holderName
                it[expMonth] = m.expMonth
                it[expYear] = m.expYear
                it[isDefault] = m.isDefault
            }
        } else {
            PaymentMethods.update({ (PaymentMethods.id eq m.id) and (PaymentMethods.userId eq userId) }) {
                it[type] = m.type.name
                it[brand] = m.brand
                it[last4] = m.last4
                it[holderName] = m.holderName
                it[expMonth] = m.expMonth
                it[expYear] = m.expYear
                it[isDefault] = m.isDefault
            }
        }
        paymentsOf(userId)
    }

    suspend fun deletePayment(userId: UUID, id: String): List<PaymentMethod> = dbQuery {
        PaymentMethods.deleteWhere { (PaymentMethods.id eq id) and (PaymentMethods.userId eq userId) }
        paymentsOf(userId)
    }

    suspend fun setDefaultPayment(userId: UUID, id: String): List<PaymentMethod> = dbQuery {
        PaymentMethods.update({ PaymentMethods.userId eq userId }) { it[isDefault] = false }
        PaymentMethods.update({ (PaymentMethods.id eq id) and (PaymentMethods.userId eq userId) }) { it[isDefault] = true }
        paymentsOf(userId)
    }
}
