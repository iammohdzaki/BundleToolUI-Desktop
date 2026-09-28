package mcp

import java.io.File

/**
 * Interface that the UI application (:composeApp) implements to fulfill MCP requests.
 */
interface McpActionHandler {
    
    /**
     * Build an APK from an AAB.
     * @param aabPath Path to the input .aab file
     * @param outputDir Directory to save the output .apks
     * @param mode "universal" or "default" (apk-set)
     * @param localTesting true if local-testing flag should be used
     * @return The absolute path to the generated .apks file
     */
    suspend fun buildApks(
        aabPath: String, 
        outputDir: String, 
        mode: String, 
        localTesting: Boolean
    ): String
    
    /**
     * Install an APKs file to a device.
     * @param apksPath Path to the .apks file
     * @param deviceId Optional specific device ID
     * @return A status message or log output
     */
    suspend fun installApks(
        apksPath: String, 
        deviceId: String?
    ): String
    
    /**
     * Get the size metrics for an APKs file.
     * @param apksPath Path to the .apks file
     * @param dimensions Comma-separated list of dimensions (e.g. "SDK,ABI")
     * @return The parsed JSON equivalent of the size CSV
     */
    suspend fun getSize(
        apksPath: String, 
        dimensions: String?
    ): String
    
    /**
     * Generates a device spec JSON for a connected device.
     * @param deviceId Optional specific device ID
     * @param outputPath Path to save the JSON file
     * @return The absolute path to the generated JSON file
     */
    suspend fun getDeviceSpec(
        deviceId: String?, 
        outputPath: String
    ): String
}
