package com.enihsyou.rpc.demo

import com.enihsyou.rpc.server.JsonRpcServer
import kotlinx.coroutines.experimental.launch
import org.slf4j.LoggerFactory
import java.net.InetAddress
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.ServerSocketChannel
import java.nio.channels.SocketChannel
import kotlin.concurrent.thread

class Server(port: Int = 20001) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    private val service = BankServiceImpl()
    private val server: JsonRpcServer = JsonRpcServer(service)

    init {
        val selector = Selector.open()
        val listenAddress = InetSocketAddress(InetAddress.getLocalHost(), port)
        val socketChannel = ServerSocketChannel.open()
        socketChannel
            .bind(listenAddress)
            .configureBlocking(false)
            .register(selector, socketChannel.validOps(), null)
        logger.info("Server is listening on ${socketChannel.localAddress}")

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
                            launch {
                                logger.debug(this.coroutineContext.toString())
                                handleRequest(request, client)
                                client.close()
                            }
                        }
                    }
                }
            }
        }.join()
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

