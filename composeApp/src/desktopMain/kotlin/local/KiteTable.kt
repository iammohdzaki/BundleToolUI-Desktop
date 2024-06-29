package local

class KiteTable<T> {
    internal constructor()
    internal constructor(content: T) {
        mContent = content
    }

    // Serialized content
    var mContent: T? = null
}