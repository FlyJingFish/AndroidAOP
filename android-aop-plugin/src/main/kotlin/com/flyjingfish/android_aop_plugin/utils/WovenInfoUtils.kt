package com.flyjingfish.android_aop_plugin.utils

import com.flyjingfish.android_aop_plugin.beans.AopMatchCut
import com.flyjingfish.android_aop_plugin.beans.AopMethodCut
import com.flyjingfish.android_aop_plugin.beans.ClassMethodRecord
import com.flyjingfish.android_aop_plugin.beans.MethodRecord
import java.util.ArrayList
import java.util.HashMap

object WovenInfoUtils {
    var aopMethodCuts: HashMap<String, AopMethodCut> = HashMap()
    var aopMatchCuts: HashMap<String, AopMatchCut> = HashMap()
    var classPaths : ArrayList<String> = ArrayList()
    private val classMethodRecords: HashMap<String, HashMap<String, MethodRecord>> = HashMap()//类名为key，value为方法map集合

    fun addAnnoInfo(info: AopMethodCut) {
        aopMethodCuts[info.anno] = info
    }

    fun isContainAnno(info: String): Boolean {
        val anno = "@" + info.substring(1, info.length).replace("/", ".").replace(";", "")
        return aopMethodCuts.contains(anno)
    }
    fun getAnnoInfo(info: String): AopMethodCut? {
        return aopMethodCuts[info]
    }

    fun addMatchInfo(info: AopMatchCut) {
        aopMatchCuts[info.baseClassName] = info
    }

    fun addClassMethodRecords(classMethodRecord: ClassMethodRecord) {
        var methodsRecord: HashMap<String, MethodRecord>? =
            classMethodRecords[classMethodRecord.classFile]
        if (methodsRecord == null) {
            methodsRecord = HashMap<String, MethodRecord>()
            classMethodRecords[classMethodRecord.classFile] = methodsRecord
        }
        val key = classMethodRecord.methodName.methodName + classMethodRecord.methodName.descriptor;
        methodsRecord[key] = classMethodRecord.methodName
    }

    fun getClassMethodRecord(classFile:String):HashMap<String, MethodRecord>?{
        val methodsRecord: HashMap<String, MethodRecord>? =
            classMethodRecords[classFile]
        return methodsRecord
    }

    fun getMatchInfo(classFile:String): AopMatchCut?{
        val key = Utils.slashToDot(classFile.substring(0, classFile.lastIndexOf(".")))
        return aopMatchCuts[key]
    }

    fun addClassPath(classPath:String){
        classPaths.add(classPath)
    }

    fun clear(){
        aopMethodCuts.clear()
        aopMatchCuts.clear()
        classPaths.clear()
        classMethodRecords.clear()
    }
}