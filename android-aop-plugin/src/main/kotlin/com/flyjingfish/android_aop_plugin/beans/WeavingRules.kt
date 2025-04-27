package com.flyjingfish.android_aop_plugin.beans


data class WeavingRules(
    val excludeWeaving: List<String>,
    val includeWeaving: List<String>
)