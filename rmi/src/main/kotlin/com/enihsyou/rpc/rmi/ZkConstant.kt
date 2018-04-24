package com.enihsyou.rpc.rmi

object ZkConstant {

    const val ZK_CONNECTION_STRING = "192.168.0.100:2181"
    const val ZK_SESSION_TIMEOUT = 5000
    const val ZK_REGISTRY_PATH = "/registry"
    const val ZK_PROVIDER_PATH = "$ZK_REGISTRY_PATH/provider"
}
