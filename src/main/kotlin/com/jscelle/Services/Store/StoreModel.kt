import kotlinx.serialization.Serializable

@Serializable
data class Store (
    val name: String,
    val latitude: Double,
    val longitude: Double
)