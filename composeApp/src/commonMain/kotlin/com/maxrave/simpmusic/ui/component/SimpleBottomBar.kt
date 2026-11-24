package com.maxrave.simpmusic.ui.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.currentBackStackEntryAsState
import org.jetbrains.compose.resources.stringResource
import kotlin.reflect.KClass

/**
 * Lite fork's phone bottom bar: a plain Material 3 [NavigationBar] glued to the bottom edge, with
 * a label under every icon. It replaces the floating capsule + round search button, which covered
 * the list under it and needed its own layer, indicator drag handling and fold animation.
 *
 * Tabs: Home, Search, Library, and Mix for you while signed in to YouTube.
 */
@Composable
fun SimpleBottomBar(
    navController: NavController,
    showMixForYouTab: Boolean = false,
    reloadDestinationIfNeeded: (KClass<*>) -> Unit = { _ -> },
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val backStack by navController.currentBackStack.collectAsState()
    val tabs =
        listOfNotNull(
            BottomNavScreen.Home,
            BottomNavScreen.Search,
            BottomNavScreen.Library,
            BottomNavScreen.MixForYou.takeIf { showMixForYouTab },
        )
    // The tab the user is in: the nearest tab root at or below the current screen in the back stack. A
    // screen inside a tab (Settings under Home, an album under Library) belongs to the tab it was opened
    // from, which the destination hierarchy alone cannot tell.
    val currentTab =
        backStack
            .lastOrNull { entry -> tabs.any { entry.destination.hasRoute(it.destination::class) } }
            ?.let { entry -> tabs.firstOrNull { entry.destination.hasRoute(it.destination::class) } }
    NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceContainer) {
        tabs.forEach { screen ->
            val selected = currentTab == screen
            // On the tab's own root screen, tapping it reloads; on a screen inside the tab it pops back to
            // the root. Going through the save/restore navigation below would restore the screen just left.
            val atRoot = backStackEntry?.destination?.hasRoute(screen.destination::class) == true
            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (atRoot) {
                        reloadDestinationIfNeeded(screen.destination::class)
                    } else if (selected && navController.popBackStack(screen.destination, inclusive = false)) {
                        // Popped to the tab's root.
                    } else {
                        navController.navigate(screen.destination) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = screen.icon,
                label = {
                    Text(
                        text = stringResource(screen.title),
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 1,
                    )
                },
                alwaysShowLabel = true,
            )
        }
    }
}
