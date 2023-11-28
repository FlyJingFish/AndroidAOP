package com.flyjingfish.android_aop_plugin.utils

import com.flyjingfish.android_aop_plugin.config.AndroidAopConfig
import java.io.File

object Utils {
    const val MethodAnnoUtils = "com.flyjingfish.android_aop_core.utils.MethodAnnoUtils"
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
}

fun printLog(text:String){
    if (AndroidAopConfig.debug){
        println(text)
    }
}