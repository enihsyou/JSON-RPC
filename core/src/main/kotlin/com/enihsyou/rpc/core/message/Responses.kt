package com.enihsyou.rpc.core.message

open class Response

data class SuccessResponse(
    /**This member is REQUIRED on success.
    This member MUST NOT exist if there was an error invoking the method.
    The value of this member is determined by the method invoked on the Server.*/
    val result: Any

) : Response()

class ErrorResponse(
    /**This member is REQUIRED on error.
    This member MUST NOT exist if there was no error triggered during invocation.
    The value for this member MUST be an Object as defined in section 5.1.*/
    val error: ErrorNode
) : Response() {

    class ErrorNode(
        /**A Number that indicates the error type that occurred.
        This MUST be an integer.*/
        val code: Int,
        /**A String providing a short description of the error.
        The message SHOULD be limited to a concise single sentence.*/
        val message: String
    )
}


