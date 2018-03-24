package com.enihsyou.rpc.server

import com.enihsyou.rpc.core.JsonRpcError
import com.enihsyou.rpc.core.RpcExceptions
import com.enihsyou.rpc.core.message.ErrorResponse
import com.enihsyou.rpc.core.message.Request
import com.enihsyou.rpc.core.message.Response
import com.enihsyou.rpc.core.message.SuccessResponse
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.util.ClassUtil
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.cast

/**
 * 处理*JSON-RPC 2.0*请求的服务器端
 *
 * 1. 将传送来的JSON-RPC请求转换成可读的JSON类型
 * 2. 检查请求是否满足*JSON-RPC 2.0*标准
 * 3. 搜索是否有满足要求的服务可提供
 * 4. 找到对应的服务提供方（一个函数）
 * 5. 为调用函数准备必要的参数信息
 * 6. 调用对应的函数
 * 7. 将调用结果包装成返回响应对象
 * 8. 如果发生错误，返回对应的错误响应对象
 * */
class JsonRpcServer(service: Any) {

    /**进行JSON和Kotlin之间的转换*/
    private val mapper = jacksonObjectMapper()
        .configure(SerializationFeature.INDENT_OUTPUT, true)

    /**储存类到类元信息之间的关系*/
    private val classesMetadata = mutableMapOf<KClass<*>, ClassMetadata>()

    init {
        classesMetadata[service::class] = Reflections.getClassMetadata(service::class)
    }

    /**处理一个传送来的JSON-RPC请求，支持独立请求和批量请求
     * 将处理工作分发到服务商，并返回响应结果
     *
     * @param textRequest 请求体的纯文本形式
     * @param service 实际处理这个请求的对象
     * @return 返回给客户端的消息，文本形式*/
    fun handle(textRequest: String, service: Any): String {
        val request = mapper.readValue<Request>(textRequest)
        val response = handleRequest(request, service)

        return toJson(response)
    }

    /**处理传递来的请求对象 //todo 可扩展为处理批量请求
     *
     * @param request 转换成了Kotlin格式的请求
     * @param service 处理请求的服务商
     * @return JSON-RPC格式的响应对象*/
    private fun handleRequest(request: Request, service: Any): Response {
        return try {
            handleSingle(request, service)
        } catch (e: Exception) {
            println(e::class.qualifiedName)
            handleError(request, e)
        }
    }

    /**处理[RuntimeException]
     * 如果[extension]的根异常被[JsonRpcError]标记了，会被转成响应的错误提示
     * 否则 "内部错误" 会作为信息返回
     *
     * @param request JSON-RPC请求的Kotlin对象
     * @param exception 执行中抛出的异常
     * @return JSON-RPC格式的错误响应*/
    private fun handleError(request: Request, exception: Exception): ErrorResponse {
        val rootCause = ClassUtil.getRootCause(exception)
        val annotation = Reflections.getAnnotation(rootCause::class.annotations, JsonRpcError::class)
            ?: return ErrorResponse(RpcExceptions.INTERNAL_ERROR)
        val code = annotation.code
        val message = annotation.message.takeIf { it.isNotBlank() } ?: rootCause.message ?: ""

        return ErrorResponse(ErrorResponse.ErrorNode(code, message))
    }

    /**处理单个JSON-RPC请求
     *
     * @param request JSON-RPC请求的Kotlin对象
     * @param service 处理请求的服务商
     * @return JSON-RPC格式的响应*/
    private fun handleSingle(request: Request, service: Any): Response {
        val classMetadata = classesMetadata[service::class]
            ?: return ErrorResponse(RpcExceptions.INVALID_REQUEST)

        if (!classMetadata.service)
            return ErrorResponse(RpcExceptions.METHOD_NOT_FOUND)

        val method = classMetadata.methods[request.method]
            ?: return ErrorResponse(RpcExceptions.METHOD_NOT_FOUND)

        val methodParams: Array<Any> = convertToMethodParams(request.params, method, service)

        val result = method.method.call(*methodParams)
            ?: return ErrorResponse(ErrorResponse.ErrorNode(0, "error"))
        return SuccessResponse(result)
    }

    /**将JSON参数按一定的顺序转换为Kotlin参数
     * @param params JSON参数
     * @param method 对应函数的元信息
     * @return 每个位置上的参数实体，已被转换为对应的Kotlin类型*/
    private fun convertToMethodParams(params: Map<String, Any>, method: MethodMetadata, instance: Any): Array<Any> {
        val methodParams = mutableListOf<Pair<Any, Int>>()

        method.parameters.values.forEach {
            val type = it.type
            val index = it.index
            val name = it.name
            if (it.kind == KParameter.Kind.INSTANCE) {
                methodParams += instance to index
                return@forEach
            }
            val param = (type.classifier as? KClass<*>)?.cast(params[name])!!

            try {
                methodParams.add(param to index)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return methodParams.sortedBy { it.second }.map { it.first }.toTypedArray()
    }

    private fun toJson(value: Any): String = mapper.writeValueAsString(value)
}
