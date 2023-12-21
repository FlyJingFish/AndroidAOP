package com.flyjingfish.android_aop_plugin.beans

data class AopMatchCut(val baseClassName:String, val methodNames:Array<String>,val cutClassName:String,val matchType:String = "EXTENDS",val excludeClass:Array<String>?) {
    enum class MatchType{
        EXTENDS,SELF,DIRECT_EXTENDS,LEAF_EXTENDS
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

    override fun hashCode(): Int {
        var result = baseClassName.hashCode()
        result = 31 * result + methodNames.contentHashCode()
        result = 31 * result + cutClassName.hashCode()
        result = 31 * result + matchType.hashCode()
        result = 31 * result + (excludeClass?.contentHashCode() ?: 0)
        return result
    }
}