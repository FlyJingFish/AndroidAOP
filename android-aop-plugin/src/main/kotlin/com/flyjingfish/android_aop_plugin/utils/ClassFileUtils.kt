package com.flyjingfish.android_aop_plugin.utils

import com.flyjingfish.android_aop_plugin.beans.InvokeClass
import com.flyjingfish.android_aop_plugin.scanner_visitor.WovenIntoCode
import javassist.CannotCompileException
import javassist.ClassPool
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
    lateinit var outputDir:File
    lateinit var outputTmpDir:File
    private val invokeClasses = mutableListOf<InvokeClass>()
    private const val oldMethodName = "invoke"
    private const val oldDescriptor = "(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;"
    fun clear(){
        invokeClasses.clear()
    }
    fun wovenInfoInvokeClass(outputJar: File? = null) {
        if (reflectInvokeMethod){
            return
        }
        for (invokeClass in invokeClasses) {

            val className = invokeClass.packageName
            val invokeBody = invokeClass.invokeBody
//            println("invokeClass.methodName="+invokeClass.methodName)
            val cp = if (outputJar != null){
                val classPool = ClassPool(null)
                val list = WovenInfoUtils.baseClassPaths
                classPool.appendSystemPath()
                for (file in list) {
                    classPool.appendClassPath(file)
                }
                classPool.appendClassPath(outputJar.absolutePath)
                classPool.appendClassPath(outputDir.absolutePath)
                classPool
            }else{
                ClassPoolUtils.getNewClassPool()
            }
            cp.importPackage("com.flyjingfish.android_aop_annotation.Conversions")
            val ctClass = cp.get(className)
            try {
                val ctMethod =
                    WovenIntoCode.getCtMethod(ctClass, oldMethodName, oldDescriptor)
                ctMethod?.setBody(invokeBody)
            } catch (e: NotFoundException) {
                throw RuntimeException(e)
            } catch (e: CannotCompileException) {
                throw RuntimeException(e)
            }
            val classByteData = ctClass.toBytecode()
//            //把类数据写入到class文件,这样你就可以把这个类文件打包供其他的人使用
            val path = outputDir.absolutePath + "/" +Utils.dotToSlash(className)+".class"
            val outFile = File(path)
            if (!outFile.parentFile.exists()){
                outFile.parentFile.mkdirs()
            }
            if (!outFile.exists()){
                outFile.createNewFile()
            }
            ByteArrayInputStream(classByteData).use {
                it.copyTo(FileOutputStream(outFile))
            }
        }
    }

    fun createInvokeClass(className:String, invokeBody:String, methodName:String) {
        if (reflectInvokeMethod){
            return
        }
        invokeClasses.add(InvokeClass(className,invokeBody,methodName))
//        val className = "$packageName.Invoke${UUID.randomUUID()}"
        val invokeClass = "com.flyjingfish.android_aop_annotation.utils.InvokeMethod"
        //新建一个类生成器，COMPUTE_FRAMES，COMPUTE_MAXS这2个参数能够让asm自动更新操作数栈
        val cw = ClassWriter(ClassWriter.COMPUTE_FRAMES or ClassWriter.COMPUTE_MAXS)
        //生成一个public的类，类路径是com.study.Human
        cw.visit(
            V1_8,
            ACC_PUBLIC, Utils.dotToSlash(className), null, "java/lang/Object", arrayOf(Utils.dotToSlash(invokeClass))
        )

        //生成默认的构造方法： public Human()
        var mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null)
        mv.visitVarInsn(ALOAD, 0)
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false)
        mv.visitInsn(RETURN)
        mv.visitMaxs(0, 0) //更新操作数栈
        mv.visitEnd() //一定要有visitEnd


        //生成静态方法
        mv = cw.visitMethod(ACC_PUBLIC, oldMethodName, oldDescriptor, null, null)
        mv.visitCode();

        //创建StringBuilder对象
        mv.visitTypeInsn(NEW, "java/lang/Object")
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false)

        mv.visitInsn(IRETURN)
        mv.visitMaxs(0, 0)
        mv.visitEnd();

        val path = outputDir.absolutePath + "/" +Utils.dotToSlash(className)+".class"
        val outFile = File(path)
        if (!outFile.parentFile.exists()){
            outFile.parentFile.mkdirs()
        }
        if (!outFile.exists()){
            outFile.createNewFile()
        }
        ByteArrayInputStream(cw.toByteArray()).use {
            it.copyTo(FileOutputStream(outFile))
        }
    }
}