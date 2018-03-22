package com.enihsyou.rpc.core.message

open class Response(
    /**A String specifying the version of the JSON-RPC protocol. MUST be exactly "2.0".*/
    val jsonrpc: VersionNode,

    /**This member is REQUIRED.
    It MUST be the same as the value of the id member in the Request Object.
    If there was an error in detecting the id in the Request object (e.g. Parse error/Invalid Request), it MUST be Null.*/
    val id: IdNode
)

class SuccessResponse(
    jsonrpc: VersionNode,
    id: IdNode,
    /**This member is REQUIRED on success.
    This member MUST NOT exist if there was an error invoking the method.
    The value of this member is determined by the method invoked on the Server.*/
    val result: ResultNode

) : Response(jsonrpc, id)

class ErrorResponse(
    jsonrpc: VersionNode,
    id: IdNode,
    /**This member is REQUIRED on error.
    This member MUST NOT exist if there was no error triggered during invocation.
    The value for this member MUST be an Object as defined in section 5.1.*/
    val error: ErrorNode
) : Response(jsonrpc, id)
