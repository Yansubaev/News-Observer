package com.ians.observer.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModelStoreOwner
import androidx.navigation.NavController
import com.ians.observer.R
import com.ians.observer.domain.model.NewsLanguage
import com.ians.observer.domain.model.NewsRegion
import com.ians.observer.domain.model.Source
import com.ians.observer.presentation.navigation.SettingsScreen

@Composable
fun MainSettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    MainSettingsScreenUI(
        onNavigate = {
            navController.navigate(it)
        }
    )
}

@Composable
fun SourcesSettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    SourcesSettingsScreenUI(
        onSourceSelected = { source, selected ->

        }
    )
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
    RegionSettingsScreenUI(
        selectedRegion = selectedRegion,
        onRegionSelected = {
            viewModel.selectRegion(it)
        }
    )
}

@Composable
private fun MainSettingsScreenUI(
    onNavigate: (String) -> Unit = {},
    onAchievementsToggled: (Boolean) -> Unit = {},
) {
    val mod = Modifier
        .fillMaxWidth()
        .height(68.dp)

    SettingsListView(
        listOf(
            SettingsItem.Navigation(
                title = R.string.settings_sources,
                iconRes = R.drawable.sources,
                route = SettingsScreen.Sources.route,
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
                onClick = {},
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
private fun SourcesSettingsScreenUI(
    onSourceSelected: (String, Boolean) -> Unit,
) {
    val mod = Modifier
        .fillMaxWidth()
        .height(68.dp)

    SettingsListView(
        listOf(
            SettingsItem.Checkbox(
                title = Source.NEWS_API.nameRes,
                checked = true,
                onCheckedChange = {
                    onSourceSelected(Source.NEWS_API.value, it)
                },
            )
        ), modifier = mod
    )
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
private fun RegionSettingsScreenUI(
    selectedRegion: NewsRegion,
    onRegionSelected: (NewsRegion) -> Unit = {}
) {
    val mod = Modifier
        .fillMaxWidth()
        .height(68.dp)

    SettingsListView(
        NewsRegion.entries.map {
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

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MainSettingsScreenUIPreview() {
    MainSettingsScreenUI()
}

