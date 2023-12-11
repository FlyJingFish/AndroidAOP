package com.flyjingfish.android_aop_plugin.beans

data class CutJson(val type:String, val className:String, var cutCount :Int = 0,val cutClasses:MutableList<CutClassesJson> = mutableListOf())