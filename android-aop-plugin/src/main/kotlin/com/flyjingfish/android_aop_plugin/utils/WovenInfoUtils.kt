package com.flyjingfish.android_aop_plugin.utils

import com.flyjingfish.android_aop_plugin.beans.AopCollectClass
import com.flyjingfish.android_aop_plugin.beans.AopCollectCut
import com.flyjingfish.android_aop_plugin.beans.AopMatchCut
import com.flyjingfish.android_aop_plugin.beans.AopMethodCut
import com.flyjingfish.android_aop_plugin.beans.AopReplaceCut
import com.flyjingfish.android_aop_plugin.beans.ClassMethodRecord
import com.flyjingfish.android_aop_plugin.beans.ClassSuperInfo
import com.flyjingfish.android_aop_plugin.beans.MethodRecord
import com.flyjingfish.android_aop_plugin.beans.OverrideClassJson
import com.flyjingfish.android_aop_plugin.beans.ReplaceInnerClassInfo
import com.flyjingfish.android_aop_plugin.beans.ReplaceMethodInfo
import com.flyjingfish.android_aop_plugin.config.AndroidAopConfig
import com.flyjingfish.android_aop_plugin.scanner_visitor.MethodReplaceInvokeAdapter2
import com.flyjingfish.android_aop_plugin.ex.AndroidAOPOverrideMethodException
import org.gradle.api.Project
import org.objectweb.asm.ClassReader
import org.objectweb.asm.Type
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CopyOnWriteArraySet
import java.util.jar.JarFile

object WovenInfoUtils {
    var isCompile = false
    private var aopMethodCuts = ConcurrentHashMap<String, AopMethodCut>()
    private var aopInstances = ConcurrentHashMap<String, String>()
    private var aopMatchCuts = ConcurrentHashMap<String, AopMatchCut>()
    private var lastAopMatchCuts = ConcurrentHashMap<String, AopMatchCut>()
    private var classPaths = CopyOnWriteArraySet<String>()
    private var baseClassPaths = CopyOnWriteArraySet<String>()
    private var classNameMap = ConcurrentHashMap<String, String>()
    private var baseClassNameMap = ConcurrentHashMap<String, String>()
    private var classSuperListMap = ConcurrentHashMap<String, ClassSuperInfo>()
    private var classSuperMap = ConcurrentHashMap<String, String>()
    private val classSuperCacheMap = ConcurrentHashMap<String, String>()
    private val classMethodRecords: ConcurrentHashMap<String, HashMap<String, MethodRecord>> =
        ConcurrentHashMap()//类名为key，value为方法map集合
    private val invokeMethodCuts = CopyOnWriteArrayList<AopReplaceCut>()
    private val realInvokeMethodMap = ConcurrentHashMap<String, String>()
    private val invokeMethodMap = ConcurrentHashMap<String, String>()
    private val replaceMethodMap = ConcurrentHashMap<String, String>()
    private val replaceMethodInfoMap = ConcurrentHashMap<String, ConcurrentHashMap<String, ReplaceMethodInfo>>()
    private val replaceMethodInfoMapUse = ConcurrentHashMap<String, ReplaceMethodInfo>()
    private val modifyExtendsClassMap = ConcurrentHashMap<String, String>()
    private val modifyExtendsClassParentMap = ConcurrentHashMap<String, Boolean>()
    private val allClassName = ConcurrentHashMap.newKeySet<String>()
    private val aopCollectInfoMap = ConcurrentHashMap<String,AopCollectCut>()
    private val lastAopCollectInfoMap = ConcurrentHashMap<String,AopCollectCut>()
    private val aopCollectClassMap = ConcurrentHashMap<String,ConcurrentHashMap<String,AopCollectClass>>()
    private val aopMethodCutInnerClassInfo = ConcurrentHashMap<String,ReplaceInnerClassInfo>()
    private val aopMethodCutInnerClassInfoClassName = ConcurrentHashMap.newKeySet<String>()
    private val aopMethodCutInnerClassInfoInvokeMethod = ConcurrentHashMap.newKeySet<String>()
    private val aopMethodCutInnerClassInfoInvokeClassName = ConcurrentHashMap.newKeySet<String>()
    private val aopMethodCutInnerClassInfoInvokeClassNameCount = ConcurrentHashMap<String,Int>()
    private val overrideClassnameSet = ConcurrentHashMap.newKeySet<String>()
    private val lastOverrideClassnameSet = ConcurrentHashMap.newKeySet<String>()
    private val overrideMethodMap = ConcurrentHashMap<String,MutableSet<String>>()
    private val lastOverrideMethodMap = ConcurrentHashMap<String,MutableSet<String>>()
    fun getClassPaths():CopyOnWriteArraySet<String>{
        return classPaths
    }

    fun getReplaceMethodInfoMapUse():ConcurrentHashMap<String, ReplaceMethodInfo>{
        return replaceMethodInfoMapUse
    }

    fun getAopCollectInfoMap(): Map<String, AopCollectCut> {
        return aopCollectInfoMap
    }

    fun getAopCollectClassMap(): Map<String,MutableMap<String,AopCollectClass>?>{
        return aopCollectClassMap
    }

    fun getAopInstances():Map<String, String>{
        return aopInstances
    }

    fun getAopMatchCuts(): Map<String, AopMatchCut>{
        return aopMatchCuts
    }
    fun addModifyExtendsClassInfo(targetClassName: String, extendsClassName: String,isParent:Boolean) {
        modifyExtendsClassMap[targetClassName] = extendsClassName
        modifyExtendsClassParentMap[targetClassName] = isParent
        InitConfig.addModifyClassInfo(targetClassName, extendsClassName)
    }
    fun getModifyExtendsClass(targetClassName: String) :String?{
        return modifyExtendsClassMap[targetClassName]
    }
    fun getModifyExtendsClassParent(targetClassName: String) :Boolean{
        return modifyExtendsClassParentMap[targetClassName] == true
    }
    fun verifyModifyExtendsClassInfo() {
        for (mutableEntry in modifyExtendsClassMap) {
            if (!getModifyExtendsClassParent(mutableEntry.key)) {
                if (mutableEntry.value.instanceof(mutableEntry.key)) {
                    throw IllegalArgumentException("${mutableEntry.value} 不能继承 ${mutableEntry.key}，或者其继承类不可以继承 ${mutableEntry.key}")
                }
            }
        }
//        printLog("classMethodRecords=$classMethodRecords")
    }
    fun hasModifyExtendsClass():Boolean{
        return modifyExtendsClassMap.isNotEmpty()
    }
    fun addReplaceMethodInfo(filePath: String, replaceMethodInfo: ReplaceMethodInfo) {
        val infoMap = replaceMethodInfoMap.computeIfAbsent(filePath) { ConcurrentHashMap() }
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
        parsingOptions = null
        getWovenParsingOptions()
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

    fun addReplaceCut(aopReplaceCut: AopReplaceCut) {
        invokeMethodCuts.add(aopReplaceCut)
    }

    fun addRealReplaceInfo(targetClassName: String,invokeClassName: String) {
        realInvokeMethodMap[targetClassName] = invokeClassName
    }
    fun getRealReplaceInfo(targetClassName: String):String? {
       return realInvokeMethodMap[targetClassName]
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

    fun isAopMethodCutClass(className: String):Boolean{
        for (aopMethodCut in aopMethodCuts) {
            if (Utils.slashToDotClassName(className).contains(aopMethodCut.value.cutClassName)){
                return true
            }
        }
        return false
    }

    fun addAopInstance(key: String,className: String) {
        aopInstances[key] = className
    }

    fun isContainAnno(info: String): Boolean {
        val anno = "@" + Type.getType(info).className
        return aopMethodCuts.containsKey(anno)
    }

    fun getAnnoInfo(info: String): AopMethodCut? {
        val anno = "@" + Type.getType(info).className
        return aopMethodCuts[anno]
    }

    fun addMatchInfo(info: AopMatchCut) {
        //baseClassName -> cutClassName 防止被覆盖
        aopMatchCuts[info.cutClassName] = info
    }
    fun isAopMatchCutClass(className: String):Boolean{
        for (aopMatchCut in aopMatchCuts) {
            if (aopMatchCut.key.contains(Utils.slashToDotClassName(className))){
                return true
            }
        }
        return false
    }
    fun addClassMethodRecords(classMethodRecord: ClassMethodRecord) {
        val methodsRecord = classMethodRecords.computeIfAbsent(classMethodRecord.classFile) { HashMap() }
        synchronized(methodsRecord){
            val key = classMethodRecord.methodName.methodName + classMethodRecord.methodName.descriptor
            val oldRecord = methodsRecord[key]
            if (methodsRecord.contains(key)) {
                if (classMethodRecord.methodName.cutClassName.isNotEmpty()) {
                    methodsRecord[key]?.cutClassName?.addAll(classMethodRecord.methodName.cutClassName)
                    methodsRecord[key]?.cutClassName?.let {
                        classMethodRecord.methodName.cutClassName.addAll(it)
                    }
                    methodsRecord[key] = classMethodRecord.methodName
                }
            } else {
                methodsRecord[key] = classMethodRecord.methodName
            }
            oldRecord?.cutInfo?.let {
                methodsRecord[key]?.cutInfo?.putAll(it)
            }
            methodsRecord[key]?.cutInfo?.let {
                it.putAll(classMethodRecord.methodName.cutInfo)
            }
        }

    }

    fun deleteClassMethodRecord(key: String) {
        classMethodRecords.remove(key)
    }

    fun getClassMethodRecord(classFile: String): HashMap<String, MethodRecord>? {
        return classMethodRecords[classFile]
    }


    fun addClassPath(classPath: String) {
        classPaths.add(classPath)
    }
    fun clear() {
        invokeMethodMap.clear()
        replaceMethodMap.clear()
        replaceMethodInfoMapUse.clear()
        modifyExtendsClassMap.clear()
        modifyExtendsClassParentMap.clear()
        invokeMethodCuts.clear()
        realInvokeMethodMap.clear()
        invokeMethodCutCache = null
        aopMethodCuts.clear()
        aopInstances.clear()
        aopMethodCutInnerClassInfo.clear()
        aopMethodCutInnerClassInfoClassName.clear()
        aopMethodCutInnerClassInfoInvokeMethod.clear()
        aopMethodCutInnerClassInfoInvokeClassName.clear()
        aopMethodCutInnerClassInfoInvokeClassNameCount.clear()
//        aopCollectClassMap.clear()
        if (!AndroidAopConfig.increment) {
            aopMatchCuts.clear()
            lastAopMatchCuts.clear()
            aopCollectInfoMap.clear()
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

            lastAopCollectInfoMap.clear()
            lastAopCollectInfoMap.putAll(aopCollectInfoMap)

            aopCollectInfoMap.clear()
            aopMatchCuts.clear()
            classPaths.clear()
//        classMethodRecords.clear()
            classNameMap.clear()
//        classSuperList.clear()
            classSuperMap.clear()
        }
        lastOverrideClassnameSet.clear()
        lastOverrideClassnameSet.addAll(overrideClassnameSet)
        overrideClassnameSet.clear()

        lastOverrideMethodMap.clear()
        lastOverrideMethodMap.putAll(overrideMethodMap)
        overrideMethodMap.clear()
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
        if (!AndroidAopConfig.increment || isCompile){
            return
        }
        val set = classSuperCacheMap.entries
        for (mutableEntry in set) {
            removeDeletedClass(mutableEntry.value)
        }
    }

    fun removeDeletedClassMethodRecord() {
        if (!AndroidAopConfig.increment || isCompile){
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
        return lastAopMatchCuts != aopMatchCuts || lastAopCollectInfoMap != aopCollectInfoMap
    }

    fun isHasExtendsReplace():Boolean{
        if (invokeMethodCutCache == null){
            synchronized(this){
                if (invokeMethodCutCache == null){
                    invokeMethodCutCache = invokeMethodCuts.filter {
                        it.matchType != AopMatchCut.MatchType.SELF.name
                    }.toMutableList()
                }
            }
        }
        return !invokeMethodCutCache.isNullOrEmpty()
    }

    private var invokeMethodCutCache : MutableList<AopReplaceCut>?= null
    fun addExtendsReplace(className:String){
        if (!isHasExtendsReplace()){
            return
        }
        val ctClass = try {
            ClassPoolUtils.classPool?.getCtClass(className) ?: return
        } catch (e: Exception) {
            return
        }

        val realClsName: String
        val superName: String?
        val interfaces: Array<String?>
        try {
            realClsName = Utils.dotToSlash(className)
            superName = ctClass.superclass.name
            val interfacesCls = ctClass.interfaces
            interfaces = arrayOfNulls(interfacesCls.size)
            if (interfacesCls != null){
                for ((index,interfacesCl) in interfacesCls.withIndex()) {
                    interfaces[index] = interfacesCl.name
                }
            }
        } catch (e: Exception) {
            return
        }

        invokeMethodCutCache?.forEach { aopReplaceCut ->
            val target = Utils.dotToSlash(aopReplaceCut.targetClassName)
            if (AopMatchCut.MatchType.SELF.name != aopReplaceCut.matchType) {

                val excludeClazz = aopReplaceCut.excludeClass
                var exclude = false
                var isDirectExtends = false
                if (excludeClazz != null) {
                    val clsName = Utils.slashToDotClassName(className)
                    for (clazz in excludeClazz) {
                        if (clsName == Utils.slashToDotClassName(clazz)) {
                            exclude = true
                            break
                        }
                    }
                }
                if (!exclude) {
                    var isImplementsInterface = false
                    if (interfaces.isNotEmpty()) {
                        for (anInterface in interfaces) {
                            val inter = Utils.slashToDotClassName(anInterface!!)
                            if (inter == Utils.slashToDotClassName(aopReplaceCut.targetClassName)) {
                                isImplementsInterface = true
                                break
                            }
                        }
                    }
                    if (isImplementsInterface || Utils.slashToDotClassName(aopReplaceCut.targetClassName) == Utils.slashToDotClassName(
                            superName!!
                        )
                    ) {
                        isDirectExtends = true
                    }
                    //isDirectExtends 为true 说明是直接继承
                    if (AopMatchCut.MatchType.DIRECT_EXTENDS.name == aopReplaceCut.matchType) {
                        if (isDirectExtends) {
                            addRealReplaceInfo(realClsName,target)
                        }
                    } else if (AopMatchCut.MatchType.LEAF_EXTENDS.name == aopReplaceCut.matchType) {
                        var isExtends = false
                        if (isDirectExtends) {
                            isExtends = true
                        } else {
                            val clsName = Utils.slashToDotClassName(className)
                            val parentClsName = aopReplaceCut.targetClassName
                            if (clsName != Utils.slashToDotClassName(parentClsName)) {
                                isExtends = clsName.instanceof(
                                    Utils.slashToDotClassName(parentClsName)
                                )
                            }
                        }
                        if (isExtends && isLeaf(className)) {
                            addRealReplaceInfo(realClsName,target)
                        }
                    } else {
                        if (isDirectExtends) {
                            addRealReplaceInfo(realClsName,target)
                        } else {
                            val clsName = Utils.slashToDotClassName(className)
                            val parentClsName = aopReplaceCut.targetClassName
                            if (clsName != Utils.slashToDotClassName(parentClsName)) {
                                val isInstanceof = clsName.instanceof(
                                    Utils.slashToDotClassName(parentClsName)
                                )
                                if (isInstanceof) {
                                    addRealReplaceInfo(realClsName,target)
                                }
                            }
                        }
                    }
                }
            }
        }
//        ctClass.detach()
    }

    fun addCollectConfig(aopCollectCut: AopCollectCut){
        aopCollectInfoMap[aopCollectCut.getKey()] = aopCollectCut
    }

    fun addCollectClass(aopCollectClass: AopCollectClass){
        val map = aopCollectClassMap.computeIfAbsent(aopCollectClass.invokeClassName) { ConcurrentHashMap() }
        map[aopCollectClass.getKey()] = aopCollectClass
    }
    fun aopCollectChanged(isClear:Boolean) {
        if (isClear){
            aopCollectClassMap.clear()
            return
        }
        val deleteKey = mutableSetOf<String>()
        aopCollectClassMap.forEach{ (key,value) ->
            var containInvokeClassName = false;
            for (mutableEntry in aopCollectInfoMap) {
                if (mutableEntry.value.invokeClassName == key){
                    containInvokeClassName = true
                    break
                }
            }
            if (!containInvokeClassName){
                deleteKey.add(key)
            }else{
                val deleteItemKey = mutableSetOf<String>()
                value.forEach{(itemKey,itItem) ->
                    var itContain = false
                    for (mutableEntry in aopCollectInfoMap) {
                        if (mutableEntry.value.invokeMethod == itItem.invokeMethod
                            && mutableEntry.value.collectClassName == itItem.collectClassName
                            && mutableEntry.value.isClazz == itItem.isClazz
                            && mutableEntry.value.regex == itItem.regex
                            && mutableEntry.value.collectType == itItem.collectType){
                            itContain = true
                            break
                        }
                    }

                    if (!itContain){
                        deleteItemKey.add(itemKey)
                    }
                }
                for (s in deleteItemKey) {
                    value.remove(s)
                }
            }
        }
        for (s in deleteKey) {
            aopCollectClassMap.remove(s)
        }
    }
    fun initAllClassName(){
        allClassName.clear()
        classNameMap.forEach{(_,value)->
            allClassName.add(value)
        }
    }
    private const val CHECK_CLASS_HINT = "AndroidAOP提示：由于您切换了debugMode模式，请clean项目。"
    fun checkNoneInvokeClass(className:String){
        if (!ClassFileUtils.reflectInvokeMethod && !allClassName.contains(className) && !ClassFileUtils.debugMode){
            throw RuntimeException(CHECK_CLASS_HINT)
        }
    }

    fun checkHasInvokeClass(className:String){
        if (!ClassFileUtils.reflectInvokeMethod && allClassName.contains(className) && !ClassFileUtils.debugMode){
            throw RuntimeException(CHECK_CLASS_HINT)
        }
    }

    fun containsInvokeClass(className:String):Boolean{
        return allClassName.contains(className)
    }

    fun checkHasInvokeJson(project: Project,variant:String){
        if (!ClassFileUtils.debugMode){
            val cacheJsonFile = File(Utils.invokeJsonFile(project,variant))
            if (cacheJsonFile.exists()){
                throw RuntimeException(CHECK_CLASS_HINT)
            }
        }
    }

    fun checkHasOverrideJson(project: Project,variant:String){
        val cacheJsonFile = File(Utils.overrideClassFile(project,variant))
        if (cacheJsonFile.exists()){
            val json = InitConfig.optFromJsonString(
                InitConfig.readAsString(cacheJsonFile.absolutePath),
                OverrideClassJson::class.java)
            throw AndroidAOPOverrideMethodException("重写${json?.overrideClassJson?.get(0)}的相关方法 已经改变，需要 clean 后重新编译")
        }
    }

    fun checkLeafConfig(isApp:Boolean){
        val verifyLeafExtends = AndroidAopConfig.verifyLeafExtends
        if (!verifyLeafExtends && isApp){
            fun getHintText(location:String):String{
                return "您已在 androidAopConfig 设置了 verifyLeafExtends = false，$location 就不能再设置 LEAF_EXTENDS 类型了,如需使用请打开此项配置"
            }
            //@AndroidAopMatchClassMethod
            aopMatchCuts.forEach {(_,cutConfig) ->
                if (cutConfig.matchType == AopMatchCut.MatchType.LEAF_EXTENDS.name){
                    throw IllegalArgumentException(getHintText(cutConfig.cutClassName))
                }
            }
            //@AndroidAopReplaceClass
            invokeMethodCuts.forEach { cutConfig ->
                if (cutConfig.matchType == AopMatchCut.MatchType.LEAF_EXTENDS.name){
                    throw IllegalArgumentException(getHintText(cutConfig.invokeClassName))
                }
            }
            //@AndroidAopCollectMethod
            aopCollectInfoMap.forEach {(_,cutConfig) ->
                if (cutConfig.collectType == AopCollectCut.CollectType.LEAF_EXTENDS.name){
                    throw IllegalArgumentException(getHintText("${cutConfig.invokeClassName}.${cutConfig.invokeMethod}"))
                }
            }
        }
    }

    fun addAopMethodCutInnerClassInfo(className:String,
                                      oldMethodName: String,
                                      oldDescriptor: String){
        val newMethodName = Utils.getTargetMethodName(oldMethodName,className,oldDescriptor)
        if (oldDescriptor.endsWith("Lkotlin/coroutines/Continuation;)Ljava/lang/Object;")) {
            val key = "$className&$oldMethodName&$oldDescriptor"
            aopMethodCutInnerClassInfo[key] = ReplaceInnerClassInfo(className,oldMethodName,oldDescriptor,newMethodName)
            aopMethodCutInnerClassInfoClassName.add(className)
        }
    }

    fun getAopMethodCutInnerClassInfo(className:String,
                                      oldMethodName: String,
                                      oldDescriptor: String,
                                      innerClassName:String,
                                      innerSuperClassName:String):ReplaceInnerClassInfo?{
        return if (innerClassName.startsWith("$className$$oldMethodName")
            && innerClassName != className
            && innerSuperClassName != "kotlin/coroutines/jvm/internal/SuspendLambda"){
            val key = "$className&$oldMethodName&$oldDescriptor"
            aopMethodCutInnerClassInfo[key]
        }else{
            null
        }
    }

    fun isHasAopMethodCutInnerClassInfo(className:String):Boolean{
        for (s in aopMethodCutInnerClassInfoClassName) {
            if (className.startsWith("$s$")){
                return true
            }
        }
        return false
    }

    fun addAopMethodCutInnerClassInfoInvokeMethod(className:String,
                                                  newMethodName: String,
                                                  descriptor: String){
        if (descriptor.endsWith("Lkotlin/coroutines/Continuation;)Ljava/lang/Object;")) {
            val key = "$className&$newMethodName&$descriptor"
            aopMethodCutInnerClassInfoInvokeMethod.add(key)

        }
    }

    fun deleteAopMethodCutInnerClassInfoInvokeMethod(className:String,
                                                  newMethodName: String,
                                                  descriptor: String){
        if (descriptor.endsWith("Lkotlin/coroutines/Continuation;)Ljava/lang/Object;")) {
            val key = "$className&$newMethodName&$descriptor"
            aopMethodCutInnerClassInfoInvokeMethod.remove(key)

        }
    }

    fun isHasAopMethodCutInnerClassInfoInvokeMethod(className:String,
                                                  newMethodName: String,
                                                  descriptor: String):Boolean{
        val key = "$className&$newMethodName&$descriptor"
        return aopMethodCutInnerClassInfoInvokeMethod.contains(key)
    }

    fun addAopMethodCutInnerClassInfoInvokeClassName(className:String,count:Int){
        aopMethodCutInnerClassInfoInvokeClassName.add(className)
        aopMethodCutInnerClassInfoInvokeClassNameCount[className] = count
    }

    fun getAopMethodCutInnerClassInfoInvokeClassInfo(className:String): HashMap<String, MethodRecord>?{
        return if (aopMethodCutInnerClassInfo.isNotEmpty() && aopMethodCutInnerClassInfoInvokeClassName.contains(className)){
            val methodsRecord: HashMap<String, MethodRecord> = HashMap()
            methodsRecord[className] = MethodRecord("invokeSuspend",
                "(Ljava/lang/Object;)Ljava/lang/Object;")
//            methodsRecord[className] = MethodRecord("invoke",
//                "(Lkotlinx/coroutines/CoroutineScope;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;",
//                null)
            methodsRecord
        }else{
            null
        }
    }

    fun recordOverrideClassname(className: String, methodName: String, descriptor: String){
        overrideClassnameSet.add(className)
        val list = overrideMethodMap.computeIfAbsent(className) { ConcurrentHashMap.newKeySet() }
        list.add("$className@$methodName@$descriptor")
    }

    fun isLastOverrideClassname(className: String):Boolean{
        return lastOverrideClassnameSet.contains(className)
    }

    fun getLastOverrideMethod(className: String):MutableSet<String>{
        val record = lastOverrideMethodMap[className]
        val set = mutableSetOf<String>()
        record?.let {
            set.addAll(it)
        }
        return set
    }

    @Volatile
    private var parsingOptions : Int ?= null

    /**
     * 为了使用 [MethodReplaceInvokeAdapter2]
     */
    fun getWovenParsingOptions():Int{
        var option = parsingOptions
        return if (option == null){
            val isHasDeleteNew = replaceMethodInfoMapUse.values.any { it.isDeleteNew() }
            option = if (isHasDeleteNew){
                ClassReader.EXPAND_FRAMES
            }else{
                0
            }
            parsingOptions = option
            option
        }else{
            option
        }
    }
}