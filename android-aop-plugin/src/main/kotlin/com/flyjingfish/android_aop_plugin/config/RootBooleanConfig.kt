package com.flyjingfish.android_aop_plugin.config

enum class RootBooleanConfig(
    val propertyName: String,
    val defaultValue: Boolean,
) {
    REFLECT_INVOKE_METHOD("androidAop.reflectInvokeMethod", false),
    ONLY_DEBUG("androidAop.debugMode.variantOnlyDebug", true),
    DEBUG_MODE("androidAop.debugMode", false);
}