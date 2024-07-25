package com.flyjingfish.android_aop_plugin.beans

data class MethodRecord(
    val methodName: String,
    val descriptor: String,
    val cutClassName: String?,
    val lambda: Boolean = false,
    val cutInfo: MutableMap<String, CutInfo> = mutableMapOf(),
    var multipleSuspendClass: Boolean = false,
    var paramNames: List<String>? = null
) {
    fun getKey(): String {
        return methodName + descriptor + cutClassName + lambda
    }
}