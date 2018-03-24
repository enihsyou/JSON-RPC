package com.enihsyou.rpc.client

import com.enihsyou.rpc.core.message.Request
import com.enihsyou.rpc.core.message.Response
import com.enihsyou.rpc.core.message.SuccessResponse
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import kotlin.reflect.KClass

class JsonRpcClient(val transport: Transport) {

    private val mapper = jacksonObjectMapper()
        .configure(SerializationFeature.INDENT_OUTPUT, true)

    fun <R> createRequest(method: String, params: Map<String, Any>) =
        RequestBuilder<R>(transport, mapper, Response::class, method, params)

    class RequestBuilder<out R>(
        val transport: Transport,
        val mapper: ObjectMapper,
        val type: KClass<*>,
        val method: String,
        val params: Map<String, Any>
    ) {

        fun excute(): R {
            val request = Request(method, params)
            val textResponse =
                transport.pass(mapper.writeValueAsString(request))
            return mapper.readValue<SuccessResponse<R>>(textResponse).result
        }
    }
}

