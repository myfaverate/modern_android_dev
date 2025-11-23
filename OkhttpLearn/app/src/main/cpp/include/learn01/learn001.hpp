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
/*
// encoder_ndk.cpp
#include <jni.h>
#include <vector>
#include <stdexcept>
#include <android/log.h>
#include <media/NdkMediaCodec.h>
#include <media/NdkMediaFormat.h>
#include <cstring>
#include <memory>

#define TAG "NDK_H264_ENCODER"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

namespace learn01 {

// Some NDKs don't define the semi-planar constant; define if missing.
#ifndef AMEDIA_COLOR_FormatYUV420SemiPlanar
static const int32_t AMEDIA_COLOR_FormatYUV420SemiPlanar = 0x15; // NV12
#endif

class Encoder {
public:
    Encoder(int width, int height, int fps, int bitrate);
    ~Encoder();

    // input: pointer to raw YUV in I420 (Y plane, U plane, V plane) format,
    //        or if inputIsI420 == false, treat as NV12 already.
    // ptsUs: presentation timestamp in microseconds
    bool encodeFrame(const uint8_t* yuv, size_t len, int64_t ptsUs, bool inputIsI420 = true);

    // try to dequeue one encoded packet; returns true if out has data
    bool dequeueEncoded(std::vector<uint8_t>& out, int64_t& outPtsUs, bool& isKeyFrame);

    // signal end-of-stream and flush outputs
    void signalEndOfStream();

    void release();

private:
    bool ensureStarted();
    void convertI420ToNV12(const uint8_t* src, uint8_t* dst);
    AMediaCodec* codec_ = nullptr;
    AMediaFormat* format_ = nullptr;
    int width_;
    int height_;
    int fps_;
    int bitrate_;
    bool started_ = false;
    bool eosSent_ = false;
};

// ---------------- implementation ----------------

Encoder::Encoder(int width, int height, int fps, int bitrate)
    : width_(width), height_(height), fps_(fps), bitrate_(bitrate) {

    codec_ = AMediaCodec_createEncoderByType("video/avc");
    if (!codec_) {
        throw std::runtime_error("AMediaCodec_createEncoderByType failed");
    }

    format_ = AMediaFormat_new();
    if (!format_) {
        AMediaCodec_delete(codec_);
        codec_ = nullptr;
        throw std::runtime_error("AMediaFormat_new failed");
    }

    AMediaFormat_setString(format_, AMEDIA_KEY_MIME, "video/avc");
    AMediaFormat_setInt32(format_, AMEDIA_KEY_WIDTH, width_);
    AMediaFormat_setInt32(format_, AMEDIA_KEY_HEIGHT, height_);
    AMediaFormat_setInt32(format_, AMEDIA_KEY_BIT_RATE, bitrate_);
    AMediaFormat_setInt32(format_, AMEDIA_KEY_FRAME_RATE, fps_);
    AMediaFormat_setInt32(format_, AMEDIA_KEY_I_FRAME_INTERVAL, 1); // 1s GOP

    // Try to request NV12 (semi-planar). If device doesn't honor it, you'll get actual format later.
    AMediaFormat_setInt32(format_, AMEDIA_KEY_COLOR_FORMAT, AMEDIA_COLOR_FormatYUV420SemiPlanar);

    media_status_t st = AMediaCodec_configure(codec_, format_, nullptr, nullptr, AMEDIACODEC_CONFIGURE_FLAG_ENCODE);
    if (st != AMEDIA_OK) {
        // try fallback with flexible format
        LOGE("configure failed with NV12 (%d). Try flexible. Error=%d", AMEDIA_COLOR_FormatYUV420SemiPlanar, st);
        AMediaFormat_setInt32(format_, AMEDIA_KEY_COLOR_FORMAT, AMEDIA_COLOR_FormatYUV420Flexible);
        st = AMediaCodec_configure(codec_, format_, nullptr, nullptr, AMEDIACODEC_CONFIGURE_FLAG_ENCODE);
        if (st != AMEDIA_OK) {
            AMediaFormat_delete(format_);
            format_ = nullptr;
            AMediaCodec_delete(codec_);
            codec_ = nullptr;
            throw std::runtime_error("AMediaCodec_configure failed");
        }
    }

    st = AMediaCodec_start(codec_);
    if (st != AMEDIA_OK) {
        AMediaFormat_delete(format_);
        format_ = nullptr;
        AMediaCodec_delete(codec_);
        codec_ = nullptr;
        throw std::runtime_error("AMediaCodec_start failed");
    }

    started_ = true;
    LOGI("Encoder started (w=%d h=%d fps=%d bitrate=%d)", width_, height_, fps_, bitrate_);
}

Encoder::~Encoder() {
    release();
}

void Encoder::release() {
    if (codec_) {
        AMediaCodec_stop(codec_);
        AMediaCodec_delete(codec_);
        codec_ = nullptr;
    }
    if (format_) {
        AMediaFormat_delete(format_);
        format_ = nullptr;
    }
    started_ = false;
    LOGI("Encoder released");
}

bool Encoder::ensureStarted() {
    return started_ && codec_;
}

void Encoder::convertI420ToNV12(const uint8_t* src, uint8_t* dst) {
    // src layout: Y (w*h), U (w*h/4), V (w*h/4)
    // dst layout NV12: Y (w*h), interleaved UV (w*h/2) as U V U V...
    int wh = width_ * height_;
    int hw = wh / 4;
    const uint8_t* srcY = src;
    const uint8_t* srcU = src + wh;
    const uint8_t* srcV = src + wh + hw;

    // copy Y
    memcpy(dst, srcY, wh);

    // interleave U and V into UV plane
    uint8_t* dstUV = dst + wh;
    for (int i = 0; i < hw; ++i) {
        dstUV[2*i]     = srcU[i]; // U
        dstUV[2*i + 1] = srcV[i]; // V
    }
}

bool Encoder::encodeFrame(const uint8_t* yuv, size_t len, int64_t ptsUs, bool inputIsI420) {
    if (!ensureStarted()) return false;
    if (eosSent_) return false;

    ssize_t inIndex = AMediaCodec_dequeueInputBuffer(codec_, 10000); // 10ms
    if (inIndex < 0) {
        if (inIndex == AMEDIACODEC_INFO_TRY_AGAIN_LATER) {
            return false;
        }
        return false;
    }

    size_t bufSize = 0;
    uint8_t* buf = AMediaCodec_getInputBuffer(codec_, inIndex, &bufSize);
    if (!buf) {
        LOGE("getInputBuffer returned null");
        return false;
    }

    // expected input size: w*h*3/2
    size_t expected = width_ * height_ * 3 / 2;
    // prepare a temporary buffer if conversion needed
    std::unique_ptr<uint8_t[]> tmp;
    const uint8_t* toCopy = nullptr;

    if (inputIsI420) {
        tmp.reset(new uint8_t[expected]);
        convertI420ToNV12(yuv, tmp.get());
        toCopy = tmp.get();
    } else {
        // assume incoming is NV12 with correct size
        toCopy = yuv;
    }

    if (bufSize < expected) {
        LOGE("input buffer too small (%zu) expected %zu", bufSize, expected);
        return false;
    }

    memcpy(buf, toCopy, expected);

    media_status_t st = AMediaCodec_queueInputBuffer(codec_, inIndex, 0, (size_t)expected, (uint64_t)ptsUs, 0);
    if (st != AMEDIA_OK) {
        LOGE("queueInputBuffer failed: %d", st);
        return false;
    }

    return true;
}

bool Encoder::dequeueEncoded(std::vector<uint8_t>& out, int64_t& outPtsUs, bool& isKeyFrame) {
    out.clear();
    isKeyFrame = false;
    if (!ensureStarted()) return false;

    AMediaCodecBufferInfo info;
    ssize_t outIndex = AMediaCodec_dequeueOutputBuffer(codec_, &info, 0);
    if (outIndex >= 0) {
        size_t bufSize = 0;
        uint8_t* buf = AMediaCodec_getOutputBuffer(codec_, outIndex, &bufSize);
        if (info.size > 0 && buf) {
            out.resize(info.size);
            memcpy(out.data(), buf + info.offset, info.size);
            outPtsUs = info.presentationTimeUs;
            if (info.flags & AMEDIACODEC_BUFFER_FLAG_KEY_FRAME) {
                isKeyFrame = true;
            }
            if (info.flags & AMEDIACODEC_BUFFER_FLAG_CODEC_CONFIG) {
                // codec config (SPS/PPS). Typically we should prepend this before first IDR
                LOGI("Got codec config (SPS/PPS) size=%d", info.size);
            }
        }
        AMediaCodec_releaseOutputBuffer(codec_, outIndex, false);
        return out.size() > 0;
    } else if (outIndex == AMEDIACODEC_INFO_OUTPUT_FORMAT_CHANGED) {
        AMediaFormat* fmt = AMediaCodec_getOutputFormat(codec_);
        LOGI("Output format changed: %s", AMediaFormat_toString(fmt));
        int32_t color = 0;
        if (AMediaFormat_getInt32(fmt, AMEDIA_KEY_COLOR_FORMAT, &color)) {
            LOGI("Encoder accepted color format: 0x%x", color);
        }
        AMediaFormat_delete(fmt);
        return false;
    } else if (outIndex == AMEDIACODEC_INFO_TRY_AGAIN_LATER) {
        return false;
    } else {
        return false;
    }
}

void Encoder::signalEndOfStream() {
    if (!ensureStarted() || eosSent_) return;
    // queue empty buffer with EOS flag
    ssize_t inIndex = AMediaCodec_dequeueInputBuffer(codec_, 10000);
    if (inIndex >= 0) {
        AMediaCodec_queueInputBuffer(codec_, inIndex, 0, 0, 0, AMEDIACODEC_BUFFER_FLAG_END_OF_STREAM);
        eosSent_ = true;
    }
}

// ----------------- JNI wrapper (example) -----------------

// Create / hold a native pointer across JNI calls
static Encoder* g_encoder = nullptr;

extern "C" JNIEXPORT jboolean JNICALL
Java_com_yourapp_EncoderWrapper_nativeInit(JNIEnv* env, jobject thiz,
                                          jint width, jint height, jint fps, jint bitrate) {
    try {
        if (g_encoder) { delete g_encoder; g_encoder = nullptr; }
        g_encoder = new Encoder(width, height, fps, bitrate);
        return JNI_TRUE;
    } catch (const std::exception& e) {
        LOGE("nativeInit exception: %s", e.what());
        return JNI_FALSE;
    }
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_yourapp_EncoderWrapper_nativeEncodeFrame(JNIEnv* env, jobject thiz,
                                                  jbyteArray yuvArray, jlong ptsUs, jboolean isI420) {
    if (!g_encoder) return JNI_FALSE;
    jbyte* data = env->GetByteArrayElements(yuvArray, nullptr);
    jsize len = env->GetArrayLength(yuvArray);
    bool ok = g_encoder->encodeFrame(reinterpret_cast<uint8_t*>(data), (size_t)len, (int64_t)ptsUs, isI420 == JNI_TRUE);
    env->ReleaseByteArrayElements(yuvArray, data, JNI_ABORT);
    return ok ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jobject JNICALL
Java_com_yourapp_EncoderWrapper_nativeDequeue(JNIEnv* env, jobject thiz) {
    // Return a java byte[] or null
    if (!g_encoder) return nullptr;
    std::vector<uint8_t> out;
    int64_t pts = 0;
    bool isKey = false;
    bool have = g_encoder->dequeueEncoded(out, pts, isKey);
    if (!have) return nullptr;

    jbyteArray arr = env->NewByteArray((jsize)out.size());
    env->SetByteArrayRegion(arr, 0, (jsize)out.size(), reinterpret_cast<const jbyte*>(out.data()));
    // You can also return an object with (byte[], pts, isKey). For brevity, return byte[] only.
    return arr;
}

extern "C" JNIEXPORT void JNICALL
Java_com_yourapp_EncoderWrapper_nativeSignalEos(JNIEnv* env, jobject thiz) {
    if (g_encoder) g_encoder->signalEndOfStream();
}

extern "C" JNIEXPORT void JNICALL
Java_com_yourapp_EncoderWrapper_nativeRelease(JNIEnv* env, jobject thiz) {
    if (g_encoder) {
        delete g_encoder;
        g_encoder = nullptr;
    }
}

} // namespace learn01

 */
#endif //OKHTTPLEARN_LEARN001_HPP
