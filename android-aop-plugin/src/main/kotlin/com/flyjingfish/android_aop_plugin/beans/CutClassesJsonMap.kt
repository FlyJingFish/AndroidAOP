package com.flyjingfish.android_aop_plugin.beans

import java.util.concurrent.ConcurrentHashMap

data class CutClassesJsonMap(val className:String, val method: ConcurrentHashMap<String, CutMethodJson> = ConcurrentHashMap())