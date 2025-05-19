package com.example.frontend_happygreen.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import SustainabilityScoreBar
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.happygreen.models.RecognizedObject
import com.happygreen.viewmodels.ObjectScanViewModel
import android.content.Intent
import android.widget.Toast
import kotlinx.coroutines.launch

@Composable
fun ObjectScanResultScreen(
    objectScanViewModel: ObjectScanViewModel = viewModel(),
    onBack: () -> Unit
) {
    val scanState by objectScanViewModel.scanState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showSavedConfirmation by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top bar with back button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Indietro")
            }

            Text(
                text = "Risultato Scansione",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )

            // Empty spacer for symmetry
            Spacer(modifier = Modifier.width(48.dp))
        }

        if (scanState.isScanning) {
            // Loading indicator
            CircularProgressIndicator(
                modifier = Modifier
                    .size(100.dp)
                    .padding(16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Analisi dell'oggetto in corso...",
                style = MaterialTheme.typography.bodyLarge
            )
        } else if (scanState.error != null) {
            // Error state
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = "Errore",
                modifier = Modifier
                    .size(100.dp)
                    .padding(16.dp),
                tint = MaterialTheme.colorScheme.error
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Si è verificato un errore",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.error
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = scanState.error ?: "Errore sconosciuto",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(onClick = { objectScanViewModel.resetScanState() }) {
                Text("Riprova")
            }
        } else if (scanState.objectDetails != null) {
            // Successfully identified object
            val objectDetails = scanState.objectDetails!!

            ObjectDetailsCard(
                objectDetails = objectDetails,
                onShare = {
                    shareObject(objectDetails, context)
                }
            )

            if (showSavedConfirmation) {
                AlertDialog(
                    onDismissRequest = { showSavedConfirmation = false },
                    title = { Text("Oggetto Salvato") },
                    text = { Text("L'oggetto è stato salvato con successo!") },
                    confirmButton = {
                        Button(onClick = { showSavedConfirmation = false }) {
                            Text("OK")
                        }
                    }
                )
            }
        } else if (scanState.recognizedLabel != null) {
            // Recognized object but no details from backend
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Oggetto riconosciuto",
                modifier = Modifier
                    .size(100.dp)
                    .padding(16.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Oggetto riconosciuto",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "È stato riconosciuto un oggetto come: ${scanState.recognizedLabel}",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Purtroppo non sono disponibili informazioni dettagliate su questo oggetto.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(onClick = onBack) {
                    Text("Indietro")
                }

                Button(onClick = { objectScanViewModel.resetScanState() }) {
                    Text("Nuova scansione")
                }
            }
        } else {
            // No result yet - initial state
            Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = "Scansiona",
                modifier = Modifier
                    .size(100.dp)
                    .padding(16.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Scansiona un oggetto",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Punta la fotocamera verso un oggetto per ottenere informazioni sulla sua sostenibilità.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ObjectDetailsCard(
    objectDetails: RecognizedObject,
    onShare: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        // Object icon/image placeholder
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Object name and category
        Text(
            text = objectDetails.name,
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "Categoria: ${objectDetails.category}",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Description
        ElevatedCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Descrizione",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = objectDetails.description,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Sustainability score
        ElevatedCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Punteggio Sostenibilità",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                SustainabilityScoreBar(score = objectDetails.sustainability_score)

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = getSustainabilityDescription(objectDetails.sustainability_score),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Eco impact
        ElevatedCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Impatto Ambientale",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = objectDetails.eco_impact,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Recycling info
        ElevatedCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Informazioni sul Riciclo",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = objectDetails.recycling_info,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Points earned
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Hai guadagnato 5 punti!",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Continua a scansionare oggetti per guadagnare più punti e sbloccare badge.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = onShare,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Condividi",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Condividi")
            }
        }
    }
}

// Helper function to provide a description based on the sustainability score
fun getSustainabilityDescription(score: Int): String {
    return when (score) {
        in 1..3 -> "Questo oggetto ha un alto impatto ambientale. Prova a cercarne uno più sostenibile o limita il suo utilizzo."
        in 4..7 -> "Questo oggetto ha un impatto ambientale moderato. Cerca di riciclarlo correttamente e considerare alternative più sostenibili."
        in 8..10 -> "Ottimo! Questo oggetto ha un basso impatto ambientale ed è una scelta sostenibile."
        else -> "Punteggio di sostenibilità non valido."
    }
}

// Helper function to share object details
fun shareObject(objectDetails: RecognizedObject, context: android.content.Context) {
    val shareText = """
        Ho scansionato un oggetto con HappyGreen!
        
        Nome: ${objectDetails.name}
        Categoria: ${objectDetails.category}
        Punteggio Sostenibilità: ${objectDetails.sustainability_score}/10
        
        ${getSustainabilityDescription(objectDetails.sustainability_score)}
        
        Impatto Ambientale: ${objectDetails.eco_impact}
        
        Informazioni sul Riciclo:
        ${objectDetails.recycling_info}
        
        Scansiona anche tu oggetti e impara a vivere in modo più sostenibile con HappyGreen!
    """.trimIndent()

    val intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, shareText)
        type = "text/plain"
    }

    context.startActivity(Intent.createChooser(intent, "Condividi tramite"))
}