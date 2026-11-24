package com.maxrave.simpmusic.ui.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
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
    val tabs =
        listOfNotNull(
            BottomNavScreen.Home,
            BottomNavScreen.Search,
            BottomNavScreen.Library,
            BottomNavScreen.MixForYou.takeIf { showMixForYouTab },
        )
    NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceContainer) {
        tabs.forEach { screen ->
            val selected =
                backStackEntry?.destination?.hierarchy?.any {
                    it.hasRoute(screen.destination::class)
                } == true
            // On the tab's own root screen, tapping it reloads; on a screen inside the tab (Settings
            // under Home, say) it goes back to the root.
            val atRoot = backStackEntry?.destination?.hasRoute(screen.destination::class) == true
            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (atRoot) {
                        reloadDestinationIfNeeded(screen.destination::class)
                    } else if (selected && navController.popBackStack(screen.destination, inclusive = false)) {
                        // Inside this tab (Settings under Home): pop back to its root. Going through the
                        // save/restore navigation below would restore the very screen that was just popped.
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
