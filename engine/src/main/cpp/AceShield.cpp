#include <jni.h>
#include <string>
#include <cstring>
#include <unistd.h>
#include <dirent.h>
#include <pthread.h>
#include <signal.h>
#include <sys/types.h>
#include <sys/syscall.h>
#include <sys/system_properties.h>
#include <android/log.h>

#define LOG_TAG "TrispyShield"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

// 1. Property Spoofer (Hides Sandbox, Spoofs Samsung)
static int (*orig_prop_get)(const char* name, char* value) = nullptr;

int fake_prop_get(const char* name, char* value) {
    if (name == nullptr) return 0;
    if (strcmp(name, "ro.kernel.qemu") == 0) { strcpy(value, "0"); return 1; }
    if (strcmp(name, "ro.hardware") == 0) { strcpy(value, "qcom"); return 4; }
    if (strcmp(name, "ro.product.model") == 0) { strcpy(value, "SM-S928B"); return 8; }
    if (strcmp(name, "ro.product.brand") == 0) { strcpy(value, "samsung"); return 7; }
    if (strcmp(name, "ro.product.manufacturer") == 0) { strcpy(value, "samsung"); return 7; }
    if (orig_prop_get) return orig_prop_get(name, value);
    return __system_property_get(name, value);
}

// 2. The ACE Thread Freezer
void freezeACE() {
    DIR* dir = opendir("/proc/self/task");
    if (!dir) return;
    struct dirent* entry;
    while ((entry = readdir(dir)) != NULL) {
        if (entry->d_name[0] < '0' || entry->d_name[0] > '9') continue;
        
        char path[256];
        snprintf(path, sizeof(path), "/proc/self/task/%s/comm", entry->d_name);
        FILE* f = fopen(path, "r");
        if (f) {
            char comm[64] = {0};
            if (fgets(comm, sizeof(comm), f)) {
                // If the thread is ACE, Terrors, or AntiCheat, pause it (SIGSTOP)
                if (strstr(comm, "ACE") || strstr(comm, "terrors") || strstr(comm, "Anti")) {
                    int tid = atoi(entry->d_name);
                    syscall(SYS_tgkill, getpid(), tid, SIGSTOP);
                    LOGI("Froze ACE Thread: %s", comm);
                }
            }
            fclose(f);
        }
    }
    closedir(dir);
}

// 3. Background Watchdog
static void* shield_thread(void*) {
    sleep(3); // Wait for FF to load
    while (true) {
        freezeACE();
        sleep(2); // Freeze every 2 seconds
    }
    return nullptr;
}

__attribute__((constructor))
void shield_init() {
    pthread_t tid;
    pthread_create(&tid, NULL, shield_thread, NULL);
}
