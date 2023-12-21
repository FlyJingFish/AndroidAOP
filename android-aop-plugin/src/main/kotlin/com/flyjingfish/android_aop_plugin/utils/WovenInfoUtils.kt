package com.flyjingfish.android_aop_plugin.utils

import com.flyjingfish.android_aop_plugin.beans.AopMatchCut
import com.flyjingfish.android_aop_plugin.beans.AopMethodCut
import com.flyjingfish.android_aop_plugin.beans.ClassMethodRecord
import com.flyjingfish.android_aop_plugin.beans.ClassSuperInfo
import com.flyjingfish.android_aop_plugin.beans.MethodRecord
import org.gradle.api.Project
import java.io.File
import java.util.jar.JarFile

object WovenInfoUtils {
    var aopMethodCuts: HashMap<String, AopMethodCut> = HashMap()
    var aopMatchCuts: HashMap<String, AopMatchCut> = HashMap()
    var lastAopMatchCuts: HashMap<String, AopMatchCut> = HashMap()
    var classPaths : HashSet<String> = HashSet()
    private var baseClassPaths : HashSet<String> = HashSet()
    private var classNameMap: HashMap<String, String> = HashMap()
    private var baseClassNameMap: HashMap<String, String> = HashMap()
    private var classSuperList = arrayListOf<ClassSuperInfo>()
    private val classMethodRecords: HashMap<String, HashMap<String, MethodRecord>> = HashMap()//类名为key，value为方法map集合

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
        if (methodsRecord.contains(key)){
            if (!classMethodRecord.methodName.cutClassName.isNullOrEmpty()){
                methodsRecord[key] = classMethodRecord.methodName
            }
        }else{
            methodsRecord[key] = classMethodRecord.methodName
        }
    }

    fun deleteClassMethodRecord(key: String){
        classMethodRecords.remove(key)
    }

    fun getClassMethodRecord(classFile:String):HashMap<String, MethodRecord>?{
        return classMethodRecords[classFile]
    }

    fun getMatchInfo(classFile:String): AopMatchCut?{
        val key = Utils.slashToDot(classFile.substring(0, classFile.lastIndexOf(".")))
        return aopMatchCuts[key]
    }

    fun addClassPath(classPath:String){
        classPaths.add(classPath)
    }

    fun clear(){
        lastAopMatchCuts.clear()
        lastAopMatchCuts.putAll(aopMatchCuts)
        aopMethodCuts.clear()
        aopMatchCuts.clear()
        classPaths.clear()
//        classMethodRecords.clear()
        classNameMap.clear()
//        classSuperList.clear()
    }

    fun addClassName(classPath:String){
        val key = Utils.slashToDot(classPath).replace(".class","").replace("$",".")
        val value = Utils.slashToDot(classPath).replace(".class","")
        classNameMap[key] = value
    }

    fun addBaseClassName(classPath:String){
        val key = Utils.slashToDot(classPath).replace(".class","").replace("$",".")
        val value = Utils.slashToDot(classPath).replace(".class","")
        baseClassNameMap[key] = value
    }

    fun getClassString(key:String):String?{
        return classNameMap[key]
    }

    fun addClassSuper(classSuper :ClassSuperInfo){
        classSuperList.add(classSuper)
    }

    fun isLeaf(className:String):Boolean{
        for (classSuperInfo in classSuperList) {
            if (classSuperInfo.superName == className || (classSuperInfo.interfaces?.contains(className) == true)){
                return false
            }
        }
        return true
    }

    fun addBaseClassInfo(project: Project){
        val androidConfig = AndroidConfig(project)
        val list: List<File> = androidConfig.getBootClasspath()
        printLog("Scan to classPath [${list}]")
//        printLog("Scan to classPath [${classPaths}]")
        clear()

        val classPaths : HashSet<String> = HashSet()
        for (file in list) {
            classPaths.add(file.absolutePath)
        }

        if (classPaths != baseClassPaths){
            baseClassPaths.clear()
            baseClassPaths.addAll(classPaths)

            baseClassNameMap.clear()
            fillClassNameMap(list)
        }
        if (baseClassNameMap.isEmpty()){
            fillClassNameMap(list)
        }
        WovenInfoUtils.classPaths.addAll(classPaths)
        classNameMap.putAll(baseClassNameMap)
    }

    private fun fillClassNameMap(list: List<File>){
        for (file in list) {
            try {
                val jarFile = JarFile(file)
                val enumeration = jarFile.entries()
                while (enumeration.hasMoreElements()) {
                    val jarEntry = enumeration.nextElement()
                    try {
                        val entryName = jarEntry.name
                        if (entryName.endsWith(Utils._CLASS)){
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

    fun aopMatchsChanged():Boolean{
        return lastAopMatchCuts != aopMatchCuts
    }
}