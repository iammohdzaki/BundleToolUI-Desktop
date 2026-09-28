package mcp

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.sse.SSE
import io.ktor.server.sse.sse
import io.ktor.sse.ServerSentEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.serialization.json.*

object McpServer {
    private var serverJob: Job? = null
    private var engine: EmbeddedServer<*, *>? = null
    private val sessionChannel = Channel<String>(Channel.UNLIMITED)
    
    // Allow UI to subscribe to logs
    var logListener: ((String) -> Unit)? = null

    private val json = Json { 
        ignoreUnknownKeys = true
        encodeDefaults = true
        explicitNulls = false
    }

    fun start(port: Int = 8123, handler: McpActionHandler) {
        if (serverJob != null) return

        logListener?.invoke("Starting MCP Server on port $port...")
        
        serverJob = CoroutineScope(Dispatchers.IO).launch {
            try {
                engine = embeddedServer(Netty, port = port, host = "127.0.0.1") {
                    install(CORS) {
                        anyHost()
                        allowHeader(HttpHeaders.ContentType)
                    }
                    install(ContentNegotiation) {
                        json(json)
                    }
                    install(SSE)

                    routing {
                        get("/") {
                            call.respondText("BundleTool MCP Server is Running!")
                        }

                        sse("/mcp/sse") {
                            logListener?.invoke("Client connected to SSE endpoint.")
                            send(ServerSentEvent(event = "endpoint", data = "/mcp/message"))
                            
                            try {
                                for (msg in sessionChannel) {
                                    send(ServerSentEvent(event = "message", data = msg))
                                }
                            } catch (e: Exception) {
                                logListener?.invoke("SSE connection closed: ${e.message}")
                            }
                        }

                        post("/mcp/message") {
                            val rawRequest = call.receiveText()
                            logListener?.invoke("← Received: $rawRequest")
                            
                            CoroutineScope(Dispatchers.IO).launch {
                                try {
                                    val request = json.decodeFromString<JsonRpcRequest>(rawRequest)
                                    val router = McpRouter(handler, json, logListener)
                                    val response = router.routeRequest(request)
                                    if (response.isNotEmpty()) {
                                        logListener?.invoke("→ Sending: $response")
                                        sessionChannel.send(response)
                                    }
                                } catch (e: Exception) {
                                    logListener?.invoke("Error parsing request: ${e.message}")
                                }
                            }
                            call.respond(HttpStatusCode.Accepted)
                        }
                    }
                }.start(wait = true)
            } catch (e: Exception) {
                logListener?.invoke("Failed to start MCP Server: ${e.message}")
            }
        }
    }

    fun stop() {
        engine?.stop(1000, 2000)
        engine = null
        serverJob?.cancel()
        serverJob = null
        logListener?.invoke("MCP Server stopped.")
    }
}
