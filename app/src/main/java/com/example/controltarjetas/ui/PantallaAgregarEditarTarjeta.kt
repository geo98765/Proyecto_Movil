package com.example.controltarjetas.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.controltarjetas.TarjetaViewModel
import com.example.controltarjetas.data.Tarjeta
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaAgregarEditarTarjeta(
    viewModel: TarjetaViewModel = viewModel(),
    tarjetaId: Int? = null,
    onNavigateBack: () -> Unit
) {
    val bancos by viewModel.todosBancos.collectAsState(initial = emptyList())

    var bancoSeleccionadoId by remember { mutableStateOf<Int?>(null) }
    var tipoTarjeta by remember { mutableStateOf("Crédito") }
    var deudaTotal by remember { mutableStateOf("") }
    var pagoMinimo by remember { mutableStateOf("") }
    var notas by remember { mutableStateOf("") }

    // NUEVO: En lugar de fecha, seleccionar mes/año
    var mesSeleccionado by remember { mutableStateOf(YearMonth.now()) }

    var expandedBanco by remember { mutableStateOf(false) }
    var expandedTipo by remember { mutableStateOf(false) }
    var expandedMes by remember { mutableStateOf(false) }

    val tiposTarjeta = listOf("Crédito", "Débito")

    // NUEVO: Generar lista de meses (actual y siguientes 11)
    val mesesDisponibles = remember {
        (0..11).map { YearMonth.now().plusMonths(it.toLong()) }
    }

    val esEdicion = tarjetaId != null

    // Cargar datos si es edición
    LaunchedEffect(tarjetaId) {
        if (tarjetaId != null) {
            val tarjeta = viewModel.obtenerTarjetaPorId(tarjetaId)
            tarjeta?.let {
                bancoSeleccionadoId = it.bancoId
                tipoTarjeta = it.tipoTarjeta
                deudaTotal = it.deudaTotal.toString()
                pagoMinimo = it.pagoMinimo?.toString() ?: ""
                notas = it.notas ?: ""
                // Cargar el mes del período
                mesSeleccionado = YearMonth.parse(it.mesPeriodo)
            }
        }
    }

    // NUEVO: Calcular fecha límite automáticamente
    val fechaLimiteCalculada = remember(bancoSeleccionadoId, mesSeleccionado) {
        val banco = bancos.find { it.id == bancoSeleccionadoId }
        if (banco?.diaPago != null) {
            val ultimoDiaDelMes = mesSeleccionado.lengthOfMonth()
            val diaAjustado = banco.diaPago.coerceIn(1, ultimoDiaDelMes)
            mesSeleccionado.atDay(diaAjustado)
        } else {
            null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (esEdicion) "Editar Tarjeta" else "Agregar Tarjeta") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver"
                        )
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (bancos.isEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        "Primero debes agregar un banco en la sección de Bancos",
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            // Selector de banco
            ExposedDropdownMenuBox(
                expanded = expandedBanco,
                onExpandedChange = { expandedBanco = it }
            ) {
                OutlinedTextField(
                    value = bancos.find { it.id == bancoSeleccionadoId }?.nombreBanco ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Seleccionar Banco *") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedBanco) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    enabled = bancos.isNotEmpty()
                )
                ExposedDropdownMenu(
                    expanded = expandedBanco,
                    onDismissRequest = { expandedBanco = false }
                ) {
                    bancos.forEach { banco ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(banco.nombreBanco)
                                    if (banco.diaPago != null) {
                                        Text(
                                            "Pago día: ${banco.diaPago}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    } else {
                                        Text(
                                            "⚠️ Sin día de pago configurado",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            },
                            onClick = {
                                bancoSeleccionadoId = banco.id
                                expandedBanco = false
                            }
                        )
                    }
                }
            }

            // Advertencia si el banco no tiene día de pago
            if (bancoSeleccionadoId != null) {
                val bancoSeleccionado = bancos.find { it.id == bancoSeleccionadoId }
                if (bancoSeleccionado?.diaPago == null) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                null,
                                tint = MaterialTheme.colorScheme.error
                            )
                            Text(
                                "Este banco no tiene día de pago configurado. Por favor, edita el banco y agrega el día de pago límite.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }

            // NUEVO: Selector de mes/año
            ExposedDropdownMenuBox(
                expanded = expandedMes,
                onExpandedChange = { expandedMes = it }
            ) {
                OutlinedTextField(
                    value = "${mesSeleccionado.month.getDisplayName(TextStyle.FULL, Locale("es"))} ${mesSeleccionado.year}",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Mes del Período *") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedMes) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = expandedMes,
                    onDismissRequest = { expandedMes = false }
                ) {
                    mesesDisponibles.forEach { mes ->
                        DropdownMenuItem(
                            text = {
                                Text("${mes.month.getDisplayName(TextStyle.FULL, Locale("es"))} ${mes.year}")
                            },
                            onClick = {
                                mesSeleccionado = mes
                                expandedMes = false
                            }
                        )
                    }
                }
            }

            // Tipo de tarjeta
            ExposedDropdownMenuBox(
                expanded = expandedTipo,
                onExpandedChange = { expandedTipo = it }
            ) {
                OutlinedTextField(
                    value = tipoTarjeta,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Tipo de Tarjeta *") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTipo) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = expandedTipo,
                    onDismissRequest = { expandedTipo = false }
                ) {
                    tiposTarjeta.forEach { tipo ->
                        DropdownMenuItem(
                            text = { Text(tipo) },
                            onClick = {
                                tipoTarjeta = tipo
                                expandedTipo = false
                            }
                        )
                    }
                }
            }

            // Deuda total
            OutlinedTextField(
                value = deudaTotal,
                onValueChange = { deudaTotal = it },
                label = { Text("Deuda Total *") },
                placeholder = { Text("0.00") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                prefix = { Text("$") }
            )

            // Pago mínimo
            OutlinedTextField(
                value = pagoMinimo,
                onValueChange = { pagoMinimo = it },
                label = { Text("Pago Mínimo (opcional)") },
                placeholder = { Text("0.00") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                prefix = { Text("$") }
            )

            // Notas
            OutlinedTextField(
                value = notas,
                onValueChange = { notas = it },
                label = { Text("Notas (opcional)") },
                placeholder = { Text("Recordatorios o comentarios") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Spacer(modifier = Modifier.height(8.dp))

            // NUEVO: Mostrar fecha calculada automáticamente
            if (fechaLimiteCalculada != null) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.DateRange,
                                null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Column {
                                Text(
                                    text = "Fecha de Pago Límite",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = fechaLimiteCalculada.toString(),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Período: $mesSeleccionado",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Botón guardar
            Button(
                onClick = {
                    if (bancoSeleccionadoId != null && deudaTotal.isNotBlank() && fechaLimiteCalculada != null) {
                        val tarjeta = Tarjeta(
                            id = tarjetaId ?: 0,
                            bancoId = bancoSeleccionadoId!!,
                            tipoTarjeta = tipoTarjeta,
                            deudaTotal = deudaTotal.toDoubleOrNull() ?: 0.0,
                            pagoMinimo = pagoMinimo.toDoubleOrNull(),
                            fechaLimitePago = fechaLimiteCalculada.toString(),
                            mesPeriodo = mesSeleccionado.toString(),
                            notas = notas.ifBlank { null }
                        )

                        if (esEdicion) {
                            viewModel.actualizarTarjeta(tarjeta)
                        } else {
                            viewModel.insertarTarjeta(tarjeta)
                        }

                        onNavigateBack()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = bancoSeleccionadoId != null &&
                        deudaTotal.isNotBlank() &&
                        fechaLimiteCalculada != null
            ) {
                Text(if (esEdicion) "Actualizar" else "Guardar")
            }
        }
    }
}