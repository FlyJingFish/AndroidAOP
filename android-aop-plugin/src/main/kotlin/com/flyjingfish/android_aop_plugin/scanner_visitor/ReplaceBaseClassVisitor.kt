package com.flyjingfish.android_aop_plugin.scanner_visitor

import com.flyjingfish.android_aop_plugin.utils.InitConfig
import com.flyjingfish.android_aop_plugin.utils.Utils.dotToSlash
import com.flyjingfish.android_aop_plugin.utils.Utils.slashToDotClassName
import com.flyjingfish.android_aop_plugin.utils.WovenInfoUtils
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Opcodes

open class ReplaceBaseClassVisitor(
    classVisitor: ClassVisitor
) : ClassVisitor(Opcodes.ASM9, classVisitor) {
    override fun visit(
        version: Int,
        access: Int,
        name: String,
        signature: String?,
        superName: String?,
        interfaces: Array<out String>?
    ) {
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

}