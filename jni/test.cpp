#include <jni.h>
#include <android/log.h>
#include <stdio.h>

void hello_test_jni(JNIEnv *env, jobject thiz) {
    __android_log_print(ANDROID_LOG_ERROR,"JNI","hello JNI test!!!");

    // 获取 DeviceInfo 类
    jclass deviceInfoClass = env->GetObjectClass(thiz);

    // 获取 getTelephonyManager 方法 ID
    jmethodID getTelephonyManagerMethod = env->GetMethodID(deviceInfoClass, "getTelephonyManager", "()Landroid/telephony/TelephonyManager;");

    // 调用 getTelephonyManager 方法，获取 TelephonyManager 对象
    jobject tm = env->CallObjectMethod(thiz, getTelephonyManagerMethod);

    // 获取 TelephonyManager 类
    jclass telephonyManagerClass = env->FindClass("android/telephony/TelephonyManager");

    // 获取 getDeviceId 方法 ID
    jmethodID getDeviceIdMethod = env->GetMethodID(telephonyManagerClass, "getDeviceId", "()Ljava/lang/String;");

    // 调用 getDeviceId 方法
    jstring deviceId = (jstring) env->CallObjectMethod(tm, getDeviceIdMethod);


    // 查找 Java 类
    jclass reflectExampleClass = (*env).FindClass( "com/flyjingfish/androidaop/ReflectExample");

    if (reflectExampleClass != nullptr) {
        // 获取构造方法 ID
        jmethodID constructor = (*env).GetMethodID( reflectExampleClass, "<init>", "()V");

        // 创建对象
        jobject reflectExampleObject = (*env).NewObject( reflectExampleClass, constructor);

        // 查找方法
        jmethodID sayHelloMethod = (*env).GetMethodID(reflectExampleClass, "sayHello",
                                                       "(Ljava/lang/String;)V");

        if (sayHelloMethod != nullptr) {

            // 调用方法
            (*env).CallVoidMethod( reflectExampleObject, sayHelloMethod, deviceId);

            // 释放字符串
            (*env).DeleteLocalRef( deviceId);
        }
    }




}

extern "C"
{


JNIEXPORT void JNICALL Java_com_flyjingfish_androidaop_NativeUtils_hello_1jni
        (JNIEnv *env, jobject thiz) {
    hello_test_jni(env,thiz);
}

}