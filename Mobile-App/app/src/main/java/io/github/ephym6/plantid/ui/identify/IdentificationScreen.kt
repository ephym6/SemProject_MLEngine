package io.github.ephym6.plantid.ui.identify

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import io.github.ephym6.plantid.R
import io.github.ephym6.plantid.domain.identification.FailureReason
import io.github.ephym6.plantid.domain.identification.IdentificationOutcome
import io.github.ephym6.plantid.domain.identification.ModelInfo
import io.github.ephym6.plantid.domain.identification.PlantSummary
import io.github.ephym6.plantid.ui.components.BackNavigationButton
import io.github.ephym6.plantid.ui.components.CenteredScrollableColumn
import io.github.ephym6.plantid.ui.components.ErrorState
import io.github.ephym6.plantid.ui.components.LoadingIndicator
import io.github.ephym6.plantid.ui.components.PlantIdCard
import io.github.ephym6.plantid.ui.components.PlantIdTopAppBar
import io.github.ephym6.plantid.ui.components.PlantPhoto
import io.github.ephym6.plantid.ui.components.PrimaryButton
import io.github.ephym6.plantid.ui.components.SecondaryButton
import io.github.ephym6.plantid.ui.components.StatusMessage
import io.github.ephym6.plantid.ui.components.StatusTone
import io.github.ephym6.plantid.ui.components.TextCard
import io.github.ephym6.plantid.ui.identify.IdentificationViewModel.UiState
import io.github.ephym6.plantid.ui.theme.PlantIDTheme
import io.github.ephym6.plantid.ui.theme.Spacing
import java.io.File

/**
 * "Analyzing your plant…" followed by the outcome. Each outcome looks and reads
 * differently, and none relies on colour alone:
 * - Identified: photo + plant name together, success banner.
 * - Uncertain: warning banner, **no species name**.
 * - Unsupported: info banner, no plant at all.
 * - Failed: error panel with Try again where retrying can help.
 * Demo results always carry a "Demo result" banner.
 *
 * Identified results offer the care guide. Save arrives with history (Sprint 5)
 * and isn't shown as a button that does nothing.
 */
@Composable
fun IdentificationScreen(
    state: UiState,
    photo: File?,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onIdentifyAnother: () -> Unit,
    onBackToHome: () -> Unit,
    onViewCareGuide: (plantId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            PlantIdTopAppBar(
                title = stringResource(
                    if (state is UiState.Identifying) R.string.identifying_title else R.string.result_title,
                ),
                navigationIcon = { BackNavigationButton(onClick = onBack) },
            )
        },
    ) { padding ->
        when (state) {
            UiState.Identifying -> IdentifyingContent(photo, Modifier.padding(padding))
            is UiState.Finished -> ResultContent(
                outcome = state.outcome,
                photo = photo,
                onRetry = onRetry,
                onIdentifyAnother = onIdentifyAnother,
                onBackToHome = onBackToHome,
                onViewCareGuide = onViewCareGuide,
                modifier = Modifier.padding(padding),
            )
        }
    }
}

@Composable
private fun IdentifyingContent(photo: File?, modifier: Modifier) {
    CenteredScrollableColumn(modifier = modifier.padding(horizontal = Spacing.screenHorizontal)) {
        PlantPhoto(
            photo = photo,
            contentDescription = stringResource(R.string.result_photo_description),
            unavailableMessage = stringResource(R.string.photo_unavailable_short),
            aspectRatio = 1f,
            modifier = Modifier.widthIn(max = THUMBNAIL_MAX_WIDTH),
        )
        // No cancel button: Back leaves the screen, which safely cancels the request.
        LoadingIndicator(message = stringResource(R.string.identifying_message))
    }
}

@Composable
private fun ResultContent(
    outcome: IdentificationOutcome,
    photo: File?,
    onRetry: () -> Unit,
    onIdentifyAnother: () -> Unit,
    onBackToHome: () -> Unit,
    onViewCareGuide: (plantId: String) -> Unit,
    modifier: Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.lg),
    ) {
        if (outcome.model.isDemo) {
            StatusMessage(
                tone = StatusTone.Warning,
                title = stringResource(R.string.demo_banner_title),
                message = stringResource(R.string.demo_banner_message),
            )
        }
        when (outcome) {
            is IdentificationOutcome.Identified -> IdentifiedContent(outcome.plant, photo)
            is IdentificationOutcome.Uncertain -> NoMatchContent(
                photo = photo,
                tone = StatusTone.Warning,
                title = stringResource(R.string.result_uncertain_title),
                message = stringResource(R.string.result_uncertain_message),
            )
            is IdentificationOutcome.Unsupported -> NoMatchContent(
                photo = photo,
                tone = StatusTone.Info,
                title = stringResource(R.string.result_unsupported_title),
                message = stringResource(R.string.result_unsupported_message),
            )
            is IdentificationOutcome.Failed -> FailedContent(outcome.reason, onRetry, onIdentifyAnother, onBackToHome)
        }
        if (outcome is IdentificationOutcome.Identified) {
            PrimaryButton(
                text = stringResource(R.string.action_view_care_guide),
                onClick = { onViewCareGuide(outcome.plant.plantId) },
                iconRes = R.drawable.ic_eco,
                modifier = Modifier.fillMaxWidth(),
            )
            SecondaryButton(
                text = stringResource(R.string.action_identify_another),
                onClick = onIdentifyAnother,
                iconRes = R.drawable.ic_photo_camera,
                modifier = Modifier.fillMaxWidth(),
            )
        } else if (outcome !is IdentificationOutcome.Failed) {
            PrimaryButton(
                text = stringResource(R.string.action_try_another_photo),
                onClick = onIdentifyAnother,
                iconRes = R.drawable.ic_photo_camera,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (outcome !is IdentificationOutcome.Failed) {
            SecondaryButton(
                text = stringResource(R.string.action_back_to_home),
                onClick = onBackToHome,
                iconRes = R.drawable.ic_home,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun ColumnScope.IdentifiedContent(plant: PlantSummary, photo: File?) {
    // Photo and name in one card so they are visually associated.
    PlantIdCard {
        PlantPhoto(
            photo = photo,
            contentDescription = stringResource(R.string.result_photo_description),
            unavailableMessage = stringResource(R.string.photo_unavailable_short),
            aspectRatio = 4f / 3f,
        )
        Text(
            text = plant.commonName ?: plant.scientificName ?: stringResource(R.string.result_name_fallback),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier
                .padding(top = Spacing.sm)
                .semantics { heading() },
        )
        if (plant.commonName != null && plant.scientificName != null) {
            Text(
                text = plant.scientificName,
                style = MaterialTheme.typography.bodyLarge,
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
    StatusMessage(
        tone = StatusTone.Success,
        title = stringResource(R.string.result_identified_title),
        message = stringResource(R.string.result_identified_message),
        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
    )
    plant.description?.let { description ->
        TextCard(title = stringResource(R.string.result_description_title), body = description)
    }
}

@Composable
private fun NoMatchContent(photo: File?, tone: StatusTone, title: String, message: String) {
    PlantPhoto(
        photo = photo,
        contentDescription = stringResource(R.string.result_photo_description),
        unavailableMessage = stringResource(R.string.photo_unavailable_short),
        aspectRatio = 4f / 3f,
    )
    StatusMessage(
        tone = tone,
        title = title,
        message = message,
        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
    )
    Text(
        text = stringResource(R.string.home_scope_message),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun FailedContent(
    reason: FailureReason,
    onRetry: () -> Unit,
    onIdentifyAnother: () -> Unit,
    onBackToHome: () -> Unit,
) {
    val photoGone = reason == FailureReason.PhotoUnavailable
    ErrorState(
        title = stringResource(R.string.result_failed_title),
        message = stringResource(
            when (reason) {
                FailureReason.PhotoUnavailable -> R.string.result_failed_photo
                FailureReason.ModelUnavailable -> R.string.result_failed_model
                FailureReason.ProcessingFailed -> R.string.result_failed_processing
                FailureReason.TimedOut -> R.string.result_failed_timed_out
            },
        ),
        // Retrying can't bring a missing photo back; offer a new photo instead.
        onRetry = if (photoGone) onIdentifyAnother else onRetry,
        retryLabel = stringResource(if (photoGone) R.string.action_try_another_photo else R.string.action_try_again),
        secondaryAction = {
            TextButton(onClick = onBackToHome) { Text(stringResource(R.string.action_back_to_home)) }
        },
        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
    )
}

private val THUMBNAIL_MAX_WIDTH = 200.dp

// ---- Previews (fictional demo data, no photo) ----

private val previewModel = ModelInfo(name = "Preview", version = null, isDemo = true)
private val previewPlant = PlantSummary("preview_plant", "Preview Plant", "Preview species (fictional)", "Preview description.")
private val previewPlantMinimal = PlantSummary("preview_minimal", "Preview Plant", null, null)

@Composable
private fun ResultPreview(state: UiState) {
    PlantIDTheme {
        IdentificationScreen(
            state = state,
            photo = null,
            onBack = {},
            onRetry = {},
            onIdentifyAnother = {},
            onBackToHome = {},
            onViewCareGuide = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun IdentifyingPreview() = ResultPreview(UiState.Identifying)

@PreviewLightDark
@Composable
private fun IdentifiedPreview() = ResultPreview(
    UiState.Finished(IdentificationOutcome.Identified(previewModel, "preview_class", previewPlant)),
)

@PreviewLightDark
@Composable
private fun IdentifiedMissingDetailsPreview() = ResultPreview(
    UiState.Finished(IdentificationOutcome.Identified(previewModel, "preview_class", previewPlantMinimal)),
)

@PreviewLightDark
@Composable
private fun UncertainPreview() = ResultPreview(UiState.Finished(IdentificationOutcome.Uncertain(previewModel)))

@PreviewLightDark
@Composable
private fun UnsupportedPreview() = ResultPreview(UiState.Finished(IdentificationOutcome.Unsupported(previewModel)))

@PreviewLightDark
@Composable
private fun FailedPreview() = ResultPreview(
    UiState.Finished(IdentificationOutcome.Failed(previewModel, FailureReason.ProcessingFailed)),
)
