#include "openslLearn.hpp"

static const char* const TAG = "openslLearn";

void workThread(){
    // 标记一个消息的 ID
    constexpr int LOOPER_ID_MESSAGE = 1;
    const auto threadId = std::this_thread::get_id();
    logger::info(TAG, std::format("threadId: {}", threadId).c_str());
    ALooper *looper = ALooper_forThread();
    if (looper == nullptr){
        logger::info(TAG, "looper == nullptr");
        looper = ALooper_prepare(ALOOPER_PREPARE_ALLOW_NON_CALLBACKS);
    }
    int pipFd[2]; // 0 read 1 write
    if (pipe(pipFd) != 0){
        logger::error(TAG, "Failed to create pipe!");
        return;
    }
    ALooper_addFd(
            looper,
            pipFd[0],
            LOOPER_ID_MESSAGE,
            ALOOPER_EVENT_INPUT,
            [](int fd, int event, void *data) -> int {
                // ALOOPER_EVENT_INPUT
                char buf[4096];
                const auto result = read(fd, buf, sizeof(buf));
                logger::info(TAG, "buf: %s, result: %d", buf, result);
                const char* msg = static_cast<const char*>(data);
                logger::info(TAG, "fd: %d, event: %d, message: %d", msg, fd, event);
                return 1; // 返回1表示继续监听
            },
            (void *) "Hello World!"
    );
    logger::info(TAG, "Worker thread looper started.");

    // 写入管道，触发事件
    const char* hello = "trigger";
    write(pipFd[1], hello, strlen(hello));

    // 循环等待事件（最多阻塞 2s）
    for (int i = 0; i < 3; i++) {
        logger::info(TAG, "i: %d", i);
        int res = ALooper_pollOnce(2000 /*ms*/, nullptr, nullptr, nullptr);
        if (res == ALOOPER_POLL_TIMEOUT) {
            logger::info(TAG, "Looper timeout...");
        }
    }

    // 清理
    ALooper_removeFd(looper, pipFd[0]);
    close(pipFd[0]);
    close(pipFd[1]);

    logger::info(TAG, "Worker thread looper finished.");
}

struct PlaybackContext {
    int minBufferSize;
    int fd;
    SLObjectItf playerObject;
    SLObjectItf engineObject;
    SLObjectItf outputMixObject;
    SLPlayItf playerPlay;
    char *buffer;
};

// 桥接函数，OpenSL 会调用它
void bufferCallback(SLAndroidSimpleBufferQueueItf queue, void* context) {
    auto *ctx = static_cast<PlaybackContext*>(context);
    const auto size = read(ctx->fd, ctx->buffer, ctx->minBufferSize);
    SLmillisecond duration;
    const auto res = (*ctx->playerPlay)->GetDuration(ctx->playerPlay, &duration);
    if (res != SL_RESULT_SUCCESS){
        logger::error(TAG, "获取播放时长失败: %d", res);
    }
    logger::info(TAG, "bufferCallback size: %d, bufferSize: %d， duration: %d", size, sizeof(ctx->buffer), duration);
    if (size > 0) {
        SLresult result = (*queue)->Enqueue(queue, ctx->buffer, size);
        if (result != SL_RESULT_SUCCESS){
            logger::error(TAG, "Enqueue failed: %d", result);
        }
    } else {
        logger::info(TAG, "播放结束...");
        close(ctx->fd);
        (*ctx->playerObject)->Destroy(ctx->playerObject);
        (*ctx->outputMixObject)->Destroy(ctx->outputMixObject);
        (*ctx->engineObject)->Destroy(ctx->engineObject);
        delete ctx->buffer;
        delete ctx;  // 一次释放所有资源
    }
}

/**
 * ffmpeg -i wav.wav -f s16le  -ac 2 -ar 48000 pcm.pcm
 * ffplay -f s16le -ch_layout stereo  -ar 48000 -i  pcm.pcm
 * 48000 Hz, 2 channels, s16
 * @param fd
 */
void openSLESPlayPCM(const int fd){

    logger::info(TAG, "openSLESPlayPCM fd: %d", fd);

    SLObjectItf engineObject = nullptr;
    SLObjectItf outputMixObject = nullptr;
    SLObjectItf playerObject = nullptr;

    SLEngineItf engineEngine = nullptr;
    SLAndroidConfigurationItf playerConfig = nullptr;
    SLPlayItf playerPlay = nullptr;
    SLAndroidSimpleBufferQueueItf bufferQueue = nullptr;

    PlaybackContext *playbackContext = nullptr;

    // 输出
    SLDataSink dataSink;
    // 输入
    SLDataSource  dataSource;

    constexpr int INTERFACE_COUNT = 2;

    const SLInterfaceID interfaces[INTERFACE_COUNT] = {
            SL_IID_ANDROIDSIMPLEBUFFERQUEUE,
            SL_IID_ANDROIDCONFIGURATION,
    };

    const SLboolean required[INTERFACE_COUNT] = {
            SL_BOOLEAN_TRUE,
            SL_BOOLEAN_TRUE,
    };

    SLDataFormat_PCM formatPcm = {
            SL_DATAFORMAT_PCM,
            2,
            SL_SAMPLINGRATE_48,
            SL_PCMSAMPLEFORMAT_FIXED_16,
            SL_PCMSAMPLEFORMAT_FIXED_16,
            SL_SPEAKER_FRONT_LEFT | SL_SPEAKER_FRONT_RIGHT,
            SL_BYTEORDER_LITTLEENDIAN
    };

    SLDataLocator_OutputMix locatorOutputMix;
    SLDataLocator_AndroidSimpleBufferQueue locatorAndroidBufferQueue = {
            .locatorType = SL_DATALOCATOR_ANDROIDSIMPLEBUFFERQUEUE,
            .numBuffers = 2,
    };

    SLuint32 performanceMode = SL_ANDROID_PERFORMANCE_LATENCY;

    SLmillisecond duration = SL_TIME_UNKNOWN;

    constexpr int sampleRate = 48000;
    constexpr int channelCount = 2;
    constexpr int bitsPerSample = 16;

    // 20ms buffer
    constexpr int frameCount = sampleRate * 0.02;   // 960
    constexpr int frameSize = channelCount * (bitsPerSample / 8); // 4 bytes
    constexpr int minBufferSize = frameCount * frameSize;         // 3840 bytes

    // 创建引擎对象
    auto result = slCreateEngine(&engineObject, 0, nullptr, 0, nullptr, nullptr);
    if (result != SL_RESULT_SUCCESS){
        logger::error(TAG, "引擎创建失败");
        return;
    }

    // 初始化引擎对象
    result = (*engineObject)->Realize(engineObject, SL_BOOLEAN_FALSE);

    if (result != SL_RESULT_SUCCESS){
        logger::error(TAG, "引擎初始化失败");
        goto error;
    }

    // 获取引擎对象接口
    result = (*engineObject)->GetInterface(engineObject, SL_IID_ENGINE, &engineEngine);

    if (result != SL_RESULT_SUCCESS){
        logger::error(TAG, "获取创建失败");
        goto error;
    }

    // 创建混音器
    result = (*engineEngine)->CreateOutputMix(engineEngine, &outputMixObject, 0, nullptr, nullptr);
    if (result != SL_RESULT_SUCCESS){
        logger::error(TAG, "创建混音器失败");
        goto error;
    }

    // 初始化混音器
    result = (*outputMixObject)->Realize(outputMixObject, SL_BOOLEAN_FALSE);
    if (result != SL_RESULT_SUCCESS){
        logger::error(TAG, "初始化混音器失败");
        goto error;
    }

    // 输入
    dataSource.pLocator = &locatorAndroidBufferQueue;
    dataSource.pFormat = &formatPcm;

    // 固定写法
    locatorOutputMix.locatorType = SL_DATALOCATOR_OUTPUTMIX;
    locatorOutputMix.outputMix = outputMixObject;

    // 输出
    dataSink.pFormat = nullptr;
    dataSink.pLocator = &locatorOutputMix;

    // 创建播放器
    result = (*engineEngine)->CreateAudioPlayer(engineEngine, &playerObject, &dataSource, &dataSink, INTERFACE_COUNT, interfaces, required);
    if (result != SL_RESULT_SUCCESS){
        logger::error(TAG, "创建音频播放器失败");
        goto error;
    }

    // 获取配置
    result = (*playerObject)->GetInterface(playerObject, SL_IID_ANDROIDCONFIGURATION, &playerConfig);
    if (result != SL_RESULT_SUCCESS){
        logger::error(TAG, "获取播放配置失败!");
        goto error;
    }

    // 设置性能模式
    result = (*playerConfig)->SetConfiguration(playerConfig, SL_ANDROID_KEY_PERFORMANCE_MODE, &performanceMode, sizeof(SLuint32));
    if (result != SL_RESULT_SUCCESS){
        logger::error(TAG, "设置播放配置失败");
        goto error;
    }

    // 初始化播放器
    result = (*playerObject)->Realize(playerObject, SL_BOOLEAN_FALSE);

    if (result != SL_RESULT_SUCCESS){
        logger::error(TAG, "初始化播放器失败");
        goto error;
    }

    // 获取播放器接口
    result = (*playerObject)->GetInterface(playerObject, SL_IID_PLAY, &playerPlay);

    if (result != SL_RESULT_SUCCESS){
        logger::error(TAG, "获取播放器失败");
        goto error;
    }

    // 获取回调接口对象
    result = (*playerObject)->GetInterface(playerObject, SL_IID_ANDROIDSIMPLEBUFFERQUEUE, &bufferQueue);
    if (result != SL_RESULT_SUCCESS){
        logger::error(TAG, "获取回调接口对象失败");
        goto error;
    }

    playbackContext = new PlaybackContext{
        .minBufferSize = minBufferSize,
        .fd = dup(fd),
        .playerObject = playerObject,
        .engineObject = engineObject,
        .outputMixObject = outputMixObject,
        .playerPlay = playerPlay,
        .buffer = new char[minBufferSize],
    };

    // 注册回调
    result = (*bufferQueue)->RegisterCallback(bufferQueue, bufferCallback, playbackContext);

    if (result != SL_RESULT_SUCCESS){
        logger::error(TAG, "回调注册失败");
        goto error;
    }

    // 开始播放
    result = (*playerPlay)->SetPlayState(playerPlay, SL_PLAYSTATE_PLAYING);
    if (result != SL_RESULT_SUCCESS){
        logger::error(TAG, "播放失败");
        goto error;
    }
    // 必须先喂下第一帧数据
    bufferCallback(bufferQueue, playbackContext);
    result = (*playerPlay)->GetDuration(playerPlay, &duration);
    if (result != SL_RESULT_SUCCESS){
        logger::error(TAG, "获取时长失败");
    }
    logger::info(TAG, "初始化完成, 播放时长: %d", duration);
    // 初始化完成
    return;
error:
    logger::info(TAG, "销毁对象");
    if (playbackContext != nullptr){
        delete playbackContext->buffer;
        delete playbackContext;
    }
    if (playerObject != nullptr){
        (*playerObject)->Destroy(playerObject);
        playerObject = nullptr;
    }
    if (outputMixObject != nullptr){
        (*outputMixObject)->Destroy(outputMixObject);
        outputMixObject = nullptr;
    }
    if (engineObject != nullptr){
        (*engineObject)->Destroy(engineObject);
        engineObject = nullptr;
    }
}

void openSLESPlay(const int fd){

    logger::info(TAG, "fd: %d", fd);

    // 暂时使用GOTO替代RAII
    SLObjectItf engineObject = nullptr;
    SLEngineItf engineEngine = nullptr;
    SLObjectItf outputMixObject = nullptr;
    SLObjectItf playerObject = nullptr;
    SLAndroidConfigurationItf playerConfig = nullptr;
    SLPlayItf playerPlay = nullptr;
    SLPrefetchStatusItf prefetchStatusItf = nullptr;

    SLDataLocator_OutputMix locatorOutputMix;
    SLDataSink dataSink;
    SLDataLocator_AndroidFD locatorAndroidFd;
    SLDataFormat_MIME mime;
    SLDataSource  dataSource;

    SLuint32  performanceMode = SL_ANDROID_PERFORMANCE_LATENCY;


    constexpr int INTERFACE_COUNT = 3;

    const SLInterfaceID interfaces[INTERFACE_COUNT] = {
            SL_IID_PREFETCHSTATUS,
            SL_IID_EQUALIZER,
            SL_IID_ANDROIDCONFIGURATION,
    };

    const SLboolean required[INTERFACE_COUNT] = {
            SL_BOOLEAN_TRUE,
            SL_BOOLEAN_TRUE,
            SL_BOOLEAN_TRUE,
    };

    SLmillisecond duration = SL_TIME_UNKNOWN;
    SLuint32 prefetchStatus = SL_PREFETCHSTATUS_UNDERFLOW;

    int sleepStatus = 0;

    auto result = slCreateEngine(&engineObject, 0, nullptr, 0, nullptr, nullptr);
    if (result != SL_RESULT_SUCCESS){
        logger::error(TAG, "引擎创建失败");
        return;
    }

    // 初始化引擎
    result = (*engineObject)->Realize(engineObject, SL_BOOLEAN_FALSE);
    if (result != SL_RESULT_SUCCESS){
        logger::error(TAG, "引擎初始化失败");
        goto error;
    }

    result = (*engineObject)->GetInterface(engineObject, SL_IID_ENGINE, &engineEngine);
    if (result != SL_RESULT_SUCCESS){
        logger::error(TAG, "获取引擎失败");
        goto error;
    }

    result = (*engineEngine)->CreateOutputMix(engineEngine, &outputMixObject, 0, nullptr, nullptr);
    if (result != SL_RESULT_SUCCESS){
        logger::error(TAG, "创建混音器失败");
        goto error;
    }

    result = (*outputMixObject)->Realize(outputMixObject, SL_BOOLEAN_FALSE);
    if (result != SL_RESULT_SUCCESS){
        logger::error(TAG, "初始化混音器失败");
        goto error;
    }

    locatorOutputMix.locatorType = SL_DATALOCATOR_OUTPUTMIX;
    locatorOutputMix.outputMix = outputMixObject;

    dataSink.pLocator = &locatorOutputMix;
    dataSink.pFormat = nullptr;

    locatorAndroidFd.fd = fd;
    locatorAndroidFd.locatorType = SL_DATALOCATOR_ANDROIDFD;
    locatorAndroidFd.length = SL_DATALOCATOR_ANDROIDFD_USE_FILE_SIZE;
    locatorAndroidFd.offset = 0LL;

    mime.formatType = SL_DATAFORMAT_MIME;
    mime.mimeType = nullptr;
    mime.containerType = SL_CONTAINERTYPE_UNSPECIFIED;

    dataSource.pFormat = &mime;
    dataSource.pLocator = &locatorAndroidFd;

    result = (*engineEngine)->CreateAudioPlayer(engineEngine, &playerObject, &dataSource, &dataSink, INTERFACE_COUNT, interfaces, required);
    if (result != SL_RESULT_SUCCESS){
        logger::error(TAG, "创建音频播放器失败");
        goto error;
    }

    result = (*playerObject)->GetInterface(playerObject, SL_IID_ANDROIDCONFIGURATION, &playerConfig);
    if (result != SL_RESULT_SUCCESS){
        logger::error(TAG, "获取播放配置失败!");
        goto error;
    }

    result = (*playerConfig)->SetConfiguration(playerConfig, SL_ANDROID_KEY_PERFORMANCE_MODE, &performanceMode,sizeof(SLuint32));
    if (result != SL_RESULT_SUCCESS){
        logger::error(TAG, "设置播放配置失败");
        goto error;
    }

    result = (*playerObject)->Realize(playerObject, SL_BOOLEAN_FALSE);

    if (result != SL_RESULT_SUCCESS){
        logger::error(TAG, "初始化播放器失败");
        goto error;
    }

    result = (*playerObject)->GetInterface(playerObject, SL_IID_PLAY, &playerPlay);

    if (result != SL_RESULT_SUCCESS){
        logger::error(TAG, "获取播放器失败");
        goto error;
    }

    result = (*playerObject)->GetInterface(playerObject, SL_IID_PREFETCHSTATUS, &prefetchStatusItf);

    if (result != SL_RESULT_SUCCESS){
        logger::error(TAG, "获取preFetch失败");
        goto error;
    }

    result = (*playerPlay)->SetPlayState(playerPlay, SL_PLAYSTATE_PAUSED);
    if (result != SL_RESULT_SUCCESS){
        logger::error(TAG, "播放暂停失败");
        goto error;
    }

    while(prefetchStatus != SL_PREFETCHSTATUS_SUFFICIENTDATA){
        sleepStatus = usleep(100 * 1000U);
        result = (*prefetchStatusItf)->GetPrefetchStatus(prefetchStatusItf, &prefetchStatus);
        logger::info(TAG, "sleepStatus: %d, result: %d", sleepStatus, result);
        if (result != SL_RESULT_SUCCESS){
            logger::error(TAG, "获取播放音频数据失败");
            goto error;
        }
    }

    result = (*playerPlay)->GetDuration(playerPlay, &duration);

    if (result != SL_RESULT_SUCCESS){
        logger::error(TAG, "获取播放音频时长数据失败");
        goto error;
    }

    logger::info(TAG, "mp3音频时长为: %d", duration);
    if (duration == SL_TIME_UNKNOWN){
        duration = 3 * 1000; // 3 seconds
    }

    (*playerPlay)->SetPlayState(playerPlay, SL_PLAYSTATE_PLAYING);

    // 1. 阻塞线程
    sleepStatus = usleep(duration * 1000); // 在ui线程会直接卡死UI

    logger::info(TAG, "sleepStatus: %d", sleepStatus);

    (*playerPlay)->SetPlayState(playerPlay, SL_PLAYSTATE_STOPPED);
    logger::info(TAG, "播放完成!");
    goto error;

error:
    if (playerObject != nullptr){ // 顺序不对会奔溃
        (*playerObject)->Destroy(playerObject);
        playerObject = nullptr;
    }
    if (outputMixObject != nullptr){
        (*outputMixObject)->Destroy(outputMixObject);
        outputMixObject = nullptr;
    }
    if (engineObject != nullptr){
        (*engineObject)->Destroy(engineObject);
        engineObject = nullptr;
    }
}

OpenSLESPlayer::OpenSLESPlayer(const int fd) : fd(fd) {
    if (fd < 0) {
        throw std::runtime_error("Invalid file descriptor");
    }
    // 创建引擎对象
    auto result = slCreateEngine(&engineObject, 0, nullptr, 0, nullptr, nullptr);
    if (result != SL_RESULT_SUCCESS){
        logger::error(TAG, "引擎创建失败");
        throw std::runtime_error("Failed to create SL engine");
    }
    // 初始化引擎
    result = (*engineObject)->Realize(engineObject, SL_BOOLEAN_FALSE);
    if (result != SL_RESULT_SUCCESS){
        logger::error(TAG, "引擎初始化失败");
        throw std::runtime_error("Failed to init SL engine");
    }
    // 获取 ENGINE 接口对象
    SLEngineItf engineEngine;
    result = (*engineObject)->GetInterface(engineObject, SL_IID_ENGINE, &engineEngine);
    if (result != SL_RESULT_SUCCESS){
        logger::error(TAG, "获取引擎失败");
        throw std::runtime_error("Failed to get SL engine");
    }
    // 创建混音器
    result = (*engineEngine)->CreateOutputMix(engineEngine, &outputMixObject, 0, nullptr, nullptr);
    if (result != SL_RESULT_SUCCESS){
        logger::error(TAG, "创建混音器失败");
        throw std::runtime_error("Failed to create outputMixObject");
    }
    // 初始化混音器
    result = (*outputMixObject)->Realize(outputMixObject, SL_BOOLEAN_FALSE);
    if (result != SL_RESULT_SUCCESS){
        logger::error(TAG, "初始化混音器失败");
        throw std::runtime_error("Failed to init outputMixObject");
    }

    // 设置输出混音器定位器 固定写法
    SLDataLocator_OutputMix locatorOutputMix;
    locatorOutputMix.locatorType = SL_DATALOCATOR_OUTPUTMIX;
    locatorOutputMix.outputMix = outputMixObject;

    // 设置音频输出
    SLDataSink dataSink;
    dataSink.pLocator = &locatorOutputMix;
    dataSink.pFormat = nullptr;

    // 设置文件来源定位器
    SLDataLocator_AndroidFD locatorAndroidFd;
    // 文件资源描述符
    locatorAndroidFd.fd = fd;
    // 固定值，表示数据来源是文件
    locatorAndroidFd.locatorType = SL_DATALOCATOR_ANDROIDFD;
    // 自动计算文件大小
    locatorAndroidFd.length = SL_DATALOCATOR_ANDROIDFD_USE_FILE_SIZE;
    // 从文件开头读取
    locatorAndroidFd.offset = 0LL;


    SLDataFormat_MIME mime;
    // 固定值，表示使用 MIME 类型描述格式
    mime.formatType = SL_DATAFORMAT_MIME;
    // 自动推断
    mime.mimeType = nullptr;
    // 容器类型（未指定）
    mime.containerType = SL_CONTAINERTYPE_UNSPECIFIED;

    // 设置音频输入
    SLDataSource  dataSource;
    dataSource.pFormat = &mime;
    dataSource.pLocator = &locatorAndroidFd;

    // 请求四个扩展接口
    constexpr int INTERFACE_COUNT = 4;

    const SLInterfaceID interfaces[INTERFACE_COUNT] = {
            SL_IID_PREFETCHSTATUS, // 预加载状态接口
            SL_IID_EQUALIZER,      // 均衡器接口
            SL_IID_ANDROIDCONFIGURATION, // Android 专属配置接口
            SL_IID_SEEK, // Android 专属配置接口
    };

    // 是否必须
    const SLboolean required[INTERFACE_COUNT] = {
            SL_BOOLEAN_TRUE,
            SL_BOOLEAN_TRUE,
            SL_BOOLEAN_TRUE,
            SL_BOOLEAN_TRUE,
    };

    // 初始化播放器，并请求扩展接口
    result = (*engineEngine)->CreateAudioPlayer(engineEngine, &playerObject, &dataSource, &dataSink, INTERFACE_COUNT, interfaces, required);
    if (result != SL_RESULT_SUCCESS){
        logger::error(TAG, "创建音频播放器失败");
        throw std::runtime_error("Failed to create playerObject");
    }

    // Android 专属配置接口
    SLAndroidConfigurationItf playerConfig;
    result = (*playerObject)->GetInterface(playerObject, SL_IID_ANDROIDCONFIGURATION, &playerConfig);
    if (result != SL_RESULT_SUCCESS){
        logger::error(TAG, "获取播放配置失败!");
        throw std::runtime_error("Failed to get playerConfig");
    }

    // 设置低延迟性能模式
    SLuint32  performanceMode = SL_ANDROID_PERFORMANCE_LATENCY;
    result = (*playerConfig)->SetConfiguration(playerConfig, SL_ANDROID_KEY_PERFORMANCE_MODE, &performanceMode, sizeof(SLuint32));
    if (result != SL_RESULT_SUCCESS){
        logger::error(TAG, "设置播放配置失败");
        throw std::runtime_error("Failed to set playerConfig");
    }


    result = (*playerObject)->Realize(playerObject, SL_BOOLEAN_FALSE);

    if (result != SL_RESULT_SUCCESS){
        logger::error(TAG, "初始化播放器失败");
        throw std::runtime_error("Failed init set playerObject");
    }

    result = (*playerObject)->GetInterface(playerObject, SL_IID_PLAY, &playerPlay);
    if (result != SL_RESULT_SUCCESS){
        logger::error(TAG, "获取播放器失败");
        throw std::runtime_error("Failed init get playerPlay");
    }

    result = (*playerObject)->GetInterface(playerObject, SL_IID_SEEK, &playerSeek);

    if (result != SL_RESULT_SUCCESS){
        logger::error(TAG, "获取定位器失败");
    }

    logger::info(TAG, "初始化完成");
}


bool OpenSLESPlayer::pause() {
    const auto result = (*this->playerPlay)->SetPlayState(playerPlay, SL_PLAYSTATE_PAUSED);
    return result == SL_RESULT_SUCCESS;
}

/**
 * stop 会清楚缓存, 下次从头开始播放
 * @return 是否stop成功
 */
bool OpenSLESPlayer::stop() {
    const auto result = (*this->playerPlay)->SetPlayState(playerPlay, SL_PLAYSTATE_STOPPED);
    return result == SL_RESULT_SUCCESS;
}

bool OpenSLESPlayer::play() {
    SLuint32 currentState;
    auto result = (*this->playerPlay)->GetPlayState(playerPlay, &currentState);
    if(result != SL_RESULT_SUCCESS){
        return false;
    }
    logger::info(TAG, "当前播放状态: %d", currentState);
    if (currentState == SL_PLAYSTATE_PLAYING){
        return false;
    }
    result = (*this->playerPlay)->SetPlayState(playerPlay, SL_PLAYSTATE_PLAYING);
    return result == SL_RESULT_SUCCESS;
}

bool OpenSLESPlayer::seek(const uint32_t position) {
    if (playerSeek == nullptr){
        return false;
    }

    const auto duration = this->getDuration();
    if (position > duration){
        return false;
    }

    const auto result = (*playerSeek)->SetPosition(playerSeek, position, SL_SEEKMODE_ACCURATE);

    return result == SL_RESULT_SUCCESS;
}

uint32_t OpenSLESPlayer::getDuration() {

    SLmillisecond duration = SL_TIME_UNKNOWN;

    SLuint32 currentState;
    auto result = (*this->playerPlay)->GetPlayState(playerPlay, &currentState);
    if(result != SL_RESULT_SUCCESS){
        return duration;
    }

    logger::info(TAG, "当前播放状态: %d", currentState);

    if (currentState == SL_PLAYSTATE_STOPPED){
        (*this->playerPlay)->SetPlayState(playerPlay, SL_PLAYSTATE_PAUSED);
    }

    SLuint32 prefetchStatus = SL_PREFETCHSTATUS_UNDERFLOW;
    SLPrefetchStatusItf prefetchStatusItf;

    result = (*playerObject)->GetInterface(playerObject, SL_IID_PREFETCHSTATUS, &prefetchStatusItf);
    if (result != SL_RESULT_SUCCESS){
        logger::error(TAG, "获取prefetchStatusItf失败");
        return duration;
    }

    while(prefetchStatus != SL_PREFETCHSTATUS_SUFFICIENTDATA){
        result = (*prefetchStatusItf)->GetPrefetchStatus(prefetchStatusItf, &prefetchStatus);
        logger::info(TAG, "while result: %d", result);
        if (result != SL_RESULT_SUCCESS){
            logger::error(TAG, "获取播放音频数据失败");
            break;
        }
    }

    result = (*playerPlay)->GetDuration(playerPlay, &duration);
    if (result != SL_RESULT_SUCCESS){
        logger::error(TAG, "获取播放音频时长数据失败");
    }

    return duration;
}

OpenSLESPlayer::~OpenSLESPlayer() {
    if (this->playerObject != nullptr){
        (*playerObject)->Destroy(playerObject);
    }
    if (this->outputMixObject != nullptr){
        (*outputMixObject)->Destroy(outputMixObject);
    }
    if (this->engineObject != nullptr) {
        (*engineObject)->Destroy(engineObject);
    }
    close(fd);
    logger::info(TAG, "对象销毁完成");
}


void recordCallback(SLAndroidSimpleBufferQueueItf simpleBufferQueueItf, void* context){
    if (context == nullptr){
        return;
    }
    const auto recorderCallbackContext = reinterpret_cast<RecorderCallbackContext*>(context);
    SLuint32 state;
    auto result = (*recorderCallbackContext->recorderRecord)->GetRecordState(recorderCallbackContext->recorderRecord, &state);
    if (result != SL_RESULT_SUCCESS){
        logger::error(TAG, "获取状态失败");
        throw std::runtime_error("Failed to get state");
    }
    switch (state) {
        case SL_RECORDSTATE_RECORDING: {
            logger::info(TAG, "录音中...");
            result = (*simpleBufferQueueItf)->Enqueue(simpleBufferQueueItf, recorderCallbackContext->buffer->data(), recorderCallbackContext->buffer->size());
            if (result != SL_RESULT_SUCCESS){
                logger::error(TAG, "引擎创建失败");
                throw std::runtime_error("Failed to create SL engine");
            }
            recorderCallbackContext->pcmFile->write(recorderCallbackContext->buffer->data(), static_cast<std::streamsize>(recorderCallbackContext->buffer->size()));
            break;
        }
        case SL_RECORDSTATE_PAUSED: {
            logger::info(TAG, "暂停状态...");
            break;
        }
        case SL_RECORDSTATE_STOPPED: {
            logger::info(TAG, "停止状态...");
            break;
        }
        default: {
            logger::info(TAG, "未知状态...");
        }
    }
}

OpenSLESRecorder::OpenSLESRecorder() {
    auto result = slCreateEngine(&engineObject, 0, nullptr, 0, nullptr, nullptr);
    if (result != SL_RESULT_SUCCESS){
        logger::error(TAG, "引擎创建失败");
        throw std::runtime_error("Failed to create SL engine");
    }
    result = (*engineObject)->Realize(engineObject, SL_BOOLEAN_FALSE);
    if (result != SL_RESULT_SUCCESS){
        logger::error(TAG, "引擎初始化失败");
        throw std::runtime_error("Failed to init SL engine");
    }
    SLEngineItf engineEngine;
    result = (*engineObject)->GetInterface(engineObject, SL_IID_ENGINE, &engineEngine);
    if (result != SL_RESULT_SUCCESS){
        logger::error(TAG, "获取engineEngine失败");
        throw std::runtime_error("Failed to get engineEngine");
    }
    // 输入数据
    SLDataLocator_IODevice ioDevice = {
            .locatorType = SL_DATALOCATOR_IODEVICE,
            .deviceType = SL_IODEVICE_AUDIOINPUT,
            .deviceID = SL_DEFAULTDEVICEID_AUDIOINPUT,
            .device = nullptr
    };
    SLDataSource dataSource = {
            .pLocator = &ioDevice,
            .pFormat = nullptr
    };

    SLDataLocator_AndroidSimpleBufferQueue simpleBufferQueue = {
            .locatorType = SL_DATALOCATOR_ANDROIDSIMPLEBUFFERQUEUE,
            .numBuffers = 2,
    };

    SLDataFormat_PCM formatPcm = {
            .formatType= SL_DATAFORMAT_PCM,
            .numChannels = 2,
            .samplesPerSec = SL_SAMPLINGRATE_48,
            .bitsPerSample = SL_PCMSAMPLEFORMAT_FIXED_16,
            .containerSize = SL_PCMSAMPLEFORMAT_FIXED_16,
            .channelMask = SL_SPEAKER_FRONT_LEFT | SL_SPEAKER_FRONT_RIGHT,
            .endianness = SL_BYTEORDER_LITTLEENDIAN
    };

    // 输出数据
    SLDataSink dataSink = {
            .pLocator = &simpleBufferQueue,
            .pFormat = &formatPcm,
    };
    const SLuint32 numInterfaces = 1;
    const SLInterfaceID  id[numInterfaces] = { SL_IID_ANDROIDSIMPLEBUFFERQUEUE };
    const SLboolean require[numInterfaces] = { SL_BOOLEAN_TRUE };
    result = (*engineEngine)->CreateAudioRecorder(engineEngine, &recorderObject, &dataSource, &dataSink, numInterfaces, id, require);
    if (result != SL_RESULT_SUCCESS){
        logger::error(TAG, "获取recorderObject失败");
        throw std::runtime_error("Failed to get recorderObject");
    }
    result = (*recorderObject)->Realize(recorderObject, SL_BOOLEAN_FALSE);
    if (result != SL_RESULT_SUCCESS){
        logger::error(TAG, "recorder Realize 失败");
        throw std::runtime_error("Failed to Realize recorderObject");
    }

    result = (*recorderObject)->GetInterface(recorderObject, SL_IID_RECORD, &recorderRecord);
    if (result != SL_RESULT_SUCCESS){
        logger::error(TAG, "获取 recorderRecord 失败");
        throw std::runtime_error("Failed to get recorderRecord");
    }
    SLAndroidSimpleBufferQueueItf simpleBufferQueueItf;
    result = (*recorderObject)->GetInterface(recorderObject, SL_IID_ANDROIDSIMPLEBUFFERQUEUE, &simpleBufferQueueItf);
    if (result != SL_RESULT_SUCCESS){
        logger::error(TAG, "获取 simpleBufferQueue 失败");
        throw std::runtime_error("Failed to get simpleBufferQueue");
    }

    auto *outPcmFile = new std::ofstream("/data/data/io.github.opensllearn/files/pcm.pcm", std::ios::binary);

    if (!outPcmFile->is_open()) {
        logger::error(TAG, "文件打开错误");
        throw std::runtime_error("Failed to open file");
    }

    context = new RecorderCallbackContext{
            outPcmFile,
        new std::vector<char>(4096),
                recorderRecord,
    };

    result = (*simpleBufferQueueItf)->RegisterCallback(simpleBufferQueueItf, recordCallback, context);
    if (result != SL_RESULT_SUCCESS){
        logger::error(TAG, "注册回调失败");
        throw std::runtime_error("Failed to RegisterCallback");
    }
    result = (*recorderRecord)->SetRecordState(recorderRecord, SL_RECORDSTATE_RECORDING);
    if (result != SL_RESULT_SUCCESS){
        logger::error(TAG, "开始录音频失败");
        throw std::runtime_error("Failed to record audio");
    }
    recordCallback(simpleBufferQueueItf, context);
}

OpenSLESRecorder::~OpenSLESRecorder() {
    logger::info(TAG, "停止+回收资源...");
    if (recorderRecord != nullptr){
        (*recorderRecord)->SetRecordState(recorderRecord, SL_RECORDSTATE_STOPPED);
    }
    if (context != nullptr){
        delete context->buffer;
        delete context->pcmFile;
        delete context;
    }
    if (recorderObject != nullptr) {
        (*recorderObject)->Destroy(recorderObject);
    }
    if (engineObject != nullptr){
        (*engineObject)->Destroy(engineObject);
    }
}

extern "C"
JNIEXPORT jlong JNICALL
Java_io_github_opensllearn_utils_Utils_getRecorder(JNIEnv* env, jobject) {
    OpenSLESRecorder *recorder = nullptr;
    try{
        recorder = new OpenSLESRecorder();
    } catch (const std::exception &e) {
        delete recorder;
        recorder = nullptr;
        env->ThrowNew(env->FindClass("java/lang/RuntimeException"), e.what());
    }
    return reinterpret_cast<jlong>(recorder);
}

extern "C"
JNIEXPORT void JNICALL
Java_io_github_opensllearn_utils_Utils_releaseRecorder(JNIEnv* env, jobject, jlong ptr) {
    auto *recorder = reinterpret_cast<OpenSLESRecorder*>(ptr);
    delete recorder;
}

extern "C"
JNIEXPORT jlong JNICALL
Java_io_github_opensllearn_utils_Utils_getPlayer(JNIEnv* env, jobject, jint fd) {
    OpenSLESPlayer *player = nullptr;
    try{
        player = new OpenSLESPlayer(fd);
    } catch (const std::exception &e) {
        delete player;
        player = nullptr;
        env->ThrowNew(env->FindClass("java/lang/RuntimeException"), e.what());
    }
    return reinterpret_cast<jlong>(player);
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_io_github_opensllearn_utils_Utils_play(JNIEnv*, jobject, jlong ptr) {
    auto* player = reinterpret_cast<OpenSLESPlayer*>(ptr);
    return player->play();
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_io_github_opensllearn_utils_Utils_pause(JNIEnv*, jobject, jlong ptr) {
    auto* player = reinterpret_cast<OpenSLESPlayer*>(ptr);
    return player->pause();
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_io_github_opensllearn_utils_Utils_stop(JNIEnv*, jobject, jlong ptr) {
    auto* player = reinterpret_cast<OpenSLESPlayer*>(ptr);
    return player->stop();
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_io_github_opensllearn_utils_Utils_seek(JNIEnv*, jobject, jlong ptr, jint position) {
    auto* player = reinterpret_cast<OpenSLESPlayer*>(ptr);
    return player->seek(position);
}

extern "C"
JNIEXPORT jint JNICALL
Java_io_github_opensllearn_utils_Utils_getDuration(JNIEnv*, jobject, jlong ptr) {
    auto* player = reinterpret_cast<OpenSLESPlayer*>(ptr);
    return static_cast<jint>(player->getDuration());
}

extern "C"
JNIEXPORT void JNICALL
Java_io_github_opensllearn_utils_Utils_releasePlayer(JNIEnv*, jobject, jlong ptr) {
    auto* player = reinterpret_cast<OpenSLESPlayer*>(ptr);
    delete player;
}

extern "C"
JNIEXPORT void JNICALL
Java_io_github_opensllearn_utils_Utils_hello(JNIEnv*, jobject, jint fd) {
    openSLESPlayPCM(fd);
}