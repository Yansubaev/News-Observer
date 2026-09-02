package com.ians.observer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ians.observer.presentation.articlepreview.ArticleDetailsSheet
import com.ians.observer.presentation.navigation.AppNavHost
import com.ians.observer.presentation.navigation.Screen
import com.ians.observer.presentation.navigation.SettingsScreen
import com.ians.observer.ui.theme.AppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val screens = listOf(Screen.Feed, Screen.Search, Screen.Favorites)
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    var selectedArticleId by rememberSaveable {
        mutableStateOf<String?>(null)
    }

    Box(Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                val name = when (currentRoute) {
                    Screen.Feed.route -> stringResource(R.string.app_name)
                    Screen.Favorites.route -> stringResource(R.string.nav_favorites)
                    Screen.Search.route -> stringResource(R.string.nav_search)
                    SettingsScreen.Main.route -> stringResource(R.string.settings)
                    SettingsScreen.FeedProviders.route -> stringResource(SettingsScreen.FeedProviders.name)
                    SettingsScreen.Region.route -> stringResource(SettingsScreen.Region.name)
                    SettingsScreen.Language.route -> stringResource(SettingsScreen.Language.name)
                    else -> ""
                }
                if (currentRoute in listOf(
                        Screen.Feed.route,
                        Screen.Favorites.route,
                    ) || currentRoute in SettingsScreen.routes
                )
                    TopAppBar(
                        title = { Text(name) },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            titleContentColor = MaterialTheme.colorScheme.primary
                        ),
                        scrollBehavior = scrollBehavior,
                        navigationIcon = {
                            if (currentRoute in SettingsScreen.routes) {
                                IconButton(
                                    onClick = {
                                        navController.popBackStack()
                                    },
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.arrow_back),
                                        contentDescription = stringResource(R.string.cd_settings),
                                    )
                                }
                            }
                        },
                        actions = {
                            if (currentRoute in listOf(Screen.Feed.route, Screen.Favorites.route)) {
                                IconButton(
                                    onClick = {
                                        navController.navigate(SettingsScreen.Main.route)
                                    },
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.settings),
                                        contentDescription = stringResource(R.string.cd_settings)
                                    )
                                }
                            }
                        })
            },
            bottomBar = {
                if (currentRoute in screens.map { it.route })
                    NavigationBar {

                        screens.forEach { screen ->
                            NavigationBarItem(
                                icon = {
                                    Icon(
                                        painter = painterResource(screen.iconRes),
                                        contentDescription = stringResource(screen.labelRes)
                                    )
                                },
                                label = { Text(stringResource(screen.labelRes)) },
                                selected = currentRoute == screen.route,
                                onClick = {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }

                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    indicatorColor = Color.Transparent
                                )
                            )
                        }

                    }
            }
        )
        { paddingValues ->
            AppNavHost(
                navController = navController,
                scrollBehavior = scrollBehavior,
                paddingValues = paddingValues,
                onArticleSelected = {
                    selectedArticleId = it.id
                }
            )
        }

        selectedArticleId?.let { articleId ->
            ArticleDetailsSheet(
                articleId = articleId,
                onDismissed = {
                    selectedArticleId = null
                }
            )
        }
    }
}
