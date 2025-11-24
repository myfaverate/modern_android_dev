//
// Created by 29051 on 2025/11/22.
//

#ifndef OKHTTPLEARN_LEARN001_HPP
#define OKHTTPLEARN_LEARN001_HPP

#include <jni.h>
#include <malloc.h>
#include <media/NdkMediaCodec.h>
#include <media/NdkMediaFormat.h>
#include <android/api-level.h>

#include <stdexcept>
#include <exception>
#include <chrono>
#include <fstream>
#include <thread>

#include "logging.hpp"

namespace learn01 {
    void hello001();
    class Encoder{
    private:
        AMediaCodec *mediaCodec = nullptr;
        AMediaFormat *mediaFormat = nullptr;
        std::ifstream *yuvFile = nullptr;
        std::ofstream *h264File = nullptr;
        typedef media_status_t (*AMediaCodec_setAsyncNotifyCallback_Type)(
                AMediaCodec*, AMediaCodecOnAsyncNotifyCallback, void *
        );
        AMediaCodec_setAsyncNotifyCallback_Type pSetAsyncNotifyCallback = nullptr;
        void* libHandle;
    public:
        Encoder();
        ~Encoder();
        uint64_t getMicroseconds();
        void load_media_codec_async_function();
    };
}
#endif //OKHTTPLEARN_LEARN001_HPP
