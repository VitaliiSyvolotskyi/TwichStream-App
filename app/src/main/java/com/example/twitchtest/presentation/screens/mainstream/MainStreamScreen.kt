package com.example.twitchtest.presentation.screens.mainstream

import android.app.Activity
import android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
import androidx.activity.compose.BackHandler
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.twitchtest.R
import com.example.twitchtest.domain.model.StreamStatus
import com.example.twitchtest.presentation.common.RequireStreamingPermissions
import com.example.twitchtest.presentation.screens.mainstream.content.StreamContent

@Composable
fun MainStreamScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: MainStreamViewModel = hiltViewModel()
) {
    RequireStreamingPermissions {
        MainStreamContent(
            onNavigateBack = onNavigateBack,
            viewModel = viewModel
        )
    }
}

@Composable
private fun MainStreamContent(
    onNavigateBack: () -> Unit,
    viewModel: MainStreamViewModel
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val activity = context as? Activity
    val snackbarHostState = remember { SnackbarHostState() }
    var showViewerSheet by remember { mutableStateOf(false) }
    var showExitDialog by remember { mutableStateOf(false) }

    BackHandler(enabled = state.isStreaming) {
        showExitDialog = true
    }

    DisposableEffect(activity) {
        onDispose {
            activity?.window?.clearFlags(FLAG_KEEP_SCREEN_ON)
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                MainStreamEffect.ShowViewerSheet -> showViewerSheet = true
                MainStreamEffect.HideViewerSheet -> showViewerSheet = false
                is MainStreamEffect.ShowError -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    LaunchedEffect(state.streamStatus, activity) {
        if (state.streamStatus == StreamStatus.ONLINE) {
            activity?.window?.addFlags(FLAG_KEEP_SCREEN_ON)
        } else {
            activity?.window?.clearFlags(FLAG_KEEP_SCREEN_ON)
        }
    }

    if (showExitDialog) {
        ExitStreamDialog(
            onConfirm = {
                showExitDialog = false
                viewModel.onIntent(MainStreamIntent.StopStream)
                onNavigateBack()
            },
            onDismiss = { showExitDialog = false }
        )
    }

    StreamContent(
        state = state,
        snackbarHostState = snackbarHostState,
        showViewerSheet = showViewerSheet,
        onIntent = viewModel::onIntent
    )
}

@Composable
private fun ExitStreamDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.end_stream_dialog_title)) },
        text = { Text(stringResource(R.string.end_stream_dialog_message)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.end_stream))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
