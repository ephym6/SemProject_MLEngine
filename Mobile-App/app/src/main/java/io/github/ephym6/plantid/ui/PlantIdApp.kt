package io.github.ephym6.plantid.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.navigation.NavDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import io.github.ephym6.plantid.ui.navigation.PlantIdNavHost
import io.github.ephym6.plantid.ui.navigation.TopLevelDestination
import io.github.ephym6.plantid.ui.navigation.isTopLevel
import io.github.ephym6.plantid.ui.navigation.navigateToTopLevel
import io.github.ephym6.plantid.ui.theme.PlantIDTheme

/**
 * App shell: bottom navigation for top-level destinations plus the navigation host.
 *
 * Insets: this outer Scaffold only reserves space for the bottom bar (which
 * handles the system navigation bar itself). Each screen's own Scaffold handles
 * the status bar, so top bars can draw edge-to-edge without double padding.
 */
@Composable
fun PlantIdApp(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    // The bar is hidden on non-tab screens (the identification flow, from Sprint 2).
    val showBottomBar = currentDestination == null ||
        TopLevelDestination.entries.any { currentDestination.isTopLevel(it) }

    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (showBottomBar) {
                PlantIdBottomBar(
                    currentDestination = currentDestination,
                    onNavigate = navController::navigateToTopLevel,
                )
            }
        },
    ) { innerPadding ->
        PlantIdNavHost(
            navController = navController,
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding),
        )
    }
}

@Composable
private fun PlantIdBottomBar(
    currentDestination: NavDestination?,
    onNavigate: (TopLevelDestination) -> Unit,
) {
    NavigationBar {
        TopLevelDestination.entries.forEach { destination ->
            NavigationBarItem(
                selected = currentDestination.isTopLevel(destination),
                onClick = { onNavigate(destination) },
                // Label is always visible and is what screen readers announce.
                icon = { Icon(painterResource(destination.iconRes), contentDescription = null) },
                label = { Text(stringResource(destination.labelRes)) },
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun PlantIdAppPreview() {
    PlantIDTheme {
        PlantIdApp()
    }
}
