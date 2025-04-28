package com.flyjingfish.android_aop_plugin.beans

import java.util.concurrent.ConcurrentHashMap

data class ModifyExtendsClassJson(
    val type :String,
    val targetClassName: String,
    val extendsClassName: String,
    var modifiedCount: Int = 0,
    var modified: MutableSet<String> = ConcurrentHashMap.newKeySet()
)