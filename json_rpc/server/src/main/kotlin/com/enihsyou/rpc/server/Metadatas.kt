package com.enihsyou.rpc.server

import kotlin.reflect.KCallable
import kotlin.reflect.KParameter
import kotlin.reflect.KType

typealias MethodName = String
typealias ParameterName = String

/**关于Kotlin class的元信息*/
data class ClassMetadata(
    /**这个类是否是一个满足`JSON-RPC 2.0`标准的服务提供方*/
    val service: Boolean,
    /**满足`JSON-RPC 2.0`标准的函数，名字和函数的[Map]集合*/
    val methods: Map<MethodName, MethodMetadata>
)

/**关于类的成员函数的元信息
 * 为了处理简单，只允许添加类的成员函数，所以调用函数时所需要的第一个INSTANCE参数不需要提供*/
data class MethodMetadata(
    /**RPC函数名*/
    val name: String,
    /**RPC函数的实体*/
    val method: KCallable<*>,
    /**RPC函数的参数列表*/
    val parameters: Map<ParameterName, ParameterMetadata>
)

data class ParameterMetadata(
    /**RPC参数名*/
    val name: String,
    /**参数类型*/
    val type: KType,
    val kind: KParameter.Kind,
    /**为函数的第几个参数*/
    val index: Int
)
