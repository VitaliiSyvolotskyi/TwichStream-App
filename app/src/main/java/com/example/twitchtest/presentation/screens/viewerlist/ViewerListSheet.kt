package com.example.twitchtest.presentation.screens.viewerlist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.twitchtest.R
import com.example.twitchtest.domain.model.User

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewerListSheet(
    onDismiss: () -> Unit,
    viewModel: ViewerListViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.viewers_count, state.viewers.size),
                style = MaterialTheme.typography.headlineSmall
            )

            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { viewModel.onIntent(ViewerListIntent.Search(it)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.search_viewers)) },
                singleLine = true
            )

            if (state.isLoading && state.viewers.isEmpty()) {
                ViewerListLoading()
            } else if (state.viewers.isNotEmpty()) {
                ViewerList(
                    viewers = state.viewers,
                    modifier = Modifier.weight(1f)
                )
            }

            state.error?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
        }
    }
}

@Composable
private fun ViewerListLoading(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ViewerList(
    viewers: List<User>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth()
    ) {
        items(viewers, key = { it.id }) { user ->
            ViewerItem(user = user)
        }
    }
}

@Composable
private fun ViewerItem(user: User) {
    ListItem(
        headlineContent = {
            Text(
                text = user.fullName,
                fontWeight = FontWeight.SemiBold
            )
        },
        supportingContent = {
            Text(text = user.email)
        },
        leadingContent = {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = user.firstName.firstOrNull()?.uppercase() ?: "?",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    )
}

