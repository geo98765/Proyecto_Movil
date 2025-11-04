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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.controltarjetas.TarjetaViewModel
import com.example.controltarjetas.data.Banco
import com.example.controltarjetas.utils.ImageHelper
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaBancos(
    viewModel: TarjetaViewModel = viewModel(),
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val bancos by viewModel.todosBancos.collectAsState(initial = emptyList())
    var mostrarDialogoAgregar by remember { mutableStateOf(false) }
    var bancoAEditar by remember { mutableStateOf<Banco?>(null) }
    var mostrarDialogoEliminar by remember { mutableStateOf(false) }
    var bancoAEliminar by remember { mutableStateOf<Banco?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Bancos") },
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
                Icon(Icons.Default.Add, "Agregar banco")
            }
        }
    ) { paddingValues ->
        if (bancos.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.CreditCard,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No hay bancos registrados",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Toca + para agregar uno",
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
                items(bancos) { banco ->
                    BancoCard(
                        banco = banco,
                        onEditClick = {
                            bancoAEditar = banco
                            mostrarDialogoAgregar = true
                        },
                        onDeleteClick = {
                            bancoAEliminar = banco
                            mostrarDialogoEliminar = true
                        }
                    )
                }
            }
        }
    }

    // Diálogo agregar/editar banco
    if (mostrarDialogoAgregar) {
        DialogoAgregarEditarBanco(
            banco = bancoAEditar,
            onDismiss = {
                mostrarDialogoAgregar = false
                bancoAEditar = null
            },
            onGuardar = { banco ->
                if (bancoAEditar != null) {
                    viewModel.actualizarBanco(banco)
                } else {
                    viewModel.insertarBanco(banco)
                }
                mostrarDialogoAgregar = false
                bancoAEditar = null
            }
        )
    }

    // Diálogo eliminar
    if (mostrarDialogoEliminar && bancoAEliminar != null) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoEliminar = false },
            title = { Text("Eliminar banco") },
            text = { Text("¿Eliminar ${bancoAEliminar?.nombreBanco}? Esto también eliminará todas las tarjetas asociadas.") },
            confirmButton = {
                TextButton(onClick = {
                    bancoAEliminar?.let {
                        // Eliminar imagen si existe
                        it.logoUri?.let { path -> ImageHelper.deleteImage(path) }
                        viewModel.eliminarBanco(it)
                    }
                    mostrarDialogoEliminar = false
                    bancoAEliminar = null
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
fun BancoCard(
    banco: Banco,
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
            if (banco.logoUri != null && File(banco.logoUri).exists()) {
                Image(
                    painter = rememberAsyncImagePainter(File(banco.logoUri)),
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
                        imageVector = Icons.Default.CreditCard,
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
                    text = banco.nombreBanco,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (banco.limiteCredito != null) {
                    Text(
                        text = "Límite: ${formatoMoneda(banco.limiteCredito)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (banco.fechaCorte != null) {
                    Text(
                        text = "Corte día: ${banco.fechaCorte}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                // NUEVO: Mostrar día de pago
                if (banco.diaPago != null) {
                    Text(
                        text = "Pago día: ${banco.diaPago}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
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

@Composable
fun DialogoAgregarEditarBanco(
    banco: Banco?,
    onDismiss: () -> Unit,
    onGuardar: (Banco) -> Unit
) {
    val context = LocalContext.current
    var nombreBanco by remember { mutableStateOf(banco?.nombreBanco ?: "") }
    var limiteCredito by remember { mutableStateOf(banco?.limiteCredito?.toString() ?: "") }
    var fechaCorte by remember { mutableStateOf(banco?.fechaCorte?.toString() ?: "") }
    var diaPago by remember { mutableStateOf(banco?.diaPago?.toString() ?: "") } // NUEVO
    var logoPath by remember { mutableStateOf(banco?.logoUri) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            // Guardar imagen en almacenamiento interno
            val savedPath = ImageHelper.saveImageToInternalStorage(context, it)
            if (savedPath != null) {
                // Eliminar imagen anterior si existe
                logoPath?.let { oldPath -> ImageHelper.deleteImage(oldPath) }
                logoPath = savedPath
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (banco != null) "Editar Banco" else "Agregar Banco") },
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
                    value = nombreBanco,
                    onValueChange = { nombreBanco = it },
                    label = { Text("Nombre del Banco *") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = limiteCredito,
                    onValueChange = { limiteCredito = it },
                    label = { Text("Límite de Crédito (opcional)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = fechaCorte,
                    onValueChange = {
                        if (it.isEmpty() || (it.toIntOrNull() in 1..31)) {
                            fechaCorte = it
                        }
                    },
                    label = { Text("Día de Corte (1-31)") },
                    modifier = Modifier.fillMaxWidth()
                )

                // NUEVO: Campo para día de pago
                OutlinedTextField(
                    value = diaPago,
                    onValueChange = {
                        if (it.isEmpty() || (it.toIntOrNull() in 1..31)) {
                            diaPago = it
                        }
                    },
                    label = { Text("Día de Pago Límite (1-31)") },
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = {
                        Text(
                            "Día límite de pago cada mes",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (nombreBanco.isNotBlank()) {
                        onGuardar(
                            Banco(
                                id = banco?.id ?: 0,
                                nombreBanco = nombreBanco,
                                logoUri = logoPath,
                                limiteCredito = limiteCredito.toDoubleOrNull(),
                                fechaCorte = fechaCorte.toIntOrNull(),
                                diaPago = diaPago.toIntOrNull() // NUEVO
                            )
                        )
                    }
                },
                enabled = nombreBanco.isNotBlank()
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

private fun formatoMoneda(monto: Double): String {
    return "$${String.format("%,.2f", monto)}"
}