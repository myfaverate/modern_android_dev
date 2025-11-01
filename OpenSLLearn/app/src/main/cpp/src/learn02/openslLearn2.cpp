#include "openslLearn2.hpp"
#include "NDKCamera.hpp"

const char* const TAG = "openslLearn2";

extern "C"
JNIEXPORT jstring JNICALL
Java_io_github_opensllearn_utils_Utils_hello1(JNIEnv *env, jobject thiz) {
    logger::info(TAG, "Java_io_github_opensllearn_utils_Utils_hello1...");
    sqlite3 *db;
    char *errMsg = nullptr;
    // 1. 打开数据库（放在 /data/data/包名/files/test.db）
    std::string dbPath = "/data/data/io.github.opensllearn/files/test.db";
    int rc = sqlite3_open(dbPath.c_str(), &db);
    if (rc != SQLITE_OK) {
        logger::error(TAG, "Can't open database: %s", sqlite3_errmsg(db));
        return env->NewStringUTF("Failed to open database");
    }
    logger::info(TAG, "Opened database successfully");

    // 2. 创建表
    const char *sqlCreate = "CREATE TABLE IF NOT EXISTS hello (id INTEGER PRIMARY KEY, msg TEXT);";
    rc = sqlite3_exec(db, sqlCreate, nullptr, nullptr, &errMsg);
    if (rc != SQLITE_OK) {
        logger::info(TAG, "SQL error: %s", errMsg);
        sqlite3_free(errMsg);
    } else {
        logger::info(TAG, "Table created successfully");
    }

    // 3. 插入一条数据
    const char *sqlInsert = "INSERT INTO hello (msg) VALUES ('Hello from C++!');";
    rc = sqlite3_exec(db, sqlInsert, nullptr, nullptr, &errMsg);
    if (rc != SQLITE_OK) {
        logger::info(TAG, "SQL error: %s", errMsg);
        sqlite3_free(errMsg);
    } else {
        logger::info(TAG, "Inserted successfully");
    }

    // 4. 查询数据
    const char *sqlSelect = "SELECT id, msg FROM hello;";
    sqlite3_stmt *stmt;
    rc = sqlite3_prepare_v2(db, sqlSelect, -1, &stmt, nullptr);
    if (rc == SQLITE_OK) {
        while (sqlite3_step(stmt) == SQLITE_ROW) {
            int id = sqlite3_column_int(stmt, 0);
            const unsigned char *msg = sqlite3_column_text(stmt, 1);
            logger::info(TAG, "Row: id=%d, msg=%s", id, msg);
        }
        sqlite3_finalize(stmt);
    }

    sqlite3_close(db);
    return env->NewStringUTF("SQLite Hello World Completed!");
}

aaudio_data_callback_result_t dataCallback(AAudioStream *stream, void *userData, void *audioData, int32_t numFrames){
    const int *fdPtr = reinterpret_cast<int*>(userData);
    const auto size = read(*fdPtr, audioData, numFrames * 2 * 2);
    if (size <= 0){
        logger::info(TAG, "播放结束, fd: %d", *fdPtr);
        AAudioStream_requestStop(stream);
        return AAUDIO_CALLBACK_RESULT_STOP;
    }
    return AAUDIO_CALLBACK_RESULT_CONTINUE;
}

AudioPlayer::AudioPlayer(int fd){
    this->fd = new int(dup(fd));
    const auto apiLevel = android_get_device_api_level();
    logger::info(TAG, "current apiLevel: %d， __ANDROID_API__: %d", apiLevel, __ANDROID_API__);
#if __ANDROID_API__ >= 26
    aaudio_result_t result = AAudio_createStreamBuilder(&builder);
    if (result != AAUDIO_OK){
        logger::info(TAG, "创建 builder 失败");
        throw std::runtime_error("Failed to create builder");
    }
    AAudioStreamBuilder_setDeviceId(builder, AAUDIO_UNSPECIFIED);
    aaudio_format_t mFormat = AAUDIO_FORMAT_PCM_I16;
    AAudioStreamBuilder_setFormat(builder, mFormat);
    AAudioStreamBuilder_setChannelCount(builder, 2);
    AAudioStreamBuilder_setSampleRate(builder, 44100);
    AAudioStreamBuilder_setSharingMode(builder, AAUDIO_SHARING_MODE_EXCLUSIVE); // 独占式
    AAudioStreamBuilder_setPerformanceMode(builder, AAUDIO_PERFORMANCE_MODE_LOW_LATENCY); // 高性能
    AAudioStreamBuilder_setDirection(builder, AAUDIO_DIRECTION_OUTPUT);
    AAudioStreamBuilder_setDataCallback(builder, dataCallback, this->fd);

    result = AAudioStreamBuilder_openStream(builder, &mAudioStream);
    if (result != AAUDIO_OK){
        logger::info(TAG, "打开 stream 失败");
        throw std::runtime_error("Failed to open stream");
    }

    // ==
    // 打印流信息
    const int32_t burst = AAudioStream_getFramesPerBurst(mAudioStream);
    const int32_t capacity = AAudioStream_getBufferCapacityInFrames(mAudioStream);
    logger::info(TAG, "framesPerBurst=%d, capacity=%d", burst, capacity);
    // ✅ 安全设置 buffer：使用 2×burst, 不超过 capacity
    const int32_t target = std::min(capacity, burst * 2);
    // ===

    result = AAudioStream_setBufferSizeInFrames(mAudioStream, target);
    if (result < 0){
        logger::error(TAG, "打开 setBufferSizeInFrames 失败, target: %d, code: %d, result: %s", target, result, AAudio_convertResultToText(result));
        throw std::runtime_error("Failed to setBufferSizeInFrames");
    }

    result = AAudioStream_requestStart(mAudioStream);

    if (result != AAUDIO_OK){
        logger::info(TAG, "开始播放 失败");
        throw std::runtime_error("Failed to play");
    }
#else
    throw std::runtime_error("不支持 audio");
#endif
}

AudioPlayer::~AudioPlayer() {
#if __ANDROID_API__ >= 26
    logger::info(TAG, "AudioPlayer 析构调用");
    if (mAudioStream != nullptr){
        AAudioStream_requestStop(mAudioStream);
        AAudioStream_close(mAudioStream);
    }
    if (builder != nullptr){
        AAudioStreamBuilder_delete(builder);
    }
    if (*fd > 0){
        close(*fd);
        delete fd;
    }
#else
    logger::error(TAG, "不支持 audio");
#endif
}
aaudio_data_callback_result_t recordCallback(AAudioStream *stream, void *userData, void *audioData, int32_t numFrames){
    const int *fdPtr = reinterpret_cast<int*>(userData);
    const auto size = write(*fdPtr, audioData, numFrames * 2 * 2);
    if (size <= 0){
        logger::info(TAG, "播放结束, fd: %d", *fdPtr);
        AAudioStream_requestStop(stream);
        return AAUDIO_CALLBACK_RESULT_STOP;
    }
    return AAUDIO_CALLBACK_RESULT_CONTINUE;
}
AudioRecord::AudioRecord(int fd) {
    this->fd = new int(dup(fd));
    aaudio_result_t result = AAudio_createStreamBuilder(&builder);
    if (result != AAUDIO_OK){
        logger::info(TAG, "创建 builder 失败");
        throw std::runtime_error("Failed to create builder");
    }
    AAudioStreamBuilder_setDeviceId(builder, AAUDIO_UNSPECIFIED);
    AAudioStreamBuilder_setFormat(builder, AAUDIO_FORMAT_PCM_I16);
    AAudioStreamBuilder_setChannelCount(builder, 2);
    AAudioStreamBuilder_setSampleRate(builder, 44100);
    AAudioStreamBuilder_setSharingMode(builder, AAUDIO_SHARING_MODE_SHARED);
    AAudioStreamBuilder_setPerformanceMode(builder, AAUDIO_PERFORMANCE_MODE_NONE);
    AAudioStreamBuilder_setDirection(builder, AAUDIO_DIRECTION_INPUT);
    AAudioStreamBuilder_setDataCallback(builder, recordCallback, this->fd);
    result = AAudioStreamBuilder_openStream(builder, &mAudioStream);
    if (result != AAUDIO_OK){
        logger::info(TAG, "打开 stream 失败");
        throw std::runtime_error("Failed to open stream");
    }

    // ==
    // 打印流信息
    const int32_t burst = AAudioStream_getFramesPerBurst(mAudioStream);
    const int32_t capacity = AAudioStream_getBufferCapacityInFrames(mAudioStream);
    logger::info(TAG, "framesPerBurst=%d, capacity=%d", burst, capacity);
    // ✅ 安全设置 buffer：使用 2×burst, 不超过 capacity
    const int32_t target = std::min(capacity, burst * 2);
    // ===

    result = AAudioStream_setBufferSizeInFrames(mAudioStream, target);
    if (result < 0){
        logger::error(TAG, "打开 setBufferSizeInFrames 失败, target: %d, code: %d, result: %s", target, result, AAudio_convertResultToText(result));
        throw std::runtime_error("Failed to setBufferSizeInFrames");
    }

    result = AAudioStream_requestStart(mAudioStream);

    if (result != AAUDIO_OK){
        logger::info(TAG, "开始录制 失败");
        throw std::runtime_error("Failed to play");
    }
}
AudioRecord::~AudioRecord() {
    logger::info(TAG, "AudioRecord 析构调用");
    if (mAudioStream != nullptr){
        AAudioStream_requestStop(mAudioStream);
        AAudioStream_close(mAudioStream);
    }
    if (builder != nullptr){
        AAudioStreamBuilder_delete(builder);
    }
    if (*fd > 0){
        close(*fd);
        delete fd;
    }
}

OboePlayer::OboePlayer(int fd) : fd(dup(fd)){
    logger::info(TAG, "OboePlayer fd: %d", fd);
}
oboe::DataCallbackResult OboePlayer::onAudioReady(
        oboe::AudioStream *audioStream,
        void *audioData,
        int32_t numFrames) {
    const auto size = read(fd, audioData, numFrames * 2 * 2);
    if (size <= 0){
        return oboe::DataCallbackResult::Stop;
    }
    return oboe::DataCallbackResult::Continue;
}
OboePlayer::~OboePlayer(){
    stream->requestStop();
    stream->release();
    close(this->fd);
}
void OboePlayer::startPlay() {
    oboe::AudioStreamBuilder builder;
    builder.setDirection(oboe::Direction::Output);
    builder.setPerformanceMode(oboe::PerformanceMode::LowLatency);
    builder.setSharingMode(oboe::SharingMode::Shared);
    builder.setFormat(oboe::AudioFormat::I16);
    builder.setChannelCount(oboe::ChannelCount::Stereo);
    builder.setSampleRate(44100);
    builder.setCallback(this);

    oboe::Result result = builder.openStream(stream);
    if (result != oboe::Result::OK){
        logger::error(TAG, "open failure");
        return;
    }

    result = stream->requestStart();
    if (result != oboe::Result::OK){
        logger::error(TAG, "start failure");
        return;
    }
}

OboeRecord::OboeRecord(int fd) : fd(dup(fd)){
    logger::info(TAG, "OboeRecord fd: %d", fd);
}
OboeRecord::~OboeRecord(){
    stream->requestStop();
    stream->release();
    close(this->fd);
}
oboe::DataCallbackResult OboeRecord::onAudioReady(oboe::AudioStream *audioStream, void *audioData,
                                                  int32_t numFrames) {
    const auto size = write(this->fd, audioData, numFrames * 2 * 2);
    if (size <= 0){
        logger::info(TAG, "播放结束, fd: %d", this->fd);
        audioStream->requestStop();
        return  oboe::DataCallbackResult::Stop;
    }
    return  oboe::DataCallbackResult::Continue;
}
void OboeRecord::startRecord() {
    oboe::AudioStreamBuilder builder;
    builder.setDirection(oboe::Direction::Input);
    builder.setPerformanceMode(oboe::PerformanceMode::LowLatency);
    builder.setSharingMode(oboe::SharingMode::Shared);
    builder.setFormat(oboe::AudioFormat::I16);
    builder.setChannelCount(oboe::ChannelCount::Stereo);
    builder.setSampleRate(44100);
    builder.setCallback(this);

    oboe::Result result = builder.openStream(stream);
    if (result != oboe::Result::OK){
        logger::error(TAG, "open failure");
        return;
    }

    result = stream->requestStart();
    if (result != oboe::Result::OK){
        logger::error(TAG, "start failure");
        return;
    }
}

extern "C"
JNIEXPORT jlong JNICALL
Java_io_github_opensllearn_utils_Utils_audioStart(JNIEnv *env, jobject, jint fd) {
    // AudioPlayer *player = nullptr;
    // try{
    //     player = new AudioPlayer(fd);
    // } catch (const std::exception &e) {
    //     delete player;
    //     player = nullptr;
    //     env->ThrowNew(env->FindClass("java/lang/RuntimeException"), e.what());
    // }
    // return reinterpret_cast<jlong>(player);

    // AudioRecord *player = nullptr;
    // try{
    //     player = new AudioRecord(fd);
    // } catch (const std::exception &e) {
    //     delete player;
    //     player = nullptr;
    //     env->ThrowNew(env->FindClass("java/lang/RuntimeException"), e.what());
    // }
    // return reinterpret_cast<jlong>(player);

    OboeRecord *player = nullptr;
    try{
        player = new OboeRecord(fd);
        player->startRecord();
    } catch (const std::exception &e) {
        delete player;
        player = nullptr;
        env->ThrowNew(env->FindClass("java/lang/RuntimeException"), e.what());
    }
    return reinterpret_cast<jlong>(player);
}
extern "C"
JNIEXPORT void JNICALL
Java_io_github_opensllearn_utils_Utils_audioRelease(JNIEnv*, jobject, jlong ptr) {
    // auto* player = reinterpret_cast<AudioPlayer*>(ptr);
    // delete player;

    auto* player = reinterpret_cast<OboeRecord*>(ptr);
    delete player;
}




extern "C"
JNIEXPORT jlong JNICALL
Java_io_github_opensllearn_utils_Utils_initCamera(JNIEnv *env, jobject, jint width, jint height, jstring pcmPath) {
    NDKCamera *ndkCamera = nullptr;
    try {
        jboolean isCopy = false;
        const char * const pcmPathStr = env->GetStringUTFChars(pcmPath, &isCopy);
        ndkCamera = new NDKCamera(width, height, pcmPathStr);
        if (isCopy){
            env->ReleaseStringUTFChars(pcmPath, pcmPathStr);
        }
    } catch (const std::exception &e) {
        delete ndkCamera;
        ndkCamera = nullptr;
        env->ThrowNew(env->FindClass("java/lang/RuntimeException"), e.what());
    }
    return reinterpret_cast<jlong>(ndkCamera);
}
extern "C"
JNIEXPORT void JNICALL
Java_io_github_opensllearn_utils_Utils_releaseCamera(JNIEnv*, jobject, jlong ptr) {
    const auto* const ndkKCamera = reinterpret_cast<NDKCamera*>(ptr);
    delete ndkKCamera;
}