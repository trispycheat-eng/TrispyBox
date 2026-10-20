#include <jni.h>
#include <string>
#include <cstring>
#include <android/log.h>
#include <sys/system_properties.h>
#include <dlfcn.h>

#define LOG_TAG "TrispyEngine"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

// Hook function for property spoofing
static int (*orig_prop_get)(const char* name, char* value) = nullptr;

int fake_prop_get(const char* name, char* value) {
    if (name == nullptr) return 0;

    // 1. KILL PC LOGO / EMULATOR DETECT
    if (strcmp(name, "ro.kernel.qemu") == 0) { strcpy(value, "0"); return 1; }
    if (strcmp(name, "ro.kernel.qemu.gles") == 0) { strcpy(value, "0"); return 1; }
    if (strcmp(name, "ro.boot.qemu") == 0) { strcpy(value, "0"); return 1; }
    if (strcmp(name, "ro.hardware") == 0) { strcpy(value, "qcom"); return 4; }

    // 2. SPOOF SAMSUNG DEVICE
    if (strcmp(name, "ro.product.model") == 0) { strcpy(value, "SM-S928B"); return 8; }
    if (strcmp(name, "ro.product.brand") == 0) { strcpy(value, "samsung"); return 7; }
    if (strcmp(name, "ro.product.manufacturer") == 0) { strcpy(value, "samsung"); return 7; }
    if (strcmp(name, "ro.product.device") == 0) { strcpy(value, "e3q"); return 3; }
    if (strcmp(name, "ro.board.platform") == 0) { strcpy(value, "sm8650"); return 6; }

    // 3. HIDE SANDBOX / ROOT TRACES
    if (strcmp(name, "ro.secure") == 0) { strcpy(value, "1"); return 1; }
    if (strcmp(name, "ro.debuggable") == 0) { strcpy(value, "0"); return 1; }
    if (strcmp(name, "ro.build.type") == 0) { strcpy(value, "user"); return 4; }

    // Fallback to real value
    if (orig_prop_get) {
        return orig_prop_get(name, value);
    }
    return __system_property_get(name, value);
}

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void* reserved) {
    LOGI("Trispy Space Native Engine Loaded");
    
    // In the next step, we will use Dobby to apply this hook globally.
    // For now, the logic is built and ready.
    
    return JNI_VERSION_1_6;
}
