package com.enihsyou.rpc.core

import com.enihsyou.rpc.core.message.ErrorResponse.ErrorNode

object RpcExceptions {
    val PARSE_ERROR = ErrorNode(-32700, "Parse error")
    val METHOD_NOT_FOUND = ErrorNode(-32601, "Method not found")
    val INVALID_REQUEST = ErrorNode(-32600, "Invalid Request")
    val INVALID_PARAMS = ErrorNode(-32602, "Invalid params")
    val INTERNAL_ERROR = ErrorNode(-32603, "Internal error")
}
