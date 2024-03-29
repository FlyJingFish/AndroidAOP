package com.flyjingfish.android_aop_plugin.utils

import com.android.build.gradle.internal.coverage.JacocoReportTask
import com.flyjingfish.android_aop_plugin.beans.CutClassesJson
import com.flyjingfish.android_aop_plugin.beans.CutClassesJsonMap
import com.flyjingfish.android_aop_plugin.beans.CutJson
import com.flyjingfish.android_aop_plugin.beans.CutJsonMap
import com.flyjingfish.android_aop_plugin.beans.CutMethodJson
import com.flyjingfish.android_aop_plugin.beans.CutReplaceClassJson
import com.flyjingfish.android_aop_plugin.beans.CutReplaceClassMap
import com.flyjingfish.android_aop_plugin.beans.CutReplaceMethodJson
import com.flyjingfish.android_aop_plugin.beans.MethodRecord
import com.flyjingfish.android_aop_plugin.beans.ModifyExtendsClassJson
import com.flyjingfish.android_aop_plugin.beans.ReplaceMethodInfo
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
    private val replaceMethodInfoMap = mutableMapOf<String, ReplaceMethodInfo>()
    private val modifyExtendsClassMap = mutableMapOf<String, ModifyExtendsClassJson>()
    var isInit: Boolean = false
    private val gson: Gson = GsonBuilder().create()
    private fun <T> optFromJsonString(jsonString: String, clazz: Class<T>): T? {
        try {
            return gson.fromJson(jsonString, clazz)
        } catch (e: Throwable) {
            JacocoReportTask.JacocoReportWorkerAction.logger.warn(
                "optFromJsonString(${jsonString}, $clazz",
                e
            )
        }
        return null
    }

    private fun optToJsonString(any: Any): String {
        try {
            return gson.toJson(any)
        } catch (throwable: Throwable) {
            JacocoReportTask.JacocoReportWorkerAction.logger.warn(
                "optToJsonString(${any}",
                throwable
            )
        }
        return ""
    }

    private fun saveFile(file: File, data: String) {
        temporaryDirMkdirs()
        val fos = FileOutputStream(file.absolutePath)
        try {
            fos.write(data.toByteArray())
        } catch (e: IOException) {
            e.printStackTrace();
        } finally {
            fos.close()
        }
    }

    private fun readAsString(path: String): String {
        return try {
            val content = String(Files.readAllBytes(Paths.get(path)));
            content
        } catch (exception: Exception) {
            ""
        }
    }


    fun initCutInfo(project: Project): Boolean {
        temporaryDir = File(project.buildDir.absolutePath + "/tmp")
        cutInfoFile = File(temporaryDir, "cutInfo.json")
        cutInfoMap.clear()
        replaceMethodInfoMap.clear()
        modifyExtendsClassMap.clear()
        return isInit
    }

    fun putCutInfo(value: MethodRecord?) {
        val cutInfoMap = value?.cutInfo
        if (cutInfoMap != null) {
            val set = cutInfoMap.entries
            for (mutableEntry in set) {
                val cutInfo = mutableEntry.value
                putCutInfo(cutInfo.type, cutInfo.className, cutInfo.anno, cutInfo.cutMethodJson)
            }
        }
    }

    fun putCutInfo(type: String, className: String, anno: String, cutMethodJson: CutMethodJson) {
        var cutJson = cutInfoMap[anno]
        if (cutJson == null) {
            val cutJsonMap = CutJsonMap(type, anno)
            cutInfoMap[anno] = cutJsonMap
            cutJson = cutJsonMap
        }
        val cutClasses = cutJson.cutClasses
        var cutClassesJsonMap = cutClasses[className]
        if (cutClassesJsonMap == null) {
            val cutClassesJson = CutClassesJsonMap(className)
            cutClasses[className] = cutClassesJson
            cutClassesJsonMap = cutClassesJson
        }
        cutClassesJsonMap.method[cutMethodJson.toString()] = cutMethodJson

    }

    fun exportCutInfo() {
        if (cutInfoJson) {
            //CutJson
            val cutJsons = mutableListOf<Any>()
            cutInfoMap.forEach { (_, cutInfo) ->
                if (cutInfo != null) {
                    val cutJson = CutJson(cutInfo.type, cutInfo.className)
                    var count = 0
                    cutInfo.cutClasses.forEach { (_, cutClasses) ->
                        val cutClassesJson =
                            CutClassesJson(cutClasses.className, cutClasses.method.size)
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
            val allReplaceMethodInfo = mutableMapOf<String, ReplaceMethodInfo>()
            allReplaceMethodInfo.putAll(WovenInfoUtils.replaceMethodInfoMapUse)
            val replaceCutMap = mutableMapOf<String,CutReplaceClassMap>()
            for (replaceMethodInfo in replaceMethodInfoMap) {
                var cutJson = replaceCutMap[replaceMethodInfo.value.getReplaceJsonKey()]
                if (cutJson == null){
                    cutJson = CutReplaceClassMap(
                        "替换切面",
                        Utils.slashToDotClassName(replaceMethodInfo.value.newOwner),
                        Utils.slashToDotClassName(replaceMethodInfo.value.oldOwner)
                    )
                    replaceCutMap[replaceMethodInfo.value.getReplaceJsonKey()] = cutJson
                }
                cutJson.method[replaceMethodInfo.value.getReplaceKey()] = CutReplaceMethodJson(
                    replaceMethodInfo.value.newMethodName,
                    replaceMethodInfo.value.newMethodDesc,
                    replaceMethodInfo.value.oldMethodName,
                    replaceMethodInfo.value.oldMethodDesc,
                    "已被使用")

                allReplaceMethodInfo.remove(replaceMethodInfo.value.getReplaceKey())
            }

            for (replaceMethodInfo in allReplaceMethodInfo) {
                var cutJson = replaceCutMap[replaceMethodInfo.value.getReplaceJsonKey()]
                if (cutJson == null){
                    cutJson = CutReplaceClassMap(
                        "替换切面",
                        Utils.slashToDotClassName(replaceMethodInfo.value.newOwner),
                        Utils.slashToDotClassName(replaceMethodInfo.value.oldOwner)
                    )
                    replaceCutMap[replaceMethodInfo.value.getReplaceJsonKey()] = cutJson
                }
                cutJson.method[replaceMethodInfo.value.getReplaceKey()] = CutReplaceMethodJson(
                    replaceMethodInfo.value.newMethodName,
                    replaceMethodInfo.value.newMethodDesc,
                    replaceMethodInfo.value.oldMethodName,
                    replaceMethodInfo.value.oldMethodDesc,
                    "未被使用")
            }

            for (mutableEntry in replaceCutMap) {
                val json = CutReplaceClassJson(
                    mutableEntry.value.type,
                    mutableEntry.value.replaceClassName,
                    mutableEntry.value.targetClassName,
                    mutableEntry.value.method.values.toMutableList()
                )
                cutJsons.add(json)
            }

            for (mutableEntry in modifyExtendsClassMap) {
                cutJsons.add(mutableEntry.value)
            }

            val json = GsonBuilder().setPrettyPrinting().create().toJson(cutJsons)

            saveFile(cutInfoFile, json)
        }
    }

    fun addReplaceMethodInfo(replaceMethodInfo: ReplaceMethodInfo) {
        replaceMethodInfoMap[replaceMethodInfo.getReplaceKey()] = replaceMethodInfo
    }

    fun addModifyClassInfo(targetClassName: String, extendsClassName: String){
        modifyExtendsClassMap[targetClassName] = ModifyExtendsClassJson("修改继承类",targetClassName,extendsClassName,"未被使用")
    }

    fun useModifyClassInfo(targetClassName: String){
        val json = modifyExtendsClassMap[targetClassName]
        json?.used = "已被使用"
    }

    private fun temporaryDirMkdirs() {
        if (!temporaryDir.exists()) {
            temporaryDir.mkdirs()
        }

    }

}