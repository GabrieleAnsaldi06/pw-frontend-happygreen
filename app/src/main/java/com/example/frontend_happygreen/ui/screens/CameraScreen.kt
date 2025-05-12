package com.example.frontend_happygreen.ui.screens

import android.Manifest
import android.content.Context
import android.util.Log
import android.view.ViewGroup
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.happygreen.viewmodels.ObjectScanViewModel

@Composable
fun CameraScanScreen(
    objectScanViewModel: ObjectScanViewModel = viewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(Unit) {
        val result = androidx.core.app.ActivityCompat.checkSelfPermission(
            context, Manifest.permission.CAMERA
        )
        hasCameraPermission = result == android.content.pm.PackageManager.PERMISSION_GRANTED
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Top
    ) {
        Text("Scanner", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(16.dp))

        if (hasCameraPermission) {
            CameraPreview(
                context = context,
                lifecycleOwner = lifecycleOwner,
                onFrameCaptured = { image ->
                    objectScanViewModel.analyzeImage(image)
                }
            )
        } else {
            Text("Permesso fotocamera non concesso", color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
fun CameraPreview(
    context: Context,
    lifecycleOwner: LifecycleOwner,
    onFrameCaptured: (ImageProxy) -> Unit
) {
    AndroidView(
        factory = { it ->
            val previewView = PreviewView(it).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }

            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder().build().also {
                    it.surfaceProvider = previewView.surfaceProvider
                }

                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also {
                        it.setAnalyzer(ContextCompat.getMainExecutor(context)) { imageProxy ->
                            onFrameCaptured(imageProxy)
                            imageProxy.close()
                        }
                    }

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageAnalysis
                    )
                } catch (e: Exception) {
                    Log.e("CameraScan", "Use case binding failed", e)
                }

            }, ContextCompat.getMainExecutor(context))

            previewView
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp)
    )
}
