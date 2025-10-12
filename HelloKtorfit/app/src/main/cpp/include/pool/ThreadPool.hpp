//
// Created by 29051 on 2025/9/26.
//

#ifndef HELLOKTORFIT_THREADPOOL_HPP
#define HELLOKTORFIT_THREADPOOL_HPP



#endif //HELLOKTORFIT_THREADPOOL_HPP
/*
#include <asio.hpp>
#include <thread>
#include <vector>
#include <memory>
#include <mutex>

class AsioThreadPool {
public:
    static AsioThreadPool& instance() {
        static AsioThreadPool pool; // C++11 保证线程安全
        return pool;
    }

    asio::io_context& get_io() { return io_; }

    void stop() {
        guard_.reset();
        for (auto& t : threads_) {
            if (t.joinable()) t.join();
        }
        threads_.clear();
    }

private:
    AsioThreadPool()
        : guard_(asio::make_work_guard(io_)) {
        unsigned cores = std::thread::hardware_concurrency();
        if (cores == 0) cores = 2;
        unsigned threadCount = std::max(1u, cores - 1);

        for (unsigned i = 0; i < threadCount; ++i) {
            threads_.emplace_back([this]{ io_.run(); });
        }
    }

    ~AsioThreadPool() {
        stop();
    }

    asio::io_context io_;
    asio::executor_work_guard<asio::io_context::executor_type> guard_;
    std::vector<std::thread> threads_;
};

extern "C"
JNIEXPORT void JNICALL
Java_edu_tyut_helloktorfit_utils_NativeUtils_helloWorld(JNIEnv*, jobject) {
    auto& pool = AsioThreadPool::instance();
    auto& io = pool.get_io();

    auto f1 = asio::co_spawn(io, async_print("hello1"), asio::use_future);
    auto f2 = asio::co_spawn(io, async_print("hello21"), asio::use_future);

    logger::info("helloKtorFit", "res1=%d, res2=%d", f1.get(), f2.get());
}

JNIEXPORT void JNICALL JNI_OnUnload(JavaVM* vm, void*) {
    AsioThreadPool::instance().stop();
}
 */

/*
超时任务
#include <asio.hpp>
#include <chrono>

using namespace std::chrono_literals;

template<typename Awaitable>
asio::awaitable<typename asio::awaitable_traits<Awaitable>::await_result_t>
run_with_timeout(Awaitable task, std::chrono::steady_clock::duration timeout) {
    using result_t = typename asio::awaitable_traits<Awaitable>::await_result_t;

    auto executor = co_await asio::this_coro::executor;

    asio::steady_timer timer(executor);
    timer.expires_after(timeout);

    asio::error_code ec;

    // 并行等待：要么 task 完成，要么 timer 到期
    co_return co_await (
        std::move(task)
        || timer.async_wait(asio::redirect_error(asio::use_awaitable, ec))
    );
}

 asio::awaitable<int> async_job(std::string name) {
    co_await asio::steady_timer(co_await asio::this_coro::executor)
                  .async_wait(asio::use_awaitable); // 模拟耗时
    co_return 42;
}

 auto& pool = AsioThreadPool::instance();

auto fut = asio::co_spawn(
    pool.get_io(),
    run_with_timeout(async_job("hello"), 3s),
    asio::use_future
);

try {
    int result = fut.get();  // 如果超时，可能返回默认值 / 抛异常
    std::cout << "result=" << result << std::endl;
} catch (std::exception& e) {
    std::cout << "task error: " << e.what() << std::endl;
}
 */