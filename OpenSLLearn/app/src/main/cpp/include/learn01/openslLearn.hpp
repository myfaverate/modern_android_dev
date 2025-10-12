//
// Created by 29051 on 2025/9/20.
//

#ifndef OPENSL_LEARN_OPENSL_LEARN_HPP
#define OPENSL_LEARN_OPENSL_LEARN_HPP

#include <thread>
#include <chrono>
#include <format>
#include <istream>
#include <vector>
#include <fstream>

#include <jni.h>
#include <android/looper.h>

extern "C" {
#include <SLES/OpenSLES.h>
#include <SLES/OpenSLES_Android.h>
}

#include "logging.hpp"

class OpenSLESPlayer {
private:
    int fd;
    SLObjectItf engineObject = nullptr;
    SLObjectItf outputMixObject = nullptr;
    SLObjectItf playerObject = nullptr;
    // 下面不需要自己回收
    SLPlayItf playerPlay = nullptr;
    SLSeekItf playerSeek = nullptr;
public:
    explicit OpenSLESPlayer(int fd);
    ~OpenSLESPlayer();
    bool play();
    bool pause();
    bool stop();
    bool seek(uint32_t position);
    uint32_t getDuration();
};

struct RecorderCallbackContext {
    std::ofstream *pcmFile;
    std::vector<char> *buffer;
    SLRecordItf recorderRecord;
};

class OpenSLESRecorder {
private:
    SLObjectItf engineObject = nullptr;
    SLObjectItf recorderObject = nullptr;
    RecorderCallbackContext *context = nullptr;
    // 下面不需要自己回收
    SLRecordItf recorderRecord = nullptr;
public:
    explicit OpenSLESRecorder();
    ~OpenSLESRecorder();
};

#endif // OPENSL_LEARN_OPENSL_LEARN_HPP