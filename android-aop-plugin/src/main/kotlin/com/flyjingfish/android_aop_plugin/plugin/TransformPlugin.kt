package com.flyjingfish.android_aop_plugin.plugin

import com.android.build.api.artifact.ScopedArtifact
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.ScopedArtifacts
import com.android.build.gradle.AppPlugin
import com.flyjingfish.android_aop_plugin.AssembleAndroidAopTask
import com.flyjingfish.android_aop_plugin.config.AndroidAopConfig
import com.flyjingfish.android_aop_plugin.config.RootBooleanConfig
import com.flyjingfish.android_aop_plugin.utils.ClassFileUtils
import com.flyjingfish.android_aop_plugin.utils.InitConfig
import org.gradle.api.Plugin
import org.gradle.api.Project

object TransformPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val isApp = project.plugins.hasPlugin(AppPlugin::class.java)
        if (!isApp) {
            return
        }
        val reflectInvokeMethodStr = project.properties[RootBooleanConfig.REFLECT_INVOKE_METHOD.propertyName]?:"false"
        val debugModeStr = project.properties[RootBooleanConfig.DEBUG_MODE.propertyName]?:"false"
        val debugMode = debugModeStr == "true"
        val reflectInvokeMethod = reflectInvokeMethodStr == "true"
        val androidComponents = project.extensions.getByType(AndroidComponentsExtension::class.java)
        androidComponents.onVariants { variant ->
            val androidAopConfig = project.extensions.getByType(AndroidAopConfig::class.java)
            androidAopConfig.initConfig()
            if (androidAopConfig.cutInfoJson){
                InitConfig.initCutInfo(project)
            }
            if (androidAopConfig.enabled){
                ClassFileUtils.reflectInvokeMethod = reflectInvokeMethod
                val task = project.tasks.register("${variant.name}AssembleAndroidAopTask", AssembleAndroidAopTask::class.java){
                    it.variant = variant.name
                }
                variant.artifacts
                    .forScope(ScopedArtifacts.Scope.ALL)
                    .use(task)
                    .toTransform(
                        ScopedArtifact.CLASSES,
                        AssembleAndroidAopTask::allJars,
                        AssembleAndroidAopTask::allDirectories,
                        AssembleAndroidAopTask::output
                    )
            }
        }
    }
}