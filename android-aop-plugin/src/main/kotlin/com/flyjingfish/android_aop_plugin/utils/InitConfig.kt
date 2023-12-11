package com.flyjingfish.android_aop_plugin.utils

import com.android.build.gradle.internal.coverage.JacocoReportTask
import com.flyjingfish.android_aop_plugin.beans.WovenInfo
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.gradle.api.Project
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths


object InitConfig {
    private lateinit var temporaryDir: File
    private lateinit var buildConfigCacheFile: File
    var isInit: Boolean = false
    private val gson: Gson = GsonBuilder().create()
    fun <T> optFromJsonString(jsonString: String, clazz: Class<T>): T? {
        try {
            return gson.fromJson(jsonString, clazz)
        } catch (e: Throwable) {
            JacocoReportTask.JacocoReportWorkerAction.logger.warn("optFromJsonString(${jsonString}, $clazz", e)
        }
        return null
    }

    fun optToJsonString(any: Any): String {
        try {
            return gson.toJson(any)
        } catch (throwable: Throwable) {
            JacocoReportTask.JacocoReportWorkerAction.logger.warn("optToJsonString(${any}", throwable)
        }
        return ""
    }

    fun loadBuildConfig(buildConfigCacheFile: File): WovenInfo {
        return if (buildConfigCacheFile.exists()) {
            val jsonString = readAsString(buildConfigCacheFile.absolutePath)
            optFromJsonString(jsonString, WovenInfo::class.java) ?: WovenInfo()
        } else {
            WovenInfo()
        }
    }
    fun saveBuildConfig() {
        val wovenInfo = WovenInfo()
        wovenInfo.aopMatchCuts = WovenInfoUtils.aopMatchCuts
        wovenInfo.aopMethodCuts = WovenInfoUtils.aopMethodCuts
        wovenInfo.classPaths = WovenInfoUtils.classPaths
        saveFile(buildConfigCacheFile, optToJsonString(wovenInfo))
    }
    fun saveFile(file: File, data:String) {
        val fos = FileOutputStream(file.absolutePath)
        try {
            fos.write(data.toByteArray())
        } catch (e: IOException) {
            e.printStackTrace();
        }finally {
            fos.close()
        }
    }
    fun readAsString(path :String) :String {
        return try {
            val content = String(Files.readAllBytes(Paths.get(path)));
            content
        }catch (exception:Exception){
            ""
        }
    }

    fun init(project: Project):Boolean{
        temporaryDir = File(project.buildDir.absolutePath+"/tmp")
        buildConfigCacheFile = File(temporaryDir, "buildAndroidAopConfigCache.json")
        val wovenInfo = loadBuildConfig(buildConfigCacheFile)
        var count =0
        if(wovenInfo.aopMatchCuts != null){
            WovenInfoUtils.aopMatchCuts = wovenInfo.aopMatchCuts!!
            count++
        }
        if(wovenInfo.aopMethodCuts != null){
            WovenInfoUtils.aopMethodCuts = wovenInfo.aopMethodCuts!!
            count++
        }
        if(wovenInfo.classPaths != null){
            WovenInfoUtils.classPaths = wovenInfo.classPaths!!
            count++
        }

        JacocoReportTask.JacocoReportWorkerAction.logger.error("buildConfigCacheFile = ${buildConfigCacheFile.absolutePath}")
        JacocoReportTask.JacocoReportWorkerAction.logger.error("buildConfig = $wovenInfo")
        isInit = count == 3
        return isInit
    }
/*

[{
		"type": "注解",
		"className": "注解全名",
		"cutClasses": [{
			"className": "切入点类",
			"method": [{
					"name": "test",
					"returnType": "",
					"paramTypes": "int,double"
				},
				{
					"name": "test",
					"returnType": "",
					"paramTypes": "int,double"
				}
			]
		}]

	},
	{
		"type": "匹配",
		"className": "匹配全名",
		"cutClasses": [{
			"className": "切入点类",
			"method": [{
					"name": "test",
					"returnType": "",
					"paramTypes": "int,double"
				},
				{
					"name": "test",
					"returnType": "",
					"paramTypes": "int,double"
				}
			]
		}]

	}
]
 */

}