package io.github.ephym6.plantid.ui.identify

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import io.github.ephym6.plantid.R
import io.github.ephym6.plantid.ui.components.BackNavigationButton
import io.github.ephym6.plantid.ui.components.PlantIdCard
import io.github.ephym6.plantid.ui.components.PlantIdTopAppBar
import io.github.ephym6.plantid.ui.components.PrimaryButton
import io.github.ephym6.plantid.ui.components.SecondaryButton
import io.github.ephym6.plantid.ui.components.StatusMessage
import io.github.ephym6.plantid.ui.components.StatusTone
import io.github.ephym6.plantid.ui.theme.PlantIDTheme
import io.github.ephym6.plantid.ui.theme.Sizes
import io.github.ephym6.plantid.ui.theme.Spacing
import java.io.File

/**
 * Lets the user check the selected photo before identifying.
 *
 * Sprint 2: "Identify plant" is shown but disabled, with an explanation. It is
 * connected to the (mock) identification service in Sprint 3.
 *
 * @param photo the stored photo, or null if the id was invalid (shown as an error).
 */
@Composable
fun PhotoPreviewScreen(
    photo: File?,
    onBack: () -> Unit,
    onChooseAnother: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            PlantIdTopAppBar(
                title = stringResource(R.string.preview_title),
                navigationIcon = { BackNavigationButton(onClick = onBack) },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.lg),
        ) {
            PhotoFrame(photo = photo)
            Text(
                text = stringResource(R.string.preview_instruction),
                style = MaterialTheme.typography.titleMedium,
            )
            PhotoTips()
            StatusMessage(
                tone = StatusTone.Info,
                title = stringResource(R.string.preview_coming_soon_title),
                message = stringResource(R.string.preview_coming_soon_message),
            )
            PrimaryButton(
                text = stringResource(R.string.preview_identify),
                onClick = {},
                enabled = false,
                modifier = Modifier.fillMaxWidth(),
            )
            SecondaryButton(
                text = stringResource(R.string.preview_choose_another),
                onClick = onChooseAnother,
                iconRes = R.drawable.ic_photo_library,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

/**
 * Shows the photo scaled to fit (never cropped or stretched). Coil decodes it
 * off the main thread at display size, not full resolution, and applies the
 * camera's EXIF rotation.
 */
@Composable
private fun PhotoFrame(photo: File?) {
    var imageState by remember(photo) { mutableStateOf<AsyncImagePainter.State>(AsyncImagePainter.State.Empty) }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(PHOTO_FRAME_ASPECT_RATIO)
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh),
        contentAlignment = Alignment.Center,
    ) {
        if (photo != null) {
            AsyncImage(
                model = photo,
                contentDescription = stringResource(R.string.preview_image_description),
                contentScale = ContentScale.Fit,
                onState = { imageState = it },
                modifier = Modifier.fillMaxSize(),
            )
        }
        when {
            photo == null || imageState is AsyncImagePainter.State.Error -> PhotoUnavailable()
            imageState is AsyncImagePainter.State.Loading -> CircularProgressIndicator()
        }
    }
}

@Composable
private fun PhotoUnavailable() {
    Column(
        modifier = Modifier.padding(Spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_error),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(Sizes.iconLarge),
        )
        Text(
            text = stringResource(R.string.preview_image_error),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun PhotoTips() {
    val tips = listOf(
        R.string.preview_tip_focus,
        R.string.preview_tip_leaves,
        R.string.preview_tip_light,
        R.string.preview_tip_single,
    )
    PlantIdCard {
        Text(
            text = stringResource(R.string.preview_tips_title),
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.semantics { heading() },
        )
        tips.forEach { tipRes ->
            Text(
                text = "•  " + stringResource(tipRes),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private const val PHOTO_FRAME_ASPECT_RATIO = 4f / 5f

@PreviewLightDark
@Composable
private fun PhotoPreviewScreenPreview() {
    // No real file in previews: shows the "photo unavailable" state.
    PlantIDTheme {
        PhotoPreviewScreen(photo = null, onBack = {}, onChooseAnother = {})
    }
}
