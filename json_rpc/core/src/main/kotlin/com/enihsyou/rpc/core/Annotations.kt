package com.enihsyou.rpc.core

/**marks a class as a JSON-RPC service*/
@Retention(AnnotationRetention.RUNTIME)
annotation class JsonRpcService

/**marks a method as eligible for calling from the network*/
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
@MustBeDocumented
annotation class JsonRpcMethod(val value: String = "")

///**a mandatory annotation for a method parameter and should contain the parameter name*/
//@Retention(AnnotationRetention.RUNTIME)
//@Target(AnnotationTarget.VALUE_PARAMETER)
//annotation class JsonRpcParam(val value: String, val optional: Boolean = false)

/**used for marking an exception as a JSON-RPC error*/
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class JsonRpcError(val code: Int = 0, val message: String = "")
