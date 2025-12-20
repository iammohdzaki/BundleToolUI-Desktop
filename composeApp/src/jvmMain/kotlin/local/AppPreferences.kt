package local

import androidx.datastore.preferences.core.stringPreferencesKey
import data.state.PersistedBundleToolConfig

private const val TAG = "AppPreferences"

class AppPreferences(private val appDataStore: AppDataStore) {

    companion object Keys {
        val CONFIG_KEY = stringPreferencesKey("bundle_tool_config")
    }

    // --- Save Config ---
    suspend fun saveBundleToolConfig(config: PersistedBundleToolConfig) {
        appDataStore.saveObject(CONFIG_KEY, config)
    }

    // --- Load Config ---
    suspend fun getBundleToolConfig(): PersistedBundleToolConfig? {
        return appDataStore.getObject(CONFIG_KEY)
    }

    // --- Clear All ---
    suspend fun clearAll() {
        appDataStore.clear()
    }
}