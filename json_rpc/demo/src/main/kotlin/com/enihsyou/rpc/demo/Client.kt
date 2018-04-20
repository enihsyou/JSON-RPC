package com.enihsyou.rpc.demo

import com.enihsyou.rpc.client.JsonRpcClient
import com.enihsyou.rpc.client.Transport
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.net.InetAddress
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.SocketChannel
import java.util.*
import kotlin.reflect.KFunction1
import kotlin.reflect.jvm.isAccessible
import kotlin.system.exitProcess

class Client(port: Int = 20001) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    private val client = JsonRpcClient(object : Transport {
        override fun pass(request: String): String {
            val connectAddress = InetSocketAddress(InetAddress.getLocalHost(), port)
            val socket = SocketChannel.open(connectAddress)

            logger.info("Connecting to server at ${socket.remoteAddress}")

            val buffer = ByteBuffer.wrap(request.toByteArray())
            socket.write(buffer)
            buffer.clear()
            logger.info("Client send:\n$request")

            val byteBuffer = ByteBuffer.allocate(256)
            socket.read(byteBuffer)
            val response = String(byteBuffer.array()).trim(0.toChar())
            logger.info("Client received:\n$response")

            socket.close()
            return response
        }
    })

    fun login(username: String, password: String): String? {
        val params = mapOf(
            "username" to username,
            "password" to password
        )
        return client.createRequest<String>("login", params).execute()
    }

    fun register(username: String, password: String): Boolean {
        val params = mapOf(
            "username" to username,
            "password" to password
        )
        return client.createRequest<Boolean>("register", params).execute() ?: false
    }

    fun check(token: String): Double? {
        val params = mapOf(
            "token" to token
        )
        return client.createRequest<Double>("check", params).execute()
    }

    fun deposit(token: String, amount: Double): Boolean {
        val params = mapOf(
            "token" to token,
            "amount" to amount
        )
        return client.createRequest<Boolean>("deposit", params).execute() ?: false
    }

    fun withdraw(token: String, amount: Double): Boolean {
        val params = mapOf(
            "token" to token,
            "amount" to amount
        )
        return client.createRequest<Boolean>("withdraw", params).execute() ?: false
    }

    fun transfer(token: String, amount: Double, to: String): Boolean {
        val params = mapOf(
            "token" to token,
            "amount" to amount,
            "to" to to
        )
        return client.createRequest<Boolean>("transfer", params).execute() ?: false
    }

    private class BadInputException : RuntimeException()
}

class Bank(private val client: Client) {
    private val scanner = Scanner(System.`in`)
    private var username: String? = null
    private var token: String? = null

    init {
        println("欢迎来到NTM银行！")
        println("请选择操作")
        splashScreen()
    }

    private fun userInput(availableFunctions: List<KFunction1<Bank, Unit>>): KFunction1<Bank, Unit> {
        print(">>> ")
        val select = scanner.nextInt() - 1
        if (select !in availableFunctions.indices) {
            System.err.println("不正确的输入")
            exitProcess(1)
        }
        return availableFunctions[select]
    }

    private inline fun <reified R> userInput(): R {
        return when (R::class) {
            String::class     -> scanner.next() as R
            BigDecimal::class -> scanner.nextBigDecimal() as R
            Double::class     -> scanner.nextDouble() as R
            else              -> throw NotImplementedError()
        }
    }

    /*调用private final*/
    private fun call(select: KFunction1<Bank, Unit>) {
        val original = select.isAccessible
        select.isAccessible = true
        select.call(this)
        select.isAccessible = original
    }

    private fun splashScreen() {
        val message = """
            1. 登录
            2. 注册
            3. 退出
        """.trimIndent()
        println(message)
        val select = userInput(listOf(Bank::login, Bank::register, Bank::exit))
        call(select)
    }

    private fun bankScreen() {
        val message = """
            $username 您现在可进行的操作有
            1. 查询余额
            2. 存款
            3. 取款
            4. 转账
            5. 登出
        """.trimIndent()
        println(message)
        val select = userInput(listOf(Bank::check, Bank::deposit, Bank::withdraw, Bank::transfer, Bank::logout))
        call(select)
    }

    private fun login() {
        println("用户名：")
        val username = userInput<String>()
        println("密码：")
        val password = userInput<String>()

        fun handleLoginError() {
            println("用户名或密码错误")
            splashScreen()
        }

        fun handleLoginSuccess(username: String, token: String) {
            println("登录成功！")
            this.username = username
            this.token = token
            bankScreen()
        }

        client.login(username, password)
            ?.run { handleLoginSuccess(username, this) }
            ?: handleLoginError()
    }

    private fun register() {
        println("用户名：")
        val username = userInput<String>()
        println("密码：")
        val password = userInput<String>()

        fun handleRegisterError() {
            println("用户名重复")
            splashScreen()
        }

        fun handleRegisterSuccess() {
            println("注册成功！现在可以用新账户进行登录")
            splashScreen()
        }

        if (!client.register(username, password))
            handleRegisterError()
        else
            handleRegisterSuccess()
    }

    private fun showSuccessMessage() {
        println("成功！")
    }

    private fun showFailedMessage() {
        println("失败！")
    }

    private fun deposit() {
        token ?: throw IllegalStateException()
        username ?: throw IllegalStateException()
        println("请输入存款金额：")
        val amount = userInput<Double>().takeIf { it >= 0.0 } ?: return {
            showFailedMessage()
            bankScreen()
        }()

        val success = client.deposit(token!!, amount)
        if (success) {
            showSuccessMessage()
        } else {
            showFailedMessage()
        }
        bankScreen()
    }

    private fun withdraw() {
        token ?: throw IllegalStateException()
        username ?: throw IllegalStateException()
        println("请输入取款金额：")
        val amount = userInput<Double>().takeIf { it >= 0.0 } ?: return {
            showFailedMessage()
            bankScreen()
        }()

        val success = client.withdraw(token!!, amount)
        if (success) {
            showSuccessMessage()
        } else {
            showFailedMessage()
        }
        bankScreen()
    }

    private fun transfer() {
        token ?: throw IllegalStateException()
        username ?: throw IllegalStateException()
        println("请输入转账金额")
        val amount = userInput<Double>().takeIf { it >= 0.0 } ?: return {
            showFailedMessage()
            bankScreen()
        }()

        println("请输入转给谁")
        val to = userInput<String>()

        val success = client.transfer(token!!, amount, to)
        if (success) {
            showSuccessMessage()
        } else {
            showFailedMessage()
        }
        bankScreen()
    }

    private fun check() {
        token ?: throw IllegalStateException()
        username ?: throw IllegalStateException()

        fun showAmountMessage(amount: Double) {
            println("用户${username}当前余额为￥$amount")
        }

        client.check(token!!)
            ?.let { showAmountMessage(it) }
            ?: showFailedMessage()

        bankScreen()
    }

    private fun logout() {
        token = null
        username = null
        println("登出成功！")
        splashScreen()
    }

    private fun exit(){
        println("NTM银行感谢您的使用！")
        exitProcess(0)
    }
}

fun main(args: Array<String>) {
    Bank(Client())
}
