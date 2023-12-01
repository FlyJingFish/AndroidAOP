package com.flyjingfish.android_aop_plugin.scanner_visitor;

import com.flyjingfish.android_aop_plugin.beans.AopMatchCut;
import com.flyjingfish.android_aop_plugin.beans.LambdaMethod;
import com.flyjingfish.android_aop_plugin.beans.MatchMethodInfo;
import com.flyjingfish.android_aop_plugin.beans.MethodRecord;
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
    private final byte[] classByte;
    private boolean isDescendantClass;
    private List<AopMatchCut> aopMatchCuts = new ArrayList<>();
    private final List<MethodRecord> cacheMethodRecords = new ArrayList<>();
    private String className;

    public AnnotationMethodScanner(Logger logger, byte[] classByte, OnCallBackMethod onCallBackMethod) {
        super(Opcodes.ASM9);
        this.logger = logger;
        this.onCallBackMethod = onCallBackMethod;
        this.classByte = classByte;
//        classPool = ClassPoolUtils.INSTANCE.getClassPool();
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        className = name;
//        if (name.contains("TestMatch")){
//
//            logger.error("name="+name+",superName="+superName+",interfaces="+new ArrayList<>(Arrays.asList(interfaces)));
//        }
        WovenInfoUtils.INSTANCE.getAopMatchCuts().forEach((key, aopMatchCut) -> {
//                try{
//                    ClassPool cp = new ClassPool(null);
//                    cp.appendSystemPath();
//                    for (String classPath : WovenInfoUtils.INSTANCE.getClassPaths()) {
//                        cp.appendClassPath(classPath);
//                    }
//
//                    CtClass superClass = cp.getCtClass(Utils.INSTANCE.slashToDot(superName));
//                    int index = 0;
//                    do {
//                        if (aopMatchCut.getBaseClassName().equals(superClass.getName())) {
//                            this.isDescendantClass= true;
//                            AnnotationMethodScanner.this.aopMatchCut = aopMatchCut;
//
//                            break;
//                        }
//                        if (index > 3){
//                            break;
//                        }
//                        index++;
//                        superClass = superClass.getSuperclass();
//                    } while (superClass != null);
//                }catch (Exception e){
//                    e.printStackTrace();
//                }
//        ClassPool cp = ClassPool.getDefault();
            boolean isContainInterface = false;
            if (interfaces != null) {
                for (String anInterface : interfaces) {
                    String inter = Utils.INSTANCE.slashToDot(anInterface).replaceAll("\\$", ".");
                    if (inter.equals(aopMatchCut.getBaseClassName())) {
                        isContainInterface = true;
                        break;
                    }
                }
            }

            if (isContainInterface || (AopMatchCut.MatchType.EXTENDS.name().equals(aopMatchCut.getMatchType()) && aopMatchCut.getBaseClassName().equals(Utils.INSTANCE.slashToDot(superName)))
                    || (AopMatchCut.MatchType.SELF.name().equals(aopMatchCut.getMatchType()) && aopMatchCut.getBaseClassName().equals(Utils.INSTANCE.slashToDot(name)))) {
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
                    ClassPool classPool = new ClassPool(null);
                    InputStream byteArrayInputStream = new ByteArrayInputStream(classByte);
                    CtClass ctClass = classPool.makeClass(byteArrayInputStream);
                    CtMethod ctMethod = getCtMethod(ctClass, methodName.getMethodName(), methodName.getDescriptor());
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

    public static CtMethod getCtMethod(CtClass ctClass, String methodName, String descriptor) throws NotFoundException {
        CtMethod[] ctMethods = ctClass.getDeclaredMethods(methodName);
        if (ctMethods != null && ctMethods.length > 0) {
            for (CtMethod ctMethod : ctMethods) {
                String allSignature = ctMethod.getSignature();
                if (descriptor.equals(allSignature)) {
                    return ctMethod;
                }
            }
        }
        return null;
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
                            ClassPool classPool = new ClassPool(null);
                            if (matchMethodInfo.getParamTypes() != null || matchMethodInfo.getReturnType() != null) {
                                classPool.appendSystemPath();
                                for (String classPath : WovenInfoUtils.INSTANCE.getClassPaths()) {
                                    try {
                                        classPool.appendClassPath(classPath);
                                    } catch (NotFoundException ignored) {
                                    }
                                }
                            }
                            InputStream byteArrayInputStream = new ByteArrayInputStream(classByte);
                            CtClass ctClass = classPool.makeClass(byteArrayInputStream);
                            CtMethod ctMethod = getCtMethod(ctClass, name, descriptor);
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
                            ignored.printStackTrace();
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
//        WovenInfoUtils.INSTANCE.getAopMatchCuts().forEach((key, aopMatchCut) -> {
//            if (className.contains("MainActivity")){
//                logger.error("======aopMatchCut="+aopMatchCut+"=size"+WovenInfoUtils.INSTANCE.getAopMatchCuts().size());
//            }
//            if (aopMatchCut.getMethodNames().length == 1){
//                this.methods.forEach(methodNode -> {
//                    for (AbstractInsnNode node : methodNode.instructions) {
//                        if (node instanceof InvokeDynamicInsnNode tmpNode) {
//                            String desc = tmpNode.desc;
//                            Type descType = Type.getType(desc);
//                            Type samBaseType = descType.getReturnType();
//                            //sam 接口名
//                            String samBase = samBaseType.getDescriptor();
//                            //sam 方法名
//                            String samMethodName = tmpNode.name;
//                            Object[] bsmArgs = tmpNode.bsmArgs;
////                            Type samMethodType = (Type) bsmArgs[0];
//                            Handle methodName = (Handle) bsmArgs[1];
//                            //sam 实现方法实际参数描述符
////                            Type implMethodType = (Type) bsmArgs[2];
//
//                            String lambdaName = methodName.getName();
////                            String bsmMethodNameAndDescriptor = samMethodName + samMethodType.getDescriptor();
//                            String thisClassName = Utils.INSTANCE.slashToDot(samBase.substring(1).replaceAll(";","")).replaceAll("\\$", ".");
//                            logger.error("className="+className+",tmpNode.name="+tmpNode.name+",desc=" + desc + ",samBase=" + samBase + ",samMethodName="
//                                    + samMethodName + ",methodName=" + lambdaName+ ",methodDesc=" + methodName.getDesc()+",thisClassName="+thisClassName);
//                            for (String aopMatchCutMethodName : aopMatchCut.getMethodNames()) {
//                                logger.error("======aopMatchCutMethodName="+aopMatchCutMethodName+"=aopMatchCut.getBaseClassName()="+aopMatchCut.getBaseClassName()+"=");
//                                if (samMethodName.equals(aopMatchCutMethodName) && aopMatchCut.getBaseClassName().equals(thisClassName)){
//                                    MethodRecord methodRecord = new MethodRecord(lambdaName, methodName.getDesc(), aopMatchCut.getCutClassName());
//                                    logger.error("======methodRecord="+methodRecord);
////                                    logger.error("======aopMatchCut="+aopMatchCut.getMatchType()+"=="+ AopMatchCut.MatchType.EXTENDS.name());
//                                    if (onCallBackMethod != null) {
//                                        onCallBackMethod.onBackName(methodRecord);
//                                    }
//                                }
//                            }
//
//                        }
//
//                    }
//
//                });
//            }
//        });

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
//                            Type samMethodType = (Type) bsmArgs[0];
                    Handle methodName = (Handle) bsmArgs[1];
                    //sam 实现方法实际参数描述符
//                            Type implMethodType = (Type) bsmArgs[2];

                    String lambdaName = methodName.getName();
//                            String bsmMethodNameAndDescriptor = samMethodName + samMethodType.getDescriptor();
                    String thisClassName = Utils.INSTANCE.slashToDot(samBase.substring(1).replaceAll(";","")).replaceAll("\\$", ".");
//                    logger.error("className="+className+",tmpNode.name="+tmpNode.name+",desc=" + desc + ",samBase=" + samBase + ",samMethodName="
//                            + samMethodName + ",methodName=" + lambdaName+ ",methodDesc=" + methodName.getDesc()+",thisClassName="+thisClassName);
                    String lambdaDesc = methodName.getDesc();
                    LambdaMethod lambdaMethod = new LambdaMethod(samMethodName,thisClassName,lambdaName,lambdaDesc);
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
                            String aopMatchCutMethodName = aopMatchCut.getMethodNames()[0];
                            if (lambdaMethod.getSamMethodName().equals(aopMatchCutMethodName) && aopMatchCut.getBaseClassName().equals(lambdaMethod.getThisClassName())){
                                MethodRecord methodRecord = new MethodRecord(lambdaMethod.getLambdaName(), lambdaMethod.getLambdaDesc(), aopMatchCut.getCutClassName());
//                            logger.error("======methodRecord="+methodRecord);
//                                    logger.error("======aopMatchCut="+aopMatchCut.getMatchType()+"=="+ AopMatchCut.MatchType.EXTENDS.name());
                                if (onCallBackMethod != null) {
                                    onCallBackMethod.onBackName(methodRecord);
                                }
                            }
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