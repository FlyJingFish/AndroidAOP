package com.flyjingfish.android_aop_annotation


interface ProceedReturn2 {

    /**
     *
     * @return suspend 函数的返回值类型,有可能拿不到实际类型，拿不到时返回 Object 类型,请使用 [ProceedJoinPoint.getTargetMethod]并通过[AopMethod.returnType]获取
     */
    @Deprecated(
        "这个方法即将删除，请使用 ProceedJoinPoint 的 targetMethod.returnType",
        ReplaceWith("joinPoint.targetMethod.returnType")
    )
    fun getReturnType(): Class<*>?;

}