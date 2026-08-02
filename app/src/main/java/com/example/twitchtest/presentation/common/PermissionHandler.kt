package com.example.twitchtest.presentation.common

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.twitchtest.R

private enum class PermissionState { PENDING, GRANTED, DENIED }

@Composable
fun RequireStreamingPermissions(
    content: @Composable () -> Unit
) {
    var state by remember { mutableStateOf(PermissionState.PENDING) }

    when (state) {
        PermissionState.PENDING -> {
            RequestStreamingPermissions(
                onAllGranted = { state = PermissionState.GRANTED },
                onDenied = { state = PermissionState.DENIED }
            )
        }
        PermissionState.DENIED -> {
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
        }
        PermissionState.GRANTED -> content()
    }
}

@Composable
fun RequestStreamingPermissions(
    onAllGranted: () -> Unit,
    onDenied: () -> Unit
) {
    val context = LocalContext.current
    val permissions = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO
    )

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        if (results.values.all { it }) {
            onAllGranted()
        } else {
            onDenied()
        }
    }

    LaunchedEffect(Unit) {
        val alreadyGranted = permissions.all { permission ->
            ContextCompat.checkSelfPermission(context, permission) ==
                PackageManager.PERMISSION_GRANTED
        }
        if (alreadyGranted) {
            onAllGranted()
        } else {
            launcher.launch(permissions)
        }
    }
}
