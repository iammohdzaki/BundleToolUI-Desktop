package mcp

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class JsonRpcRequest(
    val jsonrpc: String = "2.0",
    val id: JsonElement? = null,
    val method: String,
    val params: JsonElement? = null
)

@Serializable
data class JsonRpcResponse(
    val jsonrpc: String = "2.0",
    val id: JsonElement?,
    val result: JsonElement? = null,
    val error: JsonRpcError? = null
)

@Serializable
data class JsonRpcError(
    val code: Int,
    val message: String
)

@Serializable
data class McpTool(
    val name: String,
    val description: String,
    val inputSchema: ToolInputSchema
)

@Serializable
data class ToolInputSchema(
    val type: String = "object",
    val properties: Map<String, PropertySchema>,
    val required: List<String> = emptyList()
)

@Serializable
data class PropertySchema(
    val type: String,
    val description: String? = null,
    val enum: List<String>? = null
)

@Serializable
data class CallToolRequestParams(
    val name: String,
    val arguments: Map<String, JsonElement>
)

@Serializable
data class CallToolResult(
    val content: List<ToolContent>,
    val isError: Boolean = false
)

@Serializable
data class ToolContent(
    val type: String = "text",
    val text: String
)
