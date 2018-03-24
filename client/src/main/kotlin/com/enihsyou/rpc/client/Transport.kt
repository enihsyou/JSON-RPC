package com.enihsyou.rpc.client

interface Transport {
    fun pass(request: String): String
}
