package com.flyjingfish.android_aop_plugin.utils


import com.android.build.gradle.BaseExtension
import org.gradle.api.Project
import java.io.File

/**
 * Android相关配置
 */
class AndroidConfig(project: Project) {

    private val android: BaseExtension = project.extensions.getByName("android") as BaseExtension

    /**
     * Return boot classpath.
     * @return Collection of classes.
     */
    fun getBootClasspath(): List<File> {
        return android.bootClasspath
    }
}