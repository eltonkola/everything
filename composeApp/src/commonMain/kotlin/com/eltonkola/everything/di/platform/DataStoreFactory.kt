package com.eltonkola.everything.di.platform

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

interface DataStoreFactory {
    fun create(): DataStore<Preferences>
}