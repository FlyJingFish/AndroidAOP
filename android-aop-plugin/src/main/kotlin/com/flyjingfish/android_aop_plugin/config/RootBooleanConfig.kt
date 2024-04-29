package com.flyjingfish.android_aop_plugin.config

import com.android.build.gradle.options.ApiStage
import com.android.build.gradle.options.Option
import com.android.build.gradle.options.Stage
import com.android.build.gradle.options.parseBoolean

enum class RootBooleanConfig(
    override val propertyName: String,
    override val defaultValue: Boolean,
    stage: Stage
) : Option<Boolean> {
    REFLECT_INVOKE_METHOD("androidAop.reflectInvokeMethod", false, ApiStage.Stable),
    ONLY_DEBUG("androidAop.debugMode.variantOnlyDebug", true, ApiStage.Stable),
    DEBUG_MODE("androidAop.debugMode", false, ApiStage.Stable);

    override val status = stage.status

    override fun parse(value: Any): Boolean {
        return parseBoolean(propertyName, value)
    }
}