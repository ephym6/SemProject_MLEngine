package io.github.ephym6.plantid.data.plant

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assume.assumeTrue
import org.junit.Test
import java.io.File

/**
 * Catches drift between the bundled plants.json and the ML team's files
 * (model/classes.json, model/labels.txt, care_tips.json). If this fails, run
 * `python Mobile-App/tools/sync_plant_data.py` from the repository root and
 * commit the regenerated asset.
 *
 * Skipped when the root files aren't available, so the Android project still
 * builds on its own.
 */
class PlantDataSyncTest {

    private val repoRoot = File("../..")
    private val classesFile = File(repoRoot, "model/classes.json")
    private val labelsFile = File(repoRoot, "model/labels.txt")
    private val careTipsFile = File(repoRoot, "care_tips.json")

    private fun catalog() = PlantDataParser.parse(File("src/main/assets/plants.json").readText())

    @Test
    fun `every model label except other_plant is a plant in the catalog`() {
        assumeTrue("model/labels.txt not found", labelsFile.isFile)
        val labels = labelsFile.readLines().map { it.trim() }.filter { it.isNotEmpty() }
        val catalog = catalog()

        labels.filter { it != "other_plant" }.forEach { label ->
            assertNotNull("label '$label' has no plant: rerun sync_plant_data.py", catalog.byClassLabel(label))
        }
        assertEquals(labels.size - 1, catalog.plants.size)
    }

    @Test
    fun `plants match classes_json and care values match care_tips exactly`() {
        assumeTrue("root data files not found", classesFile.isFile && careTipsFile.isFile)
        val classes = Json.parseToJsonElement(classesFile.readText()).jsonArray.map { it.jsonObject }
        val careTips = Json.parseToJsonElement(careTipsFile.readText()).jsonObject
        val catalog = catalog()

        classes.filter { it.string("slug") != "other_plant" }.forEach { entry ->
            val label = entry.string("slug")!!
            val plant = catalog.byClassLabel(label)!!
            assertEquals(entry.string("common"), plant.commonName)
            assertEquals(entry.string("scientific"), plant.scientificName)

            val tips = careTips[plant.scientificName]?.jsonObject
            if (tips == null) {
                assertNull("$label has care data without an exact source match", plant.care)
            } else {
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
    }

    private fun JsonObject.string(key: String) = get(key)?.jsonPrimitive?.contentOrNull
    private fun JsonObject.number(key: String) = get(key)?.jsonPrimitive?.int
}
