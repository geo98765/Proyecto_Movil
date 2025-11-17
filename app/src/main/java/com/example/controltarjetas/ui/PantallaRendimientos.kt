package com.example.controltarjetas.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.controltarjetas.AhorroViewModel
import com.example.controltarjetas.InstitucionFinancieraViewModel
import com.example.controltarjetas.data.Ahorro
import com.example.controltarjetas.data.InstitucionFinanciera
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.core.entry.entryModelOf
import java.io.File
import java.text.NumberFormat
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.*

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaRendimientos(
    viewModel: AhorroViewModel,
    institucionViewModel: InstitucionFinancieraViewModel,
    onOpenDrawer: () -> Unit
) {
    val ahorros by viewModel.todosAhorros.collectAsState(initial = emptyList())
    val instituciones by institucionViewModel.todasInstituciones.collectAsState(initial = emptyList())

    // Estado para el menú desplegable de navegación
    var expandedMenu by remember { mutableStateOf(false) }

    // Estado para el diálogo de simulación
    var mostrarSimulador by remember { mutableStateOf(false) }

    // Filtrar solo los ahorros que tienen rendimiento
    val ahorrosConRendimiento = remember(ahorros, instituciones) {
        ahorros.filter { ahorro ->
            val institucion = instituciones.find { it.id == ahorro.institucionId }
            institucion?.rendimientoAnual != null && institucion.rendimientoAnual > 0.0
        }
    }

    // Calcular rendimientos totales
    val rendimientoDiarioTotal = remember(ahorrosConRendimiento, instituciones) {
        ahorrosConRendimiento.sumOf { ahorro ->
            val institucion = instituciones.find { it.id == ahorro.institucionId }!!
            val valorInicial = ahorro.calcularValorTotal(institucion.tipoInversion)
            valorInicial * (institucion.rendimientoAnual!! / 100) / 365
        }
    }

    val rendimientoSemanalTotal = rendimientoDiarioTotal * 7
    val rendimientoMensualTotal = rendimientoDiarioTotal * 30

    val rendimientoAcumuladoTotal = remember(ahorrosConRendimiento, instituciones) {
        ahorrosConRendimiento.sumOf { ahorro ->
            val institucion = instituciones.find { it.id == ahorro.institucionId }!!
            val valorInicial = ahorro.calcularValorTotal(institucion.tipoInversion)
            val diasTranscurridos = ChronoUnit.DAYS.between(
                LocalDate.parse(ahorro.fechaCreacion),
                LocalDate.now()
            ).toInt()

            val rendimientoDiario = valorInicial * (institucion.rendimientoAnual!! / 100) / 365
            rendimientoDiario * diasTranscurridos
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Rendimientos") },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(Icons.Default.Menu, "Abrir menú")
                    }
                },
                actions = {
                    // Menú desplegable para navegación
                    Box {
                        IconButton(onClick = { expandedMenu = true }) {
                            Icon(Icons.Default.MoreVert, "Opciones")
                        }
                        DropdownMenu(
                            expanded = expandedMenu,
                            onDismissRequest = { expandedMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Historial de Depósitos") },
                                onClick = {
                                    expandedMenu = false
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.AccountBalance, null)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Configuración") },
                                onClick = {
                                    expandedMenu = false
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Settings, null)
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Card con resumen de rendimientos
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Total de rendimientos
                        Column {
                            Text(
                                "Rendimientos totales",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                            Text(
                                formatoMoneda(rendimientoAcumuladoTotal),
                                style = MaterialTheme.typography.displaySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        Divider()

                        // Rendimientos por período
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            RendimientoCardInfo(
                                titulo = "Rendimientos de hoy",
                                valor = formatoMoneda(rendimientoDiarioTotal),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            RendimientoCardInfo(
                                titulo = "Semanal",
                                valor = formatoMoneda(rendimientoSemanalTotal),
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            RendimientoCardInfo(
                                titulo = "Mensual",
                                valor = formatoMoneda(rendimientoMensualTotal),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Botón para abrir simulador
            item {
                Button(
                    onClick = { mostrarSimulador = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Icon(Icons.Default.Calculate, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Hacer Simulación")
                }
            }

            // Gráfica de rendimientos históricos
            if (ahorrosConRendimiento.isNotEmpty()) {
                item {
                    Text(
                        "Historial de Rendimientos",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                item {
                    GraficaRendimientosHistoricos(
                        ahorros = ahorrosConRendimiento,
                        instituciones = instituciones
                    )
                }
            }

            // Lista de inversiones con rendimiento
            item {
                Text(
                    "Detalle por Inversión",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            if (ahorrosConRendimiento.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.TrendingUp,
                                null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "No tienes inversiones con rendimiento",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Agrega una inversión que genere rendimientos",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(ahorrosConRendimiento) { ahorro ->
                    val institucion = instituciones.find { it.id == ahorro.institucionId }
                    if (institucion != null) {
                        InversionConRendimientoCard(
                            ahorro = ahorro,
                            institucion = institucion
                        )
                    }
                }
            }
        }
    }

    // Diálogo del simulador
    if (mostrarSimulador) {
        DialogoSimuladorRendimientos(
            onDismiss = { mostrarSimulador = false }
        )
    }
}

@Composable
fun RendimientoCardInfo(
    titulo: String,
    valor: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            titulo,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
        )
        Text(
            valor,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun InversionConRendimientoCard(
    ahorro: Ahorro,
    institucion: InstitucionFinanciera
) {
    val valorInicial = ahorro.calcularValorTotal(institucion.tipoInversion)
    val diasTranscurridos = ChronoUnit.DAYS.between(
        LocalDate.parse(ahorro.fechaCreacion),
        LocalDate.now()
    ).toInt()

    val rendimientoDiario = valorInicial * (institucion.rendimientoAnual!! / 100) / 365
    val rendimientoTotal = rendimientoDiario * diasTranscurridos

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                // Logo
                if (institucion.logoUri != null) {
                    Image(
                        painter = rememberAsyncImagePainter(File(institucion.logoUri)),
                        contentDescription = null,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Savings,
                            null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Column {
                    Text(
                        ahorro.nombre,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        institucion.nombreInstitucion,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.TrendingUp,
                            null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "${institucion.rendimientoAnual}% anual",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "Rendimiento",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    formatoMoneda(rendimientoTotal),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun GraficaRendimientosHistoricos(
    ahorros: List<Ahorro>,
    instituciones: List<InstitucionFinanciera>
) {
    var periodoSeleccionado by remember { mutableStateOf("Mensual") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                "Resumen de Rendimientos",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Selector de período
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Diario", "Semanal", "Mensual").forEach { periodo ->
                    FilterChip(
                        selected = periodoSeleccionado == periodo,
                        onClick = { periodoSeleccionado = periodo },
                        label = { Text(periodo) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Calcular rendimientos según el período
            val diasPeriodo = when (periodoSeleccionado) {
                "Diario" -> 1
                "Semanal" -> 7
                "Mensual" -> 30
                else -> 1
            }

            val rendimientoTotal = ahorros.sumOf { ahorro ->
                val institucion = instituciones.find { it.id == ahorro.institucionId }!!
                val valorInicial = ahorro.calcularValorTotal(institucion.tipoInversion)
                val rendimientoAnual = institucion.rendimientoAnual ?: 0.0
                valorInicial * (rendimientoAnual / 100) * (diasPeriodo / 365.0)
            }

            // Mostrar el rendimiento calculado
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Rendimiento $periodoSeleccionado Estimado",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        formatoMoneda(rendimientoTotal),
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Lista de inversiones con sus rendimientos
            Text(
                "Desglose por inversión:",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            ahorros.forEach { ahorro ->
                val institucion = instituciones.find { it.id == ahorro.institucionId }
                if (institucion != null) {
                    val valorInicial = ahorro.calcularValorTotal(institucion.tipoInversion)
                    val rendimiento = valorInicial * (institucion.rendimientoAnual!! / 100) * (diasPeriodo / 365.0)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                Icons.Default.TrendingUp,
                                null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    ahorro.nombre,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    "${institucion.rendimientoAnual}% anual",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                        }
                        Text(
                            formatoMoneda(rendimiento),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogoSimuladorRendimientos(
    onDismiss: () -> Unit
) {
    var montoInicial by remember { mutableStateOf("") }
    var tasaAnual by remember { mutableStateOf("") }
    var periodoSeleccionado by remember { mutableStateOf("Diario") }
    var expandedPeriodo by remember { mutableStateOf(false) }

    val periodos = listOf("Diario", "Semanal", "Mensual", "Anual")

    // Calcular rendimiento proyectado
    val rendimientoProyectado = remember(montoInicial, tasaAnual, periodoSeleccionado) {
        val monto = montoInicial.toDoubleOrNull() ?: 0.0
        val tasa = tasaAnual.toDoubleOrNull() ?: 0.0

        when (periodoSeleccionado) {
            "Diario" -> monto * (tasa / 100) / 365
            "Semanal" -> monto * (tasa / 100) / 52
            "Mensual" -> monto * (tasa / 100) / 12
            "Anual" -> monto * (tasa / 100)
            else -> 0.0
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Calculate, null)
                Text("Simulador de Rendimientos")
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = montoInicial,
                    onValueChange = { montoInicial = it },
                    label = { Text("Monto Inicial") },
                    prefix = { Text("$") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = tasaAnual,
                    onValueChange = { tasaAnual = it },
                    label = { Text("Tasa de Rendimiento Anual") },
                    suffix = { Text("%") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(
                    expanded = expandedPeriodo,
                    onExpandedChange = { expandedPeriodo = it }
                ) {
                    OutlinedTextField(
                        value = periodoSeleccionado,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Período de Cálculo") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPeriodo)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedPeriodo,
                        onDismissRequest = { expandedPeriodo = false }
                    ) {
                        periodos.forEach { periodo ->
                            DropdownMenuItem(
                                text = { Text(periodo) },
                                onClick = {
                                    periodoSeleccionado = periodo
                                    expandedPeriodo = false
                                }
                            )
                        }
                    }
                }

                if (montoInicial.toDoubleOrNull() != null && tasaAnual.toDoubleOrNull() != null) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                "Rendimiento $periodoSeleccionado Proyectado",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                            Text(
                                formatoMoneda(rendimientoProyectado),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    )
}

private fun formatoMoneda(monto: Double): String {
    val formato = NumberFormat.getCurrencyInstance(Locale("es", "MX"))
    return formato.format(monto)
}