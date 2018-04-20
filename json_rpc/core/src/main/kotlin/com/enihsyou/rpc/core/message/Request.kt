package com.enihsyou.rpc.core.message

import com.fasterxml.jackson.annotation.JsonProperty

open class Request(

    @JsonProperty("method")
    val method: String,

    /**限定为参数名和参数值的形式*/
    @JsonProperty("params")
    val params: Map<String, Any>
)
