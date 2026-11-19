package io.github.ephym6.plantid.ui.identify

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import io.github.ephym6.plantid.domain.identification.FailureReason
import io.github.ephym6.plantid.domain.identification.IdentificationOutcome
import io.github.ephym6.plantid.domain.identification.IdentificationService
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import java.io.File

/**
 * Runs one identification for one photo. Scoped to the identification screen's
 * back-stack entry: it starts exactly once (rotation doesn't restart it),
 * leaving the screen cancels it, and [retry] is ignored while a request is
 * running, so repeated taps never start concurrent work.
 *
 * Any failure, including an exception or a timeout, becomes
 * [IdentificationOutcome.Failed], never a fake success.
 */
class IdentificationViewModel(
    private val photo: File?,
    private val service: IdentificationService,
    private val timeoutMillis: Long = TIMEOUT_MILLIS,
) : ViewModel() {

    sealed interface UiState {
        data object Identifying : UiState
        data class Finished(val outcome: IdentificationOutcome) : UiState
    }

    private val _uiState = MutableStateFlow<UiState>(UiState.Identifying)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        identify()
    }

    fun retry() {
        if (_uiState.value is UiState.Identifying) return
        identify()
    }

    private fun identify() {
        _uiState.value = UiState.Identifying
        viewModelScope.launch {
            _uiState.value = UiState.Finished(runIdentification())
        }
    }

    private suspend fun runIdentification(): IdentificationOutcome {
        val photo = photo ?: return failed(FailureReason.PhotoUnavailable)
        return try {
            withTimeoutOrNull(timeoutMillis) { service.identify(photo) }
                ?: failed(FailureReason.TimedOut)
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            // Deliberately not logged: no approved diagnostics approach yet (spec §6.7).
            failed(FailureReason.ProcessingFailed)
        }
    }

    private fun failed(reason: FailureReason) = IdentificationOutcome.Failed(service.modelInfo, reason)

    companion object {
        /**
         * Safety net against an endless loading state. Provisional: set it from
         * measured on-device inference time in Sprint 7 (spec: measure, don't invent).
         */
        const val TIMEOUT_MILLIS = 30_000L

        fun factory(photo: File?, service: IdentificationService): ViewModelProvider.Factory =
            viewModelFactory { initializer { IdentificationViewModel(photo, service) } }
    }
}
