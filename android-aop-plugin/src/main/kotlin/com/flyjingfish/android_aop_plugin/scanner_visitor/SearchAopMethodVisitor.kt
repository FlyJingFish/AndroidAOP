package com.flyjingfish.android_aop_plugin.scanner_visitor


import com.flyjingfish.android_aop_plugin.beans.AopMatchCut
import com.flyjingfish.android_aop_plugin.beans.CutInfo
import com.flyjingfish.android_aop_plugin.beans.CutMethodJson
import com.flyjingfish.android_aop_plugin.beans.LambdaMethod
import com.flyjingfish.android_aop_plugin.beans.MethodRecord
import com.flyjingfish.android_aop_plugin.beans.ReplaceMethodInfo
import com.flyjingfish.android_aop_plugin.utils.Utils
import com.flyjingfish.android_aop_plugin.utils.Utils.getMethodInfo
import com.flyjingfish.android_aop_plugin.utils.Utils.isHasMethodBody
import com.flyjingfish.android_aop_plugin.utils.Utils.isInstanceof
import com.flyjingfish.android_aop_plugin.utils.Utils.slashToDot
import com.flyjingfish.android_aop_plugin.utils.Utils.slashToDotClassName
import com.flyjingfish.android_aop_plugin.utils.WovenInfoUtils
import com.flyjingfish.android_aop_plugin.utils.WovenInfoUtils.getAnnoInfo
import com.flyjingfish.android_aop_plugin.utils.WovenInfoUtils.isContainAnno
import com.flyjingfish.android_aop_plugin.utils.WovenInfoUtils.isLeaf
import com.flyjingfish.android_aop_plugin.utils.printLog
import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.Handle
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.commons.Method
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.InvokeDynamicInsnNode
import org.objectweb.asm.tree.MethodNode
import java.util.UUID


class SearchAopMethodVisitor(val onCallBackMethod: OnCallBackMethod?) :
    ClassNode(Opcodes.ASM9) {
    //    private final byte[] classByte;
    private val aopMatchCuts = mutableListOf<AopMatchCut>();

    //    private final List<MethodRecord> cacheMethodRecords = new ArrayList<>();
    lateinit var className: String
    private var replaceInvokeClassName: String?=null
    private var replaceTargetClassName: String?=null


    override fun visit(
        version: Int,
        access: Int,
        name: String,
        signature: String?,
        superName: String?,
        interfaces: Array<String>?
    ) {
        className = name
        val seeClsName = slashToDotClassName(className)
        val isReplaceClass = WovenInfoUtils.containInvoke(seeClsName)
        if (isReplaceClass){
            replaceInvokeClassName = className

            replaceTargetClassName = WovenInfoUtils.getTargetClassName(seeClsName)
            if (replaceTargetClassName != null){
                val classString = WovenInfoUtils.getClassString(replaceTargetClassName!!)
                if (classString != null){
                    replaceTargetClassName = Utils.dotToSlash(classString)
                }
            }
        }
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
                    if (interfaces != null) {
                        for (anInterface in interfaces) {
                            val inter = slashToDotClassName(anInterface)
                            if (inter == slashToDotClassName(aopMatchCut.baseClassName)) {
                                isImplementsInterface = true
                                break
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
    companion object {
        const val REPLACE_POINT =
            "Lcom/flyjingfish/android_aop_annotation/anno/AndroidAopReplaceMethod"
    }
    open inner class MyMethodVisitor
        (
        val access: Int,
        val methodname: String,
        val methoddescriptor: String,
        val signature: String?,
        val exceptions: Array<String?>?, var methodName: MethodRecord
    ) : MethodNode(
        Opcodes.ASM9,
        access,
        methodname,
        methoddescriptor,
        signature,
        exceptions
    ) {
        override fun visitAnnotation(descriptor: String, visible: Boolean): AnnotationVisitor? {
//            logger.error("AnnotationMethodScanner MyMethodVisitor type: " + descriptor);
            if (isContainAnno(descriptor)) {
                if (isBackMethod(access)) {
                    val aopMethodCut = getAnnoInfo(descriptor)
                    if (aopMethodCut != null){
                        val cutInfo = CutInfo(
                            "注解切面", slashToDot(className), aopMethodCut.anno,
                            CutMethodJson(methodName.methodName, methodName.descriptor, false)
                        );
                        methodName.cutInfo[UUID.randomUUID().toString()] = cutInfo
                    }
                    onCallBackMethod?.onBackMethodRecord(methodName)
                }
            }

            if (descriptor.contains(REPLACE_POINT) && replaceTargetClassName != null && Utils.isStaticMethod(access)){

                val replaceMethodInfo = ReplaceMethodInfo(
                    replaceTargetClassName!!,"","",
                    className,methodname,methoddescriptor
                )
                return ReplaceMethodVisitor(replaceMethodInfo)
            }
            return super.visitAnnotation(descriptor, visible)
        }


        internal inner class ReplaceMethodVisitor(private val replaceMethodInfo: ReplaceMethodInfo) : AnnotationVisitor(Opcodes.ASM9) {
            private var methodname: String? = null
            override fun visit(name: String, value: Any) {
                if (name == "value") {
                    methodname = value.toString()
                }
                super.visit(name, value)
            }

            override fun visitEnd() {
                super.visitEnd()
                val name = methodname
                if (!name.isNullOrEmpty()){
                    try {
                        val methodInfo = getMethodInfo(name)
                        if (methodInfo != null && methodInfo.checkAvailable()){
                            val methodText = methodInfo.returnType + " " + methodInfo.name + methodInfo.paramTypes

                            val method = Method.getMethod(methodText)
                            replaceMethodInfo.oldMethodName = method.name
                            replaceMethodInfo.oldMethodDesc = method.descriptor
                            if (replaceMethodInfo.checkAvailable()){
                                onCallBackMethod?.onBackReplaceMethodInfo(replaceMethodInfo)
                            }
                        }


                    } catch (_: Exception) {

                    }
                }
            }
        }
    }
    private fun isBackMethod(access: Int):Boolean{
        return isHasMethodBody(access)
    }
    override fun visitMethod(
        access: Int, name: String, descriptor: String,
        signature: String?, exceptions: Array<String?>?
    ): MethodVisitor {
        if ("<init>" != name && "<clinit>" != name && aopMatchCuts.size > 0 && isBackMethod(access)) {
            for (aopMatchCut in aopMatchCuts) {
                fun addMatchMethodCut(){
                    val methodRecord = MethodRecord(
                        name,
                        descriptor,
                        aopMatchCut.cutClassName
                    )
                    val cutInfo = CutInfo(
                        "匹配切面",
                        slashToDot(
                            className
                        ),
                        aopMatchCut.cutClassName,
                        CutMethodJson(name, descriptor, false)
                    )
                    methodRecord.cutInfo[UUID.randomUUID().toString()] = cutInfo
                    onCallBackMethod?.onBackMethodRecord(methodRecord)
                }
//                printLog("$aopMatchCut === ${aopMatchCut.isMatchAllMethod()}")
                if (aopMatchCut.isMatchAllMethod() ){
                    addMatchMethodCut()
                }else{
                    for (methodName in aopMatchCut.methodNames) {
                        val matchMethodInfo = getMethodInfo(methodName)
                        if (matchMethodInfo != null && name == matchMethodInfo.name) {
                            val isBack = try {
                                Utils.verifyMatchCut(descriptor,matchMethodInfo)
                            } catch (e: Exception) {
                                true
                            }
                            if (isBack) {
                                addMatchMethodCut()
                                //                            cacheMethodRecords.add(new MethodRecord(name, descriptor, aopMatchCut.getCutClassName()));
                            }
                        }
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

    private val lambdaMethodList = mutableListOf<LambdaMethod>()


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
                    val originalClassName = slashToDot(samBase.substring(1).replace(";", ""))
                    val thisClassName = originalClassName.replace("$", ".")
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
                for ((_, _, _, _, lambdaName, lambdaDesc) in lambdaMethodList) {
                    for (aopMatchCut in aopMatchCuts) {
                        if (aopMatchCut.isMatchAllMethod()){
                            val methodRecord = MethodRecord(
                                lambdaName,
                                lambdaDesc,
                                aopMatchCut.cutClassName
                            )
                            onCallBackMethod?.onDeleteMethodRecord(methodRecord)
                        }
                    }
                }

                WovenInfoUtils.aopMatchCuts.forEach { (_: String?, aopMatchCut: AopMatchCut) ->
                    if (AopMatchCut.MatchType.SELF.name != aopMatchCut.matchType && aopMatchCut.methodNames.size == 1) {
                        for ((name, descriptor, clsName, originalClassName, lambdaName, lambdaDesc) in lambdaMethodList) {
                            if ("<init>" != name && "<clinit>" != name && "<init>" != lambdaName && "<clinit>" != lambdaName){
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

                                if (isMatch){
                                    fun addMatchMethodCut(){
                                        val cutInfo = CutInfo(
                                            "匹配切面",
                                            originalClassName + "_" + lambdaName,
                                            aopMatchCut.cutClassName,
                                            CutMethodJson(name, descriptor, true)
                                        )
                                        val methodRecord = MethodRecord(
                                            lambdaName,
                                            lambdaDesc,
                                            aopMatchCut.cutClassName,
                                            true
                                        )
                                        methodRecord.cutInfo[UUID.randomUUID().toString()] = cutInfo
                                        onCallBackMethod?.onBackMethodRecord(methodRecord)
                                    }
                                    if (aopMatchCut.isMatchAllMethod()){
                                        addMatchMethodCut()
                                    }else{
                                        val aopMatchCutMethodName = aopMatchCut.methodNames[0]
                                        val matchMethodInfo = getMethodInfo(aopMatchCutMethodName)
                                        if (matchMethodInfo != null && name == matchMethodInfo.name) {
                                            val isBack = try {
                                                Utils.verifyMatchCut(descriptor,matchMethodInfo)
                                            } catch (e: Exception) {
                                                true
                                            }
                                            if (isBack) {
                                                addMatchMethodCut()
                                            }
                                        }
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
        fun onBackMethodRecord(methodRecord: MethodRecord)
        fun onDeleteMethodRecord(methodRecord: MethodRecord)
        fun onBackReplaceMethodInfo(replaceMethodInfo: ReplaceMethodInfo)
    }
}