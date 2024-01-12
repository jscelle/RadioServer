package com.jscelle.Services.User

import kotlinx.serialization.Serializable

@Serializable
data class UserCredentials(val name: String, val password: String)

@Serializable
data class UserCreate(val name: String, val password: String, val status: String)

@Serializable
data class User(
    val name: String,
    val password: String, // Omitting password for security
    val status: String
)
