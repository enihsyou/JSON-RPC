package com.enihsyou.rpc.server

import kotlin.reflect.KClass
import kotlin.reflect.KFunction

object Reflections {

    /**从给定的[annotations]中寻找符合[clazz]的注解
     *
     * @param annotations 寻找的注解集合
     * @param clazz 需要寻找的目标
     * @param T 编译时的注解类型*/
    fun <T : Annotation> getAnnotation(annotations: Iterable<Annotation>, clazz: KClass<T>): T? {
        TODO()
    }

    /**为以后的RPC处理获取[ClassMetadata]
     * 搜索类中的每个标记为`public`的函数，构建关于函数和它的参数的元信息
     *
     * @param clazz 需要构建的类
     * @return 满足`JSON-RPC`标准的类的元信息*/
    fun getClassMetadata(clazz: KClass<*>):ClassMetadata{
        TODO()
    }

    /**获取关于函数[function]的参数的元信息*/
    private fun getMethodParameters(function: KFunction<*>):Map<String, ParameterMetadata>{
        TODO()
    }
}
