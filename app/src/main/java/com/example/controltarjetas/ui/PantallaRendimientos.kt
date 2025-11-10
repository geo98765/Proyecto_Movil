package com.example.controltarjetas.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.controltarjetas.*
import com.example.controltarjetas.data.*

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class, ExperimentalLayoutApi::class)
@Composable
fun PantallaRendimientos(
    viewModel: AhorroViewModel = viewModel(),
    onNavigateBack: (() -> Unit)? = null
) {
    val ahorros by viewModel.todosAhorros.collectAsState(initial = emptyList())
    val instituciones by viewModel.todasInstituciones.collectAsState(initial = emptyList())

    // Estado de filtros
    var periodoSeleccionado by remember { mutableStateOf(PeriodoRendimiento.MENSUAL) }
    var institucionFiltro by remember { mutableStateOf<Int?>(null) }
    var tipoFiltro by remember { mutableStateOf<String?>(null) }
    var mostrarFiltros by remember { mutableStateOf(false) }

    // Obtener ahorros con rendimiento
    val ahorrosConRendimiento = remember(ahorros, instituciones) {
        viewModel.obtenerAhorrosConRendimiento(ahorros, instituciones)
    }

    // Aplicar filtros
    val ahorrosFiltrados = remember(ahorrosConRendimiento, institucionFiltro, tipoFiltro) {
        var resultado = ahorrosConRendimiento

        institucionFiltro?.let { instId ->
            resultado = resultado.filter { it.institucion.id == instId }
        }

        tipoFiltro?.let { tipo ->
            resultado = resultado.filter { it.institucion.tipoInversion == tipo }
        }

        resultado
    }

    // Calcular totales
    val rendimientoTotal = remember(ahorrosFiltrados, periodoSeleccionado) {
        ahorrosFiltrados.sumOf {
            when (periodoSeleccionado) {
                PeriodoRendimiento.DIARIO -> it.rendimientos.diario
                PeriodoRendimiento.SEMANAL -> it.rendimientos.semanal
                PeriodoRendimiento.MENSUAL -> it.rendimientos.mensual
                PeriodoRendimiento.ANUAL -> it.rendimientos.anual
            }
        }
    }

    val montoTotalInvertido = remember(ahorrosFiltrados) {
        ahorrosFiltrados.sumOf { it.monto }
    }

    // Tipos disponibles (solo Tarjeta y CETES)
    val tiposDisponibles = listOf("Tarjeta", "CETES")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Rendimientos") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    IconButton(onClick = { mostrarFiltros = !mostrarFiltros }) {
                        Badge(
                            containerColor = if (institucionFiltro != null || tipoFiltro != null)
                                MaterialTheme.colorScheme.error
                            else
                                Color.Transparent
                        ) {
                            Icon(
                                Icons.Default.FilterList,
                                contentDescription = "Filtros"
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Tarjetas de resumen
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Tarjeta de rendimiento total
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.TrendingUp,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(28.dp)
                                )
                                Text(
                                    "Rendimiento Total",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                formatoMoneda2(rendimientoTotal),
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                "Por ${periodoSeleccionado.name.lowercase()}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                        }
                    }

                    // Tarjeta de inversión total
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    "Total Invertido",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                                )
                                Text(
                                    formatoMoneda2(montoTotalInvertido),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                            Icon(
                                Icons.Default.AccountBalance,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp),
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            }

            // Sección de filtros expandibles
            item {
                AnimatedVisibility(
                    visible = mostrarFiltros,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                "Filtros",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            // Filtro por tipo
                            Column {
                                Text(
                                    "Tipo de Inversión",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    FilterChip(
                                        selected = tipoFiltro == null,
                                        onClick = { tipoFiltro = null },
                                        label = { Text("Todos") }
                                    )
                                    tiposDisponibles.forEach { tipo ->
                                        FilterChip(
                                            selected = tipoFiltro == tipo,
                                            onClick = {
                                                tipoFiltro = if (tipoFiltro == tipo) null else tipo
                                            },
                                            label = { Text(tipo) }
                                        )
                                    }
                                }
                            }

                            // Filtro por institución
                            val institucionesFiltradas = instituciones.filter {
                                it.rendimientoAnual != null &&
                                        (it.tipoInversion == "Tarjeta" || it.tipoInversion == "CETES")
                            }

                            if (institucionesFiltradas.isNotEmpty()) {
                                Column {
                                    Text(
                                        "Institución",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                    FlowRow(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        FilterChip(
                                            selected = institucionFiltro == null,
                                            onClick = { institucionFiltro = null },
                                            label = { Text("Todas") }
                                        )
                                        institucionesFiltradas.forEach { inst ->
                                            FilterChip(
                                                selected = institucionFiltro == inst.id,
                                                onClick = {
                                                    institucionFiltro = if (institucionFiltro == inst.id) null else inst.id
                                                },
                                                label = { Text(inst.nombreInstitucion) }
                                            )
                                        }
                                    }
                                }
                            }

                            // Botón limpiar filtros
                            if (institucionFiltro != null || tipoFiltro != null) {
                                TextButton(
                                    onClick = {
                                        institucionFiltro = null
                                        tipoFiltro = null
                                    }
                                ) {
                                    Icon(Icons.Default.Clear, contentDescription = null)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Limpiar filtros")
                                }
                            }
                        }
                    }
                }
            }

            // Selector de período
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            "Período de Rendimiento",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            PeriodoRendimiento.values().forEach { periodo ->
                                FilterChip(
                                    selected = periodoSeleccionado == periodo,
                                    onClick = { periodoSeleccionado = periodo },
                                    label = { Text(periodo.name.lowercase().capitalize()) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            // Gráfica de rendimientos
            if (ahorrosFiltrados.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                "Distribución de Rendimientos",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            // Gráfica de barras simple
                            GraficaBarrasRendimientos(
                                ahorros = ahorrosFiltrados,
                                periodo = periodoSeleccionado
                            )
                        }
                    }
                }
            }

            // Lista de ahorros con rendimientos
            item {
                Text(
                    "Detalle por Inversión",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(16.dp)
                )
            }

            if (ahorrosFiltrados.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.secondary
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "No hay inversiones con rendimientos",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Solo las inversiones en Tarjetas y CETES con rendimiento configurado aparecen aquí",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            } else {
                items(ahorrosFiltrados) { ahorroConRend ->
                    TarjetaAhorroRendimiento(
                        ahorroConRendimiento = ahorroConRend,
                        periodo = periodoSeleccionado
                    )
                }
            }

            // Espacio al final
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun TarjetaAhorroRendimiento(
    ahorroConRendimiento: AhorroConRendimiento,
    periodo: PeriodoRendimiento
) {
    val rendimiento = when (periodo) {
        PeriodoRendimiento.DIARIO -> ahorroConRendimiento.rendimientos.diario
        PeriodoRendimiento.SEMANAL -> ahorroConRendimiento.rendimientos.semanal
        PeriodoRendimiento.MENSUAL -> ahorroConRendimiento.rendimientos.mensual
        PeriodoRendimiento.ANUAL -> ahorroConRendimiento.rendimientos.anual
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Cabecera
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        ahorroConRendimiento.ahorro.nombre,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        ahorroConRendimiento.institucion.nombreInstitucion,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                AssistChip(
                    onClick = { },
                    label = { Text(ahorroConRendimiento.institucion.tipoInversion) },
                    leadingIcon = {
                        Icon(
                            if (ahorroConRendimiento.institucion.tipoInversion == "Tarjeta")
                                Icons.Default.CreditCard
                            else
                                Icons.Default.AccountBalance,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Información de rendimiento
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        "Inversión",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        formatoMoneda2(ahorroConRendimiento.monto),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "Rendimiento ${periodo.name.lowercase()}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        formatoMoneda2(rendimiento),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Barra de progreso visual del rendimiento
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Percent,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    "${ahorroConRendimiento.institucion.rendimientoAnual}% anual",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun GraficaBarrasRendimientos(
    ahorros: List<AhorroConRendimiento>,
    periodo: PeriodoRendimiento
) {
    val maxRendimiento = ahorros.maxOfOrNull {
        when (periodo) {
            PeriodoRendimiento.DIARIO -> it.rendimientos.diario
            PeriodoRendimiento.SEMANAL -> it.rendimientos.semanal
            PeriodoRendimiento.MENSUAL -> it.rendimientos.mensual
            PeriodoRendimiento.ANUAL -> it.rendimientos.anual
        }
    } ?: 1.0

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ahorros.forEach { ahorro ->
            val rendimiento = when (periodo) {
                PeriodoRendimiento.DIARIO -> ahorro.rendimientos.diario
                PeriodoRendimiento.SEMANAL -> ahorro.rendimientos.semanal
                PeriodoRendimiento.MENSUAL -> ahorro.rendimientos.mensual
                PeriodoRendimiento.ANUAL -> ahorro.rendimientos.anual
            }

            val porcentaje = (rendimiento / maxRendimiento).toFloat()

            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        ahorro.ahorro.nombre,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        formatoMoneda2(rendimiento),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = porcentaje,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }
    }
}

// Función auxiliar para formato de moneda
fun formatoMoneda2(cantidad: Double): String {
    return "$${String.format("%,.2f", cantidad)}"
}