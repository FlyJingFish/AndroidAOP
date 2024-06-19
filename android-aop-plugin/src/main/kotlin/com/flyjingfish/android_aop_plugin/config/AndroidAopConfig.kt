package com.flyjingfish.android_aop_plugin.config

import com.flyjingfish.android_aop_plugin.utils.InitConfig
import com.flyjingfish.android_aop_plugin.utils.Utils
import org.gradle.api.Project
import java.io.File

open class AndroidAopConfig {
    /**
     * 是否启用，默认启用
     */
    var enabled = true
    /**
     * 是否开启debug模式，输出调试相关信息，以便排查问题，默认关闭
     */
    var debug = false
    /**
     * 是否开启验证叶子继承，默认打开，如果没有设置 AndroidAopMatchClassMethod 的 type = MatchType.LEAF_EXTENDS，可以关闭
     */
    var verifyLeafExtends = true
    /**
     * 是否生成切点信息Json
     */
    var cutInfoJson = false

    /**
     * 增量加速，有一定增速效果，默认开启，如遇问题，可选择关闭调试
     */
    var increment = true
    /**
     * 包含规则
     */
    val includes = mutableListOf<String>()
    /**
     * 排除规则
     */
    val excludes = mutableListOf<String>()

    fun include(vararg filters: String): AndroidAopConfig {
        this.includes.addAll(filters)
        return this
    }

    fun exclude(vararg filters: String): AndroidAopConfig {
        this.excludes.addAll(filters)
        return this
    }


    internal fun initConfig(){
        AndroidAopConfig.debug = debug
        AndroidAopConfig.includes.clear()
        AndroidAopConfig.excludes.clear()
        includes.forEach {
            AndroidAopConfig.includes.add("$it.")
        }
        excludes.forEach {
            AndroidAopConfig.excludes.add("$it.")
        }
        AndroidAopConfig.excludes.add(Utils.annotationPackage)
        AndroidAopConfig.excludes.add(Utils.corePackage)
        AndroidAopConfig.verifyLeafExtends = verifyLeafExtends
        AndroidAopConfig.cutInfoJson = cutInfoJson
        AndroidAopConfig.increment = increment
    }

    override fun toString(): String {
        return "AndroidAopConfig(enabled=$enabled, debug=$debug, verifyLeafExtends=$verifyLeafExtends, cutInfoJson=$cutInfoJson, increment=$increment, includes=$includes, excludes=$excludes)"
    }

    companion object{
        var debug = false
        val includes = mutableListOf<String>()
        val excludes = mutableListOf<String>()
        var verifyLeafExtends = true
        var cutInfoJson = false
        var increment = true

        internal fun syncConfig(project: Project){
            val androidAopConfig = project.extensions.getByType(AndroidAopConfig::class.java)
            androidAopConfig.initConfig()

            for (childProject in project.rootProject.childProjects) {
                if (project != childProject.value){
                    val configFile = File(Utils.configJsonFile(childProject.value))
                    InitConfig.exportConfigJson(configFile,androidAopConfig)
                }
            }
        }
    }


}