package com.flyjingfish.test_lib.mycut

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.flyjingfish.android_aop_annotation.ProceedJoinPoint
import com.flyjingfish.android_aop_annotation.base.BasePointCut
import com.flyjingfish.test_lib.ToastUtils
import com.flyjingfish.test_lib.annotation.MyAnno2

class MyAnnoCut2 : BasePointCut<MyAnno2> {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun invoke(joinPoint: ProceedJoinPoint, anno: MyAnno2): Any? {
        val params = joinPoint.targetMethod.parameters
        for (parameter in params) {
            Log.e("MyAnnoCut2" , "Parameter: "+parameter.name)

            // 获取并打印参数的注解

            // 获取并打印参数的注解
            val annotations: Array<Annotation> = parameter.annotations
            for (annotation in annotations) {
                if (annotation is TestParams) {
                    Log.e("MyAnnoCut2" , "Annotation value1: "+annotation.value)
                }else if (annotation is TestParams2) {
                    Log.e("MyAnnoCut2" , "Annotation value1: "+annotation.value)
                }
            }
        }
        val parameterAnnotations = joinPoint.targetMethod.parameterAnnotations
        Log.e("MyAnnoCut2" , "parameterAnnotations: "+parameterAnnotations.size)
        for (parameterAnnotation in parameterAnnotations) {
            for (annotation in parameterAnnotation) {
                if (annotation is TestParams) {
                    Log.e("MyAnnoCut2" , "Annotation value3: "+annotation.value)
                }else if (annotation is TestParams2) {
                    Log.e("MyAnnoCut2" , "Annotation value3: "+annotation.value)
                }
            }
        }
        ToastUtils.makeText(joinPoint.target as Context,"进入自定义Kotlin注解切面")
        return joinPoint.proceed()
    }
}