package com.flyjingfish.android_aop_plugin.utils

import com.android.build.gradle.internal.coverage.JacocoReportTask
import com.flyjingfish.android_aop_plugin.beans.AopCollectClass
import com.flyjingfish.android_aop_plugin.beans.CutCollectMethodJsonCache
import com.flyjingfish.android_aop_plugin.beans.CutClassesJson
import com.flyjingfish.android_aop_plugin.beans.CutClassesJsonMap
import com.flyjingfish.android_aop_plugin.beans.CutCollectJson
import com.flyjingfish.android_aop_plugin.beans.CutCollectMethodJson
import com.flyjingfish.android_aop_plugin.beans.CutFileJson
import com.flyjingfish.android_aop_plugin.beans.CutJson
import com.flyjingfish.android_aop_plugin.beans.CutJsonMap
import com.flyjingfish.android_aop_plugin.beans.CutMethodJson
import com.flyjingfish.android_aop_plugin.beans.CutReplaceClassJson
import com.flyjingfish.android_aop_plugin.beans.CutReplaceClassMap
import com.flyjingfish.android_aop_plugin.beans.CutReplaceMethodJson
import com.flyjingfish.android_aop_plugin.beans.MethodRecord
import com.flyjingfish.android_aop_plugin.beans.ModifyExtendsClassJson
import com.flyjingfish.android_aop_plugin.beans.ReplaceMethodInfo
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
    private val replaceMethodInfoMap = mutableMapOf<String, ReplaceMethodInfo>()
    private val modifyExtendsClassMap = mutableMapOf<String, ModifyExtendsClassJson>()
    private val collectClassMap = mutableMapOf<String, MutableMap<String, CutCollectMethodJsonCache>>()
    var isInit: Boolean = false
    private val gson: Gson = GsonBuilder().create()
    fun <T> optFromJsonString(jsonString: String, clazz: Class<T>): T? {
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

    private fun saveFile(file: File, data: String,initTemp:Boolean = true) {
        if (initTemp){
            temporaryDirMkdirs()
        }
        val fos = FileOutputStream(file.absolutePath)
        try {
            fos.write(data.toByteArray())
        } catch (e: IOException) {
            e.printStackTrace();
        } finally {
            fos.close()
        }
    }

    fun readAsString(path: String): String {
        return try {
            val content = String(Files.readAllBytes(Paths.get(path)));
            content
        } catch (exception: Exception) {
            ""
        }
    }


    fun initCutInfo(project: Project,clear:Boolean = true): Boolean {
        temporaryDir = File(project.buildDir.absolutePath + "/tmp".adapterOSPath())
        cutInfoFile = File(temporaryDir, "cutInfo.json")
        if (clear){
            clearCache()
        }
        return isInit
    }

    private fun clearCache(){
        cutInfoMap.clear()
        replaceMethodInfoMap.clear()
        modifyExtendsClassMap.clear()
        collectClassMap.clear()
    }

    fun putCutInfo(value: MethodRecord?) {
        val cutInfoMap = value?.cutInfo
        cutInfoMap?.forEach {(_,cutInfo) ->
            putCutInfo(cutInfo.type, cutInfo.className, cutInfo.anno, cutInfo.cutMethodJson)
        }
    }

    private fun putCutInfo(type: String, className: String, anno: String, cutMethodJson: CutMethodJson) {
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

    fun exportCutInfo(clearCache:Boolean = false) {
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
            allReplaceMethodInfo.putAll(WovenInfoUtils.getReplaceMethodInfoMapUse())
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
            collectClassMap.forEach {(classKey,classMap)->
                val collectClassJson = CutCollectJson("收集切面",classKey)
                classMap.forEach {(methodName,methodCache)->
                    val methodJson = CutCollectMethodJson(methodName,methodCache.collectType,methodCache.regex,methodCache.classes.size)
                    methodJson.classes.addAll(methodCache.classes)
                    collectClassJson.collectMethod.add(methodJson)
                }
                cutJsons.add(collectClassJson)
            }
            val json = GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(cutJsons)

            saveFile(cutInfoFile, json)

            if (clearCache){
                clearCache()
            }
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

    fun exportCacheCutFile(jsonFile: File,mutableList: MutableList<String>) {
        jsonFile.checkExist()
        val json = GsonBuilder().setPrettyPrinting().create().toJson(CutFileJson(mutableList))

        saveFile(jsonFile, json,false)
    }

    fun exportConfigJson(jsonFile: File,androidAopConfig: AndroidAopConfig) {
        jsonFile.checkExist()
        val json = GsonBuilder().setPrettyPrinting().create().toJson(androidAopConfig)

        saveFile(jsonFile, json,false)
    }

    fun addCollect(aopCollectClass: AopCollectClass){
        var classMap = collectClassMap[aopCollectClass.invokeClassName]
        if (classMap == null){
            classMap = mutableMapOf()
            collectClassMap[aopCollectClass.invokeClassName] = classMap
        }
        val paramKey = if (aopCollectClass.isClazz) "Class<? extends ${aopCollectClass.collectClassName}>" else aopCollectClass.collectClassName
        val methodKey = "${aopCollectClass.invokeMethod}($paramKey)"
        var methodSet = classMap[methodKey]
        if (methodSet == null){
            methodSet = CutCollectMethodJsonCache(aopCollectClass.collectType,aopCollectClass.regex)
            classMap[methodKey] = methodSet
        }
        methodSet.classes.add(aopCollectClass.collectExtendsClassName)

    }
}