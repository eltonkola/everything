package com.eltonkola.everything.di

import com.eltonkola.everything.di.platform.DataStoreFactory
import com.eltonkola.everything.di.platform.IosDataStoreFactory
import org.koin.dsl.module


val iosModule = module {
    single<DataStoreFactory> { IosDataStoreFactory() }
}