package com.flyjingfish.android_aop_plugin.utils

import javassist.ClassPool
import javassist.NotFoundException
import javassist.util.JarUtils
import org.gradle.api.Project
import java.util.concurrent.ConcurrentHashMap

object ClassPoolUtils {
    private val hasClassCache = ConcurrentHashMap<String, Pair<Boolean,String>>()
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

    fun release(project: RuntimeProject){
        val projectPath = project.rootProjectBuildDir.absolutePath.replace("/build".adapterOSPath(),"")
        JarUtils.INSTANCE.setRootProjectPath(projectPath)
        classPool = null
    }

    fun clear(){
        hasClassCache.clear()
    }

    fun extractRawTypeName(cp: ClassPool, typeName: String?): String? {
        return extractRawTypeNames(cp,typeName)?.second
    }
    fun extractRawTypeNames(cp: ClassPool,typeName: String?): Pair<Boolean,String>? {
        if (typeName == null) return null
        val name = stripAllGenerics(typeName)
        val className = name.replace("[]","")
        val cache = hasClassCache[className]
        if (cache != null){
            return Pair(cache.first,if (cache.first){
                name
            }else{
                name.replace(className,"java.lang.Object")
            })
        }
        var cachePair : Pair<Boolean,String>
        val useClassNamePair = try {
            val hasClass = hasClass(className)
            if (!hasClass){
                val ctclass = cp.getCtClass(className)
            }
            cachePair = Pair(true,className)
            Pair(true,name)
        } catch (e: NotFoundException) {
            cachePair = Pair(false,"java.lang.Object")
            Pair(false,name.replace(className,"java.lang.Object"))
        }
        hasClassCache[className] = cachePair
        return useClassNamePair
    }

    private fun hasClass(className:String):Boolean{
        return if (className == "int"
            || className == "long"
            || className == "short"
            || className == "byte"
            || className == "boolean"
            || className == "char"
            || className == "float"
            || className == "double"
            || className == "void"
            || className.startsWith("java.lang.")){
            true
        }else{
            WovenInfoUtils.getClassString(Utils.slashToDotClassName(className)) != null
        }
    }

    private fun stripAllGenerics(typeDesc: String): String {
        val result = StringBuilder()
        var angleDepth = 0
        for (c in typeDesc) {
            when (c) {
                '<' -> angleDepth++
                '>' -> angleDepth--
                else -> if (angleDepth == 0) result.append(c)
            }
        }
        return result.toString()
    }
}