package com.flyjingfish.android_aop_plugin.plugin

import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.internal.tasks.DexArchiveBuilderTask
import com.flyjingfish.android_aop_plugin.config.AndroidAopConfig
import com.flyjingfish.android_aop_plugin.tasks.AssembleAndroidAopTask
import com.flyjingfish.android_aop_plugin.utils.InitConfig
import com.flyjingfish.android_aop_plugin.utils.RuntimeProject
import io.github.flyjingfish.fast_transform.tasks.DefaultTransformTask
import io.github.flyjingfish.fast_transform.toTransformAll
import org.gradle.api.Project
import org.gradle.api.Task

object TransformPlugin : BasePlugin() {
    override fun apply(project: Project) {
        super.apply(project)
        val isApp = project.plugins.hasPlugin(AppPlugin::class.java)
        if (!isApp) {
            if (project.rootProject != project){
                project.afterEvaluate {
                    val isAndroidProject: Boolean = project.extensions.findByName(CompilePlugin.ANDROID_EXTENSION_NAME) != null
                    val isApp2 = project.plugins.hasPlugin(AppPlugin::class.java)
                    if (isApp2 && isAndroidProject){
                        throw RuntimeException("In the module of ${project.name}, [id 'android.aop'] must be written below [id 'com.android.application']")
                    }
                }
            }
            return
        }
        project.rootProject.gradle.taskGraph.addTaskExecutionGraphListener {
            try {
                var lastCanModifyTask : Task? =null
                var dexTask : DexArchiveBuilderTask? =null
                var aopTask : AssembleAndroidAopTask? =null
                for (task in it.allTasks) {
                    if (task is AssembleAndroidAopTask){
                        aopTask = task
                    }
                    if (task is DexArchiveBuilderTask){
                        dexTask = task
                        break
                    }
                    lastCanModifyTask = task
                }
                if (lastCanModifyTask != null && dexTask != null && aopTask != null){
                    if (lastCanModifyTask !is AssembleAndroidAopTask && lastCanModifyTask !is DefaultTransformTask){
                        if (aopTask.isFastDex){
                            val hintText = "When fastDex is enabled, you should put [id 'android.aop'] at the end to make ${aopTask.name} execute after ${lastCanModifyTask.name}"
                            project.logger.error(hintText)
                            aopTask.doLast {
                                project.logger.error(hintText)
                            }
                            it.allTasks[it.allTasks.size - 1].doLast {
                                project.logger.error(hintText)
                            }
                        }
                    }
                }
            } catch (_: Exception) {
            }
        }
        val androidComponents = project.extensions.getByType(AndroidComponentsExtension::class.java)
        androidComponents.onVariants { variant ->
            val androidAopConfig = project.extensions.getByType(AndroidAopConfig::class.java)
            androidAopConfig.initConfig()
            val runtimeProject = RuntimeProject.get(project)
            if (androidAopConfig.cutInfoJson){
                InitConfig.initCutInfo(runtimeProject)
            }
            val buildTypeName = variant.buildType
            if (androidAopConfig.enabled && !isDebugMode(buildTypeName,variant.name)){
                val fastDex = isFastDex(buildTypeName,variant.name)
                val task = project.tasks.register("${variant.name}AssembleAndroidAopTask", AssembleAndroidAopTask::class.java){
                    it.runtimeProject = runtimeProject
                    it.reflectInvokeMethod = isReflectInvokeMethod(buildTypeName,variant.name)
                    it.reflectInvokeMethodStatic = isReflectInvokeMethodStatic()
                    it.variant = variant.name
                }
                variant.toTransformAll(task,fastDex)
            }
        }

    }
}