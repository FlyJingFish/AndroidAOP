package com.flyjingfish.android_aop_plugin.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project

abstract class BasePlugin :Plugin<Project> {
    private lateinit var pluginConfig: PluginConfig
    private fun init(project: Project){
        pluginConfig = PluginConfig(project)
    }

    fun isIncremental():Boolean{
        return pluginConfig.isIncremental()
    }

    override fun apply(project: Project) {
        init(project)
    }

    fun isDebugMode(buildTypeName :String?,variantName :String):Boolean{
        return pluginConfig.isDebugMode(buildTypeName, variantName)
    }

    fun isFastDex(buildTypeName :String?,variantName :String):Boolean{
        return pluginConfig.isFastDex(buildTypeName, variantName)
    }

    fun isReflectInvokeMethod(buildTypeName :String?,variantName :String):Boolean{
        return pluginConfig.isReflectInvokeMethod(buildTypeName, variantName)
    }

    fun isReflectInvokeMethodStatic():Boolean{
        return pluginConfig.isReflectInvokeMethodStatic()
    }

    fun isDebugMode():Boolean{
        return pluginConfig.isDebugMode()
    }
    fun isReflectInvokeMethod():Boolean{
        return pluginConfig.isReflectInvokeMethod()
    }
    fun hasBuildConfig():Boolean{
        return pluginConfig.hasBuildConfig()
    }
}