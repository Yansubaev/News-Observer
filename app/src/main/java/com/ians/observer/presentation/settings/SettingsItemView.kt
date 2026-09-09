package com.ians.observer.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ians.observer.R

@Preview
@Composable
fun SettingsItemNavigationViewPreview() {
    SettingsItemView(
        SettingsItem.Navigation(
            R.string.settings_feed_providers,
            "sources",
            iconRes = R.drawable.sources,
        )
    )
}

@Preview
@Composable
fun SettingsItemActionViewPreview() {
    SettingsItemView(
        SettingsItem.Action(
            R.string.settings_language,
            {},
            iconRes = R.drawable.language,
        )
    )
}

@Preview
@Composable
fun SettingsItemInfoViewPreview() {
    SettingsItemView(
        SettingsItem.Info(
            R.string.settings_about,
            "asd",
            iconRes = R.drawable.info,
        )
    )
}

@Preview
@Composable
fun SettingsItemToggleViewPreview() {
    SettingsItemView(
        SettingsItem.Toggle(
            R.string.settings_notifications,
            true,
            onCheckedChange = {},
            iconRes = R.drawable.notifications,
        )
    )
}

@Preview
@Composable
fun SettingsItemCheckboxViewPreview() {
    SettingsItemView(
        SettingsItem.Checkbox(
            R.string.provider_news_api,
            true,
            onCheckedChange = {},
        )
    )
}

@Preview
@Composable
fun SettingsItemRadioViewPreview() {
    SettingsItemView(
        SettingsItem.Radiobutton(
            R.string.settings_language,
            true,
            onClick = {},
        )
    )
}

@Composable
fun SettingsItemView(
    item: SettingsItem, modifier: Modifier = Modifier
) {
    when (item) {
        is SettingsItem.Action -> SettingsItemActionVariant(item, modifier)
        is SettingsItem.Info -> SettingsItemInfoVariant(item, modifier)
        is SettingsItem.Navigation -> SettingsItemNavigationVariant(item, modifier)
        is SettingsItem.Toggle -> SettingsItemToggleVariant(item, modifier)
        is SettingsItem.Checkbox -> SettingsItemCheckboxVariant(item, modifier)
        is SettingsItem.Radiobutton -> SettingsItemRadioVariant(item, modifier)
    }
}

@Composable
fun SettingsItemActionVariant(
    item: SettingsItem.Action, modifier: Modifier = Modifier
) {
    Row(
        modifier
            .alpha(if (item.enabled) 1f else 0.38f)
            .clickable(
                enabled = item.enabled,
                onClick = item.onClick,
                role = Role.Button,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spacer(modifier = Modifier.width(12.dp))
        item.iconRes?.let {
            SettingsIcon(it, item.iconContentDescription)
            Spacer(modifier = Modifier.width(16.dp))
        }

        Text(
            modifier = Modifier.weight(1f),
            text = stringResource(item.title)
        )
        Spacer(modifier = Modifier.width(12.dp))
    }
}

@Composable
fun SettingsItemInfoVariant(
    item: SettingsItem.Info, modifier: Modifier = Modifier
) {
    Row(
        modifier.clickable(
            onClick = item.onClick, role = Role.Button
        ), verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.width(12.dp))
        item.iconRes?.let {
            SettingsIcon(it, item.iconContentDescription)
            Spacer(modifier = Modifier.width(16.dp))
        }
        Text(
            modifier = Modifier.weight(1f),
            text = stringResource(item.title)
        )
        Spacer(modifier = Modifier.width(12.dp))
    }
}

@Composable
fun SettingsItemNavigationVariant(
    item: SettingsItem.Navigation, modifier: Modifier = Modifier
) {
    Row(
        modifier.clickable(
            onClick = {
                item.onNavigate(item.route)
            }, role = Role.Button
        ), verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.width(12.dp))
        item.iconRes?.let {
            SettingsIcon(it, item.iconContentDescription)
            Spacer(modifier = Modifier.width(16.dp))
        }
        Text(
            modifier = Modifier.weight(1f),
            text = stringResource(item.title)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Icon(
            modifier = Modifier.size(12.dp),
            painter = painterResource(R.drawable.arrow),
            contentDescription = "arrow",
            tint = MaterialTheme.colorScheme.surfaceVariant
        )
        Spacer(modifier = Modifier.width(12.dp))
    }
}

@Composable
fun SettingsItemToggleVariant(
    item: SettingsItem.Toggle, modifier: Modifier = Modifier
) {
    Row(
        modifier.toggleable(
            value = item.checked,
            interactionSource = null,
            enabled = true,
            role = Role.Switch,
            onValueChange = item.onCheckedChange,
        ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.width(12.dp))
        item.iconRes?.let {
            SettingsIcon(it, item.iconContentDescription)
            Spacer(modifier = Modifier.width(16.dp))
        }
        Text(
            modifier = Modifier.weight(1f),
            text = stringResource(item.title)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Switch(item.checked, onCheckedChange = null)
        Spacer(modifier = Modifier.width(12.dp))
    }
}

@Composable
fun SettingsItemCheckboxVariant(
    item: SettingsItem.Checkbox, modifier: Modifier = Modifier
) {
    Row(
        modifier.toggleable(
            value = item.checked,
            interactionSource = null,
            enabled = true,
            role = Role.Checkbox,
            onValueChange = item.onCheckedChange,
        ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.width(12.dp))
        item.iconRes?.let {
            SettingsIcon(it, item.iconContentDescription)
            Spacer(modifier = Modifier.width(16.dp))
        }
        Text(
            modifier = Modifier.weight(1f),
            text = stringResource(item.title)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Checkbox(item.checked, onCheckedChange = null)
        Spacer(modifier = Modifier.width(12.dp))
    }
}

@Composable
fun SettingsItemRadioVariant(
    item: SettingsItem.Radiobutton, modifier: Modifier = Modifier
) {
    Row(
        modifier.selectable(
            selected = item.selected,
            role = Role.RadioButton,
            interactionSource = null,
            onClick = item.onClick,
        ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.width(12.dp))
        item.iconRes?.let {
            SettingsIcon(it, item.iconContentDescription)
            Spacer(modifier = Modifier.width(16.dp))
        }
        Text(
            modifier = Modifier.weight(1f),
            text = stringResource(item.title)
        )
        Spacer(modifier = Modifier.width(16.dp))
        RadioButton(item.selected, null)
        Spacer(modifier = Modifier.width(12.dp))
    }
}

@Composable
fun SettingsIcon(
    id: Int,
    contentDescription: Int?,
) {
    Icon(
        modifier = Modifier
            .size(36.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(corner = CornerSize(8.dp))
            )
            .padding(8.dp),
        painter = painterResource(id),
        contentDescription = contentDescription?.let { stringResource(it) },
        tint = MaterialTheme.colorScheme.onSurfaceVariant
    )
}
