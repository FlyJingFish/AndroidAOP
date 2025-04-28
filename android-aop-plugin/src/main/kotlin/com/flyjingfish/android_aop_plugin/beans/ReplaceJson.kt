package com.flyjingfish.android_aop_plugin.beans

import java.util.concurrent.ConcurrentHashMap


data class ReplaceJson(
    val oldClassName:String,
    val oldMethod:String,
    val methodLocationMap:MutableSet<String> = ConcurrentHashMap.newKeySet()
)