package com.enihsyou.rpc.server

import com.enihsyou.rpc.core.JsonRpcError
import com.enihsyou.rpc.core.JsonRpcMethod
import com.enihsyou.rpc.core.JsonRpcService

@JsonRpcService
class SimpleAddService {

    @JsonRpcMethod
    fun add(a: Int, b: Int): Int {
        return a + b
    }

    @JsonRpcMethod
    fun subtract(a: Int, b: Int?=1): Int {
        return a - b as Int
    }

    @JsonRpcMethod
    private fun privateFunction(a: Int, b: Int?=1): Int {
        return a - b as Int
    }

    @JsonRpcMethod
    fun functionThatThrow(a: Int, b: Int): Int {
        throw TestThrow()
    }

    fun notRpcFunction(a: Int, b: Int): Int {
        return a * b
    }

    @JsonRpcError(1, "testError")
    class TestThrow : RuntimeException()
}
