package com.enihsyou.rpc.rmi

import java.rmi.registry.LocateRegistry
import java.rmi.server.UnicastRemoteObject

class Server : BankService by BankServiceImpl()

fun main(args: Array<String>) {
    val obj = Server()
    val stub = UnicastRemoteObject.exportObject(obj, 0) as BankService

    // Bind the remote object's stub in the registry
    val registry = LocateRegistry.getRegistry()
    registry.bind("BankService", stub)

    System.err.println("Server ready")
//    registry.unbind("BankService")
}
