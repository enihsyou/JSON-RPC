package com.enihsyou.rpc.client

import kotlin.reflect.KCallable
import kotlin.reflect.KType

data class ClassMetadata(
    val  methods:Map<KCallable<*>, MethodMetadata>
)

data class MethodMetadata(
    /**RPC函数名*/
    val name: String,
    /**RPC函数的参数列表*/
    val parameters: Map<String, ParameterMetadata>
)

data class ParameterMetadata(
    /**RPC参数名*/
    val name: String,
    /**参数类型*/
    val type: KType
)
