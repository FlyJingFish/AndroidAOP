package com.flyjingfish.android_aop_plugin.beans

data class AopCollectCut(
    val collectClassName: String,
    val invokeClassName: String,
    val invokeMethod: String,
    val isClazz:Boolean,
    val regex : String,
    val collectType : String
){
    enum class CollectType{
        EXTENDS,DIRECT_EXTENDS,LEAF_EXTENDS
    }
    fun getKey():String{
        return invokeClassName + invokeMethod + collectClassName+isClazz+regex+collectType
    }
}
