package com.flyjingfish.android_aop_plugin.beans

data class CutJson(val type:String, val className:String, val cutClasses:MutableList<CutClassesJson> = mutableListOf())