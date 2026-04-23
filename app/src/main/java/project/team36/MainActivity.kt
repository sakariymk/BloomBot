package project.team36

import project.team36.ui.navigation.AppNavHost
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import project.team36.ui.findplants.FindPlantsViewModel
import project.team36.data.ai.ClaudeApi
import project.team36.data.ai.HybridAiRepository
import project.team36.data.klima.MetWeatherApi
import project.team36.data.klima.frost.FrostRepository
import project.team36.data.local.AppDatabase
import project.team36.data.local.LocationRepository
import project.team36.data.mcp.MetMcpClient
import project.team36.data.mcp.MetWeatherRepository
import project.team36.data.plant.PlantDataSource
import project.team36.data.plant.PlantRepository
import project.team36.ui.landingpage.LandingPageViewModel
import project.team36.ui.landingpage.LandingPageViewModelFactory
import project.team36.ui.map.MapViewModel
import project.team36.ui.map.SavedPlacesViewModel
import project.team36.ui.map.SavedPlacesViewModelFactory
import project.team36.ui.myplants.MyPlantsViewModel
import project.team36.ui.plant.PlantDetailsViewModel
import project.team36.ui.theme.Team36Theme


class MapViewModelFactory(
    private val repository: HybridAiRepository,
    private val weatherRepository: MetWeatherRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MapViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MapViewModel(repository, weatherRepository) as T
        }
        throw IllegalArgumentException("Ukjent ViewModel klasse")
    }
}

class FindPlantsViewModelFactory(
    private val plantRepository: PlantRepository,
    private val locationRepository: LocationRepository,
    private val savedPlacesViewModel: SavedPlacesViewModel
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FindPlantsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FindPlantsViewModel(plantRepository, locationRepository, savedPlacesViewModel) as T
        }
        throw IllegalArgumentException("Ukjent ViewModel klasse")
    }
}

class MyPlantsViewModelFactory(
    private val repository: LocationRepository,
    private val savedPlacesViewModel: SavedPlacesViewModel,
    private val frostRepository: FrostRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MyPlantsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MyPlantsViewModel(repository, savedPlacesViewModel, frostRepository) as T
        }
        throw IllegalArgumentException("Ukjent ViewModel klasse")
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val dataSource = PlantDataSource(this)
        val db = AppDatabase.getDatabase(this)
        val locationRepository = LocationRepository(db.savedLocationDao(), db.savedPlantDao())
        val plantRepository = PlantRepository(dataSource, locationRepository)
        val frostRepository = FrostRepository()



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
        val metWeatherApi = metRetrofit.create(MetWeatherApi::class.java) //TODO: se om dette trengs?

        val claudeKey = "***FJERNET***"
        val frostClientId = "08b40da1-9456-4846-988a-e5283cf731cb"

        val mcpClient = MetMcpClient()

        //thrower exception hvis det ikke er internett tilgang, så appen ikke krasjer
        try {
            runBlocking { mcpClient.connect() }
        } catch (e: Exception) {
            Log.e("MCP", "Could not connect: ${e.message}")

        }

        val metWeatherRepository = MetWeatherRepository(mcpClient)

        val hybridAiRepository = HybridAiRepository(
            claudeApi = claudeApi,
            mcpClient = mcpClient,
            // <------ ENDRING: Vi sender inn locationRepository (lokal database) i stedet for dataSource (CSV)
            plantDataSource = locationRepository,
            frostClientId = frostClientId,
            claudeApiKey = claudeKey
        )

        setContent {
            Team36Theme {
                val navController = rememberNavController()

                val landingPageViewModel: LandingPageViewModel = viewModel(
                    factory = LandingPageViewModelFactory(metWeatherRepository)
                )

                val savedPlacesViewModel: SavedPlacesViewModel = viewModel(
                    factory = SavedPlacesViewModelFactory(locationRepository)
                )
                val findPlantsViewModel: FindPlantsViewModel = viewModel(
                    factory = FindPlantsViewModelFactory(plantRepository, locationRepository, savedPlacesViewModel)
                )

                val myPlantsViewModel: MyPlantsViewModel = viewModel(
                    factory = MyPlantsViewModelFactory(locationRepository, savedPlacesViewModel, frostRepository)
                )

                val mapViewModel: MapViewModel = viewModel(
                    factory = MapViewModelFactory(hybridAiRepository, metWeatherRepository)
                )

                // plantDetailsScreen ViewModel --> sjekk om den skal ligge her
                val plantDetailsViewModel: PlantDetailsViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            @Suppress("UNCHECKED_CAST")
                            return PlantDetailsViewModel(plantRepository, locationRepository) as T
                        }
                    }
                )

                AppNavHost(
                    navController = navController,
                    mapViewModel = mapViewModel,
                    findPlantsViewModel = findPlantsViewModel,
                    myPlantsViewModel = myPlantsViewModel,
                    savedPlacesViewModel = savedPlacesViewModel,
                    plantDetailsViewModel = plantDetailsViewModel,
                    landingPageViewModel = landingPageViewModel
                )
            }
        }
    }
}