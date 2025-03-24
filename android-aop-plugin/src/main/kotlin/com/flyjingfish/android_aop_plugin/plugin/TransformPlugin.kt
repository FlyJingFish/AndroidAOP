package com.flyjingfish.android_aop_plugin.plugin

import com.android.build.api.artifact.ScopedArtifact
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.ScopedArtifacts
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.internal.tasks.DexArchiveBuilderTask
import com.flyjingfish.android_aop_plugin.tasks.AssembleAndroidAopTask
import com.flyjingfish.android_aop_plugin.config.AndroidAopConfig
import com.flyjingfish.android_aop_plugin.utils.InitConfig
import org.gradle.api.Project
import org.gradle.configurationcache.extensions.capitalized

object TransformPlugin : BasePlugin() {
    override fun apply(project: Project) {
        super.apply(project)
        val isApp = project.plugins.hasPlugin(AppPlugin::class.java)
        if (!isApp) {
            return
        }

        val androidComponents = project.extensions.getByType(AndroidComponentsExtension::class.java)
        val dexTasks = mutableMapOf<String,DexArchiveBuilderTask?>()
        androidComponents.onVariants { variant ->
            val androidAopConfig = project.extensions.getByType(AndroidAopConfig::class.java)
            androidAopConfig.initConfig()
            if (androidAopConfig.cutInfoJson){
                InitConfig.initCutInfo(project)
            }
            val buildTypeName = variant.buildType
            if (androidAopConfig.enabled && !isDebugMode(buildTypeName,variant.name)){
                val fastDex = isFastDex(buildTypeName,variant.name)
                val task = project.tasks.register("${variant.name}AssembleAndroidAopTask", AssembleAndroidAopTask::class.java){
                    it.reflectInvokeMethod = isReflectInvokeMethod(buildTypeName,variant.name)
                    it.reflectInvokeMethodStatic = isReflectInvokeMethodStatic()
                    it.variant = variant.name
                    it.isFastDex = fastDex
                }
                variant.artifacts
                    .forScope(ScopedArtifacts.Scope.ALL)
                    .use(task)
                    .toTransform(
                        ScopedArtifact.CLASSES,
                        AssembleAndroidAopTask::allJars,
                        AssembleAndroidAopTask::allDirectories,
                        if (fastDex) AssembleAndroidAopTask::outputDir else AssembleAndroidAopTask::outputFile
                    )
                task.configure {
                    val outDir = it.project.layout.buildDirectory.file("intermediates/classes/${variant.name}AssembleAndroidAopTask/All/")
                    val outFile = it.project.layout.buildDirectory.file("intermediates/classes/${variant.name}AssembleAndroidAopTask/All/classes.jar")
                    if (fastDex){
                        it.doFirst{
                            if (!outDir.get().asFile.exists()){
                                outDir.get().asFile.mkdirs()
                            }
                        }
                        it.doLast {
                            val dexTaskName = "dexBuilder${variant.name.capitalized()}"
                            outDir.get().asFile.listFiles()?.filter { file -> file.name != outFile.get().asFile.name}?.let { files ->
                                dexTasks[dexTaskName]?.projectClasses?.setFrom(files)
                            }

                        }
                    }


                    it.outputFile.set(outFile)
                    it.outputDir.set(outDir)

                }
            }
        }

        project.tasks.withType(DexArchiveBuilderTask::class.java).configureEach { task ->
            dexTasks[task.name] = task
        }
    }
}