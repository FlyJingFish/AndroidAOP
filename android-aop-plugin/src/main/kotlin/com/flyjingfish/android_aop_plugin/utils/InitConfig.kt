package com.flyjingfish.android_aop_plugin.utils

import com.android.build.gradle.internal.coverage.JacocoReportTask
import com.flyjingfish.android_aop_plugin.beans.CutClassesJson
import com.flyjingfish.android_aop_plugin.beans.CutClassesJsonMap
import com.flyjingfish.android_aop_plugin.beans.CutJson
import com.flyjingfish.android_aop_plugin.beans.CutJsonMap
import com.flyjingfish.android_aop_plugin.beans.CutMethodJson
import com.flyjingfish.android_aop_plugin.beans.WovenInfo
import com.flyjingfish.android_aop_plugin.config.AndroidAopConfig
import com.flyjingfish.android_aop_plugin.config.AndroidAopConfig.Companion.cutInfoJson
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
    private lateinit var cutInfoFile: File
    private val cutInfoMap = mutableMapOf<String, CutJsonMap?>()
    var isInit: Boolean = false
    private val gson: Gson = GsonBuilder().create()
    private fun <T> optFromJsonString(jsonString: String, clazz: Class<T>): T? {
        try {
            return gson.fromJson(jsonString, clazz)
        } catch (e: Throwable) {
            JacocoReportTask.JacocoReportWorkerAction.logger.warn("optFromJsonString(${jsonString}, $clazz", e)
        }
        return null
    }

    private fun optToJsonString(any: Any): String {
        try {
            return gson.toJson(any)
        } catch (throwable: Throwable) {
            JacocoReportTask.JacocoReportWorkerAction.logger.warn("optToJsonString(${any}", throwable)
        }
        return ""
    }

    private fun saveFile(file: File, data:String) {
        temporaryDirMkdirs()
        val fos = FileOutputStream(file.absolutePath)
        try {
            fos.write(data.toByteArray())
        } catch (e: IOException) {
            e.printStackTrace();
        }finally {
            fos.close()
        }
    }
    private fun readAsString(path :String) :String {
        return try {
            val content = String(Files.readAllBytes(Paths.get(path)));
            content
        }catch (exception:Exception){
            ""
        }
    }


    fun initCutInfo(project: Project):Boolean{
        temporaryDir = File(project.buildDir.absolutePath+"/tmp")
        cutInfoFile = File(temporaryDir, "cutInfo.json")
        cutInfoMap.clear()
        return isInit
    }

    fun putCutInfo(type :String,className: String,anno:String,cutMethodJson: CutMethodJson){
        var cutJson = cutInfoMap[anno]
        if (cutJson == null){
            val cutJsonMap = CutJsonMap(type,anno)
            cutInfoMap[anno] = cutJsonMap
            cutJson = cutJsonMap
        }
        val cutClasses = cutJson.cutClasses
        var cutClassesJsonMap = cutClasses[className]
        if (cutClassesJsonMap == null){
            val cutClassesJson = CutClassesJsonMap(className)
            cutClasses[className] = cutClassesJson
            cutClassesJsonMap = cutClassesJson
        }
        cutClassesJsonMap.method[cutMethodJson.toString()] = cutMethodJson

    }

    fun exportCutInfo(){
        if (cutInfoJson){
            val cutJsons = mutableListOf<CutJson>()
            cutInfoMap.forEach{ (_,cutInfo) ->
                if (cutInfo != null){
                    val cutJson = CutJson(cutInfo.type,cutInfo.className)
                    var count = 0
                    cutInfo.cutClasses.forEach{(_,cutClasses) ->
                        val cutClassesJson = CutClassesJson(cutClasses.className,cutClasses.method.size)
                        cutJson.cutClasses.add(cutClassesJson)
                        cutClasses.method.forEach { (_, cutMethodJson) ->
                            cutClassesJson.method.add(cutMethodJson)
                            count++
                        }
                    }
                    cutJson.cutCount = count
                    cutJsons.add(cutJson)
                }
            }
            val json = GsonBuilder().setPrettyPrinting().create().toJson(cutJsons)

            saveFile(cutInfoFile,json)
        }
    }

    private fun temporaryDirMkdirs(){
        if (!temporaryDir.exists()){
            temporaryDir.mkdirs()
        }

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