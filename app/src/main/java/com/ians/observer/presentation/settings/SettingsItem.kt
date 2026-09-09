package com.ians.observer.presentation.settings

sealed class SettingsItem {
    data class Navigation(
        val title: Int,
        val route: String,
        val onNavigate: (String) -> Unit = {},
        val iconRes: Int? = null,
        val iconContentDescription: Int? = null,
    ) : SettingsItem()

    data class Action(
        val title: Int,
        val onClick: () -> Unit = {},
        val enabled: Boolean = true,
        val iconRes: Int? = null,
        val iconContentDescription: Int? = null
    ) : SettingsItem()

    data class Toggle(
        val title: Int,
        val checked: Boolean,
        val onCheckedChange: (Boolean) -> Unit,
        val iconRes: Int? = null,
        val iconContentDescription: Int? = null
    ) : SettingsItem()

    data class Info(
        val title: Int,
        val value: String,
        val onClick: () -> Unit = {},
        val iconRes: Int? = null,
        val iconContentDescription: Int? = null
    ) : SettingsItem()

    data class Checkbox(
        val title: Int,
        val checked: Boolean,
        val onCheckedChange: (Boolean) -> Unit = {},
        val iconRes: Int? = null,
        val iconContentDescription: Int? = null
    ): SettingsItem()

    data class Radiobutton(
        val title: Int,
        val selected: Boolean,
        val onClick: () -> Unit = {},
        val iconRes: Int? = null,
        val iconContentDescription: Int? = null
    ): SettingsItem()
}
