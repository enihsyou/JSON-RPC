package com.enihsyou.rpc.rmi

import java.io.Serializable
import java.math.BigDecimal
import java.rmi.Remote
import java.rmi.RemoteException
import java.rmi.server.UnicastRemoteObject
import java.sql.Connection
import java.sql.DriverManager
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
    val money: Money
) : Serializable

interface BankService : Remote {

    @Throws(RemoteException::class)
    fun login(username: Username, password: Password): LoginVO

    @Throws(RemoteException::class)
    fun register(username: Username, password: Password)

    @Throws(RemoteException::class)
    fun check(username: String, password: String): Money

    @Throws(RemoteException::class)
    fun deposit(username: String, password: String, amount: Money)

    @Throws(RemoteException::class)
    fun withdraw(username: String, password: String, amount: Money)

    @Throws(RemoteException::class)
    fun transfer(username: String, password: String, amount: Money, to: Username)
}

class BankServiceImpl : UnicastRemoteObject(), BankService {
    private val connection by lazy { connect() }

    private fun connect(): Connection {
        val props = Properties().apply {
            put("user", "root")
            put("password", "enihsyou")
        }

        return DriverManager.getConnection("jdbc:mysql://192.168.0.100:3306?useSSL=false", props)
    }

    private fun queryUser(username: Username): User? {
        val statement = connection
            .prepareStatement("SELECT * FROM rmi.user WHERE username=?")
            .apply {
                setString(1, username)
            }

        val resultSet = statement.executeQuery()
        if (resultSet.next()) {
            val _username = resultSet.getString("username")
            val _password = resultSet.getString("password")
            val _money = resultSet.getBigDecimal("money")
            return User(_username, _password, _money)
        }
        return null
    }

    private fun createUser(username: Username, password: Password) {
        val statement = connection
            .prepareStatement("INSERT INTO rmi.user (username, password) VALUES (?,?);")
            .apply {
                setString(1, username)
                setString(2, password)
            }

        val resultSet = statement.executeUpdate()
        if (resultSet != 1) throw UserExistException(username)
    }

    private fun updateMoney(username: Username, money: Money) {
        val statement = connection
            .prepareStatement("UPDATE rmi.user SET money=? WHERE username=?")
            .apply {
                setBigDecimal(1, money)
                setString(2, username)
            }

        val resultSet = statement.executeUpdate()
        if (resultSet != 1) throw UserShouldExistException(username)
    }

    override fun login(username: String, password: String): LoginVO {
        debug { "[login] username: $username, password: $password" }
        val user = loggedUser(username, password)
        return LoginVO(user.username, user.money)
    }

    override fun register(username: String, password: String) {
        debug { "[register] username: $username, password: $password" }
        queryUser(username)?.run { throw UserExistException(username) }
        createUser(username, password)
    }

    override fun check(username: String, password: String): Money =
        loggedUser(username, password)
            .also { debug { "[check] username: ${it.username}, money: ${it.money}" } }.money

    override fun deposit(username: String, password: String, amount: Money) =
        loggedUser(username, password)
            .also { debug { "[deposit] username: ${it.username}, amount: $amount" } }
            .run { deposit(this, amount) }

    override fun withdraw(username: String, password: String, amount: Money) =
        loggedUser(username, password)
            .also { debug { "[withdraw] username: ${it.username}, money: ${it.money}, amount: $amount" } }
            .run { withdraw(this, amount) }

    override fun transfer(username: String, password: String, amount: Money, to: Username) {
        val user = loggedUser(username, password)
        val userTo = queryUser(to) ?: throw UserShouldExistException(to)
        debug { "[deposit] from: ${user.username}, to: ${userTo.username}, amount: $amount" }

        withdraw(user, amount)
        deposit(userTo, amount)
    }

    private fun loggedUser(username: String, password: String): User {
        val user = queryUser(username) ?: throw UserShouldExistException(username)
        if (user.password != password) throw CredentialException(username)
        return user
    }

    private fun deposit(user: User, amount: Money) {
        updateMoney(user.username, user.money + amount)
    }

    private fun withdraw(user: User, amount: Money) {
        if (user.money >= amount) {
            updateMoney(user.username, user.money - amount)
        } else throw BalanceException(user, amount)
    }
}

class UserShouldExistException(username: Username) : RuntimeException("$username 不存在")

class UserExistException(username: Username) : RuntimeException("$username 已存在")

class CredentialException(username: String) : RuntimeException("$username 登录失败")

class BalanceException(user: User, amount: Money) : RuntimeException("用户[${user.username}的余额不足以完成一次体量为[$amount]的操作")

private inline fun debug(msg: () -> String) = println(msg())
