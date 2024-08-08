package com.flyjingfish.android_aop_plugin.utils

import com.flyjingfish.android_aop_plugin.beans.InvokeClass
import com.flyjingfish.android_aop_plugin.scanner_visitor.WovenIntoCode
import com.flyjingfish.android_aop_plugin.utils.Utils.CONVERSIONS_CLASS
import javassist.CannotCompileException
import javassist.NotFoundException
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
import java.io.FileOutputStream

object ClassFileUtils {
    var reflectInvokeMethod = false
    var debugMode = false
    lateinit var outputDir:File
    var outputCacheDir:File ?= null
    private val invokeClasses = mutableListOf<InvokeClass>()
    private const val INVOKE_METHOD = "invoke"
    private const val INVOKE_DESCRIPTOR = "(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;"
    private const val INVOKE_CLASS = "com.flyjingfish.android_aop_annotation.utils.InvokeMethod"
    fun clear(){
        invokeClasses.clear()
    }
    fun wovenInfoInvokeClass(newClasses: MutableList<ByteArray>) :MutableList<String> {
        val cacheFiles = mutableListOf<String>()
        if (reflectInvokeMethod){
            return cacheFiles
        }
        for (invokeClass in invokeClasses) {

            val className = invokeClass.packageName
            val invokeBody = invokeClass.invokeBody
            val path = outputDir.absolutePath + "/" +Utils.dotToSlash(className)+".class"
            val outFile = File(path)
            if (outputCacheDir != null && outFile.exists()){
                continue
            }
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
            classByteData.saveFile(outFile)
            cacheFiles.add(path)
        }
        return cacheFiles
    }

    fun createInvokeClass(className:String, invokeBody:String, methodName:String) {
        if (reflectInvokeMethod){
            return
        }
        invokeClasses.add(InvokeClass(className,invokeBody,methodName))
        if (File(outputDir.absolutePath + "/" +Utils.dotToSlash(className)+".class").exists()){
            return
        }
        val cacheOutFir = outputCacheDir
        val outFile = if (cacheOutFir != null){
            File(cacheOutFir.absolutePath + "/" +Utils.dotToSlash(className)+".class")
        }else{
            File(outputDir.absolutePath + "/" +Utils.dotToSlash(className)+".class")
        }
        if (outFile.exists()){
            return
        }

        outFile.checkExist()
//        val className = "$packageName.Invoke${UUID.randomUUID()}"
        //新建一个类生成器，COMPUTE_FRAMES，COMPUTE_MAXS这2个参数能够让asm自动更新操作数栈
        val cw = ClassWriter(ClassWriter.COMPUTE_FRAMES or ClassWriter.COMPUTE_MAXS)
        //生成一个public的类，类路径是com.study.Human
        cw.visit(
            V1_8,
            ACC_PUBLIC, Utils.dotToSlash(className), null, "java/lang/Object", arrayOf(Utils.dotToSlash(INVOKE_CLASS))
        )

        //生成默认的构造方法： public Human()
        var mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null)
        mv.visitVarInsn(ALOAD, 0)
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false)
        mv.visitInsn(RETURN)
        mv.visitMaxs(0, 0) //更新操作数栈
        mv.visitEnd() //一定要有visitEnd


        //生成静态方法
        mv = cw.visitMethod(ACC_PUBLIC, INVOKE_METHOD, INVOKE_DESCRIPTOR, null, null)
        mv.visitCode();

        //创建StringBuilder对象
        mv.visitTypeInsn(NEW, "java/lang/Object")
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false)

        mv.visitInsn(IRETURN)
        mv.visitMaxs(0, 0)
        mv.visitEnd()

        cw.toByteArray().saveFile(outFile)
    }

    fun deleteInvokeClass(className:String) {
        if (reflectInvokeMethod){
            return
        }
        val iterator = invokeClasses.iterator()
        while (iterator.hasNext()){
            val item = iterator.next();
            if (item.packageName == className){
                iterator.remove()
                break
            }
        }
        outputCacheDir?.let {
            File(it.absolutePath + "/" +Utils.dotToSlash(className)+".class").delete()
        }
        File(outputDir.absolutePath + "/" +Utils.dotToSlash(className)+".class").delete()
    }
}