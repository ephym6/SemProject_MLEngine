package io.github.ephym6.plantid.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import io.github.ephym6.plantid.R
import io.github.ephym6.plantid.ui.components.BrandHeader
import io.github.ephym6.plantid.ui.components.SecondaryButton
import io.github.ephym6.plantid.ui.components.StatusMessage
import io.github.ephym6.plantid.ui.components.StatusTone
import io.github.ephym6.plantid.ui.components.TextCard
import io.github.ephym6.plantid.ui.theme.PlantIDTheme
import io.github.ephym6.plantid.ui.theme.Spacing

/**
 * Home (Sprint 1 placeholder). The "Identify a plant" action and recent
 * identifications arrive in Sprint 2 and Sprint 5. Until then there is no
 * button that does nothing.
 */
@Composable
fun HomeScreen(
    onOpenHistory: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(modifier = modifier) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.xl),
            verticalArrangement = Arrangement.spacedBy(Spacing.lg),
        ) {
            BrandHeader()
            Text(
                text = stringResource(R.string.home_tagline),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            StatusMessage(
                tone = StatusTone.Info,
                title = stringResource(R.string.home_coming_soon_title),
                message = stringResource(R.string.home_coming_soon_message),
            )
            TextCard(
                title = stringResource(R.string.home_scope_title),
                body = stringResource(R.string.home_scope_message),
            )
            SecondaryButton(
                text = stringResource(R.string.home_view_history),
                onClick = onOpenHistory,
                iconRes = R.drawable.ic_history,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun HomeScreenPreview() {
    PlantIDTheme { HomeScreen(onOpenHistory = {}) }
}

@Preview(name = "Large font", fontScale = 1.6f, widthDp = 320)
@Composable
private fun HomeScreenLargeFontPreview() {
    PlantIDTheme { HomeScreen(onOpenHistory = {}) }
}
