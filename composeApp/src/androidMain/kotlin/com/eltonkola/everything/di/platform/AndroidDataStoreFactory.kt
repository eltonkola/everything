package com.eltonkola.everything.di.platform

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

class AndroidDataStoreFactory(private val context: Context) : DataStoreFactory {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

    override fun create(): DataStore<Preferences> = context.dataStore
}