package com.flyjingfish.android_aop_plugin.scanner_visitor;

import com.flyjingfish.android_aop_plugin.beans.AopMatchCut;
import com.flyjingfish.android_aop_plugin.beans.MethodRecord;
import com.flyjingfish.android_aop_plugin.utils.Utils;
import com.flyjingfish.android_aop_plugin.utils.WovenInfoUtils;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.slf4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.MethodInfo;

public class AnnotationMethodScanner extends ClassVisitor {
    Logger logger;
    private final OnCallBackMethod onCallBackMethod;
    private final byte[] classByte;

    public AnnotationMethodScanner(Logger logger,byte[] classByte,OnCallBackMethod onCallBackMethod) {
        super(Opcodes.ASM8);
        this.logger = logger;
        this.onCallBackMethod = onCallBackMethod;
        this.classByte = classByte;
//        classPool = ClassPoolUtils.INSTANCE.getClassPool();
    }


    private boolean isDescendantClass;
    private AopMatchCut aopMatchCut;
    private List<MethodRecord> cacheMethodRecords = new ArrayList<>();
    private String className;
    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        className = name;
        if (superName != null){

            WovenInfoUtils.INSTANCE.getAopMatchCuts().forEach((key, aopMatchCut)->{
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


                if (aopMatchCut.getBaseClassName().equals(Utils.INSTANCE.slashToDot(superName))) {
                    this.isDescendantClass= true;
                    AnnotationMethodScanner.this.aopMatchCut = aopMatchCut;
                }
            });
        }
        super.visit(version, access, name, signature, superName, interfaces);

    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        return super.visitAnnotation(descriptor, visible);
    }

    class MyMethodVisitor extends MethodVisitor {
        MethodRecord methodName;
        MyMethodVisitor(MethodRecord methodName) {
            super(Opcodes.ASM8);
            this.methodName = methodName;
        }
        @Override
        public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
//            logger.error("AnnotationMethodScanner MyMethodVisitor type: " + descriptor);
            if (WovenInfoUtils.INSTANCE.isContainAnno(descriptor)){
                boolean isBack = true;
                try {
                    ClassPool classPool = new ClassPool(null);
                    InputStream byteArrayInputStream = new ByteArrayInputStream(classByte);
                    CtClass ctClass = classPool.makeClass(byteArrayInputStream);
                    CtMethod ctMethod = getCtMethod(ctClass,methodName.getMethodName(),methodName.getDescriptor());
                    MethodInfo methodInfo = ctMethod.getMethodInfo();
                    CodeAttribute codeAttribute = methodInfo.getCodeAttribute();
                    if (codeAttribute == null){
                        isBack = false;
                    }
                } catch (Exception ignored) {
                }
                if (onCallBackMethod != null && isBack){
                    onCallBackMethod.onBackName(methodName);
                }
            }
            return super.visitAnnotation(descriptor, visible);
        }
    }
    public static CtMethod getCtMethod(CtClass ctClass,String methodName,String descriptor) throws NotFoundException {
        CtMethod[] ctMethods = ctClass.getDeclaredMethods(methodName);
        if (ctMethods != null && ctMethods.length>0){
            for (CtMethod ctMethod : ctMethods) {
                String allSignature = ctMethod.getSignature();
                if (descriptor.equals(allSignature)){
                    return ctMethod;
                }
            }
        }
        return null;
    }
    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor,
                                     String signature, String[] exceptions) {
        if (isDescendantClass){
            for (String methodName : aopMatchCut.getMethodNames()) {
                if (methodName.equals(name)){
                    boolean isBack = true;
                    try {
                        ClassPool classPool = new ClassPool(null);
                        InputStream byteArrayInputStream = new ByteArrayInputStream(classByte);
                        CtClass ctClass = classPool.makeClass(byteArrayInputStream);
                        CtMethod ctMethod = getCtMethod(ctClass,name,descriptor);
                        MethodInfo methodInfo = ctMethod.getMethodInfo();
                        CodeAttribute codeAttribute = methodInfo.getCodeAttribute();
                        if (codeAttribute == null){
                            isBack = false;
                        }
                    } catch (Exception ignored) {
                    }
                    if (isBack){
                        cacheMethodRecords.add(new MethodRecord(name,descriptor, aopMatchCut.getCutClassName()));
                    }
                    break;
                }
            }
        }
        return new MyMethodVisitor(new MethodRecord(name,descriptor,null));
    }

    @Override
    public void visitEnd() {
        super.visitEnd();
        if (isDescendantClass){
            for (MethodRecord cacheMethodRecord : cacheMethodRecords) {
                if (onCallBackMethod != null){
                    onCallBackMethod.onBackName(cacheMethodRecord);
                }
            }
        }
        if (onCallBackMethod != null){
            onCallBackMethod.onFinish();
        }
    }

    public interface OnCallBackMethod{
        void onBackName(MethodRecord methodRecord);
        void onFinish();
    }
}