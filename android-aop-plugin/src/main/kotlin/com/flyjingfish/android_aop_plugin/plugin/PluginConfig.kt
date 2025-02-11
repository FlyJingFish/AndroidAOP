package com.flyjingfish.android_aop_plugin.plugin

import com.flyjingfish.android_aop_plugin.config.RootBooleanConfig
import org.gradle.api.Plugin
import org.gradle.api.Project

class PluginConfig(project: Project) {
    private var reflectInvokeMethod = false
    private var reflectInvokeMethodOnlyDebug = false
    private var debugMode = false
    private var onlyDebug = false
    private var isIncremental = true
    private var buildConfig = true
    private var reflectInvokeMethodStatic = true

    init{
        val reflectInvokeMethodStr = project.properties[RootBooleanConfig.REFLECT_INVOKE_METHOD.propertyName]?:"${RootBooleanConfig.REFLECT_INVOKE_METHOD.defaultValue}"
        val debugModeStr = project.properties[RootBooleanConfig.DEBUG_MODE.propertyName]?:"${RootBooleanConfig.DEBUG_MODE.defaultValue}"
        val onlyModeStr = project.properties[RootBooleanConfig.ONLY_DEBUG.propertyName]?:"${RootBooleanConfig.ONLY_DEBUG.defaultValue}"
        val isIncrementalStr = project.properties[RootBooleanConfig.INCREMENTAL.propertyName]?:"${RootBooleanConfig.INCREMENTAL.defaultValue}"
        val reflectInvokeMethodDebugStr = project.properties[RootBooleanConfig.REFLECT_INVOKE_METHOD_ONLY_DEBUG.propertyName]?:"${RootBooleanConfig.REFLECT_INVOKE_METHOD_ONLY_DEBUG.defaultValue}"
        val buildConfigStr = project.properties[RootBooleanConfig.BUILD_CONFIG.propertyName]?:"${RootBooleanConfig.BUILD_CONFIG.defaultValue}"
        val reflectInvokeMethodStaticStr = project.properties[RootBooleanConfig.REFLECT_INVOKE_METHOD_STATIC.propertyName]?:"${RootBooleanConfig.REFLECT_INVOKE_METHOD_STATIC.defaultValue}"
        debugMode = debugModeStr.toString() == "true"
        reflectInvokeMethod = reflectInvokeMethodStr.toString() == "true"
        onlyDebug = onlyModeStr.toString() == "true"
        isIncremental = isIncrementalStr.toString() == "true"
        reflectInvokeMethodOnlyDebug = reflectInvokeMethodDebugStr.toString() == "true"
        buildConfig = buildConfigStr.toString() == "true"
        reflectInvokeMethodStatic = reflectInvokeMethodStaticStr.toString() == "true"
    }

    fun isIncremental():Boolean{
        return isIncremental
    }

    fun isDebugMode(buildTypeName :String?,variantName :String):Boolean{
        return if (debugMode){
            if (onlyDebug){
                if (buildTypeName != null){
                    buildTypeName.lowercase() == "debug"
                }else{
                    variantName.lowercase().contains("debug")
                }
            }else{
                true
            }
        }else{
            false
        }
    }

    fun isReflectInvokeMethod(buildTypeName :String?,variantName :String):Boolean{
        return if (reflectInvokeMethod){
            if (reflectInvokeMethodOnlyDebug){
                if (buildTypeName != null){
                    buildTypeName.lowercase() == "debug"
                }else{
                    variantName.lowercase().contains("debug")
                }
            }else{
                true
            }
        }else{
            false
        }
    }

    fun isDebugMode():Boolean{
        return debugMode
    }
    fun isReflectInvokeMethod():Boolean{
        return reflectInvokeMethod
    }
    fun hasBuildConfig():Boolean{
        return buildConfig
    }

    fun isReflectInvokeMethodStatic():Boolean{
        return reflectInvokeMethodStatic
    }
}