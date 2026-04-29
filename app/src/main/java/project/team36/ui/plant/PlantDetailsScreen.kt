package project.team36.ui.plant

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DeviceThermostat
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.RichTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TextButton
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import project.team36.model.plant.LightType
import project.team36.model.plant.SoilType
import project.team36.ui.theme.Team36Theme
import project.team36.R
import project.team36.model.plant.displayName

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantDetailsScreen(
    navController: NavController,
    plantName: String,
    plantDetailsViewModel: PlantDetailsViewModel,
    onBackClick: () -> Unit,
    onAddPlant: (() -> Unit)? = null,
    onDeletePlant: (() -> Unit)? = null
) {
    val scrollState = rememberScrollState()
    val plants by plantDetailsViewModel.plant.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = Color(0xFFE8F5E2),
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton( onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Tilbake",
                            tint = Color(0xFF2E7D32)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                ),
                windowInsets = WindowInsets(0) //gjør at appen ikke legger til mye mellomrom på toppen av skjermen
            )
        }
    ) { innerPadding ->
        plants?.let { plant ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(scrollState)
            ) {
                Image(
                    painter = painterResource(plant.imageRes),
                    contentDescription = plant.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(16.dp))
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = plant.name,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color(0xFF2E7D32)
                    )
                    if (onAddPlant != null) {
                        OutlinedButton(
                            onClick = { onAddPlant() },
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color.White
                            ),
                            border = BorderStroke(0.dp, Color.Gray)
                        ) {
                            Text("Legg til i hage", color = Color(0xFF2E7D32))
                        }
                    } else if (onDeletePlant != null) {
                        OutlinedButton(
                            onClick = {
                                onDeletePlant()
                                onBackClick()
                            },
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color.White
                            ),
                            border = BorderStroke(0.dp, Color.Gray)
                        ) {
                            Text("Fjern plante fra hage", color = Color(0xFF2E7D32))
                        }
                    }

                }
                PlantInfoCard(title = "Om planten") {
                    Text("${plant.description}")
                }
                Spacer(modifier = Modifier.height(8.dp))
                PlantInfoCard(title = "Plantingsmåned") {
                    Text("Dette er de ideelle månedene å plante denne planten.")
                    PlantingMonthBar(plantingMonth = plant.plantingMonth)
                }
                Spacer(modifier = Modifier.height(8.dp))
                PlantInfoCard(title = "Herdighetssone") {
                    Text("Herdighetssone sier noe om hvor planten vil trives best med tanke på lokalklima.")
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeviceThermostat,
                            contentDescription = null,
                            tint = Color(0xFF5A7A3A)
                        )
                        Text(
                            "H${plant.climateZone}",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        )
                    }
                    HardinessZoneTooltip()
                }
                PlantInfoCard("Solforhold") {
                    Text("Ulike planter har ulike behov for sol, noen trives best med full sollys, mens andre foretrekker mer skygge for å trives best.")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        plant.light.forEach { lightType ->
                            OutlinedButton(
                                onClick = {},
                                shape = RoundedCornerShape(50),
                                border = BorderStroke(1.dp, Color.LightGray)
                            ) {
                                Text(
                                    text = lightType.displayName,
                                    color = Color.Black
                                )
                            }
                        }
                    }
                }
                PlantInfoCard(title = "Vanning") {
                    Text("Vi registrerer om det har regnet, slik at du kan få påminnelse om vanning dersom det ikke har regnet tilstrekkelig.")
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.WaterDrop,
                            contentDescription = null,
                            tint = Color(0xFF5A7A3A)
                        )
                        Text(
                            "Vannes ca. hver ${plant.maxDaysWithoutWater}. dag",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
                PlantInfoCard(title = "Jordtype") {
                    Text("Jorden planten trives aller best i når det gjelder struktur, sammensetning, næringsinnhold og surhetsgrad. Valg av jordtype avhenger av plantens behov, men generelt trives de fleste planter i en luftig, næringsrik jord med god drenering ")
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        plant.soil.forEach { soilType ->
                            OutlinedButton(
                                onClick = {},
                                shape = RoundedCornerShape(50),
                                border = BorderStroke(1.dp, Color.LightGray)
                            ) {
                                Text(
                                    text = soilType.displayName,
                                    color = Color.Black
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PlantInfoCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(title, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
            content()
        }
    }
}

@Composable
fun PlantingMonthBar(plantingMonth: Set<Int>) {
    val monthNames = listOf(
        "jan", "feb", "mar", "apr", "mai", "jun",
        "jul", "aug", "sep", "okt", "nov", "des"
    )
    val sorted = plantingMonth.sorted()
    val label = sorted.map { monthNames[it - 1] }.let { "${it.first()}-${it.last()}" }

    val startFraction = (sorted.first() - 1) / 12f
    val widthFraction = (sorted.last() - sorted.first() + 1) / 12f

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("jan", fontSize = 12.sp, color = Color.Gray)
            Text("jun", fontSize = 12.sp, color = Color.Gray)
            Text("des", fontSize = 12.sp, color = Color.Gray)
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp)
                .clip(RoundedCornerShape(50))
                .background(Color(0xFFB0C080))
        ) {
            Spacer(modifier = Modifier.fillMaxWidth(startFraction))
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(widthFraction / (1f - startFraction))
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xFF5A7A3A)),
                contentAlignment = Alignment.Center
            ) {
                Text(label, fontSize = 11.sp, color = Color.White)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HardinessZoneTooltip() {
    val tooltipState= rememberTooltipState(isPersistent = true)
    val scope = rememberCoroutineScope()

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TooltipBox(
            positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
            tooltip = {
                RichTooltip(
                    title = { Text("Herdighetssoner") },
                    action = {
                        TextButton(onClick = {scope.launch { tooltipState.dismiss() }}) {
                            Text("Lukk")
                        }
                    },
                    colors = TooltipDefaults.richTooltipColors(
                        containerColor = Color(0xFFE8F8E3),
                        titleContentColor = Color(0xFF2D5A27),
                        contentColor = Color.Black,
                        actionContentColor = Color(0xFF2D5A27)
                    )
                ) {
                    Text("Herdighetstallene må betraktes som en rettesnor som kan gi hjelp når du skal velge planter, men de gir ingen garanti om et godt resultat. Det er tryggest å plante i tråd med klimasoneangivelsene, men på lune steder kan det likevel være forsøket verdt å prøve med planter som ut fra det grove kartet tilsynelatende ikke skulle klare seg. \\nSom hovedregel kan du gå for planter som er merket med den herdighetssonen du bor i – og alle tallene over. For eksempel trives en plante med herdighetssone 8 i alle soner, mens herdighetssone 4 trives i sone 1 til 4. ")
                }
            },
            state = tooltipState
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                OutlinedButton(
                    onClick = { scope.launch { tooltipState.show() } },
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, Color.LightGray),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color(0xFFE8F8E3)
                    ),
                ) {
                    Text("Herdighetssone forklart", color = Color.Black)
                }
            }

        }
    }
}