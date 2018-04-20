package com.enihsyou.rpc.demo

import com.enihsyou.rpc.core.JsonRpcError
import com.enihsyou.rpc.core.JsonRpcMethod
import com.enihsyou.rpc.core.JsonRpcService
import java.util.*

/*使用UUID会遇到java.lang.String cannot be cast to java.util.UUID问题，使用String绕过*/
@JsonRpcService
interface BankService {

    @JsonRpcMethod
    fun login(username: String, password: String): String

    @JsonRpcMethod
    fun register(username: String, password: String): Boolean

    @JsonRpcMethod
    fun check(token: String): Double

    @JsonRpcMethod
    fun deposit(token: String, amount: Double): Boolean

    @JsonRpcMethod
    fun withdraw(token: String, amount: Double): Boolean

    @JsonRpcMethod
    fun transfer(token: String, amount: Double, to: String): Boolean
}

@JsonRpcService
class BankServiceImpl : BankService {

    private val users = mutableListOf<User>()

    private val loginCredential = mutableMapOf<String, User>()

    @JsonRpcMethod
    override fun login(username: String, password: String): String {
        val user = users
            .find { it.username == username }
            ?.takeIf { it.password == password }
            ?: throw CredentialException(username)

        val key = UUID.randomUUID().toString()
        loginCredential[key] = user
        return key
    }

    @JsonRpcMethod
    override fun register(username: String, password: String): Boolean {
        users.find { it.username == username }?.run { return false }
        users += User(username, password, 0.0)
        return true
    }

    @JsonRpcMethod
    override fun check(token: String): Double =
        loginCredential[token]?.money ?: throw NeedCredentialException()

    @JsonRpcMethod
    override fun deposit(token: String, amount: Double): Boolean =
        loginCredential[token]?.apply { money += amount } != null

    @JsonRpcMethod
    override fun withdraw(token: String, amount: Double): Boolean =
        loginCredential[token]?.takeIf { it.money >= amount }?.apply { money -= amount } != null

    @JsonRpcMethod
    override fun transfer(token: String, amount: Double, to: String): Boolean =
        loginCredential[token]?.let { o1 ->
            users.find { it.username == to }
                ?.takeIf { o1.money >= amount }
                ?.let { o2 ->
                    o1.money -= amount
                    o2.money += amount
                }
        } != null
}

@JsonRpcError(1, "用户名或密码不正确")
class CredentialException(username: String) : RuntimeException("$username 登录失败")

@JsonRpcError(2, "用户未登录")
class NeedCredentialException : RuntimeException("用户未登录")
