
package com.happygreen.viewmodels

import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.happygreen.data.RetrofitInstance
import com.happygreen.models.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException

data class BarcodeScanState(
    val isScanning: Boolean = false,
    val barcodeValue: String? = null,
    val product: Product? = null,
    val error: String? = null
)

class BarcodeScanViewModel : ViewModel() {

    private val _scanState = MutableStateFlow(BarcodeScanState())
    val scanState: StateFlow<BarcodeScanState> = _scanState.asStateFlow()

    private val options = BarcodeScannerOptions.Builder()
        .setBarcodeFormats(
            Barcode.FORMAT_EAN_13,
            Barcode.FORMAT_EAN_8,
            Barcode.FORMAT_UPC_A,
            Barcode.FORMAT_UPC_E,
            Barcode.FORMAT_QR_CODE
        )
        .build()

    private val scanner: BarcodeScanner = BarcodeScanning.getClient(options)

    @OptIn(ExperimentalGetImage::class)
    fun analyzeImage(imageProxy: ImageProxy) {
        _scanState.update { it.copy(isScanning = true, error = null) }

        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

            scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    if (barcodes.isNotEmpty()) {
                        val barcode = barcodes[0].rawValue
                        if (barcode != null) {
                            _scanState.update {
                                it.copy(
                                    barcodeValue = barcode,
                                    isScanning = false
                                )
                            }
                            // Cerca il prodotto dal codice a barre
                            fetchProductDetails(barcode)
                        } else {
                            _scanState.update {
                                it.copy(
                                    error = "Codice non valido",
                                    isScanning = false
                                )
                            }
                        }
                    } else {
                        _scanState.update {
                            it.copy(
                                error = "Nessun codice a barre rilevato",
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

    private fun fetchProductDetails(barcode: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.apiService.getProductByBarcode(barcode)
                if (response.isSuccessful) {
                    val product = response.body()
                    _scanState.update { it.copy(product = product) }
                } else if (response.code() == 404) {
                    // Il prodotto non è nel database, cerchiamo informazioni generiche
                    lookupProductExternally(barcode)
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

    private fun lookupProductExternally(barcode: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.apiService.lookupProduct(mapOf("barcode" to barcode))
                if (response.isSuccessful) {
                    val product = response.body()
                    _scanState.update { it.copy(product = product) }
                } else {
                    _scanState.update {
                        it.copy(error = "Prodotto non trovato nel database")
                    }
                }
            } catch (e: Exception) {
                _scanState.update {
                    it.copy(error = "Errore nella ricerca esterna: ${e.message}")
                }
            }
        }
    }

    fun resetScanState() {
        _scanState.value = BarcodeScanState()
    }
}