package com.flyjingfish.android_aop_plugin.scanner_visitor;

import com.flyjingfish.android_aop_plugin.beans.AopMatchCut;
import com.flyjingfish.android_aop_plugin.beans.AopMethodCut;
import com.flyjingfish.android_aop_plugin.utils.WovenInfoUtils;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.slf4j.Logger;

public class AnnotationScanner extends ClassVisitor {
    Logger logger;
    static final String CLASS_POINT = "Lcom/flyjingfish/android_aop_annotation/anno/AndroidAopClass";
    static final String METHOD_POINT = "Lcom/flyjingfish/android_aop_annotation/anno/AndroidAopMethod";
    static final String MATCH_POINT = "Lcom/flyjingfish/android_aop_annotation/anno/AndroidAopMatch";
    boolean isAndroidAopClass;
    public AnnotationScanner(Logger logger) {
        super(Opcodes.ASM8);
        this.logger = logger;
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        if (descriptor.contains(CLASS_POINT)){
            isAndroidAopClass = true;
        }
        return super.visitAnnotation(descriptor, visible);
    }

    class MethodAnnoVisitor extends AnnotationVisitor {
        String anno;
        String cutClassName;
        String baseClassName;
        String methodNames;
        String pointCutClassName;
        MethodAnnoVisitor() {
            super(Opcodes.ASM8);
        }
        @Override
        public void visit(String name, Object value) {
            if (isAndroidAopClass){
                if (name.equals("value")) {
                    anno = value.toString();
                }
                if (name.equals("pointCutClassName")) {
                    cutClassName = value.toString();
                }
                if (name.equals("baseClassName")) {
                    baseClassName = value.toString();
                }
                if (name.equals("methodNames")) {
                    methodNames = value.toString();
                }
                if (name.equals("pointCutClassName")) {
                    pointCutClassName = value.toString();
                }
//                WovenInfoUtils.INSTANCE.addAnnoInfo(value.toString());
            }
            super.visit(name, value);
        }

        @Override
        public void visitEnd() {
            super.visitEnd();
            if (anno != null && cutClassName != null){
                AopMethodCut cut = new AopMethodCut(anno,cutClassName);
                WovenInfoUtils.INSTANCE.addAnnoInfo(cut);
            }
            if (baseClassName != null && methodNames != null){
                AopMatchCut cut = new AopMatchCut(baseClassName,methodNames.split("-"),pointCutClassName);
                WovenInfoUtils.INSTANCE.addMatchInfo(cut);
            }
        }
    }

    class MyMethodVisitor extends MethodVisitor {
        MyMethodVisitor() {
            super(Opcodes.ASM8);
        }
        @Override
        public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
            if (isAndroidAopClass){
                if (descriptor.contains(METHOD_POINT)||descriptor.contains(MATCH_POINT)){
                    return new MethodAnnoVisitor();
                }else {
                    return super.visitAnnotation(descriptor, visible);
                }
            }else {
                return super.visitAnnotation(descriptor, visible);
            }
        }
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor,
                                     String signature, String[] exceptions) {
        if (isAndroidAopClass){
            return new MyMethodVisitor();
        }else {
            return super.visitMethod(access, name, descriptor, signature, exceptions);
        }
    }
}