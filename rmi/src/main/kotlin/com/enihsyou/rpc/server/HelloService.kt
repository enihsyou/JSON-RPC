package com.enihsyou.rpc.server

import java.rmi.Remote
import java.rmi.RemoteException

interface HelloService : Remote {

    @Throws(RemoteException::class)
    fun sayHello(): String
}
