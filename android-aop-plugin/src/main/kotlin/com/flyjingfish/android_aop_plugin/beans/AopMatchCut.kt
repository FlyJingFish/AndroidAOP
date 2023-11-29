package com.flyjingfish.android_aop_plugin.beans

data class AopMatchCut(val baseClassName:String, val methodNames:Array<String>,val cutClassName:String,val matchType:String = "EXTENDS") {
    enum class MatchType{
        EXTENDS,SELF
    }
}