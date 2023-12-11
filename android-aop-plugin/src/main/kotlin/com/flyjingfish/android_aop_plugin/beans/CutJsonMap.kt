package com.flyjingfish.android_aop_plugin.beans

data class CutJsonMap(val type:String, val className:String, val cutClasses:MutableMap<String,CutClassesJson> = mutableMapOf())