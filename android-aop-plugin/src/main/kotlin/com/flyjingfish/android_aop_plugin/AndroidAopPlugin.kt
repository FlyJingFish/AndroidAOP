package com.flyjingfish.android_aop_plugin

import com.android.build.gradle.AppPlugin
import com.flyjingfish.android_aop_plugin.config.AndroidAopConfig
import com.flyjingfish.android_aop_plugin.plugin.CompilePlugin
import com.flyjingfish.android_aop_plugin.plugin.TransformPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project

class AndroidAopPlugin : Plugin<Project> {
    companion object{
        private val androidPluginIds = listOf(
            "com.android.library",
            "com.android.dynamic-feature",
            "com.android.test",
            "com.android.asset-pack",
            "com.android.instantapp",
            "com.android.feature"
        )
        private val jvmPluginIds = listOf(
            "java-library",
            "org.jetbrains.kotlin.jvm"
        )
    }
    override fun apply(project: Project) {
        project.extensions.add("androidAopConfig", AndroidAopConfig::class.java)
        if (project.rootProject == project){
            deepSetDebugMode(project.rootProject)
        }
        CleanWithCachePlugin().apply(project)
        CompilePlugin(false).apply(project)
        TransformPlugin.apply(project)
    }

    private fun deepSetDebugMode(project: Project){
        project.gradle.beforeProject { value ->
            if (value == project) return@beforeProject
            // ① Android 相关模块
            for (pluginId in androidPluginIds) {
                value.plugins.withId(pluginId) {
                    println("Apply CompilePlugin to '${value.name}' ($pluginId)")
                    CompilePlugin(true).apply(value)
                }
            }

            // ② Java/Kotlin 模块
            for (pluginId in jvmPluginIds) {
                value.plugins.withId(pluginId) {
                    println("Apply CompilePlugin to '${value.name}' ($pluginId - JVM)")
                    CompilePlugin(true).apply(value)
                }
            }
            value.afterEvaluate {
                val notApp = !it.plugins.hasPlugin(AppPlugin::class.java)
                val noneHasAop = !it.plugins.hasPlugin("android.aop")
                if (!notApp && noneHasAop){
                    throw RuntimeException("Missing plugin configuration [id 'android.aop'] in module ${it.name}")
                }
            }
        }
    }
}