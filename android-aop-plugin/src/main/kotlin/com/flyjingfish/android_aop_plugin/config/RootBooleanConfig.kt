package com.flyjingfish.android_aop_plugin.config

enum class RootBooleanConfig(
    val propertyName: String,
    val defaultValue: Boolean,
) {
    DEBUG_MODE("androidAop.debugMode", false),
    REFLECT_INVOKE_METHOD("androidAop.reflectInvokeMethod", false),
    ONLY_DEBUG("androidAop.debugMode.variantOnlyDebug", true),
    INCREMENTAL("androidAop.debugMode.isIncremental", true);
}