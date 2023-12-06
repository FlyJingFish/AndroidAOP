package com.flyjingfish.android_aop_plugin.utils

import com.flyjingfish.android_aop_plugin.beans.MatchMethodInfo
import com.flyjingfish.android_aop_plugin.config.AndroidAopConfig
import javassist.NotFoundException
import java.io.File
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.regex.Matcher
import java.util.regex.Pattern


object Utils {
    const val annotationPackage = "com.flyjingfish.android_aop_annotation."
    const val corePackage = "com.flyjingfish.android_aop_core."
    const val MethodAnnoUtils = "${annotationPackage}utils.MethodAnnoUtils"
    const val _CLASS = ".class"
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
            if (names.size == 2) {
                returnType = names[0]
                name = names[1]
            } else {
                name = nameStr
            }
            params = paramStr
        } else if (methodName.contains(" ")) {
            val names = methodName.split(" ").toTypedArray()
            if (names.size == 2) {
                name = names[1]
                returnType = names[0]
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
        return matchMethodInfo
    }

    @Throws(NotFoundException::class)
    fun isInstanceof(classNameKey: String, instanceofClassNameKey: String): Boolean {
        val className: String? = WovenInfoUtils.getClassString(classNameKey)
        val instanceofClassName: String? = WovenInfoUtils.getClassString(instanceofClassNameKey)
        if (className == null || instanceofClassName == null){
            return false
        }
        val pool = ClassPoolUtils.classPool
        val clazz = pool!!.get(className)
        val instanceofClazz = pool.get(instanceofClassName)
        return clazz.subtypeOf(instanceofClazz)
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
}

fun printLog(text: String) {
    if (AndroidAopConfig.debug) {
        println(text)
    }
}