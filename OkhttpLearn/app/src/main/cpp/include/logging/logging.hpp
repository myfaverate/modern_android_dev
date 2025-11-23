/**
 * @author zsh
 *
 * Created by 29051 on 2025/11/22.
 */
#ifndef OKHTTPLEARN_LOGGING_HPP
#define OKHTTPLEARN_LOGGING_HPP

#include <cstdarg>
#include <android/log.h>

namespace logger {
    inline void info(const char * const tag, const char * const fmt, ...) {
        va_list args;
        va_start(args, fmt);
        __android_log_vprint(ANDROID_LOG_INFO, tag, fmt, args);
        va_end(args);
    }
    inline void error(const char * const tag, const char * const fmt, ...) {
        va_list args;
        va_start(args, fmt);
        __android_log_vprint(ANDROID_LOG_ERROR, tag, fmt, args);
        va_end(args);
    }
}

#endif // OKHTTPLEARN_LOGGING_HPP
