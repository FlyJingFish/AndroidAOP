package com.flyjingfish.android_aop_plugin.beans

data class CutCollectMethodJson(val method:String,val classCount:Int,val classes:MutableList<String> = mutableListOf())