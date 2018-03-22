package com.enihsyou.rpc.core.message

abstract class Node

class VersionNode : Node() {

}

class MethodNode : Node() {

}

class ParameterNode : Node() {
}

class IdNode : Node() {

}

class ResultNode : Node() {

}

class ErrorNode(
    /**A Number that indicates the error type that occurred.
    This MUST be an integer.*/
    val code: Int,
    /**A String providing a short description of the error.
    The message SHOULD be limited to a concise single sentence.*/
    val message: String,
    /**A Primitive or Structured value that contains additional information about the error.
    This may be omitted.
    The value of this member is defined by the Server (e.g. detailed error information, nested errors etc.).*/
    val data: String
) : Node() {

}
