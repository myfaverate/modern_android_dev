#include "helloKtorFit.hpp"

static const char * const TAG = "helloKtorFit";

asio::awaitable<decltype(std::string::size_type())> async_print(const std::string message){
    auto executor = co_await asio::this_coro::executor;
    asio::steady_timer timer(executor);
    timer.expires_after(std::chrono::seconds(1));
    co_await timer.async_wait(asio::use_awaitable);
    logger::info(TAG, "message: %s", message.c_str());
    co_return message.size();
}

void hello1(){
    auto cores = std::thread::hardware_concurrency();

    logger::info(TAG, "cores: %d", cores);

    asio::io_context ioContext;

    // 创建一个 work_guard 避免 io_context 过早退出
    auto work = asio::make_work_guard(ioContext);

    // 启动线程池
    std::vector<std::thread> threads;
    for(unsigned i = 0; i < cores; ++i){
        threads.emplace_back([&](){ ioContext.run(); });
    }

    // 提交协程
    auto result1 = asio::co_spawn(ioContext, async_print("hello1"), asio::use_future);
    auto result2 = asio::co_spawn(ioContext, async_print("hello21"), asio::use_future);

    // 等待结果
    logger::info(TAG, "before1...");
    logger::info(TAG, "result1: %d, result2: %d", result1.get(), result2.get());
    logger::info(TAG, "after1...");

    // 停止 io_context
    work.reset();
    logger::info(TAG, "before2...");
    for(auto& t : threads) t.join();
    logger::info(TAG, "after2...");
}

void hello2(){
    const auto cores = std::thread::hardware_concurrency();
    asio::thread_pool pool(cores);
    auto f1 = asio::co_spawn(pool, async_print("hello1"), asio::use_future);
    auto f2 = asio::co_spawn(pool, async_print("hello21"), asio::use_future);
    // 等待结果
    logger::info(TAG, "before1...");
    logger::info(TAG, "result1: %d, result2: %d", f1.get(), f2.get());
    logger::info(TAG, "after1...");
}

extern "C"
JNIEXPORT void JNICALL
Java_edu_tyut_helloktorfit_utils_NativeUtils_helloWorld(JNIEnv*, jobject) {
    hello2();
}