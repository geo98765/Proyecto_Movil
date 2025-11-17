package com.example.controltarjetas.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
fun PantallaListaAhorros(
    viewModel: AhorroViewModel,
    institucionViewModel: InstitucionFinancieraViewModel,
    onAgregarAhorro: () -> Unit,
    onEditarAhorro: (Ahorro) -> Unit,
    onNavigateInstituciones: () -> Unit,
    onOpenDrawer: () -> Unit
) {
    val ahorros by viewModel.todosAhorros.collectAsState(initial = emptyList())
    val instituciones by institucionViewModel.todasInstituciones.collectAsState(initial = emptyList())
    val totalesPorTipo by viewModel.totalesPorTipo.collectAsState(initial = emptyList())

    var mostrarDialogoEliminar by remember { mutableStateOf(false) }
    var ahorroAEliminar by remember { mutableStateOf<Ahorro?>(null) }
    var tipoFiltroSeleccionado by remember { mutableStateOf<String?>(null) }

    // Crear mapa de instituciones por ID
    val institucionesMap = remember(instituciones) {
        instituciones.associateBy { it.id }
    }

    // Filtrar ahorros según el tipo seleccionado
    val ahorrosFiltrados = remember(ahorros, tipoFiltroSeleccionado, instituciones) {
        if (tipoFiltroSeleccionado == null) {
            ahorros
        } else {
            ahorros.filter { ahorro ->
                val institucion = institucionesMap[ahorro.institucionId]
                institucion?.tipoInversion == tipoFiltroSeleccionado
            }
        }
    }

    // Calcular total general
    val totalGeneral = remember(ahorrosFiltrados, instituciones) {
        ahorrosFiltrados.sumOf { ahorro ->
            val institucion = institucionesMap[ahorro.institucionId]
            ahorro.calcularValorTotal(institucion?.tipoInversion ?: "")
        }
    }

    // Calcular rendimientos solo de inversiones con rendimiento
    val rendimientosTotales = remember(ahorrosFiltrados, instituciones) {
        ahorrosFiltrados.filter { ahorro ->
            val institucion = institucionesMap[ahorro.institucionId]
            institucion?.rendimientoAnual != null && institucion.rendimientoAnual > 0.0
        }.sumOf { ahorro ->
            val institucion = institucionesMap[ahorro.institucionId]!!
            val valorInicial = ahorro.calcularValorTotal(institucion.tipoInversion)
            val diasTranscurridos = ChronoUnit.DAYS.between(
                LocalDate.parse(ahorro.fechaCreacion),
                LocalDate.now()
            ).toInt()

            calcularRendimientoTotal(valorInicial, institucion.rendimientoAnual!!, diasTranscurridos)
        }
    }

    // Calcular rendimientos de hoy
    val rendimientosHoy = remember(ahorrosFiltrados, instituciones) {
        ahorrosFiltrados.filter { ahorro ->
            val institucion = institucionesMap[ahorro.institucionId]
            institucion?.rendimientoAnual != null && institucion.rendimientoAnual > 0.0
        }.sumOf { ahorro ->
            val institucion = institucionesMap[ahorro.institucionId]!!
            val valorInicial = ahorro.calcularValorTotal(institucion.tipoInversion)

            calcularRendimientoDiario(valorInicial, institucion.rendimientoAnual!!)
        }
    }

    // Calcular tasa de rendimiento promedio
    val tasaRendimientoPromedio = remember(ahorrosFiltrados, instituciones) {
        val ahorrosConRendimiento = ahorrosFiltrados.filter { ahorro ->
            val institucion = institucionesMap[ahorro.institucionId]
            institucion?.rendimientoAnual != null && institucion.rendimientoAnual > 0.0
        }

        if (ahorrosConRendimiento.isEmpty()) {
            0.0
        } else {
            ahorrosConRendimiento.sumOf { ahorro ->
                val institucion = institucionesMap[ahorro.institucionId]!!
                institucion.rendimientoAnual ?: 0.0
            } / ahorrosConRendimiento.size
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Ahorros") },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(Icons.Default.Menu, "Abrir menú")
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateInstituciones) {
                        Icon(Icons.Default.AccountBalance, "Instituciones")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAgregarAhorro,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, "Agregar ahorro")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // Card principal estilo DiDi Cuenta
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // Saldo Total
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    "Saldo Total (MXN$)",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                )
                                Icon(
                                    Icons.Default.Visibility,
                                    null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                formatoMoneda(totalGeneral),
                                style = MaterialTheme.typography.displaySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        Divider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f))

                        // Rendimientos
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    "Rendimientos de hoy",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                )
                                Text(
                                    if (rendimientosHoy > 0) "Por actualizar" else formatoMoneda(0.0),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    "Rendimientos totales",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                )
                                Text(
                                    formatoMoneda(rendimientosTotales),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        // Tasa de rendimiento
                        if (tasaRendimientoPromedio > 0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Tasa de rendimiento promedio",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        Icons.Default.TrendingUp,
                                        null,
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        String.format("%.2f%%", tasaRendimientoPromedio),
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Gráfica de rendimientos históricos
            if (ahorrosFiltrados.any { ahorro ->
                    val institucion = institucionesMap[ahorro.institucionId]
                    institucion?.rendimientoAnual != null && institucion.rendimientoAnual > 0.0
                }) {
                item {
                    GraficaRendimientosMejorada(
                        ahorros = ahorrosFiltrados,
                        instituciones = instituciones
                    )
                }
            }

            // Filtros por tipo de inversión
            if (instituciones.isNotEmpty()) {
                item {
                    FiltrosTipoInversion(
                        instituciones = instituciones,
                        tipoSeleccionado = tipoFiltroSeleccionado,
                        onTipoSeleccionado = { tipo ->
                            tipoFiltroSeleccionado = if (tipoFiltroSeleccionado == tipo) null else tipo
                        }
                    )
                }
            }

            // Resumen por tipo
            if (totalesPorTipo.isNotEmpty() && tipoFiltroSeleccionado == null) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                "Distribución por tipo",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            totalesPorTipo.forEach { tipo ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = when (tipo.tipoInversion) {
                                                "Tarjeta" -> Icons.Default.CreditCard
                                                "Acciones" -> Icons.Default.TrendingUp
                                                "Cripto" -> Icons.Default.CurrencyBitcoin
                                                "CETES" -> Icons.Default.AccountBalance
                                                else -> Icons.Default.Savings
                                            },
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp),
                                            tint = colorPorTipo(tipo.tipoInversion)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            tipo.tipoInversion,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                    Text(
                                        formatoMoneda(tipo.total),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Lista de ahorros
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Mis Inversiones",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (ahorrosFiltrados.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
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
                                Icons.Default.Savings,
                                null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                if (tipoFiltroSeleccionado != null)
                                    "No tienes inversiones de tipo $tipoFiltroSeleccionado"
                                else
                                    "No tienes ahorros registrados",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Agrega tu primer ahorro o inversión",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(ahorrosFiltrados, key = { it.id }) { ahorro ->
                    val institucion = institucionesMap[ahorro.institucionId]
                    if (institucion != null) {
                        AhorroCardMejorado(
                            ahorro = ahorro,
                            institucion = institucion,
                            onEditClick = { onEditarAhorro(ahorro) },
                            onDeleteClick = {
                                ahorroAEliminar = ahorro
                                mostrarDialogoEliminar = true
                            }
                        )
                    }
                }
            }
        }
    }

    // Diálogo de confirmación para eliminar
    if (mostrarDialogoEliminar && ahorroAEliminar != null) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoEliminar = false },
            icon = {
                Icon(
                    Icons.Default.Delete,
                    null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text("Eliminar ahorro") },
            text = {
                Text("¿Estás seguro de eliminar \"${ahorroAEliminar?.nombre}\"?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        ahorroAEliminar?.let {
                            viewModel.eliminar(it)
                        }
                        mostrarDialogoEliminar = false
                        ahorroAEliminar = null
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Eliminar")
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FiltrosTipoInversion(
    instituciones: List<InstitucionFinanciera>,
    tipoSeleccionado: String?,
    onTipoSeleccionado: (String) -> Unit
) {
    val tiposUnicos = remember(instituciones) {
        instituciones.map { it.tipoInversion }.distinct()
    }

    if (tiposUnicos.isNotEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                "Filtrar por tipo",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tiposUnicos.forEach { tipo ->
                    FilterChip(
                        selected = tipoSeleccionado == tipo,
                        onClick = { onTipoSeleccionado(tipo) },
                        label = { Text(tipo) },
                        leadingIcon = {
                            Icon(
                                imageVector = when (tipo) {
                                    "Tarjeta" -> Icons.Default.CreditCard
                                    "Acciones" -> Icons.Default.TrendingUp
                                    "Cripto" -> Icons.Default.CurrencyBitcoin
                                    "CETES" -> Icons.Default.AccountBalance
                                    else -> Icons.Default.Savings
                                },
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AhorroCardMejorado(
    ahorro: Ahorro,
    institucion: InstitucionFinanciera,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val valorTotal = ahorro.calcularValorTotal(institucion.tipoInversion)

    // Calcular rendimiento si existe
    val rendimiento = if (institucion.rendimientoAnual != null && institucion.rendimientoAnual > 0) {
        val diasTranscurridos = ChronoUnit.DAYS.between(
            LocalDate.parse(ahorro.fechaCreacion),
            LocalDate.now()
        ).toInt()
        calcularRendimientoTotal(valorTotal, institucion.rendimientoAnual, diasTranscurridos)
    } else null

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                // Logo e información
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
                                .background(colorPorTipo(institucion.tipoInversion)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when (institucion.tipoInversion) {
                                    "Tarjeta" -> Icons.Default.CreditCard
                                    "Acciones" -> Icons.Default.TrendingUp
                                    "Cripto" -> Icons.Default.CurrencyBitcoin
                                    "CETES" -> Icons.Default.AccountBalance
                                    else -> Icons.Default.Savings
                                },
                                contentDescription = null,
                                tint = Color.White
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
                        Text(
                            institucion.tipoInversion,
                            style = MaterialTheme.typography.bodySmall,
                            color = colorPorTipo(institucion.tipoInversion)
                        )
                    }
                }

                // Acciones
                Row {
                    IconButton(onClick = onEditClick) {
                        Icon(Icons.Default.Edit, "Editar", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = onDeleteClick) {
                        Icon(Icons.Default.Delete, "Eliminar", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Monto y rendimiento
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        "Monto Invertido",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        formatoMoneda(valorTotal),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (rendimiento != null) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            "Rendimiento Generado",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.TrendingUp,
                                null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                formatoMoneda(rendimiento),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // Rendimiento anual si existe
            if (institucion.rendimientoAnual != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.Percent,
                            null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "Tasa: ${institucion.rendimientoAnual}% anual",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Descripción
            if (!ahorro.descripcion.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            Icons.Default.Info,
                            null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            ahorro.descripcion,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun GraficaRendimientosMejorada(
    ahorros: List<Ahorro>,
    instituciones: List<InstitucionFinanciera>
) {
    val ahorrosConRendimiento = ahorros.filter { ahorro ->
        val institucion = instituciones.find { it.id == ahorro.institucionId }
        institucion?.rendimientoAnual != null && institucion.rendimientoAnual > 0.0
    }

    if (ahorrosConRendimiento.isNotEmpty()) {
        var periodoSeleccionado by remember { mutableStateOf("Mensual") }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
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

                val rendimientoTotal = ahorrosConRendimiento.sumOf { ahorro ->
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

                ahorrosConRendimiento.forEach { ahorro ->
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
}

@Composable
fun colorPorTipo(tipo: String): Color {
    return when (tipo) {
        "Tarjeta" -> Color(0xFF4CAF50) // Verde
        "Acciones" -> Color(0xFF2196F3) // Azul
        "Cripto" -> Color(0xFFFF9800) // Naranja
        "CETES" -> Color(0xFF9C27B0) // Púrpura
        else -> MaterialTheme.colorScheme.primary
    }
}

private fun formatoMoneda(monto: Double): String {
    val formato = NumberFormat.getCurrencyInstance(Locale("es", "MX"))
    return formato.format(monto)
}

// Funciones auxiliares para calcular rendimientos
private fun calcularRendimientoDiario(montoInicial: Double, tasaAnual: Double): Double {
    return montoInicial * (tasaAnual / 100) / 365
}

private fun calcularRendimientoTotal(montoInicial: Double, tasaAnual: Double, diasTranscurridos: Int): Double {
    val rendimientoDiario = calcularRendimientoDiario(montoInicial, tasaAnual)
    return rendimientoDiario * diasTranscurridos
}