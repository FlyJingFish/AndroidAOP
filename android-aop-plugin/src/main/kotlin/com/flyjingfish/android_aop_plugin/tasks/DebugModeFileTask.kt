package com.flyjingfish.android_aop_plugin.tasks

import com.android.build.gradle.AppPlugin
import com.flyjingfish.android_aop_plugin.config.AndroidAopConfig
import com.flyjingfish.android_aop_plugin.plugin.BasePlugin
import com.flyjingfish.android_aop_plugin.plugin.PluginConfig
import com.flyjingfish.android_aop_plugin.utils.InitConfig
import com.flyjingfish.android_aop_plugin.utils.Utils
import com.flyjingfish.android_aop_plugin.utils.adapterOSPath
import com.flyjingfish.android_aop_plugin.utils.checkExist
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException


abstract class DebugModeFileTask : DefaultTask() {
    @get:Input
    abstract var debugModeDir : String

    @get:Input
    abstract var variantName : String

    @get:Input
    abstract var isAndroidModule : Boolean

    @get:Input
    abstract var buildTypeName : String?

    @get:Input
    abstract var packageName : String?

    @TaskAction
    fun taskAction() {
        val pluginConfig = PluginConfig(project)
        val exportFile :Boolean = if (isAndroidModule){
            val isApp = project.plugins.hasPlugin(AppPlugin::class.java)
            val androidAopConfig : AndroidAopConfig = if (isApp){
                project.extensions.getByType(AndroidAopConfig::class.java)
            }else{
                var config = InitConfig.optFromJsonString(
                    InitConfig.readAsString(Utils.configJsonFile(project)),
                    AndroidAopConfig::class.java)
                if (config == null){
                    config = AndroidAopConfig()
                }
                config
            }

            androidAopConfig.enabled && pluginConfig.isDebugMode(buildTypeName,variantName)
        }else{
            var config = InitConfig.optFromJsonString(
                InitConfig.readAsString(Utils.configJsonFile(project)),
                AndroidAopConfig::class.java)
            if (config == null){
                config = AndroidAopConfig()
            }
            config.enabled && pluginConfig.isDebugMode()
        }

        if (!exportFile){
            return
        }

        val packageName = this.packageName
        if (packageName.isNullOrEmpty() || packageName == "null"){
            return
        }
        val filePath = "$debugModeDir/${Utils.dotToSlash(packageName)}/DebugModeBuildConfig.java".adapterOSPath()
        val file = File(filePath)
        if (file.exists()){
            val content = file.readText()
            if (content.contains("\"$packageName\";")){
                return
            }
        }
        val javaCode = """
                package $packageName;

                public class DebugModeBuildConfig {
                    public static final String packageName = "$packageName";
                }
                
                """.trimIndent()
        file.checkExist()
        try {
            BufferedWriter(FileWriter(filePath)).use { writer ->
                writer.write(javaCode)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}