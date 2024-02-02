package com.flyjingfish.android_aop_plugin.beans

data class CutReplaceClassJson(
    val type: String,
    val replaceClassName: String,
    val targetClassName: String,
    val method: MutableList<CutReplaceMethodJson>
)