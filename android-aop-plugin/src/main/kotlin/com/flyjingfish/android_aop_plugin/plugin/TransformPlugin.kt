package com.flyjingfish.android_aop_plugin.plugin

import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.DynamicFeaturePlugin
import com.android.build.gradle.internal.tasks.DexArchiveBuilderTask
import com.flyjingfish.android_aop_plugin.config.AndroidAopConfig
import com.flyjingfish.android_aop_plugin.tasks.AssembleAndroidAopTask
import com.flyjingfish.android_aop_plugin.utils.AppClasses
import com.flyjingfish.android_aop_plugin.utils.RuntimeProject
import io.github.flyjingfish.fast_transform.tasks.DefaultTransformTask
import io.github.flyjingfish.fast_transform.toTransformAll
import org.gradle.api.Project
import org.gradle.api.Task

object TransformPlugin : BasePlugin() {
    override fun apply(project: Project) {
        super.apply(project)
        val isApp = project.plugins.hasPlugin(AppPlugin::class.java)
        val isDynamic = project.plugins.hasPlugin(DynamicFeaturePlugin::class.java)
        if (!isApp&&!isDynamic) {
            if (project.rootProject != project){
                project.afterEvaluate {
                    val isAndroidProject: Boolean = project.extensions.findByName(CompilePlugin.ANDROID_EXTENSION_NAME) != null
                    val isApp2 = project.plugins.hasPlugin(AppPlugin::class.java)
                    if (isApp2 && isAndroidProject){
                        throw RuntimeException("In the module of ${project.name}, [id 'android.aop'] must be written below [id 'com.android.application' or id 'com.android.dynamic-feature']")
                    }
                }
            }
            return
        }
        if (isApp){
            project.rootProject.gradle.taskGraph.addTaskExecutionGraphListener {
                AppClasses.clearModuleNames()
                for (task in it.allTasks) {
                    val project = task.project
                    val isApp = project.plugins.hasPlugin(AppPlugin::class.java)
                    val isDynamic = project.plugins.hasPlugin(DynamicFeaturePlugin::class.java)
                    if (isApp || isDynamic){
                        AppClasses.addModuleName(project.name)
                    }
                }
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
        }
        val androidComponents = project.extensions.getByType(AndroidComponentsExtension::class.java)
        androidComponents.onVariants { variant ->
            val runtimeProject = RuntimeProject.get(project)
            val androidAopConfig = project.extensions.getByType(AndroidAopConfig::class.java)
            if (isApp){
                androidAopConfig.initConfig()
            }
            val buildTypeName = variant.buildType
            if (androidAopConfig.enabled && !isDebugMode(buildTypeName,variant.name)){
                val fastDex = isFastDex(buildTypeName,variant.name)
                val task = project.tasks.register("${variant.name}AssembleAndroidAopTask", AssembleAndroidAopTask::class.java){
                    it.runtimeProject = runtimeProject
                    it.reflectInvokeMethod = isReflectInvokeMethod(buildTypeName,variant.name)
                    it.reflectInvokeMethodStatic = isReflectInvokeMethodStatic()
                    it.variant = variant.name
                    it.isDynamic = isDynamic
                    it.isApp = isApp
                }
                variant.toTransformAll(task,fastDex)
            }
        }

    }
}