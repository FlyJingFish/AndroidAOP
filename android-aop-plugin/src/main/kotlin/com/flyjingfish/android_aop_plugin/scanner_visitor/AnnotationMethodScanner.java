package com.flyjingfish.android_aop_plugin.scanner_visitor;

import com.flyjingfish.android_aop_plugin.beans.AopMatchCut;
import com.flyjingfish.android_aop_plugin.beans.MatchMethodInfo;
import com.flyjingfish.android_aop_plugin.beans.MethodRecord;
import com.flyjingfish.android_aop_plugin.utils.Utils;
import com.flyjingfish.android_aop_plugin.utils.WovenInfoUtils;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
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
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.atomic.AtomicInteger;

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
    public void visitInnerClass(String name, String outerName, String innerName, int access) {
        super.visitInnerClass(name, outerName, innerName, access);
        if (name.contains("MainActivity")) {
            logger.error("name=" + name + ",outerName=" + outerName + ",innerName=" + innerName);
        }
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
private final 
    AtomicInteger counter = new AtomicInteger(0);
    @Override
    public void visitEnd() {
        super.visitEnd();
        if (className.contains("MainActivity")) {
            logger.error("visitEnd=1");

            this.methods.forEach(methodNode -> {
                logger.error("visitEnd=2=name="+methodNode.name);
                for (AbstractInsnNode node : methodNode.instructions) {
                    logger.error("visitEnd=3="+node);
                    if (node instanceof InvokeDynamicInsnNode) {
                        InvokeDynamicInsnNode tmpNode = (InvokeDynamicInsnNode) node;
//形如：(Ljava/util/Date;)Ljava/util/function/Consumer;   可以从 desc 中获取函数式接口，以及动态参数的内容。
//如果没有参数那么描述符的参数部分应该是空。

                        String desc = tmpNode.desc;
                        Type descType = Type.getType(desc);
                        Type samBaseType = descType.getReturnType();
//sam 接口名
                        String samBase = samBaseType.getDescriptor();
//sam 方法名
                        String samMethodName = tmpNode.name;
                        Object[] bsmArgs = tmpNode.bsmArgs;
//sam 方法描述符

                        Type samMethodType = (Type) bsmArgs[0];

//sam 实现方法实际参数描述符

                        Type implMethodType = (Type) bsmArgs[2];

//sam name + desc，可以用来辨别是否是需要 Hook 的 lambda 表达式

                        String bsmMethodNameAndDescriptor = samMethodName + samMethodType.getDescriptor();

//中间方法的名称

                        String middleMethodName = "lambda$" + samMethodName + "$sa" + counter.incrementAndGet();
                        logger.error("tmpNode.name="+tmpNode.name+",desc=" + desc + ",samBase=" + samBase + ",samMethodName=" + samMethodName + ",bsmMethodNameAndDescriptor=" + bsmMethodNameAndDescriptor+ ",middleMethodName=" + middleMethodName);
//
////中间方法的描述符
//
//                    String middleMethodDesc = "";
//
//                    Type[] descArgTypes = descType.getArgumentTypes();
//
//                    if (descArgTypes.length == 0) {
//
//                        middleMethodDesc = implMethodType.getDescriptor();
//
//                    } else {
//
//                        middleMethodDesc = "(";
//
//                        for (Type tmpType : descArgTypes) {
//
//                            middleMethodDesc += tmpType.getDescriptor();
//
//                        }
//
//                        middleMethodDesc += implMethodType.getDescriptor().replace("(", "");
//
//                    }
//
////INDY 原本的 handle，需要将此 handle 替换成新的 handle
//
//                    Handle oldHandle = (Handle) bsmArgs[1];
//
//                    Handle newHandle = new Handle(Opcodes.H_INVOKESTATIC, this.name, middleMethodName, middleMethodDesc, false);
//
//                    InvokeDynamicInsnNode newDynamicNode = new InvokeDynamicInsnNode(tmpNode.name, tmpNode.desc, tmpNode.bsm, samMethodType, newHandle, implMethodType);
//
//                    iterator.remove();
//
//                    iterator.add(newDynamicNode);
//
//                    generateMiddleMethod(oldHandle, middleMethodName, middleMethodDesc);

                    }

                }

            });
        }

//        this.methods.addAll(syntheticMethodList);

//        accept(cv);


        if (isDescendantClass) {
            for (MethodRecord cacheMethodRecord : cacheMethodRecords) {
                if (onCallBackMethod != null) {
                    logger.error("cacheMethodRecord=" + cacheMethodRecord);
                    onCallBackMethod.onBackName(cacheMethodRecord);
                }
            }
        }



    }
    private void generateMiddleMethod (Handle oldHandle, String middleMethodName, String
            middleMethodDesc){

////开始对生成的方法中插入或者调用相应的代码
//
//        MethodNode methodNode = new MethodNode(Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC /*| Opcodes.ACC_SYNTHETIC*/,
//
//                middleMethodName, middleMethodDesc, null, null);
//
//        methodNode.visitCode();
//
//// 此块 tag 具体可以参考: [https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-6.html#jvms-6.5.invokedynamic](https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-6.html#jvms-6.5.invokedynamic)
//
//        int accResult = oldHandle.getTag();
//
//        switch (accResult) {
//
//            case Opcodes.H_INVOKEINTERFACE:
//
//                accResult = Opcodes.INVOKEINTERFACE;
//
//                break;
//
//            case Opcodes.H_INVOKESPECIAL:
//
////private, this, super 等会调用
//
//                accResult = Opcodes.INVOKESPECIAL;
//
//                break;
//
//            case Opcodes.H_NEWINVOKESPECIAL:
//
////constructors
//
//                accResult = Opcodes.INVOKESPECIAL;
//
//                methodNode.visitTypeInsn(Opcodes.NEW, oldHandle.getOwner());
//
//                methodNode.visitInsn(Opcodes.DUP);
//
//                break;
//
//            case Opcodes.H_INVOKESTATIC:
//
//                accResult = Opcodes.INVOKESTATIC;
//
//                break;
//
//            case Opcodes.H_INVOKEVIRTUAL:
//
//                accResult = Opcodes.INVOKEVIRTUAL;
//
//                break;
//
//        }
//
//        Type middleMethodType = Type.getType(middleMethodDesc);
//
//        Type[] argumentsType = middleMethodType.getArgumentTypes();
//
//        if (argumentsType.length > 0) {
//
//            int loadIndex = 0;
//
//            for (Type tmpType : argumentsType) {
//
//                int opcode = tmpType.getOpcode(Opcodes.ILOAD);
//
//                methodNode.visitVarInsn(opcode, loadIndex);
//
//                loadIndex += tmpType.getSize();
//
//            }
//
//        }
//
//        methodNode.visitMethodInsn(accResult, oldHandle.getOwner(), oldHandle.getName(), oldHandle.getDesc(), false);
//
//        Type returnType = middleMethodType.getReturnType();
//
//        int returnOpcodes = returnType.getOpcode(Opcodes.IRETURN);
//
//        methodNode.visitInsn(returnOpcodes);
//
//        methodNode.visitEnd();
//
//        syntheticMethodList.add(methodNode);

    }
    public interface OnCallBackMethod {
        void onBackName(MethodRecord methodRecord);
    }
}