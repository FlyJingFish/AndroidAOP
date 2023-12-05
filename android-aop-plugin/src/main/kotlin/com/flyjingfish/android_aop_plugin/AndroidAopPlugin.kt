package com.flyjingfish.android_aop_plugin

import com.android.build.api.artifact.ScopedArtifact
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.ScopedArtifacts
import com.android.build.gradle.AppPlugin
import com.flyjingfish.android_aop_plugin.config.AndroidAopConfig
import com.flyjingfish.android_aop_plugin.utils.Utils
import org.gradle.api.Plugin
import org.gradle.api.Project

class AndroidAopPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val isApp = project.plugins.hasPlugin(AppPlugin::class.java)
        val logger = project.logger;
        if (!isApp) {
            logger.error("Plugin ['android.aop'] can only be used under the application, not under the module library invalid!")
            return
        }
        project.extensions.add("androidAopConfig", AndroidAopConfig::class.java)
        val androidComponents = project.extensions.getByType(AndroidComponentsExtension::class.java)
        androidComponents.onVariants { variant ->
            val androidAopConfig = project.extensions.getByType(AndroidAopConfig::class.java)
            AndroidAopConfig.debug = androidAopConfig.debug
            androidAopConfig.includes.forEach {
                AndroidAopConfig.includes.add("$it.")
            }
            androidAopConfig.excludes.forEach {
                AndroidAopConfig.excludes.add("$it.")
            }
            AndroidAopConfig.excludes.add(Utils.annotationPackage)
            AndroidAopConfig.excludes.add(Utils.corePackage)
            if (androidAopConfig.enabled){
                val task = project.tasks.register("${variant.name}AssembleAndroidAopTask", AssembleAndroidAopTask::class.java)
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