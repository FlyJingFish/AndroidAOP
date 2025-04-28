package com.flyjingfish.android_aop_plugin.beans

data class CutReplaceMethodJson(
    val replaceMethod: String,
    val targetClassName: String,
    val targetMethod: String,
    val locations:MutableList<CutReplaceLocationMap> = mutableListOf()
)