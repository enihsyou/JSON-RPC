package com.enihsyou.rpc.core

@Retention(AnnotationRetention.RUNTIME)
annotation class JsonRpcService

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
@MustBeDocumented
annotation class JsonRpcMethod(val value: String = "")

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class JsonRpcParam(val value: String)

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class JsonRpcError(val code: Int = 0, val message: String = "")

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class JsonRpcOptional
