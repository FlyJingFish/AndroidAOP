package com.flyjingfish.android_aop_plugin.beans

data class CutClassesJson(val className:String,var methodCount :Int = 0, val method:MutableList<CutMethodJson2> = mutableListOf())