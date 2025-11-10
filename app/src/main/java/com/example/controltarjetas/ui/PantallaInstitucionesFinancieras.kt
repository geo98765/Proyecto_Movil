package com.example.controltarjetas.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.controltarjetas.InstitucionFinancieraViewModel
import com.example.controltarjetas.data.InstitucionFinanciera
import com.example.controltarjetas.utils.ImageHelper
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaInstitucionesFinancieras(
    viewModel: InstitucionFinancieraViewModel = viewModel(),
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val instituciones by viewModel.todasInstituciones.collectAsState(initial = emptyList())
    var mostrarDialogoAgregar by remember { mutableStateOf(false) }
    var institucionAEditar by remember { mutableStateOf<InstitucionFinanciera?>(null) }
    var mostrarDialogoEliminar by remember { mutableStateOf(false) }
    var institucionAEliminar by remember { mutableStateOf<InstitucionFinanciera?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Instituciones Financieras") },
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
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { mostrarDialogoAgregar = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, "Agregar institución")
            }
        }
    ) { paddingValues ->
        if (instituciones.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.AccountBalance,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No hay instituciones registradas",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Toca + para agregar una",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(instituciones) { institucion ->
                    InstitucionCard(
                        institucion = institucion,
                        onEditClick = {
                            institucionAEditar = institucion
                            mostrarDialogoAgregar = true
                        },
                        onDeleteClick = {
                            institucionAEliminar = institucion
                            mostrarDialogoEliminar = true
                        }
                    )
                }
            }
        }
    }

    // Diálogo agregar/editar institución
    if (mostrarDialogoAgregar) {
        DialogoAgregarEditarInstitucion(
            institucion = institucionAEditar,
            onDismiss = {
                mostrarDialogoAgregar = false
                institucionAEditar = null
            },
            onGuardar = { institucion ->
                if (institucionAEditar != null) {
                    viewModel.actualizarInstitucion(institucion)
                } else {
                    viewModel.insertarInstitucion(institucion)
                }
                mostrarDialogoAgregar = false
                institucionAEditar = null
            }
        )
    }

    // Diálogo eliminar
    if (mostrarDialogoEliminar && institucionAEliminar != null) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoEliminar = false },
            title = { Text("Eliminar institución") },
            text = { Text("¿Eliminar ${institucionAEliminar?.nombreInstitucion}? Esto también eliminará todos los ahorros asociados.") },
            confirmButton = {
                TextButton(onClick = {
                    institucionAEliminar?.let {
                        it.logoUri?.let { path -> ImageHelper.deleteImage(path) }
                        viewModel.eliminarInstitucion(it)
                    }
                    mostrarDialogoEliminar = false
                    institucionAEliminar = null
                }) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
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

@Composable
fun InstitucionCard(
    institucion: InstitucionFinanciera,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Logo
            if (institucion.logoUri != null && File(institucion.logoUri).exists()) {
                Image(
                    painter = rememberAsyncImagePainter(File(institucion.logoUri)),
                    contentDescription = "Logo",
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
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
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Información
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = institucion.nombreInstitucion,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = institucion.tipoInversion,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }

                if (institucion.rendimientoAnual != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.TrendingUp,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Rendimiento: ${institucion.rendimientoAnual}% anual",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Botones
            IconButton(onClick = onEditClick) {
                Icon(Icons.Default.Edit, "Editar", tint = MaterialTheme.colorScheme.primary)
            }
            IconButton(onClick = onDeleteClick) {
                Icon(Icons.Default.Delete, "Eliminar", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogoAgregarEditarInstitucion(
    institucion: InstitucionFinanciera?,
    onDismiss: () -> Unit,
    onGuardar: (InstitucionFinanciera) -> Unit
) {
    val context = LocalContext.current
    var nombreInstitucion by remember { mutableStateOf(institucion?.nombreInstitucion ?: "") }
    var tipoInversion by remember { mutableStateOf(institucion?.tipoInversion ?: "Tarjeta") }
    var rendimientoAnual by remember { mutableStateOf(institucion?.rendimientoAnual?.toString() ?: "") }
    var logoPath by remember { mutableStateOf(institucion?.logoUri) }
    var expandedTipo by remember { mutableStateOf(false) }

    val tiposInversion = listOf("Tarjeta", "Acciones", "Cripto", "CETES")

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val savedPath = ImageHelper.saveImageToInternalStorage(context, it)
            if (savedPath != null) {
                logoPath?.let { oldPath -> ImageHelper.deleteImage(oldPath) }
                logoPath = savedPath
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (institucion != null) "Editar Institución" else "Agregar Institución") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Logo
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { imagePickerLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (logoPath != null && File(logoPath!!).exists()) {
                        Image(
                            painter = rememberAsyncImagePainter(File(logoPath!!)),
                            contentDescription = "Logo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.AddCircle,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Toca para agregar logo",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = nombreInstitucion,
                    onValueChange = { nombreInstitucion = it },
                    label = { Text("Nombre de la Institución *") },
                    placeholder = { Text("GBM, Kuspit, etc.") },
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(
                    expanded = expandedTipo,
                    onExpandedChange = { expandedTipo = it }
                ) {
                    OutlinedTextField(
                        value = tipoInversion,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Tipo de Inversión *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTipo) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedTipo,
                        onDismissRequest = { expandedTipo = false }
                    ) {
                        tiposInversion.forEach { tipo ->
                            DropdownMenuItem(
                                text = { Text(tipo) },
                                onClick = {
                                    tipoInversion = tipo
                                    expandedTipo = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = rendimientoAnual,
                    onValueChange = { rendimientoAnual = it },
                    label = { Text("Rendimiento Anual (opcional)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    suffix = { Text("%") }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (nombreInstitucion.isNotBlank()) {
                        onGuardar(
                            InstitucionFinanciera(
                                id = institucion?.id ?: 0,
                                nombreInstitucion = nombreInstitucion,
                                logoUri = logoPath,
                                tipoInversion = tipoInversion,
                                rendimientoAnual = rendimientoAnual.toDoubleOrNull()
                            )
                        )
                    }
                },
                enabled = nombreInstitucion.isNotBlank()
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}