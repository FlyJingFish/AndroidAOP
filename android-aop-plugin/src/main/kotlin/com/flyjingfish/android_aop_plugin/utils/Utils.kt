package com.flyjingfish.android_aop_plugin.utils

import com.flyjingfish.android_aop_plugin.beans.MatchMethodInfo
import com.flyjingfish.android_aop_plugin.config.AndroidAopConfig
import org.gradle.api.Project
import org.objectweb.asm.Opcodes
import org.objectweb.asm.commons.Method
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.regex.Matcher
import java.util.regex.Pattern
import java.util.zip.ZipInputStream


object Utils {
    const val annotationPackage = "com.flyjingfish.android_aop_annotation."
    const val corePackage = "com.flyjingfish.android_aop_core."
    const val JoinAnnoCutUtils = "${annotationPackage}utils.JoinAnnoCutUtils"
    const val _CLASS = ".class"
    const val AOP_CONFIG_END_NAME = "\$\$AndroidAopClass.class"
    const val KEEP_CLASS = "com.flyjingfish.android_aop_annotation.aop_anno.AopKeep"
    const val JOIN_POINT_CLASS = "com.flyjingfish.android_aop_annotation.AndroidAopJoinPoint"
    const val CONVERSIONS_CLASS = "com.flyjingfish.android_aop_annotation.Conversions"
    private val JAR_SIGNATURE_EXTENSIONS = setOf("SF", "RSA", "DSA", "EC")
    fun isJarSignatureRelatedFiles(name: String): Boolean {
        return name.startsWith("META-INF/") && name.substringAfterLast('.') in JAR_SIGNATURE_EXTENSIONS
    }
    fun dotToSlash(str: String): String {
        return str.replace(".", "/")
    }

    fun slashToDot(str: String): String {
        return str.replace("/", ".")
    }

    fun isExcludeFilterMatched(str: String?, filters: List<String>?): Boolean {
        return isFilterMatched(str, filters, FilterPolicy.EXCLUDE)
    }

    fun isIncludeFilterMatched(str: String?, filters: List<String>?): Boolean {
        return isFilterMatched(str, filters, FilterPolicy.INCLUDE)
    }

    fun slashToDotClassName(str: String): String {
        return str.replace("/", ".").replace("$", ".")
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

    private fun isContained(str: String?, filter: String): Boolean {
        if (str == null) {
            return false
        }

        if (str.contains(filter)) {
            return true
        } else {
            if (filter.contains("/")) {
                return str.contains(filter.replace("/", File.separator))
            } else if (filter.contains("\\")) {
                return str.contains(filter.replace("\\", File.separator))
            }
        }

        return false
    }

    enum class FilterPolicy {
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

    fun isInstanceof(classNameKey: String, instanceofClassNameKey: String): Boolean {
        val className: String? = WovenInfoUtils.getClassString(classNameKey)
        val instanceofClassName: String? = WovenInfoUtils.getClassString(instanceofClassNameKey)
        if (className == null || instanceofClassName == null){
            return false
        }
        val subtypeOf = try {
            val pool = ClassPoolUtils.classPool
            val clazz = pool!!.get(className)
            val instanceofClazz = pool.get(instanceofClassName)
            clazz.subtypeOf(instanceofClazz)
        } catch (e: Exception) {
            false
        }
        return subtypeOf
    }

    fun computeMD5(string: String): String? {
        return try {
            val messageDigest = MessageDigest.getInstance("MD5")
            val digestBytes = messageDigest.digest(string.toByteArray())
            bytesToHex(digestBytes)
        } catch (var3: NoSuchAlgorithmException) {
            throw IllegalStateException(var3)
        }
    }

    private fun bytesToHex(bytes: ByteArray): String? {
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

    fun isHasMethodBody(access: Int):Boolean{
        val isAbstractMethod = access and Opcodes.ACC_ABSTRACT != 0
        val isNativeMethod = access and Opcodes.ACC_NATIVE != 0
        return !isAbstractMethod && !isNativeMethod
    }
    fun isStaticMethod(access: Int):Boolean{
        return access and Opcodes.ACC_STATIC != 0
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
        return project.buildDir.absolutePath+"/tmp/android-aop/${variantName}/cacheInfo.json"
    }
    fun aopCompileTempDir(project:Project, variantName:String):String{
        return project.buildDir.absolutePath + "/tmp/android-aop/${variantName}/tempCompileClass/"
    }
    fun aopCompileTempOtherDir(project:Project, variantName:String):String{
        return project.buildDir.absolutePath + "/tmp/android-aop/${variantName}/tempCompileOtherClass/"
    }
    fun aopCompileTempOtherJson(project:Project, variantName:String):String{
        return project.buildDir.absolutePath + "/tmp/android-aop/${variantName}/tempCompileOtherClassJson/needDelClassInfo.json"
    }
    fun aopTransformTempDir(project:Project, variantName:String):String{
        return project.buildDir.absolutePath+"/tmp/android-aop/tempInvokeClass/${variantName}/"
    }
    fun aopTransformCollectTempDir(project:Project, variantName:String):String{
        return project.buildDir.absolutePath+"/tmp/android-aop/tempCollectClass/${variantName}/"
    }
    fun configJsonFile(project:Project):String{
        return project.buildDir.absolutePath+"/tmp/android-aop/config/androidAopConfig.json"
    }
}

fun printLog(text: String) {
    if (AndroidAopConfig.debug) {
        println(text)
    }
}