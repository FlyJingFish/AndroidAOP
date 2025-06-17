package com.flyjingfish.android_aop_plugin.utils


import java.io.File
import java.io.Serializable

/**
 * Android相关配置
 */
data class AndroidConfig(private var bootClasspath: List<File> ?= null):Serializable {

    fun setBootClasspath(list :List<File>){
        bootClasspath = list
    }
    /**
     * Return boot classpath.
     * @return Collection of classes.
     */
    fun getBootClasspath(): List<File> {
        return bootClasspath ?: emptyList()
    }
}