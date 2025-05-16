package com.happygreen.viewmodels

import android.graphics.Bitmap
import android.media.Image
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeler
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import com.happygreen.data.RetrofitInstance
import com.happygreen.models.RecognizedObject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException

data class ObjectScanState(
    val isScanning: Boolean = false,
    val recognizedLabel: String? = null,
    val confidenceScore: Float? = null,
    val objectDetails: RecognizedObject? = null,
    val error: String? = null
)

class ObjectScanViewModel : ViewModel() {

    private val _scanState = MutableStateFlow(ObjectScanState())
    val scanState: StateFlow<ObjectScanState> = _scanState.asStateFlow()

    private val imageLabeler: ImageLabeler = ImageLabeling.getClient(
        ImageLabelerOptions.Builder()
            .setConfidenceThreshold(0.7f)
            .build()
    )

    @OptIn(ExperimentalGetImage::class)
    fun analyzeImage(imageProxy: ImageProxy) {
        _scanState.update { it.copy(isScanning = true, error = null) }

        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

            imageLabeler.process(image)
                .addOnSuccessListener { labels ->
                    if (labels.isNotEmpty()) {
                        val topLabel = labels[0]
                        _scanState.update {
                            it.copy(
                                recognizedLabel = topLabel.text,
                                confidenceScore = topLabel.confidence,
                                isScanning = false
                            )
                        }
                        // Cerca informazioni sull'oggetto riconosciuto
                        fetchObjectDetails(topLabel.text)
                    } else {
                        _scanState.update {
                            it.copy(
                                error = "Nessun oggetto riconosciuto",
                                isScanning = false
                            )
                        }
                    }
                }
                .addOnFailureListener { e ->
                    _scanState.update {
                        it.copy(
                            error = "Errore nell'analisi: ${e.message}",
                            isScanning = false
                        )
                    }
                }
        }
    }

    fun analyzeBitmap(bitmap: Bitmap) {
        _scanState.update { it.copy(isScanning = true, error = null) }

        val image = InputImage.fromBitmap(bitmap, 0)

        imageLabeler.process(image)
            .addOnSuccessListener { labels ->
                if (labels.isNotEmpty()) {
                    val topLabel = labels[0]
                    _scanState.update {
                        it.copy(
                            recognizedLabel = topLabel.text,
                            confidenceScore = topLabel.confidence,
                            isScanning = false
                        )
                    }
                    // Cerca informazioni sull'oggetto riconosciuto
                    fetchObjectDetails(topLabel.text)
                } else {
                    _scanState.update {
                        it.copy(
                            error = "Nessun oggetto riconosciuto",
                            isScanning = false
                        )
                    }
                }
            }
            .addOnFailureListener { e ->
                _scanState.update {
                    it.copy(
                        error = "Errore nell'analisi: ${e.message}",
                        isScanning = false
                    )
                }
            }
    }

    private fun fetchObjectDetails(objectName: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.apiService.getObjects()
                if (response.isSuccessful) {
                    val objects = response.body() ?: emptyList()
                    val matchedObject = objects.find {
                        it.name.equals(objectName, ignoreCase = true) ||
                                objectName.contains(it.name, ignoreCase = true) ||
                                it.name.contains(objectName, ignoreCase = true)
                    }

                    _scanState.update { it.copy(objectDetails = matchedObject) }

                    // Registra la scansione sul backend
                    if (matchedObject != null) {
                        val scanData = mapOf(
                            "object_id" to matchedObject.id,
                            "confidence_score" to (_scanState.value.confidenceScore ?: 0f)
                        )
                        RetrofitInstance.apiService.recordScan(scanData)
                    }
                } else {
                    _scanState.update {
                        it.copy(error = "Errore nel recupero dei dettagli: ${response.message()}")
                    }
                }
            } catch (e: IOException) {
                _scanState.update {
                    it.copy(error = "Errore di rete: ${e.message}")
                }
            } catch (e: Exception) {
                _scanState.update {
                    it.copy(error = "Errore: ${e.message}")
                }
            }
        }
    }

    fun resetScanState() {
        _scanState.value = ObjectScanState()
    }
}