package com.flyjingfish.android_aop_plugin.utils

import com.flyjingfish.android_aop_plugin.beans.AopMatchCut
import com.flyjingfish.android_aop_plugin.beans.ExtendsWeavingRules
import com.flyjingfish.android_aop_plugin.beans.MatchMethodInfo
import com.flyjingfish.android_aop_plugin.beans.ReplaceMethodInfo
import com.flyjingfish.android_aop_plugin.config.AndroidAopConfig
import javassist.CtMethod
import javassist.Modifier
import javassist.NotFoundException
import javassist.bytecode.SignatureAttribute
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.objectweb.asm.Opcodes
import org.objectweb.asm.commons.Method
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.regex.Matcher
import java.util.regex.Pattern
import java.util.zip.ZipInputStream


object Utils {
    const val annotationPackage = "com.flyjingfish.android_aop_annotation"
    const val corePackage = "com.flyjingfish.android_aop_core"
    const val extraPackage = "com.flyjingfish.android_aop_extra"
    const val JoinAnnoCutUtils = "${annotationPackage}.utils.JoinAnnoCutUtils"
    const val _CLASS = ".class"
    const val AOP_CONFIG_END_NAME = "\$\$AndroidAopClass.class"
    const val KEEP_CLASS = "com.flyjingfish.android_aop_annotation.aop_anno.AopKeep"
    const val OBJECT_UTILS = "com.flyjingfish.android_aop_annotation.utils.ObjectGetUtils"
    const val JOIN_POINT_CLASS = "com.flyjingfish.android_aop_annotation.AndroidAopJoinPoint"
    const val CONVERSIONS_CLASS = "com.flyjingfish.android_aop_annotation.Conversions"
    const val JOIN_LOCK = "com.flyjingfish.android_aop_annotation.utils.JoinPointLock"
    const val METHOD_SUFFIX = "\$\$AndroidAOP"
    var logger: Logger ?= null

    fun dotToSlash(str: String): String {
        return str.replace(".", "/")
    }

    fun slashToDot(str: String): String {
        return str.replace("/", ".")
    }

    fun slashToDotClassName(str: String): String {
        return str.replace("/", ".").replace("$", ".")
    }

    fun isExcludeFilterMatched(str: String?, filters: List<String>?): Boolean {
        return isFilterMatched(str, filters, FilterPolicy.EXCLUDE)
    }

    fun isIncludeFilterMatched(str: String?, filters: List<String>?): Boolean {
        return isFilterMatched(str, filters, FilterPolicy.INCLUDE)
    }

    private fun isFilterMatched(
        str: String?,
        filters: List<String>?,
        filterPolicy: FilterPolicy
    ): Boolean {
        if (str == null) {
            return false
        }

        if (filters.isNullOrEmpty()) {
            return filterPolicy == FilterPolicy.INCLUDE
        }

        for (s in filters) {
            if (isContained(str, s)) {
                return true
            }
        }

        return false
    }

    private fun isContained(str: String?, packageName: String): Boolean {
        if (str == null) {
            return false
        }

        val realPackageName = if (packageName.contains("/")) {
            packageName.replace("/", ".")
        } else if (packageName.contains("\\")) {
            packageName.replace("\\", ".")
        } else {
            packageName
        }

        return if (str == realPackageName){
            true
        }else {
            val filter = if (realPackageName.endsWith(".")){
                realPackageName
            }else {
                "$realPackageName."
            }
            str.startsWith(filter)
        }
    }

    private enum class FilterPolicy {
        INCLUDE,
        EXCLUDE
    }

    fun getMethodInfo(methodName: String): MatchMethodInfo? {
        val pattern: Pattern = Pattern.compile("\\(.*?\\)")
        val matcher: Matcher = pattern.matcher(methodName)
        var returnType: String? = null
        var name: String? = null
        var params: String? = null
        if (matcher.find()) {
            val paramStr: String = matcher.group()
            val nameStr: String = matcher.replaceAll("")
            val names = nameStr.split(" ")
            if (names.size > 1) {
                for (value in names) {
                    if (value.isNotEmpty() && returnType == null){
                        returnType = value
                    }else if (value.isNotEmpty() && returnType != null){
                        name = value
                    }
                }
                if (name == null && returnType != null){
                    name = returnType
                    returnType = null
                }
            } else {
                name = nameStr
            }
            params = paramStr
        } else if (methodName.contains(" ")) {
            val names = methodName.split(" ").toTypedArray()
            if (names.size > 1) {
                for (value in names) {
                    if (value.isNotEmpty() && returnType == null){
                        returnType = value
                    }else if (value.isNotEmpty() && returnType != null){
                        name = value
                    }
                }
                if (name == null && returnType != null){
                    name = returnType
                    returnType = null
                }
            } else {
                for (s in names) {
                    if ("" != s) {
                        name = s
                    }
                }
            }
        } else {
            name = methodName
        }
        var matchMethodInfo: MatchMethodInfo? = null
        if (name != null && "" != name) {
            matchMethodInfo = MatchMethodInfo()
            matchMethodInfo.name = name
            if (returnType != null && "" != returnType) {
                matchMethodInfo.returnType = returnType
            }
            matchMethodInfo.paramTypes = params
        }

        if (matchMethodInfo?.returnType == "suspend"){
            //说明是协程函数
            matchMethodInfo.returnType = "java.lang.Object"
            if (matchMethodInfo.paramTypes == "()"){
                matchMethodInfo.paramTypes = matchMethodInfo.paramTypes?.replace(")","kotlin.coroutines.Continuation)")
            }else if (!matchMethodInfo.paramTypes.isNullOrEmpty()){
                matchMethodInfo.paramTypes = matchMethodInfo.paramTypes?.replace(")",",kotlin.coroutines.Continuation)")
            }

        }
        return matchMethodInfo
    }

    fun verifyMatchCut(descriptor: String?, info: MatchMethodInfo): Boolean {
        val descriptorPattern = Pattern.compile("\\(.*?\\)")
        val descriptorMatcher = descriptorPattern.matcher(descriptor)
        val descriptorParamType = if (descriptorMatcher.find()) {
            descriptorMatcher.group()
        }else{
            null
        }
        val descriptorMatcher1 = descriptorPattern.matcher(descriptor)
        val descriptorReturnType = if (descriptorMatcher1.find()) {
            descriptorMatcher1.replaceAll("")
        }else{
            null
        }

        var back = true
        if (info.paramTypes != null) {//验证参数类型
            val paramMethodName = "void " + info.name + info.paramTypes
            val paramMethod = Method.getMethod(paramMethodName)
            val paramPattern = Pattern.compile("\\(.*?\\)")
            val paramMatcher = paramPattern.matcher(paramMethod.descriptor)
            val paramType = if (paramMatcher.find()) {
                paramMatcher.group()
            }else{
                null
            }

            back = paramType == descriptorParamType
        }

        if (info.returnType != null && back) {//验证返回类型
            val returnMethodName = info.returnType + " " + info.name + "()"
            val returnMethod = Method.getMethod(returnMethodName)
            val returnPattern = Pattern.compile("\\(.*?\\)")
            val returnMatcher = returnPattern.matcher(returnMethod.descriptor)
            val returnType = if (returnMatcher.find()) {
                returnMatcher.replaceAll("")
            }else{
                null
            }
            back = returnType == descriptorReturnType
        }

        return back
    }

    fun bytesToHex(bytes: ByteArray): String {
        val hexString = StringBuilder()
        for (b in bytes) {
            val hex = Integer.toHexString(0xff and b.toInt())
            if (hex.length == 1) {
                hexString.append('0')
            }
            hexString.append(hex)
        }
        return hexString.toString()
    }

    fun openJar(jarPath:String,destDir:String) {
        // JAR文件路径和目标目录
        ZipInputStream(FileInputStream(jarPath)).use { zis ->
            while (true) {
                val entry = zis.nextEntry ?: break
                val entryName: String = entry.name
                if (entryName.isEmpty() || entryName.startsWith("META-INF/") || "module-info.class" == entryName) {
                    continue
                }
                val filePath: String = destDir + File.separator + entryName
                if (!entry.isDirectory) {
                    val file = File(filePath)
                    val parent = file.parentFile
                    if (!parent.exists()) {
                        parent.mkdirs()
                    }
                    FileOutputStream(file).use {
                        zis.copyTo(it)
                    }
                } else {
                    File(filePath).mkdirs()
                }
            }
        }
    }

    fun invokeJsonFile(project:Project, variantName:String):String{
        return project.buildDir.absolutePath+"/tmp/android-aop/${variantName}/cacheInfo.json".adapterOSPath()
    }
    fun overrideClassFile(project:Project, variantName:String):String{
        return project.buildDir.absolutePath+"/tmp/android-aop/${variantName}/overrideClass.json".adapterOSPath()
    }
    fun aopCompileTempDir(project:Project, variantName:String):String{
        return project.buildDir.absolutePath + "/tmp/android-aop/${variantName}/tempCompileClass/".adapterOSPath()
    }
    fun aopCompileTempOtherDir(project:Project, variantName:String):String{
        return project.buildDir.absolutePath + "/tmp/android-aop/${variantName}/tempCompileOtherClass/".adapterOSPath()
    }
    fun aopCompileTempInvokeDir(project:Project, variantName:String):String{
        return project.buildDir.absolutePath + "/tmp/android-aop/${variantName}/tempCompileInvokeClass/".adapterOSPath()
    }
    fun aopCompileTempInvokeCacheDir(project:Project, variantName:String):String{
        return project.buildDir.absolutePath + "/tmp/android-aop/${variantName}/tempCompileInvokeCacheClass/".adapterOSPath()
    }
    fun aopCompileTempOtherJson(project:Project, variantName:String):String{
        return project.buildDir.absolutePath + "/tmp/android-aop/${variantName}/tempCompileOtherClassJson/needDelClassInfo.json".adapterOSPath()
    }
    fun aopTransformTempDir(project:Project, variantName:String):String{
        return project.buildDir.absolutePath+"/tmp/android-aop/${variantName}/tempInvokeClass/".adapterOSPath()
    }
    fun aopTransformCollectTempDir(project:Project, variantName:String):String{
        return project.buildDir.absolutePath+"/tmp/android-aop/${variantName}/tempCollectClass/".adapterOSPath()
    }
//    fun aopProjectJarTempDir(project:Project, variantName:String):String{
//        return project.buildDir.absolutePath+"/tmp/android-aop/${variantName}/tempProjectJar/".adapterOSPath()
//    }
    fun aopTransformIgnoreJarDir(project:Project, variantName:String):String{
        return project.buildDir.absolutePath+"/tmp/android-aop/${variantName}/tempTransformIgnoreJar/".adapterOSPath()
    }
    fun aopDebugModeJavaDir(variantName:String):String{
        return aopDebugModeJavaDirNoAdapter(variantName).adapterOSPath()
    }
    fun aopDebugModeJavaDir4Java():String{
        return aopDebugModeJavaDir4JavaNoAdapter().adapterOSPath()
    }
    fun aopDebugModeJavaDirNoAdapter(variantName:String):String{
        return "/generated/android_aop/${variantName}/"
    }
    fun aopDebugModeJavaDir4JavaNoAdapter():String{
        return "/generated/sources/android_aop/"
    }
    fun configJsonFile(project:Project):String{
        return project.buildDir.absolutePath+"/tmp/android-aop/config/androidAopConfig.json".adapterOSPath()
    }

//    fun isProjectJar(project:Project?,filePath: String):Boolean{
//        if (project == null){
//            return false
//        }
//        val projectPath = project.rootProject.buildDir.absolutePath.replace("/build".adapterOSPath(),"")
//        return filePath.startsWith(projectPath) && filePath.endsWith(".jar")
//    }
//
//    fun getNewProjectJar(project:Project,variantName:String,filePath: String):String?{
//        val oldJar = File(filePath)
//        if (oldJar.exists()){
//            val path = aopProjectJarTempDir(project,variantName)
//            val jarPath = "$path${UUID.randomUUID().toString().replace("-","")}/${oldJar.absolutePath.computeMD5()}.jar".adapterOSPath()
//            val newJar = File(jarPath)
//            oldJar.copyTo(newJar,true)
//            return newJar.absolutePath
//        }
//        return null
//    }

    private val classnamePattern = Pattern.compile("Lkotlin/coroutines/jvm/internal/SuspendLambda;Lkotlin/jvm/functions/Function2<Lkotlinx/coroutines/CoroutineScope;Lkotlin/coroutines/Continuation<-.*?>;Ljava/lang/Object;>;")
    private val classnamePattern1 = Pattern.compile("Lkotlin/coroutines/jvm/internal/SuspendLambda;Lkotlin/jvm/functions/Function2<Lkotlinx/coroutines/CoroutineScope;Lkotlin/coroutines/Continuation<-.*?")

    fun getSuspendClassType(type: String?): String? {
        val typeName = getType(type, classnamePattern, classnamePattern1, ">;Ljava/lang/Object;>;")
        if (typeName != null) {
            return extractRawTypeName(typeName)
        }
        return null
    }
    private val signatureClassnamePattern = Pattern.compile("\\(.*?kotlin/coroutines/Continuation<-.*?>;\\)Ljava/lang/Object;")
    private val signatureClassnamePattern1 = Pattern.compile("\\(.*?kotlin/coroutines/Continuation<-.*?")
    fun getSuspendMethodType(type: String?): String? {
        val typeName = getType(type,signatureClassnamePattern,signatureClassnamePattern1,">;\\)Ljava/lang/Object;")
        if (typeName != null){
            return extractRawTypeName(typeName)
        }
        return null
    }
    fun extractRawTypeName(typeName: String): String {
        val name = stripAllGenerics(typeName)
        val className = name.replace("[]","")
        return try {
            val ctclass = ClassPoolUtils.getNewClassPool().getCtClass(className)
            name
        } catch (e: NotFoundException) {
            name.replace(className,"java.lang.Object")
        }

    }
    fun extractRawTypeNames(typeName: String): Pair<Boolean,String> {
        val name = stripAllGenerics(typeName)
        val className = name.replace("[]","")
        return try {
            val ctclass = ClassPoolUtils.getNewClassPool().getCtClass(className)
            Pair(true,name)
        } catch (e: NotFoundException) {
            Pair(false,name.replace(className,"java.lang.Object"))
        }

    }
    private fun stripAllGenerics(typeDesc: String): String {
        val result = StringBuilder()
        var angleDepth = 0
        for (c in typeDesc) {
            when (c) {
                '<' -> angleDepth++
                '>' -> angleDepth--
                else -> if (angleDepth == 0) result.append(c)
            }
        }
        return result.toString()
    }
    private fun getType(type: String?, classnamePattern:Pattern, classnamePattern1:Pattern, replaceText:String): String? {
        if (type == null){
            return null
        }
        var className :String? = null
        val matcher = classnamePattern.matcher(type)
        if (matcher.find()) {
            val type2 = matcher.group()
            val matcher1 = classnamePattern1.matcher(type2)
            if (matcher1.find()) {
                val realType = matcher1.replaceFirst("")
                val realMatcher = classnamePattern.matcher(realType)
                val realTypeClass: String = if (realMatcher.find()) {
                    realMatcher.replaceFirst("")
                } else {
                    realType.replace(replaceText.toRegex(), "")
                }
                className = realTypeClass
            }
        }
        if (className != null){
            className = getSeeClassName(className)
            val fanMatcher = fanClassnamePattern.matcher(className)
            if (fanMatcher.find()) {
                className = fanMatcher.replaceAll("")
            }
        }
        return className
    }
    private fun getSeeClassName(className: String): String {
        return if (classnameArrayPattern.matcher(className).find()) {
            getArrayClazzName(className)
        } else {
            className.substring(1).replace("/",".").replace(";","")
        }
    }

    private val classnameArrayPattern = Pattern.compile("\\[")
    private val fanClassnamePattern = Pattern.compile("<.*?>$")
    private val fanClassnamePattern2 = Pattern.compile("<.*?>;$")
    private fun getArrayClazzName(classname: String): String {
        val subStr = "["
        var count = 0
        var index = 0
        while (classname.indexOf(subStr, index).also { index = it } != -1) {
            index += subStr.length
            count++
        }
        val realClassName = classnameArrayPattern.matcher(classname).replaceAll("")
        val matcher = fanClassnamePattern2.matcher(realClassName)
        val clazzName = if (matcher.find()) {
            matcher.replaceAll("")
        }else{
            realClassName
        }
        return getTypeInternal(clazzName)+"[]".repeat(count)
    }

    private fun getTypeInternal(classname: String): String {
        return when (classname) {
            "Z" -> "boolean"
            "C" -> "char"
            "B" -> "byte"
            "S" -> "short"
            "I" -> "int"
            "F" -> "float"
            "J" -> "long"
            "D" -> "double"
            else -> classname.substring(1).replace("/",".").replace(";","")
        }
    }
    private val AOPMethodPattern: Pattern = Pattern.compile("\\$\\$.{32}\\$\\\$AndroidAOP$")
    fun isAOPMethod(methodName: String):Boolean{
        val matcher: Matcher = AOPMethodPattern.matcher(methodName)
        return matcher.find()
    }

    fun getTargetMethodName(oldMethodName:String,className:String,descriptor:String):String{
        return "${oldMethodName.replace("-","$")}$$${(slashToDot(className)+descriptor).computeMD5()}${METHOD_SUFFIX}"
    }

    fun getTargetFieldName(oldMethodName:String,className:String,descriptor:String):String{
        return "${getTargetMethodName(oldMethodName, className, descriptor)}Field"
    }

    fun getRealMethodName(methodName:String):String{
        val matcher: Matcher = AOPMethodPattern.matcher(methodName)
        return if (matcher.find()) {
            matcher.replaceAll("")
        }else{
            methodName
        }
    }
}

fun printLog(text: String) {
    if (AndroidAopConfig.debug) {
        println(text)
    }
}

fun printError(text: String) {
    Utils.logger?.error(text)
}

fun File.checkExist(delete:Boolean = false){
    if (!parentFile.exists()){
        parentFile.mkdirs()
    }
    if (!exists()){
        createNewFile()
    }else if (delete){
        delete()
        createNewFile()
    }
}
val JAR_SIGNATURE_EXTENSIONS = setOf("SF", "RSA", "DSA", "EC")
fun String.isJarSignatureRelatedFiles(): Boolean {
    return startsWith("META-INF/") && substringAfterLast('.') in JAR_SIGNATURE_EXTENSIONS
}
fun String.computeMD5(): String {
    return try {
        val messageDigest = MessageDigest.getInstance("MD5")
        val digestBytes = messageDigest.digest(toByteArray())
        Utils.bytesToHex(digestBytes)
    } catch (var3: NoSuchAlgorithmException) {
        throw IllegalStateException(var3)
    }
}
fun String.instanceof(instanceofClassNameKey: String): Boolean {
    val className: String? = WovenInfoUtils.getClassString(this)
    val instanceofClassName: String? = WovenInfoUtils.getClassString(instanceofClassNameKey)
    if (className == null || instanceofClassName == null){
        return false
    }
    val subtypeOf = try {
        val pool = ClassPoolUtils.classPool
        val clazz = pool!!.get(className)
        val instanceofClazz = pool.get(instanceofClassName)
        val subtypeOf = clazz.subtypeOf(instanceofClazz)
//        clazz.detach()
//        instanceofClazz.detach()
        subtypeOf
    } catch (e: Exception) {
        false
    }
    return subtypeOf
}

fun Int.isHasMethodBody():Boolean{
    val access: Int = this
    val isAbstractMethod = access and Opcodes.ACC_ABSTRACT != 0
    val isNativeMethod = access and Opcodes.ACC_NATIVE != 0
    return !isAbstractMethod && !isNativeMethod
}
fun Int.isStaticMethod():Boolean{
    val access: Int = this
    return access and Opcodes.ACC_STATIC != 0
}

fun Int.isPrivate():Boolean{
    val access: Int = this
    return access and Opcodes.ACC_PRIVATE != 0
}

fun Int.addPublic(isAddPublic: Boolean):Int{
    return if (isAddPublic){
        if (Modifier.isPublic(this)){
            this
        }else{
            this and (Opcodes.ACC_PRIVATE or Opcodes.ACC_PROTECTED).inv() or Opcodes.ACC_PUBLIC
        }
    }else{
        this
    }
}

fun ByteArray.saveFile(outFile : File){
    val oldByte = outFile.readBytes()
    if (!oldByte.contentEquals(this)) {
        inputStream().use { inputStream->
            outFile.outputStream().use {
                inputStream.copyTo(it)
            }
        }
    }
}

fun File.saveEntry(inputStream: InputStream) {
    val oldBytes = readBytes()
    val newBytes = inputStream.readAllBytes()
    if (!oldBytes.contentEquals(newBytes)) {
        this.outputStream().use {
            it.write(newBytes)
        }
    }

}

fun AndroidAopConfig.Companion.inRules(className: String):Boolean{
    return className.inWeavingRules(includes,excludes)
}

fun ReplaceMethodInfo.inRules(className: String):Boolean{
    return className.inWeavingRules( weavingRules?.includeWeaving,weavingRules?.excludeWeaving)
}

fun String.inWeavingRules(includeWeaving: List<String>?,excludeWeaving: List<String>?):Boolean{
    return Utils.isIncludeFilterMatched(this, includeWeaving) && !Utils.isExcludeFilterMatched(
        this,
        excludeWeaving
    )
}

fun ExtendsWeavingRules.inRules(className: String):Boolean{
    return className.inWeavingRules(weavingRules?.includeWeaving,weavingRules?.excludeWeaving)
}

fun AopMatchCut.inRules(className: String):Boolean{
    return className.inWeavingRules(weavingRules?.includeWeaving,weavingRules?.excludeWeaving)
}

fun AndroidAopConfig.Companion.inExcludePackingRules(entryName: String):Boolean{
    return entryName.isEmpty() ||
            entryName.startsWith("META-INF/") ||
            entryName == "module-info.class" ||
            excludePackagings.contains(entryName)
}

fun File.getFileClassname(directory :File):String {
    return getRelativePath(directory).toClassPath()
}

fun File.getRelativePath(directory :File):String {
    return directory.toURI().relativize(toURI()).path
}

fun String.adapterOSPath():String {
    return replace('/',File.separatorChar)
}

fun String.toClassPath():String {
    return replace(File.separatorChar, '/')
}

fun Throwable.printDetail(){
    if (AndroidAopConfig.debug) {
        printStackTrace()
    }
}

fun CtMethod.getReturnTypeName():String{
    val returnTypeName = returnType.name
    return Utils.extractRawTypeName(returnTypeName)
}
