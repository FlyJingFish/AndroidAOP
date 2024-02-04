package com.flyjingfish.android_aop_plugin.beans

class MatchMethodInfo {
    lateinit var name: String
    var returnType: String? = null
    var paramTypes: String? = null
    override fun toString(): String {
        return "MatchMethodInfo(name='$name', returnType=$returnType, paramTypes=$paramTypes)"
    }
    fun checkAvailable():Boolean{
        return name.isNotEmpty() && !returnType.isNullOrEmpty() && !paramTypes.isNullOrEmpty()
    }
}