package com.flyjingfish.android_aop_plugin

import com.flyjingfish.android_aop_plugin.config.AndroidAopConfig
import com.flyjingfish.android_aop_plugin.plugin.CompilePlugin
import com.flyjingfish.android_aop_plugin.plugin.TransformPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project

class AndroidAopPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.extensions.add("androidAopConfig", AndroidAopConfig::class.java)

        CompilePlugin.apply(project)
        TransformPlugin.apply(project)
    }
}