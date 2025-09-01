package com.eltonkola.everything.di.platform

import platform.Foundation.NSHomeDirectory

actual fun getPlatform(): Platform = Platform.IOS

actual fun getNotesDirectory(): String {
    return "${NSHomeDirectory()}/Documents/YourApp/notes"
}