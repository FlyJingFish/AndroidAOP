package com.flyjingfish.android_aop_plugin.scanner_visitor;

import com.flyjingfish.android_aop_plugin.beans.AopMatchCut;
import com.flyjingfish.android_aop_plugin.beans.LambdaMethod;
import com.flyjingfish.android_aop_plugin.beans.MatchMethodInfo;
import com.flyjingfish.android_aop_plugin.beans.MethodRecord;
import com.flyjingfish.android_aop_plugin.utils.ClassPoolUtils;
import com.flyjingfish.android_aop_plugin.utils.Utils;
import com.flyjingfish.android_aop_plugin.utils.WovenInfoUtils;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.slf4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.MethodInfo;

public class AnnotationMethodScanner extends ClassNode {
    Logger logger;
    private final OnCallBackMethod onCallBackMethod;
//    private final byte[] classByte;
    private boolean isDescendantClass;
    private final List<AopMatchCut> aopMatchCuts = new ArrayList<>();
    private final List<MethodRecord> cacheMethodRecords = new ArrayList<>();
    private String className;

    public AnnotationMethodScanner(Logger logger,OnCallBackMethod onCallBackMethod) {
        super(Opcodes.ASM9);
        this.logger = logger;
        this.onCallBackMethod = onCallBackMethod;
//        this.classByte = classByte;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        className = name;
//        logger.error("className="+className+",superName="+superName+",interfaces="+ Arrays.asList(interfaces));
        WovenInfoUtils.INSTANCE.getAopMatchCuts().forEach((key, aopMatchCut) -> {
            String[] excludeClazz = aopMatchCut.getExcludeClass();
            boolean exclude = false;
            boolean isSubType = false;
            if (excludeClazz != null){
                String clsName = Utils.INSTANCE.slashToDot(className).replaceAll("\\$", ".");
                for (String clazz : excludeClazz) {
                    if (clsName.equals(clazz)){
                        exclude = true;
                        break;
                    }
                }
            }
            if (!exclude){
                boolean isImplementsInterface = false;
                if (interfaces != null) {
                    for (String anInterface : interfaces) {
                        String inter = Utils.INSTANCE.slashToDot(anInterface).replaceAll("\\$", ".");
                        if (inter.equals(aopMatchCut.getBaseClassName()) && AopMatchCut.MatchType.EXTENDS.name().equals(aopMatchCut.getMatchType())) {
                            isImplementsInterface = true;
                            break;
                        }
                    }
                }

                if (isImplementsInterface
                        || (AopMatchCut.MatchType.EXTENDS.name().equals(aopMatchCut.getMatchType()) && aopMatchCut.getBaseClassName().equals(Utils.INSTANCE.slashToDot(superName).replaceAll("\\$", ".")))
                        ) {
                    isSubType = true;
                    this.isDescendantClass = true;
                    AnnotationMethodScanner.this.aopMatchCuts.add(aopMatchCut);
                }
            }
            if (!isSubType){
                String clsName = Utils.INSTANCE.slashToDot(className).replaceAll("\\$", ".");
                String parentClsName = aopMatchCut.getBaseClassName();
                if (!exclude && AopMatchCut.MatchType.EXTENDS.name().equals(aopMatchCut.getMatchType())
                        && !clsName.equals(parentClsName)) {
                    try {

                        boolean isInstanceof = Utils.INSTANCE.isInstanceof(Utils.INSTANCE.slashToDot(className).replaceAll("\\$","."),parentClsName);
//                        logger.error("isInstanceof="+isInstanceof);
                        if (isInstanceof){
                            this.isDescendantClass = true;
                            AnnotationMethodScanner.this.aopMatchCuts.add(aopMatchCut);
                        }

                    } catch (NotFoundException e) {
                        e.printStackTrace();
                    }

                }
            }
            if ((AopMatchCut.MatchType.SELF.name().equals(aopMatchCut.getMatchType()) && aopMatchCut.getBaseClassName().equals(Utils.INSTANCE.slashToDot(name).replaceAll("\\$", ".")))) {
                this.isDescendantClass = true;
                AnnotationMethodScanner.this.aopMatchCuts.add(aopMatchCut);
            }
        });
        super.visit(version, access, name, signature, superName, interfaces);

    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        return super.visitAnnotation(descriptor, visible);
    }

    class MyMethodVisitor extends MethodNode {
        MethodRecord methodName;

//        MyMethodVisitor(MethodRecord methodName) {
//            super(Opcodes.ASM8);
//            this.methodName = methodName;
//        }

        public MyMethodVisitor(
                final int access,
                final String name,
                final String descriptor,
                final String signature,
                final String[] exceptions,MethodRecord methodName) {
            super(/* latest api = */ Opcodes.ASM9, access, name, descriptor, signature, exceptions);
            this.methodName = methodName;
        }

        @Override
        public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
//            logger.error("AnnotationMethodScanner MyMethodVisitor type: " + descriptor);
            if (WovenInfoUtils.INSTANCE.isContainAnno(descriptor)) {
                boolean isBack = true;
                try {
                    ClassPool classPool = ClassPoolUtils.INSTANCE.getClassPool();
//                    InputStream byteArrayInputStream = new ByteArrayInputStream(classByte);
//                    CtClass ctClass = classPool.makeClass(byteArrayInputStream);
                    String clsName = Utils.INSTANCE.slashToDot(className.replaceAll("\\.class",""));
                    CtClass ctClass =  classPool.get(clsName);
                    CtMethod ctMethod = WovenIntoCode.INSTANCE.getCtMethod(ctClass, methodName.getMethodName(), methodName.getDescriptor());
                    MethodInfo methodInfo = ctMethod.getMethodInfo();
                    CodeAttribute codeAttribute = methodInfo.getCodeAttribute();
                    if (codeAttribute == null) {
                        isBack = false;
                    }
                } catch (Exception ignored) {
                }
                if (onCallBackMethod != null && isBack) {
                    onCallBackMethod.onBackName(methodName);
                }
            }
            return super.visitAnnotation(descriptor, visible);
        }
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor,
                                     String signature, String[] exceptions) {

        if (isDescendantClass) {
            for (AopMatchCut aopMatchCut : aopMatchCuts) {
                for (String methodName : aopMatchCut.getMethodNames()) {
                    MatchMethodInfo matchMethodInfo = Utils.INSTANCE.getMethodInfo(methodName);
                    if (matchMethodInfo != null && name.equals(matchMethodInfo.getName())) {
                        boolean isBack = true;
                        try {
                            ClassPool classPool = ClassPoolUtils.INSTANCE.getClassPool();
                            String clsName = Utils.INSTANCE.slashToDot(className.replaceAll("\\.class",""));
                            CtClass ctClass =  classPool.get(clsName);
//                            InputStream byteArrayInputStream = new ByteArrayInputStream(classByte);
//                            CtClass ctClass = classPool.makeClass(byteArrayInputStream);
                            CtMethod ctMethod = WovenIntoCode.INSTANCE.getCtMethod(ctClass, name, descriptor);
                            if (matchMethodInfo.getParamTypes() != null) {
                                CtClass[] ctClasses = ctMethod.getParameterTypes();
                                StringBuilder paramStr = new StringBuilder();
                                paramStr.append("(");
                                int length = ctClasses.length;
                                for (int i = 0; i < length; i++) {
                                    paramStr.append(ctClasses[i].getName());
                                    if (i != length - 1) {
                                        paramStr.append(",");
                                    }
                                }
                                paramStr.append(")");
                                //有设置参数类型这一项
                                if (!paramStr.toString().equals(matchMethodInfo.getParamTypes())) {
                                    isBack = false;
                                }
                            }
                            if (matchMethodInfo.getReturnType() != null) {
                                CtClass returnCtClass = ctMethod.getReturnType();
                                String returnType = returnCtClass.getName();
                                //有设置返回类型这一项
                                if (!returnType.equals(matchMethodInfo.getReturnType())) {
                                    isBack = false;
                                }
                            }
//                            logger.error("paramStr="+paramStr+",returnType="+returnType+",matchMethodInfo="+matchMethodInfo);
                            MethodInfo methodInfo = ctMethod.getMethodInfo();
                            CodeAttribute codeAttribute = methodInfo.getCodeAttribute();
                            if (codeAttribute == null) {
                                isBack = false;
                            }
                        } catch (Exception ignored) {
                        }
                        if (isBack) {
                            cacheMethodRecords.add(new MethodRecord(name, descriptor, aopMatchCut.getCutClassName()));
                        }
                        break;
                    }
                }
            }
        }
        MyMethodVisitor myMethodVisitor = new MyMethodVisitor(access,name,descriptor,signature,exceptions,new MethodRecord(name, descriptor, null));
        methods.add(myMethodVisitor);
        return myMethodVisitor;
    }
    List<LambdaMethod> lambdaMethodList = new ArrayList<>();
    @Override
    public void visitEnd() {
        super.visitEnd();

        this.methods.forEach(methodNode -> {
//            logger.error(className+"======methods.forEach=");
            for (AbstractInsnNode node : methodNode.instructions) {
                if (node instanceof InvokeDynamicInsnNode tmpNode) {
                    String desc = tmpNode.desc;
                    Type descType = Type.getType(desc);
                    Type samBaseType = descType.getReturnType();
                    //sam 接口名
                    String samBase = samBaseType.getDescriptor();
                    //sam 方法名
                    String samMethodName = tmpNode.name;
                    Object[] bsmArgs = tmpNode.bsmArgs;
                            Type samMethodType = (Type) bsmArgs[0];
                    Handle methodName = (Handle) bsmArgs[1];
                    //sam 实现方法实际参数描述符
//                            Type implMethodType = (Type) bsmArgs[2];

                    String lambdaName = methodName.getName();
//                    String bsmMethodNameAndDescriptor = samMethodName + samMethodType.getDescriptor();
                    String thisClassName = Utils.INSTANCE.slashToDot(samBase.substring(1).replaceAll(";","")).replaceAll("\\$", ".");
                    String originalClassName = Utils.INSTANCE.slashToDot(samBase.substring(1).replaceAll(";",""));
//                    logger.error("className="+className+",tmpNode.name="+tmpNode.name+",desc=" + desc + ",samBase=" + samBase + ",samMethodName="
//                            + samMethodName + ",methodName=" + lambdaName+ ",methodDesc=" + methodName.getDesc()+",thisClassName="+thisClassName+
//                            ",getDescriptor"+samMethodType.getDescriptor());
                    String lambdaDesc = methodName.getDesc();
                    String samMethodDesc = samMethodType.getDescriptor();
                    LambdaMethod lambdaMethod = new LambdaMethod(samMethodName,samMethodDesc,thisClassName,originalClassName,lambdaName,lambdaDesc);
                    lambdaMethodList.add(lambdaMethod);
                }

            }
            if (lambdaMethodList.size() > 0){
//            logger.error(className+"======lambdaMethodList="+lambdaMethodList);
                WovenInfoUtils.INSTANCE.getAopMatchCuts().forEach((key, aopMatchCut) -> {
//                if (className.contains("MainActivity")){
//                    logger.error("======aopMatchCut="+aopMatchCut+"=size"+WovenInfoUtils.INSTANCE.getAopMatchCuts().size());
//                }
                    if (AopMatchCut.MatchType.EXTENDS.name().equals(aopMatchCut.getMatchType()) && aopMatchCut.getMethodNames().length == 1){
                        for (LambdaMethod lambdaMethod : lambdaMethodList) {
                            boolean subType = aopMatchCut.getBaseClassName().equals(lambdaMethod.getThisClassName());
//                            logger.error("lambdaMethod="+lambdaMethod+",parent="+parent);
                            if (!subType){
                                String clsName = lambdaMethod.getThisClassName();
                                String parentClsName = aopMatchCut.getBaseClassName();
                                try {
                                    subType = Utils.INSTANCE.isInstanceof(clsName,parentClsName);
//                                        logger.error("className="+className+"lambdaMethod="+lambdaMethod+",isInstanceof="+parent);

                                } catch (NotFoundException e) {
                                    e.printStackTrace();
                                }
                            }
                            String name = lambdaMethod.getSamMethodName();
                            String descriptor = lambdaMethod.getSamMethodDesc();
                            String aopMatchCutMethodName = aopMatchCut.getMethodNames()[0];

                            MatchMethodInfo matchMethodInfo = Utils.INSTANCE.getMethodInfo(aopMatchCutMethodName);
                            if (subType && matchMethodInfo != null && name.equals(matchMethodInfo.getName())) {
                                boolean isBack = true;
                                try {
                                    ClassPool classPool = ClassPoolUtils.INSTANCE.getClassPool();
                                    CtMethod ctMethod = null;
                                    if (matchMethodInfo.getParamTypes() != null || matchMethodInfo.getReturnType() != null) {
                                        CtClass ctClass = classPool.get(lambdaMethod.getOriginalClassName());
                                        ctMethod = WovenIntoCode.INSTANCE.getCtMethod(ctClass, name, descriptor);
                                    }
                                    if (matchMethodInfo.getParamTypes() != null && ctMethod != null) {
                                        CtClass[] ctClasses = ctMethod.getParameterTypes();
                                        StringBuilder paramStr = new StringBuilder();
                                        paramStr.append("(");
                                        int length = ctClasses.length;
                                        for (int i = 0; i < length; i++) {
                                            paramStr.append(ctClasses[i].getName());
                                            if (i != length - 1) {
                                                paramStr.append(",");
                                            }
                                        }
                                        paramStr.append(")");
                                        //有设置参数类型这一项
                                        if (!paramStr.toString().equals(matchMethodInfo.getParamTypes())) {
                                            isBack = false;
                                        }
                                    }
                                    if (matchMethodInfo.getReturnType() != null && ctMethod != null) {
                                        CtClass returnCtClass = ctMethod.getReturnType();
                                        String returnType = returnCtClass.getName();
                                        //有设置返回类型这一项
                                        if (!returnType.equals(matchMethodInfo.getReturnType())) {
                                            isBack = false;
                                        }
                                    }
//                            logger.error("paramStr="+paramStr+",returnType="+returnType+",matchMethodInfo="+matchMethodInfo);
                                } catch (Exception ignored) {
//                                    ignored.printStackTrace();
                                }
                                if (isBack) {
                                    MethodRecord methodRecord = new MethodRecord(lambdaMethod.getLambdaName(), lambdaMethod.getLambdaDesc(), aopMatchCut.getCutClassName());
                                    if (onCallBackMethod != null) {
                                        onCallBackMethod.onBackName(methodRecord);
                                    }
                                }
                            }


//                            if (lambdaMethod.getSamMethodName().equals(aopMatchCutMethodName) && aopMatchCut.getBaseClassName().equals(lambdaMethod.getThisClassName())){
//                                MethodRecord methodRecord = new MethodRecord(lambdaMethod.getLambdaName(), lambdaMethod.getLambdaDesc(), aopMatchCut.getCutClassName());
////                            logger.error("======methodRecord="+methodRecord);
////                                    logger.error("======aopMatchCut="+aopMatchCut.getMatchType()+"=="+ AopMatchCut.MatchType.EXTENDS.name());
//                                if (onCallBackMethod != null) {
//                                    onCallBackMethod.onBackName(methodRecord);
//                                }
//                            }
                        }
                    }
                });
            }
        });


        if (isDescendantClass) {
            for (MethodRecord cacheMethodRecord : cacheMethodRecords) {
                if (onCallBackMethod != null) {
                    onCallBackMethod.onBackName(cacheMethodRecord);
                }
            }
        }



    }



    public interface OnCallBackMethod {
        void onBackName(MethodRecord methodRecord);
    }
}