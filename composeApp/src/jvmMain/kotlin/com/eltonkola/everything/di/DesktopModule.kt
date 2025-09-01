package com.eltonkola.everything.di

import com.eltonkola.everything.di.platform.DataStoreFactory
import com.eltonkola.everything.di.platform.DesktopDataStoreFactory
import org.koin.dsl.module

val desktopModule = module {
    single<DataStoreFactory> { DesktopDataStoreFactory() }
}