package local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import java.io.File

interface Storage {
    fun provideDataStore(): DataStore<Preferences>
}

object StorageProvider : Storage {
    override fun provideDataStore(): DataStore<Preferences> {
        return createDataStore(
            producePath = {
                val file = File(System.getProperty("java.io.tmpdir"), dataStoreFileName)
                file.absolutePath
            }
        )
    }
}