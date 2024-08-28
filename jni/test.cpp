#include <jni.h>
#include <android/log.h>

void hello_test_jni()
{
    __android_log_print(ANDROID_LOG_ERROR,"JNI","hello JNI test!!!");
}

extern "C"
{


JNIEXPORT void JNICALL Java_com_flyjingfish_androidaop_NativeUtils_hello_1jni
(JNIEnv *, jobject)
{
    hello_test_jni();
}

}