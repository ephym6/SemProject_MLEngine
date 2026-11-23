package io.github.ephym6.plantid.data.plant

import io.github.ephym6.plantid.domain.plant.CareRecommendations
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class PlantDataParserTest {

    /** The real bundled file. Unit tests run with the module directory as working directory. */
    private val bundledText = File("src/main/assets/plants.json").readText()

    @Test
    fun `bundled data has the 30 model species, six with complete care values`() {
        val catalog = PlantDataParser.parse(bundledText)

        assertEquals(30, catalog.plants.size)
        catalog.plants.forEach { plant ->
            assertNotNull("${plant.id} common name", plant.commonName)
            assertNotNull("${plant.id} scientific name", plant.scientificName)
        }
        val withCare = catalog.plants.filter { CareRecommendations.tipsFor(it.care).isNotEmpty() }
        assertEquals(
            setOf("coleus", "golden_pothos", "jade_plant", "ming_aralia", "rubber_plant", "weeping_fig"),
            withCare.map { it.id }.toSet(),
        )
        withCare.forEach { assertEquals("${it.id} tips", 4, CareRecommendations.tipsFor(it.care).size) }
        assertEquals(catalog.plants.size, catalog.plants.map { it.classLabel }.toSet().size)
        assertNull("other_plant is not a plant", catalog.byClassLabel("other_plant"))
        assertEquals("Epipremnum aureum", catalog.byClassLabel("golden_pothos")?.scientificName)
    }

    @Test
    fun `bundled data credits its source and licence`() {
        val source = PlantDataParser.parse(bundledText).source

        assertEquals("CC BY-NC 4.0", source.license)
        assertTrue(source.url!!.contains("floradb-houseplants-care-sample"))
    }

    @Test
    fun `bundled data contains no toxicity information`() {
        val plants = Json.parseToJsonElement(bundledText).jsonObject["plants"]!!.jsonArray
        plants.forEach { element ->
            val keys = collectKeys(element.jsonObject)
            assertFalse("toxicity key in ${element.jsonObject["id"]}: $keys", keys.any { "toxic" in it.lowercase() })
        }
    }

    @Test
    fun `optional fields may be missing`() {
        val catalog = PlantDataParser.parse(
            """{"schemaVersion":1,"source":{"name":"S"},"plants":[{"id":"x","classLabel":"X"}]}""",
        )
        val plant = catalog.plants.single()
        assertNull(plant.commonName)
        assertNull(plant.care)
        assertNull(catalog.source.license)
    }

    @Test
    fun `malformed data, unknown schema and duplicate ids are rejected`() {
        assertThrows(IllegalArgumentException::class.java) { PlantDataParser.parse("not json") }
        assertThrows(IllegalArgumentException::class.java) {
            PlantDataParser.parse("""{"schemaVersion":2,"source":{"name":"S"},"plants":[]}""")
        }
        assertThrows(IllegalArgumentException::class.java) {
            PlantDataParser.parse(
                """{"schemaVersion":1,"source":{"name":"S"},"plants":[
                   {"id":"x","classLabel":"X"},{"id":"x","classLabel":"Y"}]}""",
            )
        }
    }

    private fun collectKeys(obj: JsonObject): Set<String> =
        obj.keys + obj.values.filterIsInstance<JsonObject>().flatMap { collectKeys(it) }
}
