package project.team36.ui.map

import PlaceInfoCard
import android.Manifest
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
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.annotation.generated.PointAnnotation
import com.mapbox.maps.extension.compose.annotation.rememberIconImage
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
    val savedPlaces by savedPlacesViewModel.savedPlaces.collectAsStateWithLifecycle() // NYTT: hent savedPlaces herfra

    //få lokasjon permissions
    val locationPermissionState = rememberPermissionState(
        Manifest.permission.ACCESS_FINE_LOCATION
    )
    LaunchedEffect(Unit) {
        locationPermissionState.launchPermissionRequest()
    }

    val mapViewportState = rememberMapViewportState {
        setCameraOptions {
            zoom(uiState.zoom)
            center(Point.fromLngLat(uiState.centerLng, uiState.centerLat))
            pitch(0.0)
            bearing(0.0)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kart(?)") }
            )
        },
        bottomBar = {
            NavigationBar{
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate("map")},
                    icon = { }, //fyller ut med icon senere!
                    label = { Text("Kart")}
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate("myPlants") },
                    icon = { }, //fyller ut med icon senere!
                    label = { Text("Mine planter")}
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate("findPlants") },
                    icon = { }, //fyller ut med icon senere!
                    label = { Text("Oppdag planter")}
                )
            }
        }
    ) {
            innerPadding ->
        Box(modifier = modifier
            .fillMaxSize()
            .padding(innerPadding)) {
            //dette er selve kartet
            MapboxMap(
                modifier = modifier.fillMaxSize(),
                mapViewportState = mapViewportState,
                scaleBar = {} //disabler den scale bar
            ) {
                if (locationPermissionState.status.isGranted) {
                    @Suppress("COMPOSE_APPLIER_CALL_MISMATCH")
                    MapEffect(Unit) { mapView ->
                        mapView.location.updateSettings {
                            locationPuck = createDefault2DPuck(withBearing = true)
                            enabled = true
                            puckBearingEnabled = false
                        }
                        mapViewportState.transitionToFollowPuckState()
                    }
                }

                //viser pin på valgt lokasjon
                uiState.selectedResult?.coordinate?.let { coord ->
                    val marker = rememberIconImage(
                        key = R.drawable.plant_map_marker,
                        painter = painterResource(id = R.drawable.plant_map_marker)
                    )
                    PointAnnotation(point = coord) {
                        iconImage = marker
                    }
                }

                //Går gjennom savedPlaces lista og putter pins på kartet
                savedPlaces.forEach { place ->

                    place.result.coordinate.let { coord ->
                        val savedMarker = rememberIconImage(
                            key = R.drawable.plant_map_marker,
                            painter = painterResource(id = R.drawable.plant_map_marker)
                        )
                        PointAnnotation(point = coord) {
                            iconImage = savedMarker
                            interactionsState.onClicked {
                                viewModel.onSavedPlaceClicked(place)
                                true
                            }
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .align (Alignment.TopCenter)
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


            //Boks som spør om brukeren vil legge til lokasjonen i mine steder
            // kun vises showSaveDialog er true, fra viewmodel
            if (uiState.showSaveDialog) {
                AlertDialog(
                    onDismissRequest = { viewModel.dismissSaveDialog() },
                    title = { Text("Lagre sted?") },

                    confirmButton = { TextButton(onClick = {viewModel.confirmSavePlace(savedPlacesViewModel)}) { Text("Ja") } },
                    dismissButton = {TextButton(onClick = {viewModel.dismissSaveDialog()}) { Text("Nei") }}
                )
            }
        }


        //viser planteinfocard når pin blir trykket på av brukeren
        uiState.selectedSavedPlace?.let { place ->
            PlaceInfoCard(
                place = place,
                onDismiss = { viewModel.dismissPlaceInfo() },
                onEdit = { newName ->
                    savedPlacesViewModel.updatePlace(place, newName)
                    viewModel.dismissPlaceInfo()
                },
                onDelete = {
                    savedPlacesViewModel.removePlace(place)
                    viewModel.dismissPlaceInfo()
                },
                navController = navController as NavHostController
            )
        }
    }
}