# --- Kotlin / Compose ---
-dontwarn kotlin.**
-dontwarn kotlinx.**

# --- Ktor ---
-dontwarn io.ktor.**
-dontwarn org.slf4j.**
-dontwarn ch.qos.logback.**

# --- Android references inside libs ---
-dontwarn android.**

# --- Jakarta / Servlet / Mail ---
-dontwarn jakarta.servlet.**
-dontwarn jakarta.mail.**
-dontwarn javax.mail.**

# --- Okio / Virtual threads ---
-dontwarn okio.**
-dontwarn java.lang.Thread$Builder*

# --- Reflection-heavy libs ---
-dontwarn org.codehaus.janino.**
-dontwarn org.codehaus.commons.compiler.**

# Keep main entry
-keep class **Kt {
    public static void main(java.lang.String[]);
}
