package com.eltonkola.everything.di.platform

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import kotlinx.cinterop.ExperimentalForeignApi
import okio.Path.Companion.toPath
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

class IosDataStoreFactory : DataStoreFactory {
    @OptIn(ExperimentalForeignApi::class)
    override fun create(): DataStore<Preferences> {
        return PreferenceDataStoreFactory.createWithPath(
            produceFile = {
                val documentDirectory = NSFileManager.defaultManager.URLForDirectory(
                    directory = NSDocumentDirectory,
                    inDomain = NSUserDomainMask,
                    appropriateForURL = null,
                    create = false,
                    error = null,
                )
                (requireNotNull(documentDirectory).path + "/datastore/settings.preferences_pb").toPath()
            }
        )
    }
}