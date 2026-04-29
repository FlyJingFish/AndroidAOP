package com.flyjingfish.android_aop_plugin.plugin

import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.DynamicFeaturePlugin
import com.android.build.gradle.internal.tasks.DexArchiveBuilderTask
import com.flyjingfish.android_aop_plugin.config.AndroidAopConfig
import com.flyjingfish.android_aop_plugin.tasks.AssembleAndroidAopTask
import com.flyjingfish.android_aop_plugin.utils.AppClasses
import com.flyjingfish.android_aop_plugin.utils.RuntimeProject
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
        val androidAopConfig = project.extensions.getByType(AndroidAopConfig::class.java)

        project.rootProject.gradle.taskGraph.addTaskExecutionGraphListener {
            for (task in it.allTasks) {
                val project = task.project
                val isDynamic = project.plugins.hasPlugin(DynamicFeaturePlugin::class.java)
                if (isDynamic && task is AssembleAndroidAopTask){
                    task.doFirst {
                        AndroidAopConfig.syncConfig(project)
                    }
                }
            }
            val appTask = it.allTasks.firstOrNull {
                val project = it.project
                val isApp = project.plugins.hasPlugin(AppPlugin::class.java)
                isApp
            }
            appTask?.let { app ->
                AndroidAopConfig.syncConfig(app.project)
            }
            AppClasses.clearModuleNames()
            if (isApp){
                for (task in it.allTasks) {
                    val project = task.project
                    val isApp = project.plugins.hasPlugin(AppPlugin::class.java)
                    val isDynamic = project.plugins.hasPlugin(DynamicFeaturePlugin::class.java)
                    if (isApp || isDynamic){
                        AppClasses.addModuleName(project.name)
                    }
                }
                try {
                    val assembleAndroidAopTasks =  it.allTasks.filter {
                        it is AssembleAndroidAopTask
                    }

                    for ((index,aopTask) in assembleAndroidAopTasks.withIndex()) {
                        val nextTask = it.allTasks[index+1]
                        var lastCanModifyTask : Task? =null
                        for (i in index+1 until it.allTasks.size) { // 0..list.size-1
                            val task = it.allTasks[i]
                            if (task is DexArchiveBuilderTask){
                                break
                            }
                            lastCanModifyTask = task
                        }
                        if (aopTask is AssembleAndroidAopTask && aopTask.isFastDex && nextTask !is DexArchiveBuilderTask && lastCanModifyTask != null && lastCanModifyTask !is AssembleAndroidAopTask){
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

                } catch (_: Exception) {
                }
            }
        }
        val androidComponents = project.extensions.getByType(AndroidComponentsExtension::class.java)
        androidComponents.onVariants { variant ->
            val runtimeProject = RuntimeProject.get(project)

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