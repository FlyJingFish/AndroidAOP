package com.flyjingfish.android_aop_plugin.plugin

import com.flyjingfish.android_aop_plugin.config.RootBooleanConfig
import org.gradle.api.Plugin
import org.gradle.api.Project

abstract class BasePlugin :Plugin<Project> {
    var reflectInvokeMethod = false
    private var debugMode = false
    private var onlyDebug = false
    private fun init(project: Project){
        val reflectInvokeMethodStr = project.properties[RootBooleanConfig.REFLECT_INVOKE_METHOD.propertyName]?:"false"
        val debugModeStr = project.properties[RootBooleanConfig.DEBUG_MODE.propertyName]?:"false"
        val onlyModeStr = project.properties[RootBooleanConfig.ONLY_DEBUG.propertyName]?:"false"
        debugMode = RootBooleanConfig.DEBUG_MODE.parse(debugModeStr)
        reflectInvokeMethod = RootBooleanConfig.REFLECT_INVOKE_METHOD.parse(reflectInvokeMethodStr)
        onlyDebug =RootBooleanConfig.ONLY_DEBUG.parse(onlyModeStr)
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