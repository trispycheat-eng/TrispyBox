#include <jni.h>
#include <string>
#include <cstring>
#include <android/log.h>
#include <sys/system_properties.h>

#define LOG_TAG "TrispyEngine"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

static int (*orig_prop_get)(const char* name, char* value) = nullptr;

// Spoofing function
int fake_prop_get(const char* name, char* value) {
    if (name == nullptr) return 0;

    // KILL EMULATOR TRACES (Fixes PC Logo)
    if (strcmp(name, "ro.kernel.qemu") == 0) { strcpy(value, "0"); return 1; }
    if (strcmp(name, "ro.kernel.qemu.gles") == 0) { strcpy(value, "0"); return 1; }
    if (strcmp(name, "ro.hardware") == 0) { strcpy(value, "qcom"); return 4; }

    // SPOOF SAMSUNG GALAXY S24 ULTRA
    if (strcmp(name, "ro.product.model") == 0) { strcpy(value, "SM-S928B"); return 8; }
    if (strcmp(name, "ro.product.brand") == 0) { strcpy(value, "samsung"); return 7; }
    if (strcmp(name, "ro.product.manufacturer") == 0) { strcpy(value, "samsung"); return 7; }
    if (strcmp(name, "ro.board.platform") == 0) { strcpy(value, "sm8650"); return 6; }
    if (strcmp(name, "ro.build.fingerprint") == 0) { strcpy(value, "samsung/e3qxeea/e3q:14/UP1A.231005.007/S928BXXU1AWLM:user/release-keys"); return 70; }

    // HIDE SANDBOX/MAGISK TRACES
    if (strcmp(name, "ro.secure") == 0) { strcpy(value, "1"); return 1; }
    if (strcmp(name, "ro.debuggable") == 0) { strcpy(value, "0"); return 1; }

    if (orig_prop_get) {
        return orig_prop_get(name, value);
    }
    return __system_property_get(name, value);
}

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void* reserved) {
    LOGI("Trispy Space C++ Engine Loaded. Spoofing Active.");
    // We will apply the inline hook to fake_prop_get here in Step 2.
    return JNI_VERSION_1_6;
}
