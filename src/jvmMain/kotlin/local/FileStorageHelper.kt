package local

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.Input
import com.esotericsoftware.kryo.io.Output
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.file.Paths

const val DB_PATH = "/storage"

class FileStorageHelper {

    private lateinit var kryo: Kryo

    init {
        initializeDir()
    }
    private fun getKryo(): Kryo {
        return if (this::kryo.isInitialized) {
            kryo
        } else {
            val kryo = Kryo()
            kryo.register(KiteTable::class.java)
            kryo
        }
    }

    private fun initializeDir() {
        val file = File(getCurrentDir())
        file.mkdir()
    }

    private fun getCurrentDir(): String {
        return Paths.get("").toAbsolutePath().toString() + DB_PATH
    }

    private fun getPath(key: String): String {
        return "${getCurrentDir()}/$key.kb"
    }

    fun <E> save(key: String, value: E) {
        var kryoOutput: Output?
        try {
            val kiteTable = KiteTable(value)
            val fileStream = FileOutputStream(File(getPath(key)))
            kryoOutput = Output(fileStream)
            getKryo().writeObject(kryoOutput, kiteTable)
            kryoOutput.flush()
            fileStream.flush()
            kryoOutput.close()
        } catch (e: Exception) {
            throw KiteDbException(e.message)
        }
    }

    fun read(key: String): Any? {
        val keyFile = File(getPath(key))
        if (!keyFile.exists()) return null
        val kryoInput = Input(FileInputStream(keyFile))
        val kiteTable = getKryo().readObject(kryoInput, KiteTable::class.java)
        return kiteTable.mContent
    }

    fun delete(key: String): Boolean {
        val keyFile = File(getPath(key))
        return if (keyFile.exists()) {
            keyFile.delete()
        } else {
            false
        }
    }

}