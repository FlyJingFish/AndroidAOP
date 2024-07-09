package com.flyjingfish.android_aop_plugin.beans

data class ReplaceInnerClassInfo(
    val className:String,
    val methodName:String,
    val methodDescriptor:String,
    val targetMethodName:String
)