package com.flyjingfish.android_aop_ksp
import com.flyjingfish.android_aop_annotation.anno.AndroidAopClass
import com.flyjingfish.android_aop_annotation.anno.AndroidAopMatch
import com.flyjingfish.android_aop_annotation.anno.AndroidAopMatchClassMethod
import com.flyjingfish.android_aop_annotation.anno.AndroidAopMethod
import com.flyjingfish.android_aop_annotation.anno.AndroidAopPointCut
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.Origin
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec
import java.lang.annotation.ElementType
import java.lang.annotation.RetentionPolicy

class AndroidAopSymbolProcessor(private val codeGenerator: CodeGenerator,
                                private val logger: KSPLogger
) : SymbolProcessor {
  private val packageName = "com.flyjingfish.android_aop_core.aop"
  override fun process(resolver: Resolver): List<KSAnnotated> {
//    logger.error("---------AndroidAopSymbolProcessor---------")
    val ret1 = processPointCut(resolver)
    val ret2 = processMatch(resolver)
    val ret = arrayListOf<KSAnnotated>()
    ret.addAll(ret1)
    ret.addAll(ret2)
    return ret
  }

  fun processPointCut(resolver: Resolver):List<KSAnnotated>{
    val symbols = resolver.getSymbolsWithAnnotation(AndroidAopPointCut::class.qualifiedName!!)
    for (symbol in symbols) {
      var targetClassName :String ?= null
      var isRetention = false
      for (annotation in symbol.annotations) {

        for (argument in annotation.arguments) {
          if (annotation.toString() == "@AndroidAopPointCut") {
            if (argument.name?.getShortName() == "value") {
              targetClassName =
                (argument.value as KSType).declaration.packageName.asString() + "." + argument.value.toString()
//              logger.error("argument.value = ${(argument.value as KSType).declaration.packageName.asString()}")
            }
          }else if (annotation.toString() == "@Target") {
            if (argument.name?.getShortName() == "value") {
              if (argument.value is ElementType){
                val value = argument.value
                if (ElementType.METHOD != value){
                  throw IllegalArgumentException("注意：请给 $symbol 设置 @Target 为 METHOD ")
                }
              }else if (argument.value is ArrayList<*>){
                val value : ArrayList<*> = argument.value as ArrayList<*>
                for (s in value) {
                  if (symbol.origin == Origin.JAVA){
                    if ("METHOD" != s.toString()){
                      throw IllegalArgumentException("注意：请给 $symbol 设置 @Target 为 METHOD ")
                    }
                  }else if (symbol.origin == Origin.KOTLIN){
                    if ("kotlin.annotation.AnnotationTarget.FUNCTION" != s.toString()){
                      throw IllegalArgumentException("注意：请给 $symbol 设置 @Target 为 FUNCTION ")
                    }
                  }
                }
              }else if (argument.value is Array<*>){
                val value : Array<*> = argument.value as Array<*>
                for (s in value) {
                  if (symbol.origin == Origin.KOTLIN){
                    if ("kotlin.annotation.AnnotationTarget.FUNCTION" != s.toString()){
                      throw IllegalArgumentException("注意：请给 $symbol 设置 @Target 为 FUNCTION ")
                    }
                  }
                }
              }
              logger.error("Target = ${argument.value?.javaClass?.name}")
//              val retention = argument.value.toString()
            }
          }else if (annotation.toString() == "@Retention") {
            isRetention = true
            if (argument.name?.getShortName() == "value") {
              val retention = argument.value.toString()
              if ((symbol.origin == Origin.JAVA && "RUNTIME" != retention) || (symbol.origin == Origin.KOTLIN && "kotlin.annotation.AnnotationRetention.RUNTIME" != retention)){
                throw IllegalArgumentException("注意：请给 $symbol 设置 @Retention 为 RUNTIME ")
              }
            }
          }
        }
//        logger.error("className = ${symbol},classAllName = ${(symbol as KSClassDeclaration).packageName.asString()},targetClassName=${targetClassName} methodName=${methodNames}")
      }
      if (targetClassName == null){
        continue
      }
      if (!isRetention){
        throw IllegalArgumentException("注意：请给 $symbol 设置 @Retention 为 RUNTIME ")
      }

      val className = (symbol as KSClassDeclaration).packageName.asString() +"."+ symbol

      val fileName = "${symbol}\$\$AndroidAopClass";
      val typeBuilder = TypeSpec.classBuilder(
        fileName
      ).addModifiers(KModifier.FINAL)
        .addAnnotation(AndroidAopClass::class)

      val whatsMyName1 = whatsMyName("withinAnnotatedClass")
        .addAnnotation(
          AnnotationSpec.builder(AndroidAopMethod::class)
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

      val typeSpec = typeBuilder.build()
      val javaFile = FileSpec.builder(packageName,fileName).addType(typeSpec)
        .build()
      codeGenerator
        .createNewFile(
          Dependencies.ALL_FILES,
          "com.flyjingfish.android_aop_core.aop",
          fileName
        )
        .writer()
        .use { javaFile.writeTo(it) }
    }
    val ret = symbols.filter { !it.validate() }.toList()
    return ret
  }

  fun processMatch(resolver: Resolver):List<KSAnnotated>{
    val symbols = resolver.getSymbolsWithAnnotation(AndroidAopMatchClassMethod::class.qualifiedName!!)
    for (symbol in symbols) {
      var targetClassName :String ?= null
      var methodNames :ArrayList<String> ?= null
      for (annotation in symbol.annotations) {


        for (argument in annotation.arguments) {
          if (annotation.toString() == "@AndroidAopMatchClassMethod") {
            if (argument.name?.getShortName() == "targetClassName") {
              targetClassName = argument.value.toString()
            }
            if (argument.name?.getShortName() == "methodName") {
              methodNames = argument.value as ArrayList<String>
            }
          }
        }
//        logger.error("className = ${symbol},classAllName = ${(symbol as KSClassDeclaration).packageName.asString()},targetClassName=${targetClassName} methodName=${methodNames}")

      }
      val className = (symbol as KSClassDeclaration).packageName.asString() +"."+ symbol
      if (targetClassName == null || methodNames == null){
        continue
      }
      val fileName = "${symbol}\$\$AndroidAopClass";
      val typeBuilder = TypeSpec.classBuilder(
        fileName
      ).addModifiers(KModifier.FINAL)
        .addAnnotation(AndroidAopClass::class)
      val stringBuilder = StringBuilder()
      for (i in methodNames.indices) {
        stringBuilder.append(methodNames[i])
        if (i != methodNames.size - 1) {
          stringBuilder.append("-")
        }
      }
      val whatsMyName1 = whatsMyName("withinAnnotatedClass")
        .addAnnotation(
          AnnotationSpec.builder(AndroidAopMatch::class)
            .addMember(
              "baseClassName = %S",
              targetClassName
            )
            .addMember(
              "methodNames = %S",
              stringBuilder
            )
            .addMember(
              "pointCutClassName = %S",
              className
            )
            .build()
        )

      typeBuilder.addFunction(whatsMyName1.build())

      val typeSpec = typeBuilder.build()
      val javaFile = FileSpec.builder(packageName,fileName).addType(typeSpec)
        .build()
      codeGenerator
        .createNewFile(
          Dependencies.ALL_FILES,
          "com.flyjingfish.android_aop_core.aop",
          fileName
        )
        .writer()
        .use { javaFile.writeTo(it) }
    }
    val ret = symbols.filter { !it.validate() }.toList()
//    val aopList = symbols
//      .filter { it is KSPropertyDeclaration && it.validate() }
//      .map { it as KSPropertyDeclaration }.toList()
//    generate(codeGenerator, logger, aopList)
    return ret
  }

  fun generate(
    codeGenerator: CodeGenerator, logger: KSPLogger, list: List<KSPropertyDeclaration>
  ) {
// 将获取的符号按包名与类名分组
    val map = list.groupBy {
      val parent = it.parent as KSClassDeclaration
      val key = "${parent::class.java.simpleName},${parent.packageName.asString()}"
      key
    }

    map.forEach {
      val classItem = it.value[0].parent as KSClassDeclaration
      // 添加文件
      val fileSpecBuilder = FileSpec.builder(
        classItem.packageName.asString(),
        "${classItem::class.java.simpleName}ViewBind"
      )
      logger.error("${classItem::class.java.simpleName}ViewBind")

      // 添加方法
      val functionBuilder = FunSpec.builder("bindView")
        .receiver(classItem::class.java)

      it.value.forEach { item ->
        // 获取属性名与注解的值
        val symbolName = item.simpleName.asString()
        val annotationValue =
          (item.annotations.firstOrNull()?.arguments?.firstOrNull()?.value as? Int) ?: 0
        functionBuilder.addStatement("$symbolName = findViewById(${annotationValue})")
      }
      // 写文件
      codeGenerator
        .createNewFile(
          Dependencies.ALL_FILES,
          "com.flyjingfish.android_aop_core.aop",
          "${classItem::class.java.simpleName}ViewBind"
        )
        .writer()
        .use { fileSpecBuilder.addFunction(functionBuilder.build())
          .build().writeTo(it) }

    }
  }

  private fun whatsMyName(name: String): FunSpec.Builder {
    return FunSpec.builder(name).addModifiers(KModifier.FINAL)
  }
}