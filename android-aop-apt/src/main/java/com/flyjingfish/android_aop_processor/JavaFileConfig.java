package com.flyjingfish.android_aop_processor;

import com.squareup.javapoet.TypeSpec;

public class JavaFileConfig {
    public TypeSpec.Builder typeBuilder;
    public String packageName;

    public JavaFileConfig(TypeSpec.Builder typeBuilder, String packageName) {
        this.typeBuilder = typeBuilder;
        this.packageName = packageName;
    }
}
