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
    var fechaLimitePago by remember { mutableStateOf(LocalDate.now()) }
    var notas by remember { mutableStateOf("") }

    var expandedBanco by remember { mutableStateOf(false) }
    var expandedTipo by remember { mutableStateOf(false) }
    var mostrarDatePicker by remember { mutableStateOf(false) }

    val tiposTarjeta = listOf("Crédito", "Débito")

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
                fechaLimitePago = LocalDate.parse(it.fechaLimitePago)
                notas = it.notas ?: ""
            }
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
                            text = { Text(banco.nombreBanco) },
                            onClick = {
                                bancoSeleccionadoId = banco.id
                                expandedBanco = false

                                // Auto-rellenar fecha de corte si existe
                                if (banco.fechaCorte != null) {
                                    val hoy = LocalDate.now()
                                    val mesActual = YearMonth.from(hoy)
                                    val fechaCorteEsteMes = mesActual.atDay(banco.fechaCorte.coerceIn(1, mesActual.lengthOfMonth()))

                                    // Si ya pasó el corte este mes, usar el siguiente
                                    fechaLimitePago = if (hoy.isAfter(fechaCorteEsteMes)) {
                                        mesActual.plusMonths(1).atDay(banco.fechaCorte.coerceIn(1, mesActual.plusMonths(1).lengthOfMonth()))
                                    } else {
                                        fechaCorteEsteMes
                                    }
                                }
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

            // Fecha límite de pago con DatePicker
            OutlinedTextField(
                value = fechaLimitePago.toString(),
                onValueChange = {},
                readOnly = true,
                label = { Text("Fecha Límite de Pago *") },
                trailingIcon = {
                    IconButton(onClick = { mostrarDatePicker = true }) {
                        Icon(Icons.Default.DateRange, "Seleccionar fecha")
                    }
                },
                modifier = Modifier.fillMaxWidth()
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

            // Información del mes/período
            val mesPeriodo = YearMonth.from(fechaLimitePago).toString()
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Text(
                    text = "Período: $mesPeriodo",
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Botón guardar
            Button(
                onClick = {
                    if (bancoSeleccionadoId != null && deudaTotal.isNotBlank()) {
                        val tarjeta = Tarjeta(
                            id = tarjetaId ?: 0,
                            bancoId = bancoSeleccionadoId!!,
                            tipoTarjeta = tipoTarjeta,
                            deudaTotal = deudaTotal.toDoubleOrNull() ?: 0.0,
                            pagoMinimo = pagoMinimo.toDoubleOrNull(),
                            fechaLimitePago = fechaLimitePago.toString(),
                            mesPeriodo = mesPeriodo,
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
                enabled = bancoSeleccionadoId != null && deudaTotal.isNotBlank()
            ) {
                Text(if (esEdicion) "Actualizar" else "Guardar")
            }
        }
    }

    // DatePicker modal
    if (mostrarDatePicker) {
        DatePickerModal(
            onDateSelected = { date ->
                fechaLimitePago = date
                mostrarDatePicker = false
            },
            onDismiss = { mostrarDatePicker = false },
            initialDate = fechaLimitePago
        )
    }
}