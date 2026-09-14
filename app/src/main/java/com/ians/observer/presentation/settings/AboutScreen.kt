package com.ians.observer.presentation.settings

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.ians.observer.BuildConfig
import com.ians.observer.R
import com.ians.observer.domain.model.ProviderId
import com.ians.observer.presentation.helper.openEmailComposer
import com.ians.observer.presentation.helper.openUriInBrowser
import com.ians.observer.presentation.helper.parseArticleUri
import com.ians.observer.presentation.mapper.aboutDescriptionRes
import com.ians.observer.presentation.mapper.titleRes
import com.ians.observer.presentation.mapper.termsUrlRes
import com.ians.observer.presentation.mapper.websiteUrlRes

@Composable
fun AboutScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val contactUrl = stringResource(R.string.url_contact)
    val email = stringResource(R.string.developer_email)
    val emailSubject = stringResource(R.string.about_email_subject)

    fun openUrl(url: String) {
        parseArticleUri(url)?.let { uri -> openUriInBrowser(uri, context) }
    }

    AboutScreenUI(
        versionName = BuildConfig.VERSION_NAME,
        providerIds = viewModel.availableProviderIds,
        onOpenUrl = ::openUrl,
        onEmailClick = {
            // No mail app installed: the contact page lists the same address.
            if (!openEmailComposer(email, emailSubject, context)) openUrl(contactUrl)
        }
    )
}

@Composable
private fun AboutScreenUI(
    versionName: String,
    providerIds: Set<ProviderId>,
    onOpenUrl: (String) -> Unit,
    onEmailClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = stringResource(R.string.about_version, versionName),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.size(4.dp))
            Text(stringResource(R.string.about_app_description))
        }

        AboutSection(R.string.about_independence_title) {
            Text(
                text = stringResource(R.string.about_independence_text),
                style = MaterialTheme.typography.bodyMedium
            )
        }

        AboutSection(R.string.about_news_sources) {
            Text(
                text = stringResource(R.string.about_news_attribution),
                style = MaterialTheme.typography.bodyMedium
            )

            ProviderId.entries.filter { it in providerIds }.forEach { providerId ->
                HorizontalDivider()
                ProviderBlock(providerId = providerId, onOpenUrl = onOpenUrl)
            }

            HorizontalDivider()
            Text(
                text = stringResource(
                    R.string.about_terms_last_reviewed,
                    stringResource(R.string.about_terms_last_reviewed_date)
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        AboutSection(R.string.about_legal_title) {
            Text(
                text = stringResource(R.string.about_contact_hint),
                style = MaterialTheme.typography.bodyMedium
            )

            val privacyUrl = stringResource(R.string.url_privacy_policy)
            val termsUrl = stringResource(R.string.url_terms_of_use)
            val contactUrl = stringResource(R.string.url_contact)
            val sourceUrl = stringResource(R.string.url_source_code)

            Column {
                AboutLinkRow(R.string.about_contact_email, onEmailClick)
                AboutLinkRow(R.string.about_contact_page) { onOpenUrl(contactUrl) }
                AboutLinkRow(R.string.about_privacy_policy) { onOpenUrl(privacyUrl) }
                AboutLinkRow(R.string.about_terms_of_use) { onOpenUrl(termsUrl) }
                AboutLinkRow(R.string.about_source_code) { onOpenUrl(sourceUrl) }
            }
        }

        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            text = stringResource(
                R.string.about_developer,
                stringResource(R.string.developer_name)
            ),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun AboutSection(
    @StringRes title: Int,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(title),
                style = MaterialTheme.typography.titleMedium
            )
            content()
        }
    }
}

@Composable
private fun ProviderBlock(
    providerId: ProviderId,
    onOpenUrl: (String) -> Unit,
) {
    val websiteUrl = stringResource(providerId.websiteUrlRes)
    val termsUrl = stringResource(providerId.termsUrlRes)

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = stringResource(providerId.titleRes),
            style = MaterialTheme.typography.titleSmall
        )
        Text(
            text = stringResource(providerId.aboutDescriptionRes),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = { onOpenUrl(websiteUrl) }) {
                Text(stringResource(R.string.about_provider_website))
            }
            TextButton(onClick = { onOpenUrl(termsUrl) }) {
                Text(stringResource(R.string.about_provider_terms))
            }
        }
    }
}

@Composable
private fun AboutLinkRow(
    @StringRes title: Int,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier.weight(1f),
            text = stringResource(title),
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.width(12.dp))
        Icon(
            modifier = Modifier.size(18.dp),
            painter = painterResource(R.drawable.ic_open_in_new),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
    }
}

@Preview(showBackground = true, heightDp = 1400)
@Composable
private fun AboutScreenUIPreview() {
    AboutScreenUI(
        versionName = "1.0",
        providerIds = setOf(ProviderId.NEWS_DATA, ProviderId.GDELT),
        onOpenUrl = {},
        onEmailClick = {}
    )
}
