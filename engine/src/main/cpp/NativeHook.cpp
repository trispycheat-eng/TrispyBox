#include <jni.h>
#include <string>
#include <cstring>
#include <android/log.h>
#include <sys/system_properties.h>
#include <dlfcn.h>

#define LOG_TAG "TrispyEngine"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void* reserved) {
    LOGI("Trispy Space Native Engine Loaded");
    return JNI_VERSION_1_6;
}
