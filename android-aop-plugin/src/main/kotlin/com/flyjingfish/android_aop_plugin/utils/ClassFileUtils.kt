package com.flyjingfish.android_aop_plugin.utils

import com.flyjingfish.android_aop_plugin.beans.InvokeClass
import com.flyjingfish.android_aop_plugin.scanner_visitor.WovenIntoCode
import com.flyjingfish.android_aop_plugin.utils.Utils.CONVERSIONS_CLASS
import javassist.CannotCompileException
import javassist.CtNewMethod
import javassist.NotFoundException
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes.ACC_PUBLIC
import org.objectweb.asm.Opcodes.ALOAD
import org.objectweb.asm.Opcodes.DUP
import org.objectweb.asm.Opcodes.INVOKESPECIAL
import org.objectweb.asm.Opcodes.IRETURN
import org.objectweb.asm.Opcodes.NEW
import org.objectweb.asm.Opcodes.RETURN
import org.objectweb.asm.Opcodes.V1_8
import java.io.ByteArrayInputStream
import java.io.File


object ClassFileUtils {
    var reflectInvokeMethod = false
    var reflectInvokeMethodStatic = false
    var debugMode = false
    lateinit var outputDir:File
    var outputCacheDir:File ?= null
    private val invokeClasses = mutableMapOf<String,MutableList<InvokeClass>?>()
    private const val INVOKE_METHOD = "invoke"
    private const val INVOKE_DESCRIPTOR = "(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;"
    private const val INVOKE_CLASS = "com.flyjingfish.android_aop_annotation.utils.InvokeMethod"
    private const val INVOKE_CLASSES = "com.flyjingfish.android_aop_annotation.utils.InvokeMethods"
    fun clear(){
        invokeClasses.clear()
    }
    fun wovenInfoInvokeClass(newClasses: MutableList<ByteArray>) :MutableList<String> = runBlocking{
        val cacheFiles = mutableListOf<String>()
        if (reflectInvokeMethod && !reflectInvokeMethodStatic){
            return@runBlocking cacheFiles
        }

        if (reflectInvokeMethod && reflectInvokeMethodStatic){
            for (invokeClasses in invokeClasses) {
                val value = invokeClasses.value ?: continue
                val path = outputDir.absolutePath + File.separatorChar +Utils.dotToSlash(invokeClasses.key).adapterOSPath()+".class"
                val outFile = File(path)
                if (outputCacheDir != null && outFile.exists()){
                    continue
                }
                val cp = ClassPoolUtils.getNewClassPool()
                outputCacheDir?.let {
                    cp.appendClassPath(it.absolutePath)
                }
                newClasses.forEach {
                    cp.makeClass(ByteArrayInputStream(it))
                }
                cp.importPackage(CONVERSIONS_CLASS)
                val ctClass = cp.get(invokeClasses.key)

                for (invokeClass in value) {
                    val className = invokeClass.packageName
                    val invokeBody = invokeClass.invokeBody

                    try {
                        val methodBody =
                            "public static final Object ${className.replace(".","_")}(Object target,Object[] vars)  " +
                                    invokeBody
                        val newMethod = CtNewMethod.make(methodBody, ctClass)
                        ctClass.addMethod(newMethod);
                    } catch (e: NotFoundException) {
                        throw RuntimeException(e)
                    } catch (e: CannotCompileException) {
                        printLog("invokeBody=$invokeBody")
                        throw RuntimeException(e)
                    }
                }
                val classByteData = ctClass.toBytecode()
//            //把类数据写入到class文件,这样你就可以把这个类文件打包供其他的人使用
                outFile.checkExist()
                classByteData.saveFile(outFile)
                cacheFiles.add(path)
//            ctClass.detach()
            }
        }else{
            val invokeJobs = mutableListOf<Deferred<Unit>>()
            for (invokeClasses in invokeClasses) {
                val value = invokeClasses.value ?: continue
                for (invokeClass in value) {
                    val className = invokeClass.packageName
                    val invokeBody = invokeClass.invokeBody
                    val path = outputDir.absolutePath + File.separatorChar +Utils.dotToSlash(className).adapterOSPath()+".class"
                    val outFile = File(path)
                    if (outputCacheDir != null && outFile.exists()){
                        continue
                    }
                    val job = async(Dispatchers.IO) {

//            println("invokeClass.methodName="+invokeClass.methodName)
                        val cp = ClassPoolUtils.getNewClassPool()
                        outputCacheDir?.let {
                            cp.appendClassPath(it.absolutePath)
                        }
                        newClasses.forEach {
                            cp.makeClass(ByteArrayInputStream(it))
                        }
                        cp.importPackage(CONVERSIONS_CLASS)
                        val ctClass = cp.get(className)
                        try {
                            val ctMethod =
                                WovenIntoCode.getCtMethod(ctClass, INVOKE_METHOD, INVOKE_DESCRIPTOR)
                            ctMethod?.setBody(invokeBody)
                        } catch (e: NotFoundException) {
                            throw RuntimeException(e)
                        } catch (e: CannotCompileException) {
                            printLog("invokeBody=$invokeBody")
                            throw RuntimeException(e)
                        }
                        val classByteData = ctClass.toBytecode()
//            //把类数据写入到class文件,这样你就可以把这个类文件打包供其他的人使用
                        outFile.checkExist()
                        cacheFiles.add(path)
                        classByteData.saveFile(outFile)
                    }
                    invokeJobs.add(job)
                }
//            ctClass.detach()
            }
            invokeJobs.awaitAll()
        }
        cacheFiles
    }

    fun createInvokeClass(staticClassName:String, classMethodName:String, invokeBody:String, methodName:String) {
        if (reflectInvokeMethod && !reflectInvokeMethodStatic){
            return
        }
        var list = invokeClasses[staticClassName]
        if (list == null){
            list = mutableListOf()
            invokeClasses[staticClassName] = list
        }
        list.add(InvokeClass(classMethodName,invokeBody,methodName))
        val className = if (reflectInvokeMethod && reflectInvokeMethodStatic){
            staticClassName
        }else{
            classMethodName
        }
        if (File(outputDir.absolutePath + File.separatorChar +Utils.dotToSlash(className).adapterOSPath()+".class").exists()){
            return
        }
        val cacheOutFir = outputCacheDir
        val outFile = if (cacheOutFir != null){
            File(cacheOutFir.absolutePath + File.separatorChar +Utils.dotToSlash(className).adapterOSPath()+".class")
        }else{
            File(outputDir.absolutePath + File.separatorChar +Utils.dotToSlash(className).adapterOSPath()+".class")
        }
        if (outFile.exists()){
            return
        }

        outFile.checkExist()
//        val className = "$packageName.Invoke${UUID.randomUUID()}"
        //新建一个类生成器，COMPUTE_FRAMES，COMPUTE_MAXS这2个参数能够让asm自动更新操作数栈
        val cw = ClassWriter(ClassWriter.COMPUTE_FRAMES or ClassWriter.COMPUTE_MAXS)
        //生成一个public的类，类路径是com.study.Human
        if (reflectInvokeMethod && reflectInvokeMethodStatic){
            cw.visit(
                V1_8,
                ACC_PUBLIC, Utils.dotToSlash(className), null, "java/lang/Object", arrayOf(Utils.dotToSlash(
                    INVOKE_CLASSES))
            )
        }else{
            cw.visit(
                V1_8,
                ACC_PUBLIC, Utils.dotToSlash(className), null, "java/lang/Object", arrayOf(Utils.dotToSlash(INVOKE_CLASS))
            )
        }

        var mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null)
        mv.visitVarInsn(ALOAD, 0)
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false)
        mv.visitInsn(RETURN)
        mv.visitMaxs(0, 0) //更新操作数栈
        mv.visitEnd() //一定要有visitEnd


        mv = cw.visitMethod(ACC_PUBLIC, INVOKE_METHOD, INVOKE_DESCRIPTOR, null, null)
        mv.visitCode();

        mv.visitTypeInsn(NEW, "java/lang/Object")
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false)

        mv.visitInsn(IRETURN)
        mv.visitMaxs(0, 0)
        mv.visitEnd()

        cw.toByteArray().saveFile(outFile)
    }

    fun deleteInvokeClass(className:String) {
        if (reflectInvokeMethod && !reflectInvokeMethodStatic){
            return
        }
        for (invokeClass in invokeClasses) {
            val value = invokeClass.value ?: continue
            val iterator = value.iterator()
            while (iterator.hasNext()){
                val item = iterator.next()
                if (item.packageName == className){
                    iterator.remove()
                    break
                }
            }
        }

        outputCacheDir?.let {
            val file = File(it.absolutePath +File.separatorChar +Utils.dotToSlash(className).adapterOSPath()+".class")
            if (file.exists()){
                file.delete()
            }
        }
        val file = File(outputDir.absolutePath + File.separatorChar +Utils.dotToSlash(className).adapterOSPath()+".class")
        if (file.exists()){
            file.delete()
        }
    }
}