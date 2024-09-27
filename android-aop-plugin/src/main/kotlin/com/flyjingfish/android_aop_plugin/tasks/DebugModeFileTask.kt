package com.flyjingfish.android_aop_plugin.tasks

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
    abstract var packageName : String?
    @TaskAction
    fun taskAction() {
        val packageName = this.packageName
        if (packageName.isNullOrEmpty()){
            return
        }
        val filePath = "$debugModeDir/${Utils.dotToSlash(packageName)}/DebugModeBuildConfig.java".adapterOSPath()
        val file = File(filePath)
        if (file.exists()){
            return
        }
        val javaCode = """
                package $packageName;

                public class DebugModeBuildConfig {
                    public static String packageName = "$packageName";
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