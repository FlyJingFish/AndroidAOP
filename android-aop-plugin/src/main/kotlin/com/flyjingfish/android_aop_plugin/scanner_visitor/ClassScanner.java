package com.flyjingfish.android_aop_plugin.scanner_visitor;

import com.flyjingfish.android_aop_plugin.utils.WovenInfoUtils;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;
import org.slf4j.Logger;

public class ClassScanner extends ClassVisitor {
    Logger logger;
    public ClassScanner(Logger logger) {
        super(Opcodes.ASM8);
        this.logger = logger;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        WovenInfoUtils.INSTANCE.addClassName(name);
    }
}