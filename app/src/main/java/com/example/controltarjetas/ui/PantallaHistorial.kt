package com.example.controltarjetas.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.controltarjetas.TarjetaViewModel
import com.example.controltarjetas.data.HistorialPago
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaHistorial(
    viewModel: TarjetaViewModel = viewModel(),
    onNavigateBack: () -> Unit
) {
    val historial by viewModel.todoHistorial.collectAsState(initial = emptyList())
    var mostrarDialogoEliminar by remember { mutableStateOf(false) }
    var historialAEliminar by remember { mutableStateOf<HistorialPago?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historial de Pagos") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        if (historial.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No hay pagos registrados",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Gray
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(historial) { pago ->
                    HistorialCard(
                        historial = pago,
                        onDeleteClick = {
                            historialAEliminar = pago
                            mostrarDialogoEliminar = true
                        }
                    )
                }
            }
        }
    }

    // Diálogo eliminar
    if (mostrarDialogoEliminar && historialAEliminar != null) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoEliminar = false },
            title = { Text("Eliminar registro") },
            text = { Text("¿Eliminar este registro del historial?") },
            confirmButton = {
                TextButton(onClick = {
                    historialAEliminar?.let { viewModel.eliminarHistorial(it.id) }
                    mostrarDialogoEliminar = false
                    historialAEliminar = null
                }) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoEliminar = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun HistorialCard(
    historial: HistorialPago,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = historial.nombreBanco,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        Icons.Default.Delete,
                        "Eliminar",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Deuda Pagada", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    Text(
                        text = formatoMoneda(historial.deudaTotal),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32)
                    )
                }
                if (historial.pagoMinimo != null) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Pago Mínimo", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        Text(
                            text = formatoMoneda(historial.pagoMinimo),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Fecha Límite", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    Text(historial.fechaLimitePago, style = MaterialTheme.typography.bodyMedium)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Fecha de Pago", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    Text(historial.fechaPago, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
            }

            if (!historial.notas.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Nota: ${historial.notas}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.DarkGray
                )
            }
        }
    }
}

private fun formatoMoneda(monto: Double): String {
    val formato = NumberFormat.getCurrencyInstance(Locale("es", "MX"))
    return formato.format(monto)
}