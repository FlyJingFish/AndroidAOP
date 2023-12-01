package com.flyjingfish.android_aop_plugin.beans

data class LambdaMethod(
    val samMethodName: String,
    val thisClassName: String,
    val lambdaName: String,
    val lambdaDesc: String
)