package com.flyjingfish.android_aop_ksp

import com.google.devtools.ksp.getAllSuperTypes
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration

fun KSClassDeclaration.isSubtype(superType :String,logger: KSPLogger):Boolean{
    val hh = this
    getAllSuperTypes().toList().forEach {
        val className = "${it.declaration.packageName.asString()}.${it.declaration}"
        if (className == superType){
            return true
        }
    }
    return false
}