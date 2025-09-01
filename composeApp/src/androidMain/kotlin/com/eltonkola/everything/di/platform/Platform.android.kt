package com.eltonkola.everything.di.platform

import com.eltonkola.everything.MainApp


actual fun getPlatform(): Platform = Platform.ANDROID

actual fun getNotesDirectory(): String =
    "${MainApp.context?.filesDir}/notes"