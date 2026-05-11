package project.team36.model.ai

import kotlinx.serialization.Serializable

@Serializable
data class AiRecommendationResponse(
    val plantAdvice: List<PlantAdvice>, // Spesifikke råd for hver plante i hagen
    val generalTasks: List<String> // Generelle hageoppgaver
)

// Dataklasse for individuelle planteråd
@Serializable
data class PlantAdvice(
    val Recommend: String? = null,
    val plantName: String,
    val advice: String
)