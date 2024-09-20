package com.flyjingfish.android_aop_plugin.beans

data class MethodRecord(
    val methodName: String,
    val descriptor: String,
    val cutClassName:MutableSet<String> = mutableSetOf(),
    val lambda: Boolean = false,
    val cutInfo: MutableMap<String, CutInfo> = mutableMapOf(),
    val overrideMethod: Boolean = false,
    val overrideClassname: String = "",
) {
    fun getKey(): String {
        return methodName + descriptor + cutClassName + lambda
    }
}