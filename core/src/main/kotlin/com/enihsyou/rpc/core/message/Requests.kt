package com.enihsyou.rpc.core.message

open class Requests(

    /**A String specifying the version of the JSON-RPC protocol. MUST be exactly "2.0".*/
    val jsonrpc: VersionNode,

    /**A String containing the name of the method to be invoked. Method names that begin with the word rpc followed by a period character (U+002E or ASCII 46) are reserved for rpc-internal methods and extensions and MUST NOT be used for anything else.*/
    val method: MethodNode,

    /**A Structured value that holds the parameter values to be used during the invocation of the method. This member MAY be omitted.*/
    val params: ParameterNode,

    /**An identifier established by the Client that MUST contain a String, Number, or NULL value if included. If it is not included it is assumed to be a notification. The value SHOULD normally not be Null [1] and Numbers SHOULD NOT contain fractional parts*/
    val id: IdNode
)

