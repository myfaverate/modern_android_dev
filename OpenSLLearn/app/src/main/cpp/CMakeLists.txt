
# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html.
# For more examples on how to use CMake, see https://github.com/android/ndk-samples.

# Sets the minimum CMake version required for this project.
cmake_minimum_required(VERSION 3.22.1)

# Declares the project name. The project name can be accessed via ${ PROJECT_NAME},
# Since this is the top level CMakeLists.txt, the project name is also accessible
# with ${CMAKE_PROJECT_NAME} (both CMake variables are in-sync within the top level
# build script scope).
project(openslLearn VERSION 0.1.0 LANGUAGES C CXX)

# ✅ 设置 C++ 标准
set(CMAKE_CXX_STANDARD 23)  # 使用 C++26 标准
set(CMAKE_CXX_STANDARD_REQUIRED ON)  # 强制使用指定标准
set(CMAKE_CXX_EXTENSIONS OFF)        # 禁用编译器扩展（使用纯标准）

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.
#
# In this top level CMakeLists.txt, ${CMAKE_PROJECT_NAME} is used to define
# the target library name; in the sub-module's CMakeLists.txt, ${PROJECT_NAME}
# is preferred for the same purpose.
#
# In order to load a library into your app from Java/Kotlin, you must call
# System.loadLibrary() and pass the name of the library defined here;
# for GameActivity/NativeActivity derived applications, the same library name must be
# used in the AndroidManifest.xml file.

# 第一个库
# 查找源文件
file(GLOB_RECURSE LEARN01_SOURCES CONFIGURE_DEPENDS
        "src/learn01/*.cpp"
        "src/learn01/*.c"
)
add_library(${CMAKE_PROJECT_NAME} SHARED ${LEARN01_SOURCES})

# 设置头文件包含路径
target_include_directories(${CMAKE_PROJECT_NAME}
        PUBLIC ${CMAKE_CURRENT_SOURCE_DIR}/include/learn01
        PUBLIC ${CMAKE_CURRENT_SOURCE_DIR}/include/logging
)

# Specifies libraries CMake should link to your target library. You
# can link libraries from various origins, such as libraries defined in this
# build script, prebuilt third-party libraries, or Android system libraries.
target_link_libraries(${CMAKE_PROJECT_NAME}
    # List libraries link to the target library
    android
    log
    OpenSLES
)

# 新增第二个库 (openslLearn2)
file(GLOB_RECURSE LEARN02_SOURCES CONFIGURE_DEPENDS
        "src/learn02/*.cpp"
        "src/learn02/*.c"
        "src/sqlite/*.cpp"
        "src/sqlite/*.c"
)
set(LIBRARY_NAME2 ${CMAKE_PROJECT_NAME}2)
message("LIBRARY_NAME2: ${LIBRARY_NAME2}")
add_library(${LIBRARY_NAME2} SHARED ${LEARN02_SOURCES})  # 使用不同源文件
target_include_directories(${LIBRARY_NAME2}
        PUBLIC ${CMAKE_CURRENT_SOURCE_DIR}/include/sqlite
        PUBLIC ${CMAKE_CURRENT_SOURCE_DIR}/include/learn02
        PUBLIC ${CMAKE_CURRENT_SOURCE_DIR}/include/logging
)
find_package (oboe REQUIRED CONFIG)
target_link_libraries(${LIBRARY_NAME2}
        android
        log
        aaudio
        oboe::oboe
        camera2ndk
        mediandk
)