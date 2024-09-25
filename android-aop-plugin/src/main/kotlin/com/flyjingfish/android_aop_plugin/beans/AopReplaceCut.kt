package com.flyjingfish.android_aop_plugin.beans

data class AopReplaceCut(val targetClassName:String, val invokeClassName:String,val matchType:String = "EXTENDS",val excludeClass:Array<String>?) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AopReplaceCut

        if (targetClassName != other.targetClassName) return false
        if (invokeClassName != other.invokeClassName) return false
        if (matchType != other.matchType) return false
        if (excludeClass != null) {
            if (other.excludeClass == null) return false
            if (!excludeClass.contentEquals(other.excludeClass)) return false
        } else if (other.excludeClass != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = targetClassName.hashCode()
        result = 31 * result + invokeClassName.hashCode()
        result = 31 * result + matchType.hashCode()
        result = 31 * result + (excludeClass?.contentHashCode() ?: 0)
        return result
    }
}