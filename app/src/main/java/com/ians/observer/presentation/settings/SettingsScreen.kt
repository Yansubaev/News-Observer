package com.ians.observer.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import com.ians.observer.R
import com.ians.observer.domain.model.NewsCountry
import com.ians.observer.domain.model.NewsLanguage
import com.ians.observer.domain.model.ProviderId
import com.ians.observer.presentation.mapper.titleRes
import com.ians.observer.presentation.navigation.SettingsScreen

@Composable
fun MainSettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    var showClearCacheDialog by rememberSaveable {
        mutableStateOf(false)
    }

    val cacheClearedMessage = stringResource(R.string.settings_cache_cleared)
    val cachedClearFailedMessage = stringResource(R.string.settings_cache_clear_failed)

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(
        viewModel,
        lifecycleOwner,
        cacheClearedMessage,
        cachedClearFailedMessage
    ) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.events.collect { event ->
                val message = when (event) {
                    SettingsViewModel.SettingsEvent.CacheCleared -> cacheClearedMessage
                    SettingsViewModel.SettingsEvent.CacheClearFailed -> cachedClearFailedMessage
                }

                snackbarHostState.showSnackbar(message)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        MainSettingsScreenUI(
            onNavigate = {
                navController.navigate(it)
            },
            onClearCacheClick = {
                showClearCacheDialog = true
            }
        )


        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )

    }

    if (showClearCacheDialog) {
        ClearCacheDialog(
            onDismiss = {
                showClearCacheDialog = false
            },
            onConfirm = {
                showClearCacheDialog = false
                viewModel.clearCachedArticles()
            }
        )
    }
}

@Composable
fun FeedProviderSettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val selectedProviderId by viewModel.selectedFeedProvider.collectAsStateWithLifecycle()
    val availableProviderIds = viewModel.feedProviderIds

    val map = mutableMapOf<ProviderId, Boolean>()
    for (providerId in availableProviderIds) {
        map[providerId] = providerId == selectedProviderId
    }

    ProvidersSettingsScreenUI(map) { providerId ->
        viewModel.selectFeedProvider(providerId)
    }
}

@Composable
fun SearchProviderSettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val selectedProviderId by viewModel.selectedSearchProvider.collectAsStateWithLifecycle()
    val availableProviderIds = viewModel.searchProviderIds

    val map = mutableMapOf<ProviderId, Boolean>()
    for (providerId in availableProviderIds) {
        map[providerId] = providerId == selectedProviderId
    }

    ProvidersSettingsScreenUI(map) { providerId ->
        viewModel.selectSearchProvider(providerId)
    }
}

@Composable
fun LanguageSettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val selectedLanguage by viewModel.selectedLanguageState.collectAsState()
    LanguageSettingsScreenUI(
        selectedLanguage = selectedLanguage,
        onLanguageSelected = {
            viewModel.selectLanguage(it)
        }
    )
}

@Composable
fun RegionSettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val selectedRegion by viewModel.selectedRegionState.collectAsState()
    CountrySettingsScreenUI(
        selectedRegion = selectedRegion,
        onRegionSelected = {
            viewModel.selectCountry(it)
        }
    )
}

@Composable
private fun MainSettingsScreenUI(
    onNavigate: (String) -> Unit = {},
    onClearCacheClick: () -> Unit = {},
    onAchievementsToggled: (Boolean) -> Unit = {},
) {
    val mod = Modifier
        .fillMaxWidth()
        .height(68.dp)

    SettingsListView(
        listOf(
            SettingsItem.Navigation(
                title = R.string.settings_feed_providers,
                iconRes = R.drawable.sources,
                route = SettingsScreen.FeedProviders.route,
                onNavigate = onNavigate,
                iconContentDescription = R.string.cd_settings_sources
            ),
            SettingsItem.Navigation(
                title = R.string.settings_search_providers,
                iconRes = R.drawable.ic_search,
                route = SettingsScreen.SearchProviders.route,
                onNavigate = onNavigate,
                iconContentDescription = R.string.cd_settings_sources
            ),
            SettingsItem.Navigation(
                title = R.string.settings_language,
                iconRes = R.drawable.language,
                route = SettingsScreen.Language.route,
                onNavigate = onNavigate,
                iconContentDescription = R.string.cd_settings_language
            ),
            SettingsItem.Navigation(
                title = R.string.settings_region,
                iconRes = R.drawable.region,
                route = SettingsScreen.Region.route,
                onNavigate = onNavigate,
                iconContentDescription = R.string.cd_settings_region
            ),
            SettingsItem.Toggle(
                title = R.string.settings_notifications,
                iconRes = R.drawable.notifications,
                checked = false,
                onCheckedChange = {},
                iconContentDescription = R.string.cd_settings_notifications
            ),
            SettingsItem.Action(
                title = R.string.settings_clear_cache,
                iconRes = R.drawable.clear_cache,
                onClick = onClearCacheClick,
                iconContentDescription = R.string.cd_settings_clear_cache
            ),
            SettingsItem.Info(
                title = R.string.settings_about,
                iconRes = R.drawable.info,
                value = "",
                onClick = {},
                iconContentDescription = R.string.cd_settings_about
            ),
        ), modifier = mod
    )
}

@Composable
private fun ProvidersSettingsScreenUI(
    providers: Map<ProviderId, Boolean>,
    onProviderSelected: (ProviderId) -> Unit,
) {
    val mod = Modifier
        .fillMaxWidth()
        .height(68.dp)

    val list = providers.map { providerId ->
        SettingsItem.Radiobutton(
            title = providerId.key.titleRes,
            selected = providerId.value,
            onClick = {
                onProviderSelected(providerId.key)
            }
        )
    }

    SettingsListView(list = list, modifier = mod)
}

@Composable
private fun LanguageSettingsScreenUI(
    selectedLanguage: NewsLanguage,
    onLanguageSelected: (NewsLanguage) -> Unit = {}
) {
    val mod = Modifier
        .fillMaxWidth()
        .height(68.dp)

    SettingsListView(
        NewsLanguage.entries.map {
            SettingsItem.Radiobutton(
                title = it.titleRes,
                selected = it == selectedLanguage,
                onClick = {
                    onLanguageSelected(it)
                }
            )
        }, modifier = mod
    )
}

@Composable
private fun CountrySettingsScreenUI(
    selectedRegion: NewsCountry,
    onRegionSelected: (NewsCountry) -> Unit = {}
) {
    val mod = Modifier
        .fillMaxWidth()
        .height(68.dp)

    SettingsListView(
        NewsCountry.entries.map {
            SettingsItem.Radiobutton(
                title = it.titleRes,
                selected = it == selectedRegion,
                onClick = {
                    onRegionSelected(it)
                }
            )
        }, modifier = mod
    )
}


@Composable
fun SettingsListView(
    list: List<SettingsItem>, modifier: Modifier = Modifier
) {

    LazyColumn(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxSize()
            .padding(horizontal = 12.dp)
            .clip(RoundedCornerShape(16.dp))
    ) {
        items(
            count = list.size,
        ) { index ->
            SettingsItemView(list[index], modifier)
        }
    }
}

@Composable
fun ClearCacheDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(stringResource(R.string.settings_clear_cache_dialog_title))
        },
        text = {
            Text(stringResource(R.string.settings_clear_cache_dialog_message))
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.settings_clear_cache_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.settings_clear_cache_cancel))
            }
        }
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MainSettingsScreenUIPreview() {
    MainSettingsScreenUI()
}
