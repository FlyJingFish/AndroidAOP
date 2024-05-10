package com.flyjingfish.android_aop_plugin.scanner_visitor

import com.flyjingfish.android_aop_plugin.utils.InitConfig
import com.flyjingfish.android_aop_plugin.utils.Utils
import com.flyjingfish.android_aop_plugin.utils.Utils.computeMD5
import com.flyjingfish.android_aop_plugin.utils.Utils.dotToSlash
import com.flyjingfish.android_aop_plugin.utils.Utils.slashToDotClassName
import com.flyjingfish.android_aop_plugin.utils.WovenInfoUtils
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.commons.AdviceAdapter

open class ReplaceBaseClassVisitor(
    classVisitor: ClassVisitor
) : ClassVisitor(Opcodes.ASM9, classVisitor) {
    lateinit var thisClassName:String
    var isHasStaticClock = false
    var hasCollect = false
    override fun visit(
        version: Int,
        access: Int,
        name: String,
        signature: String?,
        superName: String?,
        interfaces: Array<out String>?
    ) {
        thisClassName = slashToDotClassName(name)
        hasCollect = WovenInfoUtils.aopCollectClassMap[thisClassName] != null
        val replaceExtendsClassName = WovenInfoUtils.getModifyExtendsClass(slashToDotClassName(name))
        val newReplaceExtendsClassName = replaceExtendsClassName?.let {
            WovenInfoUtils.getClassString(
                it
            )
        }
        if (!replaceExtendsClassName.isNullOrEmpty() && !newReplaceExtendsClassName.isNullOrEmpty()){
            InitConfig.useModifyClassInfo(slashToDotClassName(name))
            super.visit(version, access, name, signature, dotToSlash(newReplaceExtendsClassName), interfaces)
        }else{
            super.visit(version, access, name, signature, superName, interfaces)
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
            mv = MyMethodAdapter(mv, access, name, descriptor)
        }
        return mv
    }

    inner class MyMethodAdapter(mv: MethodVisitor, access: Int, name: String, desc: String?) :
        AdviceAdapter(Opcodes.ASM9, mv, access, name, desc) {

        override fun visitInsn(opcode: Int) {
            if (opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN) {
                val className = "$thisClassName\$Inner${computeMD5(thisClassName)}"
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
}