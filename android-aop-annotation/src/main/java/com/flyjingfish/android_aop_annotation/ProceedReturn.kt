package com.flyjingfish.android_aop_annotation

class ProceedReturn(targetClass: Class<*>, args: Array<Any?>?, target: Any?) :
    ProceedReturn2(targetClass, args, target) {

    /**
     * 继续执行 suspend 函数的返回值代码块
     *
     * @return 返回 suspend 函数的返回值代码块的结果
     */
    fun proceed(): Any? {
        return super.realProceed()
    }

}