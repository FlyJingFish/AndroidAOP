package com.flyjingfish.android_aop_plugin.scanner_visitor

import com.flyjingfish.android_aop_plugin.beans.ReplaceMethodInfo
import com.flyjingfish.android_aop_plugin.utils.InitConfig
import com.flyjingfish.android_aop_plugin.utils.Utils
import com.flyjingfish.android_aop_plugin.utils.WovenInfoUtils
import com.flyjingfish.android_aop_plugin.utils.printLog
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.commons.LocalVariablesSorter


class MethodReplaceInvokeAdapterUtils(private val className:String, private val superName: String, val methodAccess:Int,
                                      val methodName:String, val methodDesc:String,private val methodVisitor: MethodVisitor?,private var superCall : SuperCall){
    private val methodNameDesc = Utils.getRealMethodName(methodName)+methodDesc
    private val isConstructorMethod = methodNameDesc.startsWith("<init>(")
    private val canReplaceMethod = !(superName == "kotlin/coroutines/jvm/internal/ContinuationImpl" && methodNameDesc == "invokeSuspend(Ljava/lang/Object;)Ljava/lang/Object;" )
    val deleteNews = mutableListOf<ReplaceMethodInfo>()
    interface OnResultListener{
        fun onBack()
        fun onBack(delNews:List<ReplaceMethodInfo>)
    }
    var onResultListener : OnResultListener ?= null
    interface SuperCall{
        fun superVisitTypeInsn(opcode: Int, type: String)

        fun superVisitMethodInsn(opcode: Int,
                                 owner: String,
                                 name: String,
                                 descriptor: String,
                                 isInterface: Boolean)

        fun superVisitVarInsn(opcode: Int, varIndex:Int)
        fun superVisitInsn(opcode: Int)
        fun superVisitLdcInsn(value: Any?)
    }

    fun visitTypeInsn(opcode: Int, type: String) {
        if (opcode == Opcodes.NEW) {
            val replaceMethodInfo = getReplaceInfo(type, "<init>", "")
            if (replaceMethodInfo != null && replaceMethodInfo.replaceType == ReplaceMethodInfo.ReplaceType.NEW && replaceMethodInfo.newClassName.isNotEmpty()){
                superCall.superVisitTypeInsn(opcode, replaceMethodInfo.newClassName)
                onResultListener?.onBack()
                return
            }
        }
        superCall.superVisitTypeInsn(opcode, type)
    }

    private fun getReplaceInfo(owner: String,
                               name: String,
                               descriptor: String):ReplaceMethodInfo?{
        val key = owner + name + descriptor
        var replaceMethodInfo = WovenInfoUtils.getReplaceMethodInfoUse(key)
        var isReplaceClass = false
        if (replaceMethodInfo == null){
            val oldOwner = WovenInfoUtils.getRealReplaceInfo(owner)
            if (oldOwner != null){
                val oldKey = oldOwner + name + descriptor
                replaceMethodInfo = WovenInfoUtils.getReplaceMethodInfoUse(oldKey)
                isReplaceClass = replaceMethodInfo != null && replaceMethodInfo.oldOwner == oldOwner && replaceMethodInfo.oldMethodName == name && replaceMethodInfo.oldMethodDesc == descriptor
            }
        }else{
            isReplaceClass = replaceMethodInfo.oldOwner == owner && replaceMethodInfo.oldMethodName == name && replaceMethodInfo.oldMethodDesc == descriptor
        }
        return if (isReplaceClass){
            replaceMethodInfo
        }else {
            null
        }
    }

    fun visitMethodInsn(
        opcode: Int,
        owner: String,
        name: String,
        descriptor: String,
        isInterface: Boolean
    ) {
        val isInMethodInner = opcode == Opcodes.INVOKESPECIAL && owner == superName  && name == methodName && descriptor == methodDesc

        var replaceMethodInfo = getReplaceInfo(owner, name, "")
        var isReplaceClass = replaceMethodInfo != null && replaceMethodInfo.replaceType == ReplaceMethodInfo.ReplaceType.NEW && replaceMethodInfo.newClassName.isNotEmpty()
        if (!isReplaceClass){
            val methodReplaceMethodInfo = getReplaceInfo(owner, name, descriptor)
            if (methodReplaceMethodInfo != null){
                replaceMethodInfo = methodReplaceMethodInfo
            }
            isReplaceClass = replaceMethodInfo != null
        }

        if (isReplaceClass && replaceMethodInfo != null
            && (!className.contains(replaceMethodInfo.newOwner) || methodNameDesc != "${replaceMethodInfo.newMethodName}${replaceMethodInfo.newMethodDesc}")) {
            val shouldReplace: Boolean = if (replaceMethodInfo.replaceType == ReplaceMethodInfo.ReplaceType.NEW){
                if (isConstructorMethod){
                    !(owner == superName && name == "<init>")
                }else{
                    "" == replaceMethodInfo.oldMethodDesc
                }
            }else if (replaceMethodInfo.replaceType == ReplaceMethodInfo.ReplaceType.INIT) {
                if (isConstructorMethod){
                    !(owner == superName && name == "<init>")
                }else{
                    descriptor == replaceMethodInfo.oldMethodDesc
                }
            } else if (opcode == Opcodes.INVOKESTATIC) {
                descriptor == replaceMethodInfo.newMethodDesc
            } else {
                descriptor.replace("(", "(L${replaceMethodInfo.oldOwner};") == replaceMethodInfo.newMethodDesc || descriptor.replace("(", "(Ljava/lang/Object;") == replaceMethodInfo.newMethodDesc
            }
            if (shouldReplace && !isInMethodInner) {
                val isThisInit = owner == className && methodName == "<init>" && methodName == name
                val isInitAop = replaceMethodInfo.replaceType == ReplaceMethodInfo.ReplaceType.INIT
                if (isInitAop && isThisInit){
                    superCall.superVisitMethodInsn(opcode, owner, name, descriptor, isInterface)
                    return
                }
                if (replaceMethodInfo.replaceType == ReplaceMethodInfo.ReplaceType.NEW && replaceMethodInfo.isCallNew()) {
                    superCall.superVisitMethodInsn(opcode, replaceMethodInfo.newClassName, name, descriptor, isInterface)
                }else if (isInitAop) {
                    if (replaceMethodInfo.isDeleteNew()){
//                        val argTypes = Type.getArgumentTypes(descriptor)
//
//                        // 保存参数到本地变量表，注意逆序存储
//                        val localIndexes = mutableListOf<Int>()
//                        for (i in argTypes.indices.reversed()) {
//                            val argType = argTypes[i]
//                            val local = (methodVisitor as LocalVariablesSorter).newLocal(argType)
//                            storeLocal(local, argType)
//                            localIndexes.add(0, local) // 顺序压入
//                        }

                        // 弹掉 uninit_obj
//                        superCall.superVisitInsn(Opcodes.POP)

//                        // 3. 加载类对象（传入静态方法）
//                        superCall.superVisitLdcInsn(Type.getObjectType(owner))
//
                        // 恢复参数
//                        for (i in argTypes.indices) {
//                            loadLocal(localIndexes[i], argTypes[i])
//                        }
                        deleteNews.add(replaceMethodInfo.copy(oldOwner = owner))

                    }else{
                        superCall.superVisitMethodInsn(opcode, owner, name, descriptor, isInterface)
                    }
                }
                InitConfig.addReplaceMethodInfo(replaceMethodInfo)
                if (replaceMethodInfo.replaceType == ReplaceMethodInfo.ReplaceType.NEW && !replaceMethodInfo.isCallNew()){
                    superCall.superVisitMethodInsn(opcode, replaceMethodInfo.newClassName, name, descriptor, isInterface)
                    onResultListener?.onBack()
                }else{
                    if (canReplaceMethod && !replaceMethodInfo.isDeleteNew()){
                        // 注意，最后一个参数是false，会不会太武断呢？
                        superCall.superVisitMethodInsn(
                            Opcodes.INVOKESTATIC,
                            replaceMethodInfo.newOwner,
                            replaceMethodInfo.newMethodName,
                            replaceMethodInfo.newMethodDesc,
                            false
                        )
                        if (isInitAop){
                            superCall.superVisitTypeInsn(Opcodes.CHECKCAST, owner)
                        }
                        onResultListener?.onBack()
                    }else {
                        superCall.superVisitMethodInsn(opcode, owner, name, descriptor, isInterface)
                    }
                }
            } else {
                superCall.superVisitMethodInsn(opcode, owner, name, descriptor, isInterface)
            }
        } else {
            superCall.superVisitMethodInsn(opcode, owner, name, descriptor, isInterface)
        }
    }

    private fun storeLocal(index: Int, type: Type) {
        superCall.superVisitVarInsn(type.getOpcode(Opcodes.ISTORE), index)
    }

    private fun loadLocal(index: Int, type: Type) {
        superCall.superVisitVarInsn(type.getOpcode(Opcodes.ILOAD), index)
    }
}