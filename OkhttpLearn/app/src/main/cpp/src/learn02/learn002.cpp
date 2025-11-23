//
// Created by 29051 on 2025/11/22.
//
#include "learn002.hpp"

static const char * const TAG = "learn002";

namespace learn02 {
    void hello002(){
        logger::info(TAG, "learn02 learn002...");
    }
}
extern "C"
JNIEXPORT void JNICALL
Java_io_github_okhttplearn_utils_Utils_nativeHello02(JNIEnv *env, jobject thiz) {
    learn02::hello002();
}