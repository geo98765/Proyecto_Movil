package com.example.controltarjetas.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.controltarjetas.TarjetaViewModel
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.core.entry.entryModelOf
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaEstadisticas(
    viewModel: TarjetaViewModel = viewModel(),
    onNavigateBack: () -> Unit
) {
    val deudasPorMes by viewModel.deudasPorMes.collectAsState(initial = emptyList())
    val deudasPorBanco by viewModel.deudasPorBanco.collectAsState(initial = emptyList())
    val pagosPorFecha by viewModel.pagosPorFecha.collectAsState(initial = emptyList())
    val deudaTotal by viewModel.deudaTotal.collectAsState(initial = 0.0)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Estadísticas") },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Resumen general
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
                        text = "Deuda Total Actual",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = formatoMoneda(deudaTotal ?: 0.0),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Gráfica de deudas por mes
            if (deudasPorMes.isNotEmpty()) {
                Text(
                    text = "Deudas Mensuales",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        val chartEntryModel = entryModelOf(
                            *deudasPorMes.reversed().mapIndexed { index, deuda ->
                                index.toFloat() to deuda.total.toFloat()
                            }.toTypedArray()
                        )

                        Chart(
                            chart = columnChart(),
                            model = chartEntryModel,
                            startAxis = rememberStartAxis(),
                            bottomAxis = rememberBottomAxis(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Leyenda
                        deudasPorMes.reversed().forEach { deuda ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = deuda.mesPeriodo,
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    text = formatoMoneda(deuda.total),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Deudas por banco
            if (deudasPorBanco.isNotEmpty()) {
                Text(
                    text = "Deudas por Banco",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                deudasPorBanco.forEach { deuda ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = deuda.nombreBanco,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = formatoMoneda(deuda.total),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // Historial de pagos
            if (pagosPorFecha.isNotEmpty()) {
                Text(
                    text = "Historial de Pagos",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        val chartEntryModel = entryModelOf(
                            *pagosPorFecha.reversed().mapIndexed { index, pago ->
                                index.toFloat() to pago.total.toFloat()
                            }.toTypedArray()
                        )

                        Chart(
                            chart = lineChart(),
                            model = chartEntryModel,
                            startAxis = rememberStartAxis(),
                            bottomAxis = rememberBottomAxis(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        pagosPorFecha.reversed().take(5).forEach { pago ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = pago.fechaPago,
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    text = formatoMoneda(pago.total),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2E7D32)
                                )
                            }
                        }
                    }
                }
            }

            // Mensaje si no hay datos
            if (deudasPorMes.isEmpty() && deudasPorBanco.isEmpty() && pagosPorFecha.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No hay suficientes datos para mostrar estadísticas",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

private fun formatoMoneda(monto: Double): String {
    val formato = NumberFormat.getCurrencyInstance(Locale("es", "MX"))
    return formato.format(monto)
}