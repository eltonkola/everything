package com.eltonkola.everything.di

import com.eltonkola.everything.data.local.AppSettings
import com.eltonkola.everything.data.local.NoteManager
import com.eltonkola.everything.data.parser.EvryParser
import com.eltonkola.everything.data.repository.EvryRepository
import com.eltonkola.everything.data.repository.FileSystemInterface
import com.eltonkola.everything.data.repository.NotesRepository
import com.eltonkola.everything.data.repository.NotesUseCase
import com.eltonkola.everything.data.repository.OkioFileSystemWrapper
import com.eltonkola.everything.di.platform.DataStoreFactory
import com.eltonkola.everything.ui.screens.LandingViewModel
import com.eltonkola.everything.ui.screens.MainAppViewModel
import com.eltonkola.everything.ui.screens.SplashViewModel
import com.eltonkola.everything.ui.screens.main.TabFilesViewModel
import com.eltonkola.everything.ui.screens.main.TabHomeViewModel
import com.eltonkola.everything.ui.screens.main.TabMapViewModel
import com.eltonkola.everything.ui.screens.main.edit.NoteEditViewModel
import com.eltonkola.everything.ui.screens.main.settings.MainSettingsViewModel
import com.eltonkola.everything.ui.screens.main.settings.theme.ThemeSettingsViewModel
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.core.logger.Logger
import org.koin.core.logger.MESSAGE
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module
import co.touchlab.kermit.Logger as KermitLogger

val appModule = module {

    singleOf(::AppSettings)

    viewModelOf(::SplashViewModel)
    viewModelOf(::LandingViewModel)
    viewModelOf(::MainAppViewModel)
    viewModelOf(::TabHomeViewModel)
    viewModelOf(::TabMapViewModel)
    viewModelOf(::TabFilesViewModel)
    viewModelOf(::MainSettingsViewModel)
    viewModelOf(::ThemeSettingsViewModel)

    single { get<DataStoreFactory>().create() }

    single<FileSystemInterface> { OkioFileSystemWrapper() }

    single<NotesRepository>{ EvryRepository(get(), get())}

    singleOf(::NoteManager)
    singleOf(::EvryParser)
    viewModelOf(::NoteEditViewModel)

    singleOf(::NotesUseCase)

}

fun initKoin(appDeclaration: KoinAppDeclaration = {}) {
    startKoin {
        logger(KermitLogger.asKoinLogger())
        appDeclaration()
    }
}

fun KermitLogger.asKoinLogger(): Logger {
    val koinKermit = this.withTag("Koin")

    return object : Logger() {
        override fun display(level: Level, msg: MESSAGE) {
            when (level) {
                Level.DEBUG -> koinKermit.d { msg }
                Level.INFO -> koinKermit.i { msg }
                Level.WARNING -> koinKermit.w { msg }
                Level.ERROR -> koinKermit.e { msg }
                Level.NONE -> { /* Do nothing for NONE level */ }
            }
        }
    }
}
