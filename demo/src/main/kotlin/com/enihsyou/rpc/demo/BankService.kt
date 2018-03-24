package com.enihsyou.rpc.demo

import com.enihsyou.rpc.core.JsonRpcError
import com.enihsyou.rpc.core.JsonRpcMethod
import com.enihsyou.rpc.core.JsonRpcService
import java.math.BigDecimal
import java.util.*

@JsonRpcService
interface IBankService {

    @JsonRpcMethod
    fun login(username: String, password: String): UUID

    @JsonRpcMethod
    fun register(username: String, password: String)

    @JsonRpcMethod
    fun check(token: UUID): BigDecimal

    @JsonRpcMethod
    fun deposit(token: UUID, amount: BigDecimal): Boolean

    @JsonRpcMethod
    fun withdraw(token: UUID, amount: BigDecimal): Boolean

    @JsonRpcMethod
    fun transfer(token: UUID, amount: BigDecimal, to: String): Boolean
}

@JsonRpcService
class BankService : IBankService {

    private val users = mutableListOf<User>()

    private val loginCredential = mutableMapOf<UUID, User>()

    @JsonRpcMethod
    override fun login(username: String, password: String): UUID {
        val user = users
            .find { it.username == username }
            ?.takeIf { it.password == password }
            ?: throw CredentialException(username)

        val key = UUID.randomUUID()
        loginCredential[key] = user
        return key
    }

    @JsonRpcMethod
    override fun register(username: String, password: String) {
        users += User(username, password, BigDecimal.ZERO)
    }

    @JsonRpcMethod
    override fun check(token: UUID): BigDecimal =
        loginCredential[token]?.money ?: throw NeedCredentialException()

    @JsonRpcMethod
    override fun deposit(token: UUID, amount: BigDecimal): Boolean =
        loginCredential[token]?.apply { money = money.add(amount) } != null

    @JsonRpcMethod
    override fun withdraw(token: UUID, amount: BigDecimal): Boolean =
        loginCredential[token]?.apply { money = money.subtract(amount) } != null

    @JsonRpcMethod
    override fun transfer(token: UUID, amount: BigDecimal, to: String): Boolean =
        loginCredential[token]?.let { o1 ->
            users.find { it.username == to }
                ?.let { o2 ->
                    o1.money = o1.money.subtract(amount)
                    o2.money = o2.money.add(amount)
                }
        } != null
}

@JsonRpcError(1, "用户名或密码不正确")
class CredentialException(username: String) : RuntimeException("$username 登录失败")

@JsonRpcError(2, "用户未登录")
class NeedCredentialException : RuntimeException("用户未登录")
