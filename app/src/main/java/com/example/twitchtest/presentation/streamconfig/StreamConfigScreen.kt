package com.example.twitchtest.presentation.streamconfig

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

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
                title = { Text("Stream Configuration") }
            )
        }
    ) { innerPadding ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Enter your Twitch stream key to start streaming.",
                    style = MaterialTheme.typography.bodyLarge
                )

                OutlinedTextField(
                    value = state.streamKey,
                    onValueChange = { viewModel.onIntent(StreamConfigIntent.UpdateKey(it)) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Stream Key") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    supportingText = state.error?.let { error ->
                        { Text(text = error, color = MaterialTheme.colorScheme.error) }
                    }
                )

                Button(
                    onClick = { viewModel.onIntent(StreamConfigIntent.SaveKey) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save Key")
                }

                Button(
                    onClick = { viewModel.onIntent(StreamConfigIntent.NavigateToStream) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = state.isSaved
                ) {
                    Text("Start Streaming")
                }
            }
        }
    }
}



