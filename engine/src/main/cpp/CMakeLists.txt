cmake_minimum_required(VERSION 3.22.1)
project("trispy_native")

add_library(
    trispy_native
    SHARED
    NativeHook.cpp
    AceShield.cpp
)

target_link_libraries(
    trispy_native
    log
    android
)
