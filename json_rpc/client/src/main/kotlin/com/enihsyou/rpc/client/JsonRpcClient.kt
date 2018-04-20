package com.enihsyou.rpc.client

import com.enihsyou.rpc.core.message.Request
import com.enihsyou.rpc.core.message.SuccessResponse
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue

class JsonRpcClient(val transport: Transport) {

    private val mapper = jacksonObjectMapper()
        .configure(SerializationFeature.INDENT_OUTPUT, true)

    fun <R> createRequest(method: String, params: Map<String, Any>) =
        RequestBuilder<R>(transport, mapper, method, params)

    class RequestBuilder<T>(
        private val transport: Transport,
        private val mapper: ObjectMapper,
        private val method: String,
        private val params: Map<String, Any>
    ) {

        fun execute(): T? {
            val request = Request(method, params)
            val response = transport.pass(mapper.writeValueAsString(request))
            return try {
                mapper.readValue<SuccessResponse<T>>(response).result
            } catch (e: Exception) {
                return null
            }
        }
    }
}

