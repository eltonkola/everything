package com.eltonkola.everything

import com.eltonkola.everything.di.appModule
import com.eltonkola.everything.di.initKoin
import com.eltonkola.everything.di.iosModule

fun startKoin() {
    initKoin{
        modules(appModule, iosModule)
    }
}