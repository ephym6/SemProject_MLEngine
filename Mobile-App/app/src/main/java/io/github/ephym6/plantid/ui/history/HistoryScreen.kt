package io.github.ephym6.plantid.ui.history

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import io.github.ephym6.plantid.R
import io.github.ephym6.plantid.ui.components.CenteredScrollableColumn
import io.github.ephym6.plantid.ui.components.EmptyState
import io.github.ephym6.plantid.ui.components.PlantIdTopAppBar
import io.github.ephym6.plantid.ui.components.PrimaryButton
import io.github.ephym6.plantid.ui.theme.PlantIDTheme

/**
 * History (placeholder until Sprint 5): always shows the empty state, which
 * offers to start an identification.
 */
@Composable
fun HistoryScreen(
    onIdentifyPlant: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = { PlantIdTopAppBar(title = stringResource(R.string.history_title)) },
    ) { padding ->
        CenteredScrollableColumn(modifier = Modifier.padding(padding)) {
            EmptyState(
                iconRes = R.drawable.ic_history,
                title = stringResource(R.string.history_empty_title),
                message = stringResource(R.string.history_empty_message),
                action = {
                    PrimaryButton(
                        text = stringResource(R.string.action_identify_plant),
                        onClick = onIdentifyPlant,
                        iconRes = R.drawable.ic_photo_camera,
                    )
                },
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun HistoryScreenPreview() {
    PlantIDTheme { HistoryScreen(onIdentifyPlant = {}) }
}
