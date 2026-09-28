package mcp

import kotlinx.serialization.json.*

class McpRouter(
    private val handler: McpActionHandler,
    private val json: Json,
    private val logListener: ((String) -> Unit)?
) {
    suspend fun routeRequest(request: JsonRpcRequest): String {
        return when (request.method) {
            "initialize" -> handleInitialize(request.id)
            "notifications/initialized" -> {
                logListener?.invoke("MCP Client fully initialized.")
                "" // Notifications don't get responses
            }
            "ping" -> handlePing(request.id)
            "tools/list" -> handleToolsList(request.id)
            "tools/call" -> handleToolsCall(request)
            else -> {
                logListener?.invoke("Unknown method: ${request.method}")
                if (request.id != null) {
                    buildErrorResponse(request.id, -32601, "Method not found: ${request.method}")
                } else {
                    ""
                }
            }
        }
    }

    private fun handleInitialize(id: JsonElement?): String {
        val result = buildJsonObject {
            put("protocolVersion", "2024-11-05")
            put("capabilities", buildJsonObject { 
                put("tools", buildJsonObject { })
            })
            put("serverInfo", buildJsonObject { 
                put("name", "bundletool-mcp")
                put("version", "1.0.0")
            })
        }
        return buildResultResponse(id, result)
    }

    private fun handlePing(id: JsonElement?): String {
        return buildResultResponse(id, buildJsonObject { })
    }

    private fun handleToolsList(id: JsonElement?): String {
        val tools = listOf(
            McpTool(
                name = "build_apks",
                description = "Builds an APKs archive from an AAB file using BundleTool",
                inputSchema = ToolInputSchema(
                    properties = mapOf(
                        "aabPath" to PropertySchema("string", "Absolute path to the .aab file"),
                        "outputDir" to PropertySchema("string", "Directory to save the .apks file"),
                        "mode" to PropertySchema("string", "Build mode", listOf("universal", "default")),
                        "localTesting" to PropertySchema("boolean", "Enable local testing flag")
                    ),
                    required = listOf("aabPath", "outputDir")
                )
            ),
            McpTool(
                name = "install_apks",
                description = "Installs an APKs archive to a connected device",
                inputSchema = ToolInputSchema(
                    properties = mapOf(
                        "apksPath" to PropertySchema("string", "Absolute path to the .apks file"),
                        "deviceId" to PropertySchema("string", "Specific device ID (optional)")
                    ),
                    required = listOf("apksPath")
                )
            ),
            McpTool(
                name = "get_device_spec",
                description = "Generates a device spec JSON for a connected device",
                inputSchema = ToolInputSchema(
                    properties = mapOf(
                        "outputPath" to PropertySchema("string", "Path to save the JSON file"),
                        "deviceId" to PropertySchema("string", "Specific device ID (optional)")
                    ),
                    required = listOf("outputPath")
                )
            ),
            McpTool(
                name = "get_size",
                description = "Gets size metrics for an APKs file",
                inputSchema = ToolInputSchema(
                    properties = mapOf(
                        "apksPath" to PropertySchema("string", "Path to the .apks file"),
                        "dimensions" to PropertySchema("string", "Comma-separated dimensions (e.g. SDK,ABI)")
                    ),
                    required = listOf("apksPath")
                )
            )
        )
        val result = buildJsonObject {
            put("tools", json.encodeToJsonElement(tools))
        }
        return buildResultResponse(id, result)
    }

    private suspend fun handleToolsCall(request: JsonRpcRequest): String {
        val paramsStr = request.params?.jsonObject?.toString() ?: "{}"
        val callParams = json.decodeFromString<CallToolRequestParams>(paramsStr)
        
        logListener?.invoke("Executing tool: ${callParams.name}...")
        
        return try {
            val resultText = when (callParams.name) {
                "build_apks" -> {
                    val aabPath = callParams.arguments["aabPath"]?.jsonPrimitive?.content ?: ""
                    val outputDir = callParams.arguments["outputDir"]?.jsonPrimitive?.content ?: ""
                    val mode = callParams.arguments["mode"]?.jsonPrimitive?.content ?: "default"
                    val localTesting = callParams.arguments["localTesting"]?.jsonPrimitive?.booleanOrNull ?: false
                    handler.buildApks(aabPath, outputDir, mode, localTesting)
                }
                "install_apks" -> {
                    val apksPath = callParams.arguments["apksPath"]?.jsonPrimitive?.content ?: ""
                    val deviceId = callParams.arguments["deviceId"]?.jsonPrimitive?.content
                    handler.installApks(apksPath, deviceId)
                }
                "get_device_spec" -> {
                    val outputPath = callParams.arguments["outputPath"]?.jsonPrimitive?.content ?: ""
                    val deviceId = callParams.arguments["deviceId"]?.jsonPrimitive?.content
                    handler.getDeviceSpec(deviceId, outputPath)
                }
                "get_size" -> {
                    val apksPath = callParams.arguments["apksPath"]?.jsonPrimitive?.content ?: ""
                    val dimensions = callParams.arguments["dimensions"]?.jsonPrimitive?.content
                    handler.getSize(apksPath, dimensions)
                }
                else -> throw IllegalArgumentException("Unknown tool: ${callParams.name}")
            }
            
            val result = CallToolResult(content = listOf(ToolContent(text = resultText)))
            logListener?.invoke("Tool '${callParams.name}' executed successfully.")
            buildResultResponse(request.id, json.encodeToJsonElement(result))
            
        } catch (e: Exception) {
            logListener?.invoke("Tool execution failed: ${e.message}")
            val result = CallToolResult(content = listOf(ToolContent(text = "Error: ${e.message}")), isError = true)
            buildResultResponse(request.id, json.encodeToJsonElement(result))
        }
    }

    private fun buildResultResponse(id: JsonElement?, result: JsonElement): String {
        val obj = buildJsonObject {
            put("jsonrpc", "2.0")
            if (id != null) put("id", id)
            put("result", result)
        }
        return json.encodeToString(JsonElement.serializer(), obj)
    }

    private fun buildErrorResponse(id: JsonElement?, code: Int, message: String): String {
        val obj = buildJsonObject {
            put("jsonrpc", "2.0")
            if (id != null) put("id", id)
            put("error", buildJsonObject {
                put("code", code)
                put("message", message)
            })
        }
        return json.encodeToString(JsonElement.serializer(), obj)
    }
}
