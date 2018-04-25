package com.enihsyou.rpc.rmi

object ZkConstant {

    const val ZK_CONNECTION_STRING = "192.168.0.100:2181"
    const val ZK_SESSION_TIMEOUT = 2000
    const val ZK_REGISTRY_PATH = "/registry"
    const val ZK_PROVIDER_PATH = "$ZK_REGISTRY_PATH/provider"
}

object RmiConstant {

    const val Rmi_CONNECTION_HOST = "192.168.0.100"
    const val Rmi_CONNECTION_PORT = 10090
}
