package com.flyjingfish.android_aop_plugin.beans

//data class CutMethodJson(val name: String, val paramTypes: String, val returnType: String?,val lambda: Boolean)
data class CutMethodJson(val name: String, val descriptor: String, val lambda: Boolean)