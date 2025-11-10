package com.example.controltarjetas.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.controltarjetas.AhorroViewModel
import com.example.controltarjetas.InstitucionFinancieraViewModel
import com.example.controltarjetas.data.Ahorro
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaAgregarEditarAhorro(
    viewModel: AhorroViewModel = viewModel(), // ← Cambiar orden
    institucionViewModel: InstitucionFinancieraViewModel = viewModel(), // ← Cambiar orden
    ahorroId: Int? = null, // ← Cambiar orden
    onNavigateBack: () -> Unit  // ← Y esto
) {
    val esEdicion = ahorroId != null
    val instituciones by institucionViewModel.todasInstituciones.collectAsState(initial = emptyList())

    var institucionSeleccionadaId by remember { mutableStateOf<Int?>(null) }
    var nombre by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }

    // Campos específicos por tipo
    var montoTarjeta by remember { mutableStateOf("") }
    var cantidadAcciones by remember { mutableStateOf("") }
    var precioCompraAccion by remember { mutableStateOf("") }
    var simboloAccion by remember { mutableStateOf("") }
    var cantidadCripto by remember { mutableStateOf("") }
    var precioCompraCripto by remember { mutableStateOf("") }
    var simboloCripto by remember { mutableStateOf("") }
    var montoCetes by remember { mutableStateOf("") }
    var plazoCetes by remember { mutableStateOf("") }
    var tasaCetes by remember { mutableStateOf("") }

    var expandedInstitucion by remember { mutableStateOf(false) }

    // Obtener institución seleccionada
    val institucionSeleccionada = remember(institucionSeleccionadaId, instituciones) {
        instituciones.find { it.id == institucionSeleccionadaId }
    }

    // Cargar datos si es edición
    LaunchedEffect(ahorroId) {
        if (ahorroId != null) {
            val ahorro = viewModel.obtenerPorId(ahorroId)
            ahorro?.let {
                institucionSeleccionadaId = it.institucionId
                nombre = it.nombre
                descripcion = it.descripcion ?: ""

                val inst = institucionViewModel.obtenerInstitucionPorId(it.institucionId)
                when (inst?.tipoInversion) {
                    "Tarjeta" -> {
                        montoTarjeta = it.montoTarjeta?.toString() ?: ""
                    }
                    "Acciones" -> {
                        cantidadAcciones = it.cantidadAcciones?.toString() ?: ""
                        precioCompraAccion = it.precioCompraAccion?.toString() ?: ""
                        simboloAccion = it.simboloAccion ?: ""
                    }
                    "Cripto" -> {
                        cantidadCripto = it.cantidadCripto?.toString() ?: ""
                        precioCompraCripto = it.precioCompraCripto?.toString() ?: ""
                        simboloCripto = it.simboloCripto ?: ""
                    }
                    "CETES" -> {
                        montoCetes = it.montoCetes?.toString() ?: ""
                        plazoCetes = it.plazoCetes?.toString() ?: ""
                        tasaCetes = it.tasaCetes?.toString() ?: ""
                    }
                }
            }
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (esEdicion) "Editar Ahorro" else "Nuevo Ahorro") },
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Mensaje si no hay instituciones
            if (instituciones.isEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        "Primero debes agregar una institución financiera en la sección de Instituciones",
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            // Selector de institución
            ExposedDropdownMenuBox(
                expanded = expandedInstitucion,
                onExpandedChange = { expandedInstitucion = it }
            ) {
                OutlinedTextField(
                    value = institucionSeleccionada?.nombreInstitucion ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Seleccionar Institución *") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedInstitucion) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    enabled = instituciones.isNotEmpty(),
                    leadingIcon = {
                        Icon(
                            when (institucionSeleccionada?.tipoInversion) {
                                "Tarjeta" -> Icons.Default.CreditCard
                                "Acciones" -> Icons.Default.TrendingUp
                                "Cripto" -> Icons.Default.CurrencyBitcoin
                                "CETES" -> Icons.Default.AccountBalance
                                else -> Icons.Default.Savings
                            },
                            null
                        )
                    }
                )
                ExposedDropdownMenu(
                    expanded = expandedInstitucion,
                    onDismissRequest = { expandedInstitucion = false }
                ) {
                    instituciones.forEach { institucion ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(institucion.nombreInstitucion)
                                    Surface(
                                        color = MaterialTheme.colorScheme.secondaryContainer,
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            institucion.tipoInversion,
                                            style = MaterialTheme.typography.labelSmall,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            },
                            onClick = {
                                institucionSeleccionadaId = institucion.id
                                expandedInstitucion = false
                                // Limpiar campos al cambiar institución
                                montoTarjeta = ""
                                cantidadAcciones = ""
                                precioCompraAccion = ""
                                simboloAccion = ""
                                cantidadCripto = ""
                                precioCompraCripto = ""
                                simboloCripto = ""
                                montoCetes = ""
                                plazoCetes = ""
                                tasaCetes = ""
                            },
                            leadingIcon = {
                                Icon(
                                    when (institucion.tipoInversion) {
                                        "Tarjeta" -> Icons.Default.CreditCard
                                        "Acciones" -> Icons.Default.TrendingUp
                                        "Cripto" -> Icons.Default.CurrencyBitcoin
                                        "CETES" -> Icons.Default.AccountBalance
                                        else -> Icons.Default.Savings
                                    },
                                    null
                                )
                            }
                        )
                    }
                }
            }

            // Mostrar rendimiento si la institución lo tiene
            if (institucionSeleccionada?.rendimientoAnual != null) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.TrendingUp,
                            null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "Rendimiento anual estimado: ${institucionSeleccionada?.rendimientoAnual}%",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Nombre
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre de la inversión *") },
                placeholder = { Text("Ej: Inversión Enero, Ahorro Casa, etc.") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Label, null) }
            )

            // Campos específicos según tipo de institución
            institucionSeleccionada?.let { institucion ->
                when (institucion.tipoInversion) {
                    "Tarjeta" -> {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    "Datos de la Tarjeta de Ahorro",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                OutlinedTextField(
                                    value = montoTarjeta,
                                    onValueChange = { montoTarjeta = it },
                                    label = { Text("Monto a Ahorrar *") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    modifier = Modifier.fillMaxWidth(),
                                    prefix = { Text("$") }
                                )
                            }
                        }
                    }
                    "Acciones" -> {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    "Datos de Acciones",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                OutlinedTextField(
                                    value = simboloAccion,
                                    onValueChange = { simboloAccion = it.uppercase() },
                                    label = { Text("Símbolo de Acción *") },
                                    placeholder = { Text("AAPL, TSLA, etc.") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                OutlinedTextField(
                                    value = cantidadAcciones,
                                    onValueChange = { cantidadAcciones = it },
                                    label = { Text("Cantidad de Acciones *") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                OutlinedTextField(
                                    value = precioCompraAccion,
                                    onValueChange = { precioCompraAccion = it },
                                    label = { Text("Precio Total por Acción *") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    modifier = Modifier.fillMaxWidth(),
                                    prefix = { Text("$") }
                                )
                                if (cantidadAcciones.isNotBlank() && precioCompraAccion.isNotBlank()) {
                                    val precio = precioCompraAccion.toDoubleOrNull() ?: 0.0
                                    Surface(
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("Inversión Total:")
                                            Text(
                                                "$${String.format("%,.2f", precio)}",
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    "Cripto" -> {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    "Datos de Criptomoneda",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                OutlinedTextField(
                                    value = simboloCripto,
                                    onValueChange = { simboloCripto = it.uppercase() },
                                    label = { Text("Símbolo de Cripto *") },
                                    placeholder = { Text("BTC, ETH, etc.") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                OutlinedTextField(
                                    value = cantidadCripto,
                                    onValueChange = { cantidadCripto = it },
                                    label = { Text("Cantidad *") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                OutlinedTextField(
                                    value = precioCompraCripto,
                                    onValueChange = { precioCompraCripto = it },
                                    label = { Text("Precio de Compra *") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    modifier = Modifier.fillMaxWidth(),
                                    prefix = { Text("$") }
                                )
                                if (cantidadCripto.isNotBlank() && precioCompraCripto.isNotBlank()) {
                                    val precio = precioCompraCripto.toDoubleOrNull() ?: 0.0
                                    Surface(
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("Inversión Total:")
                                            Text(
                                                "$${String.format("%,.2f", precio)}",
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    "CETES" -> {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    "Datos de CETES",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                OutlinedTextField(
                                    value = montoCetes,
                                    onValueChange = { montoCetes = it },
                                    label = { Text("Monto Invertido *") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    modifier = Modifier.fillMaxWidth(),
                                    prefix = { Text("$") }
                                )
                                OutlinedTextField(
                                    value = plazoCetes,
                                    onValueChange = { plazoCetes = it },
                                    label = { Text("Plazo (días) *") },
                                    placeholder = { Text("28, 91, 182, 364") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                OutlinedTextField(
                                    value = tasaCetes,
                                    onValueChange = { tasaCetes = it },
                                    label = { Text("Tasa Anual *") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    modifier = Modifier.fillMaxWidth(),
                                    suffix = { Text("%") }
                                )
                            }
                        }
                    }
                }
            }

            // Descripción
            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                label = { Text("Descripción (opcional)") },
                placeholder = { Text("Notas o comentarios") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                leadingIcon = { Icon(Icons.Default.Notes, null) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Botón guardar
            Button(
                onClick = {
                    if (institucionSeleccionadaId != null && nombre.isNotBlank() &&
                        validarCamposPorTipo(
                            institucionSeleccionada?.tipoInversion ?: "",
                            montoTarjeta,
                            cantidadAcciones,
                            precioCompraAccion,
                            simboloAccion,
                            cantidadCripto,
                            precioCompraCripto,
                            simboloCripto,
                            montoCetes,
                            plazoCetes,
                            tasaCetes
                        )
                    ) {
                        val ahorro = Ahorro(
                            id = ahorroId ?: 0,
                            institucionId = institucionSeleccionadaId!!,
                            nombre = nombre,
                            descripcion = descripcion.ifBlank { null },
                            fechaCreacion = LocalDate.now().toString(),
                            montoTarjeta = if (institucionSeleccionada?.tipoInversion == "Tarjeta") montoTarjeta.toDoubleOrNull() else null,
                            cantidadAcciones = if (institucionSeleccionada?.tipoInversion == "Acciones") cantidadAcciones.toDoubleOrNull() else null,
                            precioCompraAccion = if (institucionSeleccionada?.tipoInversion == "Acciones") precioCompraAccion.toDoubleOrNull() else null,
                            simboloAccion = if (institucionSeleccionada?.tipoInversion == "Acciones") simboloAccion.ifBlank { null } else null,
                            cantidadCripto = if (institucionSeleccionada?.tipoInversion == "Cripto") cantidadCripto.toDoubleOrNull() else null,
                            precioCompraCripto = if (institucionSeleccionada?.tipoInversion == "Cripto") precioCompraCripto.toDoubleOrNull() else null,
                            simboloCripto = if (institucionSeleccionada?.tipoInversion == "Cripto") simboloCripto.ifBlank { null } else null,
                            montoCetes = if (institucionSeleccionada?.tipoInversion == "CETES") montoCetes.toDoubleOrNull() else null,
                            plazoCetes = if (institucionSeleccionada?.tipoInversion == "CETES") plazoCetes.toIntOrNull() else null,
                            tasaCetes = if (institucionSeleccionada?.tipoInversion == "CETES") tasaCetes.toDoubleOrNull() else null
                        )

                        if (esEdicion) {
                            viewModel.actualizar(ahorro)
                        } else {
                            viewModel.insertar(ahorro)
                        }

                        onNavigateBack()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = institucionSeleccionadaId != null && nombre.isNotBlank() &&
                        validarCamposPorTipo(
                            institucionSeleccionada?.tipoInversion ?: "",
                            montoTarjeta,
                            cantidadAcciones,
                            precioCompraAccion,
                            simboloAccion,
                            cantidadCripto,
                            precioCompraCripto,
                            simboloCripto,
                            montoCetes,
                            plazoCetes,
                            tasaCetes
                        )
            ) {
                Icon(Icons.Default.Save, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (esEdicion) "Actualizar" else "Guardar")
            }
        }
    }
}

private fun validarCamposPorTipo(
    tipo: String,
    montoTarjeta: String,
    cantidadAcciones: String,
    precioCompraAccion: String,
    simboloAccion: String,
    cantidadCripto: String,
    precioCompraCripto: String,
    simboloCripto: String,
    montoCetes: String,
    plazoCetes: String,
    tasaCetes: String
): Boolean {
    return when (tipo) {
        "Tarjeta" -> montoTarjeta.isNotBlank() && montoTarjeta.toDoubleOrNull() != null
        "Acciones" -> cantidadAcciones.isNotBlank() && precioCompraAccion.isNotBlank() && simboloAccion.isNotBlank()
        "Cripto" -> cantidadCripto.isNotBlank() && precioCompraCripto.isNotBlank() && simboloCripto.isNotBlank()
        "CETES" -> montoCetes.isNotBlank() && plazoCetes.isNotBlank() && tasaCetes.isNotBlank()
        else -> false
    }
}