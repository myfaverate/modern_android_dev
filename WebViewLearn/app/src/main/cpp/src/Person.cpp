//
// Created by 29051 on 2025/7/5.
//
#include "Person.hpp"
#include "logging.hpp"

constexpr const char* TAG = "Person";

Person::Person(std::string name, const int &age, std::string gender): name(std::move(name)), age(age), gender(std::move(gender)) {
    std::string format = std::format("Person(name={}, age={}, gender={})", this->name, this->age, this->gender);
    // LOGI("Person init: %s", format.c_str());
    logger::info(TAG, "Person init: %s", format.c_str());
}

Person::~Person() {
    logger::info(TAG, "Person 析构函数调用...");
}

const std::string &Person::getName() const {
    return this -> name;
}

#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wshadow"
void Person::setName(const std::string &name) {
#pragma clang diagnostic pop
    this->name = name;
}

int Person::getAge() const {
    return this->age;
}

#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wshadow"
void Person::setAge(int age) {
#pragma clang diagnostic pop
    this->age = age;
}

const std::string &Person::getGender() const {
    return this->gender;
}

#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wshadow"
void Person::setGender(const std::string &gender) {
#pragma clang diagnostic pop
    this->gender = gender;
}

std::string Person::toString() const {
    return std::format("Person(name={}, age={}, gender={})", this->name, this->age, this->gender);
}