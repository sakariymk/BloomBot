package project.team36

import project.team36.ui.navigation.AppNavHost
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import project.team36.data.klima.frost.FrostRepository
import project.team36.data.local.AppDatabase
import project.team36.data.local.LocationRepository
import project.team36.data.mcp.MetMcpClient
import project.team36.data.mcp.MetWeatherRepository
import project.team36.data.network.ConnectivityObserver
import project.team36.data.plant.PlantDataSource
import project.team36.data.plant.PlantRepository
import project.team36.ui.findplants.FindPlantsViewModelFactory
import project.team36.ui.landingpage.LandingPageViewModel
import project.team36.ui.landingpage.LandingPageViewModelFactory
import project.team36.ui.map.MapViewModel
import project.team36.ui.map.MapViewModelFactory
import project.team36.ui.map.SavedPlacesViewModel
import project.team36.ui.map.SavedPlacesViewModelFactory
import project.team36.ui.myplants.MyPlantsViewModel
import project.team36.ui.plant.PlantDetailsViewModel
import project.team36.ui.plant.PlantDetailsViewModelFactory
import project.team36.ui.theme.Team36Theme
import androidx.compose.runtime.DisposableEffect
import androidx.activity.enableEdgeToEdge
import androidx.activity.SystemBarStyle
import androidx.core.view.WindowCompat
import project.team36.ui.myplants.MyPlantsViewModelFactory


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //gjør statusbaren gjennomsiktig i appen
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                scrim = android.graphics.Color.TRANSPARENT,
                darkScrim = android.graphics.Color.TRANSPARENT
            ),
            //setter fargen til telefonens navigasjonsknapper
            navigationBarStyle = SystemBarStyle.light(
                scrim = android.graphics.Color.TRANSPARENT,
                darkScrim = android.graphics.Color.TRANSPARENT
            )
        )

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

        val claudeKey = "***FJERNET***"

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
            claudeApiKey = claudeKey
        )

        setContent {
            Team36Theme {
                val navController = rememberNavController()

                val connectivityObserver = remember { ConnectivityObserver(applicationContext) }

                DisposableEffect(Unit) {
                    connectivityObserver.register()
                    onDispose { connectivityObserver.unregister() }
                }

                val isConnected by connectivityObserver.isConnected.collectAsState()

                val snackbarHostState = remember { SnackbarHostState() }
                LaunchedEffect(isConnected) {
                    if (!isConnected) {
                        snackbarHostState.showSnackbar(
                            message = "Ingen internettforbindelse",
                            duration = SnackbarDuration.Indefinite
                        )
                    }
                }

                val landingPageViewModel: LandingPageViewModel = viewModel(
                    factory = LandingPageViewModelFactory(metWeatherRepository, hybridAiRepository)
                )
                val savedPlacesViewModel: SavedPlacesViewModel = viewModel(
                    factory = SavedPlacesViewModelFactory(locationRepository)
                )
                val findPlantsViewModel: FindPlantsViewModel = viewModel(
                    factory = FindPlantsViewModelFactory(
                        plantRepository,
                        locationRepository,
                        savedPlacesViewModel
                    )
                )
                val myPlantsViewModel: MyPlantsViewModel = viewModel(
                    factory = MyPlantsViewModelFactory(
                        locationRepository,
                        savedPlacesViewModel,
                        frostRepository
                    )
                )
                val mapViewModelFactory = MapViewModelFactory(metWeatherRepository, locationRepository)

                val plantDetailsViewModel: PlantDetailsViewModel = viewModel(
                    factory = PlantDetailsViewModelFactory(plantRepository, locationRepository)
                )

                AppNavHost(
                    navController = navController,
                    snackbarHostState = snackbarHostState,
                    mapViewModelFactory = mapViewModelFactory,
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