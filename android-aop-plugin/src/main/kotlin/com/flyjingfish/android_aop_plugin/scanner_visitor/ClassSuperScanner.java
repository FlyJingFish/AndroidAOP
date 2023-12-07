package com.flyjingfish.android_aop_plugin.scanner_visitor;

import com.flyjingfish.android_aop_plugin.beans.ClassSuperInfo;
import com.flyjingfish.android_aop_plugin.utils.UtilsKt;
import com.flyjingfish.android_aop_plugin.utils.WovenInfoUtils;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;

public class ClassSuperScanner extends ClassVisitor {
    public ClassSuperScanner() {
        super(Opcodes.ASM9);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        ArrayList<String> interfaceList;
        if (interfaces == null){
            interfaceList = new ArrayList<>();
        }else {
            interfaceList = new ArrayList<>(Arrays.asList(interfaces));
        }
        WovenInfoUtils.INSTANCE.addClassSuper(new ClassSuperInfo(name,superName,interfaceList));
    }
}