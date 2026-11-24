package com.example.lab5.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.searchDataStore by preferencesDataStore(
    name = "search_preferences"
)

class SearchPreferences(
    private val context: Context
) {

    companion object {
        private val LAST_SEARCH = stringPreferencesKey("last_search")
    }

    val lastSearch: Flow<String?> =
        context.searchDataStore.data.map { preferences ->
            preferences[LAST_SEARCH]
        }

    suspend fun saveSearch(iataCode: String) {
        context.searchDataStore.edit { preferences ->
            preferences[LAST_SEARCH] = iataCode
        }
    }

    suspend fun clearSearch() {
        context.searchDataStore.edit { preferences ->
            preferences.remove(LAST_SEARCH)
        }
    }
}