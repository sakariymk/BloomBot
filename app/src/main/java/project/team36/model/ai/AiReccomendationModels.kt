package project.team36.model.ai

import kotlinx.serialization.Serializable

@Serializable
data class AiRecommendationResponse(
    val plantAdvice: List<PlantAdvice>,
    val generalTasks: List<String>
)

// Dataclass for advice
@Serializable
data class PlantAdvice(
    val Recommend: String? = null,
    val plantName: String,
    val advice: String
)