package com.flyjingfish.android_aop_plugin.utils

import javassist.ClassPool
import javassist.NotFoundException

object ClassPoolUtils {
    var classPool = ClassPool(null)

    fun initClassPool(){
        classPool.appendSystemPath()
        for (classPath in WovenInfoUtils.classPaths){
            try {
                classPool.appendClassPath(classPath)
            } catch (e: NotFoundException) {
                throw RuntimeException(e)
            }
        }
    }
}