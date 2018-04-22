package com.enihsyou.rpc.rmi

import java.io.Serializable
import java.math.BigDecimal
import java.rmi.Remote
import java.rmi.RemoteException
import java.util.*

private typealias Username = String
private typealias Password = String
private typealias Token = UUID
private typealias Money = BigDecimal

data class User(
    val username: Username,
    val password: Password,
    var money: Money
) : Serializable

data class LoginVO(
    val username: Username,
    val money: Money,
    val token: Token
) : Serializable

/*使用UUID会遇到java.lang.String cannot be cast to java.util.UUID问题，使用String绕过*/
interface BankService : Remote {

    @Throws(RemoteException::class)
    fun login(username: Username, password: Password): LoginVO

    @Throws(RemoteException::class)
    fun register(username: Username, password: Password)

    @Throws(RemoteException::class)
    fun check(token: Token): Money

    @Throws(RemoteException::class)
    fun deposit(token: Token, amount: Money)

    @Throws(RemoteException::class)
    fun withdraw(token: Token, amount: Money)

    @Throws(RemoteException::class)
    fun transfer(token: Token, amount: Money, to: Username)
}

internal class BankServiceImpl : BankService {

    private val users = mutableListOf<User>()

    private val loginCredential = mutableMapOf<Token, User>()

    override fun login(username: String, password: String): LoginVO {
        debug { "[login] username: $username, password: $password" }
        val user = findUser(username) ?: throw UserShouldExistException(username)

        if (user.password != password) throw CredentialException(username)

        val key = UUID.randomUUID()
        loginCredential[key] = user
        debug { "[login] username: $username success token: $key" }
        return LoginVO(user.username, user.money, key)
    }

    override fun register(username: String, password: String) {
        debug { "[register] username: $username, password: $password" }
        findUser(username)?.run { throw UserExistException(username) }
        users += User(username, password, BigDecimal.ZERO)
    }

    override fun check(token: Token): Money =
        loggedUser(token)
            .also { debug { "[check] username: ${it.username}, money: ${it.money}" } }.money

    override fun deposit(token: Token, amount: Money) =
        loggedUser(token)
            .also { debug { "[deposit] username: ${it.username}, amount: $amount" } }
            .run { deposit(this, amount) }

    override fun withdraw(token: Token, amount: Money) =
        loggedUser(token)
            .also { debug { "[withdraw] username: ${it.username}, money: ${it.money}, amount: $amount" } }
            .run { withdraw(this, amount) }

    override fun transfer(token: Token, amount: Money, to: Username) {
        val user = loggedUser(token)
        val userTo = findUser(to) ?: throw UserShouldExistException(to)
        debug { "[deposit] from: ${user.username}, to: ${userTo.username}, amount: $amount" }

        withdraw(user, amount)
        deposit(userTo, amount)
    }

    private fun findUser(username: String) = users.find { it.username == username }

    private fun loggedUser(token: Token) = loginCredential[token] ?: throw NeedCredentialException()

    private fun deposit(user: User, amount: Money) {
        user.money += amount
    }

    private fun withdraw(user: User, amount: Money) {
        if (user.money >= amount) {
            user.money -= amount
        } else throw BalanceException(user, amount)
    }
}

class UserShouldExistException(username: Username) : RuntimeException("$username 不存在")

class UserExistException(username: Username) : RuntimeException("$username 已存在")

class CredentialException(username: String) : RuntimeException("$username 登录失败")

class BalanceException(user: User, amount: Money) : RuntimeException("用户[${user.username}的余额不足以完成一次体量为[$amount]的操作")

class NeedCredentialException : RuntimeException("用户未登录")

private inline fun debug(msg: () -> String) = println(msg())
