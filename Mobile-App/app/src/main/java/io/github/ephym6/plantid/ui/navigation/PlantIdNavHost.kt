package io.github.ephym6.plantid.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import io.github.ephym6.plantid.BuildConfig
import io.github.ephym6.plantid.ui.about.AboutScreen
import io.github.ephym6.plantid.ui.history.HistoryScreen
import io.github.ephym6.plantid.ui.home.HomeScreen

/** All app destinations. Screens receive navigation callbacks, never the NavController. */
@Composable
fun PlantIdNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = HomeRoute,
        modifier = modifier,
    ) {
        composable<HomeRoute> {
            HomeScreen(
                onOpenHistory = { navController.navigateToTopLevel(TopLevelDestination.History) },
            )
        }
        composable<HistoryRoute> {
            HistoryScreen()
        }
        composable<AboutRoute> {
            AboutScreen(versionName = BuildConfig.VERSION_NAME)
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

/** True if this destination is [destination] or nested inside it. */
fun NavDestination?.isTopLevel(destination: TopLevelDestination): Boolean =
    this?.hierarchy?.any { it.hasRoute(destination.route::class) } == true
