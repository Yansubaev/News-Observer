package com.ians.observer.presentation.settings

import android.Manifest
import android.os.Build
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import com.ians.observer.BuildConfig
import com.ians.observer.R
import com.ians.observer.domain.model.NewsCountry
import com.ians.observer.domain.model.NewsLanguage
import com.ians.observer.domain.model.ProviderId
import com.ians.observer.presentation.helper.canPostNotifications
import com.ians.observer.presentation.helper.openNotificationSettings
import com.ians.observer.presentation.helper.openUriInBrowser
import com.ians.observer.presentation.helper.parseArticleUri
import com.ians.observer.presentation.mapper.titleRes
import com.ians.observer.presentation.mapper.websiteUrlRes
import com.ians.observer.presentation.navigation.SettingsScreen

@Composable
fun MainSettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val activity = LocalActivity.current

    var showClearCacheDialog by rememberSaveable {
        mutableStateOf(false)
    }

    var showAboutDialog by rememberSaveable {
        mutableStateOf(false)
    }

    var awaitingNotificationSettingsResult by rememberSaveable {
        mutableStateOf(false)
    }

    val cacheClearedMessage = stringResource(R.string.settings_cache_cleared)
    val cachedClearFailedMessage = stringResource(R.string.settings_cache_clear_failed)
    val testNotificationScheduledMessage =
        stringResource(R.string.settings_test_notification_scheduled)

    val snackbarHostState = remember { SnackbarHostState() }

    val notificationPermissionRequested by viewModel.notificationPermissionRequestedState
        .collectAsStateWithLifecycle()

    val notificationsEnabled by viewModel.notificationsEnabledState.collectAsStateWithLifecycle()

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.setNotificationEnabled(true)
        } else {
            viewModel.setNotificationEnabled(false)
        }
    }


    LaunchedEffect(
        viewModel,
        lifecycleOwner,
        cacheClearedMessage,
        cachedClearFailedMessage,
        testNotificationScheduledMessage,
    ) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.events.collect { event ->
                val message = when (event) {
                    SettingsViewModel.SettingsEvent.CacheCleared -> cacheClearedMessage
                    SettingsViewModel.SettingsEvent.CacheClearFailed -> cachedClearFailedMessage
                    SettingsViewModel.SettingsEvent.TestNotificationScheduled ->
                        testNotificationScheduledMessage
                }

                snackbarHostState.showSnackbar(message)
            }
        }
    }

    LaunchedEffect(
        viewModel,
        lifecycleOwner
    ) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            val canPost = context.canPostNotifications()

            when {
                awaitingNotificationSettingsResult -> {
                    viewModel.setNotificationEnabled(canPost)
                    awaitingNotificationSettingsResult = false
                }

                !canPost -> {
                    viewModel.setNotificationEnabled(false)
                }
            }
        }
    }

    fun onNotificationToggled(enabled: Boolean) {
        if (!enabled) {
            viewModel.setNotificationEnabled(false)
        } else {
            when {
                context.canPostNotifications() -> {
                    viewModel.setNotificationEnabled(true)
                }

                Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                        !notificationPermissionRequested -> {
                    viewModel.markNotificationPermissionRequested()
                    notificationPermissionLauncher.launch(
                        Manifest.permission.POST_NOTIFICATIONS
                    )
                }

                Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                        activity != null &&
                        ActivityCompat.shouldShowRequestPermissionRationale(
                            /* activity = */ activity,
                            /* permission = */ Manifest.permission.POST_NOTIFICATIONS
                        ) -> {
                    notificationPermissionLauncher.launch(
                        Manifest.permission.POST_NOTIFICATIONS
                    )
                }

                else -> {
                    awaitingNotificationSettingsResult = true
                    context.openNotificationSettings()
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        MainSettingsScreenUI(
            notificationsEnabled = notificationsEnabled,
            onNavigate = {
                navController.navigate(it)
            },
            onClearCacheClick = {
                showClearCacheDialog = true
            },
            onNotificationsToggled = ::onNotificationToggled,
            onTestNotificationClick = viewModel::sendTestNotification,
            onAboutClicked = {
                showAboutDialog = true
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

    if (showAboutDialog) {
        val providerSiteUrls = viewModel.availableProviderIds.associateWith { providerId ->
            stringResource(providerId.websiteUrlRes)
        }

        AboutAlertDialog(
            versionName = BuildConfig.VERSION_NAME,
            providerIds = viewModel.availableProviderIds,
            onOpenProviderSite = { providerId ->
                providerSiteUrls[providerId]?.let(::parseArticleUri)?.let { uri ->
                    openUriInBrowser(uri, context)
                }
            },
            onDismiss = {
                showAboutDialog = false
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
    val enabledProviderIds by viewModel.enabledSearchProviderIds.collectAsStateWithLifecycle()

    SearchProvidersSettingsScreenUI(
        providerIds = viewModel.searchProviderIds,
        enabledProviderIds = enabledProviderIds,
        onProviderEnabledChange = viewModel::setSearchProviderEnabled
    )
}

@Composable
private fun SearchProvidersSettingsScreenUI(
    providerIds: Set<ProviderId>,
    enabledProviderIds: Set<ProviderId>,
    onProviderEnabledChange: (ProviderId, Boolean) -> Unit,
) {
    val modifier = Modifier
        .fillMaxWidth()
        .height(68.dp)

    val list = ProviderId.entries
        .filter { it in providerIds }
        .map { providerId ->
            SettingsItem.Checkbox(
                title = providerId.titleRes,
                checked = providerId in enabledProviderIds,
                enabled = providerId !in enabledProviderIds || enabledProviderIds.size > 1,
                onCheckedChange = { enabled ->
                    onProviderEnabledChange(providerId, enabled)
                }
            )
        }

    SettingsListView(list = list, modifier = modifier)
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
    notificationsEnabled: Boolean,
    onNavigate: (String) -> Unit = {},
    onClearCacheClick: () -> Unit = {},
    onNotificationsToggled: (Boolean) -> Unit = {},
    onTestNotificationClick: () -> Unit = {},
    onAboutClicked: () -> Unit = {}
) {
    val mod = Modifier
        .fillMaxWidth()
        .height(68.dp)

    val items = buildList {
        add(
            SettingsItem.Navigation(
                title = R.string.settings_feed_providers,
                iconRes = R.drawable.sources,
                route = SettingsScreen.FeedProviders.route,
                onNavigate = onNavigate,
                iconContentDescription = R.string.cd_settings_sources
            )
        )
        add(
            SettingsItem.Navigation(
                title = R.string.settings_search_providers,
                iconRes = R.drawable.ic_search,
                route = SettingsScreen.SearchProviders.route,
                onNavigate = onNavigate,
                iconContentDescription = R.string.cd_settings_sources
            )
        )
        add(
            SettingsItem.Navigation(
                title = R.string.settings_language,
                iconRes = R.drawable.language,
                route = SettingsScreen.Language.route,
                onNavigate = onNavigate,
                iconContentDescription = R.string.cd_settings_language
            )
        )
        add(
            SettingsItem.Navigation(
                title = R.string.settings_region,
                iconRes = R.drawable.region,
                route = SettingsScreen.Region.route,
                onNavigate = onNavigate,
                iconContentDescription = R.string.cd_settings_region
            )
        )
        add(
            SettingsItem.Toggle(
                title = R.string.settings_notifications,
                iconRes = R.drawable.notifications,
                checked = notificationsEnabled,
                onCheckedChange = onNotificationsToggled,
                iconContentDescription = R.string.cd_settings_notifications
            )
        )
        if (BuildConfig.DEBUG) {
            add(
                SettingsItem.Action(
                    title = R.string.settings_test_notification,
                    iconRes = R.drawable.notifications,
                    onClick = onTestNotificationClick,
                    enabled = notificationsEnabled,
                    iconContentDescription = R.string.cd_settings_test_notification,
                )
            )
        }
        add(
            SettingsItem.Action(
                title = R.string.settings_clear_cache,
                iconRes = R.drawable.clear_cache,
                onClick = onClearCacheClick,
                iconContentDescription = R.string.cd_settings_clear_cache
            )
        )
        add(
            SettingsItem.Info(
                title = R.string.settings_about,
                iconRes = R.drawable.info,
                value = "",
                onClick = onAboutClicked,
                iconContentDescription = R.string.cd_settings_about
            )
        )
    }

    SettingsListView(
        list = items,
        modifier = mod,
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

@Composable
fun AboutAlertDialog(
    versionName: String,
    providerIds: Set<ProviderId>,
    onOpenProviderSite: (ProviderId) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(stringResource(R.string.app_name))
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(stringResource(R.string.about_app_description))

                Text(
                    text = stringResource(
                        R.string.about_version,
                        versionName
                    ),
                    style = MaterialTheme.typography.bodySmall
                )

                HorizontalDivider()

                Text(
                    text = stringResource(R.string.about_news_sources),
                    style = MaterialTheme.typography.titleSmall
                )

                Text(
                    text = providerIds
                        .map { providerId -> stringResource(providerId.titleRes) }
                        .joinToString(separator = " • ")
                )

                Text(
                    text = stringResource(R.string.about_news_attribution),
                    style = MaterialTheme.typography.bodySmall
                )

                if (ProviderId.GDELT in providerIds) {
                    Text(
                        modifier = Modifier.clickable(
                            role = Role.Button,
                            onClick = { onOpenProviderSite(ProviderId.GDELT) }
                        ),
                        text = stringResource(R.string.about_gdelt_attribution),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary,
                        textDecoration = TextDecoration.Underline
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.about_close))
            }
        }
    )

}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MainSettingsScreenUIPreview() {
    MainSettingsScreenUI(notificationsEnabled = true)
}
