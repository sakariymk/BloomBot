package project.team36.model.ai

import kotlinx.serialization.Serializable

@Serializable
data class AiRecommendationResponse(
    val weatherSummary: String, // Kort oppsummering av været/varsler
    val plantAdvice: List<PlantAdvice>, // Spesifikke råd for hver plante i hagen
    val generalTasks: List<String> // Generelle hageoppgaver
)

// Dataklasse for individuelle planteråd
@Serializable
data class PlantAdvice(
    val plantName: String,
    val advice: String
)