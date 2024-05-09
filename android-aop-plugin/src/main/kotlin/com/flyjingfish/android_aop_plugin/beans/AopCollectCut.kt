package com.flyjingfish.android_aop_plugin.beans

data class AopCollectCut(
    val collectClassName: String,
    val invokeClassName: String,
    val invokeMethod: String,
)
