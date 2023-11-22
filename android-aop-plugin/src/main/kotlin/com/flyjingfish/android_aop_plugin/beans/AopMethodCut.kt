package com.flyjingfish.android_aop_plugin.beans

class AopMethodCut(val anno:String,val cutClassName:String) {
    override fun toString(): String {
        return "AopMethodCut(anno='$anno', cutClassName='$cutClassName')"
    }
}