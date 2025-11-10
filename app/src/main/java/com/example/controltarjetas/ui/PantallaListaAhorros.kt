package com.example.controltarjetas.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
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
import com.example.controltarjetas.AhorroViewModel
import com.example.controltarjetas.data.*
import com.example.controltarjetas.InstitucionFinancieraViewModel


@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PantallaListaAhorros(
    viewModel: AhorroViewModel = viewModel(),
    institucionViewModel: InstitucionFinancieraViewModel = viewModel(),
    onAgregarAhorro: () -> Unit,
    onEditarAhorro: (Ahorro) -> Unit,
    onNavigateInstituciones: () -> Unit,
    onOpenDrawer: () -> Unit,
    onNavigateRendimientos: (() -> Unit)? = null  // ← AGREGAR ESTE PARÁMETRO
) {
    val ahorros by viewModel.todosAhorros.collectAsState(initial = emptyList())
    val instituciones by viewModel.todasInstituciones.collectAsState(initial = emptyList())
    val totalesPorTipo by viewModel.totalesPorTipo.collectAsState(initial = emptyList())
    val estadoFiltros by viewModel.estadoFiltros.collectAsState()

    var mostrarFiltros by remember { mutableStateOf(false) }
    var mostrarOrdenamiento by remember { mutableStateOf(false) }
    var busqueda by remember { mutableStateOf("") }

    // Aplicar filtros
    val ahorrosFiltrados = remember(ahorros, instituciones, estadoFiltros) {
        viewModel.filtrarAhorros(ahorros, instituciones)
    }

    // Calcular total filtrado
    val totalFiltrado = remember(ahorrosFiltrados, instituciones) {
        val mapaInstituciones = instituciones.associateBy { it.id }
        ahorrosFiltrados.sumOf { ahorro ->
            val institucion = mapaInstituciones[ahorro.institucionId]
            ahorro.calcularValorTotal(institucion?.tipoInversion ?: "")
        }
    }

    // Contar filtros activos
    val filtrosActivos = remember(estadoFiltros) {
        var count = 0
        if (estadoFiltros.tipoInversion != FiltroTipoInversion.TODOS) count++
        if (estadoFiltros.institucionId != null) count++
        if (estadoFiltros.busqueda.isNotBlank()) count++
        count
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Ahorros") },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(Icons.Default.Menu, "Menú")
                    }
                },
                actions = {
                    // NUEVO: Botón de rendimientos
                    if (onNavigateRendimientos != null) {
                        IconButton(onClick = onNavigateRendimientos) {
                            Icon(
                                Icons.Default.TrendingUp,
                                contentDescription = "Ver Rendimientos",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }

                    // Botón de instituciones (si ya lo tienes)
                    IconButton(onClick = onNavigateInstituciones) {
                        Icon(
                            Icons.Default.AccountBalance,
                            contentDescription = "Instituciones",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAgregarAhorro,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Agregar") }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Barra de búsqueda
            item {
                OutlinedTextField(
                    value = busqueda,
                    onValueChange = {
                        busqueda = it
                        viewModel.actualizarFiltros(
                            estadoFiltros.copy(busqueda = it)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    placeholder = { Text("Buscar por nombre, descripción o institución...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (busqueda.isNotEmpty()) {
                            IconButton(onClick = {
                                busqueda = ""
                                viewModel.actualizarFiltros(
                                    estadoFiltros.copy(busqueda = "")
                                )
                            }) {
                                Icon(Icons.Default.Clear, contentDescription = "Limpiar")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // Panel de filtros expandible
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
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Filtros",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                if (filtrosActivos > 0) {
                                    TextButton(
                                        onClick = {
                                            viewModel.actualizarFiltros(
                                                EstadoFiltrosAhorro(
                                                    orden = estadoFiltros.orden
                                                )
                                            )
                                            busqueda = ""
                                        }
                                    ) {
                                        Icon(
                                            Icons.Default.Clear,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Limpiar")
                                    }
                                }
                            }

                            // Filtro por tipo de inversión
                            Column {
                                Text(
                                    "Tipo de Inversión",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    FiltroTipoInversion.values().forEach { tipo ->
                                        FilterChip(
                                            selected = estadoFiltros.tipoInversion == tipo,
                                            onClick = {
                                                viewModel.actualizarFiltros(
                                                    estadoFiltros.copy(tipoInversion = tipo)
                                                )
                                            },
                                            label = {
                                                Text(
                                                    when (tipo) {
                                                        FiltroTipoInversion.TODOS -> "Todos"
                                                        FiltroTipoInversion.TARJETA -> "Tarjeta"
                                                        FiltroTipoInversion.ACCIONES -> "Acciones"
                                                        FiltroTipoInversion.CRIPTO -> "Cripto"
                                                        FiltroTipoInversion.CETES -> "CETES"
                                                    }
                                                )
                                            },
                                            leadingIcon = if (estadoFiltros.tipoInversion == tipo) {
                                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                            } else null
                                        )
                                    }
                                }
                            }

                            // Filtro por institución
                            if (instituciones.isNotEmpty()) {
                                Column {
                                    Text(
                                        "Institución Financiera",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    FlowRow(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        FilterChip(
                                            selected = estadoFiltros.institucionId == null,
                                            onClick = {
                                                viewModel.actualizarFiltros(
                                                    estadoFiltros.copy(institucionId = null)
                                                )
                                            },
                                            label = { Text("Todas") },
                                            leadingIcon = if (estadoFiltros.institucionId == null) {
                                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                            } else null
                                        )
                                        instituciones.forEach { institucion ->
                                            FilterChip(
                                                selected = estadoFiltros.institucionId == institucion.id,
                                                onClick = {
                                                    viewModel.actualizarFiltros(
                                                        estadoFiltros.copy(
                                                            institucionId = if (estadoFiltros.institucionId == institucion.id)
                                                                null
                                                            else
                                                                institucion.id
                                                        )
                                                    )
                                                },
                                                label = { Text(institucion.nombreInstitucion) },
                                                leadingIcon = if (estadoFiltros.institucionId == institucion.id) {
                                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                                } else null
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Resumen de total
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "Total Invertido",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                            Text(
                                formatoMoneda2(totalFiltrado),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            if (filtrosActivos > 0) {
                                Text(
                                    "${ahorrosFiltrados.size} de ${ahorros.size} inversiones",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                                )
                            }
                        }
                        Icon(
                            Icons.Default.Savings,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Distribución por tipo (si no hay filtros de tipo activos)
            if (totalesPorTipo.isNotEmpty() && estadoFiltros.tipoInversion == FiltroTipoInversion.TODOS) {
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
                                "Distribución por Tipo",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            totalesPorTipo.forEach { tipo ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
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
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            tipo.tipoInversion,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                    Text(
                                        formatoMoneda2(tipo.total),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Encabezado de lista
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Mis Inversiones",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "${ahorrosFiltrados.size} ${if (ahorrosFiltrados.size == 1) "inversión" else "inversiones"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            // Lista de ahorros
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
                                Icons.Default.SearchOff,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.secondary
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "No se encontraron inversiones",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                if (filtrosActivos > 0)
                                    "Intenta ajustar los filtros"
                                else
                                    "Agrega tu primera inversión",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            } else {
                items(ahorrosFiltrados) { ahorro ->
                    val institucion = instituciones.find { it.id == ahorro.institucionId }
                    TarjetaAhorro(
                        ahorro = ahorro,
                        institucion = institucion,
                        onClick = { onEditarAhorro(ahorro) }
                    )
                }
            }

            // Espacio al final
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    // Diálogo de ordenamiento
    if (mostrarOrdenamiento) {
        AlertDialog(
            onDismissRequest = { mostrarOrdenamiento = false },
            title = { Text("Ordenar por") },
            text = {
                Column {
                    OrdenAhorro.values().forEach { orden ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = estadoFiltros.orden == orden,
                                onClick = {
                                    viewModel.actualizarFiltros(
                                        estadoFiltros.copy(orden = orden)
                                    )
                                    mostrarOrdenamiento = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                when (orden) {
                                    OrdenAhorro.MONTO_DESC -> "Monto (Mayor a Menor)"
                                    OrdenAhorro.MONTO_ASC -> "Monto (Menor a Mayor)"
                                    OrdenAhorro.FECHA_DESC -> "Fecha (Más Reciente)"
                                    OrdenAhorro.FECHA_ASC -> "Fecha (Más Antigua)"
                                    OrdenAhorro.NOMBRE_ASC -> "Nombre (A-Z)"
                                    OrdenAhorro.NOMBRE_DESC -> "Nombre (Z-A)"
                                }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { mostrarOrdenamiento = false }) {
                    Text("Cerrar")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TarjetaAhorro(
    ahorro: Ahorro,
    institucion: InstitucionFinanciera?,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        onClick = onClick,
        elevation = CardDefaults.cardElevation(2.dp)
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        ahorro.nombre,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (institucion != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                institucion.nombreInstitucion,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Text("•", style = MaterialTheme.typography.bodySmall)
                            Text(
                                institucion.tipoInversion,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        formatoMoneda2(ahorro.calcularValorTotal(institucion?.tipoInversion ?: "")),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (institucion?.rendimientoAnual != null) {
                        Text(
                            "${institucion.rendimientoAnual}% anual",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }
    }
}