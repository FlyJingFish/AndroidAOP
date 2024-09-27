package com.flyjingfish.android_aop_plugin.config

import com.android.build.gradle.AppPlugin
import com.flyjingfish.android_aop_plugin.plugin.CompilePlugin
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
     * 增量加速，有一定增速效果，默认开启
     */
    @Deprecated("已弃用，再使用此配置已无作用")
    var increment = true
    /**
     * 包含规则,可以精确到直接使用类名
     */
    val includes = mutableListOf<String>()
    /**
     * 排除规则,可以精确到直接使用类名
     */
    val excludes = mutableListOf<String>()

    /**
     * 包含规则,可以精确到直接使用类名
     */
    fun include(vararg filters: String): AndroidAopConfig {
        this.includes.addAll(filters)
        return this
    }

    /**
     * 排除规则,可以精确到直接使用类名
     */
    fun exclude(vararg filters: String): AndroidAopConfig {
        this.excludes.addAll(filters)
        return this
    }


    internal fun initConfig(){
        AndroidAopConfig.debug = debug
        AndroidAopConfig.includes.clear()
        AndroidAopConfig.excludes.clear()
        AndroidAopConfig.includes.addAll(includes)
        AndroidAopConfig.excludes.addAll(excludes)
        AndroidAopConfig.excludes.add(Utils.annotationPackage)
        AndroidAopConfig.excludes.add(Utils.corePackage)
        AndroidAopConfig.excludes.add(Utils.extraPackage)
        AndroidAopConfig.verifyLeafExtends = verifyLeafExtends
        AndroidAopConfig.cutInfoJson = cutInfoJson
//        AndroidAopConfig.increment = increment
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
        var increment = false

        internal fun syncConfig(project: Project){
            val androidAopConfig = project.extensions.getByType(AndroidAopConfig::class.java)
            androidAopConfig.initConfig()

            deepExportConfigJson(project,project.rootProject,androidAopConfig)
        }

        private fun deepExportConfigJson(appProject: Project,project: Project,androidAopConfig:AndroidAopConfig){
            val childProjects = project.childProjects
            if (childProjects.isEmpty()){
                return
            }
            for (childProject in childProjects) {
                if (appProject != childProject.value){
                    val configFile = File(Utils.configJsonFile(childProject.value))
                    InitConfig.exportConfigJson(configFile,androidAopConfig)
                    deepExportConfigJson(appProject,childProject.value,androidAopConfig)
                }
            }
        }
    }


}