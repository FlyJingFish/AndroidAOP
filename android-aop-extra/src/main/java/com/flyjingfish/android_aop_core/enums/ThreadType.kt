package com.flyjingfish.android_aop_core.enums

enum class ThreadType {
    /**
     * 单线程池
     */
    SingleIO,

    /**
     * 多线程池
     */
    MultipleIO,

    /**
     * 磁盘读写线程池,本质上是 [SingleIO]
     */
    DiskIO,

    /**
     * 网络请求线程池,本质上是 [MultipleIO]
     */
    NetworkIO
}