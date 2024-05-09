package com.flyjingfish.android_aop_plugin.tasks

import com.flyjingfish.android_aop_plugin.config.AndroidAopConfig
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

abstract class SyncConfigTask : DefaultTask() {
    @TaskAction
    fun taskAction() {
        AndroidAopConfig.syncConfig(project)
    }
}