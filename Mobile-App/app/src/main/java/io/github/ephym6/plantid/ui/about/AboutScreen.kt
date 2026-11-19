package io.github.ephym6.plantid.ui.about

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import io.github.ephym6.plantid.R
import io.github.ephym6.plantid.ui.components.PlantIdTopAppBar
import io.github.ephym6.plantid.ui.components.TextCard
import io.github.ephym6.plantid.ui.theme.PlantIDTheme
import io.github.ephym6.plantid.ui.theme.Spacing

/**
 * About: purpose, scope, prototype disclaimer, privacy and version.
 * The privacy text must be updated whenever photo handling or storage changes.
 */
@Composable
fun AboutScreen(
    versionName: String,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = { PlantIdTopAppBar(title = stringResource(R.string.about_title)) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            TextCard(
                title = stringResource(R.string.about_purpose_title),
                body = stringResource(R.string.about_purpose_body),
            )
            TextCard(
                title = stringResource(R.string.about_prototype_title),
                body = stringResource(R.string.about_prototype_body),
            )
            TextCard(
                title = stringResource(R.string.about_supported_title),
                body = stringResource(R.string.about_supported_body),
            )
            TextCard(
                title = stringResource(R.string.about_privacy_title),
                body = stringResource(R.string.about_privacy_body),
            )
            Text(
                text = stringResource(R.string.about_version, versionName),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = Spacing.sm),
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun AboutScreenPreview() {
    PlantIDTheme { AboutScreen(versionName = "1.0") }
}
