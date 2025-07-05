//
// Created by 29051 on 2025/7/5.
//

#ifndef WEBVIEWLEARN_PERSON_HPP
#define WEBVIEWLEARN_PERSON_HPP
#include<string>
#include<ostream>

class Person{
private:
    std::string name;
    int age;
    std::string gender;
public:

    Person(std::string name, const int &age, std::string gender);

    ~Person();

    [[nodiscard]] const std::string &getName() const;

    void setName(const std::string &name);

    [[nodiscard]] int getAge() const;

    void setAge(int age);

    [[nodiscard]] const std::string &getGender() const;

    void setGender(const std::string &gender);

    friend std::ostream &operator<<(std::ostream &os, const Person &person) {
        os << "Person(name=" << person.name << " age=" << person.age << " gender: " << person.gender << ")";
        return os;
    }

    [[nodiscard]] std::string toString() const;
};
#endif //WEBVIEWLEARN_PERSON_HPP
