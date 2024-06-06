#include <jni.h>
#include <string>

extern "C" JNIEXPORT jstring JNICALL
Java_com_playtime_sdk_PlaytimeSDK_getBaseUrl(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "https://appcampaign.in/playtime_sdk/api101/";
    return env->NewStringUTF(hello.c_str());
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_playtime_sdk_PlaytimeSDK_getUrl(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "https://appcampaign.in/playtime_sdk/web_view/index.php";
    return env->NewStringUTF(hello.c_str());
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_playtime_sdk_PlaytimeSDK_getMIV(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "wegw6g4e68v468v4";
    return env->NewStringUTF(hello.c_str());
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_playtime_sdk_PlaytimeSDK_getKey(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "rhheh165r6r6tgh7";
    return env->NewStringUTF(hello.c_str());
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_playtime_sdk_PlaytimeSDK_getPName(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "PlaytimeSDK";
    return env->NewStringUTF(hello.c_str());
}