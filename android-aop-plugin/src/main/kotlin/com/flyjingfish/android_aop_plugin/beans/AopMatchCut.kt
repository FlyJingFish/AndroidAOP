package com.flyjingfish.android_aop_plugin.beans

import java.util.regex.Pattern

data class AopMatchCut(val baseClassName:String, val methodNames:Array<String>,val cutClassName:String,val matchType:String = "EXTENDS",val excludeClass:Array<String>?, val overrideMethod: Boolean = false) {
    enum class MatchType{
        EXTENDS,SELF,DIRECT_EXTENDS,LEAF_EXTENDS
    }
    fun isMatchAllMethod():Boolean{
        return methodNames.size == 1 && methodNames[0] == "*"
    }
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AopMatchCut

        if (baseClassName != other.baseClassName) return false
        if (!methodNames.contentEquals(other.methodNames)) return false
        if (cutClassName != other.cutClassName) return false
        if (matchType != other.matchType) return false
        if (excludeClass != null) {
            if (other.excludeClass == null) return false
            if (!excludeClass.contentEquals(other.excludeClass)) return false
        } else if (other.excludeClass != null) return false

        return true
    }
    private val AllClassnamePattern = Pattern.compile(".\\*$")
    fun isMatchPackageName():Boolean{
        return matchType == MatchType.SELF.name && isPackageName()
    }

    fun isPackageName():Boolean{
        val fanMatcher = AllClassnamePattern.matcher(baseClassName)
        return fanMatcher.find()
    }

    fun isMatchPackageNameFor(className:String):Boolean{
        if (!isMatchPackageName()){
            return false
        }
        val matchPackageName = getMatchPackageName()
        if ((matchPackageName.replace(".","")).isEmpty()){
            return false
        }
        val filter = if (matchPackageName.endsWith(".")){
            matchPackageName
        }else {
            "$matchPackageName."
        }
        return className.startsWith(filter)
    }

    private fun getMatchPackageName():String{
        val fanMatcher = AllClassnamePattern.matcher(baseClassName)
        return if (fanMatcher.find()){
            fanMatcher.replaceAll("")
        }else{
            baseClassName
        }
    }

    override fun hashCode(): Int {
        var result = baseClassName.hashCode()
        result = 31 * result + methodNames.contentHashCode()
        result = 31 * result + cutClassName.hashCode()
        result = 31 * result + matchType.hashCode()
        result = 31 * result + (excludeClass?.contentHashCode() ?: 0)
        return result
    }
}