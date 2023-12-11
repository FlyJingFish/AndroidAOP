package com.flyjingfish.android_aop_plugin.beans

data class CutClassesJsonMap(val className:String, val method:MutableMap<String,CutMethodJson> = mutableMapOf())