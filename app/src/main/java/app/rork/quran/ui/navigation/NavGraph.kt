package app.rork.quran.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.lifecycle.Lifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import app.rork.quran.ui.screen.bookmarks.BookmarksScreen
import app.rork.quran.ui.screen.home.HomeScreen
import app.rork.quran.ui.screen.settings.SettingsScreen

object Routes {
    const val HOME      = "home"
    const val BOOKMARKS = "bookmarks"
    const val SETTINGS  = "settings"
}

private const val ANIM_DURATION = 350

@Composable
fun QuranNavGraph(requestedAyahId: String? = null, onAyahHandled: () -> Unit = {}) {
    val navController = rememberNavController()
    androidx.compose.runtime.LaunchedEffect(requestedAyahId) {
        if (requestedAyahId != null) {
            navController.popBackStack(Routes.HOME, false)
        }
    }
    fun caNavigate(): Boolean {
        return navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED;
    }

    NavHost(
        navController    = navController,
        startDestination = Routes.HOME,
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = tween(ANIM_DURATION),
            )
        },
        exitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = tween(ANIM_DURATION),
            )
        },
        popEnterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = tween(ANIM_DURATION),
            )
        },
        popExitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = tween(ANIM_DURATION),
            )
        },
    ) {
        composable(Routes.HOME) {
            HomeScreen(
                requestedAyahId = requestedAyahId,
                onAyahHandled = onAyahHandled,
                onNavigateToSettings = {
                    if (caNavigate()) {
                        navController.navigate(Routes.SETTINGS) { launchSingleTop = true }
                    }
                },
                onNavigateToBookmarks = {
                    if (caNavigate()) {
                        navController.navigate(Routes.BOOKMARKS) { launchSingleTop = true }
                    }
                },
            )
        }
        composable(Routes.BOOKMARKS) {
            BookmarksScreen(onBack = {
                if (caNavigate()) {
                    navController.popBackStack()
                }
            })
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(onBack = {
                if (caNavigate()) {
                    navController.popBackStack()
                }
            })
        }
    }
}