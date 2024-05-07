package com.flyjingfish.android_aop_plugin

import com.flyjingfish.android_aop_plugin.config.AndroidAopConfig
import com.flyjingfish.android_aop_plugin.plugin.CompilePlugin
import com.flyjingfish.android_aop_plugin.plugin.TransformPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project

class AndroidAopPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.extensions.add("androidAopConfig", AndroidAopConfig::class.java)
        if (project.rootProject == project){
            project.rootProject.childProjects.forEach { (_,value)->
                value.afterEvaluate {
                    val hasAop = it.plugins.hasPlugin("android.aop")
                    if (!hasAop && it.hasProperty("android")){
                        CompilePlugin.apply(it)
                    }
                }
            }
        }
        CompilePlugin.apply(project)
        TransformPlugin.apply(project)
    }
}