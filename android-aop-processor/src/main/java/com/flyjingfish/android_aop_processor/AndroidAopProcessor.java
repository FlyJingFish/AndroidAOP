package com.flyjingfish.android_aop_processor;

import com.flyjingfish.android_aop_annotation.anno.AndroidAopClass;
import com.flyjingfish.android_aop_annotation.anno.AndroidAopMatch;
import com.flyjingfish.android_aop_annotation.anno.AndroidAopMatchClassMethod;
import com.flyjingfish.android_aop_annotation.anno.AndroidAopMethod;
import com.flyjingfish.android_aop_annotation.anno.AndroidAopPointCut;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
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

public class AndroidAopProcessor extends AbstractProcessor {
    Filer mFiler;
    private static final String packageName = "com.flyjingfish.android_aop_core.aop";

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> set = new LinkedHashSet<>();
        set.add(AndroidAopPointCut.class.getCanonicalName());
        set.add(AndroidAopMatchClassMethod.class.getCanonicalName());
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
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        System.out.println("======AndroidAopProcessor======"+set.size());
        if (isEmpty(set)){
            return false;
        }
        processPointCut(set, roundEnvironment);
        processMatch(set, roundEnvironment);
        return false;
    }

    public void processPointCut(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(AndroidAopPointCut.class);
        for (TypeElement typeElement: set){
            Name name = typeElement.getSimpleName();
            for (Element element : elements) {
                Name name1 = element.getSimpleName();
                AndroidAopPointCut cut = element.getAnnotation(AndroidAopPointCut.class);
                Target target = element.getAnnotation(Target.class);
                String className;
                try{
                    className = cut.value().getName();
                }catch (MirroredTypeException mirroredTypeException){
                    String errorMessage = mirroredTypeException.getLocalizedMessage();
                    className = errorMessage.substring( errorMessage.lastIndexOf(" ")+1);
                }
                TypeSpec.Builder typeBuilder = TypeSpec.classBuilder(name1+"$$AndroidAopClass")
                        .addAnnotation(AndroidAopClass.class)
                        .addModifiers(Modifier.PUBLIC, Modifier.FINAL);
                MethodSpec.Builder whatsMyName1 = whatsMyName("withinAnnotatedClass")
                        .addAnnotation(AnnotationSpec.builder(AndroidAopMethod.class)
                                .addMember("value", "$S", "@"+element)
                                .addMember("pointCutClassName", "$S", className)
                                .build());

                typeBuilder.addMethod(whatsMyName1.build());

                TypeSpec typeSpec = typeBuilder.build();

                JavaFile javaFile = JavaFile.builder(packageName, typeSpec)
                        .build();
                try {
                    javaFile.writeTo(mFiler);
                } catch (IOException e) {
//                    throw new RuntimeException(e);
                }
            }
        }
    }

    public void processMatch(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(AndroidAopMatchClassMethod.class);
        for (TypeElement typeElement: set){
            Name name = typeElement.getSimpleName();
            for (Element element : elements) {
                Name name1 = element.getSimpleName();
//                System.out.println("======"+element);
                AndroidAopMatchClassMethod cut = element.getAnnotation(AndroidAopMatchClassMethod.class);
                Target target = element.getAnnotation(Target.class);
                String className = cut.targetClassName();
                String[] methodNames = cut.methodName();
                StringBuilder stringBuilder = new StringBuilder();
                for (int i = 0; i < methodNames.length; i++) {
                    stringBuilder.append(methodNames[i]);
                    if (i != methodNames.length -1){
                        stringBuilder.append("-");
                    }
                }
                TypeSpec.Builder typeBuilder = TypeSpec.classBuilder(name1+"$$AndroidAopClass")
                        .addAnnotation(AndroidAopClass.class)
                        .addModifiers(Modifier.PUBLIC, Modifier.FINAL);
                MethodSpec.Builder whatsMyName1 = whatsMyName("withinAnnotatedClass")
                        .addAnnotation(AnnotationSpec.builder(AndroidAopMatch.class)
                                .addMember("baseClassName", "$S", className)
                                .addMember("methodNames", "$S", stringBuilder)
                                .addMember("pointCutClassName", "$S", element)
                                .build());

                typeBuilder.addMethod(whatsMyName1.build());

                TypeSpec typeSpec = typeBuilder.build();

                JavaFile javaFile = JavaFile.builder(packageName, typeSpec)
                        .build();
                try {
                    javaFile.writeTo(mFiler);
                } catch (IOException e) {
//                    throw new RuntimeException(e);
                }
            }
        }
    }


    private static MethodSpec.Builder whatsMyName(String name) {
        return MethodSpec.methodBuilder(name)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL);
    }
}
