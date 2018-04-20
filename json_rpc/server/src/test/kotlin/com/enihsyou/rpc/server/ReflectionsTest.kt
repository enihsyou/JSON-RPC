package com.enihsyou.rpc.server

import com.enihsyou.rpc.core.JsonRpcMethod
import com.enihsyou.rpc.core.JsonRpcService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import kotlin.reflect.full.starProjectedType

internal class ReflectionsTest {

    @Test
    fun getAnnotation() {
        val rpcClass = SimpleAddService::class
        assertThat(Reflections.getAnnotation(rpcClass.annotations, JsonRpcService::class))
            .`as`("测试RPC类{}", rpcClass.simpleName)
            .isNotNull()
            .isInstanceOf(JsonRpcService::class.java)

        val rpcAddFunction = SimpleAddService::add
        val noneRpcFunction = SimpleAddService::notRpcFunction
        val rpcThrowFunction = SimpleAddService::functionThatThrow
        assertThat(Reflections.getAnnotation(rpcAddFunction.annotations, JsonRpcMethod::class))
            .`as`("测试有效RPC函数{}#{}", rpcClass.simpleName, rpcAddFunction.name)
            .isNotNull()
            .isInstanceOf(JsonRpcMethod::class.java)
        assertThat(Reflections.getAnnotation(noneRpcFunction.annotations, JsonRpcMethod::class))
            .`as`("测试无效RPC函数{}#{}", rpcClass.simpleName, noneRpcFunction.name)
            .isNull()
        assertThat(Reflections.getAnnotation(rpcThrowFunction.annotations, JsonRpcMethod::class))
            .`as`("测试含有异常的RPC函数{}#{}", rpcClass.simpleName, rpcThrowFunction.name)
            .isNotNull()
            .isInstanceOf(JsonRpcMethod::class.java)
    }

    @Test
    fun getClassMetadata() {
        val rpcClass = SimpleAddService::class
        val metadata = Reflections.getClassMetadata(rpcClass)
        assertThat(metadata)
            .`as`("get class metadata")
            .hasFieldOrPropertyWithValue("service", true)
            .hasFieldOrProperty("methods")
        assertThat(metadata.methods)
            .hasSize(3)
            .containsKeys("add", "subtract", "functionThatThrow")
            .doesNotContainKey("notRpcFunction")
        assertThat(metadata.methods["add"]!!.parameters)
            .hasSize(3)
            .satisfies {
                assertThat(it["a"]).isNotNull()
                    .hasFieldOrPropertyWithValue("name", "a")
                    .hasFieldOrPropertyWithValue("type", Int::class.starProjectedType)
                    .hasFieldOrPropertyWithValue("index", 1)
                assertThat(it["b"]).isNotNull()
                    .hasFieldOrPropertyWithValue("name", "b")
                    .hasFieldOrPropertyWithValue("type", Int::class.starProjectedType)
                    .hasFieldOrPropertyWithValue("index", 2)
            }
    }
}
