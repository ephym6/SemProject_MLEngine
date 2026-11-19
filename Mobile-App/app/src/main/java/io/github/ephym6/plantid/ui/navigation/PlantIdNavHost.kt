package io.github.ephym6.plantid.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import io.github.ephym6.plantid.BuildConfig
import io.github.ephym6.plantid.ui.about.AboutScreen
import io.github.ephym6.plantid.ui.history.HistoryScreen
import io.github.ephym6.plantid.ui.home.HomeScreen
import io.github.ephym6.plantid.ui.identify.PhotoPreviewScreen
import java.io.File

/**
 * All app destinations. Screens receive navigation callbacks, never the NavController.
 *
 * @param onIdentifyPlant opens the photo source chooser (handled at app level).
 * @param photoFileFor resolves a photo id from a route to its file.
 */
@Composable
fun PlantIdNavHost(
    navController: NavHostController,
    onIdentifyPlant: () -> Unit,
    photoFileFor: (photoId: String) -> File?,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = HomeRoute,
        modifier = modifier,
    ) {
        composable<HomeRoute> {
            HomeScreen(
                onIdentifyPlant = onIdentifyPlant,
                onOpenHistory = { navController.navigateToTopLevel(TopLevelDestination.History) },
            )
        }
        composable<HistoryRoute> {
            HistoryScreen(onIdentifyPlant = onIdentifyPlant)
        }
        composable<AboutRoute> {
            AboutScreen(versionName = BuildConfig.VERSION_NAME)
        }
        composable<PhotoPreviewRoute> { entry ->
            val photoId = entry.toRoute<PhotoPreviewRoute>().photoId
            PhotoPreviewScreen(
                photo = remember(photoId) { photoFileFor(photoId) },
                onBack = { navController.popBackStack() },
                onChooseAnother = onIdentifyPlant,
            )
        }
    }
}

/**
 * Switches bottom-bar tab. Keeps only Home underneath, so Back from any tab
 * returns to Home and Back from Home leaves the app. Each tab's state is saved
 * and restored when switching.
 */
fun NavHostController.navigateToTopLevel(destination: TopLevelDestination) {
    navigate(destination.route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

/**
 * Opens the preview for a newly selected photo. If a preview is already open
 * ("Choose another photo"), it is replaced, so Back still goes to where the
 * flow started.
 */
fun NavHostController.navigateToPhotoPreview(photoId: String) {
    navigate(PhotoPreviewRoute(photoId)) {
        popUpTo<PhotoPreviewRoute> { inclusive = true }
    }
}

/** True if this destination is [destination] or nested inside it. */
fun NavDestination?.isTopLevel(destination: TopLevelDestination): Boolean =
    this?.hierarchy?.any { it.hasRoute(destination.route::class) } == true
