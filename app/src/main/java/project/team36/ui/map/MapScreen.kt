package project.team36.ui.map

import android.Manifest
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.geojson.Polygon
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.annotation.generated.PointAnnotation
import com.mapbox.maps.extension.compose.annotation.rememberIconImage
import com.mapbox.maps.plugin.locationcomponent.createDefault2DPuck
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.extension.style.expressions.dsl.generated.*
import com.mapbox.maps.extension.style.layers.addLayer
import com.mapbox.maps.extension.style.layers.generated.fillLayer
import com.mapbox.maps.extension.style.layers.getLayer
import com.mapbox.maps.extension.style.sources.addSource
import com.mapbox.maps.extension.style.sources.generated.geoJsonSource
import com.mapbox.maps.extension.style.sources.generated.vectorSource
import com.mapbox.maps.extension.style.sources.getSourceAs
import com.mapbox.maps.extension.style.sources.generated.GeoJsonSource
import project.team36.R
import project.team36.data.mcp.WeatherAlert

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
    val mapViewportState = viewModel.mapViewportState
    val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

    LaunchedEffect(Unit) {
        locationPermissionState.launchPermissionRequest()
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = true,
                    onClick = { navController.navigate("map") },
                    icon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                    label = { Text("Kart") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate("myPlants") },
                    icon = { Icon(Icons.Default.List, contentDescription = null) },
                    label = { Text("Mine planter") }
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = modifier.fillMaxSize().padding(innerPadding)) {
            MapboxMap(
                modifier = Modifier.fillMaxSize(),
                mapViewportState = mapViewportState,
                scaleBar = {}
            ) {
                if (locationPermissionState.status.isGranted) {
                    MapEffect(Unit) { mapView ->
                        mapView.location.updateSettings {
                            locationPuck = createDefault2DPuck(withBearing = true)
                            enabled = true
                        }

                        mapView.location.addOnIndicatorPositionChangedListener { point ->
                            viewModel.updateUserPosition(point.latitude(), point.longitude())
                        }

                        viewModel.onMapReady(mapView.mapboxMap)

                        mapView.mapboxMap.getStyle { style ->
                            if (style.getSourceAs<GeoJsonSource>(ClimateZoneConfig.SOURCE_ID) == null) {
                                style.addSource(vectorSource(ClimateZoneConfig.SOURCE_ID) {
                                    tiles(listOf(ClimateZoneConfig.TILE_URL))
                                })
                                style.addLayer(fillLayer(ClimateZoneConfig.LAYER_ID, ClimateZoneConfig.SOURCE_ID) {
                                    sourceLayer(ClimateZoneConfig.SOURCE_LAYER)
                                    fillOpacity(literal(0.0))
                                })
                            }
                        }
                    }
                }

                MapEffect(uiState.locationAlerts) { mapView ->
                    val style = mapView.mapboxMap.getStyle() ?: return@MapEffect
                    val sourceId = "alerts-source"
                    val layerId = "alerts-layer"

                    if (uiState.locationAlerts.isEmpty()) {
                        if (style.getLayer(layerId) != null) {
                            style.removeStyleLayer(layerId)
                        }
                        if (style.getSourceAs<GeoJsonSource>(sourceId) != null) {
                            style.removeStyleSource(sourceId)
                        }
                        return@MapEffect
                    }

                    try {
                        val features = uiState.locationAlerts.flatMap { alert ->
                            alert.polygons.map { poly ->
                                val points = poly.map { Point.fromLngLat(it.second, it.first) }
                                val closedPoints = if (points.isNotEmpty() && points.first() != points.last()) {
                                    points + points.first()
                                } else points
                                Feature.fromGeometry(Polygon.fromLngLats(listOf(closedPoints))).apply {
                                    addStringProperty("severity", alert.severity)
                                }
                            }
                        }

                        val featureCollection = FeatureCollection.fromFeatures(features)
                        val existingSource = style.getSourceAs<GeoJsonSource>(sourceId)

                        if (existingSource != null) {
                            existingSource.featureCollection(featureCollection)
                        } else {
                            style.addSource(geoJsonSource(sourceId) { featureCollection(featureCollection) })
                            if (style.getLayer(layerId) == null) {
                                style.addLayer(fillLayer(layerId, sourceId) {
                                    fillColor(match {
                                        get { literal("severity") }
                                        literal("Moderate"); color(android.graphics.Color.parseColor("#FFD700"))
                                        literal("Severe"); color(android.graphics.Color.parseColor("#FF8C00"))
                                        literal("Extreme"); color(android.graphics.Color.parseColor("#FF0000"))
                                        color(android.graphics.Color.parseColor("#FFD700"))
                                    })
                                    fillOpacity(0.4)
                                    fillOutlineColor(android.graphics.Color.DKGRAY)
                                })
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("MapError", "Tegnefeil: ${e.message}")
                    }
                }

                savedLocations.forEach { location ->
                    val coord = Point.fromLngLat(location.lon ?: 0.0, location.lat ?: 0.0)
                    val marker = rememberIconImage(key = R.drawable.plant_map_marker, painter = painterResource(R.drawable.plant_map_marker))
                    PointAnnotation(point = coord) {
                        iconImage = marker
                        interactionsState.onClicked { viewModel.onSavedLocationClicked(location); true }
                    }
                }
            }

            Column(modifier = Modifier.align(Alignment.TopCenter).padding(top = 16.dp).fillMaxWidth()) {
                SearchBar(
                    query = uiState.searchQuery,
                    onQueryChanged = { viewModel.onSearchQueryChanged(it) },
                    suggestions = uiState.suggestions,
                    onSuggestionClicked = { viewModel.handleSuggestionSelection(it, mapViewportState) },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                if (uiState.locationAlerts.isNotEmpty()) {
                    AlertBanner(uiState.locationAlerts.first())
                }
            }

            if (uiState.showSaveDialog) {
                AlertDialog(
                    onDismissRequest = { viewModel.dismissSaveDialog() },
                    title = { Text("Lagre sted?") },
                    confirmButton = {
                        TextButton(onClick = { viewModel.confirmSavePlace(savedPlacesViewModel) }) { Text("Ja") }
                    },
                    dismissButton = {
                        TextButton(onClick = { viewModel.dismissSaveDialog() }) { Text("Nei") }
                    }
                )
            }
        }

        uiState.selectedSavedLocation?.let { location ->
            PlaceInfoCard(
                location = location,
                onGetAiAdvice = { lat, lon -> viewModel.fetchAiAdvice(lat, lon) },
                aiResponse = uiState.aiResponse,
                isAiLoading = uiState.isAiLoading,
                onDismissAi = { viewModel.dismissAiAdvice() },
                onDismiss = { viewModel.dismissPlaceInfo() },
                onEdit = { newName ->
                    savedPlacesViewModel.updateLocation(location.copy(name = newName))
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

@Composable
fun AlertBanner(alert: WeatherAlert) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(16.dp).heightIn(max = 200.dp),
        color = MaterialTheme.colorScheme.errorContainer,
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 6.dp
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
            Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(text = alert.event, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                Text(text = alert.description, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}