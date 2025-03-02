package edu.tyut.login.data.remote.repository

import edu.tyut.login.data.remote.bean.Person
import edu.tyut.login.data.remote.bean.Response
import edu.tyut.login.data.remote.service.LoginService
import jakarta.inject.Inject

internal class LoginRepository @Inject constructor(
    private val loginService: LoginService
) {
    internal suspend fun login(): Response<Person> {
        return loginService.login()
    }
}