package project.team36.ui.map

import android.Manifest
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mapbox.geojson.Point
import com.mapbox.maps.extension.compose.MapboxMap
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.annotation.generated.PointAnnotation
import com.mapbox.maps.extension.compose.annotation.rememberIconImage
import com.mapbox.maps.extension.style.expressions.dsl.generated.literal
import com.mapbox.maps.extension.style.layers.addLayer
import com.mapbox.maps.extension.style.layers.generated.fillLayer
import com.mapbox.maps.extension.style.sources.addSource
import com.mapbox.maps.extension.style.sources.generated.vectorSource
import com.mapbox.maps.plugin.locationcomponent.createDefault2DPuck
import com.mapbox.maps.plugin.locationcomponent.location
import project.team36.R

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    modifier: Modifier = Modifier,
    viewModel: MapViewModel = viewModel(),
    savedPlacesViewModel: SavedPlacesViewModel,
    navController: NavController
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val savedLocations by savedPlacesViewModel.savedLocations.collectAsStateWithLifecycle()
    val selectedLocation by savedPlacesViewModel.selectedLocation.collectAsStateWithLifecycle()
    val mapViewportState = viewModel.mapViewportState

    val locationPermissionState = rememberPermissionState(
        Manifest.permission.ACCESS_FINE_LOCATION
    )
    LaunchedEffect(Unit) {
        locationPermissionState.launchPermissionRequest()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hjemmeskjerm") }
            )
        },
        bottomBar = {
            NavigationBar{
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate("map")},
                    icon = { },
                    label = { Text("Kart")}
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate("myPlaces") },
                    icon = { },
                    label = { Text("Mine steder")}
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate("findPlants/${selectedLocation?.name ?: "ukjent"}") },
                    icon = { }, //fyller ut med icon senere!
                    label = { Text("Oppdag planter")}
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = modifier
            .fillMaxSize()
            .padding(innerPadding)) {

            MapboxMap(
                modifier = modifier.fillMaxSize(),
                mapViewportState = mapViewportState,
                scaleBar = {}
            ) {
                if (locationPermissionState.status.isGranted) {
                    @Suppress("COMPOSE_APPLIER_CALL_MISMATCH")
                    MapEffect(Unit) { mapView ->
                        mapView.location.updateSettings {
                            locationPuck = createDefault2DPuck(withBearing = true)
                            enabled = true
                            puckBearingEnabled = false
                        }
                        viewModel.onLocationReady()
                        viewModel.onMapReady(mapView.mapboxMap)

                        mapView.mapboxMap.getStyle { style ->
                            style.addSource(vectorSource(ClimateZoneConfig.SOURCE_ID) {
                                tiles(listOf(ClimateZoneConfig.TILE_URL))
                            })
                            style.addLayer(fillLayer(ClimateZoneConfig.LAYER_ID, ClimateZoneConfig.SOURCE_ID) {
                                sourceLayer(ClimateZoneConfig.SOURCE_LAYER)
                                fillOpacity(literal(0.0))
                            })
                            Log.d("ZONE_TEST", "Klimasone source og lag lagt til!")
                        }
                    }
                }

                uiState.selectedResult?.coordinate?.let { coord ->
                    val marker = rememberIconImage(
                        key = R.drawable.plant_map_marker,
                        painter = painterResource(id = R.drawable.plant_map_marker)
                    )
                    PointAnnotation(point = coord) {
                        iconImage = marker
                    }
                }

                savedLocations.forEach { location ->
                    val lat = location.lat ?: return@forEach
                    val lon = location.lon ?: return@forEach
                    val coord = Point.fromLngLat(lon, lat)
                    val savedMarker = rememberIconImage(
                        key = R.drawable.plant_map_marker,
                        painter = painterResource(id = R.drawable.plant_map_marker)
                    )
                    PointAnnotation(point = coord) {
                        iconImage = savedMarker
                        interactionsState.onClicked {
                            viewModel.onSavedLocationClicked(location)
                            true
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 48.dp)
                    .fillMaxWidth()
            ) {
                SearchBar(
                    query = uiState.searchQuery,
                    onQueryChanged = { viewModel.onSearchQueryChanged(it) },
                    suggestions = uiState.suggestions,
                    onSuggestionClicked = { viewModel.handleSuggestionSelection(it, mapViewportState) },
                    modifier = Modifier
                        .padding( start = 16.dp, end = 16.dp)
                        .wrapContentHeight()
                )
            }

            if (uiState.showSaveDialog) {
                AlertDialog(
                    onDismissRequest = { viewModel.dismissSaveDialog() },
                    title = { Text("Lagre sted?") },

                    confirmButton = {
                        TextButton(
                            onClick = {
                                viewModel.confirmSavePlace(savedPlacesViewModel)
                                viewModel.dismissSaveDialog()
                            }) { Text("Ja") } },
                    dismissButton = {TextButton(onClick = {viewModel.dismissSaveDialog()}) { Text("Nei") }}
                )
            }
        }

        uiState.selectedSavedLocation?.let { location ->
            PlaceInfoCard(
                location = location,
                // <------ ENDRING: Passerer hele lokasjonen inn for begge knappene
                onGetAiAdvice = { loc -> viewModel.fetchAiAdvice(loc) },
                onGetFutureAiAdvice = { loc -> viewModel.fetchFutureAiAdvice(loc) },
                aiResponse = uiState.aiResponse,
                isAiLoading = uiState.isAiLoading,
                isFutureAiLoading = uiState.isFutureAiLoading,
                // <------ ENDRING: Sender med de tre nye test-parameterne
                onGetRawApiData = { loc -> viewModel.fetchRawApiData(loc) },
                rawApiData = uiState.rawApiData,
                onDismissRawApi = { viewModel.dismissRawApiData() },

                onDismissAi = { viewModel.dismissAiAdvice() },
                onDismiss = { viewModel.dismissPlaceInfo() },
                onEdit = { newName ->
                    savedPlacesViewModel.updateLocation(location.copy (name = newName))
                    viewModel.dismissPlaceInfo()
                },
                onDelete = {
                    savedPlacesViewModel.deleteLocation(location)
                    viewModel.dismissPlaceInfo()
                },
                navController = navController as NavHostController
            )
        }
    }
}