package io.github.ephym6.plantid.data.plant

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assume.assumeTrue
import org.junit.Test
import java.io.File

/**
 * Catches drift between the bundled plants.json and the AI team's root files.
 * If this fails, run `python Mobile-App/tools/sync_plant_data.py` from the
 * repository root and commit the regenerated asset.
 *
 * Skipped when the root files aren't available, so the Android project still
 * builds on its own.
 */
class PlantDataSyncTest {

    private val repoRoot = File("../..")
    private val speciesMapFile = File(repoRoot, "species_map.json")
    private val careTipsFile = File(repoRoot, "care_tips.json")

    @Test
    fun `bundled plants match species_map and care_tips`() {
        assumeTrue("root data files not found", speciesMapFile.isFile && careTipsFile.isFile)

        val speciesMap = Json.parseToJsonElement(speciesMapFile.readText()).jsonObject
        val careTips = Json.parseToJsonElement(careTipsFile.readText()).jsonObject
        val catalog = PlantDataParser.parse(File("src/main/assets/plants.json").readText())

        assertEquals(speciesMap.keys, catalog.plants.map { it.classLabel }.toSet())
        catalog.plants.forEach { plant ->
            val scientificName = speciesMap.getValue(plant.classLabel).jsonPrimitive.content
            val tips = careTips.getValue(scientificName).jsonObject
            assertEquals(scientificName, plant.scientificName)
            assertEquals(tips.string("common_name"), plant.commonName)
            assertEquals(tips.string("family"), plant.family)
            assertEquals(tips.number("watering_frequency_days"), plant.care?.wateringIntervalDays)
            assertEquals(tips.number("ideal_humidity_percent"), plant.care?.humidityPercent)
            assertEquals(tips.number("min_lux")!!..tips.number("max_lux")!!, plant.care?.lightLux)
            assertEquals(
                tips.number("min_temp_celsius")!!..tips.number("max_temp_celsius")!!,
                plant.care?.temperatureCelsius,
            )
        }
    }

    private fun JsonObject.string(key: String) = get(key)?.jsonPrimitive?.contentOrNull
    private fun JsonObject.number(key: String) = get(key)?.jsonPrimitive?.int
}
