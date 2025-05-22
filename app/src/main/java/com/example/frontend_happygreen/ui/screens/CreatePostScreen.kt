package com.example.frontend_happygreen.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.happygreen.viewmodels.PostViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(
    groupId: Int,
    postViewModel: PostViewModel,
    onBack: () -> Unit,
    onPostCreated: () -> Unit
) {
    val context = LocalContext.current
    val uiState by postViewModel.uiState.collectAsState()

    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var locationName by remember { mutableStateOf("") }
    var latitude by remember { mutableStateOf<Double?>(null) }
    var longitude by remember { mutableStateOf<Double?>(null) }
    var isLocationEnabled by remember { mutableStateOf(false) }
    var showLocationDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    // Location permission launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            getCurrentLocation(context) { lat, lng, location ->
                latitude = lat
                longitude = lng
                locationName = location
                isLocationEnabled = true
            }
        }
    }

    // Image picker launcher (placeholder per future implementation)
    val imagePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        // TODO: Handle image selection and upload
        // Per ora lasciamo vuoto, da implementare con upload di immagini
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Top App Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Torna indietro"
                )
            }

            Text(
                text = "Nuovo Post",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            TextButton(
                onClick = {
                    if (title.isNotBlank() && content.isNotBlank()) {
                        postViewModel.createPost(
                            title = title,
                            content = content,
                            groupId = groupId,
                            latitude = latitude,
                            longitude = longitude,
                            locationName = locationName.takeIf { it.isNotBlank() },
                            imageUrl = null, // TODO: Implementare upload immagini
                            onSuccess = onPostCreated,
                            onError = { error ->
                                errorMessage = error
                                showErrorDialog = true
                            }
                        )
                    }
                },
                enabled = title.isNotBlank() && content.isNotBlank() && !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Pubblica")
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Title Field
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Titolo del post") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = title.isBlank()
        )

        if (title.isBlank()) {
            Text(
                text = "Il titolo è obbligatorio",
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Content Field
        OutlinedTextField(
            value = content,
            onValueChange = { content = it },
            label = { Text("Contenuto del post") },
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            maxLines = 10,
            isError = content.isBlank()
        )

        if (content.isBlank()) {
            Text(
                text = "Il contenuto è obbligatorio",
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Additional Options Section
        Text(
            text = "Opzioni aggiuntive",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Location Toggle
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (isLocationEnabled)
                    MaterialTheme.colorScheme.primaryContainer
                else
                    MaterialTheme.colorScheme.surface
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Aggiungi posizione",
                            fontWeight = FontWeight.Medium
                        )
                    }

                    if (isLocationEnabled && locationName.isNotBlank()) {
                        Text(
                            text = locationName,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp, start = 28.dp)
                        )
                    }
                }

                Switch(
                    checked = isLocationEnabled,
                    onCheckedChange = { enabled ->
                        if (enabled) {
                            // Check permission
                            if (ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.ACCESS_FINE_LOCATION
                                ) == PackageManager.PERMISSION_GRANTED
                            ) {
                                getCurrentLocation(context) { lat, lng, location ->
                                    latitude = lat
                                    longitude = lng
                                    locationName = location
                                    isLocationEnabled = true
                                }
                            } else {
                                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                            }
                        } else {
                            isLocationEnabled = false
                            latitude = null
                            longitude = null
                            locationName = ""
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Image Upload (Placeholder)
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Aggiungi immagine",
                        fontWeight = FontWeight.Medium
                    )
                }

                IconButton(
                    onClick = {
                        // TODO: Implementare selezione immagine
                        imagePickerLauncher.launch("image/*")
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Aggiungi immagine"
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Preview Section
        if (title.isNotBlank() || content.isNotBlank()) {
            Text(
                text = "Anteprima",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    if (title.isNotBlank()) {
                        Text(
                            text = title,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    if (content.isNotBlank()) {
                        Text(
                            text = content,
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )
                    }

                    if (isLocationEnabled && locationName.isNotBlank()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = locationName,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }

    // Error Dialog
    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            title = { Text("Errore") },
            text = { Text(errorMessage) },
            confirmButton = {
                TextButton(
                    onClick = { showErrorDialog = false }
                ) {
                    Text("OK")
                }
            }
        )
    }

    // Location Dialog (if needed)
    if (showLocationDialog) {
        AlertDialog(
            onDismissRequest = { showLocationDialog = false },
            title = { Text("Posizione") },
            text = { Text("Vuoi aggiungere la tua posizione attuale al post?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLocationDialog = false
                        if (ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            ) == PackageManager.PERMISSION_GRANTED
                        ) {
                            getCurrentLocation(context) { lat, lng, location ->
                                latitude = lat
                                longitude = lng
                                locationName = location
                                isLocationEnabled = true
                            }
                        } else {
                            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                        }
                    }
                ) {
                    Text("Sì")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showLocationDialog = false }
                ) {
                    Text("No")
                }
            }
        )
    }
}

private fun getCurrentLocation(
    context: android.content.Context,
    onLocationReceived: (Double, Double, String) -> Unit
) {
    try {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    // Per ora usiamo coordinate generiche, in una vera implementazione
                    // dovresti usare un servizio di geocoding per ottenere il nome del luogo
                    val locationName = "Lat: ${String.format("%.4f", it.latitude)}, " +
                            "Lng: ${String.format("%.4f", it.longitude)}"
                    onLocationReceived(it.latitude, it.longitude, locationName)
                }
            }
        }
    } catch (e: Exception) {
        // Gestisci l'errore
        e.printStackTrace()
    }
}