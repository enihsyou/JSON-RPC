package com.enihsyou.rpc.server

import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KType

/**关于Kotlin class的元信息*/
class ClassMetadata(
    /**这个类是否是一个满足`JSON-RPC 2.0`标准的服务提供方*/
    val service: Boolean,
    /**满足`JSON-RPC 2.0`标准的函数，名字和函数的[Map]集合*/
    val methods: Map<String, FunctionMetadata>
)

/**关于Kotlin function的元信息*/
class FunctionMetadata(
    /**RPC函数名*/
    val name: String,
    /**RPC函数的实体*/
    val method: KFunction<*>,
    /**RPC函数的参数列表*/
    val parameters: Map<String, ParameterMetadata>
)

class ParameterMetadata(
    /**RPC参数名*/
    val name: String,
    /**参数类型*/
    val type: KClass<*>,
    /**参数泛型类型*/
    val genericType: KType,
    /**为函数的第几个参数*/
    val index: Int,
    /**是否为可选参数*/
    val optional: Boolean
)
