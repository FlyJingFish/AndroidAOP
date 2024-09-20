package com.flyjingfish.android_aop_plugin.utils

import javassist.ClassPool
import javassist.NotFoundException
import javassist.util.JarUtils
import org.gradle.api.Project

object ClassPoolUtils {
    var classPool : ClassPool ?= null
        get() {
            if (field == null){
//                field = initClassPool(null,null)
                field = initClassPool()
            }
            return field
        }

//    fun initClassPool(project: Project?,variantName :String?):ClassPool {
//        if (project != null && variantName != null){
//            val tempJars = File(Utils.aopProjectJarTempDir(project,variantName))
//            if (tempJars.exists()){
//                tempJars.deleteRecursively()
//            }
//        }
//        val classPool = ClassPool(null)
//        classPool.appendSystemPath()
//        for (classPath in WovenInfoUtils.getClassPaths()) {
//            try {
//                val isProjectJar = Utils.isProjectJar(project,classPath)
//                if (isProjectJar){
//                    val newPath = Utils.getNewProjectJar(project!!,variantName!!,classPath)
//                    if (newPath != null){
//                        classPool.appendClassPath(newPath)
//                    }
//                }else{
//                    classPool.appendClassPath(classPath)
//                }
//            } catch (_: NotFoundException) {
//            }
//        }
//        this.classPool = classPool
//        return classPool
//    }


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

    fun release(project: Project){
        val projectPath = project.rootProject.buildDir.absolutePath.replace("/build".adapterOSPath(),"")
        JarUtils.INSTANCE.setRootProjectPath(projectPath)
        classPool = null
    }
}