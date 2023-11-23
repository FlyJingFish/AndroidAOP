package com.flyjingfish.android_aop_plugin.beans

class WovenInfo {
    var aopMethodCuts: HashMap<String, AopMethodCut>? = null
    var aopMatchCuts: HashMap<String, AopMatchCut>? = null
    var classPaths: ArrayList<String>? = null
    override fun toString(): String {
        return "WovenInfo(aopMethodCuts=$aopMethodCuts, aopMatchCuts=$aopMatchCuts, classPaths=$classPaths)"
    }


}