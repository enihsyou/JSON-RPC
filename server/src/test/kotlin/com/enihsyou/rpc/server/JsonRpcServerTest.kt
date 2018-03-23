package com.enihsyou.rpc.server

import org.assertj.core.api.Assertions.assertThat

import org.junit.jupiter.api.Test

internal class JsonRpcServerTest {
    private val service = SimpleAddService()
    private val server = JsonRpcServer(service)

    @Test
    fun handle() {
        //language=JSON
        val request = "{\n  \"method\": \"add\",\n  \"params\": {\n    \"a\": 1,\n    \"b\": 2\n  }\n}"
        val response =
            server.handle(request, service)
        println(response)
        assertThat(response)
            .isNotBlank()
    }
}
