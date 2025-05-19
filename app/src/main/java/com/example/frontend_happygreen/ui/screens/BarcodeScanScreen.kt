package com.example.frontend_happygreen.ui.screens

import SustainabilityScoreBar
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.happygreen.viewmodels.BarcodeScanViewModel

@Composable
fun BarcodeScanScreen(
    barcodeScanViewModel: BarcodeScanViewModel = viewModel(),
    onBack: () -> Unit
) {
    val scanState by barcodeScanViewModel.scanState.collectAsState()

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
                text = "Scansione Codice a Barre",
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
                text = "Ricerca prodotto in corso...",
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

            Button(onClick = { barcodeScanViewModel.resetScanState() }) {
                Text("Riprova")
            }
        } else if (scanState.product != null) {
            // Successfully found product
            val product = scanState.product!!

            ProductDetailsCard(product)
        } else if (scanState.barcodeValue != null) {
            // Recognized barcode but no product details
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Prodotto non trovato",
                modifier = Modifier
                    .size(100.dp)
                    .padding(16.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Codice a barre rilevato",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Codice a barre: ${scanState.barcodeValue}",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Purtroppo non sono disponibili informazioni su questo prodotto nel nostro database.",
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

                Button(onClick = { barcodeScanViewModel.resetScanState() }) {
                    Text("Nuova scansione")
                }
            }
        } else {
            // No result yet - initial state
            Icon(
                imageVector = Icons.Default.QrCodeScanner,
                contentDescription = "Scansiona codice a barre",
                modifier = Modifier
                    .size(100.dp)
                    .padding(16.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Scansiona un codice a barre",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Punta la fotocamera verso il codice a barre di un prodotto per ottenere informazioni sulla sua sostenibilità.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ProductDetailsCard(product: com.happygreen.models.Product) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        // Product icon/image placeholder
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            if (product.imageUrl != null) {
                // Se si volesse implementare il caricamento dell'immagine, si potrebbe usare una libreria come Coil
                // Qui mostro solo l'icona per semplicità
                Icon(
                    imageVector = if (product.ecoFriendly) Icons.Default.EmojiNature else Icons.Default.Info,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = if (product.ecoFriendly) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary
                )
            } else {
                Icon(
                    imageVector = if (product.ecoFriendly) Icons.Default.EmojiNature else Icons.Default.Info,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = if (product.ecoFriendly) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Product name and manufacturer
        Text(
            text = product.name,
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "Produttore: ${product.manufacturer}",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = "Codice a barre: ${product.barcode}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Eco-friendly badge if applicable
        if (product.ecoFriendly) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF4CAF50).copy(alpha = 0.2f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(24.dp)
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Text(
                        text = "Prodotto Eco-Friendly",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF4CAF50)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Recyclable badge if applicable
        if (product.recyclable) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF2196F3).copy(alpha = 0.2f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Recycling,
                        contentDescription = null,
                        tint = Color(0xFF2196F3),
                        modifier = Modifier.size(24.dp)
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Text(
                        text = "Prodotto Riciclabile",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF2196F3)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

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

                SustainabilityScoreBar(score = product.sustainabilityScore)

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = getProductSustainabilityDescription(
                        score = product.sustainabilityScore,
                        ecoFriendly = product.ecoFriendly
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Product description
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
                    text = product.description,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Sustainability info
        ElevatedCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Informazioni Ambientali",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = product.ecoInfo,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Alternative products
        if (product.alternatives.isNotEmpty()) {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Alternative Sostenibili",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = product.alternatives,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

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
                    text = "Hai guadagnato 2 punti!",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Continua a scansionare prodotti per guadagnare più punti e sbloccare badge.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

// Helper function to provide a description based on the sustainability score for products
fun getProductSustainabilityDescription(score: Int, ecoFriendly: Boolean): String {
    return when {
        ecoFriendly && score >= 8 -> "Eccellente! Questo prodotto è certificato eco-friendly e ha un impatto ambientale molto basso."
        ecoFriendly -> "Ottimo! Questo prodotto è eco-friendly, ma ci sono ancora margini di miglioramento."
        score >= 8 -> "Questo prodotto ha un basso impatto ambientale, anche se non è certificato come eco-friendly."
        score >= 5 -> "Questo prodotto ha un impatto ambientale moderato. Considera alternative più sostenibili."
        else -> "Questo prodotto ha un alto impatto ambientale. Ti consigliamo di considerare alternative più sostenibili."
    }
}