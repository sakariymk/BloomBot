package project.team36.data.mcp

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.sse.SSE
import io.modelcontextprotocol.kotlin.sdk.client.Client
import io.modelcontextprotocol.kotlin.sdk.client.StreamableHttpClientTransport
import io.modelcontextprotocol.kotlin.sdk.types.CallToolRequest
import io.modelcontextprotocol.kotlin.sdk.types.CallToolRequestParams
import io.modelcontextprotocol.kotlin.sdk.types.Implementation
import io.modelcontextprotocol.kotlin.sdk.types.TextContent
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

//MCP-client oppsett fra https://github.com/modelcontextprotocol/kotlin-sdk
class MetMcpClient {
    private val httpClient = HttpClient(Android) { install(SSE) }
    private var isConnected = false
    private val connectMutex = Mutex()

    private val client = Client(
        clientInfo = Implementation(
            name = "plant-app",
            version = "1.0.0"
        )
    )

    suspend fun connect() {
        connectMutex.withLock {
            if (isConnected) return
            try {
                val transport = StreamableHttpClientTransport(
                    client = httpClient,
                    url = "https://webapi.met.no/mcp-server"
                )
                client.connect(transport)
                isConnected = true
                Log.d("MCP_CAPS", "Tilkoblet! Capabilities: ${client.serverCapabilities}")
                val tools = client.listTools()
                Log.d("MCP_TOOLS", "Verktøy: ${tools?.tools?.map { it.name }}")
            } catch (e: Exception) {
                isConnected = false
                Log.e("MCP_CAPS", "connect() feilet: ${e.message}")
                throw Exception("Kunne ikke koble til MET MCP-server: ${e.message}")
            }
        }
    }

    //Hente forecast som tekst
    suspend fun getForecast(latitude: Double, longitude: Double): String {
        val result = client.callTool(
            CallToolRequest(
                params = CallToolRequestParams(
                    name = "forecast",
                    arguments = buildJsonObject {
                        put("latitude", JsonPrimitive(latitude))
                        put("longitude", JsonPrimitive(longitude))
                    }
                )
            )
        )
        return (result.content?.firstOrNull() as? TextContent)?.text ?: ""
    }

    //Hente alerts som tekst
    suspend fun getAlerts(): String {
        connect()
        val alertResult = client.callTool(
            CallToolRequest(
                params = CallToolRequestParams(
                    name = "alerts",
                    arguments = buildJsonObject {
                        put("geographic_domain", JsonPrimitive("land"))
                        put("language", JsonPrimitive("no"))
                    }
                )
            )
        )
        return (alertResult.content?.firstOrNull() as? TextContent)?.text ?: ""
    }
}