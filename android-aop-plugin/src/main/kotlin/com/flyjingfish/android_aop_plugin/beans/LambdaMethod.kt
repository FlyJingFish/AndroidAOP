package com.flyjingfish.android_aop_plugin.beans

data class LambdaMethod(
    val samMethodName: String,
    val samMethodDesc: String,
    val thisClassName: String,
    val originalClassName: String,
    val lambdaName: String,
    val lambdaDesc: String
)