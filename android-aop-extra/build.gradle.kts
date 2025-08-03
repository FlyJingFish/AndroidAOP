plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
}

// 引入外部 gradle 脚本
apply(from = "$rootDir/gradle/android_base.gradle")
apply(from = "$rootDir/gradle/android_publish.gradle")

android {
    namespace = "com.flyjingfish.android_aop_extra"
}

//ksp {
//    useKsp2 = true
//}

dependencies {
    implementation(project(path = ":android-aop-annotation"))
    implementation(project(path = ":android-aop-core"))
    ksp(project(":android-aop-apt"))
}
