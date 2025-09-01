package com.eltonkola.everything.di.platform

enum class Platform {
    ANDROID, IOS, DESKTOP
}

expect fun getPlatform(): Platform

expect fun getNotesDirectory(): String