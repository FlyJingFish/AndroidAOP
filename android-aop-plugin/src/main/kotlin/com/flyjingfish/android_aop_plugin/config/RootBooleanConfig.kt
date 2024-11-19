package com.flyjingfish.android_aop_plugin.config

enum class RootBooleanConfig(
    val propertyName: String,
    val defaultValue: Boolean,
) {
    DEBUG_MODE("androidAop.debugMode", false),
    ONLY_DEBUG("androidAop.debugMode.variantOnlyDebug", true),
    INCREMENTAL("androidAop.debugMode.isIncremental", true),
    REFLECT_INVOKE_METHOD("androidAop.reflectInvokeMethod", false),
    REFLECT_INVOKE_METHOD_ONLY_DEBUG("androidAop.reflectInvokeMethod.variantOnlyDebug", false),
    BUILD_CONFIG("androidAop.debugMode.buildConfig", true),
    REFLECT_INVOKE_METHOD_STATIC("androidAop.reflectInvokeMethod.static", false);
}