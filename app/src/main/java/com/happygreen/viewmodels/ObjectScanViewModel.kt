package com.happygreen.viewmodels

import androidx.camera.core.ImageProxy
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ObjectScanViewModel : ViewModel() {

    private val _result = MutableStateFlow<String?>(null)
    val result = _result.asStateFlow()

    fun analyzeImage(image: ImageProxy) {
        _result.value = "Oggetto rilevato: WIP"
    }
}
