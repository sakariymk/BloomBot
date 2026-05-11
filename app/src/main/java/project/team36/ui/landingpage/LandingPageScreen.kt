package project.team36.ui.landingpage

import android.Manifest
import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import project.team36.ui.map.LocationsViewModel
import project.team36.ui.navigation.BottomNavBar
import androidx.compose.material3.*
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.core.content.edit
import project.team36.ui.myplants.MyPlantsViewModel
import project.team36.data.mcp.WeatherAlert
import project.team36.R

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun LandingPageScreen(
    navController: NavController,
    locationsViewModel: LocationsViewModel,
    landingPageViewModel: LandingPageViewModel,
    myPlantsViewModel: MyPlantsViewModel
) {

    val uiState by landingPageViewModel.uiState.collectAsStateWithLifecycle()
    val savedLocations by locationsViewModel.savedLocations.collectAsStateWithLifecycle()
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }

    // for onboarding
    val context = LocalContext.current
    val prefs = remember {
        context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    }

    // Vis automatisk første gang
    LaunchedEffect(Unit) {
        if (!prefs.getBoolean("has_seen_intro", false)) {
            landingPageViewModel.showInfoDialog()
            prefs.edit { putBoolean("has_seen_intro", true) }
        }
    }

    val locationPermission = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

    val plantsByLocation by remember(savedLocations) {
        myPlantsViewModel.getPlantsNeedingWaterByLocation(savedLocations)
    }.collectAsStateWithLifecycle(initialValue = emptyMap())

    LaunchedEffect(Unit) { locationPermission.launchPermissionRequest() }

    //Avslutter LaunchedEffect-blokken tidlig dersom det er ingen lagrede steder
    //Henter data for lagrede steder
    //Setter selectedLocation til første tab når siden lastes inn, så anbefalte planter kan displaye for denne hagen med en gang
    LaunchedEffect(savedLocations) {
        if (savedLocations.isEmpty()) return@LaunchedEffect
        landingPageViewModel.loadDataForLocations(savedLocations)
        if (locationsViewModel.selectedLocation.value == null) {
            locationsViewModel.selectLocation(savedLocations[0])
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Hjem",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                actions = {
                    IconButton(onClick = { landingPageViewModel.showInfoDialog() }) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Guide",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
                windowInsets = WindowInsets(0)
            )
        },
        bottomBar = { BottomNavBar(navController = navController, locationsViewModel) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            if (savedLocations.isEmpty()) {
                Text(
                    text = "Ingen lagrede hager ennå.",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            } else {
                //horisontal scrollable liste med lagrede hager
                PrimaryScrollableTabRow(
                    selectedTabIndex = selectedTabIndex,
                    edgePadding = 16.dp,
                    containerColor = MaterialTheme.colorScheme.background,
                    divider = {},
                    indicator = {}
                ) {
                    savedLocations.forEachIndexed { index, location ->
                        val alerts = uiState.locationAlerts[location.id] ?: emptyList()
                        val hasAlert = alerts.isNotEmpty()
                        val alertColor = alerts.firstOrNull()?.color
                            ?: Color.Red
                        val isSelected = selectedTabIndex == index
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index; locationsViewModel.selectLocation(location) },
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .height(40.dp)
                                .background(
                                    color = if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.tertiary,
                                    shape = CircleShape
                                )
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.outline,
                                    shape = CircleShape
                                ),
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = location.name ?: "Hage",
                                        color = if (isSelected) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onTertiary,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                    if (hasAlert) {
                                        Spacer(Modifier.width(4.dp))
                                        Icon(
                                            Icons.Default.Warning,
                                            contentDescription = null,
                                            tint = alertColor,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        )
                    }
                }

                val selectedLocation =
                    savedLocations.getOrNull(selectedTabIndex) ?: savedLocations[0]
                val alertsForSelectedLocation: List<WeatherAlert> =
                    uiState.locationAlerts[selectedLocation.id] ?: emptyList()

                Spacer(modifier = Modifier.height(8.dp))

                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    if (alertsForSelectedLocation.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Column {
                            alertsForSelectedLocation.forEach { alert ->
                                LocationAlertBanner(alert)
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    WeatherCard(
                        weatherUiState = uiState.locationForecasts[selectedLocation.id],
                        isLoading = uiState.isWeatherLoading,
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        //knapp for å hente AI-råd
                        Button(
                            onClick = { landingPageViewModel.fetchAiAdvice(selectedLocation) },
                            modifier = Modifier
                                .wrapContentWidth()
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            Color(0xFF6DBDD7),
                                            Color(0xFF69CE64)
                                        )
                                    ),
                                    shape = CircleShape
                                ),
                            enabled = !uiState.isAiLoading && !uiState.isFutureAiLoading,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                            ),
                            shape = CircleShape
                        ) {
                            if (uiState.isAiLoading) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(28.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Image(
                                    painter = painterResource(id = R.drawable.lightbulb),
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    colorFilter = ColorFilter.tint(Color.White)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Få råd fra Bloombot",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    }


                    //AlertDialog som viser AI resultatene
                    if (uiState.aiResponse != null) {
                        AlertDialog(
                            onDismissRequest = { landingPageViewModel.dismissAiAdvice() },
                            containerColor = MaterialTheme.colorScheme.surface,
                            title = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Image(
                                        painter = painterResource(id = R.drawable.bloombot_yap),
                                        contentDescription = "Bloombot",
                                        modifier = Modifier.size(48.dp),
                                        contentScale = ContentScale.Fit
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Bloombot sier:", fontWeight = FontWeight.Bold)
                                }
                            },
                            text = {
                                Column(
                                    modifier = Modifier.verticalScroll(rememberScrollState()),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Column {
                                        Text(
                                            "Været:",
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(uiState.aiResponse!!.weatherSummary)
                                    }
                                    if (uiState.aiResponse!!.plantAdvice.isNotEmpty()) {
                                        Column {
                                            Text(
                                                "Dine planter:",
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            uiState.aiResponse!!.plantAdvice.forEach { advice ->
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    "• ${advice.plantName}",
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                                Text(
                                                    advice.advice,
                                                    modifier = Modifier.padding(start = 12.dp),
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                        }
                                    }
                                    if (uiState.aiResponse!!.generalTasks.isNotEmpty()) {
                                        Column {
                                            Text(
                                                "Hageoppgaver:",
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            uiState.aiResponse!!.generalTasks.forEach { task ->
                                                Text(
                                                    "- $task",
                                                    modifier = Modifier.padding(start = 4.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            },
                            confirmButton = {
                                TextButton(onClick = { landingPageViewModel.dismissAiAdvice() }) {
                                    Text("Lukk", color = MaterialTheme.colorScheme.onSurface)
                                }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    //liste med dagens oppgaver, hvilke planter som skal vannes for dagen
                    DailyTasksCard(
                        plantsByLocation = plantsByLocation,
                        onMarkAsWatered = { myPlantsViewModel.markAsWatered(it) }
                    )
                }
            }
        }
    }

        //onboarding dialogboks, kort intro til appen
        if (uiState.showInfoDialog) {
            OnboardingDialog(
                onDismiss = { landingPageViewModel.dismissInfoDialog() },
                onFinish = {
                    landingPageViewModel.dismissInfoDialog()
                    navController.navigate("myPlaces")
                }
            )
        }
    }

    //metAlerts banner for en lokasjon, vises om det er farevarsel for lokasjonen
    @Composable
    fun LocationAlertBanner(alert: WeatherAlert) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(alert.color.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                .border(1.dp, alert.color.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                .padding(horizontal = 6.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = alert.color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.padding(vertical = 6.dp)) {
                Text(
                    text = alert.event.uppercase(),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = alert.description,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    lineHeight = 18.sp
                )
            }
        }
    }