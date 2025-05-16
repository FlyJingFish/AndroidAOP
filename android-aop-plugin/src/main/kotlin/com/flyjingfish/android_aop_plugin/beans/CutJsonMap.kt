package com.flyjingfish.android_aop_plugin.beans

import java.util.concurrent.ConcurrentHashMap

data class CutJsonMap(val type:String, val className:String, val cutClasses:ConcurrentHashMap<String,CutClassesJsonMap> = ConcurrentHashMap())