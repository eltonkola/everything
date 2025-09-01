package com.eltonkola.everything

import android.app.Application
import android.content.Context
import com.eltonkola.everything.di.androidModule
import com.eltonkola.everything.di.appModule
import com.eltonkola.everything.di.initKoin
import org.koin.android.ext.koin.androidContext

class MainApp : Application() {

    companion object {
        var context: Context? = null
    }

    override fun onCreate() {
        super.onCreate()
        context =  this
        initKoin {
            androidContext(this@MainApp)
            modules(appModule, androidModule)
        }
    }
}