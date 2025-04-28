package com.flyjingfish.android_aop_plugin.beans

data class CutReplaceMethodJson(
    val replaceMethod: String,
    val targetClassName: String,
    val targetMethod: String,
    var locationCount:Int = 0,
    val locations:MutableList<CutReplaceLocationMap> = mutableListOf()
)