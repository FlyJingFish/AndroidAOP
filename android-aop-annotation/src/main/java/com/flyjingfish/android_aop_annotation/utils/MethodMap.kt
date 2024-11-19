package com.flyjingfish.android_aop_annotation.utils

import java.lang.reflect.Method

internal class MethodMap(val originalMethod: Method,val targetMethod: Method,val targetStaticMethod: Method?)