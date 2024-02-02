package com.flyjingfish.android_aop_plugin.beans

data class CutReplaceClassMap(
    val type: String,
    val replaceClassName: String,
    val targetClassName: String,
    val method:MutableMap<String,CutReplaceMethodJson> = mutableMapOf()
)