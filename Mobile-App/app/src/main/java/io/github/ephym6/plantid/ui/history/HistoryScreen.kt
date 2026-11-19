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
import io.github.ephym6.plantid.ui.theme.PlantIDTheme

/**
 * History (Sprint 1 placeholder): always shows the empty state. Saved records
 * arrive in Sprint 5. The "Identify a plant" action is added once that flow
 * exists (Sprint 2).
 */
@Composable
fun HistoryScreen(modifier: Modifier = Modifier) {
    Scaffold(
        modifier = modifier,
        topBar = { PlantIdTopAppBar(title = stringResource(R.string.history_title)) },
    ) { padding ->
        CenteredScrollableColumn(modifier = Modifier.padding(padding)) {
            EmptyState(
                iconRes = R.drawable.ic_history,
                title = stringResource(R.string.history_empty_title),
                message = stringResource(R.string.history_empty_message),
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun HistoryScreenPreview() {
    PlantIDTheme { HistoryScreen() }
}
