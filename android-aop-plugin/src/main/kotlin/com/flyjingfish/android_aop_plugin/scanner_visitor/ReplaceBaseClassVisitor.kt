package com.flyjingfish.android_aop_plugin.scanner_visitor

import com.flyjingfish.android_aop_plugin.utils.ClassPoolUtils
import com.flyjingfish.android_aop_plugin.utils.InitConfig
import com.flyjingfish.android_aop_plugin.utils.Utils.dotToSlash
import com.flyjingfish.android_aop_plugin.utils.Utils.slashToDot
import com.flyjingfish.android_aop_plugin.utils.Utils.slashToDotClassName
import com.flyjingfish.android_aop_plugin.utils.WovenInfoUtils
import com.flyjingfish.android_aop_plugin.utils.computeMD5
import com.flyjingfish.android_aop_plugin.utils.inRules
import com.flyjingfish.android_aop_plugin.utils.printError
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.commons.AdviceAdapter
import org.objectweb.asm.signature.SignatureReader
import org.objectweb.asm.signature.SignatureVisitor
import org.objectweb.asm.signature.SignatureWriter

open class ReplaceBaseClassVisitor(
    classVisitor: ClassVisitor
) : ClassVisitor(Opcodes.ASM9, classVisitor) {
    lateinit var thisClassName:String
    lateinit var clazzName:String
    lateinit var oldSuperName:String
    var modifyExtendsClassName:String?=null
    var isHasStaticClock = false
    var hasCollect = false
    var modifyed = false
    override fun visit(
        version: Int,
        access: Int,
        name: String,
        signature: String?,
        superName: String,
        interfaces: Array<out String>?
    ) {
        clazzName = name
        oldSuperName = superName
        thisClassName = slashToDotClassName(name)
        hasCollect = WovenInfoUtils.getAopCollectClassMap()[thisClassName] != null
        var replaceExtendsClassName = WovenInfoUtils.getModifyExtendsClass(thisClassName)
        if (replaceExtendsClassName == null){
            val superDotName = slashToDotClassName(superName)
            replaceExtendsClassName = WovenInfoUtils.getModifyExtendsClass(superDotName)
            if (replaceExtendsClassName != null){
                val isParent = WovenInfoUtils.getModifyExtendsClassParent(superDotName)
                if (!isParent){
                    replaceExtendsClassName = null
                }else if (replaceExtendsClassName == thisClassName){
                    replaceExtendsClassName = null
                }
                if (WovenInfoUtils.getModifyExtendsClassRules(superDotName)?.inRules(thisClassName) != true){
                    replaceExtendsClassName = null
                }
            }
        }else{
            val isParent = WovenInfoUtils.getModifyExtendsClassParent(thisClassName)
            if (isParent){
                replaceExtendsClassName = null
            }
            if (WovenInfoUtils.getModifyExtendsClassRules(thisClassName)?.inRules(thisClassName) != true){
                replaceExtendsClassName = null
            }
        }
        val newReplaceExtendsClassName = replaceExtendsClassName?.let {
            WovenInfoUtils.getClassString(
                it
            )
        }
        if (!replaceExtendsClassName.isNullOrEmpty() && !newReplaceExtendsClassName.isNullOrEmpty()){
            InitConfig.useModifyClassInfo(thisClassName)
            modifyExtendsClassName = dotToSlash(newReplaceExtendsClassName)
            val newSignature = if (signature != null) {
                updateSignature(signature, superName, modifyExtendsClassName!!);
            }else{
                signature
            }
            super.visit(version, access, name, newSignature, modifyExtendsClassName, interfaces)
        }else{
            super.visit(version, access, name, signature, superName, interfaces)
        }
    }
    private fun updateSignature(signature: String, oldClass: String, newClass: String): String {
        val reader = SignatureReader(signature)
        val writer = SignatureWriter()

        reader.accept(
            SignatureRemapper(
                writer,
                oldClass,
                newClass
            )
        )
        return writer.toString()
    }

    internal class SignatureRemapper(
        private val delegate: SignatureVisitor,
        private val oldClass: String,
        private val newClass: String
    ) : SignatureVisitor(Opcodes.ASM9) {

        private var isTargetClass = false

        override fun visitClassType(name: String) {
            if (name == oldClass) {
                isTargetClass = true // 标记正在修改的类
                delegate.visitClassType(newClass)
            } else {
                isTargetClass = false
                delegate.visitClassType(name)
            }
        }

        override fun visitTypeArgument(wildcard: Char): SignatureVisitor {
            return if (isTargetClass) {
                delegate.visitTypeArgument(wildcard) // 直接传递泛型参数
            } else {
                delegate.visitTypeArgument(wildcard)
            }
        }

        override fun visitFormalTypeParameter(name: String?) {
            delegate.visitFormalTypeParameter(name)
        }

        override fun visitClassBound(): SignatureVisitor {
            return SignatureRemapper(delegate.visitClassBound(), oldClass, newClass)
        }

        override fun visitInterfaceBound(): SignatureVisitor {
            return SignatureRemapper(delegate.visitInterfaceBound(), oldClass, newClass)
        }

        override fun visitSuperclass(): SignatureVisitor {
            return SignatureRemapper(delegate.visitSuperclass(), oldClass, newClass)
        }

        override fun visitInterface(): SignatureVisitor {
            return SignatureRemapper(delegate.visitInterface(), oldClass, newClass)
        }

        override fun visitTypeVariable(name: String?) {
            delegate.visitTypeVariable(name) // 直接传递泛型参数
        }

        override fun visitEnd() {
            delegate.visitEnd() // 确保不会额外添加 `T=;`
        }
    }

    override fun visitMethod(
        access: Int,
        name: String,
        descriptor: String,
        signature: String?,
        exceptions: Array<String?>?
    ): MethodVisitor? {

        var mv = super.visitMethod(
            access,
            name,
            descriptor,
            signature,
            exceptions
        )
        if (hasCollect && name == "<clinit>"){
            isHasStaticClock = true
            mv = MethodStaticAdapter(mv, access, name, descriptor)
        }
        if (modifyExtendsClassName != null && name == "<init>"){
            mv = MethodInitAdapter(mv)
        }
        mv = ReplaceInvokeMethodVisitor(mv,clazzName,oldSuperName)
        mv.onResultListener = object :ReplaceInvokeMethodVisitor.OnResultListener{
            override fun onBack() {
                modifyed = true
            }
        }
        return mv
    }

    inner class MethodStaticAdapter(mv: MethodVisitor, access: Int, name: String, desc: String?) :
        AdviceAdapter(Opcodes.ASM9, mv, access, name, desc) {

        override fun visitInsn(opcode: Int) {
            if (opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN) {
                modifyed = true
                val className = "$thisClassName\$Inner${thisClassName.computeMD5()}"
                mv.visitTypeInsn(NEW, dotToSlash(className));
                mv.visitInsn(DUP);//压入栈
                //弹出一个对象所在的地址，进行初始化操作，构造函数默认为空，此时栈大小为1（到目前只有一个局部变量）
                mv.visitMethodInsn(INVOKESPECIAL, dotToSlash(className),"<init>","()V",false);

            }
            super.visitInsn(opcode)
        }

        override fun visitMaxs(maxStack: Int, maxLocals: Int) {
            super.visitMaxs(maxStack + 4, maxLocals)
        }

        override fun onMethodExit(opcode: Int) {
            super.onMethodExit(opcode)
        }
    }

    inner class MethodInitAdapter(methodVisitor: MethodVisitor?) :
        MethodVisitor(Opcodes.ASM9, methodVisitor) {
        override fun visitMethodInsn(
            opcode: Int,
            owner: String,
            name: String,
            descriptor: String,
            isInterface: Boolean
        ) {
            val extendClass = modifyExtendsClassName
            if (name == "<init>" && oldSuperName == owner && extendClass != null) {
                if (!hasConstructor(slashToDot(extendClass),descriptor)){
                    printError("你需要保证 ${slashToDotClassName(extendClass)} 的构造方法和被修改类的构造方法的个数和定义都一致")
                }
                modifyed = true
                super.visitMethodInsn(opcode, extendClass, name, descriptor, isInterface)
            } else {
                super.visitMethodInsn(opcode, owner, name, descriptor, isInterface)
            }
        }

        private fun hasConstructor(extendClass:String, descriptor: String):Boolean{
            val hasConstructor = try {
                val cp = ClassPoolUtils.getNewClassPool()
                val ctClass = cp.get(extendClass)
                val has = ctClass.getConstructor(descriptor) != null
//                ctClass.detach()
                has
            } catch (e: Exception) {
                false
            }
            return hasConstructor
        }
    }
}