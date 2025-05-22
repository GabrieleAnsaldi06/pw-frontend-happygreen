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
import androidx.lifecycle.ViewModelProvider

@Composable
fun CameraScanScreen(onNavigateToBarcodeScan: () -> Unit, onScanResult: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Create ViewModel with proper factory to provide Application context
    val objectScanViewModel: ObjectScanViewModel = viewModel(
        factory = ViewModelProvider.AndroidViewModelFactory.getInstance(
            context.applicationContext as android.app.Application
        )
    )

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
            // Error handling in case ML Kit initialization fails
            val scanState by objectScanViewModel.scanState.collectAsState()

            if (scanState.error != null && scanState.error!!.contains("ML Kit")) {
                Text(
                    "Errore nell'inizializzazione del riconoscimento immagini. " +
                            "Verifica che Google Play Services sia aggiornato.",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                CameraPreview(
                    context = context,
                    lifecycleOwner = lifecycleOwner,
                    onFrameCaptured = { image ->
                        try {
                            objectScanViewModel.analyzeImage(image)
                        } catch (e: Exception) {
                            Log.e("CameraScan", "Error analyzing image", e)
                        }
                    }
                )

                // Show scan state
                if (scanState.isScanning) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    )
                }

                scanState.recognizedLabel?.let { label ->
                    Text(
                        "Riconosciuto: $label",
                        modifier = Modifier.padding(16.dp)
                    )
                }

                scanState.error?.let { error ->
                    Text(
                        error,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
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
    var cameraError by remember { mutableStateOf<String?>(null) }

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
                try {
                    val cameraProvider = cameraProviderFuture.get()

                    val preview = Preview.Builder().build().also {
                        it.surfaceProvider = previewView.surfaceProvider
                    }

                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                        .also {
                            it.setAnalyzer(ContextCompat.getMainExecutor(context)) { imageProxy ->
                                try {
                                    // Create a copy of the image if needed
                                    onFrameCaptured(imageProxy)
                                    // Close the imageProxy AFTER the analysis completes (in the onSuccess/onFailure callbacks)
                                    // Not closing here as we'll do it after processing
                                } catch (e: Exception) {
                                    Log.e("CameraScan", "Error in image analysis", e)
                                    imageProxy.close() // Close on error
                                }
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
                        cameraError = null
                    } catch (e: Exception) {
                        Log.e("CameraScan", "Use case binding failed", e)
                        cameraError = "Errore nell'inizializzazione della fotocamera: ${e.message}"
                    }
                } catch (e: Exception) {
                    Log.e("CameraScan", "Camera provider error", e)
                    cameraError = "Errore provider fotocamera: ${e.message}"
                }
            }, ContextCompat.getMainExecutor(context))

            previewView
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp)
    )

    cameraError?.let {
        Text(
            text = it,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(16.dp)
        )
    }
}