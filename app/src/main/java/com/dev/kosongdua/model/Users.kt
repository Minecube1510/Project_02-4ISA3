package com.dev.kosongdua.model

data class Users (
    val uid: String ?= "",
    val name: String ?= "",
    val email: String ?= "",
    val password: String ?= ""
)