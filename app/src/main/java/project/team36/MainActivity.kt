package project.team36

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.navigation.compose.rememberNavController
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import project.team36.ui.myplants.MyPlantsScreen
import project.team36.ui.findplants.FindPlantsScreen
import project.team36.ui.findplants.FindPlantsViewModel
import project.team36.data.ai.ClaudeApi
import project.team36.data.ai.HybridAiRepository
import project.team36.data.klima.MetWeatherApi
import project.team36.data.plant.PlantDataSource
import project.team36.data.plant.PlantRepository
import project.team36.ui.map.MapScreen
import project.team36.ui.map.MapViewModel
import project.team36.ui.map.SavedPlacesViewModel
import project.team36.ui.navigation.AppNavHost
//import project.team36.ui.plant.PlantSwipeViewModel
import project.team36.ui.theme.Team36Theme
//import project.team36.ui.plant.PlantSwipeScreen

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

        val dataSource = PlantDataSource(this)
        val plantRepository = PlantRepository(dataSource)

        val contentType = "application/json".toMediaType()
        val jsonConfig = Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
            coerceInputValues = true
        }

        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()

        val claudeRetrofit = Retrofit.Builder()
            .baseUrl("https://api.anthropic.com/")
            .client(okHttpClient)
            .addConverterFactory(jsonConfig.asConverterFactory(contentType))
            .build()
        val claudeApi = claudeRetrofit.create(ClaudeApi::class.java)

        val metRetrofit = Retrofit.Builder()
            .baseUrl("https://api.met.no/")
            .build()
        val metWeatherApi = metRetrofit.create(MetWeatherApi::class.java)

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

                val navController = rememberNavController()

                // Initialiserer ViewModels
                val savedPlacesViewModel: SavedPlacesViewModel = viewModel()
                val findPlantsViewModel: FindPlantsViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            return FindPlantsViewModel(plantRepository) as T
                        }
                    }
                )
                //val swipeViewModel = remember { PlantSwipeViewModel(plantRepository) }
                val mapViewModel: MapViewModel = viewModel(
                    factory = MapViewModelFactory(hybridAiRepository)
                )

                AppNavHost(
                    navController = navController,
                    mapViewModel = mapViewModel,
                    findPlantsViewModel = findPlantsViewModel,
                    savedPlacesViewModel = savedPlacesViewModel
                )
            }
        }
    }
}