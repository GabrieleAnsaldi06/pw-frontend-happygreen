package com.example.frontend_happygreen.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.MarkerState
import com.happygreen.viewmodels.*

@Composable
fun MapScreen(
    viewModel: MapViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadUserLocation()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Mappa",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(16.dp)
        )

        Box(modifier = Modifier.fillMaxSize()) {
            GoogleMapView(uiState = uiState)
        }
    }
}

@SuppressLint("MissingPermission", "UnrememberedMutableState")
@Composable
fun GoogleMapView(uiState: MapUiState) {
    val userLocation = LatLng(45.4642, 9.19)
    val cameraPositionState = rememberCameraPositionState()

    LaunchedEffect(userLocation) {
        cameraPositionState.move(
            CameraUpdateFactory.newLatLngZoom(userLocation, 14f)
        )
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState
    ) {
        Marker(
            state = MarkerState(position = userLocation),
            title = "Sei qui"
        )
    }
}
