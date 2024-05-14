package com.flyjingfish.android_aop_plugin.beans

data class CutCollectJson(val type:String,val collectClass:String,val collectMethod:MutableList<CutCollectMethodJson> = mutableListOf())