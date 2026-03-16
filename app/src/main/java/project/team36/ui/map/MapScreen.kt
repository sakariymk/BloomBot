package project.team36.ui.map

import android.Manifest
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mapbox.geojson.Point
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.plugin.locationcomponent.createDefault2DPuck
import com.mapbox.maps.plugin.locationcomponent.location

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MapScreen(
    modifier: Modifier = Modifier,
    viewModel: MapViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

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


    Box(modifier = modifier.fillMaxSize()) {
        MapboxMap(
            modifier = modifier.fillMaxSize(),
            mapViewportState = mapViewportState
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
                onSuggestionClicked = { viewModel.onSuggestionClicked(it) },
                modifier = Modifier
                    .padding( start = 16.dp, end = 16.dp)
                    .wrapContentHeight()
            )
        }


    }
}
