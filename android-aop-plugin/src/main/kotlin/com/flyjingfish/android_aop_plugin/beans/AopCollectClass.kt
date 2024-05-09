package com.flyjingfish.android_aop_plugin.beans

data class AopCollectClass(
    val collectClassName: String,
    val invokeClassName: String,
    val invokeMethod: String,
    val collectExtendsClassName: String
)
