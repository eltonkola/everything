package com.eltonkola.everything.di.platform

actual fun getPlatform(): Platform = Platform.DESKTOP

actual fun getNotesDirectory(): String =
    "${System.getProperty("user.home")}/.everything/notes"