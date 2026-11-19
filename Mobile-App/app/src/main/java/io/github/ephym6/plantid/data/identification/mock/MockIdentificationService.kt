package io.github.ephym6.plantid.data.identification.mock

import io.github.ephym6.plantid.domain.identification.FailureReason
import io.github.ephym6.plantid.domain.identification.IdentificationOutcome
import io.github.ephym6.plantid.domain.identification.IdentificationService
import io.github.ephym6.plantid.domain.identification.ModelInfo
import io.github.ephym6.plantid.domain.identification.PlantSummary
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

/**
 * DEMO ONLY. Returns a predictable outcome for the selected [DemoScenario]
 * after a short fixed delay, so the UI flow can be built and tested before the
 * real model exists (spec §8.1). It does **not** analyse the photo (it only
 * checks the file still exists), and its results are not predictions. [modelInfo] is marked `isDemo` so the UI labels
 * every result as demo output.
 *
 * The plants it returns are fictional ([DemoPlants]). No real species,
 * accuracy or care facts.
 */
class MockIdentificationService(
    private val responseDelayMillis: Long = DEFAULT_DELAY_MILLIS,
    private val slowDelayMillis: Long = SLOW_DELAY_MILLIS,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : IdentificationService, DemoScenarioController {

    override val modelInfo = ModelInfo(name = "PlantID demo identifier", version = null, isDemo = true)

    private val _scenario = MutableStateFlow(DemoScenario.Identified)
    override val scenario: StateFlow<DemoScenario> = _scenario.asStateFlow()

    override fun select(scenario: DemoScenario) {
        _scenario.value = scenario
    }

    override suspend fun identify(photo: File): IdentificationOutcome {
        // Read once at the start so a change mid-request can't mix scenarios.
        val scenario = _scenario.value
        delay(if (scenario == DemoScenario.Slow) slowDelayMillis else responseDelayMillis)
        // Behave like a real identifier if the photo has gone (e.g. cache cleared).
        if (!withContext(ioDispatcher) { photo.isFile }) {
            return IdentificationOutcome.Failed(modelInfo, FailureReason.PhotoUnavailable)
        }
        return when (scenario) {
            DemoScenario.Identified, DemoScenario.Slow ->
                IdentificationOutcome.Identified(modelInfo, DemoPlants.CLASS_A, DemoPlants.complete)
            DemoScenario.MissingDetails ->
                IdentificationOutcome.Identified(modelInfo, DemoPlants.CLASS_B, DemoPlants.minimal)
            DemoScenario.Uncertain -> IdentificationOutcome.Uncertain(modelInfo)
            DemoScenario.Unsupported -> IdentificationOutcome.Unsupported(modelInfo)
            DemoScenario.Failure -> IdentificationOutcome.Failed(modelInfo, FailureReason.ProcessingFailed)
        }
    }

    companion object {
        const val DEFAULT_DELAY_MILLIS = 1_200L
        const val SLOW_DELAY_MILLIS = 6_000L
    }
}

/** Outcomes the mock can return. Covers the demo-data cases in spec §7.4. */
enum class DemoScenario {
    /** A normal supported result with all optional fields. */
    Identified,

    /** A supported result missing scientific name and description. */
    MissingDetails,

    /** Low confidence: no species shown. */
    Uncertain,

    /** No supported plant found. */
    Unsupported,

    /** Inference error; the user can retry. */
    Failure,

    /** Same as [Identified] but slow, to check the loading state. */
    Slow,
}

/** Lets the UI choose the mock's next outcome. Only available while the mock is in use. */
interface DemoScenarioController {
    val scenario: StateFlow<DemoScenario>
    fun select(scenario: DemoScenario)
}

/** Fictional plant records for demo results. Not real species; no real plant facts. */
object DemoPlants {
    const val CLASS_A = "demo_class_a"
    const val CLASS_B = "demo_class_b"

    val complete = PlantSummary(
        plantId = "demo_plant_a",
        commonName = "Demo Plant A",
        scientificName = "Demo species (fictional)",
        description = "A fictional plant used to demonstrate the result screen. It is not a real species.",
    )

    /** Only a common name, to check that missing optional details don't break the screen. */
    val minimal = PlantSummary(
        plantId = "demo_plant_b",
        commonName = "Demo Plant B",
        scientificName = null,
        description = null,
    )
}
