package com.example.controltarjetas.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.controltarjetas.data.Banco
import com.example.controltarjetas.data.Tarjeta
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaListaTarjetas(
    viewModel: TarjetaViewModel = viewModel(),
    onAgregarTarjeta: () -> Unit,
    onEditarTarjeta: (Tarjeta) -> Unit,
    onNavigateBancos: () -> Unit,
    onNavigateHistorial: () -> Unit,
    onNavigateEstadisticas: () -> Unit,     // NUEVO
    onNavigateConfiguracion: () -> Unit
) {
    val tarjetas by viewModel.todasLasTarjetas.collectAsState(initial = emptyList())
    val bancos by viewModel.todosBancos.collectAsState(initial = emptyList())
    val deudaTotal by viewModel.deudaTotal.collectAsState(initial = 0.0)

    val scope = rememberCoroutineScope()

    var mostrarDialogoEliminar by remember { mutableStateOf(false) }
    var tarjetaAEliminar by remember { mutableStateOf<Tarjeta?>(null) }

    var mostrarDialogoConfirmarPago by remember { mutableStateOf(false) }
    var tarjetaAPagar by remember { mutableStateOf<Tarjeta?>(null) }
    var bancoTarjetaAPagar by remember { mutableStateOf<Banco?>(null) }

    // Crear mapa de bancos por ID para búsqueda rápida
    val bancosMap = remember(bancos) {
        bancos.associateBy { it.id }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Control de Tarjetas") },
                actions = {
                    IconButton(onClick = onNavigateEstadisticas) {
                        Icon(Icons.Default.Star, "Estadísticas")
                    }
                    IconButton(onClick = onNavigateHistorial) {
                        Icon(Icons.Default.Info, "Historial")
                    }
                    IconButton(onClick = onNavigateBancos) {
                        Icon(Icons.Default.Build, "Bancos")
                    }
                    IconButton(onClick = onNavigateConfiguracion) {
                        Icon(Icons.Default.Settings, "Configuración")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (bancos.isEmpty()) {
                        // Mostrar mensaje de que necesita agregar bancos primero
                    } else {
                        onAgregarTarjeta()
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Agregar tarjeta"
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Resumen de deuda total
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Deuda Total Pendiente",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = formatoMoneda(deudaTotal ?: 0.0),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "${tarjetas.size} tarjeta(s) activa(s)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // Mensaje si no hay bancos
            if (bancos.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Build,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Primero agrega tus bancos",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = onNavigateBancos) {
                            Icon(Icons.Default.Build, null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Ir a Bancos")
                        }
                    }
                }
            }
            // Lista de tarjetas
            else if (tarjetas.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No hay tarjetas registradas",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.Gray
                        )
                        Text(
                            text = "Toca + para agregar una",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(tarjetas) { tarjeta ->
                        val banco = bancosMap[tarjeta.bancoId]
                        if (banco != null) {
                            TarjetaCard(
                                tarjeta = tarjeta,
                                banco = banco,
                                onEditClick = { onEditarTarjeta(tarjeta) },
                                onDeleteClick = {
                                    tarjetaAEliminar = tarjeta
                                    mostrarDialogoEliminar = true
                                },
                                onMarcarPagada = {
                                    tarjetaAPagar = tarjeta
                                    bancoTarjetaAPagar = banco
                                    mostrarDialogoConfirmarPago = true
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Diálogo de confirmación para eliminar
    if (mostrarDialogoEliminar && tarjetaAEliminar != null) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoEliminar = false },
            title = { Text("Eliminar tarjeta") },
            text = {
                val banco = bancosMap[tarjetaAEliminar?.bancoId]
                Text("¿Estás seguro de eliminar la tarjeta de ${banco?.nombreBanco}?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        tarjetaAEliminar?.let { viewModel.eliminarTarjeta(it) }
                        mostrarDialogoEliminar = false
                        tarjetaAEliminar = null
                    }
                ) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    mostrarDialogoEliminar = false
                    tarjetaAEliminar = null
                }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Diálogo de confirmación de pago
    if (mostrarDialogoConfirmarPago && tarjetaAPagar != null && bancoTarjetaAPagar != null) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoConfirmarPago = false },
            icon = { Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF2E7D32)) },
            title = { Text("Confirmar Pago") },
            text = {
                Column {
                    Text("¿Ya pagaste la deuda de ${bancoTarjetaAPagar?.nombreBanco}?")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Monto: ${formatoMoneda(tarjetaAPagar!!.deudaTotal)}",
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Fecha límite: ${tarjetaAPagar!!.fechaLimitePago}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Este registro se moverá al historial.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            viewModel.marcarComoPagadaYGuardarHistorial(
                                tarjeta = tarjetaAPagar!!,
                                banco = bancoTarjetaAPagar!!,
                                onCompletado = {
                                    mostrarDialogoConfirmarPago = false
                                    tarjetaAPagar = null
                                    bancoTarjetaAPagar = null
                                }
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2E7D32)
                    )
                ) {
                    Text("Sí, Pagada")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    mostrarDialogoConfirmarPago = false
                    tarjetaAPagar = null
                    bancoTarjetaAPagar = null
                }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

private fun formatoMoneda(monto: Double): String {
    val formato = NumberFormat.getCurrencyInstance(Locale("es", "MX"))
    return formato.format(monto)
}