package com.flyjingfish.android_aop_plugin.plugin

import com.flyjingfish.android_aop_plugin.config.RootBooleanConfig
import org.gradle.api.Plugin
import org.gradle.api.Project

abstract class BasePlugin :Plugin<Project> {
    var reflectInvokeMethod = false
    private var debugMode = false
    private var onlyDebug = false
    private var isIncremental = true
    private fun init(project: Project){
        val reflectInvokeMethodStr = project.properties[RootBooleanConfig.REFLECT_INVOKE_METHOD.propertyName]?:"${RootBooleanConfig.REFLECT_INVOKE_METHOD.defaultValue}"
        val debugModeStr = project.properties[RootBooleanConfig.DEBUG_MODE.propertyName]?:"${RootBooleanConfig.DEBUG_MODE.defaultValue}"
        val onlyModeStr = project.properties[RootBooleanConfig.ONLY_DEBUG.propertyName]?:"${RootBooleanConfig.ONLY_DEBUG.defaultValue}"
        val isIncrementalStr = project.properties[RootBooleanConfig.INCREMENTAL.propertyName]?:"${RootBooleanConfig.INCREMENTAL.defaultValue}"
        debugMode = debugModeStr.toString() == "true"
        reflectInvokeMethod = reflectInvokeMethodStr.toString() == "true"
        onlyDebug = onlyModeStr.toString() == "true"
        isIncremental = isIncrementalStr.toString() == "true"
    }

    fun isIncremental():Boolean{
        return isIncremental
    }

    override fun apply(project: Project) {
        init(project)
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
}