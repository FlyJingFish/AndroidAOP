package com.flyjingfish.android_aop_processor;

import com.flyjingfish.android_aop_annotation.anno.AndroidAopCollectMethod;
import com.flyjingfish.android_aop_annotation.aop_anno.AopClass;
import com.flyjingfish.android_aop_annotation.aop_anno.AopCollectMethod;
import com.flyjingfish.android_aop_annotation.aop_anno.AopMatchClassMethod;
import com.flyjingfish.android_aop_annotation.anno.AndroidAopMatchClassMethod;
import com.flyjingfish.android_aop_annotation.aop_anno.AopPointCut;
import com.flyjingfish.android_aop_annotation.anno.AndroidAopPointCut;
import com.flyjingfish.android_aop_annotation.anno.AndroidAopReplaceClass;
import com.flyjingfish.android_aop_annotation.anno.AndroidAopModifyExtendsClass;
import com.flyjingfish.android_aop_annotation.anno.AndroidAopReplaceMethod;
import com.flyjingfish.android_aop_annotation.aop_anno.AopReplaceMethod;
import com.flyjingfish.android_aop_annotation.aop_anno.AopModifyExtendsClass;
import com.flyjingfish.android_aop_annotation.base.BasePointCut;
import com.flyjingfish.android_aop_annotation.base.BasePointCutCreator;
import com.flyjingfish.android_aop_annotation.base.MatchClassMethod;
import com.flyjingfish.android_aop_annotation.base.MatchClassMethodCreator;
import com.flyjingfish.android_aop_annotation.enums.MatchType;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementScanner6;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

public class AndroidAopProcessor extends AbstractProcessor {
    Filer mFiler;
//    private static final String packageName = "com.flyjingfish.android_aop_core.aop";
    private TypeMirror matchClassMethodType;
    private Types types;
    private static final String AOP_METHOD_NAME = "aopConfigMethod";
    private Elements elementUtils;

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> set = new LinkedHashSet<>();
        set.add(AndroidAopPointCut.class.getCanonicalName());
        set.add(AndroidAopMatchClassMethod.class.getCanonicalName());
        set.add(AndroidAopReplaceClass.class.getCanonicalName());
        set.add(AndroidAopReplaceMethod.class.getCanonicalName());
        set.add(AndroidAopModifyExtendsClass.class.getCanonicalName());
        set.add(AndroidAopCollectMethod.class.getCanonicalName());
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
        elementUtils = processingEnv.getElementUtils();
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
        processCollectMethod(set, roundEnvironment);
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
            ClassName superinterface = ClassName.bestGuess(BasePointCutCreator.class.getName());
            TypeSpec.Builder typeBuilder = TypeSpec.classBuilder(name1+"$$AndroidAopClass")
                    .addAnnotation(AopClass.class)
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .addSuperinterface(superinterface);
            MethodSpec.Builder whatsMyName1 = whatsMyName(AOP_METHOD_NAME)
                    .addAnnotation(AnnotationSpec.builder(AopPointCut.class)
                            .addMember("value", "$S", "@"+element)
                            .addMember("pointCutClassName", "$S", className)
                            .build());

            typeBuilder.addMethod(whatsMyName1.build());

            String elementClassName = element.toString();
            String packageName = elementClassName.substring(0, elementClassName.lastIndexOf("."));
            String cutClassPackageName = className.substring(0, className.lastIndexOf("."));

            TypeName implementClassName = ClassName.get(packageName, element.getSimpleName().toString());
            ClassName bindClassName = ClassName.bestGuess(BasePointCut.class.getName());
            ParameterizedTypeName returnType = ParameterizedTypeName.get(bindClassName,implementClassName);

            MethodSpec.Builder whatsMyName2 = whatsMyName("newInstance")
                    .addAnnotation(Override.class)
                    .addModifiers(Modifier.FINAL)
                    .addModifiers(Modifier.PUBLIC)
                    .returns(returnType)
                    .addStatement("return new "+className+"()");

            typeBuilder.addMethod(whatsMyName2.build());

            TypeSpec typeSpec = typeBuilder.build();

            JavaFile javaFile = JavaFile.builder(cutClassPackageName, typeSpec)
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

            ClassName superinterface = ClassName.bestGuess(MatchClassMethodCreator.class.getName());

            TypeSpec.Builder typeBuilder = TypeSpec.classBuilder(name1+"$$AndroidAopClass")
                    .addAnnotation(AopClass.class)
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .addSuperinterface(superinterface);
            MethodSpec.Builder whatsMyName1 = whatsMyName(AOP_METHOD_NAME)
                    .addAnnotation(AnnotationSpec.builder(AopMatchClassMethod.class)
                            .addMember("baseClassName", "$S", className)
                            .addMember("methodNames", "$S", methodNamesBuilder)
                            .addMember("pointCutClassName", "$S", element)
                            .addMember("matchType", "$S", matchType.name())
                            .addMember("excludeClasses", "$S", excludeClassesBuilder)
                            .build());

            typeBuilder.addMethod(whatsMyName1.build());

            ClassName bindClassName = ClassName.bestGuess(MatchClassMethod.class.getName());

            MethodSpec.Builder whatsMyName2 = whatsMyName("newInstance")
                    .addModifiers(Modifier.FINAL)
                    .addModifiers(Modifier.PUBLIC)
                    .addAnnotation(Override.class)
                    .returns(bindClassName)
                    .addStatement("return new "+element+"()");

            typeBuilder.addMethod(whatsMyName2.build());

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
            String[] excludeClasses = cut.excludeClasses();
            MatchType matchType = cut.type();
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
                    .addAnnotation(AnnotationSpec.builder(AopReplaceMethod.class)
                            .addMember("targetClassName", "$S", className)
                            .addMember("invokeClassName", "$S", element)
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

    private void processCollectMethod(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(AndroidAopCollectMethod.class);
        for (Element element : elements) {
            Name name1 = element.getSimpleName();

            AndroidAopCollectMethod cut = element.getAnnotation(AndroidAopCollectMethod.class);
            String regex = cut.regex();
            String collectType = cut.collectType().name();

            boolean isStatic = false;
            boolean isPublic = false;
            Set<Modifier> modifiers = element.getModifiers();
            for (Modifier modifier : modifiers) {
                if ("static".equals(modifier.toString())){
                    isStatic = true;
                }
                if ("public".equals(modifier.toString())){
                    isPublic = true;
                }
            }
            if (!isPublic){
                throw new IllegalArgumentException("注意：" + "方法 "+element.getEnclosingElement()+"."+name1+" 必须是public公共方法 ");
            }
            if (!isStatic){
                throw new IllegalArgumentException("注意：" + "方法 "+element.getEnclosingElement()+"."+name1+" 必须是静态方法 ");
            }


            ExecutableElement executableElement = (ExecutableElement) element;
            String returnType = executableElement.getReturnType().toString();
            if (!"void".equals(returnType)){
                throw new IllegalArgumentException("注意：函数"+element.getEnclosingElement()+"."+name1+" 只可以设置 void 作为返回类型");
            }
            if (executableElement.getParameters().isEmpty()){
                throw new IllegalArgumentException("注意：函数"+element.getEnclosingElement()+"."+name1+" 必须设置您想收集的类作为参数");
            }else if (executableElement.getParameters().size() != 1){
                throw new IllegalArgumentException("注意：函数"+element.getEnclosingElement()+"."+name1+" 参数必须设置一个");
            }
            VariableElement variableElement = executableElement.getParameters().get(0);

            TypeMirror asType = variableElement.asType();
            Element typeElement = types.asElement(asType);
//            System.out.println("asType="+typeElement);
            if (typeElement == null){
                throw new IllegalArgumentException("注意：函数"+element.getEnclosingElement()+"."+name1+" 参数的类型不可以设置为"+asType);
            }
            String collectClassName = typeElement.toString();
            boolean isClazz = "java.lang.Class".equals(collectClassName);
            boolean regexIsEmpty = regex == null || "".equals(regex);
            if (isClazz){
                String checkType = variableElement.asType().toString();
                String type = getType(checkType);
                if (regexIsEmpty){
                    if (type == null){
                        throw new IllegalArgumentException("注意：函数"+element.getEnclosingElement()+"."+name1+" 参数的泛型设置的不对");
                    }else {
                        collectClassName = type;
                    }
                }else {
                    if (checkType(checkType)){
                        throw new IllegalArgumentException("注意：函数"+element.getEnclosingElement()+"."+name1+" 参数的泛型设置的不对");
                    }else {
                        if (type == null){
                            collectClassName = "java.lang.Object";
                        }else {
                            collectClassName = type;
                        }
                    }

                }

            }
            if ("kotlin.reflect.KClass".equals(collectClassName)){
                throw new IllegalArgumentException("注意：函数"+element.getEnclosingElement()+"."+name1+" 参数不可以设定为 "+collectClassName);
            }
            if ("kotlin.Any".equals(collectClassName) || "java.lang.Object".equals(collectClassName)){
                if (regexIsEmpty){
                    throw new IllegalArgumentException("注意：函数"+element.getEnclosingElement()+"."+name1+" 参数不可以设定为 "+collectClassName);
                }else{
                    collectClassName = "java.lang.Object";
                }
            }
            String clazzName;
            try {
                clazzName = element.getEnclosingElement().getSimpleName()+computeMD5(name1+"("+isClazz+","+collectType+","+regex+","+variableElement.asType()+")");
            } catch (NoSuchAlgorithmException e) {
                clazzName = element.getEnclosingElement().getSimpleName().toString();
            }
//            System.out.println("collectClassName="+collectClassName);
            TypeSpec.Builder typeBuilder = TypeSpec.classBuilder(clazzName+"$$AndroidAopClass")
                    .addAnnotation(AopClass.class)
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL);
            MethodSpec.Builder whatsMyName1 = whatsMyName(AOP_METHOD_NAME)
                    .addAnnotation(AnnotationSpec.builder(AopCollectMethod.class)
                            .addMember("collectClassName", "$S", collectClassName)
                            .addMember("invokeClassName", "$S", element.getEnclosingElement())
                            .addMember("invokeMethod", "$S", name1)
                            .addMember("isClazz", "$S", isClazz)
                            .addMember("regex", "$S", regex)
                            .addMember("collectType", "$S", collectType)
                            .build());

            typeBuilder.addMethod(whatsMyName1.build());
            TypeSpec typeSpec = typeBuilder.build();
            String elementClassName = element.getEnclosingElement().toString();
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

    private static final Pattern classnameArrayPattern = Pattern.compile("<\\? extends .*?>");
    private static final Pattern classnameArrayPattern1 = Pattern.compile("<\\? extends .*?");
    private static final Pattern classnameArrayPattern2 = Pattern.compile("<\\? super .*?>");

    public static String getType(String type) {
        Matcher matcher = classnameArrayPattern.matcher(type);
        if (matcher.find()){
            String type2= matcher.group();
            Matcher matcher1 = classnameArrayPattern1.matcher(type2);
            if (matcher1.find()){
                String realType = matcher1.replaceFirst("");
                Matcher realMatcher = classnameArrayPattern.matcher(realType);
                String realTypeClass;
                if (realMatcher.find()){
                    realTypeClass = realMatcher.replaceFirst("");
                }else {
                    realTypeClass = realType.replaceAll(">","");
                }
                return realTypeClass;
            }
        }
        return null;
    }

    public static boolean checkType(String type) {
        Matcher matcher = classnameArrayPattern2.matcher(type);
        return matcher.find();
    }

    private static String computeMD5(String message) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] digest = md.digest(message.getBytes());
        return bytesToHex(digest);
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
    private static MethodSpec.Builder whatsMyName(String name) {
        return MethodSpec.methodBuilder(name)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL);
    }
}
