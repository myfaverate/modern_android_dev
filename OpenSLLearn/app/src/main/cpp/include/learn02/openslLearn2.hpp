//
// Created by 29051 on 2025/9/20.
//

#ifndef OPENSL_LEARN_OPENSL_LEARN1_HPP
#define OPENSL_LEARN_OPENSL_LEARN1_HPP

#include <jni.h>
#include <stdexcept> // std::runtime_error
#include <algorithm>  // std::min
#include <oboe/oboe.h>
#include <aaudio/AAudio.h>
#include <sqlite3.h>

#include "logging.hpp"

extern "C" {
#include <unistd.h> // dup
}

class AudioPlayer {
private:
    int *fd;
    AAudioStream *mAudioStream = nullptr;
    AAudioStreamBuilder *builder = nullptr;
public:
    explicit AudioPlayer(int fd);
    ~AudioPlayer();
};

class AudioRecord{
private:
    int *fd;
    AAudioStream *mAudioStream = nullptr;
    AAudioStreamBuilder *builder = nullptr;
public:
    explicit AudioRecord(int fd);
    ~AudioRecord();
};

class OboePlayer : public oboe::AudioStreamCallback{
private:
    std::shared_ptr<oboe::AudioStream> stream;
    int fd;
public:
    explicit OboePlayer(int fd);
    ~OboePlayer() override;
    void startPlay();
    oboe::DataCallbackResult onAudioReady(oboe::AudioStream *audioStream,
                                          void *audioData,
                                          int32_t numFrames) override;
};

class OboeRecord : public oboe::AudioStreamCallback{
private:
    std::shared_ptr<oboe::AudioStream> stream;
    int fd;
public:
    explicit OboeRecord(int fd);
    ~OboeRecord() override;
    void startRecord();
    oboe::DataCallbackResult onAudioReady(oboe::AudioStream *audioStream,
                                          void *audioData,
                                          int32_t numFrames) override;
};

extern "C"
JNIEXPORT jstring JNICALL
Java_io_github_opensllearn_utils_Utils_hello1(JNIEnv *env, jobject thiz);

#endif //OPENSL_LEARN_OPENSL_LEARN1_HPP
