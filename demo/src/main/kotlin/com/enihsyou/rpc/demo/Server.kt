package com.enihsyou.rpc.demo

import com.enihsyou.rpc.server.JsonRpcServer
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.ServerSocketChannel
import java.nio.channels.SocketChannel
import kotlin.concurrent.thread

class Server {
    private val service: Any = SimpleAddService()
    private val server: JsonRpcServer = JsonRpcServer(service)

    init {
        val selector = Selector.open()
        val socketChannel = ServerSocketChannel.open()
        val listenAddress = InetSocketAddress(20001)
        socketChannel.bind(listenAddress).configureBlocking(false)
        println("Server listening on ${socketChannel.localAddress}")
        socketChannel.register(selector, socketChannel.validOps(), null)
        thread {
            while (true) {
                selector.select()

                val selectedKeys = selector.selectedKeys()
                for (key in selectedKeys) {
                    when {
                        key.isAcceptable -> {
                            val client = socketChannel.accept()
                            if (client != null) {
                                client.configureBlocking(false)

                                client.register(selector, SelectionKey.OP_READ)
                                println("connected to ${client.localAddress}")
                            }
                        }

                        key.isReadable   -> {
                            val client = key.channel() as SocketChannel
                            val buffer = ByteBuffer.allocate(256)
                            client.read(buffer)
                            val request = String(buffer.array()).trim(0.toChar())
                            println("received $request")

                            handleRequest(request, client)
                            client.close()
                        }
                    }
                }
            }
        }
    }

    private fun sendResponse(message: String, socketChannel: SocketChannel) {
        val buffer = ByteBuffer.wrap(message.toByteArray())
        socketChannel.write(buffer)
        buffer.clear()
    }

    private fun handleRequest(incomingRequest: String, socketChannel: SocketChannel) {
        val outgoingResponse = server.handle(incomingRequest, service)
        println("processed $outgoingResponse")
        sendResponse(outgoingResponse, socketChannel)
    }
}

fun main(args: Array<String>) {
    Server()
}
