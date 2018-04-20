package com.enihsyou.rpc.server

import java.rmi.registry.LocateRegistry


fun main(args: Array<String>) {
    val host = if (args.isEmpty()) null else args[0]

    val registry = LocateRegistry.getRegistry(host)
    val stub = registry.lookup("HelloService") as HelloService
    val response = stub.sayHello()
    println("response: $response")
}
