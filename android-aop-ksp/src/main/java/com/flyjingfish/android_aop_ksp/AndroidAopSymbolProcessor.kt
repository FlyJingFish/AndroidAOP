package com.flyjingfish.android_aop_ksp
import com.flyjingfish.android_aop_annotation.anno.AndroidAopCollectMethod
import com.flyjingfish.android_aop_annotation.anno.AndroidAopMatchClassMethod
import com.flyjingfish.android_aop_annotation.anno.AndroidAopModifyExtendsClass
import com.flyjingfish.android_aop_annotation.anno.AndroidAopPointCut
import com.flyjingfish.android_aop_annotation.anno.AndroidAopReplaceClass
import com.flyjingfish.android_aop_annotation.anno.AndroidAopReplaceMethod
import com.flyjingfish.android_aop_annotation.anno.AndroidAopReplaceNew
import com.flyjingfish.android_aop_annotation.aop_anno.AopClass
import com.flyjingfish.android_aop_annotation.aop_anno.AopCollectMethod
import com.flyjingfish.android_aop_annotation.aop_anno.AopMatchClassMethod
import com.flyjingfish.android_aop_annotation.aop_anno.AopModifyExtendsClass
import com.flyjingfish.android_aop_annotation.aop_anno.AopPointCut
import com.flyjingfish.android_aop_annotation.aop_anno.AopReplaceMethod
import com.flyjingfish.android_aop_annotation.base.BasePointCut
import com.flyjingfish.android_aop_annotation.base.BasePointCutCreator
import com.flyjingfish.android_aop_annotation.base.MatchClassMethod
import com.flyjingfish.android_aop_annotation.base.MatchClassMethodCreator
import com.flyjingfish.android_aop_annotation.base.MatchClassMethodSuspend
import com.google.devtools.ksp.containingFile
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.FileLocation
import com.google.devtools.ksp.symbol.FunctionKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.Modifier
import com.google.devtools.ksp.symbol.Origin
import com.google.devtools.ksp.validate
import com.google.gson.Gson
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeSpec
import java.lang.annotation.ElementType
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.regex.Matcher
import java.util.regex.Pattern

class AndroidAopSymbolProcessor(private val codeGenerator: CodeGenerator,
                                private val logger: KSPLogger
) : SymbolProcessor {
  companion object{
    const val AOP_METHOD_NAME = "aopConfigMethod"
    val IGNORE_TYPE = mutableSetOf<String>().apply {
      add("kotlin.Int")
      add("kotlin.Float")
      add("kotlin.Double")
      add("kotlin.Long")
      add("kotlin.Byte")
      add("kotlin.Boolean")
      add("kotlin.Short")
      add("kotlin.Char")
      add("kotlin.String")
      add("java.lang.String")
      add("kotlin.reflect.KClass")
    }
    val mGson = Gson()
  }
  override fun process(resolver: Resolver): List<KSAnnotated> {
//    logger.error("---------AndroidAopSymbolProcessor---------")
    val ret1 = processPointCut(resolver)
    val ret2 = processMatch(resolver)
    processReplaceMethod(resolver,AndroidAopReplaceMethod::class.qualifiedName!!)
    processReplaceMethod(resolver,AndroidAopReplaceNew::class.qualifiedName!!)
    val ret3 = processReplace(resolver)
    val ret4 = processModifyExtendsClass(resolver)
    val ret5 = processCollectMethod(resolver)
    val ret = arrayListOf<KSAnnotated>()
    ret.addAll(ret1)
    ret.addAll(ret2)
    ret.addAll(ret3)
    ret.addAll(ret4)
    ret.addAll(ret5)
    return ret
  }

  private fun processPointCut(resolver: Resolver): List<KSAnnotated> {
    val symbols = resolver.getSymbolsWithAnnotation(AndroidAopPointCut::class.qualifiedName!!)
    for (symbol in symbols) {
      val annotationMap = getAnnotation(symbol)
      val classMethodMap: MutableMap<String, Any?> =
        annotationMap["@"+AndroidAopPointCut::class.simpleName] ?: continue

      val value: KSType =
        (if (classMethodMap["value"] != null) classMethodMap["value"] as KSType else null)
          ?: continue

      val cutClassName: String = value.declaration.packageName.asString() + "." + value.toString()

      val targetClassName = value.declaration.qualifiedName?.asString()?:cutClassName

      val targetMap: MutableMap<String, Any?>? = annotationMap["@Target"]
      if (targetMap != null) {
        val targetValue = targetMap["value"]
        if (targetValue is ElementType) {
          if (ElementType.METHOD != targetValue) {
            throw IllegalArgumentException("注意：请给 $symbol 设置 @Target 为 METHOD ")
          }
        } else if (targetValue is ArrayList<*>) {
          for (s in targetValue) {
            if (symbol.origin == Origin.JAVA) {
              if ("METHOD" != s.toString()) {
                if (targetValue.size > 1) {
                  throw IllegalArgumentException("注意： $symbol 只可以设置 @Target 为 METHOD 这一种")
                } else {
                  throw IllegalArgumentException("注意：请给 $symbol 设置 @Target 为 METHOD ")
                }
              }
            } else if (symbol.origin == Origin.KOTLIN) {
              if ("kotlin.annotation.AnnotationTarget.FUNCTION" != s.toString()) {
                throw IllegalArgumentException("注意：请给 $symbol 设置 @Target 为 FUNCTION ")
              }
            }
          }
        }
        val allowedTargets = targetMap["allowedTargets"]

        if (allowedTargets is ArrayList<*>) {
          for (s in allowedTargets) {
            if (symbol.origin == Origin.JAVA) {
              if ("METHOD" != s.toString()) {
                throw IllegalArgumentException("注意：请给 $symbol 设置 @Target 为 METHOD ")
              }
            } else if (symbol.origin == Origin.KOTLIN) {
              if ("kotlin.annotation.AnnotationTarget.FUNCTION" != s.toString() && "kotlin.annotation.AnnotationTarget.PROPERTY_GETTER" != s.toString() && "kotlin.annotation.AnnotationTarget.PROPERTY_SETTER" != s.toString()) {
                throw IllegalArgumentException("注意：$symbol 只可设置 @Target 为 FUNCTION、PROPERTY_SETTER 或 PROPERTY_GETTER")
              }
            }
          }
        }
      } else {
        if (symbol.origin == Origin.JAVA) {
          throw IllegalArgumentException("注意：请给 $symbol 设置 @Retention 为 METHOD ")
        } else if (symbol.origin == Origin.KOTLIN) {
          throw IllegalArgumentException("注意：请给 $symbol 设置 @Retention 为 FUNCTION、PROPERTY_SETTER 或 PROPERTY_GETTER 至少一种")
        }
      }

      val retentionMap: MutableMap<String, Any?>? = annotationMap["@Retention"]
      if (retentionMap != null) {
        val retention = retentionMap["value"]?.toString()
        if ((symbol.origin == Origin.JAVA && "RUNTIME" != retention) || (symbol.origin == Origin.KOTLIN && "kotlin.annotation.AnnotationRetention.RUNTIME" != retention)) {
          throw IllegalArgumentException("注意：请给 $symbol 设置 @Retention 为 RUNTIME ")
        }
      } else {
        throw IllegalArgumentException("注意：请给 $symbol 设置 @Retention 为 RUNTIME ")
      }

      val className = (symbol as KSClassDeclaration).qualifiedName?.asString()?:(symbol.packageName.asString() + "." + symbol)
      val superinterface = ClassName.bestGuess(BasePointCutCreator::class.qualifiedName!!)

      val fileName = "${symbol}\$\$AndroidAopClass";
      val typeBuilder = TypeSpec.classBuilder(
        fileName
      ).addModifiers(KModifier.FINAL)
        .addAnnotation(AopClass::class)
        .addSuperinterface(superinterface)

      val implementClassName = ClassName(symbol.packageName.asString(), "$symbol")
      val bindClassName = ClassName.bestGuess(BasePointCut::class.qualifiedName!!)
      val returnType = bindClassName.parameterizedBy(implementClassName)

      val whatsMyName2 = whatsMyName("newInstance")
        .addModifiers(KModifier.OVERRIDE)
        .addModifiers(KModifier.FINAL)
        .addModifiers(KModifier.PUBLIC)
        .returns(returnType)
        .addStatement("return $value()")
        .addAnnotation(
          AnnotationSpec.builder(AopPointCut::class)
            .addMember(
              "value = %S",
              "@$className"
            )
            .addMember(
              "pointCutClassName = %S",
              targetClassName
            )
            .build()
        )

      typeBuilder.addFunction(whatsMyName2.build())

      writeToFile(typeBuilder,value.declaration.packageName.asString(),fileName, symbol)
    }
    return symbols.filter { !it.validate() }.toList()
  }

  private fun processMatch(resolver: Resolver): List<KSAnnotated> {
    val symbols =
      resolver.getSymbolsWithAnnotation(AndroidAopMatchClassMethod::class.qualifiedName!!)
    for (symbol in symbols) {
      var isMatchClassMethod = false
      if (symbol is KSClassDeclaration) {
//        val typeList = symbol.superTypes.toList()
//
//        for (ksTypeReference in typeList) {
//          val superClassName = ksTypeReference.resolve().declaration.packageName.asString() + "." + ksTypeReference
//          val declaration = ksTypeReference.resolve()
//          if (declaration is KSClassDeclaration){
//            if (declaration.isSubtype(MatchClassMethod::class.java.name)){
//              isMatchClassMethod = true
//            }
//          }
////          if (superClassName == MatchClassMethod::class.java.name||superClassName == MatchClassMethodSuspend::class.java.name) {
////            isMatchClassMethod = true
////          }
//        }
        if (symbol.isSubtype(MatchClassMethod::class.java.name,logger)){
          isMatchClassMethod = true
        }
      }
      if (!isMatchClassMethod) {
        throw IllegalArgumentException("注意：$symbol 必须实现 ${MatchClassMethod::class.java.name} 或 ${MatchClassMethodSuspend::class.java.name} 接口")
      }

      val annotationMap = getAnnotation(symbol)
      val classMethodMap: MutableMap<String, Any?> =
        annotationMap["@"+AndroidAopMatchClassMethod::class.simpleName] ?: continue

      val targetClassName: String? = classMethodMap["targetClassName"]?.toString()
      val methodNames: ArrayList<String> =
        if (classMethodMap["methodName"] is ArrayList<*>) classMethodMap["methodName"] as ArrayList<String> else ArrayList()
      val excludeClasses: ArrayList<String> =
        if (classMethodMap["excludeClasses"] is ArrayList<*>) classMethodMap["excludeClasses"] as ArrayList<String> else ArrayList()
      val typeStr: String? = classMethodMap["type"]?.toString()
      val matchType = typeStr?.substring(typeStr.lastIndexOf(".") + 1) ?: "EXTENDS"

      val className = (symbol as KSClassDeclaration).packageName.asString() + "." + symbol
      if (targetClassName == null || methodNames == null) {
        continue
      }
      val overrideMethodStr: String = classMethodMap["overrideMethod"]?.toString() ?: "false"
      val overrideMethod = overrideMethodStr == "true"
      val superinterface = ClassName.bestGuess(MatchClassMethodCreator::class.qualifiedName!!)


      val fileName = "${symbol}\$\$AndroidAopClass";
      val typeBuilder = TypeSpec.classBuilder(
        fileName
      ).addModifiers(KModifier.FINAL)
        .addAnnotation(AopClass::class).addSuperinterface(superinterface)

      val methodNamesBuilder = StringBuilder()
      var hasMethodStar = false
      for (i in methodNames.indices) {
        if (methodNames[i] == "*"){
          hasMethodStar = true
        }
        methodNamesBuilder.append(methodNames[i])
        if (i != methodNames.size - 1) {
          methodNamesBuilder.append("-")
        }
      }
      if (hasMethodStar && methodNames.size > 1){
        throw IllegalArgumentException("注意：$symbol 匹配所有方法时不可以再设置其他方法名了")
      }
      val excludeClassesBuilder = StringBuilder()
      for (i in excludeClasses.indices) {
        excludeClassesBuilder.append(excludeClasses[i])
        if (i != excludeClasses.size - 1) {
          excludeClassesBuilder.append("-")
        }
      }
      val matchAll = "*" == methodNamesBuilder.toString() || targetClassName.contains("*")
      if (matchAll && overrideMethod){
        throw IllegalArgumentException("注意：$symbol 匹配所有方法或匹配包名时不可以设置 overrideMethod 为 true")
      }

      val bindClassName = ClassName.bestGuess(MatchClassMethod::class.qualifiedName!!)

      val whatsMyName2 = whatsMyName("newInstance")
        .addModifiers(KModifier.OVERRIDE)
        .addModifiers(KModifier.FINAL)
        .addModifiers(KModifier.PUBLIC)
        .returns(bindClassName)
        .addStatement("return $symbol()")
        .addAnnotation(
          AnnotationSpec.builder(AopMatchClassMethod::class)
            .addMember(
              "baseClassName = %S",
              targetClassName
            )
            .addMember(
              "methodNames = %S",
              mGson.toJson(methodNames)
            )
            .addMember(
              "pointCutClassName = %S",
              className
            )
            .addMember(
              "matchType = %S",
              matchType
            )
            .addMember(
              "excludeClasses = %S",
              mGson.toJson(excludeClasses)
            )
            .addMember(
              "overrideMethod = %L",
              overrideMethod
            )
            .build()
        )

      typeBuilder.addFunction(whatsMyName2.build())

      writeToFile(typeBuilder,symbol.packageName.asString(), fileName, symbol)
    }
    return symbols.filter { !it.validate() }.toList()
  }

  private fun processReplace(resolver: Resolver): List<KSAnnotated> {
    val symbols :Sequence<KSAnnotated> =
      resolver.getSymbolsWithAnnotation(AndroidAopReplaceClass::class.qualifiedName!!)
    for (symbol in symbols) {
      val annotationMap = getAnnotation(symbol)
      val classMethodMap: MutableMap<String, Any?> =
        annotationMap["@"+AndroidAopReplaceClass::class.simpleName] ?: continue

      val targetClassName: String? = classMethodMap["value"]?.toString()


      val className = (symbol as KSClassDeclaration).packageName.asString() + "." + symbol
      if (targetClassName == null) {
        continue
      }

      val excludeClasses: ArrayList<String> =
        if (classMethodMap["excludeClasses"] is ArrayList<*>) classMethodMap["excludeClasses"] as ArrayList<String> else ArrayList()
      val typeStr: String? = classMethodMap["type"]?.toString()
      val matchType = typeStr?.substring(typeStr.lastIndexOf(".") + 1) ?: "EXTENDS"

      val excludeClassesBuilder = StringBuilder()
      for (i in excludeClasses.indices) {
        excludeClassesBuilder.append(excludeClasses[i])
        if (i != excludeClasses.size - 1) {
          excludeClassesBuilder.append("-")
        }
      }
      val fileName = "${symbol}\$\$AndroidAopClass";
      val typeBuilder = TypeSpec.classBuilder(
        fileName
      ).addModifiers(KModifier.FINAL)
        .addAnnotation(AopClass::class)
      val whatsMyName1 = whatsMyName(AOP_METHOD_NAME)
        .addAnnotation(
          AnnotationSpec.builder(AopReplaceMethod::class)
            .addMember(
              "targetClassName = %S",
              targetClassName
            )
            .addMember(
              "invokeClassName = %S",
              className
            )
            .addMember(
              "matchType = %S",
              matchType
            )
            .addMember(
              "excludeClasses = %S",
              mGson.toJson(excludeClasses)
            )
            .build()
        )

      typeBuilder.addFunction(whatsMyName1.build())
      writeToFile(typeBuilder,symbol.packageName.asString(), fileName, symbol)
    }
    return symbols.filter { !it.validate() }.toList()
  }

  private fun processReplaceMethod(resolver: Resolver,qualifiedName :String): List<KSAnnotated> {
    val symbols :Sequence<KSAnnotated> =
      resolver.getSymbolsWithAnnotation(qualifiedName)
    for (symbol in symbols) {
      val annotationMap = getAnnotation(symbol)

      if (symbol.origin == Origin.KOTLIN){
        if (!annotationMap.containsKey("@JvmStatic")){
          var className = "${(symbol as KSFunctionDeclaration).packageName.asString()}."
          var parent = symbol.parent
          while (parent !is KSFile){
            className = "$className$parent."
            parent = parent?.parent
          }
          throw IllegalArgumentException("注意：函数$className${symbol} 必须添加 @JvmStatic 注解")
        }

      }else if (symbol.origin == Origin.JAVA){
        if (symbol is KSFunctionDeclaration){
          if (symbol.functionKind != FunctionKind.STATIC){
            var className = "${symbol.packageName.asString()}."
            var parent = symbol.parent
            while (parent !is KSFile){
              className = "$className$parent."
              parent = parent?.parent
            }
            throw IllegalArgumentException("注意：方法$className${symbol} 必须是静态方法")
          }
        }
      }
    }
    return symbols.filter { !it.validate() }.toList()
  }

  private fun processReplaceNew(resolver: Resolver): List<KSAnnotated> {
    val symbols :Sequence<KSAnnotated> =
      resolver.getSymbolsWithAnnotation(AndroidAopReplaceMethod::class.qualifiedName!!)
    for (symbol in symbols) {
      val annotationMap = getAnnotation(symbol)

      if (symbol.origin == Origin.KOTLIN){
        if (!annotationMap.containsKey("@JvmStatic")){
          var className = "${(symbol as KSFunctionDeclaration).packageName.asString()}."
          var parent = symbol.parent
          while (parent !is KSFile){
            className = "$className$parent."
            parent = parent?.parent
          }
          throw IllegalArgumentException("注意：函数$className${symbol} 必须添加 @JvmStatic 注解")
        }

      }else if (symbol.origin == Origin.JAVA){
        if (symbol is KSFunctionDeclaration){
          if (symbol.functionKind != FunctionKind.STATIC){
            var className = "${symbol.packageName.asString()}."
            var parent = symbol.parent
            while (parent !is KSFile){
              className = "$className$parent."
              parent = parent?.parent
            }
            throw IllegalArgumentException("注意：方法$className${symbol} 必须是静态方法")
          }
        }
      }
    }
    return symbols.filter { !it.validate() }.toList()
  }


  private fun processModifyExtendsClass(resolver: Resolver): List<KSAnnotated> {
    val symbols :Sequence<KSAnnotated> =
      resolver.getSymbolsWithAnnotation(AndroidAopModifyExtendsClass::class.qualifiedName!!)
    for (symbol in symbols) {
      val annotationMap = getAnnotation(symbol)
      val classMethodMap: MutableMap<String, Any?> =
        annotationMap["@"+AndroidAopModifyExtendsClass::class.simpleName] ?: continue

      val targetClassName: String? = classMethodMap["value"]?.toString()


      val className = (symbol as KSClassDeclaration).packageName.asString() + "." + symbol
      if (targetClassName == null) {
        continue
      }
      val fileName = "${symbol}\$\$AndroidAopClass";
      val typeBuilder = TypeSpec.classBuilder(
        fileName
      ).addModifiers(KModifier.FINAL)
        .addAnnotation(AopClass::class)
      val whatsMyName1 = whatsMyName(AOP_METHOD_NAME)
        .addAnnotation(
          AnnotationSpec.builder(AopModifyExtendsClass::class)
            .addMember(
              "targetClassName = %S",
              targetClassName
            )
            .addMember(
              "extendsClassName = %S",
              className
            )
            .build()
        )

      typeBuilder.addFunction(whatsMyName1.build())
      writeToFile(typeBuilder,symbol.packageName.asString(), fileName, symbol)
    }
    return symbols.filter { !it.validate() }.toList()
  }

  private fun processCollectMethod(resolver: Resolver): List<KSAnnotated> {
    val symbols :Sequence<KSAnnotated> =
      resolver.getSymbolsWithAnnotation(AndroidAopCollectMethod::class.qualifiedName!!)
    val funMap = mutableMapOf<String,MutableList<CollectMethod>>()
    for (symbol in symbols) {
      val annotationMap = getAnnotation(symbol)
      if (symbol !is KSFunctionDeclaration) continue

      val classMethodMap: MutableMap<String, Any?> =
        annotationMap["@"+AndroidAopCollectMethod::class.simpleName] ?: continue

      val regex: String = classMethodMap["regex"]?.toString()?:""
      val typeStr: String? = classMethodMap["collectType"]?.toString()
      val collectType: String = typeStr?.substring(typeStr.lastIndexOf(".") + 1) ?: "DIRECT_EXTENDS"
      val regexIsEmpty = regex.isEmpty()

      var className = "${symbol.packageName.asString()}."
      var parent = symbol.parent
      var clazzName =""
      while (parent !is KSFile){
        className = "$className$parent."
        clazzName = parent.toString()
        parent = parent?.parent
      }
      var exceptionHintPreText: String
      var exceptionJavaHintPreText: String
      val location = symbol.location
      if (location is FileLocation){
        exceptionHintPreText = "注意：函数$className${symbol}:line = ${location.lineNumber}"
        exceptionJavaHintPreText = "注意：方法$className${symbol}:line = ${location.lineNumber}"
      }else{
        exceptionHintPreText = "注意：函数$className${symbol}"
        exceptionJavaHintPreText = "注意：方法$className${symbol}"
      }

      if (symbol.parameters.isEmpty()){
        throw IllegalArgumentException("$exceptionHintPreText 必须设置您想收集的类作为参数")
      }else if (symbol.parameters.size != 1){
        throw IllegalArgumentException("$exceptionHintPreText 参数必须设置一个")
      }

      val returnType = symbol.returnType
      if (returnType.toString() != "Unit"){
        throw IllegalArgumentException("$exceptionHintPreText 不可以设置返回值类型")
      }else{
        val nullable = "${symbol.returnType?.resolve()?.nullability}" == "NULLABLE"
        if (nullable){
          throw IllegalArgumentException("$exceptionHintPreText 返回值类型不可设置可为 null 类型")
        }
      }
      if (symbol.origin == Origin.KOTLIN){
        if (!annotationMap.containsKey("@JvmStatic")){
          throw IllegalArgumentException("$exceptionHintPreText 必须添加 @JvmStatic 注解")
        }
        val isPrivate = symbol.modifiers.contains(Modifier.PRIVATE)
        val isInternal = symbol.modifiers.contains(Modifier.INTERNAL)
        if (isPrivate){
          throw IllegalArgumentException("$exceptionHintPreText 不可以设置 private")
        }
        if (isInternal){
          throw IllegalArgumentException("$exceptionHintPreText 不可以设置 internal")
        }

      }else if (symbol.origin == Origin.JAVA){
        if (symbol.functionKind != FunctionKind.STATIC){
          throw IllegalArgumentException("$exceptionJavaHintPreText 必须是静态方法")
        }
        val isPublic = symbol.modifiers.contains(Modifier.PUBLIC)
        if (!isPublic){
          throw IllegalArgumentException("$exceptionJavaHintPreText 必须是public公共方法")
        }
      }
      if (!regexIsEmpty && regex.replace(" ","").isEmpty()){
        throw IllegalArgumentException("$exceptionHintPreText 的 regex 必须包含字符，不可以只有空格")
      }
      val invokeClassName = className.substring(0,className.length-1)
//      logger.error("invokeClassName=$invokeClassName")
      val parameter = symbol.parameters[0]
      val collectClassShortName = "${parameter.type.resolve().declaration}"
      val collectOutClassName = "${parameter.type.resolve().declaration.packageName.asString()}.${collectClassShortName}"

      val isClazz = collectOutClassName == "java.lang.Class"
      var collectClassName = if (isClazz){
        if (symbol.origin == Origin.KOTLIN){
//          logger.error("$exceptionHintPreText---${parameter.type.resolve()}")
          val checkType = "${parameter.type.resolve()}"
          if (regexIsEmpty){
            if (!checkKotlinType(checkType)){
              throw IllegalArgumentException("$exceptionHintPreText 的 参数的泛型设置的不对")
            }
          }else{
            if (checkKotlinType1(checkType)){
              throw IllegalArgumentException("$exceptionHintPreText 的 参数的泛型设置的不对")
            }
          }
        }else if (symbol.origin == Origin.JAVA){
//          logger.error("$exceptionHintPreText---${parameter.type}")
          val checkType = "${parameter.type}"
          if (regexIsEmpty){
            if (!checkJavaType(checkType)){
              throw IllegalArgumentException("$exceptionJavaHintPreText 的 参数的泛型设置的不对")
            }
          }else{
            if (checkJavaType1(checkType)){
              throw IllegalArgumentException("$exceptionJavaHintPreText 的 参数的泛型设置的不对")
            }
          }
        }
        val element = parameter.type.element
        if (element != null){
          val typeArguments = element.typeArguments
          if (typeArguments.isEmpty()){
            if (regexIsEmpty){
              throw IllegalArgumentException("$exceptionHintPreText 的 Class 必须指明 他的范型继承于哪个类")
            }else{
              "java.lang.Object"
            }
          }else{
            val type = typeArguments[0].type
            if (type == null){
              if (regexIsEmpty){
                throw IllegalArgumentException("$exceptionHintPreText 的 Class 必须指明 他的范型继承于哪个类")
              }else{
                "java.lang.Object"
              }
            }else{
//              val subPackageName = type.resolve().declaration.packageName.asString().toString()
//              val subClazzName = type.resolve().declaration.toString()
              type.resolve().declaration.qualifiedName?.asString().toString()
            }

          }
        }else{
          throw IllegalArgumentException("$exceptionHintPreText 的 Class 必须指明 他的范型继承于哪个类")
        }

      }else{
        "${parameter.type.resolve().declaration.qualifiedName?.asString()}"
      }

      if (collectClassName in IGNORE_TYPE){
        throw IllegalArgumentException("$exceptionHintPreText 的 参数不可以设定为 $collectClassName")
      }

      if (collectClassName == "kotlin.Any" || collectClassName == "java.lang.Object"){
        if (regexIsEmpty){
          throw IllegalArgumentException("$exceptionHintPreText 的 regex 为空时它的参数不可以设定为 $collectClassName")
        }else{
          collectClassName = "java.lang.Object"
        }
      }

      val funAllList = funMap[invokeClassName]
      val funList = if (funAllList == null){
        val list = mutableListOf<CollectMethod>()
        funMap[invokeClassName] = list
        list
      }else{
        funAllList
      }
      funList.add(CollectMethod(symbol,collectClassName,invokeClassName,symbol.toString(),"$isClazz", regex, collectType))
//      logger.error("invokeClassName=${symbol.location}")

    }

    funMap.forEach {(invokeClassName,funList) ->
      val fileName = "${invokeClassName.getClassName()}\$\$AndroidAopClass";
      val typeBuilder = TypeSpec.classBuilder(
        fileName
      ).addModifiers(KModifier.FINAL)
        .addAnnotation(AopClass::class)
      var symbol : KSFunctionDeclaration?=null
      for ((index,collectMethod) in funList.withIndex()) {
        symbol = collectMethod.symbol
        val whatsMyName1 = whatsMyName(AOP_METHOD_NAME+index)
          .addAnnotation(
            AnnotationSpec.builder(AopCollectMethod::class)
              .addMember(
                "collectClass = %T::class",
                ClassName.bestGuess(
                  collectMethod.collectClassName
                )
              )
              .addMember(
                "invokeClass = %T::class",
                ClassName.bestGuess(
                  collectMethod.invokeClassName
                )
              )
              .addMember(
                "invokeMethod = %S",
                collectMethod.invokeMethod
              )
              .addMember(
                "isClazz = %L",
                collectMethod.isClazz == "true"
              )
              .addMember(
                "regex = %S",
                collectMethod.regex
              )
              .addMember(
                "collectType = %S",
                collectMethod.collectType
              )
              .build()
          )

        typeBuilder.addFunction(whatsMyName1.build())
      }

      symbol?.let {
        writeToFile(typeBuilder,it.packageName.asString(), fileName, it.containingFile)
      }
    }

    return symbols.filter { !it.validate() }.toList()
  }
  private val javaPattern: Pattern = Pattern.compile("<\\? extends .*?>")
  private val javaPattern1: Pattern = Pattern.compile("<\\? super .*?>")

  private fun checkJavaType(type: String): Boolean {
    val matcher: Matcher = javaPattern.matcher(type)
    return matcher.find()
  }

  private fun checkJavaType1(type: String): Boolean {
    val matcher: Matcher = javaPattern1.matcher(type)
    return matcher.find()
  }

  private val kotlinPattern: Pattern = Pattern.compile("<out .*?>")
  private val kotlinPattern1: Pattern = Pattern.compile("<in .*?>")
  private fun checkKotlinType(type: String): Boolean {
    val matcher: Matcher = kotlinPattern.matcher(type)
    return matcher.find()
  }

  private fun checkKotlinType1(type: String): Boolean {
    val matcher: Matcher = kotlinPattern1.matcher(type)
    return matcher.find()
  }
  private fun writeToFile(
    typeBuilder: TypeSpec.Builder,
    packageName:String,
    fileName: String,
    symbol: KSAnnotated
  ) {
    writeToFile(typeBuilder, packageName, fileName, symbol.containingFile)
  }

  private fun writeToFile(
    typeBuilder: TypeSpec.Builder,
    packageName:String,
    fileName: String,
    sourceFile: KSFile?
  ) {
    val typeSpec = typeBuilder.build()
    val kotlinFile = FileSpec.builder(packageName, fileName).addType(typeSpec)
      .build()
    codeGenerator
      .createNewFile(
        Dependencies(false, sourceFile!!),
        packageName,
        fileName
      )
      .writer()
      .use { kotlinFile.writeTo(it) }
  }

  private fun getAnnotation(symbol : KSAnnotated):MutableMap<String,MutableMap<String,Any?>?>{
    val map = mutableMapOf<String,MutableMap<String,Any?>?>()
    for (annotation in symbol.annotations) {
      val annotationName = annotation.toString()
      var innerMap = map[annotationName]
      if (innerMap == null){
        innerMap = mutableMapOf()
        map[annotationName] = innerMap
      }

      for (argument in annotation.arguments) {
        innerMap[argument.name?.getShortName().toString()] = argument.value as Any
      }
    }
    return map
  }

  private fun whatsMyName(name: String): FunSpec.Builder {
    return FunSpec.builder(name).addModifiers(KModifier.FINAL)
  }

  private fun computeMD5(string: String): String? {
    return try {
      val messageDigest = MessageDigest.getInstance("MD5")
      val digestBytes = messageDigest.digest(string.toByteArray())
      bytesToHex(digestBytes)
    } catch (var3: NoSuchAlgorithmException) {
      throw IllegalStateException(var3)
    }
  }
  private fun bytesToHex(bytes: ByteArray): String? {
    val hexString = StringBuilder()
    for (b in bytes) {
      val hex = Integer.toHexString(0xff and b.toInt())
      if (hex.length == 1) {
        hexString.append('0')
      }
      hexString.append(hex)
    }
    return hexString.toString()
  }
}