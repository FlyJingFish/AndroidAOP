package com.flyjingfish.android_aop_plugin

import com.android.build.gradle.AppPlugin
import com.flyjingfish.android_aop_plugin.config.AndroidAopConfig
import com.flyjingfish.android_aop_plugin.plugin.CompilePlugin
import com.flyjingfish.android_aop_plugin.plugin.TransformPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project

class AndroidAopPlugin : Plugin<Project> {
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
        val childProjects = project.childProjects
        if (childProjects.isEmpty()){
            return
        }
        childProjects.forEach { (_,value)->
            value.afterEvaluate {
                val notApp = !it.plugins.hasPlugin(AppPlugin::class.java)
                val noneHasAop = !it.plugins.hasPlugin("android.aop")
                if (notApp && noneHasAop && it.hasProperty("android")){
                    CompilePlugin(true).apply(it)
                }
            }
            deepSetDebugMode(value)
        }
    }
}