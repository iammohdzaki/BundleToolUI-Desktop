package local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import okio.Path.Companion.toPath

internal const val dataStoreFileName = "bundletool_ui.preferences_pb"

fun createDataStore(producePath: () -> String): DataStore<Preferences> =
    PreferenceDataStoreFactory.createWithPath(
        produceFile = { producePath().toPath() }
    )

class AppDataStore(
    private val dataStore: DataStore<Preferences>,
    val json: Json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        isLenient = true
    }
) {

    // Save primitive (String, Boolean, Int, etc.)
    suspend fun <T> save(key: Preferences.Key<T>, value: T) {
        dataStore.edit { prefs -> prefs[key] = value }
    }

    // Get primitive with default value
    suspend fun <T> get(key: Preferences.Key<T>, default: T): T {
        return dataStore.data.map { prefs -> prefs[key] ?: default }.first()
    }

    // Save object as JSON using kotlinx.serialization
    suspend inline fun <reified T> saveObject(key: Preferences.Key<String>, obj: T) {
        val jsonString = json.encodeToString(obj)
        save(key, jsonString)
    }

    // Get object from JSON
    suspend inline fun <reified T> getObject(key: Preferences.Key<String>): T? {
        val jsonString = get(key, "")
        return if (jsonString.isEmpty()) null else json.decodeFromString<T>(jsonString)
    }

    // Clear all preferences
    suspend fun clear() {
        dataStore.edit { it.clear() }
    }
}