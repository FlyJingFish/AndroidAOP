package com.flyjingfish.android_aop_plugin.plugin

import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.gradle.AppPlugin
import com.flyjingfish.android_aop_plugin.config.AndroidAopConfig
import com.flyjingfish.android_aop_plugin.tasks.AssembleAndroidAopTask
import com.flyjingfish.android_aop_plugin.utils.InitConfig
import io.github.flyjingfish.fast_transform.toTransformAll
import org.gradle.api.Project

object TransformPlugin : BasePlugin() {
    override fun apply(project: Project) {
        super.apply(project)
        val isApp = project.plugins.hasPlugin(AppPlugin::class.java)
        if (!isApp) {
            return
        }

        val androidComponents = project.extensions.getByType(AndroidComponentsExtension::class.java)
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
                }
                variant.toTransformAll(task,fastDex)
            }
        }

    }
}