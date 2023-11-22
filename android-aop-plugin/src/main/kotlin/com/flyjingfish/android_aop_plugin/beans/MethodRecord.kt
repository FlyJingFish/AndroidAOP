package com.flyjingfish.android_aop_plugin.beans

class MethodRecord(val methodName :String,val descriptor:String,val cutClassName:String?) {
    override fun toString(): String {
        return "MethodRecord(methodName='$methodName', descriptor='$descriptor', cutClassName='$cutClassName')"
    }
}