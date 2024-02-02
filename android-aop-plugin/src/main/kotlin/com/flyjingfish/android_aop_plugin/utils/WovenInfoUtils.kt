package com.flyjingfish.android_aop_plugin.utils

import com.flyjingfish.android_aop_plugin.beans.AopMatchCut
import com.flyjingfish.android_aop_plugin.beans.AopMethodCut
import com.flyjingfish.android_aop_plugin.beans.ClassMethodRecord
import com.flyjingfish.android_aop_plugin.beans.ClassSuperInfo
import com.flyjingfish.android_aop_plugin.beans.MethodRecord
import com.flyjingfish.android_aop_plugin.beans.ReplaceMethodInfo
import com.flyjingfish.android_aop_plugin.config.AndroidAopConfig
import org.gradle.api.Project
import java.io.File
import java.util.jar.JarFile

object WovenInfoUtils {
    var aopMethodCuts: HashMap<String, AopMethodCut> = HashMap()
    var aopMatchCuts: HashMap<String, AopMatchCut> = HashMap()
    private var lastAopMatchCuts: HashMap<String, AopMatchCut> = HashMap()
    var classPaths: HashSet<String> = HashSet()
    private var baseClassPaths: HashSet<String> = HashSet()
    private var classNameMap: HashMap<String, String> = HashMap()
    private var baseClassNameMap: HashMap<String, String> = HashMap()
    private var classSuperListMap = HashMap<String, ClassSuperInfo>()
    private var classSuperMap = HashMap<String, String>()
    private val classSuperCacheMap = HashMap<String, String>()
    private val classMethodRecords: HashMap<String, HashMap<String, MethodRecord>> =
        HashMap()//类名为key，value为方法map集合
    private val invokeMethodMap = HashMap<String, String>()
    private val replaceMethodMap = HashMap<String, String>()
    private val replaceMethodInfoMap = HashMap<String, HashMap<String, ReplaceMethodInfo>>()
    val replaceMethodInfoMapUse = HashMap<String, ReplaceMethodInfo>()
    fun addReplaceMethodInfo(filePath: String, replaceMethodInfo: ReplaceMethodInfo) {
        var infoMap = replaceMethodInfoMap[filePath]
        if (infoMap == null){
            infoMap = HashMap()
            replaceMethodInfoMap[filePath] = infoMap
        }
        infoMap[replaceMethodInfo.getReplaceKey()] = replaceMethodInfo
    }
    fun deleteReplaceMethodInfo(filePath: String) {
        replaceMethodInfoMap.remove(filePath)
    }
    fun makeReplaceMethodInfoUse() {
        replaceMethodInfoMapUse.clear()
        for (mutableEntry in replaceMethodInfoMap) {
            replaceMethodInfoMapUse.putAll(mutableEntry.value)
        }
    }
    fun getReplaceMethodInfoUse(key: String):ReplaceMethodInfo? {
        return replaceMethodInfoMapUse[key]
    }
    fun hasReplace():Boolean{
        return replaceMethodInfoMapUse.isNotEmpty()
    }
    fun addReplaceInfo(targetClassName: String,invokeClassName: String) {
        invokeMethodMap[invokeClassName] = targetClassName
        replaceMethodMap[targetClassName] = invokeClassName
    }
    fun containInvoke(className: String):Boolean{
        return invokeMethodMap.containsKey(className)
    }
    fun getTargetClassName(className: String):String?{
        return invokeMethodMap[className]
    }

    fun isReplaceMethod(className: String):Boolean{
        if (containReplace(className)){
            return false
        }
        for (mutableEntry in invokeMethodMap) {
            if (className.contains(mutableEntry.key)){
                return false
            }
        }
        return true
    }

    fun containReplace(className: String):Boolean{
        return replaceMethodMap.containsKey(className)
    }
    fun getReplaceClassName(className: String):String?{
        return replaceMethodMap[className]
    }
    fun addAnnoInfo(info: AopMethodCut) {
        aopMethodCuts[info.anno] = info
    }

    fun isContainAnno(info: String): Boolean {
        val anno = "@" + info.substring(1, info.length).replace("/", ".").replace(";", "")
        return aopMethodCuts.contains(anno)
    }

    fun getAnnoInfo(info: String): AopMethodCut? {
        val anno = "@" + info.substring(1, info.length).replace("/", ".").replace(";", "")
        return aopMethodCuts[anno]
    }

    fun addMatchInfo(info: AopMatchCut) {
        aopMatchCuts[info.baseClassName] = info
    }

    fun addClassMethodRecords(classMethodRecord: ClassMethodRecord) {
        var methodsRecord: HashMap<String, MethodRecord>? =
            classMethodRecords[classMethodRecord.classFile]
        if (methodsRecord == null) {
            methodsRecord = HashMap()
            classMethodRecords[classMethodRecord.classFile] = methodsRecord
        }
        val key = classMethodRecord.methodName.methodName + classMethodRecord.methodName.descriptor
        val oldRecord = methodsRecord[key]
        if (methodsRecord.contains(key)) {
            if (!classMethodRecord.methodName.cutClassName.isNullOrEmpty()) {
                methodsRecord[key] = classMethodRecord.methodName
            }
        } else {
            methodsRecord[key] = classMethodRecord.methodName
        }
        oldRecord?.cutInfo?.let { methodsRecord[key]?.cutInfo?.putAll(it) }
    }

    fun deleteClassMethodRecord(key: String) {
        classMethodRecords.remove(key)
    }

    fun getClassMethodRecord(classFile: String): HashMap<String, MethodRecord>? {
        return classMethodRecords[classFile]
    }

    fun getMatchInfo(classFile: String): AopMatchCut? {
        val key = Utils.slashToDot(classFile.substring(0, classFile.lastIndexOf(".")))
        return aopMatchCuts[key]
    }

    fun addClassPath(classPath: String) {
        classPaths.add(classPath)
    }
    private fun clear() {
        invokeMethodMap.clear()
        replaceMethodMap.clear()
        replaceMethodInfoMapUse.clear()
        if (!AndroidAopConfig.increment) {
            aopMethodCuts.clear()
            aopMatchCuts.clear()
            lastAopMatchCuts.clear()
            classPaths.clear()
            baseClassPaths.clear()
            classNameMap.clear()
            baseClassNameMap.clear()
            classSuperListMap.clear()
            classSuperMap.clear()
            classSuperCacheMap.clear()
            classMethodRecords.clear()
        } else {

            classSuperCacheMap.clear()
            classSuperCacheMap.putAll(classSuperMap)

            lastAopMatchCuts.clear()
            lastAopMatchCuts.putAll(aopMatchCuts)
            aopMethodCuts.clear()
            aopMatchCuts.clear()
            classPaths.clear()
//        classMethodRecords.clear()
            classNameMap.clear()
//        classSuperList.clear()
            classSuperMap.clear()
        }
    }

    fun addClassName(classPath: String) {
        val key = Utils.slashToDot(classPath).replace(".class", "").replace("$", ".")
        val value = Utils.slashToDot(classPath).replace(".class", "")
        classNameMap[key] = value
    }

    private fun addBaseClassName(classPath: String) {
        val key = Utils.slashToDot(classPath).replace(".class", "").replace("$", ".")
        val value = Utils.slashToDot(classPath).replace(".class", "")
        baseClassNameMap[key] = value
    }

    fun getClassString(key: String): String? {
        return classNameMap[key]
    }

    fun addClassSuper(file: String, classSuper: ClassSuperInfo) {
        classSuperListMap[classSuper.className] = classSuper
        classSuperMap[file] = classSuper.className
    }

    fun isLeaf(className: String): Boolean {
        val set = classSuperListMap.entries
        for (classSuperInfo in set) {
            if (classSuperInfo.value.superName == className || (classSuperInfo.value.interfaces?.contains(
                    className
                ) == true)
            ) {
                return false
            }
        }
        return true
    }

    private fun removeDeletedClass(key: String) {
//        printLog("removeDeletedClass= key =$key,value=${classSuperListMap[key]}")
        classSuperListMap.remove(key)
    }

    /**
     * 删除访问到的文件，最后剩下就是真正删除了的文件
     */
    fun removeClassCache(key: String) {
        classSuperCacheMap.remove(key)
    }

    fun removeDeletedClass() {
        if (!AndroidAopConfig.increment){
            return
        }
        val set = classSuperCacheMap.entries
        for (mutableEntry in set) {
            removeDeletedClass(mutableEntry.value)
        }
    }

    fun removeDeletedClassMethodRecord() {
        if (!AndroidAopConfig.increment){
            return
        }
        val set = classSuperCacheMap.entries
        for (mutableEntry in set) {
            classMethodRecords.remove(mutableEntry.key)
        }
    }

    fun addBaseClassInfo(project: Project) {
        val androidConfig = AndroidConfig(project)
        val list: List<File> = androidConfig.getBootClasspath()
//        printLog("Scan to classPath [${list}]")
//        printLog("Scan to classPath [${classPaths}]")
        clear()

        val classPaths: HashSet<String> = HashSet()
        for (file in list) {
            classPaths.add(file.absolutePath)
        }

        if (classPaths != baseClassPaths) {
            baseClassPaths.clear()
            baseClassPaths.addAll(classPaths)

            baseClassNameMap.clear()
            fillClassNameMap(list)
        }
        if (baseClassNameMap.isEmpty()) {
            fillClassNameMap(list)
        }
        WovenInfoUtils.classPaths.addAll(classPaths)
        classNameMap.putAll(baseClassNameMap)
    }

    private fun fillClassNameMap(list: List<File>) {
        for (file in list) {
            try {
                val jarFile = JarFile(file)
                val enumeration = jarFile.entries()
                while (enumeration.hasMoreElements()) {
                    val jarEntry = enumeration.nextElement()
                    try {
                        val entryName = jarEntry.name
                        if (entryName.endsWith(Utils._CLASS)) {
                            addBaseClassName(entryName)
                        }
                    } catch (_: Exception) {

                    }
                }
                jarFile.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun aopMatchsChanged(): Boolean {
        return lastAopMatchCuts != aopMatchCuts
    }
}