package com.flyjingfish.android_aop_plugin.beans

data class CutClassesJson(val className:String, val method:MutableList<CutMethodJson> = mutableListOf())