package com.flyjingfish.android_aop_plugin.beans

data class CutReplaceMethodJson(
    val replaceMethod: String,
    val replaceMethodDescriptor: String,
    val targetMethod: String,
    val targetMethodDescriptor: String,
    val used: String
)