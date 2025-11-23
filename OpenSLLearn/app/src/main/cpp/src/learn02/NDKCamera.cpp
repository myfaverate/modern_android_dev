//
// Created by 29051 on 2025/10/25.
//
#include "NDKCamera.hpp"

#include <utility>

const char * const TAG = "NDKCamera";

/**
 * CameraManager → CameraService → Camera HAL v3 → Sensor/Driver
 * @param width
 * @param height
 */
NDKCamera::NDKCamera(int width, int height, std::string yuvPath) : mWidth(width), wHeight(height), yuvPath(std::move(yuvPath)) {
    logger::info(TAG, "width: %d, height: %d, yuvPath: %s", this -> mWidth, this -> wHeight, this -> yuvPath.c_str());
    this->yuvStream = new std::ofstream(this->yuvPath, std::ios::binary);
    if (!this->yuvStream->is_open()){
        logger::error(TAG, "文件打开失败...");
        return;
    }
    aCameraManager = ACameraManager_create();
    if (aCameraManager == nullptr){
        logger::error(TAG, "aCameraManager is null");
        return;
    }
    ACameraIdList *cameraIdList = nullptr;
    camera_status_t status = ACameraManager_getCameraIdList(aCameraManager, &cameraIdList);
    if (status != ACAMERA_OK){
        logger::error(TAG, "开启 getCameraIdList 失败");
        return;
    }
    if (cameraIdList->numCameras <= 0){
        logger::error(TAG, "此设备没有摄像头");
        return;
    }
    for(int i = 0; i < cameraIdList->numCameras; i ++ ){
        logger::info(TAG, "index: %d, cameraId: %s", i, cameraIdList->cameraIds[i]);
    }
    const char* cameraId = cameraIdList->cameraIds[1];
    this->printCameraCapabilities(cameraId);
    ACameraDevice_StateCallbacks deviceStateCallbacks = {
            .context = nullptr,
            .onDisconnected = [](void*, ACameraDevice* aCameraDevice) -> void {},
            .onError = [](void*, ACameraDevice* aCameraDevice, int errorCode) -> void {},
    };
    status = ACameraManager_openCamera(aCameraManager, cameraId, &deviceStateCallbacks, &device);
    if (status != ACAMERA_OK){
        logger::error(TAG, "开启 camera 失败");
        return;
    }
    media_status_t mediaStatus = AImageReader_new(width, height, AIMAGE_FORMAT_YUV_420_888, 4, &aImageReader);
    if (mediaStatus != AMEDIA_OK){
        logger::error(TAG, "AImageReader_new 失败");
        return;
    }
    AImageReader_ImageListener imageListener = {
            .context = this,
            .onImageAvailable = [](void* context, AImageReader* reader) -> void {
                AImage *image = nullptr;
                media_status_t mediaStatus = AImageReader_acquireNextImage(reader, &image);
                if (mediaStatus != AMEDIA_OK || image == nullptr){
                    logger::error(TAG, "获取当前yuv帧失败");
                    AImage_delete(image);
                    return;
                }
                int32_t width = 0, height = 0;
                mediaStatus = AImage_getWidth(image, &width);
                if (mediaStatus != AMEDIA_OK || image == nullptr){
                    logger::error(TAG, "获取当前yuv帧宽度失败");
                    AImage_delete(image);
                    return;
                }
                mediaStatus = AImage_getHeight(image, &height);
                if (mediaStatus != AMEDIA_OK || image == nullptr){
                    logger::error(TAG, "获取当前yuv帧高度失败");
                    AImage_delete(image);
                    return;
                }
                // ==========
                const auto *ndkCamera = reinterpret_cast<NDKCamera*>(context);
                int32_t planes = 0;
                AImage_getNumberOfPlanes(image, &planes);
                for (int plane = 0; plane < 3; ++plane) {
                    uint8_t* planeData = nullptr;
                    int planeDataLen = 0;
                    if (AImage_getPlaneData(image, plane, &planeData, &planeDataLen) != AMEDIA_OK) {
                        logger::error(TAG, "AImage_getPlaneData failed plane=%d", plane);
                        AImage_delete(image);
                        return;
                    }
                    int rowStride = 0, pixelStride = 0;
                    AImage_getPlaneRowStride(image, plane, &rowStride);
                    AImage_getPlanePixelStride(image, plane, &pixelStride);

                    int planeWidth = (plane == 0) ? width : width / 2;
                    int planeHeight = (plane == 0) ? height : height / 2;

                    logger::info(TAG, "planes: %d, planeDataLen: %d, rowStride: %d, pixelStride: %d, planeWidth: %d, planeHeight: %d, threadId: %lld", planes, planeDataLen, rowStride, pixelStride, planeWidth, planeHeight, std::this_thread::get_id());

                    // 按行按 pixelStride 写入，确保是连续的 Y then U then V
                    for (int y = 0; y < planeHeight; ++y) {
                        const uint8_t* rowPtr = planeData + y * rowStride;
                        if (pixelStride == 1) {
                            // 直接写 planeWidth 字节
                            ndkCamera->yuvStream->write(reinterpret_cast<const char*>(rowPtr), planeWidth);
                        } else {
                            // 需要按 pixelStride 抽取
                            for (int x = 0; x < planeWidth; ++x) {
                                ndkCamera->yuvStream->put(rowPtr[x * pixelStride]);
                            }
                        }
                    }
                }
                AImage_delete(image);
                logger::info(TAG, "yuv width: %d, height: %d", width, height);
            },
    };
    AImageReader_setImageListener(aImageReader, &imageListener);
    ANativeWindow* window = nullptr;
    mediaStatus = AImageReader_getWindow(aImageReader, &window);
    if (mediaStatus != AMEDIA_OK){
        logger::error(TAG, "AImageReader_getWindow 失败");
        return;
    }
    ACaptureRequest *request = nullptr;
    status = ACameraDevice_createCaptureRequest(device, TEMPLATE_PREVIEW, &request);
    if (status != ACAMERA_OK){
        logger::error(TAG, "开启 ACameraDevice_createCaptureRequest 失败");
        return;
    }
    // 设置帧率范围
    int32_t range[2] = {30, 30}; // 固定 30fps
    ACaptureRequest_setEntry_i32(request,
                                 ACAMERA_CONTROL_AE_TARGET_FPS_RANGE,
                                 2, range);
    ACameraOutputTarget *aCameraOutputTarget = nullptr;
    status = ACameraOutputTarget_create(window, &aCameraOutputTarget);
    if (status != ACAMERA_OK){
        logger::error(TAG, "开启 ACameraOutputTarget_create 失败");
        return;
    }
    status = ACaptureRequest_addTarget(request, aCameraOutputTarget);
    if (status != ACAMERA_OK){
        logger::error(TAG, "开启 ACaptureRequest_addTarget 失败");
        return;
    }
    ACameraCaptureSession_stateCallbacks sessionStateCallbacks = {
            .context = nullptr,
            .onClosed = [](void* context, ACameraCaptureSession *session) -> void {
                logger::info(TAG, "onClosed...");
            },
            .onReady = [](void* context, ACameraCaptureSession *session) -> void {
                logger::info(TAG, "onReady...");
            },
            .onActive = [](void* context, ACameraCaptureSession *session) -> void {
                logger::info(TAG, "onActive...");
            },
    };

    ACameraCaptureSession_captureCallbacks captureCallbacks = {
            .context = nullptr,
            .onCaptureStarted = [](void* context, ACameraCaptureSession* session,
                                   const ACaptureRequest* request, int64_t timestamp) -> void {
                logger::info(TAG, "onCaptureStarted timestamp: %lld", timestamp);
            },
            .onCaptureProgressed = [](void* context, ACameraCaptureSession* session,
                                      ACaptureRequest* request, const ACameraMetadata* result) -> void {
                logger::info(TAG, "onCaptureProgressed...");
            },
            .onCaptureCompleted = [](void* context, ACameraCaptureSession* session,
                                     ACaptureRequest* request, const ACameraMetadata* result) -> void {
                ACameraMetadata_const_entry fpsEntry = {};
                if (ACameraMetadata_getConstEntry(result,
                                                  ACAMERA_CONTROL_AE_TARGET_FPS_RANGE, &fpsEntry) == ACAMERA_OK) {
                    if (fpsEntry.count >= 2) {
                        int32_t minFps = fpsEntry.data.i32[0];
                        int32_t maxFps = fpsEntry.data.i32[1];
                        logger::info(TAG, "onCaptureCompleted 当前帧率范围: [%d, %d]", minFps, maxFps);
                    }
                }
            },
            .onCaptureFailed = [](void* context, ACameraCaptureSession* session,
                                  ACaptureRequest* request, ACameraCaptureFailure* failure) -> void {
                logger::info(TAG, "onCaptureFailed frameNumber: %d, reason: %d, sequenceId: %d, wasImageCaptured: %d", failure->frameNumber, failure->reason, failure->sequenceId, failure->wasImageCaptured);
            },
            .onCaptureSequenceCompleted = [](void* context, ACameraCaptureSession* session,
                                             int sequenceId, int64_t frameNumber) -> void {
                logger::info(TAG, "onCaptureSequenceCompleted sequenceId: %d, frameNumber: %d", sequenceId, frameNumber);
            },
            .onCaptureSequenceAborted = [](void* context, ACameraCaptureSession* session,
                                           int sequenceId) -> void {
                logger::info(TAG, "onCaptureSequenceAborted sequenceId: %d", sequenceId);
            },
            .onCaptureBufferLost = [](void* context, ACameraCaptureSession* session,
                                      ACaptureRequest* request, ACameraWindowType* window, int64_t frameNumber) -> void {
                logger::info(TAG, "onCaptureBufferLost frameNumber: %d", frameNumber);
            },
    };

    status = ACaptureSessionOutputContainer_create(&aCaptureSessionOutputContainer);

    if (status != ACAMERA_OK){
        logger::error(TAG, "开启 ACaptureSessionOutputContainer_create 失败");
        return;
    }
    status = ACaptureSessionOutput_create(window, &sessionOutput);

    if (status != ACAMERA_OK){
        logger::error(TAG, "开启 ACaptureSessionOutput_create 失败");
        return;
    }
    status = ACaptureSessionOutputContainer_add(aCaptureSessionOutputContainer, sessionOutput);
    if (status != ACAMERA_OK){
        logger::error(TAG, "开启 ACaptureSessionOutputContainer_add 失败");
        return;
    }
    status = ACameraDevice_createCaptureSession(device, aCaptureSessionOutputContainer, &sessionStateCallbacks, &session);
    if (status != ACAMERA_OK){
        logger::error(TAG, "开启 ACameraDevice_createCaptureSession 失败");
        return;
    }
#if __ANDROID_API__ >= 33
    ACameraCaptureSession_captureCallbacksV2 captureCallbacksV2 = {
            .context = nullptr,
            .onCaptureStarted = [](void* context, ACameraCaptureSession* session,
                                   const ACaptureRequest* request, int64_t timestamp, int64_t frameNumber) -> void {

            },
            .onCaptureProgressed = [](void* context, ACameraCaptureSession* session,
                                      ACaptureRequest* request, const ACameraMetadata* result) -> void {

            },
            .onCaptureCompleted = [](void* context, ACameraCaptureSession* session,
                                     ACaptureRequest* request, const ACameraMetadata* result) -> void {

            },
            .onCaptureFailed = [](void* context, ACameraCaptureSession* session,
                                  ACaptureRequest* request, ACameraCaptureFailure* failure) -> void {

            },
            .onCaptureSequenceCompleted = [](void* context, ACameraCaptureSession* session,
                                             int sequenceId, int64_t frameNumber) -> void {

            },
            .onCaptureSequenceAborted = [](void* context, ACameraCaptureSession* session,
                                           int sequenceId) -> void {

            },
            .onCaptureBufferLost = [](void* context, ACameraCaptureSession* session,
                                      ACaptureRequest* request, ACameraWindowType* window, int64_t frameNumber) -> void {

            },
    };
    status = ACameraCaptureSession_setRepeatingRequestV2(session, &captureCallbacksV2, 1, &request, nullptr);
    if (status != ACAMERA_OK){
        logger::error(TAG, "开启 ACameraCaptureSession_setRepeatingRequestV2 失败");
        return;
    }
#else
    status = ACameraCaptureSession_setRepeatingRequest(session, &captureCallbacks, 1, &request, nullptr);
    if (status != ACAMERA_OK){
        logger::error(TAG, "开启 ACamferaCaptureSession_setRepeatingRequest 失败");
        return;
    }
#endif
}
NDKCamera::~NDKCamera() {
    logger::info(TAG, "~NDKCamera...");
    if (this->aImageReader != nullptr){
        AImageReader_delete(this->aImageReader);
    }
    if (session != nullptr){
        ACameraCaptureSession_close(session);
    }
    if (device != nullptr){
        ACameraDevice_close(device);
    }
    if (aCameraManager != nullptr) {
        ACameraManager_delete(aCameraManager);
    }
    if (this->yuvStream != nullptr){
        this->yuvStream->close();
    }
    if (this->aCaptureSessionOutputContainer != nullptr){
        ACaptureSessionOutputContainer_free(this->aCaptureSessionOutputContainer);
    }
    if (this->sessionOutput != nullptr){
        ACaptureSessionOutput_free(this->sessionOutput);
    }
}

void NDKCamera::printCameraCapabilities(const char * const cameraId){
    ACameraMetadata *metadata = nullptr;
    camera_status_t status = ACameraManager_getCameraCharacteristics(this->aCameraManager, cameraId, &metadata);
    if(status != ACAMERA_OK){
        logger::error(TAG, "获取摄像头信息失败");
        return;
    }
    ACameraMetadata_const_entry entry = {};
    if (ACameraMetadata_getConstEntry(metadata, ACAMERA_SCALER_AVAILABLE_STREAM_CONFIGURATIONS, &entry) == ACAMERA_OK){
        logger::info(TAG, "支持的分辨率:");
        for(uint32_t i = 0; i + 3 < entry.count; i += 4){
            int32_t format = entry.data.i32[i + 0];
            int32_t width = entry.data.i32[i + 1];
            int32_t height = entry.data.i32[i + 2];
            int32_t isInput = entry.data.i32[i + 3];
            if (isInput == 0 && format == AIMAGE_FORMAT_YUV_420_888){
                logger::info(TAG, "format: %d, width: %d, height: %d, isInput: %d", format, width, height, isInput);
            }
        }
    }
    if (ACameraMetadata_getConstEntry(metadata, ACAMERA_CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES, &entry) == ACAMERA_OK){
        logger::info(TAG, "支持的帧率范围:");
        for (uint32_t i = 0; i + 1 < entry.count; i += 2) {
            logger::info(TAG, "帧率范围: [%d, %d]", entry.data.i32[i], entry.data.i32[i + 1]);
        }
    }
    ACameraMetadata_free(metadata);
}