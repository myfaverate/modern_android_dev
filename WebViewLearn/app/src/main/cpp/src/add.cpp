/**
 * @author zsh
 * @date Created by 29051 on 2025/7/5.
 */
#include "add.hpp"
#include "logging.hpp"

constexpr const char* TAG = "add";

int add(int a, int b){
    logger::info("add", "add a: %d, b: %d", a, b);
    return a + b;
}
