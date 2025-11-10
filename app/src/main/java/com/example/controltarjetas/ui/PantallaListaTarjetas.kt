package com.example.controltarjetas.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.controltarjetas.FiltroFecha
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
    onNavigateEstadisticas: () -> Unit,
    onNavigateConfiguracion: () -> Unit
) {
    val tarjetas by viewModel.todasLasTarjetas.collectAsState(initial = emptyList())
    val bancos by viewModel.todosBancos.collectAsState(initial = emptyList())
    val deudaTotal by viewModel.deudaTotal.collectAsState(initial = 0.0)

    val scope = rememberCoroutineScope()

    // Estado del filtro - Por defecto próximas 3 semanas
    var filtroSeleccionado by remember { mutableStateOf(FiltroFecha.PROXIMAS_3_SEMANAS) }
    var expandedFiltro by remember { mutableStateOf(false) }

    // Estado para el menú FAB
    var expandedFab by remember { mutableStateOf(false) }

    // Aplicar filtro a las tarjetas
    val tarjetasFiltradas = remember(tarjetas, filtroSeleccionado) {
        viewModel.filtrarTarjetasPorFecha(tarjetas, filtroSeleccionado)
    }

    var mostrarDialogoEliminar by remember { mutableStateOf(false) }
    var tarjetaAEliminar by remember { mutableStateOf<Tarjeta?>(null) }

    var mostrarDialogoConfirmarPago by remember { mutableStateOf(false) }
    var tarjetaAPagar by remember { mutableStateOf<Tarjeta?>(null) }
    var bancoTarjetaAPagar by remember { mutableStateOf<Banco?>(null) }

    var mostrarDialogoMSI by remember { mutableStateOf(false) }

    // Crear mapa de bancos por ID para búsqueda rápida
    val bancosMap = remember(bancos) {
        bancos.associateBy { it.id }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Control de Tarjetas",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = onNavigateEstadisticas) {
                        Icon(
                            Icons.Default.Star,
                            "Estadísticas",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    IconButton(onClick = onNavigateHistorial) {
                        Icon(
                            Icons.Default.Info,
                            "Historial",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    IconButton(onClick = onNavigateBancos) {
                        Icon(
                            Icons.Default.CreditCard,
                            "Bancos",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    IconButton(onClick = onNavigateConfiguracion) {
                        Icon(
                            Icons.Default.Settings,
                            "Configuración",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
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
            // Nuevo FAB con menú desplegable
            Box {
                ExtendedFloatingActionButton(
                    onClick = { expandedFab = !expandedFab },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Nuevo Pago"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Nuevo Pago")
                }

                // Menú desplegable
                DropdownMenu(
                    expanded = expandedFab,
                    onDismissRequest = { expandedFab = false }
                ) {
                    DropdownMenuItem(
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(Icons.Default.CreditCard, null)
                                Column {
                                    Text(
                                        "Pago Normal",
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        "Un solo pago",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        },
                        onClick = {
                            expandedFab = false
                            if (bancos.isNotEmpty()) {
                                onAgregarTarjeta()
                            }
                        },
                        enabled = bancos.isNotEmpty()
                    )
                    DropdownMenuItem(
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(Icons.Default.ShoppingCart, null)
                                Column {
                                    Text(
                                        "Meses Sin Intereses",
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        "Pagos mensuales automáticos",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        },
                        onClick = {
                            expandedFab = false
                            mostrarDialogoMSI = true
                        },
                        enabled = bancos.isNotEmpty()
                    )
                }
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
                ),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.CreditCard,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Deuda Total Pendiente",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = formatoMoneda(deudaTotal ?: 0.0),
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${tarjetas.size} pago(s) activo(s)",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Filtro de fechas
            if (tarjetas.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
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
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.DateRange,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary
                            )
                            Column {
                                Text(
                                    "Filtrar por fecha",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Text(
                                    "${tarjetasFiltradas.size} de ${tarjetas.size} pagos",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                                )
                            }
                        }

                        ExposedDropdownMenuBox(
                            expanded = expandedFiltro,
                            onExpandedChange = { expandedFiltro = it }
                        ) {
                            AssistChip(
                                onClick = { expandedFiltro = true },
                                label = {
                                    Text(
                                        when (filtroSeleccionado) {
                                            FiltroFecha.PROXIMAS_3_SEMANAS -> "3 semanas"
                                            FiltroFecha.ESTA_SEMANA -> "Esta semana"
                                            FiltroFecha.ESTE_MES -> "Este mes"
                                            FiltroFecha.TODAS -> "Todos"
                                        }
                                    )
                                },
                                trailingIcon = {
                                    Icon(
                                        Icons.Default.ArrowDropDown,
                                        contentDescription = null
                                    )
                                },
                                modifier = Modifier.menuAnchor()
                            )

                            ExposedDropdownMenu(
                                expanded = expandedFiltro,
                                onDismissRequest = { expandedFiltro = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Próximas 3 semanas") },
                                    onClick = {
                                        filtroSeleccionado = FiltroFecha.PROXIMAS_3_SEMANAS
                                        expandedFiltro = false
                                    },
                                    leadingIcon = {
                                        if (filtroSeleccionado == FiltroFecha.PROXIMAS_3_SEMANAS) {
                                            Icon(Icons.Default.Check, null)
                                        }
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Esta semana") },
                                    onClick = {
                                        filtroSeleccionado = FiltroFecha.ESTA_SEMANA
                                        expandedFiltro = false
                                    },
                                    leadingIcon = {
                                        if (filtroSeleccionado == FiltroFecha.ESTA_SEMANA) {
                                            Icon(Icons.Default.Check, null)
                                        }
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Este mes") },
                                    onClick = {
                                        filtroSeleccionado = FiltroFecha.ESTE_MES
                                        expandedFiltro = false
                                    },
                                    leadingIcon = {
                                        if (filtroSeleccionado == FiltroFecha.ESTE_MES) {
                                            Icon(Icons.Default.Check, null)
                                        }
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Todos los pagos") },
                                    onClick = {
                                        filtroSeleccionado = FiltroFecha.TODAS
                                        expandedFiltro = false
                                    },
                                    leadingIcon = {
                                        if (filtroSeleccionado == FiltroFecha.TODAS) {
                                            Icon(Icons.Default.Check, null)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Mensaje si no hay bancos
            AnimatedVisibility(
                visible = bancos.isEmpty(),
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.CreditCard,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Primero agrega tus bancos",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Necesitas agregar al menos un banco antes de crear pagos",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onNavigateBancos,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(Icons.Default.CreditCard, null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Ir a Bancos")
                        }
                    }
                }
            }

            // Lista de tarjetas
            AnimatedVisibility(
                visible = bancos.isNotEmpty() && tarjetas.isEmpty(),
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No hay pagos registrados",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Toca 'Nuevo Pago' para agregar tu primer pago",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Mostrar mensaje si no hay resultados del filtro
            if (tarjetas.isNotEmpty() && tarjetasFiltradas.isEmpty()) {
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
                            Icons.Default.Info,
                            contentDescription = null,
                            modifier = Modifier.size(60.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "No hay pagos en este período",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Cambia el filtro para ver más pagos",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (tarjetasFiltradas.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(tarjetasFiltradas, key = { it.id }) { tarjeta ->
                        val banco = bancosMap[tarjeta.bancoId]
                        if (banco != null) {
                            TarjetaCard(
                                tarjeta = tarjeta,
                                banco = banco,
                                todasLasTarjetas = tarjetas,
                                viewModel = viewModel,
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
            icon = {
                Icon(
                    Icons.Default.Delete,
                    null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text("Eliminar pago") },
            text = {
                val banco = bancosMap[tarjetaAEliminar?.bancoId]
                Text("¿Estás seguro de eliminar el pago de ${banco?.nombreBanco}?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        tarjetaAEliminar?.let { viewModel.eliminarTarjeta(it) }
                        mostrarDialogoEliminar = false
                        tarjetaAEliminar = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Eliminar")
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
            icon = {
                Icon(
                    Icons.Default.CheckCircle,
                    null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = { Text("Confirmar Pago") },
            text = {
                Column {
                    Text("¿Ya pagaste la deuda de ${bancoTarjetaAPagar?.nombreBanco}?")
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                "Monto: ${formatoMoneda(tarjetaAPagar!!.deudaTotal)}",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Fecha límite: ${tarjetaAPagar!!.fechaLimitePago}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Este registro se moverá al historial.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
                    }
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

    // Diálogo para agregar MSI
    if (mostrarDialogoMSI) {
        DialogAgregarMSI(
            bancos = bancos,
            onDismiss = { mostrarDialogoMSI = false },
            onConfirmar = { bancoId, descripcion, montoTotal, meses, mesInicio ->
                viewModel.crearTarjetasMSI(
                    bancoId = bancoId,
                    descripcion = descripcion,
                    montoTotal = montoTotal,
                    meses = meses,
                    mesInicio = mesInicio,
                    onCompletado = {
                        mostrarDialogoMSI = false
                    }
                )
            }
        )
    }
}

private fun formatoMoneda(monto: Double): String {
    val formato = NumberFormat.getCurrencyInstance(Locale("es", "MX"))
    return formato.format(monto)
}