package com.example.twitchtest.presentation.screens.streamconfig

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.twitchtest.R
import com.example.twitchtest.presentation.screens.streamconfig.StreamConfigIntent.NavigateToStream
import com.example.twitchtest.presentation.screens.streamconfig.StreamConfigIntent.SaveKey

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StreamConfigScreen(
    onNavigateToStream: () -> Unit,
    viewModel: StreamConfigViewModel = hiltViewModel()
) {
    val state = viewModel.state.collectAsStateWithLifecycle().value

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                StreamConfigEffect.NavigateToMainStream -> onNavigateToStream()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.stream_config_title)) }
            )
        }
    ) { innerPadding ->
        StreamConfigContent(
            state = state,
            onIntent = viewModel::onIntent,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
private fun StreamConfigContent(
    state: StreamConfigState,
    onIntent: (StreamConfigIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.stream_config_description),
            style = MaterialTheme.typography.bodyLarge
        )

        var keyVisible by rememberSaveable { mutableStateOf(false) }

        OutlinedTextField(
            value = state.streamKey,
            onValueChange = { onIntent(StreamConfigIntent.UpdateKey(it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.stream_key_label)) },
            visualTransformation = if (keyVisible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            singleLine = true,
            trailingIcon = {
                IconButton(onClick = { keyVisible = !keyVisible }) {
                    Icon(
                        imageVector = if (keyVisible) {
                            Icons.Filled.VisibilityOff
                        } else {
                            Icons.Filled.Visibility
                        },
                        contentDescription = if (keyVisible) {
                            stringResource(R.string.hide_key)
                        } else {
                            stringResource(R.string.show_key)
                        }
                    )
                }
            },
            supportingText = state.error?.let { error ->
                { Text(text = error, color = MaterialTheme.colorScheme.error) }
            }
        )

        Button(
            onClick = { onIntent(SaveKey) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.save_key))
        }

        Button(
            onClick = { onIntent(NavigateToStream) },
            modifier = Modifier.fillMaxWidth(),
            enabled = state.isSaved
        ) {
            Text(stringResource(R.string.start_streaming))
        }
    }
}
