package com.flyjingfish.android_aop_processor;

import com.flyjingfish.android_aop_annotation.aop_anno.AopClass;
import com.flyjingfish.android_aop_annotation.aop_anno.AopMatchClassMethod;
import com.flyjingfish.android_aop_annotation.anno.AndroidAopMatchClassMethod;
import com.flyjingfish.android_aop_annotation.aop_anno.AopPointCut;
import com.flyjingfish.android_aop_annotation.anno.AndroidAopPointCut;
import com.flyjingfish.android_aop_annotation.anno.AndroidAopReplaceClass;
import com.flyjingfish.android_aop_annotation.anno.AndroidAopModifyExtendsClass;
import com.flyjingfish.android_aop_annotation.anno.AndroidAopReplaceMethod;
import com.flyjingfish.android_aop_annotation.aop_anno.AopReplaceMethod;
import com.flyjingfish.android_aop_annotation.aop_anno.AopModifyExtendsClass;
import com.flyjingfish.android_aop_annotation.base.MatchClassMethod;
import com.flyjingfish.android_aop_annotation.enums.MatchType;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

public class AndroidAopProcessor extends AbstractProcessor {
    Filer mFiler;
//    private static final String packageName = "com.flyjingfish.android_aop_core.aop";
    private TypeMirror matchClassMethodType;
    private Types types;
    private static final String AOP_METHOD_NAME = "aopConfigMethod";

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> set = new LinkedHashSet<>();
        set.add(AndroidAopPointCut.class.getCanonicalName());
        set.add(AndroidAopMatchClassMethod.class.getCanonicalName());
        set.add(AndroidAopReplaceClass.class.getCanonicalName());
        set.add(AndroidAopReplaceMethod.class.getCanonicalName());
        set.add(AndroidAopModifyExtendsClass.class.getCanonicalName());
        return set;
    }
    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }
    public static boolean isEmpty(final Map<?,?> map) {
        return map == null || map.isEmpty();
    }
    public static boolean isEmpty(final Set<?> set) {
        return set == null || set.isEmpty();
    }
    public static boolean isNotEmpty(final Map<?,?> map) {
        return !isEmpty(map);
    }
    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        mFiler = processingEnv.getFiler();
        Elements elementUtils = processingEnv.getElementUtils();
        matchClassMethodType = elementUtils.getTypeElement(MatchClassMethod.class.getName()).asType();
        types = processingEnv.getTypeUtils();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        if (isEmpty(set)){
            return false;
        }

        processPointCut(set, roundEnvironment);
        processMatch(set, roundEnvironment);
        processReplaceMethod(set, roundEnvironment);
        processReplace(set, roundEnvironment);
        processModifyExtendsClass(set, roundEnvironment);
        return false;
    }



    public void processPointCut(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(AndroidAopPointCut.class);
        for (Element element : elements) {
            Name name1 = element.getSimpleName();
            AndroidAopPointCut cut = element.getAnnotation(AndroidAopPointCut.class);
            Target target = element.getAnnotation(Target.class);
            Retention retention = element.getAnnotation(Retention.class);
            if (target == null){
                throw new IllegalArgumentException("注意：" + "请给 "+name1+" 设置 @Target 为ElementType.METHOD ");
            }
            ElementType[] elementTypes = target.value();
            if (elementTypes.length > 1){
                throw new IllegalArgumentException("注意：" + name1 + "只可以设置 @Target 为 ElementType.METHOD 这一种");
            }else if (elementTypes.length == 1){
                if (elementTypes[0] != ElementType.METHOD){
                    throw new IllegalArgumentException("注意：" + "请给 "+name1+" 设置 @Target 为 ElementType.METHOD ");
                }
            }else {
                throw new IllegalArgumentException("注意：" + "请给 "+name1+" 设置 @Target 为 ElementType.METHOD ");
            }
            if (retention == null){
                throw new IllegalArgumentException("注意：" + "请给 "+name1+" 设置 @Retention 为 RetentionPolicy.RUNTIME ");
            }
            RetentionPolicy retentionPolicy = retention.value();
            if (retentionPolicy == null){
                throw new IllegalArgumentException("注意：" + "请给 "+name1+" 设置 @Retention 为 RetentionPolicy.RUNTIME ");
            }else if (retentionPolicy != RetentionPolicy.RUNTIME){
                throw new IllegalArgumentException("注意：" + "必须给 "+name1+" 设置 @Retention 为 RetentionPolicy.RUNTIME ");
            }

            String className;
            try{
                className = cut.value().getName();
            }catch (MirroredTypeException mirroredTypeException){
                String errorMessage = mirroredTypeException.getLocalizedMessage();
                className = errorMessage.substring( errorMessage.lastIndexOf(" ")+1);
            }
            TypeSpec.Builder typeBuilder = TypeSpec.classBuilder(name1+"$$AndroidAopClass")
                    .addAnnotation(AopClass.class)
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL);
            MethodSpec.Builder whatsMyName1 = whatsMyName(AOP_METHOD_NAME)
                    .addAnnotation(AnnotationSpec.builder(AopPointCut.class)
                            .addMember("value", "$S", "@"+element)
                            .addMember("pointCutClassName", "$S", className)
                            .build());

            typeBuilder.addMethod(whatsMyName1.build());

            TypeSpec typeSpec = typeBuilder.build();
            String elementClassName = element.toString();
            String packageName = elementClassName.substring(0, elementClassName.lastIndexOf("."));
            JavaFile javaFile = JavaFile.builder(packageName, typeSpec)
                    .build();
            try {
                javaFile.writeTo(mFiler);
            } catch (IOException e) {
//                throw new RuntimeException(e);
            }
        }
    }

    public void processMatch(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(AndroidAopMatchClassMethod.class);
        for (Element element : elements) {
            if (!types.isSubtype(element.asType(), matchClassMethodType)) {
                throw new IllegalArgumentException("注意：" +element+ " 必须实现 MatchClassMethod 接口");
            }
            Name name1 = element.getSimpleName();
//                System.out.println("======"+element);
            AndroidAopMatchClassMethod cut = element.getAnnotation(AndroidAopMatchClassMethod.class);
            String className = cut.targetClassName();
            String[] methodNames = cut.methodName();
            String[] excludeClasses = cut.excludeClasses();
            MatchType matchType = cut.type();
            StringBuilder methodNamesBuilder = new StringBuilder();
            for (int i = 0; i < methodNames.length; i++) {
                methodNamesBuilder.append(methodNames[i]);
                if (i != methodNames.length -1){
                    methodNamesBuilder.append("-");
                }
            }
            StringBuilder excludeClassesBuilder = new StringBuilder();
            for (int i = 0; i < excludeClasses.length; i++) {
                excludeClassesBuilder.append(excludeClasses[i]);
                if (i != excludeClasses.length -1){
                    excludeClassesBuilder.append("-");
                }
            }
            TypeSpec.Builder typeBuilder = TypeSpec.classBuilder(name1+"$$AndroidAopClass")
                    .addAnnotation(AopClass.class)
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL);
            MethodSpec.Builder whatsMyName1 = whatsMyName(AOP_METHOD_NAME)
                    .addAnnotation(AnnotationSpec.builder(AopMatchClassMethod.class)
                            .addMember("baseClassName", "$S", className)
                            .addMember("methodNames", "$S", methodNamesBuilder)
                            .addMember("pointCutClassName", "$S", element)
                            .addMember("matchType", "$S", matchType.name())
                            .addMember("excludeClasses", "$S", excludeClassesBuilder)
                            .build());

            typeBuilder.addMethod(whatsMyName1.build());

            TypeSpec typeSpec = typeBuilder.build();
            String elementClassName = element.toString();
            String packageName = elementClassName.substring(0, elementClassName.lastIndexOf("."));
            JavaFile javaFile = JavaFile.builder(packageName, typeSpec)
                    .build();
            try {
                javaFile.writeTo(mFiler);
            } catch (IOException e) {
//                throw new RuntimeException(e);
            }
        }
    }



    private void processReplace(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {

        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(AndroidAopReplaceClass.class);
        for (Element element : elements) {
            Name name1 = element.getSimpleName();
//                System.out.println("======"+element);
            AndroidAopReplaceClass cut = element.getAnnotation(AndroidAopReplaceClass.class);
            String className = cut.value();

            TypeSpec.Builder typeBuilder = TypeSpec.classBuilder(name1+"$$AndroidAopClass")
                    .addAnnotation(AopClass.class)
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL);
            MethodSpec.Builder whatsMyName1 = whatsMyName(AOP_METHOD_NAME)
                    .addAnnotation(AnnotationSpec.builder(AopReplaceMethod.class)
                            .addMember("targetClassName", "$S", className)
                            .addMember("invokeClassName", "$S", element)
                            .build());

            typeBuilder.addMethod(whatsMyName1.build());

            TypeSpec typeSpec = typeBuilder.build();
            String elementClassName = element.toString();
            String packageName = elementClassName.substring(0, elementClassName.lastIndexOf("."));
            JavaFile javaFile = JavaFile.builder(packageName, typeSpec)
                    .build();
            try {
                javaFile.writeTo(mFiler);
            } catch (IOException e) {
//                throw new RuntimeException(e);
            }
        }
    }

    private void processModifyExtendsClass(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(AndroidAopModifyExtendsClass.class);
        for (Element element : elements) {
            Name name1 = element.getSimpleName();
//                System.out.println("======"+element);
            AndroidAopModifyExtendsClass cut = element.getAnnotation(AndroidAopModifyExtendsClass.class);
            String className = cut.value();

            TypeSpec.Builder typeBuilder = TypeSpec.classBuilder(name1+"$$AndroidAopClass")
                    .addAnnotation(AopClass.class)
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL);
            MethodSpec.Builder whatsMyName1 = whatsMyName(AOP_METHOD_NAME)
                    .addAnnotation(AnnotationSpec.builder(AopModifyExtendsClass.class)
                            .addMember("targetClassName", "$S", className)
                            .addMember("extendsClassName", "$S", element)
                            .build());

            typeBuilder.addMethod(whatsMyName1.build());

            TypeSpec typeSpec = typeBuilder.build();
            String elementClassName = element.toString();
            String packageName = elementClassName.substring(0, elementClassName.lastIndexOf("."));
            JavaFile javaFile = JavaFile.builder(packageName, typeSpec)
                    .build();
            try {
                javaFile.writeTo(mFiler);
            } catch (IOException e) {
//                throw new RuntimeException(e);
            }
        }
    }

    private void processReplaceMethod(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(AndroidAopReplaceMethod.class);
        for (Element element : elements) {
            Name name1 = element.getSimpleName();
            boolean isStatic = false;
            Set<Modifier> modifiers = element.getModifiers();
            for (Modifier modifier : modifiers) {
                if ("static".equals(modifier.toString())){
                    isStatic = true;
                }
            }
            if (!isStatic){
                throw new IllegalArgumentException("注意：" + "方法 "+element.getEnclosingElement()+"."+name1+" 必须是静态方法 ");
            }
        }
    }
    private static MethodSpec.Builder whatsMyName(String name) {
        return MethodSpec.methodBuilder(name)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL);
    }
}
