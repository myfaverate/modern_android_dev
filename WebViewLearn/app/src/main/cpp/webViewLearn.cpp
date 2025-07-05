#include<memory>
#include<string>
#include<optional>
#include<jni.h>
#include<android/log.h>
#include<format>
#include<random>
#include <memory>
#include<iostream>

#include "add.hpp"
#include "Person.hpp"
#include "logging.hpp"

constexpr const char* TAG = "webViewLearn";

extern "C" JNIEXPORT jstring JNICALL
Java_edu_tyut_webviewlearn_utils_NativeUtils_stringFromJNI(
        JNIEnv *env,
        jobject
) {
    std::uniform_int_distribution<int> dist = std::uniform_int_distribution<int>(0, 10000);
    std::mt19937 gen(std::random_device{}());
    std::unique_ptr<Person> person = std::make_unique<Person>("name", 18, "nana");
    std::string name = std::format("Hello World 世界C++ value: {}, add: {}, person: {}", dist(gen),
                                   add(100, 250), person->toString());
    logger::info(TAG, "webView: %s", name.c_str());
    std::cout << person << "\n";
    return env->NewStringUTF(name.c_str());
}
