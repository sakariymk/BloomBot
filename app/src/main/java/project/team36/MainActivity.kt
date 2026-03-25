package project.team36

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel

// --- IMPORTER FOR NETTVERK ---
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit

import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

// --- DINE KLASSER ---
import project.team36.data.ai.ClaudeApi
import project.team36.data.ai.HybridAiRepository
import project.team36.data.klima.MetWeatherApi
import project.team36.data.plante.PlantDataSource
import project.team36.data.plante.PlantRepository
import project.team36.ui.map.MapScreen
import project.team36.ui.map.MapViewModel
import project.team36.ui.plante.PlantSwipeScreen
import project.team36.ui.plante.PlantSwipeViewModel
import project.team36.ui.theme.Team36Theme

// Fabrikk for å dytte Repository inn i ViewModel
class MapViewModelFactory(
    private val repository: HybridAiRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MapViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MapViewModel(repository) as T
        }
        throw IllegalArgumentException("Ukjent ViewModel klasse")
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Datakilder
        val dataSource = PlantDataSource(this)
        val plantRepository = PlantRepository(dataSource)

        // 2. Oppsett av JSON-konvertering (viktig for Claude)
        val contentType = "application/json".toMediaType()
        val jsonConfig = Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
            coerceInputValues = true
        }

        // 3. Retrofit for Claude (Anthropic)
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()

// 2. Bygg Retrofit for Claude med den nye klienten
        val claudeRetrofit = Retrofit.Builder()
            .baseUrl("https://api.anthropic.com/")
            .client(okHttpClient) // <-- DENNE LINJEN ER NY OG VIKTIG
            .addConverterFactory(jsonConfig.asConverterFactory(contentType))
            .build()

        val claudeApi = claudeRetrofit.create(ClaudeApi::class.java)

        // 4. Retrofit for Været (MET)
        val metRetrofit = Retrofit.Builder()
            .baseUrl("https://api.met.no/")
            .build()
        val metWeatherApi = metRetrofit.create(MetWeatherApi::class.java)

        // 5. NØKLER OG REPOSITORY
        // Bytt ut med dine faktiske nøkler!
        val claudeKey = "***FJERNET***"
        val frostClientId = "08b40da1-9456-4846-988a-e5283cf731cb"

        val hybridAiRepository = HybridAiRepository(
            claudeApi = claudeApi,
            metWeatherApi = metWeatherApi,
            plantDataSource = dataSource,
            frostClientId = frostClientId,
            claudeApiKey = claudeKey
        )

        setContent {
            Team36Theme {
                var currentScreen by remember { mutableStateOf("map") }

                val swipeViewModel = remember { PlantSwipeViewModel(plantRepository) }

                // Bruker fabrikken for å sikre at ViewModel får tilgang til AI-motoren
                val mapViewModel: MapViewModel = viewModel(
                    factory = MapViewModelFactory(hybridAiRepository)
                )

                Scaffold(
                    bottomBar = {
                        NavigationBar {
                            NavigationBarItem(
                                selected = currentScreen == "map",
                                onClick = { currentScreen = "map" },
                                label = { Text("Kart") },
                                icon = { Icon(Icons.Default.Place, contentDescription = "Kart") }
                            )
                            NavigationBarItem(
                                selected = currentScreen == "swipe",
                                onClick = { currentScreen = "swipe" },
                                label = { Text("Finn Planter") },
                                icon = { Icon(Icons.Default.Home, contentDescription = "Sveip") }
                            )
                        }
                    }
                ) { innerPadding ->
                    Surface(
                        modifier = Modifier.padding(innerPadding),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        when (currentScreen) {
                            "map" -> MapScreen(modifier = Modifier, viewModel = mapViewModel)
                            "swipe" -> PlantSwipeScreen(viewModel = swipeViewModel)
                        }
                    }
                }
            }
        }
    }
}