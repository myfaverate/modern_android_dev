//
// Created by 29051 on 2025/10/25.
//

#ifndef OPENSL_LEARN_CAMERA_HPP
#define OPENSL_LEARN_CAMERA_HPP

extern "C" {
#include <camera/NdkCameraManager.h>
#include <media/NdkImageReader.h>
}

#include <string>
#include <fstream>
#include <thread>

#include "logging.hpp"

class NDKCamera {
private:
    int mWidth;
    int wHeight;
    ACameraManager *aCameraManager = nullptr;
    ACameraDevice *device = nullptr;
    ACameraCaptureSession *session = nullptr;
    AImageReader *aImageReader = nullptr;
    ACaptureSessionOutputContainer *aCaptureSessionOutputContainer = nullptr;
    ACaptureSessionOutput *sessionOutput = nullptr;
    std::string yuvPath;
    std::ofstream *yuvStream = nullptr;
public:
    NDKCamera(int width, int height, std::string yuvPath);
    ~NDKCamera();
    /**
     * Capabilities 功能
     */
    void printCameraCapabilities(const char * cameraId);
};

#endif //OPENSL_LEARN_CAMERA_HPP
