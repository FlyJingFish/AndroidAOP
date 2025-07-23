package com.flyjingfish.android_aop_ksp

import com.google.devtools.ksp.symbol.KSFile
import com.squareup.kotlinpoet.TypeSpec

data class KtFileConfig(
    val typeBuilder: TypeSpec.Builder,
    val packageName:String,
    val fileName: String,
    val sourceFile: KSFile?
)