package com.flyjingfish.android_aop_plugin

import com.flyjingfish.android_aop_plugin.config.AndroidAopConfig
import com.flyjingfish.android_aop_plugin.config.RootBooleanConfig
import com.flyjingfish.android_aop_plugin.core.CompilePlugin
import com.flyjingfish.android_aop_plugin.core.TransformPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project

class AndroidAopPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.extensions.add("androidAopConfig", AndroidAopConfig::class.java)

        val debugModeStr = project.properties[RootBooleanConfig.DEBUG_MODE.propertyName]?:"false"
        val debugMode = debugModeStr == "true"
        if (debugMode){
            CompilePlugin.apply(project)
        }else{
            TransformPlugin.apply(project)
        }
    }
}