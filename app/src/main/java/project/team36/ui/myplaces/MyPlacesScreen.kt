package project.team36.ui.myplaces

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.navigation.NavController
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import project.team36.model.location.SavedLocation
import project.team36.ui.map.SavedPlacesViewModel
import project.team36.ui.navigation.BottomNavBar

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MyPlacesScreen (
    savedPlacesViewModel: SavedPlacesViewModel,
    onLocationCardClick: (SavedLocation) -> Unit,
    onAddPlaceClick: () -> Unit,
    navController: NavController,

) {
    val savedLocations by savedPlacesViewModel.savedLocations.collectAsState()
    val selectedLocation by savedPlacesViewModel.selectedLocation.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Mine steder",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color(0xFF2E7D32)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFE8F5E2)
                )
            )
        },

        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { onAddPlaceClick() },
                icon = { Icon(Icons.Default.Add, contentDescription = "Nytt sted") },
                text = { Text("Nytt sted") },
                containerColor = Color(0xFF2D5A27),
                contentColor = Color.White,
                shape = RoundedCornerShape(50)
            )
        },
            bottomBar = { BottomNavBar(navController = navController, savedPlacesViewModel)}

    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFE8F5E2))
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                if (savedLocations.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Ingen steder lagt til enda",
                            color = Color.Gray
                        )
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(savedLocations) { location ->
                            LocationCard(
                                location = location,
                                onClick = {
                                    savedPlacesViewModel.selectLocation(location)
                                    onLocationCardClick(location)
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

// Card for display av stedsnavn i MyPlaces
@Composable
fun LocationCard (
    location: SavedLocation,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "\uD83C\uDFE0", // hus-emoji
                fontSize = 36.sp,
                modifier = Modifier.padding(end = 16.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = location.name ?: "Ukjent sted",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
                Text(
                    text = location.address ?: "Ingen adress funnet",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }
            Icon(
                imageVector = Icons.Default.ArrowForwardIos,
                contentDescription = "Gå til $location.name",
                tint = Color(0xFF2E7D32)
            )
        }
    }
}

// Preview - slett før innlevering?
@Preview(showBackground = true)
@Composable
fun MyPlacesScreenPreview() {
    val fakeLocations = listOf(
        SavedLocation(id = 1, name = "Hjemme", lat = 59.9, lon = 10.7, address = "Gaustadalléen 2", zone = 0),
        SavedLocation(id = 2, name = "Jobb", lat = 59.91, lon = 10.75, address = "Karl Johans gate 1", zone = 0)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE8F5E2))
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = "Mine steder",
                fontSize = 28.sp,
                fontWeight = FontWeight.Normal,
                color = Color(0xFF2E7D32),
                modifier = Modifier.padding(vertical = 16.dp)
            )
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(fakeLocations) { location ->
                    LocationCard(location = location, onClick = {})
                }
            }
        }

        ExtendedFloatingActionButton(
            onClick = {},
            icon = { Icon(Icons.Default.Add, contentDescription = "Nytt sted") },
            text = { Text("Nytt sted") },
            containerColor = Color(0xFF2D5A27),
            contentColor = Color.White,
            shape = RoundedCornerShape(50),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 16.dp)
        )
    }
}