package com.enihsyou.rpc.demo

import com.enihsyou.rpc.client.JsonRpcClient
import com.enihsyou.rpc.client.Transport
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.SocketChannel

class TransportImpl : Transport {
    override fun pass(request: String): String {
        val connectAddress = InetSocketAddress(20001)
        val socket = SocketChannel.open(connectAddress)

        println("Connecting to server at ${socket.remoteAddress}")

        val buffer = ByteBuffer.wrap(request.toByteArray())
        socket.write(buffer)
        buffer.clear()
        println("Send $request")

        val byteBuffer = ByteBuffer.allocate(256)
        socket.read(byteBuffer)
        val response = String(byteBuffer.array()).trim(0.toChar())
        socket.close()
        return response
    }
}

class Client {

    fun add(amount: String) {
        val client = JsonRpcClient(TransportImpl())

        println(
            client.createRequest<Int>(
                "add", mapOf(
                    "a" to 1,
                    "b" to 2
                )
            ).excute() + 2
        )
    }
}

fun main(args: Array<String>) {
    System.out.println("total = " + Runtime.getRuntime().totalMemory()/1024/1024)
    System.out.println("max   = " + Runtime.getRuntime().maxMemory() / 1024 / 1024)
    Client().add("")
}
