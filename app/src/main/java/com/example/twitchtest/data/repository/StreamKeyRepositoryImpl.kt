package com.example.twitchtest.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.twitchtest.domain.repository.StreamKeyRepository
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class StreamKeyRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : StreamKeyRepository {

    override fun getStreamKey(): Flow<String?> =
        dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                preferences[STREAM_KEY]
            }

    override suspend fun saveStreamKey(key: String) {
        dataStore.edit { preferences ->
            preferences[STREAM_KEY] = key
        }
    }

    private companion object {
        val STREAM_KEY = stringPreferencesKey("stream_key")
    }
}

