package io.github.ephym6.plantid.data.identification.mock

import io.github.ephym6.plantid.domain.identification.FailureReason
import io.github.ephym6.plantid.domain.identification.IdentificationOutcome
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class MockIdentificationServiceTest {

    private val photo: File = File.createTempFile("mock_test_", ".jpg")

    @After
    fun tearDown() {
        photo.delete()
    }

    private fun TestScope.newService() =
        MockIdentificationService(ioDispatcher = StandardTestDispatcher(testScheduler))

    private fun TestScope.identifyWith(scenario: DemoScenario): IdentificationOutcome {
        val service = newService()
        service.select(scenario)
        val result = async { service.identify(photo) }
        advanceTimeBy(MockIdentificationService.SLOW_DELAY_MILLIS + 1)
        runCurrent()
        return result.getCompleted()
    }

    @Test
    fun `every outcome is marked as demo output`() = runTest {
        DemoScenario.entries.forEach { scenario ->
            assertTrue("$scenario", identifyWith(scenario).model.isDemo)
        }
    }

    @Test
    fun `default scenario is a complete fictional identification without scores`() = runTest {
        val outcome = identifyWith(newService().scenario.value) as IdentificationOutcome.Identified

        assertEquals(DemoPlants.complete, outcome.plant)
        assertEquals(DemoPlants.CLASS_A, outcome.classId)
        assertTrue("mock must not invent scores", outcome.scores.isEmpty())
    }

    @Test
    fun `missing details scenario has only a common name`() = runTest {
        val outcome = identifyWith(DemoScenario.MissingDetails) as IdentificationOutcome.Identified

        assertEquals("Demo Plant B", outcome.plant.commonName)
        assertNull(outcome.plant.scientificName)
        assertNull(outcome.plant.description)
    }

    @Test
    fun `uncertain, unsupported and failure scenarios return no plant`() = runTest {
        val uncertain = identifyWith(DemoScenario.Uncertain)
        assertTrue(uncertain is IdentificationOutcome.Uncertain)
        assertTrue((uncertain as IdentificationOutcome.Uncertain).scores.isEmpty())

        assertTrue(identifyWith(DemoScenario.Unsupported) is IdentificationOutcome.Unsupported)

        assertEquals(
            FailureReason.ProcessingFailed,
            (identifyWith(DemoScenario.Failure) as IdentificationOutcome.Failed).reason,
        )
    }

    @Test
    fun `results are deterministic`() = runTest {
        DemoScenario.entries.forEach { scenario ->
            assertEquals("$scenario", identifyWith(scenario), identifyWith(scenario))
        }
    }

    @Test
    fun `normal scenarios respond after the default delay`() = runTest {
        val service = newService()
        val result = async { service.identify(photo) }

        advanceTimeBy(MockIdentificationService.DEFAULT_DELAY_MILLIS - 1)
        runCurrent()
        assertTrue("still loading", result.isActive)

        advanceTimeBy(2)
        runCurrent()
        assertTrue(result.isCompleted)
    }

    @Test
    fun `slow scenario takes longer`() = runTest {
        val service = newService()
        service.select(DemoScenario.Slow)
        val result = async { service.identify(photo) }

        advanceTimeBy(MockIdentificationService.DEFAULT_DELAY_MILLIS + 1)
        runCurrent()
        assertTrue("still loading", result.isActive)

        advanceTimeBy(MockIdentificationService.SLOW_DELAY_MILLIS)
        runCurrent()
        assertTrue(result.getCompleted() is IdentificationOutcome.Identified)
    }

    @Test
    fun `scenario is fixed when the request starts`() = runTest {
        val service = newService()
        service.select(DemoScenario.Unsupported)
        val result = async { service.identify(photo) }
        runCurrent()

        service.select(DemoScenario.Identified)
        advanceTimeBy(MockIdentificationService.SLOW_DELAY_MILLIS)
        runCurrent()

        assertTrue(result.getCompleted() is IdentificationOutcome.Unsupported)
    }

    @Test
    fun `missing photo fails instead of returning a demo success`() = runTest {
        photo.delete()

        val outcome = identifyWith(DemoScenario.Identified)

        assertEquals(FailureReason.PhotoUnavailable, (outcome as IdentificationOutcome.Failed).reason)
    }
}
