package com.flyjingfish.android_aop_plugin.beans

class AopReplaceCut(val targetClassName:String, val invokeClassName:String,val matchType:String = "EXTENDS",val excludeClass:Array<String>?) {
}