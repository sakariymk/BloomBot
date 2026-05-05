package project.team36.ui.landingpage

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import project.team36.R
import project.team36.ui.theme.Team36Theme

@Composable
fun OnboardingDialog(
    onDismiss: () -> Unit,
    onFinish: () -> Unit
) {
    data class OnboardingStep(
        val title: String,
        val description: String,
        val imageRes: Int
    )

    val steps = listOf(
        OnboardingStep( "Velkommen til Bloombot!", "Hjelp plantene dine å trives med smarte tips, værdata og vekstsporing.", R.drawable.bloombot),
        OnboardingStep("Registrer din hage", "Lagre en lokasjon for å ha oversikt over din hage. Vi gir deg også værvarsel tilpasset akkurat din hage.", R.drawable.defaultplant),
        OnboardingStep("Legg til dine planter", "Få anbefalinger om planter som vil trives i din hage.", R.drawable.defaultplant),
        OnboardingStep("Tilpasset KI-råd", "Vi overvåker været og gir beskjed om vanning. Ingen manuell jobb!", R.drawable.defaultplant)
    )

    var currentStep by remember { mutableIntStateOf(0) }
    val isLastStep = currentStep == steps.size - 1
    val step = steps[currentStep]

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E0))
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {

                // Hopp over
                Box(modifier = Modifier.fillMaxWidth()) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.align(Alignment.TopEnd).padding(4.dp)
                    ) {
                        Text("Hopp over", color = Color(0xFF386633), fontSize = 12.sp)
                    }
                }

                // Illustrasjon
                Image(
                    painter = painterResource(id = step.imageRes),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .padding(horizontal = 24.dp)
                )

                Column(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Animerte steg-prikker
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        steps.indices.forEach { index ->
                            Box(
                                modifier = Modifier
                                    .height(6.dp)
                                    .width(if (index == currentStep) 24.dp else 6.dp)
                                    .background(
                                        color = if (index == currentStep) Color(0xFF386633)
                                        else Color(0xFF386633).copy(alpha = 0.3f),
                                        shape = CircleShape
                                    )
                            )
                        }
                    }

                    // Tittel
                    Text(
                        text = step.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = Color(0xFF1B3A2D)
                    )

                    // Beskrivelse
                    Text(
                        text = step.description,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = Color(0xFF386633)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Knapper
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (currentStep > 0) {
                            OutlinedButton(
                                onClick = { currentStep-- },
                                modifier = Modifier.weight(1.5f),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, Color(0xFF386633))
                            ) {
                                Text("Tilbake", color = Color(0xFF386633))
                            }
                        }
                        Button(
                            onClick = { if (isLastStep) onFinish() else currentStep++ },
                            modifier = Modifier.weight(2.5f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isLastStep) Color(0xFF386633) else Color(0xFFB8D9B0)
                            ),

                        ) {
                            Text(
                                text = if (isLastStep) "Kom i gang!" else "Neste",
                                color = if (isLastStep) Color.White else Color(0xFF386633),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun OnboardingDialogPreview() {
    Team36Theme {
        OnboardingDialog(
            onDismiss = {},
            onFinish = {}
        )
    }
}