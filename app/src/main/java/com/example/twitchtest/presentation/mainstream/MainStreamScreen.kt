package com.example.twitchtest.presentation.mainstream

import android.app.Activity
import android.view.WindowManager
import com.pedro.library.view.OpenGlView
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraFront
import androidx.compose.material.icons.filled.CameraRear
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.twitchtest.R
import com.example.twitchtest.domain.model.StreamStatus
import com.example.twitchtest.presentation.common.RequestStreamingPermissions
import com.example.twitchtest.presentation.viewerlist.ViewerListSheet

@Composable
fun MainStreamScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: MainStreamViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val streamManager = viewModel.getStreamManager()
    val context = LocalContext.current
    val activity = context as? Activity
    val snackbarHostState = remember { SnackbarHostState() }
    var permissionsGranted by remember { mutableStateOf(false) }
    var permissionsDenied by remember { mutableStateOf(false) }
    var showViewerSheet by remember { mutableStateOf(false) }
    var showExitDialog by remember { mutableStateOf(false) }

    val isStreaming =
        state.streamStatus == StreamStatus.ONLINE || state.streamStatus == StreamStatus.CONNECTING

    BackHandler(enabled = isStreaming) {
        showExitDialog = true
    }

    if (!permissionsGranted && !permissionsDenied) {
        RequestStreamingPermissions(
            onAllGranted = { permissionsGranted = true },
            onDenied = { permissionsDenied = true }
        )
    }

    if (permissionsDenied) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.permissions_required),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(24.dp)
            )
        }
        return
    }

    if (!permissionsGranted) return

    DisposableEffect(activity) {
        onDispose {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
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
            activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text(stringResource(R.string.end_stream_dialog_title)) },
            text = { Text(stringResource(R.string.end_stream_dialog_message)) },
            confirmButton = {
                TextButton(onClick = {
                    showExitDialog = false
                    viewModel.onIntent(MainStreamIntent.StopStream)
                    onNavigateBack()
                }) {
                    Text(stringResource(R.string.end_stream))
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        AndroidView(
            factory = { previewContext ->
                OpenGlView(previewContext).also { openGlView ->
                    streamManager.initialize(openGlView)
                    var surfaceCreatedOnce = false
                    openGlView.holder.addCallback(object : android.view.SurfaceHolder.Callback {
                        override fun surfaceCreated(holder: android.view.SurfaceHolder) {
                            if (!surfaceCreatedOnce) {
                                // First creation — just start preview
                                surfaceCreatedOnce = true
                                streamManager.startPreview()
                            } else {
                                // Surface was destroyed and recreated (e.g. back from background)
                                // Re-create camera with fresh OpenGL context and resume stream if needed
                                streamManager.onSurfaceRecreated(openGlView)
                            }
                        }

                        override fun surfaceChanged(
                            holder: android.view.SurfaceHolder,
                            format: Int,
                            width: Int,
                            height: Int
                        ) = Unit

                        override fun surfaceDestroyed(holder: android.view.SurfaceHolder) {
                            streamManager.stopPreview()
                        }
                    })
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        StatusInfoPanel(
            status = state.streamStatus,
            duration = state.duration,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = statusBarPadding + 12.dp)
        )

        AnimatedVisibility(
            visible = state.streamStatus == StreamStatus.ONLINE,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = statusBarPadding + 8.dp, end = 16.dp),
            enter = scaleIn(tween(300)) + fadeIn(tween(300)),
            exit = scaleOut(tween(300)) + fadeOut(tween(300))
        ) {
            IconButton(
                onClick = { viewModel.onIntent(MainStreamIntent.OpenViewerSheet) },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f))
            ) {
                Icon(
                    imageVector = Icons.Filled.Groups,
                    contentDescription = stringResource(R.string.viewers)
                )
            }
        }

        ControlBar(
            streamStatus = state.streamStatus,
            isMuted = state.isMuted,
            isFrontCamera = state.isFrontCamera,
            onIntent = viewModel::onIntent,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = navBarPadding + 16.dp)
        )

        if (showViewerSheet) {
            ViewerListSheet(
                onDismiss = { viewModel.onIntent(MainStreamIntent.CloseViewerSheet) }
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun StatusInfoPanel(
    status: StreamStatus,
    duration: Long,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(statusColor(status))
            )
            Column {
                Text(
                    text = status.name,
                    style = MaterialTheme.typography.labelLarge
                )
                if (status == StreamStatus.ONLINE) {
                    Text(
                        text = formatDuration(duration),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
private fun ControlBar(
    streamStatus: StreamStatus,
    isMuted: Boolean,
    isFrontCamera: Boolean,
    onIntent: (MainStreamIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    val isLive = streamStatus == StreamStatus.ONLINE || streamStatus == StreamStatus.CONNECTING
    val fabColor by animateColorAsState(
        targetValue = if (isLive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
        animationSpec = tween(300),
        label = "fabColor"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = { onIntent(MainStreamIntent.ToggleMicrophone) },
            modifier = Modifier
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f))
        ) {
            Crossfade(
                targetState = isMuted,
                animationSpec = tween(200),
                label = "mic"
            ) { muted ->
                Icon(
                    imageVector = if (muted) Icons.Filled.MicOff else Icons.Filled.Mic,
                    contentDescription = if (muted) stringResource(R.string.unmute_microphone) else stringResource(R.string.mute_microphone)
                )
            }
        }

        FloatingActionButton(
            onClick = {
                if (isLive) {
                    onIntent(MainStreamIntent.StopStream)
                } else {
                    onIntent(MainStreamIntent.StartStream)
                }
            },
            containerColor = fabColor
        ) {
            Crossfade(
                targetState = isLive,
                animationSpec = tween(200),
                label = "stream"
            ) { live ->
                Icon(
                    imageVector = if (live) Icons.Filled.Stop else Icons.Filled.PlayArrow,
                    contentDescription = if (live) stringResource(R.string.stop_stream) else stringResource(R.string.start_stream)
                )
            }
        }

        IconButton(
            onClick = { onIntent(MainStreamIntent.SwitchCamera) },
            modifier = Modifier
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f))
        ) {
            Crossfade(
                targetState = isFrontCamera,
                animationSpec = tween(200),
                label = "camera"
            ) { front ->
                Icon(
                    imageVector = if (front) Icons.Filled.CameraRear else Icons.Filled.CameraFront,
                    contentDescription = if (front) stringResource(R.string.switch_to_back_camera) else stringResource(R.string.switch_to_front_camera)
                )
            }
        }
    }
}

private fun statusColor(status: StreamStatus): Color = when (status) {
    StreamStatus.OFFLINE -> Color.Gray
    StreamStatus.CONNECTING -> Color(0xFFFFA000)
    StreamStatus.ONLINE -> Color(0xFF2E7D32)
    StreamStatus.RECONNECTING -> Color(0xFFFF6F00)
}

private fun formatDuration(seconds: Long): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val remainingSeconds = seconds % 60
    return "%02d:%02d:%02d".format(hours, minutes, remainingSeconds)
}

