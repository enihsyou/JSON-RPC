package com.enihsyou.rpc.server

import com.enihsyou.rpc.core.JsonRpcMethod
import com.enihsyou.rpc.core.JsonRpcService
import org.slf4j.LoggerFactory
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KVisibility
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.jvm.isAccessible

object Reflections {
    private val logger = LoggerFactory.getLogger(this::class.java)
    /**从给定的[annotations]中寻找符合[clazz]的注解
     *
     * @param annotations 寻找的注解集合
     * @param clazz 需要寻找的目标
     * @param T 编译时的注解类型*/
    @Suppress("UNCHECKED_CAST")
    fun <T : Annotation> getAnnotation(annotations: Iterable<Annotation>, clazz: KClass<T>): T? {
        return annotations.firstOrNull { it.annotationClass == clazz } as T?
    }

    /**为以后的RPC处理获取[ClassMetadata]
     * 搜索类中的每个标记为`public`的函数，构建关于函数和它的参数的元信息
     *
     * @param clazz 需要构建的类
     * @return 满足`JSON-RPC`标准的类的元信息*/
    fun getClassMetadata(clazz: KClass<*>): ClassMetadata {
        val methodMetadata = mutableMapOf<String, MethodMetadata>()
        logger.debug("Get ClassMetadata for {}", clazz.qualifiedName)
        /*包含超类在内的non-extension non-static函数*/
        clazz.memberFunctions.forEach {
            /*检查函数是否为public*/
            if (it.visibility != KVisibility.PUBLIC)
                logger.trace("{}#{}不是PUBLIC，跳过", clazz.simpleName, it.name)
                    .run { return@forEach }

            /*检查函数是否带有[JsonRpcMethod]注解*/
            val rpcFunction = getAnnotation(it.annotations, JsonRpcMethod::class)
                ?: logger.trace("{}#{} didn't use JsonRpcMethod annotation, skip", clazz.simpleName, it.name)
                    .run { return@forEach }

            /*获取函数名和参数列表*/
            val rpcFunctionName = if (rpcFunction.value.isNotBlank()) rpcFunction.value else it.name
            val rpcFunctionParams = getFunctionParameters(it)

            /*保存到可调用列表中*/
            it.isAccessible = true
            methodMetadata[rpcFunctionName] = MethodMetadata(
                rpcFunctionName,
                it,
                rpcFunctionParams
            )
                .also { logger.debug("Add {} to {} RPC list, params: {}", it.name, clazz.simpleName, it.parameters) }
        }

        val isService = getAnnotation(clazz.annotations, JsonRpcService::class) != null

        return ClassMetadata(isService, methodMetadata)
    }

    /**获取关于函数[function]的参数的元信息*/
    private fun getFunctionParameters(function: KFunction<*>): Map<String, ParameterMetadata> {
        val parametersMetadata = mutableMapOf<String, ParameterMetadata>()

        function.parameters.forEach { it ->
            val rpcParameterName = it.name ?: it.toString()
            val rpcParameterType = it.type
            parametersMetadata[rpcParameterName] =
                ParameterMetadata(rpcParameterName, rpcParameterType, it.index)
        }
        return parametersMetadata
    }
}
