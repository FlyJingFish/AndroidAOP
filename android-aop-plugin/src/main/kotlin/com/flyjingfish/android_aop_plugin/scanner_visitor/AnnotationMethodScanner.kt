package com.flyjingfish.android_aop_plugin.scanner_visitor


import com.flyjingfish.android_aop_plugin.beans.AopMatchCut
import com.flyjingfish.android_aop_plugin.beans.CutMethodJson
import com.flyjingfish.android_aop_plugin.beans.LambdaMethod
import com.flyjingfish.android_aop_plugin.beans.MethodRecord
import com.flyjingfish.android_aop_plugin.config.AndroidAopConfig.Companion.cutInfoJson
import com.flyjingfish.android_aop_plugin.scanner_visitor.WovenIntoCode.getCtMethod
import com.flyjingfish.android_aop_plugin.utils.ClassPoolUtils.classPool
import com.flyjingfish.android_aop_plugin.utils.InitConfig.putCutInfo
import com.flyjingfish.android_aop_plugin.utils.Utils.getMethodInfo
import com.flyjingfish.android_aop_plugin.utils.Utils.isInstanceof
import com.flyjingfish.android_aop_plugin.utils.Utils.slashToDot
import com.flyjingfish.android_aop_plugin.utils.Utils.slashToDotClassName
import com.flyjingfish.android_aop_plugin.utils.WovenInfoUtils
import com.flyjingfish.android_aop_plugin.utils.WovenInfoUtils.getAnnoInfo
import com.flyjingfish.android_aop_plugin.utils.WovenInfoUtils.isContainAnno
import com.flyjingfish.android_aop_plugin.utils.WovenInfoUtils.isLeaf
import javassist.CtMethod
import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.Handle
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.InvokeDynamicInsnNode
import org.objectweb.asm.tree.MethodNode
import org.slf4j.Logger


class AnnotationMethodScanner(val logger: Logger, val onCallBackMethod: OnCallBackMethod) :
    ClassNode(Opcodes.ASM9) {
    //    private final byte[] classByte;
    private val aopMatchCuts = mutableListOf<AopMatchCut>();

    //    private final List<MethodRecord> cacheMethodRecords = new ArrayList<>();
    lateinit var className: String


    override fun visit(
        version: Int,
        access: Int,
        name: String,
        signature: String?,
        superName: String?,
        interfaces: Array<String>?
    ) {
        className = name
        //        logger.error("className="+className+",superName="+superName+",interfaces="+ Arrays.asList(interfaces));
        WovenInfoUtils.aopMatchCuts.forEach { (_: String?, aopMatchCut: AopMatchCut) ->
            if (AopMatchCut.MatchType.SELF.name != aopMatchCut.matchType) {
                val excludeClazz = aopMatchCut.excludeClass
                var exclude = false
                var isDirectExtends = false
                if (excludeClazz != null) {
                    val clsName = slashToDotClassName(className)
                    for (clazz in excludeClazz) {
                        if (clsName == slashToDotClassName(clazz)) {
                            exclude = true
                            break
                        }
                    }
                }
                if (!exclude) {
                    var isImplementsInterface = false
                    if (AopMatchCut.MatchType.EXTENDS.name == aopMatchCut.matchType) {
                        if (interfaces != null) {
                            for (anInterface in interfaces) {
                                val inter = slashToDotClassName(anInterface)
                                if (inter == slashToDotClassName(aopMatchCut.baseClassName)) {
                                    isImplementsInterface = true
                                    break
                                }
                            }
                        }
                    }
                    if (isImplementsInterface || slashToDotClassName(aopMatchCut.baseClassName) == slashToDotClassName(
                            superName!!
                        )
                    ) {
                        isDirectExtends = true
                    }
                    //isDirectExtends 为true 说明是直接继承
                    if (AopMatchCut.MatchType.DIRECT_EXTENDS.name == aopMatchCut.matchType) {
                        if (isDirectExtends) {
                            aopMatchCuts.add(aopMatchCut)
                        }
                    } else if (AopMatchCut.MatchType.LEAF_EXTENDS.name == aopMatchCut.matchType) {
                        var isExtends = false
                        if (isDirectExtends) {
                            isExtends = true
                        } else {
                            val clsName = slashToDotClassName(className)
                            val parentClsName = aopMatchCut.baseClassName
                            if (clsName != slashToDotClassName(parentClsName)) {
                                isExtends = isInstanceof(clsName, slashToDotClassName(parentClsName))
                            }
                        }
                        if (isExtends && isLeaf(className)) {
                            aopMatchCuts.add(aopMatchCut)
                        }
                    } else {
                        if (isDirectExtends) {
                            aopMatchCuts.add(aopMatchCut)
                        } else {
                            val clsName = slashToDotClassName(className)
                            val parentClsName = aopMatchCut.baseClassName
                            if (clsName != slashToDotClassName(parentClsName)) {
                                val isInstanceof = isInstanceof(clsName, slashToDotClassName(parentClsName))
                                if (isInstanceof) {
                                    aopMatchCuts.add(aopMatchCut)
                                }
                            }
                        }
                    }
                }
            }
            if (AopMatchCut.MatchType.SELF.name == aopMatchCut.matchType && slashToDotClassName(
                    aopMatchCut.baseClassName
                ) == slashToDotClassName(name)
            ) {
                aopMatchCuts.add(aopMatchCut)
            }
        }
        super.visit(version, access, name, signature, superName, interfaces)
    }

    override fun visitAnnotation(descriptor: String?, visible: Boolean): AnnotationVisitor? {
        return super.visitAnnotation(descriptor, visible)
    }

    open inner class MyMethodVisitor
        (
        access: Int,
        name: String,
        descriptor: String?,
        signature: String?,
        exceptions: Array<String?>?, var methodName: MethodRecord
    ) : MethodNode(
        Opcodes.ASM9, access,
        name,
        descriptor,
        signature,
        exceptions
    ) {
        override fun visitAnnotation(descriptor: String, visible: Boolean): AnnotationVisitor? {
//            logger.error("AnnotationMethodScanner MyMethodVisitor type: " + descriptor);
            if (isContainAnno(descriptor)) {
                var isBack = true
                try {
                    val classPool = classPool
                    //                    InputStream byteArrayInputStream = new ByteArrayInputStream(classByte);
//                    CtClass ctClass = classPool.makeClass(byteArrayInputStream);
                    val clsName = slashToDot(className)
                    val ctClass = classPool!![clsName]
                    val ctMethod = getCtMethod(ctClass, methodName.methodName, methodName.descriptor)
                    val methodInfo = ctMethod!!.methodInfo
                    val codeAttribute = methodInfo.codeAttribute
                    if (codeAttribute == null) {
                        isBack = false
                    }
                } catch (ignored: Exception) {
                }
                if (isBack) {
                    onCallBackMethod.onBackName(methodName)
                    val aopMethodCut = getAnnoInfo(descriptor)
                    if (aopMethodCut != null && cutInfoJson) {
                        putCutInfo(
                            "注解切面", slashToDot(className), aopMethodCut.anno,
                            CutMethodJson(methodName.methodName, methodName.descriptor, false)
                        )
                    }
                }
            }
            return super.visitAnnotation(descriptor, visible)
        }
    }

    override fun visitMethod(
        access: Int, name: String, descriptor: String,
        signature: String?, exceptions: Array<String?>?
    ): MethodVisitor {
        if (aopMatchCuts.size > 0) {
            for (aopMatchCut in aopMatchCuts) {
                for (methodName in aopMatchCut.methodNames) {
                    val matchMethodInfo = getMethodInfo(methodName)
                    if (matchMethodInfo != null && name == matchMethodInfo.name) {
                        var isBack = true
                        try {
                            val classPool = classPool
                            val clsName = slashToDot(className)
                            val ctClass = classPool!![clsName]
                            //                            InputStream byteArrayInputStream = new ByteArrayInputStream(classByte);
//                            CtClass ctClass = classPool.makeClass(byteArrayInputStream);
                            val ctMethod = getCtMethod(ctClass, name, descriptor)
                            if (matchMethodInfo.paramTypes != null) {
                                val ctClasses = ctMethod!!.parameterTypes
                                val paramStr = java.lang.StringBuilder()
                                paramStr.append("(")
                                val length = ctClasses.size
                                for (i in 0 until length) {
                                    paramStr.append(ctClasses[i].name)
                                    if (i != length - 1) {
                                        paramStr.append(",")
                                    }
                                }
                                paramStr.append(")")
                                //有设置参数类型这一项
                                if (paramStr.toString() != matchMethodInfo.paramTypes) {
                                    isBack = false
                                }
                            }
                            if (matchMethodInfo.returnType != null) {
                                val returnCtClass = ctMethod!!.returnType
                                val returnType = returnCtClass.name
                                //有设置返回类型这一项
                                if (returnType != matchMethodInfo.returnType) {
                                    isBack = false
                                }
                            }
                            //                            logger.error("paramStr="+paramStr+",returnType="+returnType+",matchMethodInfo="+matchMethodInfo);
                            val methodInfo = ctMethod!!.methodInfo
                            val codeAttribute = methodInfo.codeAttribute
                            if (codeAttribute == null) {
                                isBack = false
                            }
                        } catch (ignored: java.lang.Exception) {
                        }
                        if (isBack) {
                            onCallBackMethod.onBackName(
                                MethodRecord(
                                    name,
                                    descriptor,
                                    aopMatchCut.cutClassName
                                )
                            )
                            //                            cacheMethodRecords.add(new MethodRecord(name, descriptor, aopMatchCut.getCutClassName()));
                            if (cutInfoJson) {
                                putCutInfo(
                                    "匹配切面",
                                    slashToDot(
                                        className
                                    ),
                                    aopMatchCut.cutClassName,
                                    CutMethodJson(name, descriptor, false)
                                )
                            }
                        }
                        break
                    }
                }
            }
        }
        val myMethodVisitor = MyMethodVisitor(
            access, name, descriptor, signature, exceptions, MethodRecord(name, descriptor, null)
        )
        methods.add(myMethodVisitor)
        return myMethodVisitor
    }

    var lambdaMethodList = mutableListOf<LambdaMethod>()


    override fun visitEnd() {
        super.visitEnd()

        this.methods.forEach { methodNode ->

            for (tmpNode in methodNode.instructions) {
                if (tmpNode is InvokeDynamicInsnNode) {
                    val desc = tmpNode.desc
                    val descType = Type.getType(desc)
                    val samBaseType = descType.returnType
                    //sam 接口名
                    val samBase = samBaseType.descriptor
                    //sam 方法名
                    val samMethodName = tmpNode.name
                    val bsmArgs = tmpNode.bsmArgs
                    val samMethodType = bsmArgs[0] as Type
                    val methodName = bsmArgs[1] as Handle
                    //sam 实现方法实际参数描述符
//                  Type implMethodType = (Type) bsmArgs[2];
                    val lambdaName = methodName.name
                    val thisClassName = slashToDot(
                        samBase.substring(1).replace(";".toRegex(), "")
                    ).replace("$", ".")
                    val originalClassName =
                        slashToDot(samBase.substring(1).replace(";".toRegex(), ""))
                    //                    logger.error("className="+className+",tmpNode.name="+tmpNode.name+",desc=" + desc + ",samBase=" + samBase + ",samMethodName="
//                            + samMethodName + ",methodName=" + lambdaName+ ",methodDesc=" + methodName.getDesc()+",thisClassName="+thisClassName+
//                            ",getDescriptor"+samMethodType.getDescriptor());
                    val lambdaDesc = methodName.desc
                    val samMethodDesc = samMethodType.descriptor
                    val lambdaMethod = LambdaMethod(
                        samMethodName,
                        samMethodDesc,
                        thisClassName,
                        originalClassName,
                        lambdaName,
                        lambdaDesc
                    )
                    lambdaMethodList.add(lambdaMethod)
                }
            }

            if (lambdaMethodList.size > 0) {
                WovenInfoUtils.aopMatchCuts.forEach { (_: String?, aopMatchCut: AopMatchCut) ->
                    if (AopMatchCut.MatchType.SELF.name != aopMatchCut.matchType && aopMatchCut.methodNames.size == 1) {
                        for ((_, descriptor, clsName, originalClassName, lambdaName, lambdaDesc) in lambdaMethodList) {
                            val isDirectExtends =
                                slashToDotClassName(aopMatchCut.baseClassName) == clsName
                            var isMatch = false
                            if (AopMatchCut.MatchType.DIRECT_EXTENDS.name == aopMatchCut.matchType) {
                                isMatch = isDirectExtends
                            } else if (AopMatchCut.MatchType.EXTENDS.name == aopMatchCut.matchType || AopMatchCut.MatchType.LEAF_EXTENDS.name == aopMatchCut.matchType) {
                                isMatch = isDirectExtends
                                if (!isMatch) {
                                    val parentClsName = aopMatchCut.baseClassName
                                    isMatch = isInstanceof(clsName, slashToDotClassName(parentClsName))
                                }
                            }
                            val aopMatchCutMethodName = aopMatchCut.methodNames[0]
                            val matchMethodInfo = getMethodInfo(aopMatchCutMethodName)
                            if (isMatch && matchMethodInfo != null && name == matchMethodInfo.name) {
                                var isBack = true
                                try {
                                    val classPool = classPool
                                    var ctMethod: CtMethod? = null
                                    if (matchMethodInfo.paramTypes != null || matchMethodInfo.returnType != null) {
                                        val ctClass = classPool!![originalClassName]
                                        ctMethod = getCtMethod(ctClass, name, descriptor)
                                    }
                                    if (matchMethodInfo.paramTypes != null && ctMethod != null) {
                                        val ctClasses = ctMethod.parameterTypes
                                        val paramStr = StringBuilder()
                                        paramStr.append("(")
                                        val length = ctClasses.size
                                        for (i in 0 until length) {
                                            paramStr.append(ctClasses[i].name)
                                            if (i != length - 1) {
                                                paramStr.append(",")
                                            }
                                        }
                                        paramStr.append(")")
                                        //有设置参数类型这一项
                                        if (paramStr.toString() != matchMethodInfo.paramTypes) {
                                            isBack = false
                                        }
                                    }
                                    if (matchMethodInfo.returnType != null && ctMethod != null) {
                                        val returnCtClass = ctMethod.returnType
                                        val returnType = returnCtClass.name
                                        //有设置返回类型这一项
                                        if (returnType != matchMethodInfo.returnType) {
                                            isBack = false
                                        }
                                    }
                                } catch (ignored: java.lang.Exception) {
                                }
                                if (isBack) {
                                    val methodRecord = MethodRecord(
                                        lambdaName,
                                        lambdaDesc,
                                        aopMatchCut.cutClassName
                                    )
                                    onCallBackMethod.onBackName(methodRecord)
                                    if (cutInfoJson) {
//                                        InitConfig.INSTANCE.putCutInfo("匹配切面",lambdaMethod.getOriginalClassName()+"_"+lambdaMethod.getLambdaName(),aopMatchCut.getCutClassName(),new CutMethodJson(name,returnType == null?(descriptor.substring(0,descriptor.lastIndexOf(")")+1)):paramStr.toString(),returnType == null?(descriptor.substring(descriptor.lastIndexOf(")")+1)):returnType,true));
                                        putCutInfo(
                                            "匹配切面",
                                            originalClassName + "_" + lambdaName,
                                            aopMatchCut.cutClassName,
                                            CutMethodJson(name, descriptor, true)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    interface OnCallBackMethod {
        fun onBackName(methodRecord: MethodRecord)
    }
}