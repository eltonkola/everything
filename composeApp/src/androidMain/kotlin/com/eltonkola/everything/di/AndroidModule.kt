package com.eltonkola.everything.di

import com.eltonkola.everything.di.platform.AndroidDataStoreFactory
import com.eltonkola.everything.di.platform.DataStoreFactory
import org.koin.dsl.module

val androidModule = module {
    single<DataStoreFactory> { AndroidDataStoreFactory(get()) }
}