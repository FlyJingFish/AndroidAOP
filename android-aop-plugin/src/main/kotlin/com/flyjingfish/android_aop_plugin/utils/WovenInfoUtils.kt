package com.flyjingfish.android_aop_plugin.utils

import com.flyjingfish.android_aop_plugin.beans.AopCollectClass
import com.flyjingfish.android_aop_plugin.beans.AopCollectCut
import com.flyjingfish.android_aop_plugin.beans.AopMatchCut
import com.flyjingfish.android_aop_plugin.beans.AopMethodCut
import com.flyjingfish.android_aop_plugin.beans.AopReplaceCut
import com.flyjingfish.android_aop_plugin.beans.ClassMethodRecord
import com.flyjingfish.android_aop_plugin.beans.ClassSuperInfo
import com.flyjingfish.android_aop_plugin.beans.MethodRecord
import com.flyjingfish.android_aop_plugin.beans.ReplaceInnerClassInfo
import com.flyjingfish.android_aop_plugin.beans.ReplaceMethodInfo
import com.flyjingfish.android_aop_plugin.config.AndroidAopConfig
import com.flyjingfish.android_aop_plugin.scanner_visitor.WovenIntoCode
import org.gradle.api.Project
import java.io.File
import java.util.concurrent.CopyOnWriteArraySet
import java.util.jar.JarFile

object WovenInfoUtils {
    var isCompile = false
    private var aopMethodCuts: HashMap<String, AopMethodCut> = HashMap()
    private var aopInstances: HashMap<String, String> = HashMap()
    private var aopMatchCuts: HashMap<String, AopMatchCut> = HashMap()
    private var lastAopMatchCuts: HashMap<String, AopMatchCut> = HashMap()
    private var classPaths: CopyOnWriteArraySet<String> = CopyOnWriteArraySet()
    private var baseClassPaths: CopyOnWriteArraySet<String> = CopyOnWriteArraySet()
    private var classNameMap: HashMap<String, String> = HashMap()
    private var baseClassNameMap: HashMap<String, String> = HashMap()
    private var classSuperListMap = HashMap<String, ClassSuperInfo>()
    private var classSuperMap = HashMap<String, String>()
    private val classSuperCacheMap = HashMap<String, String>()
    private val classMethodRecords: HashMap<String, HashMap<String, MethodRecord>> =
        HashMap()//类名为key，value为方法map集合
    private val invokeMethodCuts = mutableListOf<AopReplaceCut>()
    private val realInvokeMethodMap = HashMap<String, String>()
    private val invokeMethodMap = HashMap<String, String>()
    private val replaceMethodMap = HashMap<String, String>()
    private val replaceMethodInfoMap = HashMap<String, HashMap<String, ReplaceMethodInfo>>()
    private val replaceMethodInfoMapUse = HashMap<String, ReplaceMethodInfo>()
    private val modifyExtendsClassMap = HashMap<String, String>()
    private val allClassName = mutableSetOf<String>()
    private val aopCollectInfoMap = mutableMapOf<String,AopCollectCut>()
    private val lastAopCollectInfoMap = mutableMapOf<String,AopCollectCut>()
    private val aopCollectClassMap = mutableMapOf<String,MutableMap<String,AopCollectClass>?>()
    private val aopMethodCutInnerClassInfo = mutableMapOf<String,ReplaceInnerClassInfo>()
    private val aopMethodCutInnerClassInfoClassName = mutableSetOf<String>()
    private val aopMethodCutInnerClassInfoInvokeMethod = mutableSetOf<String>()
    private val aopMethodCutInnerClassInfoInvokeClassName = mutableSetOf<String>()
    private val aopMethodCutInnerClassInfoInvokeClassNameCount = mutableMapOf<String,Int>()
    fun getClassPaths():CopyOnWriteArraySet<String>{
        return classPaths
    }

    fun getReplaceMethodInfoMapUse():HashMap<String, ReplaceMethodInfo>{
        return replaceMethodInfoMapUse
    }

    fun getAopCollectInfoMap(): Map<String, AopCollectCut> {
        return aopCollectInfoMap
    }

    fun getAopCollectClassMap(): Map<String,MutableMap<String,AopCollectClass>?>{
        return aopCollectClassMap
    }

    fun getAopInstances():HashMap<String, String>{
        return aopInstances
    }

    fun getAopMatchCuts(): HashMap<String, AopMatchCut>{
        return aopMatchCuts
    }
    fun addModifyExtendsClassInfo(targetClassName: String, extendsClassName: String) {
        modifyExtendsClassMap[targetClassName] = extendsClassName
        InitConfig.addModifyClassInfo(targetClassName, extendsClassName)
    }
    fun getModifyExtendsClass(targetClassName: String) :String?{
        return modifyExtendsClassMap[targetClassName]
    }
    fun verifyModifyExtendsClassInfo() {
        for (mutableEntry in modifyExtendsClassMap) {
            if (mutableEntry.value.instanceof(mutableEntry.key)){
                throw IllegalArgumentException("${mutableEntry.value} 不能继承 ${mutableEntry.key}，或者其继承类不可以继承 ${mutableEntry.key}")
            }
        }
//        printLog("classMethodRecords=$classMethodRecords")
    }
    fun hasModifyExtendsClass():Boolean{
        return modifyExtendsClassMap.isNotEmpty()
    }
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
        val anno = "@" + info.substring(1, info.length).replace("/", ".").replace(";", "")
        return aopMethodCuts.contains(anno)
    }

    fun getAnnoInfo(info: String): AopMethodCut? {
        val anno = "@" + info.substring(1, info.length).replace("/", ".").replace(";", "")
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
        var methodsRecord: HashMap<String, MethodRecord>? =
            classMethodRecords[classMethodRecord.classFile]
        if (methodsRecord == null) {
            methodsRecord = HashMap()
            classMethodRecords[classMethodRecord.classFile] = methodsRecord
        }
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

    fun deleteClassMethodRecord(key: String) {
        classMethodRecords.remove(key)
    }

    fun getClassMethodRecord(classFile: String): HashMap<String, MethodRecord>? {
        return classMethodRecords[classFile]
    }


    fun addClassPath(classPath: String) {
        classPaths.add(classPath)
    }
    private fun clear() {
        invokeMethodMap.clear()
        replaceMethodMap.clear()
        replaceMethodInfoMapUse.clear()
        modifyExtendsClassMap.clear()
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
        return lastAopMatchCuts != aopMatchCuts || lastAopCollectInfoMap != aopCollectInfoMap
    }

    fun isHasExtendsReplace():Boolean{
        if (invokeMethodCutCache == null){
            invokeMethodCutCache = invokeMethodCuts.filter {
                it.matchType != AopMatchCut.MatchType.SELF.name
            }.toMutableList()
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
    }

    fun addCollectConfig(aopCollectCut: AopCollectCut){
        aopCollectInfoMap[aopCollectCut.getKey()] = aopCollectCut
    }

    fun addCollectClass(aopCollectClass: AopCollectClass){
        var set = aopCollectClassMap[aopCollectClass.invokeClassName]
        if (set == null){
            set = mutableMapOf()
            aopCollectClassMap[aopCollectClass.invokeClassName] = set
        }
        set[aopCollectClass.getKey()] = aopCollectClass
    }
    fun aopCollectChanged(isClear:Boolean) {
        if (isClear){
            aopCollectClassMap.clear()
            return
        }
        val iterator = aopCollectClassMap.iterator()
        while (iterator.hasNext()){
            val item = iterator.next()
            val key = item.key
            val value = item.value
            var containInvokeClassName = false;
            for (mutableEntry in aopCollectInfoMap) {
                if (mutableEntry.value.invokeClassName == key){
                    containInvokeClassName = true
                    break
                }
            }
            if (!containInvokeClassName){
                iterator.remove()
            }else{
                value?.let {
                    val itIterator = it.iterator()
                    while (itIterator.hasNext()){
                        val itItem = itIterator.next()
                        var itContain = false
                        for (mutableEntry in aopCollectInfoMap) {
                            if (mutableEntry.value.invokeMethod == itItem.value.invokeMethod
                                && mutableEntry.value.collectClassName == itItem.value.collectClassName
                                && mutableEntry.value.isClazz == itItem.value.isClazz
                                && mutableEntry.value.regex == itItem.value.regex
                                && mutableEntry.value.collectType == itItem.value.collectType){
                                itContain = true
                                break
                            }
                        }

                        if (!itContain){
                            itIterator.remove()
                        }
                    }
                }

            }

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
}