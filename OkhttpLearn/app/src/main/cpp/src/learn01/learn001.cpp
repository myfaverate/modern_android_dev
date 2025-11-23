//
// Created by 29051 on 2025/11/22.
//
#include <dlfcn.h>
#include "learn001.hpp"

static const char * const TAG = "learn001";

namespace learn01 {
    void hello001(){
        logger::info(TAG, "learn01 learn001...");
    }
    /**
     * ffmpeg -i video.mp4 -codec copy -an output.h264
     * ffmpeg -i video.mp4 -codec copy -vn -sn output.aac
     * ffmpeg -i output.h264 -pix_fmt yuv420p output.yuv
     * ffplay -f rawvideo -pixel_format yuv420p -video_size 720x1280 output.yuv
     * adb push output.yuv /data/data/io.github.okhttplearn/files
     * https://zhuanlan.zhihu.com/p/71928833
     * https://www.yuque.com/keith-an9fr/aab7xp/cy9suo
     * https://www.cnblogs.com/linuxAndMcu/p/14533228.html
     * https://ffmpeg.xianwaizhiyin.net/demuxer/h264_format.html
     * https://blogs.hdvr.top/2021/09/22/%E9%9F%B3%E8%A7%86%E9%A2%91%E4%B9%8B-H-264%E6%A0%BC%E5%BC%8F/
     */
    uint64_t Encoder::getMicroseconds(){
        const std::chrono::time_point<std::chrono::system_clock> now = std::chrono::system_clock::now();
        const std::chrono::duration<long long int, std::ratio<1LL, 1000000LL>> duration = now.time_since_epoch();
        const std::chrono::duration<long long int, std::ratio<1LL, 1000000LL>> microseconds = std::chrono::duration_cast<std::chrono::microseconds>(duration);
        return microseconds.count();
    }
    void Encoder::load_media_codec_async_function(){
        this -> libHandle = dlopen("libmediandk.so", RTLD_NOW);
        if (libHandle) {
            pSetAsyncNotifyCallback = (AMediaCodec_setAsyncNotifyCallback_Type) dlsym(
                    libHandle,
                    "AMediaCodec_setAsyncNotifyCallback"
            );
            // 注意：不应该 dlclose，因为它可能被其他 NDK API 使用
        }
    }
    Encoder::Encoder() {
        load_media_codec_async_function();
        logger::info(TAG, "主线程 id: %d", std::this_thread::get_id());
        this -> yuvFile = new std::ifstream("/data/data/io.github.okhttplearn/files/output.yuv", std::ios::binary);
        this -> h264File = new std::ofstream("/data/data/io.github.okhttplearn/files/output.h264", std::ios::binary);
        if(!yuvFile->is_open() || !h264File->is_open()){
            throw std::runtime_error("文件打开 is null");
        }
        this->mediaFormat = AMediaFormat_new();
        if (mediaFormat == nullptr){
            throw std::runtime_error("mediaFormat is null");
        }
        AMediaFormat_setString(mediaFormat, AMEDIAFORMAT_KEY_MIME, "video/avc");
        AMediaFormat_setInt32(mediaFormat, AMEDIAFORMAT_KEY_WIDTH, 720);
        AMediaFormat_setInt32(mediaFormat, AMEDIAFORMAT_KEY_HEIGHT, 1280);
        AMediaFormat_setInt32(mediaFormat, AMEDIAFORMAT_KEY_FRAME_RATE, 30);
        AMediaFormat_setInt32(mediaFormat, AMEDIAFORMAT_KEY_BIT_RATE, 200'0000);
        AMediaFormat_setInt32(mediaFormat, AMEDIAFORMAT_KEY_COLOR_FORMAT, 19);
        AMediaFormat_setInt32(mediaFormat, AMEDIAFORMAT_KEY_I_FRAME_INTERVAL, 1);

        this->mediaCodec = AMediaCodec_createEncoderByType("video/avc");
        if (mediaCodec == nullptr){
            throw std::runtime_error("mediaCodec is null");
        }

        auto status = AMediaCodec_configure(mediaCodec, mediaFormat, nullptr, nullptr, AMEDIACODEC_CONFIGURE_FLAG_ENCODE);
        if (status != AMEDIA_OK){
            throw std::runtime_error("AMediaCodec_configure status != A_MEDIA_OK");
        }

        const auto callback =  AMediaCodecOnAsyncNotifyCallback{
            .onAsyncInputAvailable = [](AMediaCodec *codec, void *userdata, int32_t index) -> void {
                const auto encoder = reinterpret_cast<Encoder*>(userdata);
                // 如何实现回调转协程同步？
                size_t outSize = 0;
                uint8_t *buffer = AMediaCodec_getInputBuffer(codec, index, &outSize);
                const int frameSize = 720 * 1280 * 3 / 2;
                if (outSize == frameSize){
                    encoder->yuvFile->read(reinterpret_cast<char*>(buffer), frameSize);
                    const auto gCount = encoder->yuvFile->gcount();
                    logger::info(TAG, "onAsyncInputAvailable outSize: %d, frameSize: %d, gCount: %d, threadId: %d, timestamp: %lld", outSize, frameSize, gCount, std::this_thread::get_id(), encoder->getMicroseconds() / 1000);
                    const auto status = AMediaCodec_queueInputBuffer(codec, index, 0, frameSize, encoder->getMicroseconds() / 1000, 0);
                    if (status != AMEDIA_OK){
                        logger::error(TAG, "AMediaCodec_queueInputBuffer error1");
                    } else {
                        logger::info(TAG, "AMediaCodec_queueInputBuffer success");
                    }
                } else {
                    const auto status = AMediaCodec_queueInputBuffer(codec, index, 0, 0, encoder->getMicroseconds() / 1000, AMEDIACODEC_BUFFER_FLAG_END_OF_STREAM);
                    if (status != AMEDIA_OK){
                        logger::error(TAG, "AMediaCodec_queueInputBuffer error2");
                    }
                }
            },
            .onAsyncOutputAvailable = [](AMediaCodec *codec, void *userdata, int32_t index, AMediaCodecBufferInfo *bufferInfo) -> void {
                size_t outSize = 0;
                const auto buffer = AMediaCodec_getOutputBuffer(codec, index, &outSize);
                const auto encoder = reinterpret_cast<Encoder*>(userdata);
                encoder->h264File->write(reinterpret_cast<char*>(buffer), outSize);
                const auto status = AMediaCodec_releaseOutputBuffer(codec, index, false);
                logger::info(TAG, "index: %d, bufferInfo->size: %d, outSize: %d, threadId: %d", index, bufferInfo->size, outSize, std::this_thread::get_id());
                if (status != AMEDIA_OK){
                    logger::error(TAG, "onAsyncOutputAvailable AMediaCodec_queueInputBuffer error1");
                }
                if (bufferInfo->flags == AMEDIACODEC_BUFFER_FLAG_END_OF_STREAM){
                    logger::info(TAG, "onAsyncOutputAvailable end...");
                }
            },
            .onAsyncFormatChanged = [](AMediaCodec *codec, void *userdata, AMediaFormat *format) -> void {
                logger::info(TAG, "onAsyncFormatChanged...");
            },
            .onAsyncError =  [](AMediaCodec *codec, void *userdata, media_status_t error,int32_t actionCode, const char *detail) -> void {
                logger::error(TAG, "onAsyncError media_status_t: %d, actionCode: %d, detail: %s", error, actionCode, detail);
            },
        };
        // 注册回调

        logger::info(TAG, "android api: %d, runtime api: %d", __ANDROID_API__, android_get_device_api_level());
        if (android_get_device_api_level() >= __ANDROID_API_P__){
            pSetAsyncNotifyCallback(mediaCodec, callback, this);
        } else {
            throw std::runtime_error("__ANDROID_API__ < 28");
        }
// #if __ANDROID_API__ >= __ANDROID_API_P__
//         AMediaCodec_setAsyncNotifyCallback(mediaCodec, callback, this);
// #else
//         throw std::runtime_error("__ANDROID_API__ < 28");
// #endif

        status = AMediaCodec_start(mediaCodec);
        if (status != AMEDIA_OK){
            throw std::runtime_error("start error");
        } else {
            logger::info(TAG, "start success...");
        }
    }
    Encoder::~Encoder() {
        logger::info(TAG, "析构...");
        if (this->mediaCodec != nullptr){
            AMediaCodec_stop(mediaCodec);
            AMediaCodec_delete(mediaCodec);
            this->mediaCodec = nullptr;
        }
        if (mediaFormat != nullptr){
            AMediaFormat_delete(mediaFormat);
            this->mediaFormat = nullptr;
        }
        if(this->yuvFile != nullptr){
            this->yuvFile->close();
            this->yuvFile = nullptr;
        }
        if(this->h264File != nullptr){
            this->h264File->close();
            this->h264File = nullptr;
        }
        if (this->libHandle != nullptr){
            dlclose(this->libHandle);
            this->libHandle = nullptr;
        }
    }
}

extern "C"
JNIEXPORT jlong JNICALL
Java_io_github_okhttplearn_utils_Utils_nativeEncoder(JNIEnv *env, jobject thiz) {
    learn01::Encoder* encoder = nullptr;
    try {
        encoder = new learn01::Encoder();
    } catch (const std::exception &e){
        delete encoder;
        encoder = nullptr;
        env->ThrowNew(env->FindClass("java/lang/RuntimeException"), e.what());
    }
    return reinterpret_cast<jlong>(encoder);
}

extern "C"
JNIEXPORT void JNICALL
Java_io_github_okhttplearn_utils_Utils_nativeReleaseEncoder(JNIEnv *env, jobject thiz, jlong ptr) {
    const auto* const encoder = reinterpret_cast<learn01::Encoder*>(ptr);
    delete encoder;
}