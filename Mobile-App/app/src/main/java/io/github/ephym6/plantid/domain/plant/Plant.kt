package io.github.ephym6.plantid.domain.plant

import io.github.ephym6.plantid.domain.identification.PlantSummary

/**
 * A supported plant (spec §7.1). Not the same as an identification event: one
 * plant can be identified many times.
 *
 * @param classLabel the classifier's label for this plant (e.g. "Epipremnum_aureum").
 */
data class Plant(
    val id: String,
    val classLabel: String,
    val commonName: String?,
    val scientificName: String?,
    val family: String?,
    val care: CareData?,
)

/** Care values exactly as the source provides them. Any field may be missing. */
data class CareData(
    val lightLux: IntRange? = null,
    val temperatureCelsius: IntRange? = null,
    val humidityPercent: Int? = null,
    /** The source's typical watering interval. Not a schedule to follow regardless of conditions. */
    val wateringIntervalDays: Int? = null,
)

/** Where care information comes from. Shown with every care guide. */
data class ContentSource(
    val name: String,
    val url: String?,
    val license: String?,
)

/** All supported plants plus the source of their care information. */
data class PlantCatalog(
    val source: ContentSource,
    val plants: List<Plant>,
) {
    fun byId(id: String): Plant? = plants.firstOrNull { it.id == id }
    fun byClassLabel(classLabel: String): Plant? = plants.firstOrNull { it.classLabel == classLabel }
}

/** Bundled plant data and care information. */
interface PlantRepository {
    /** The catalog, or a failure if the bundled data can't be read. */
    suspend fun catalog(): Result<PlantCatalog>
}

fun Plant.toSummary(): PlantSummary = PlantSummary(
    plantId = id,
    commonName = commonName,
    scientificName = scientificName,
    // The current source has no descriptions; none are invented.
    description = null,
)
