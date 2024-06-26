package com.flyjingfish.android_aop_plugin.beans

data class AopCollectClass(
    val collectClassName: String,
    val invokeClassName: String,
    val invokeMethod: String,
    val collectExtendsClassName: String,
    val isClazz:Boolean,
    val regex:String,
    val collectType : String
){
    fun getKey():String{
        return collectClassName+invokeClassName+invokeMethod+collectExtendsClassName+isClazz+regex+collectType
    }
}
