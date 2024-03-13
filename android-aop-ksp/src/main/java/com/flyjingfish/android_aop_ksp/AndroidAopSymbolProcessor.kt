package com.flyjingfish.android_aop_ksp
import com.flyjingfish.android_aop_annotation.aop_anno.AopClass
import com.flyjingfish.android_aop_annotation.aop_anno.AopMatchClassMethod
import com.flyjingfish.android_aop_annotation.anno.AndroidAopMatchClassMethod
import com.flyjingfish.android_aop_annotation.aop_anno.AopPointCut
import com.flyjingfish.android_aop_annotation.anno.AndroidAopPointCut
import com.flyjingfish.android_aop_annotation.anno.AndroidAopReplaceClass
import com.flyjingfish.android_aop_annotation.anno.AndroidAopModifyExtendsClass
import com.flyjingfish.android_aop_annotation.anno.AndroidAopReplaceMethod
import com.flyjingfish.android_aop_annotation.aop_anno.AopReplaceMethod
import com.flyjingfish.android_aop_annotation.aop_anno.AopModifyExtendsClass
import com.flyjingfish.android_aop_annotation.base.MatchClassMethod
import com.google.devtools.ksp.containingFile
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.FunctionKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.Origin
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec
import java.lang.annotation.ElementType

class AndroidAopSymbolProcessor(private val codeGenerator: CodeGenerator,
                                private val logger: KSPLogger
) : SymbolProcessor {
  companion object{
    const val AOP_METHOD_NAME = "aopConfigMethod"
  }
  override fun process(resolver: Resolver): List<KSAnnotated> {
//    logger.error("---------AndroidAopSymbolProcessor---------")
    val ret1 = processPointCut(resolver)
    val ret2 = processMatch(resolver)
    processReplaceMethod(resolver)
    val ret3 = processReplace(resolver)
    val ret4 = processModifyExtendsClass(resolver)
    val ret = arrayListOf<KSAnnotated>()
    ret.addAll(ret1)
    ret.addAll(ret2)
    ret.addAll(ret3)
    ret.addAll(ret4)
    return ret
  }

  private fun processPointCut(resolver: Resolver): List<KSAnnotated> {
    val symbols = resolver.getSymbolsWithAnnotation(AndroidAopPointCut::class.qualifiedName!!)
    for (symbol in symbols) {
      val annotationMap = getAnnotation(symbol)
      val classMethodMap: MutableMap<String, Any?> =
        annotationMap["@"+AndroidAopPointCut::class.simpleName] ?: continue

      val value: KSType? =
        if (classMethodMap["value"] != null) classMethodMap["value"] as KSType else null
      val targetClassName: String =
        (if (value != null) value.declaration.packageName.asString() + "." + value.toString() else null)
          ?: continue

      val targetMap: MutableMap<String, Any?>? = annotationMap["@Target"]
      if (targetMap != null) {
        val value = targetMap["value"]
        if (value is ElementType) {
          if (ElementType.METHOD != value) {
            throw IllegalArgumentException("注意：请给 $symbol 设置 @Target 为 METHOD ")
          }
        } else if (value is ArrayList<*>) {
          for (s in value) {
            if (symbol.origin == Origin.JAVA) {
              if ("METHOD" != s.toString()) {
                if (value.size > 1) {
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
          val value: ArrayList<*> = allowedTargets
          for (s in value) {
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
        val value = retentionMap["value"]
        val retention = value.toString()
        if ((symbol.origin == Origin.JAVA && "RUNTIME" != retention) || (symbol.origin == Origin.KOTLIN && "kotlin.annotation.AnnotationRetention.RUNTIME" != retention)) {
          throw IllegalArgumentException("注意：请给 $symbol 设置 @Retention 为 RUNTIME ")
        }
      } else {
        throw IllegalArgumentException("注意：请给 $symbol 设置 @Retention 为 RUNTIME ")
      }

      val className = (symbol as KSClassDeclaration).packageName.asString() + "." + symbol

      val fileName = "${symbol}\$\$AndroidAopClass";
      val typeBuilder = TypeSpec.classBuilder(
        fileName
      ).addModifiers(KModifier.FINAL)
        .addAnnotation(AopClass::class)

      val whatsMyName1 = whatsMyName(AOP_METHOD_NAME)
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

      typeBuilder.addFunction(whatsMyName1.build())

      writeToFile(typeBuilder,symbol.packageName.asString(),fileName, symbol)
    }
    return symbols.filter { !it.validate() }.toList()
  }

  private fun processMatch(resolver: Resolver): List<KSAnnotated> {
    val symbols =
      resolver.getSymbolsWithAnnotation(AndroidAopMatchClassMethod::class.qualifiedName!!)
    for (symbol in symbols) {
      var isMatchClassMethod = false
      if (symbol is KSClassDeclaration) {
        val typeList = symbol.superTypes.toList()

        for (ksTypeReference in typeList) {
          val superClassName = ksTypeReference.resolve().declaration.packageName.asString() + "." + ksTypeReference
          if (superClassName == MatchClassMethod::class.java.name) {
            isMatchClassMethod = true
          }
        }
      }
      if (!isMatchClassMethod) {
        throw IllegalArgumentException("注意：$symbol 必须实现 ${MatchClassMethod::class.java.name} 接口")
      }

      val annotationMap = getAnnotation(symbol)
      val classMethodMap: MutableMap<String, Any?> =
        annotationMap["@"+AndroidAopMatchClassMethod::class.simpleName] ?: continue

      val targetClassName: String? = classMethodMap["targetClassName"]?.toString()
      val methodNames: ArrayList<String>? =
        if (classMethodMap["methodName"] is ArrayList<*>) classMethodMap["methodName"] as ArrayList<String> else null
      val excludeClasses: ArrayList<String>? =
        if (classMethodMap["excludeClasses"] is ArrayList<*>) classMethodMap["excludeClasses"] as ArrayList<String> else null
      val typeStr: String? = classMethodMap["type"]?.toString()
      val matchType = typeStr?.substring(typeStr.lastIndexOf(".") + 1) ?: "EXTENDS"


      val className = (symbol as KSClassDeclaration).packageName.asString() + "." + symbol
      if (targetClassName == null || methodNames == null) {
        continue
      }
      val fileName = "${symbol}\$\$AndroidAopClass";
      val typeBuilder = TypeSpec.classBuilder(
        fileName
      ).addModifiers(KModifier.FINAL)
        .addAnnotation(AopClass::class)
      val methodNamesBuilder = StringBuilder()
      for (i in methodNames.indices) {
        methodNamesBuilder.append(methodNames[i])
        if (i != methodNames.size - 1) {
          methodNamesBuilder.append("-")
        }
      }
      val excludeClassesBuilder = StringBuilder()
      if (excludeClasses != null) {
        for (i in excludeClasses.indices) {
          excludeClassesBuilder.append(excludeClasses[i])
          if (i != excludeClasses.size - 1) {
            excludeClassesBuilder.append("-")
          }
        }
      }
      val whatsMyName1 = whatsMyName(AOP_METHOD_NAME)
        .addAnnotation(
          AnnotationSpec.builder(AopMatchClassMethod::class)
            .addMember(
              "baseClassName = %S",
              targetClassName
            )
            .addMember(
              "methodNames = %S",
              methodNamesBuilder
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
              excludeClassesBuilder
            )
            .build()
        )

      typeBuilder.addFunction(whatsMyName1.build())
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
            .build()
        )

      typeBuilder.addFunction(whatsMyName1.build())
      writeToFile(typeBuilder,symbol.packageName.asString(), fileName, symbol)
    }
    return symbols.filter { !it.validate() }.toList()
  }

  private fun processReplaceMethod(resolver: Resolver): List<KSAnnotated> {
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

  private fun writeToFile(
    typeBuilder: TypeSpec.Builder,
    packageName:String,
    fileName: String,
    symbol: KSAnnotated
  ) {
    val typeSpec = typeBuilder.build()
    val kotlinFile = FileSpec.builder(packageName, fileName).addType(typeSpec)
      .build()
    codeGenerator
      .createNewFile(
        Dependencies(false, symbol.containingFile!!),
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
}