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
import com.flyjingfish.android_aop_plugin.beans.CutReplaceLocationMap
import com.flyjingfish.android_aop_plugin.beans.CutReplaceMethodJson
import com.flyjingfish.android_aop_plugin.beans.MethodRecord
import com.flyjingfish.android_aop_plugin.beans.ModifyExtendsClassJson
import com.flyjingfish.android_aop_plugin.beans.OverrideClassJson
import com.flyjingfish.android_aop_plugin.beans.ReplaceJson
import com.flyjingfish.android_aop_plugin.beans.ReplaceMethodInfo
import com.flyjingfish.android_aop_plugin.config.AndroidAopConfig
import com.flyjingfish.android_aop_plugin.config.AndroidAopConfig.Companion.cutInfoJson
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.gradle.api.Project
import org.objectweb.asm.Type
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.util.concurrent.ConcurrentHashMap


object InitConfig {
    private lateinit var temporaryDir: File
    private lateinit var buildConfigCacheFile: File
    private lateinit var cutInfoFile: File
    private val cutInfoMap = ConcurrentHashMap<String, CutJsonMap?>()
    private val replaceMethodInfoMap = ConcurrentHashMap<String, ConcurrentHashMap<String, ReplaceJson>>()
    private val modifyExtendsClassMap = ConcurrentHashMap<String, ModifyExtendsClassJson>()
    private val collectClassMap = ConcurrentHashMap<String, ConcurrentHashMap<String, CutCollectMethodJsonCache>>()
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

            val replaceCutList = mutableListOf<CutReplaceClassJson>()
            replaceMethodInfoMap.forEach { (aopClassName, concurrentHashMap) ->
                val map = CutReplaceClassJson("替换切面",aopClassName)
                concurrentHashMap.forEach { (s, replaceJson) ->
                    val json = CutReplaceMethodJson(s,replaceJson.oldClassName,replaceJson.oldMethod)
                    for (locationStr in replaceJson.methodLocationMap) {
                        val locations = locationStr.split("@")
                        json.locations.add(CutReplaceLocationMap(locations[0],locations[1]))
                    }
                    map.method.add(json)
                }
                replaceCutList.add(map)
            }

            for (json in replaceCutList) {
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


    fun addReplaceMethodInfo(replaceMethodInfo: ReplaceMethodInfo,locationClassName:String,locationMethodName:String,locationMethodDesc:String) {
//        val replaceMethodMap = ConcurrentHashMap<String, ConcurrentHashMap<String, ReplaceJson>>()
        val aopClassName:String = Utils.slashToDot(replaceMethodInfo.newOwner)
        val aopMethod:String = Type.getReturnType(replaceMethodInfo.newMethodDesc).className+" "+replaceMethodInfo.newMethodName+"("+Type.getArgumentTypes(replaceMethodInfo.newMethodDesc).joinToString{it.className}+")"
        val oldMethod:String = if (replaceMethodInfo.oldMethodName == "<init>"){
            replaceMethodInfo.oldMethodName+"("+Type.getArgumentTypes(replaceMethodInfo.oldMethodDesc).joinToString{it.className}+")"
        }else{
            Type.getReturnType(replaceMethodInfo.oldMethodDesc).className+" "+replaceMethodInfo.oldMethodName+"("+Type.getArgumentTypes(replaceMethodInfo.oldMethodDesc).joinToString{it.className}+")"
        }
        val cutMap = replaceMethodInfoMap.computeIfAbsent(aopClassName) { ConcurrentHashMap() }
        val methodJson = cutMap.computeIfAbsent(aopMethod) { ReplaceJson(Utils.slashToDot(replaceMethodInfo.oldOwner),oldMethod) }
        val location:String = "${Utils.slashToDot(locationClassName)}@"+Type.getReturnType(locationMethodDesc).className+" "+locationMethodName+"("+Type.getArgumentTypes(locationMethodDesc).joinToString{it.className}+")"

        methodJson.methodLocationMap.add(location)
    }

    fun addModifyClassInfo(targetClassName: String, extendsClassName: String){
        modifyExtendsClassMap[extendsClassName] = ModifyExtendsClassJson("修改继承类",targetClassName,extendsClassName)
    }

    fun useModifyClassInfo(targetClassName: String,extendsClassName:String){
        val json = modifyExtendsClassMap[extendsClassName]
        json?.used?.add(targetClassName)
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

    fun exportOverrideClassFile(jsonFile: File,mutableList: MutableList<String>) {
        jsonFile.checkExist()
        val json = GsonBuilder().setPrettyPrinting().create().toJson(OverrideClassJson(mutableList))

        saveFile(jsonFile, json,false)
    }

    fun exportConfigJson(jsonFile: File,androidAopConfig: AndroidAopConfig) {
        jsonFile.checkExist()
        val json = GsonBuilder().setPrettyPrinting().create().toJson(androidAopConfig)

        saveFile(jsonFile, json,false)
    }

    fun addCollect(aopCollectClass: AopCollectClass){
        val classMap = collectClassMap.computeIfAbsent(aopCollectClass.invokeClassName) { ConcurrentHashMap() }
        val paramKey = if (aopCollectClass.isClazz) "Class<? extends ${aopCollectClass.collectClassName}>" else aopCollectClass.collectClassName
        val methodKey = "${aopCollectClass.invokeMethod}($paramKey)"
        val methodSet = classMap.computeIfAbsent(methodKey) { CutCollectMethodJsonCache(aopCollectClass.collectType,aopCollectClass.regex) }
        methodSet.classes.add(aopCollectClass.collectExtendsClassName)

    }
}