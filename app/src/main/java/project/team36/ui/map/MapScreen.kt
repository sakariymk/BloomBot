package project.team36.ui.map

import android.Manifest
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.mapbox.maps.plugin.locationcomponent.createDefault2DPuck
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.extension.style.layers.addLayer
import com.mapbox.maps.extension.style.layers.generated.fillLayer
import com.mapbox.maps.extension.style.sources.addSource
import com.mapbox.maps.extension.style.sources.generated.vectorSource
import project.team36.R
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import project.team36.ui.navigation.BottomNavBar
import androidx.compose.ui.window.Dialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.saveable.rememberSaveable

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    modifier: Modifier = Modifier,
    viewModel: MapViewModel = viewModel(),
    locationsViewModel: LocationsViewModel,
    navController: NavController,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val savedLocations by locationsViewModel.savedLocations.collectAsStateWithLifecycle()
    val mapViewportState = viewModel.mapViewportState
    val context = LocalContext.current


    //henter live lokasjon
    val locationPermissionState = rememberPermissionState(
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    //hvis ingen steder er lagret enda, viser en liten velkomstdialog
    var showLocationChoiceDialog by rememberSaveable(savedLocations.isEmpty()) {
        mutableStateOf(savedLocations.isEmpty())
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Legg til hage",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {navController.popBackStack()}) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Lukk skjermen",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                windowInsets = WindowInsets(0) //gjør at appen ikke legger til mye mellomrom på toppen av skjermen
            )
        },
        bottomBar = {
            BottomNavBar(navController = navController, locationsViewModel)
        }
    ) { innerPadding ->
        Box(modifier = modifier
            .fillMaxSize()
            .padding(innerPadding)) {

            MapboxMap(
                modifier = Modifier.fillMaxSize(),
                mapViewportState = mapViewportState,
                scaleBar = {}
            ) {
                if (locationPermissionState.status.isGranted) {
                    @Suppress("COMPOSE_APPLIER_CALL_MISMATCH")
                    MapEffect(Unit) { mapView ->
                        viewModel.onMapReady(mapView.mapboxMap)

                        mapView.mapboxMap.getStyle { style ->
                            style.addSource(vectorSource(ClimateZoneConfig.SOURCE_ID) {
                                tiles(listOf(ClimateZoneConfig.TILE_URL))
                            })
                            //legger til klimasonekart som en usynlig overlay, gjørat man kan hente ut data om klimasone på kartet
                            style.addLayer(fillLayer(ClimateZoneConfig.LAYER_ID, ClimateZoneConfig.SOURCE_ID) {
                                sourceLayer(ClimateZoneConfig.SOURCE_LAYER)
                                fillOpacity(literal(0.0))
                            })
                        }

                        if (locationPermissionState.status.isGranted) {
                            mapView.location.updateSettings {
                                locationPuck = createDefault2DPuck(withBearing = true)
                                enabled = true
                                puckBearingEnabled = false
                            }
                            mapView.location.addOnIndicatorPositionChangedListener { point ->
                                viewModel.updateUserPosition(point.latitude(), point.longitude())
                            }
                            viewModel.onLocationReady()
                        }
                    }
                }

                // Viser midlertidig markør for valgt søkeresultat
                uiState.selectedResult?.coordinate?.let { coord ->
                    val marker = rememberIconImage(
                        key = R.drawable.plant_map_marker,
                        painter = painterResource(id = R.drawable.plant_map_marker)
                    )
                    PointAnnotation(point = coord) {
                        iconImage = marker
                    }
                }

                // Viser alle lagrede steder
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
                    .padding(top = 16.dp)
                    .fillMaxWidth()
            ) {
                SearchBar(
                    query = uiState.searchQuery,
                    onQueryChanged = { viewModel.onSearchQueryChanged(it) },
                    suggestions = uiState.suggestions,
                    onSuggestionClicked = { viewModel.handleSuggestionSelection(it, mapViewportState) },
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .wrapContentHeight()
                )
            }

            if (showLocationChoiceDialog) {
                //dialogboks som popper opp første gangen man lagrer et sted (eller har 0 lagrde steder)
                Dialog(onDismissRequest = { showLocationChoiceDialog = false }) {
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.background),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("🌱", fontSize = 48.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Hvor vil du dyrke?",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Vi bruker posisjonen din til å finne planter som passer der du er",
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center,
                                color = Color.DarkGray
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                "🪴  Anbefalte planter for stedet",
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center
                                )

                            Text("🌡  Lokal temperatur og klima",
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            //knapp for å lagre sted med live lokasjon
                            Button(
                                //hvis man har gitt tillatelse for å bruke lokasjon, så lagres lokasjonen
                                onClick = {
                                    if (locationPermissionState.status.isGranted) {
                                        viewModel.requestLiveLocationSave()
                                        showLocationChoiceDialog = false
                                        //hvis man ikke har gitt lokasjonstillatelse, så trigges den på nytt når man trykker på knappen
                                    } else {
                                        locationPermissionState.launchPermissionRequest()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(50.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
                            ) {
                                Text("Bruk min posisjon nå", color = Color.White, modifier = Modifier.padding(vertical = 4.dp))
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            //knapp som lukker dialogboksen slik at brukeren får søke opp adresse
                            OutlinedButton(
                                onClick = { showLocationChoiceDialog = false },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(50.dp)
                            ) {
                                Text("Søk etter en adresse", color = Color.DarkGray, modifier = Modifier.padding(vertical = 4.dp))
                            }
                        }
                    }
                }
            }

            //Dialogboks som lar brukeren bekrefte om de har lyst til å lagre stedet
            if (uiState.showLiveLocationConfirm) {
                AlertDialog(
                    onDismissRequest = { viewModel.dismissLiveLocationConfirm() },
                    title = { Text("Lagre sted?") },
                    containerColor = MaterialTheme.colorScheme.surface,
                    text = { Text("Vil du lagre din nåværende posisjon?") },
                    confirmButton = {
                        OutlinedButton(
                            enabled = !uiState.isSaving,
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            //når brukern lagrer stedet, blir man navigert til stedet
                            onClick = {
                                viewModel.confirmSaveLiveLocation(context) { navController.navigate("myPlaces") }
                            }
                        ) {
                            if (uiState.isSaving) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            } else {
                                Text("Ja")
                            }
                        }
                    },
                    dismissButton = {
                        OutlinedButton(
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                            onClick = { viewModel.dismissLiveLocationConfirm() }
                        ) { Text("Nei") }
                    }
                )
            }

            if (uiState.showSaveDialog) {
                AlertDialog(
                    onDismissRequest = { viewModel.dismissSaveDialog() },
                    title = { Text("Lagre hage?") },
                    containerColor = MaterialTheme.colorScheme.surface,
                    confirmButton = {
                        OutlinedButton(
                            enabled = true,
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            onClick = {
                                if (!uiState.isSaving) { // ignorer klikk hvis allerede lagrer
                                    viewModel.confirmSavePlace { navController.navigate("myPlaces") } //bruker blir navigert til stedet når man har lagra
                                }
                            }
                        ) {
                            if (uiState.isSaving) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )

                            } else {
                                Text("Ja")
                            }
                        }
                    },
                    dismissButton = {
                        OutlinedButton(
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                            onClick = { viewModel.dismissSaveDialog() }
                        ) { Text("Nei") }
                    }
                )
            }
        }

        //Info-kort for lagret sted
        uiState.selectedSavedLocation?.let { location ->
            PlaceInfoCard(
                location = location,
                onDismiss = { viewModel.dismissPlaceInfo() },
                navController = navController as NavHostController
            )
        }
    }
}