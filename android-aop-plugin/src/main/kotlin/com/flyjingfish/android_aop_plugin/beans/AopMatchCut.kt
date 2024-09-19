package com.flyjingfish.android_aop_plugin.beans

import java.util.regex.Pattern

data class AopMatchCut(val baseClassName:String, val methodNames:Array<String>,val cutClassName:String,val matchType:String = "EXTENDS",val excludeClass:Array<String>?, val overrideMethod: Boolean = false) {
    enum class MatchType{
        EXTENDS,SELF,DIRECT_EXTENDS,LEAF_EXTENDS
    }
    fun isMatchAllMethod():Boolean{
        return methodNames.size == 1 && methodNames[0] == "*"
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

}