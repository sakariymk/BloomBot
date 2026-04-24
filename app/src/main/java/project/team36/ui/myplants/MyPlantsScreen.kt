package project.team36.ui.myplants

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.mapbox.maps.extension.style.expressions.dsl.generated.image
import project.team36.ui.map.SavedPlacesViewModel
import project.team36.ui.plant.PlantCard
import project.team36.model.location.SavedLocation
import project.team36.model.location.SavedPlant
import project.team36.model.plant.PlantInfo
import project.team36.ui.navigation.BottomNavBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyPlantsScreen(
    navController: NavController,
    savedPlacesViewModel: SavedPlacesViewModel,
    myPlantsViewModel: MyPlantsViewModel
) {

    val savedLocations by savedPlacesViewModel.savedLocations.collectAsStateWithLifecycle()
    val selectedLocation by savedPlacesViewModel.selectedLocation.collectAsStateWithLifecycle()

    //hent planter for valgt sted
    val plants by myPlantsViewModel.plants.collectAsStateWithLifecycle()

    LaunchedEffect(selectedLocation) {
        selectedLocation?.let {
            val lat = it.lat ?: return@let
            val lon = it.lon ?: return@let
            myPlantsViewModel.loadPrecipitationForLocation(lat, lon)
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = selectedLocation?.name ?: "Mine planter",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color(0xFF2E7D32)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate("myPlaces")} ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Tilbake",
                            tint = Color(0xFF2E7D32)
                        )
                    }
                },
                actions = {

                    var expanded by remember { mutableStateOf(false) }
                    var showDeleteDialog by remember { mutableStateOf(false) }
                    var showRenameDialog by remember { mutableStateOf(false) }
                    var newLocationName by remember { mutableStateOf("") }

                    Box {
                        IconButton(onClick = {expanded = true }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Rediger sted",
                                tint = Color(0xFF2E7D32)
                            )
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false}
                        ) {
                            DropdownMenuItem(
                                text = { Text("Bytt navn") },
                                onClick = {
                                    expanded = false
                                    newLocationName = selectedLocation?.name ?: ""
                                    showRenameDialog = true
                                },
                            )
                            DropdownMenuItem(
                                text = { Text("Slett sted") },
                                onClick = {
                                    showDeleteDialog = true
                                    expanded = false
                                }
                            )
                            if (showDeleteDialog) {
                                AlertDialog(
                                    title = { Text("Ønsker du å slette stedet?") },
                                    text = { Text("Du vil miste alle lagrede planter på dette stedet.") },
                                    onDismissRequest = { showDeleteDialog = false},
                                    confirmButton = {
                                        TextButton(
                                            onClick = {
                                                selectedLocation?.let { myPlantsViewModel.deleteLocation(it) }
                                                navController.navigate("myPlaces")
                                                showDeleteDialog = false
                                            }
                                        ) { Text("Slett") }
                                    },
                                    dismissButton = {
                                        TextButton(onClick = { showDeleteDialog = false} ) {
                                            Text("Behold sted")
                                        }
                                    }
                                )
                            }
                            if (showRenameDialog) {
                                AlertDialog(
                                    title = { Text("Bytt navn på stedet") },
                                    text = {
                                        OutlinedTextField(
                                            value = newLocationName,
                                            onValueChange = { newLocationName = it },
                                            label = { Text("Nytt navn") },
                                            singleLine = true
                                        )
                                    },
                                    onDismissRequest = { showRenameDialog = false },
                                    confirmButton = {
                                        TextButton(
                                            onClick = {
                                                selectedLocation?.let {
                                                    myPlantsViewModel.renameLocation(it, newLocationName)
                                                }
                                                savedPlacesViewModel.updateSelectedLocationName(newLocationName)
                                                showRenameDialog = false
                                                navController.navigate("myPlants") {
                                                    popUpTo("myPlaces") { inclusive = false }
                                                }
                                            },
                                            enabled = newLocationName.isNotBlank()
                                        ) { Text("Lagre") }
                                    },
                                    dismissButton = {
                                        TextButton(onClick =  { showRenameDialog = false }) {
                                            Text("Avbryt")
                                        }
                                    }
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFE8F5E2)
                )
            )
        },

        bottomBar = { BottomNavBar(navController = navController, savedPlacesViewModel)},

        // Finn ut hvordan sende informasjon om hvor du kommer fra
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { navController.navigate("findPlants/${selectedLocation?.name ?: "Ukjent sted"}") },
                icon = { Icon(Icons.Default.Add, contentDescription = "Nytt sted") },
                text = { Text("Ny plante") },
                containerColor = Color(0xFF2D5A27),
                contentColor = Color.White,
                shape = RoundedCornerShape(50)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFE8F5E2))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (plants.isEmpty()) {
                item {
                    EmptyPlantCard(
                        onClick = { navController.navigate("findPlants/${selectedLocation?.name ?: "Ukjent sted"}")}
                    )
                }
            } else {
                // Vi mater inn riktig liste, og tegner opp nøyaktig det elementet vi er på.
                items(plants){ plant ->
                    PlantCard(
                        plant = plant,
                        needsWater = myPlantsViewModel.needsWater(plant),
                        daysUntilWatering = myPlantsViewModel.daysUntilWatering(plant),
                        onMarkAsWatered = { myPlantsViewModel.markAsWatered(plant) },
                        navController = navController
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyPlantCard(
    onClick: () -> Unit
){
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 2.dp,
                color = Color(0xFF2E7D32),
                shape = RoundedCornerShape(16.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF2E7D32)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Eco,
                    contentDescription = "Ikon av plante",
                    tint = Color(0xFFE8F5E2),
                    modifier = Modifier.size(40.dp)
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Ingen planter i denne hagen enda.\nGro din første plante!",
                    fontSize = 14.sp,
                    color = Color.Black
                )
                Button(
                    onClick = onClick ,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2E7D32)
                    ),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Legg til")
                    Spacer(Modifier.width(6.dp))
                    Text("Ny Plante")
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

//For at alt skal fungere midledtidig før vi får laget nye plantsCards, eller andre løsninger
fun SavedPlant.toPlantInfo(): PlantInfo = PlantInfo(
    name = this.name,
    soil = this.soil,
    light = this.light,
    isFrostResistant = this.isFrostResistant,
    waterPerWeek = this.waterPerWeek,
    plantingMonth = this.plantingMonth,
    climateZone = this.climateZone,
    hasBeenWatered = this.hasBeenWatered,
    daysSinceWatered = this.daysSinceWatered,
    maxDaysWithoutWater = this.maxDaysWithoutWater,
    imageRes = this.imageRes,
    description = this.description
)