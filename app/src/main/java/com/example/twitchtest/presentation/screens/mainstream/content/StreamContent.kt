package com.example.twitchtest.presentation.screens.mainstream.content

import android.view.SurfaceHolder
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.twitchtest.domain.model.StreamStatus
import com.example.twitchtest.presentation.screens.mainstream.MainStreamIntent
import com.example.twitchtest.presentation.screens.mainstream.MainStreamState
import com.example.twitchtest.presentation.screens.mainstream.content.components.ControlBar
import com.example.twitchtest.presentation.screens.mainstream.content.components.StatusInfoPanel
import com.example.twitchtest.presentation.screens.viewerlist.ViewerListSheet
import com.pedro.library.view.OpenGlView

@Composable
fun StreamContent(
    state: MainStreamState,
    snackbarHostState: SnackbarHostState,
    showViewerSheet: Boolean,
    onIntent: (MainStreamIntent) -> Unit
) {
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    Box(modifier = Modifier.fillMaxSize()) {
        CameraPreview(onIntent = onIntent)

        StatusInfoPanel(
            status = state.streamStatus,
            duration = state.duration,
            onClick = if (state.streamStatus == StreamStatus.ONLINE) {
                { onIntent(MainStreamIntent.OpenViewerSheet) }
            } else {
                null
            },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = statusBarPadding + 12.dp)
        )


        ControlBar(
            isStreaming = state.isStreaming,
            isMuted = state.isMuted,
            isFrontCamera = state.isFrontCamera,
            onIntent = onIntent,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = navBarPadding + 16.dp)
        )

        if (showViewerSheet) {
            ViewerListSheet(
                onDismiss = { onIntent(MainStreamIntent.CloseViewerSheet) }
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun CameraPreview(onIntent: (MainStreamIntent) -> Unit) {
    AndroidView(
        factory = { previewContext ->
            OpenGlView(previewContext).also { openGlView ->
                onIntent(MainStreamIntent.InitializeStream(openGlView))
                var surfaceCreatedOnce = false
                openGlView.holder.addCallback(object : SurfaceHolder.Callback {
                    override fun surfaceCreated(holder: SurfaceHolder) {
                        if (!surfaceCreatedOnce) {
                            surfaceCreatedOnce = true
                            onIntent(MainStreamIntent.StartPreview)
                        } else {
                            onIntent(MainStreamIntent.SurfaceRecreated(openGlView))
                        }
                    }

                    override fun surfaceChanged(
                        holder: SurfaceHolder,
                        format: Int,
                        width: Int,
                        height: Int
                    ) = Unit

                    override fun surfaceDestroyed(holder: SurfaceHolder) {
                        onIntent(MainStreamIntent.StopPreview)
                    }
                })
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}

