package com.flyjingfish.android_aop_plugin.beans

data class CutCollectMethodJsonCache(val collectType:String, val regex:String, val classes:MutableList<String> = mutableListOf())