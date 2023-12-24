package com.flyjingfish.android_aop_plugin.beans

data class MethodRecord(val methodName :String,val descriptor:String,val cutClassName:String?, val cutInfo: MutableMap<String, CutInfo> = mutableMapOf())