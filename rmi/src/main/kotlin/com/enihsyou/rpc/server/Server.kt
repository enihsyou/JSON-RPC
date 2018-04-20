package com.enihsyou.rpc.server

import java.rmi.registry.LocateRegistry
import java.rmi.server.UnicastRemoteObject

class Server : HelloService {

    override fun sayHello(): String {
        println("Client connect")
        return "HelloService, world!"
    }
}

fun main(args: Array<String>) {
    val obj = Server()
    val stub = UnicastRemoteObject.exportObject(obj, 0) as HelloService

    // Bind the remote object's stub in the registry
    val registry = LocateRegistry.getRegistry()
    registry.bind("HelloService", stub)

    System.err.println("Server ready")
}
