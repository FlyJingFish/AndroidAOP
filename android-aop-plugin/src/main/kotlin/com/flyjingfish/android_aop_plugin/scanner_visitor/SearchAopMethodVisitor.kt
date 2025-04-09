package com.flyjingfish.android_aop_plugin.scanner_visitor


import com.flyjingfish.android_aop_annotation.base.BasePointCut
import com.flyjingfish.android_aop_annotation.base.BasePointCutSuspend
import com.flyjingfish.android_aop_annotation.base.MatchClassMethod
import com.flyjingfish.android_aop_annotation.base.MatchClassMethodSuspend
import com.flyjingfish.android_aop_annotation.utils.InvokeMethod
import com.flyjingfish.android_aop_plugin.beans.AopCollectClass
import com.flyjingfish.android_aop_plugin.beans.AopCollectCut
import com.flyjingfish.android_aop_plugin.beans.AopMatchCut
import com.flyjingfish.android_aop_plugin.beans.CutInfo
import com.flyjingfish.android_aop_plugin.beans.CutMethodJson
import com.flyjingfish.android_aop_plugin.beans.LambdaMethod
import com.flyjingfish.android_aop_plugin.beans.MethodRecord
import com.flyjingfish.android_aop_plugin.beans.ReplaceMethodInfo
import com.flyjingfish.android_aop_plugin.utils.Utils
import com.flyjingfish.android_aop_plugin.utils.Utils.getMethodInfo
import com.flyjingfish.android_aop_plugin.utils.Utils.isAOPMethod
import com.flyjingfish.android_aop_plugin.utils.Utils.slashToDot
import com.flyjingfish.android_aop_plugin.utils.Utils.slashToDotClassName
import com.flyjingfish.android_aop_plugin.utils.WovenInfoUtils
import com.flyjingfish.android_aop_plugin.utils.WovenInfoUtils.addCollectClass
import com.flyjingfish.android_aop_plugin.utils.WovenInfoUtils.getAnnoInfo
import com.flyjingfish.android_aop_plugin.utils.WovenInfoUtils.isContainAnno
import com.flyjingfish.android_aop_plugin.utils.WovenInfoUtils.isLeaf
import com.flyjingfish.android_aop_plugin.utils.instanceof
import com.flyjingfish.android_aop_plugin.utils.isHasMethodBody
import com.flyjingfish.android_aop_plugin.utils.isStaticMethod
import javassist.Modifier
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
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.coroutines.Continuation


class SearchAopMethodVisitor(val onCallBackMethod: OnCallBackMethod?) :
    ClassNode(Opcodes.ASM9) {
    private val aopMatchCuts = mutableListOf<AopMatchCut>()

    lateinit var className: String
    private var replaceInvokeClassName: String?=null
    private var replaceTargetClassName: String?=null
    private var isOverrideClass = false
    private var overrideMethodSet :MutableSet<String> = mutableSetOf()


    override fun visit(
        version: Int,
        access: Int,
        name: String,
        signature: String?,
        superName: String?,
        interfaces: Array<String>?
    ) {
        className = name
        isOverrideClass = WovenInfoUtils.isLastOverrideClassname(slashToDot(className))
        if (isOverrideClass){
            overrideMethodSet.addAll(WovenInfoUtils.getLastOverrideMethod(slashToDot(className)))
        }
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
        val isAbstractClass = access and Opcodes.ACC_ABSTRACT != 0
        WovenInfoUtils.getAopCollectInfoMap().forEach{(_,aopCollectCut) ->
            val find = if (aopCollectCut.regex.isNotEmpty()){
                val classnameArrayPattern: Pattern = Pattern.compile(aopCollectCut.regex)
                val matcher: Matcher = classnameArrayPattern.matcher(slashToDot(className))
                matcher.find()
            }else{
                true
            }
            if (find){
                val isObject = slashToDotClassName(aopCollectCut.collectClassName) == "java.lang.Object"
                var isMatchExtends = false
                if (!isObject){
                    var isDirectExtends = false
                    var isImplementsInterface = false
                    if (interfaces != null) {
                        for (anInterface in interfaces) {
                            val inter = slashToDot(anInterface)
                            if (inter == aopCollectCut.collectClassName) {
                                isImplementsInterface = true
                                break
                            }
                        }
                    }
                    if (isImplementsInterface || aopCollectCut.collectClassName == slashToDot(
                            superName!!
                        )
                    ) {
                        isDirectExtends = true
                    }
                    if (AopCollectCut.CollectType.DIRECT_EXTENDS.name == aopCollectCut.collectType) {
                        if (isDirectExtends) {
                            isMatchExtends = true
                        }
                    } else if (AopCollectCut.CollectType.LEAF_EXTENDS.name == aopCollectCut.collectType) {
                        var isExtends = false
                        if (isDirectExtends) {
                            isExtends = true
                        } else {
                            val clsName = slashToDotClassName(className)
                            val parentClsName = aopCollectCut.collectClassName
                            if (clsName != slashToDotClassName(parentClsName)) {
                                isExtends = clsName.instanceof(slashToDotClassName(parentClsName))
                            }
                        }
                        if (isExtends && isLeaf(className)) {
                            isMatchExtends = true
                        }
                    } else {
                        if (isDirectExtends) {
                            isMatchExtends = true
                        } else {
                            val clsName = slashToDotClassName(className)
                            val parentClsName = aopCollectCut.collectClassName
                            if (clsName != slashToDotClassName(parentClsName)) {
                                val isInstanceof = clsName.instanceof(slashToDotClassName(parentClsName))
                                if (isInstanceof) {
                                    isMatchExtends = true
                                }
                            }
                        }
                    }
                }else{
                    isMatchExtends = true
                }
                val isAdd = if (aopCollectCut.isClazz){
                    true
                }else !isAbstractClass

                if (isMatchExtends && isAdd){
                    addCollectClass(AopCollectClass(aopCollectCut.collectClassName,aopCollectCut.invokeClassName,aopCollectCut.invokeMethod,className,aopCollectCut.isClazz,aopCollectCut.regex,aopCollectCut.collectType))
                }
            }
        }
        val aopMatchCuts = mutableListOf<AopMatchCut>();
        //        logger.error("className="+className+",superName="+superName+",interfaces="+ Arrays.asList(interfaces));
        WovenInfoUtils.getAopMatchCuts().forEach { (_: String?, aopMatchCut: AopMatchCut) ->
            if (aopMatchCut.isPackageName()){
                if (aopMatchCut.isMatchPackageName()){
                    val clsName = slashToDotClassName(className)
                    if (aopMatchCut.isMatchPackageNameFor(clsName)){
                        val excludeClazz = aopMatchCut.excludeClass
                        var exclude = false
                        if (excludeClazz != null) {
                            for (clazz in excludeClazz) {
                                if (clsName == slashToDotClassName(clazz)) {
                                    exclude = true
                                    break
                                }
                            }
                        }
                        if (!exclude && !className.endsWith("\$\$AndroidAopClass")){
                            val isExtendMatchClassMethod = clsName.instanceof(MatchClassMethod::class.java.name)
                            val isExtendBasePointCut = clsName.instanceof(BasePointCut::class.java.name)
                            val isExtendInvokeMethod = clsName.instanceof(InvokeMethod::class.java.name)
                            val isExtendContinuation = clsName.instanceof(Continuation::class.java.name)
                            if (!isExtendMatchClassMethod && !isExtendBasePointCut && !isExtendInvokeMethod && !isExtendContinuation) {
                                aopMatchCuts.add(aopMatchCut)
                            }
                        }
                    }
                }
                return@forEach
            }
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
                                isExtends = clsName.instanceof(slashToDotClassName(parentClsName))
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
                                val isInstanceof = clsName.instanceof(slashToDotClassName(parentClsName))
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
        for (aopMatchCut in aopMatchCuts) {
            this.aopMatchCuts.add(AopMatchCut(aopMatchCut.baseClassName,aopMatchCut.methodNames.copyOf(),aopMatchCut.cutClassName,aopMatchCut.matchType,aopMatchCut.excludeClass?.copyOf(),aopMatchCut.overrideMethod))
        }
        if (aopMatchCuts.isNotEmpty()){
            onCallBackMethod?.onBackMatch(this.aopMatchCuts)
        }
        super.visit(version, access, name, signature, superName, interfaces)
    }

    override fun visitAnnotation(descriptor: String?, visible: Boolean): AnnotationVisitor? {
        return super.visitAnnotation(descriptor, visible)
    }
    companion object {
        const val REPLACE_POINT =
            "Lcom/flyjingfish/android_aop_annotation/anno/AndroidAopReplaceMethod"
        const val NEW_POINT =
            "Lcom/flyjingfish/android_aop_annotation/anno/AndroidAopReplaceNew"
        val initClassnamePattern = Pattern.compile("<init>\\(.*?\\)")
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
            if (isContainAnno(descriptor) && !isAOPMethod(methodname)) {
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
                    WovenInfoUtils.addAopMethodCutInnerClassInfo(className,methodname,methoddescriptor)
                }
            }

            if ((descriptor.contains(REPLACE_POINT) || descriptor.contains(NEW_POINT)) && replaceTargetClassName != null && access.isStaticMethod()){

                val replaceMethodInfo = ReplaceMethodInfo(
                    replaceTargetClassName!!,"","",
                    className,methodname,methoddescriptor
                )
                if (descriptor.contains(NEW_POINT)){
                    replaceMethodInfo.replaceType = ReplaceMethodInfo.ReplaceType.NEW
                    val returnType = Type.getReturnType(replaceMethodInfo.newMethodDesc)
                    val returnTypeDescriptor = returnType.descriptor
                    val returnTypeClassName = returnType.className
                    val paramsTypes = Type.getArgumentTypes(replaceMethodInfo.newMethodDesc)
                    val paramType0 : Type? = if (paramsTypes.size == 1){
                        paramsTypes[0]
                    }else null

                    val paramType0Descriptor = paramType0?.descriptor ?:""
                    val paramType0ClassName = paramType0?.className ?:""

                    if (paramType0Descriptor.startsWith("L")
                        && paramType0Descriptor.endsWith(";")
                        && (returnTypeDescriptor == "V" || (returnTypeDescriptor.startsWith("L") && returnTypeDescriptor.endsWith(";")))){
                        replaceMethodInfo.oldMethodName = "<init>"
                        replaceMethodInfo.newClassName = paramType0Descriptor.substring(1,paramType0Descriptor.length - 1)
                        onCallBackMethod?.onBackReplaceMethodInfo(replaceMethodInfo)
                    }
                }else{
                    return ReplaceMethodVisitor(replaceMethodInfo)
                }
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
                        val fanMatcher = initClassnamePattern.matcher(name)
                        val newName = if (name.startsWith("<init>") && fanMatcher.find()){
                            if (replaceMethodInfo.replaceType == ReplaceMethodInfo.ReplaceType.METHOD){
                                replaceMethodInfo.replaceType = ReplaceMethodInfo.ReplaceType.INIT
                            }
                            "void $name"
                        }else{
                            name
                        }

                        val methodInfo = getMethodInfo(newName)
                        if (methodInfo != null && methodInfo.checkAvailable()){
                            val methodText = methodInfo.returnType + " " + methodInfo.name + methodInfo.paramTypes

                            val method = Method.getMethod(methodText)
                            replaceMethodInfo.oldMethodName = method.name
                            replaceMethodInfo.oldMethodDesc = method.descriptor
                            if (replaceMethodInfo.checkAvailable()){
                                if (replaceMethodInfo.replaceType == ReplaceMethodInfo.ReplaceType.INIT){
                                    val returnType = Type.getReturnType(replaceMethodInfo.newMethodDesc)
                                    val returnTypeDescriptor = returnType.descriptor
                                    val returnTypeClassName = returnType.className
                                    val paramsTypes = Type.getArgumentTypes(replaceMethodInfo.newMethodDesc)
                                    val paramType0 : Type? = if (paramsTypes.size == 1){
                                        paramsTypes[0]
                                    }else null

                                    val newReplace = Type.getArgumentTypes(replaceMethodInfo.newMethodDesc).joinToString("")
                                    val oldReplace = Type.getArgumentTypes(replaceMethodInfo.oldMethodDesc).joinToString("")

                                    if (returnTypeDescriptor.startsWith("L") && returnTypeDescriptor.endsWith(";")
                                        && (paramType0?.className == slashToDotClassName(replaceMethodInfo.oldOwner)||newReplace == oldReplace)
                                        && (returnTypeClassName == slashToDotClassName(replaceMethodInfo.oldOwner) || slashToDotClassName(returnTypeClassName).instanceof(slashToDotClassName(replaceMethodInfo.oldOwner)))){
                                        onCallBackMethod?.onBackReplaceMethodInfo(replaceMethodInfo)
                                    }
                                } else{
                                    onCallBackMethod?.onBackReplaceMethodInfo(replaceMethodInfo)
                                }
                            }
                        }


                    } catch (_: Exception) {

                    }
                }
            }
        }

        private val isSuspend: Boolean
        private var invokeClassName: String? = null
        private var invokeClassNameCount = 0

        init {
            val isSuspendMethod: Boolean =
            methoddescriptor.endsWith("Lkotlin/coroutines/Continuation;)Ljava/lang/Object;")
            isSuspend = (isSuspendMethod && !slashToDot(className).instanceof(
                MatchClassMethodSuspend::class.java.name)
                    &&!slashToDot(className).instanceof(BasePointCutSuspend::class.java.name))
        }


        override fun visitMethodInsn(
            opcode: Int,
            owner: String,
            name: String,
            descriptor: String,
            isInterface: Boolean
        ) {

            if (isSuspend && name == "<init>" && owner.startsWith("$className$")) {
                invokeClassName = owner
                invokeClassNameCount++
            }
            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface)
        }

        override fun visitEnd() {
            super.visitEnd()
            val suspendClassName = invokeClassName
            if (isSuspend && suspendClassName != null) {
                WovenInfoUtils.addAopMethodCutInnerClassInfoInvokeClassName(suspendClassName,invokeClassNameCount)
            }
        }
    }
    private fun isBackMethod(access: Int):Boolean{
        return access.isHasMethodBody()
    }
    override fun visitMethod(
        access: Int, name: String, descriptor: String,
        signature: String?, exceptions: Array<String?>?
    ): MethodVisitor {
        if (isOverrideClass
            && !Modifier.isStatic(access)
            && !Modifier.isFinal(access)
            && !Modifier.isPrivate(access)){
            overrideMethodSet.remove("${slashToDot(className)}@$name@$descriptor")
        }
        if ("<init>" != name && "<clinit>" != name && aopMatchCuts.size > 0 && isBackMethod(access) && !isAOPMethod(name)) {
            for (aopMatchCut in aopMatchCuts) {
                fun addMatchMethodCut(){
                    val methodRecord = MethodRecord(
                        name,
                        descriptor,
                        mutableSetOf(aopMatchCut.cutClassName)
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

                    WovenInfoUtils.addAopMethodCutInnerClassInfo(className,name,descriptor)
                }
//                printLog("$aopMatchCut === ${aopMatchCut.isMatchAllMethod()}")
                if (aopMatchCut.isMatchAllMethod()){
                    addMatchMethodCut()
                }else{
                    val removeIndex = mutableSetOf<Int>()
                    for ((index,methodName) in aopMatchCut.methodNames.withIndex()) {
                        val matchMethodInfo = getMethodInfo(methodName)
                        if (matchMethodInfo != null && name == matchMethodInfo.name) {
                            val isBack = try {
                                Utils.verifyMatchCut(descriptor,matchMethodInfo)
                            } catch (e: Exception) {
                                true
                            }
                            if (isBack) {
                                addMatchMethodCut()
                                removeIndex.add(index)
                                //                            cacheMethodRecords.add(new MethodRecord(name, descriptor, aopMatchCut.getCutClassName()));
                            }
                        }
                    }
                    for (index in removeIndex) {
                        aopMatchCut.methodNames[index] = "@null"
                    }
                }


            }
        }
        val myMethodVisitor = MyMethodVisitor(
            access, name, descriptor, signature, exceptions, MethodRecord(name, descriptor)
        )
        methods.add(myMethodVisitor)
        return myMethodVisitor
    }

    private val lambdaMethodList = mutableListOf<LambdaMethod>()


    override fun visitEnd() {
        super.visitEnd()
        if (isOverrideClass && overrideMethodSet.isNotEmpty()){
            onCallBackMethod?.onThrowOverrideMethod(slashToDot(className),overrideMethodSet)
        }

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
                    if (bsmArgs[0] is Type && bsmArgs[1] is Handle){
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
            }

            if (lambdaMethodList.size > 0) {
                for ((_, _, _, _, lambdaName, lambdaDesc) in lambdaMethodList) {
                    for (aopMatchCut in aopMatchCuts) {
                        if (aopMatchCut.isMatchAllMethod()){
                            val methodRecord = MethodRecord(
                                lambdaName,
                                lambdaDesc,
                                mutableSetOf(aopMatchCut.cutClassName)
                            )
                            onCallBackMethod?.onDeleteMethodRecord(methodRecord)
                        }
                    }
                }

                WovenInfoUtils.getAopMatchCuts().forEach { (_: String?, aopMatchCut: AopMatchCut) ->
                    if (AopMatchCut.MatchType.SELF.name != aopMatchCut.matchType && aopMatchCut.methodNames.size == 1) {
                        for ((name, descriptor, clsName, originalClassName, lambdaName, lambdaDesc) in lambdaMethodList) {
                            if ("<init>" != name && "<clinit>" != name && "<init>" != lambdaName && "<clinit>" != lambdaName && !isAOPMethod(name)){
                                val isDirectExtends =
                                    slashToDotClassName(aopMatchCut.baseClassName) == clsName
                                var isMatch = false
                                if (AopMatchCut.MatchType.DIRECT_EXTENDS.name == aopMatchCut.matchType) {
                                    isMatch = isDirectExtends
                                } else if (AopMatchCut.MatchType.EXTENDS.name == aopMatchCut.matchType || AopMatchCut.MatchType.LEAF_EXTENDS.name == aopMatchCut.matchType) {
                                    isMatch = isDirectExtends
                                    if (!isMatch) {
                                        val parentClsName = aopMatchCut.baseClassName
                                        isMatch = clsName.instanceof(slashToDotClassName(parentClsName))
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
                                            mutableSetOf(aopMatchCut.cutClassName),
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
        fun onBackMatch(aopMatchCuts: List<AopMatchCut>)
        fun onBackMethodRecord(methodRecord: MethodRecord)
        fun onDeleteMethodRecord(methodRecord: MethodRecord)
        fun onBackReplaceMethodInfo(replaceMethodInfo: ReplaceMethodInfo)
        fun onThrowOverrideMethod(className:String,overrideMethods:Set<String> )
    }
}