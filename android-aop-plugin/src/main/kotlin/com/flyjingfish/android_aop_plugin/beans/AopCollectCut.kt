package com.flyjingfish.android_aop_plugin.beans

data class AopCollectCut(
    val collectClassName: String,
    val invokeClassName: String,
    val invokeMethod: String,
    val isClazz:Boolean
){
    fun getKey():String{
        return invokeClassName + invokeMethod + collectClassName+isClazz
    }
}
