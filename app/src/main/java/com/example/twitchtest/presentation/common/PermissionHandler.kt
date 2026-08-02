package com.example.twitchtest.presentation.common

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

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
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
        if (alreadyGranted) {
            onAllGranted()
        } else {
            launcher.launch(permissions)
        }
    }
}

