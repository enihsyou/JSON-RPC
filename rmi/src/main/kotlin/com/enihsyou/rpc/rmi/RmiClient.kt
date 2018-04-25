package com.enihsyou.rpc.rmi

import org.apache.zookeeper.Watcher
import org.apache.zookeeper.ZooKeeper
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.math.RoundingMode
import java.net.URI
import java.rmi.Remote
import java.rmi.registry.LocateRegistry
import java.util.*
import java.util.concurrent.CountDownLatch
import kotlin.reflect.KFunction1
import kotlin.reflect.jvm.isAccessible
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    val consumer = ServiceConsumer()
    val bankService = consumer.lookup<BankService>()
    Bank(bankService)
}

class Bank(private val service: BankService) {
    private val scanner = Scanner(System.`in`)
    private var username: String? = null
    private var password: String? = null

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
            用户[$username]: 您现在可进行的操作有
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

        try {
            val success = service.login(username, password)
            this.username = success.username
            this.password = password
            println("登录成功！")
            bankScreen()
        } catch (e: Exception) {
            when (e) {
                is UserShouldExistException -> println("用户名不存在")
                is CredentialException      -> println("用户名或密码错误")
                else                        -> e.printStackTrace()
            }
            splashScreen()
        }
    }

    private fun register() {
        println("用户名：")
        val username = userInput<String>()
        println("密码：")
        val password = userInput<String>()

        try {
            service.register(username, password)
            println("注册成功！现在可以用新账户进行登录")
        } catch (e: Exception) {
            when (e) {
                is UserExistException -> println(e.message)
                else                  -> println("用户名重复").also { e.printStackTrace() }
            }
        } finally {
            splashScreen()
        }
    }

    private fun deposit() {
        username ?: throw IllegalStateException()
        password ?: throw IllegalStateException()

        println("请输入存款金额：")
        val amount = userInput<BigDecimal>().takeIf { it.signum() >= 0 } ?: return {
            println("输入存款金额需要个正数")
            bankScreen()
        }()

        service.deposit(username!!, password!!, amount)
        println("操作成功！")

        bankScreen()
    }

    private fun withdraw() {
        username ?: throw IllegalStateException()
        println("请输入取款金额：")
        val amount = userInput<BigDecimal>().takeIf { it.signum() >= 0 } ?: return {
            println("输入的取款金额需要个正数")
            bankScreen()
        }()

        try {
            service.withdraw(username!!, password!!, amount)
            println("操作成功！")
        } catch (e: Exception) {
            when (e) {
                is BalanceException -> println("失败！余额不足")
                else                -> e.printStackTrace()
            }
        }
        bankScreen()
    }

    private fun transfer() {
        username ?: throw IllegalStateException()
        password ?: throw IllegalStateException()

        println("请输入转账金额")
        val amount = userInput<BigDecimal>().takeIf { it.signum() >= 0 } ?: return {
            println("失败！")
            bankScreen()
        }()

        println("请输入转给谁")
        val to = userInput<String>()
        try {
            service.transfer(username!!, password!!, amount, to)
            println("操作成功！")
        } catch (e: Exception) {
            when (e) {
                is BalanceException         -> println(e.message)
                is UserShouldExistException -> println(e.message)
                else                        -> println("失败！").also { e.printStackTrace() }
            }
        }
        bankScreen()
    }

    private fun check() {
        username ?: throw IllegalStateException()
        password ?: throw IllegalStateException()

        fun showAmountMessage(amount: BigDecimal) {
            println("用户${username}当前余额为￥${amount.setScale(2, RoundingMode.DOWN)}")
        }

        val money = service.check(username!!, password!!)
        showAmountMessage(money)

        bankScreen()
    }

    private fun logout() {
        username = null
        password = null
        println("登出成功！")
        splashScreen()
    }

    private fun exit() {
        println("NTM银行感谢您的使用！")
        exitProcess(0)
    }
}

class ServiceConsumer {

    // 用于等待 SyncConnected 事件触发后继续执行当前线程
    private val latch = CountDownLatch(1)

    // 定义一个 volatile 成员变量，用于保存最新的 RMI 地址（考虑到该变量或许会被其它线程所修改，一旦修改后，该变量的值会影响到所有线程）
    @Volatile
    private var urlList: List<String> = ArrayList()

    // 构造器
    init {
        // 连接 ZooKeeper 服务器并获取 ZooKeeper 对象
        val zk = connectServer(ZkConstant.ZK_CONNECTION_STRING, ZkConstant.ZK_SESSION_TIMEOUT)
        // 观察 /registry 节点的所有子节点并更新 urlList 成员变量
        watchNode(zk)
    }

    // 查找 RMI 服务
    fun <T : Remote> lookup(): T = urlList.shuffled().first().let { lookupService(it) }

    // 连接 ZooKeeper 服务器
    private fun connectServer(zkServer: String, zkTimeout: Int): ZooKeeper =
        ZooKeeper(zkServer, zkTimeout) { if (it.state == Watcher.Event.KeeperState.SyncConnected) latch.countDown() }
            .apply { latch.await().also { LOGGER.debug("ZooKeeper connected (sessionId: {})", sessionId) } }

    // 观察 /registry 节点下所有子节点是否有变化
    private fun watchNode(zk: ZooKeeper) {

        val nodeList = zk
            // 若子节点有变化，则重新调用该方法（为了获取最新子节点中的数据）
            .getChildren(ZkConstant.ZK_REGISTRY_PATH) {
                if (it.type == Watcher.Event.EventType.NodeChildrenChanged) watchNode(zk)
            }
        val dataList = nodeList
            .asSequence()
            .map {
                zk.getData(ZkConstant.ZK_REGISTRY_PATH + "/" + it, false, null) // 获取 /registry 的子节点中的数据
            }
            .map { String(it) }
            .toList() // 用于存放 /registry 所有子节点中的数据
        LOGGER.debug("node data: {}", dataList)
        urlList = dataList // 更新最新的 RMI 地址
    }

    // 在 JNDI 中查找 RMI 远程服务对象
    private fun <T : Remote> lookupService(rmiUrl: String): T {
        val host = URI.create(rmiUrl).host
        val port = RmiConstant.Rmi_CONNECTION_PORT
        LOGGER.debug("Get Proxy from $host:$port")
        return LocateRegistry.getRegistry(host, port).lookup(rmiUrl) as T
    }

    companion object {

        private val LOGGER = LoggerFactory.getLogger(ServiceConsumer::class.java)
    }
}
