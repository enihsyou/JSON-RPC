package com.enihsyou.rpc.rmi

import org.apache.zookeeper.CreateMode
import org.apache.zookeeper.Watcher
import org.apache.zookeeper.ZooDefs
import org.apache.zookeeper.ZooKeeper
import org.slf4j.LoggerFactory
import java.rmi.Naming
import java.rmi.Remote
import java.rmi.registry.LocateRegistry
import java.util.*
import java.util.concurrent.CountDownLatch

/*https://my.oschina.net/huangyong/blog/345164*/
fun main(args: Array<String>) {
    val host = "localhost"
    val port = 1099 + Random().nextInt(1000)
    val service = "com.enihsyou.rpc.rmi.BankService"
    val rmiUrl = "rmi://$host:$port/$service"

    val provider = ServiceProvider()
    provider.publish(BankServiceImpl(), host, port, rmiUrl)

    System.err.println("RmiServer ready")
}

object ZkConstant {

    const val ZK_CONNECTION_STRING = "localhost:2181"
    const val ZK_SESSION_TIMEOUT = 5000
    const val ZK_REGISTRY_PATH = "/registry"
    const val ZK_PROVIDER_PATH = "$ZK_REGISTRY_PATH/provider"
}

class ServiceProvider {
    private val latch = CountDownLatch(1)

    // 发布 RMI 服务并注册 RMI 地址到 ZooKeeper 中
    fun publish(remote: Remote, host: String, port: Int, rmiUrl: String) {
        // 发布 RMI 服务并返回 RMI 地址
        publishService(remote, host, port, rmiUrl)
        // 连接 ZooKeeper 服务器并获取 ZooKeeper 对象
        val zooKeeper = connectServer(ZkConstant.ZK_CONNECTION_STRING, ZkConstant.ZK_SESSION_TIMEOUT)
        // 创建 ZNode 并将 RMI 地址放入 ZNode 上
        createNode(zooKeeper, rmiUrl)
    }

    // 连接 ZooKeeper 服务器
    private fun connectServer(zkServer: String, zkTimeout: Int): ZooKeeper =
        ZooKeeper(zkServer, zkTimeout) { if (it.state == Watcher.Event.KeeperState.SyncConnected) latch.countDown() }
            .apply { latch.await().also { LOGGER.debug("ZooKeeper connected (sessionId: {})", sessionId) } }

    // 发布 RMI 服务
    private fun publishService(remote: Remote, host: String, port: Int, rmiUrl: String) {
        LocateRegistry.createRegistry(port)
        Naming.rebind(rmiUrl, remote)
        LOGGER.debug("RMI service published (url: {})", rmiUrl)
    }

    // 创建 ZNode
    private fun createNode(zk: ZooKeeper, url: String) {
        // 创建一个临时性且有序的 ZNode
        val path = zk.create(
            ZkConstant.ZK_PROVIDER_PATH,
            url.toByteArray(),
            ZooDefs.Ids.OPEN_ACL_UNSAFE,
            CreateMode.EPHEMERAL_SEQUENTIAL
        )
        LOGGER.debug("create zookeeper node ({} => {})", path, url)
    }

    companion object {

        private val LOGGER = LoggerFactory.getLogger(ServiceProvider::class.java)
    }
}
