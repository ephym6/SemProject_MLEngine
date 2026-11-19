package io.github.ephym6.plantid.ui.identify

import io.github.ephym6.plantid.domain.identification.FailureReason
import io.github.ephym6.plantid.domain.identification.IdentificationOutcome
import io.github.ephym6.plantid.domain.identification.IdentificationService
import io.github.ephym6.plantid.domain.identification.ModelInfo
import io.github.ephym6.plantid.ui.identify.IdentificationViewModel.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class IdentificationViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val photo = File("unused_by_fake.jpg")
    private val model = ModelInfo(name = "Fake", version = "test", isDemo = false)
    private val unsupported = IdentificationOutcome.Unsupported(model)

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private class FakeService(
        override val modelInfo: ModelInfo,
        var behaviour: suspend (File) -> IdentificationOutcome,
    ) : IdentificationService {
        var calls = 0
        override suspend fun identify(photo: File): IdentificationOutcome {
            calls++
            return behaviour(photo)
        }
    }

    private fun failed(reason: FailureReason) = UiState.Finished(IdentificationOutcome.Failed(model, reason))

    @Test
    fun `starts identifying immediately and shows the service outcome`() = runTest(dispatcher) {
        val service = FakeService(model) { delay(500); unsupported }
        val viewModel = IdentificationViewModel(photo, service)

        assertEquals(UiState.Identifying, viewModel.uiState.value)
        advanceUntilIdle()

        assertEquals(UiState.Finished(unsupported), viewModel.uiState.value)
        assertEquals(1, service.calls)
    }

    @Test
    fun `missing photo fails without calling the service`() = runTest(dispatcher) {
        val service = FakeService(model) { unsupported }
        val viewModel = IdentificationViewModel(photo = null, service = service)
        advanceUntilIdle()

        assertEquals(failed(FailureReason.PhotoUnavailable), viewModel.uiState.value)
        assertEquals(0, service.calls)
    }

    @Test
    fun `an exception becomes a processing failure, not a crash or fake success`() = runTest(dispatcher) {
        val service = FakeService(model) { error("inference exploded") }
        val viewModel = IdentificationViewModel(photo, service)
        advanceUntilIdle()

        assertEquals(failed(FailureReason.ProcessingFailed), viewModel.uiState.value)
    }

    @Test
    fun `a request that never finishes times out`() = runTest(dispatcher) {
        val service = FakeService(model) { awaitCancellation() }
        val viewModel = IdentificationViewModel(photo, service, timeoutMillis = 1_000)

        advanceTimeBy(999)
        runCurrent()
        assertEquals(UiState.Identifying, viewModel.uiState.value)

        advanceTimeBy(2)
        runCurrent()
        assertEquals(failed(FailureReason.TimedOut), viewModel.uiState.value)
    }

    @Test
    fun `retry while identifying is ignored`() = runTest(dispatcher) {
        val service = FakeService(model) { delay(500); unsupported }
        val viewModel = IdentificationViewModel(photo, service)

        viewModel.retry()
        viewModel.retry()
        advanceUntilIdle()

        assertEquals(1, service.calls)
    }

    @Test
    fun `retry after a failure runs again and can succeed`() = runTest(dispatcher) {
        val service = FakeService(model) { IdentificationOutcome.Failed(model, FailureReason.ProcessingFailed) }
        val viewModel = IdentificationViewModel(photo, service)
        advanceUntilIdle()

        service.behaviour = { delay(100); unsupported }
        viewModel.retry()
        assertEquals(UiState.Identifying, viewModel.uiState.value)
        advanceUntilIdle()

        assertEquals(UiState.Finished(unsupported), viewModel.uiState.value)
        assertEquals(2, service.calls)
    }
}
