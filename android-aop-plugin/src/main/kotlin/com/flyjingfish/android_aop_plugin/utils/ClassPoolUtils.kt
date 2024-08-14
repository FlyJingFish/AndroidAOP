package com.flyjingfish.android_aop_plugin.utils

import javassist.ClassPool
import javassist.NotFoundException

object ClassPoolUtils {
    var classPool : ClassPool ?= null
        get() {
            if (field == null){
                field = initClassPool()
            }
            return field
        }

    fun initClassPool():ClassPool {
        val classPool = ClassPool(null)
        classPool.appendSystemPath()
        for (classPath in WovenInfoUtils.getClassPaths()) {
            try {
                classPool.appendClassPath(classPath)
            } catch (_: NotFoundException) {
            }
        }
        this.classPool = classPool
        return classPool
    }

    fun getNewClassPool(): ClassPool {
        val classPool = ClassPool(null)
        classPool.appendSystemPath()
        for (classPath in WovenInfoUtils.getClassPaths()) {
            try {
                classPool.appendClassPath(classPath)
            } catch (_: NotFoundException) {
            }
        }
        return classPool
    }

}