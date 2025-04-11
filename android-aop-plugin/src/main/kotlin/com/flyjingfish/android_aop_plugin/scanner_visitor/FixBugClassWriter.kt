package com.flyjingfish.android_aop_plugin.scanner_visitor

import com.flyjingfish.android_aop_plugin.utils.ClassPoolUtils
import com.flyjingfish.android_aop_plugin.utils.Utils
import com.flyjingfish.android_aop_plugin.utils.WovenInfoUtils
import com.flyjingfish.android_aop_plugin.utils.printLog
import javassist.ClassPool
import javassist.CtClass
import javassist.NotFoundException
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter

class FixBugClassWriter(classReader: ClassReader,flags: Int) : ClassWriter(classReader,flags) {

    override fun getCommonSuperClass(type1: String, type2: String): String {
        return try {
            super.getCommonSuperClass(type1, type2)
        } catch (e: TypeNotPresentException) {
            try {
                val pool = ClassPoolUtils.getNewClassPool()
                val ct1 = pool.getCtClass(Utils.slashToDot(type1))
                val ct2 = pool.getCtClass(Utils.slashToDot(type2))

                val common = findCommonSuperClass(ct1, ct2,pool)
                val realType = Utils.dotToSlash(common.name)
//                printLog("getCommonSuperClass: type1=$type1, type2=$type2 => $realType")
                realType
            } catch (e: NotFoundException) {
                e.printStackTrace()
                "java/lang/Object"
            }
        }
    }

    private fun findCommonSuperClass(c1: CtClass, c2: CtClass,classPool: ClassPool): CtClass {
        if (c1 == c2) return c1
        if (c1.subtypeOf(c2)) return c2
        if (c2.subtypeOf(c1)) return c1

        var super1 = c1
        val visited = mutableSetOf<CtClass>()

        while (true) {
            visited.add(super1)
            try {
                super1 = super1.superclass ?: break
            } catch (e: NotFoundException) {
                break
            }
        }

        var super2 = c2
        while (true) {
            if (visited.contains(super2)) return super2
            try {
                super2 = super2.superclass ?: break
            } catch (e: NotFoundException) {
                break
            }
        }

        return classPool.get("java.lang.Object")
    }
}