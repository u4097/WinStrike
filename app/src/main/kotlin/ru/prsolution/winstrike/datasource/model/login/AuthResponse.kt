package ru.prsolution.winstrike.datasource.model.login

class AuthResponse(
    var message: String? = null,
    var token: String? = null,
    var user: User? = null
)
