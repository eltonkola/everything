package com.eltonkola.everything.di.platform

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import okio.Path.Companion.toPath
import java.io.File

class DesktopDataStoreFactory : DataStoreFactory {
    override fun create(): DataStore<Preferences> {
        return PreferenceDataStoreFactory.createWithPath(
            produceFile = {
                val appDataDir = System.getProperty("user.home") + "/.myapp"
                File(appDataDir).mkdirs()
                "$appDataDir/settings.preferences_pb".toPath()
            }
        )
    }
}