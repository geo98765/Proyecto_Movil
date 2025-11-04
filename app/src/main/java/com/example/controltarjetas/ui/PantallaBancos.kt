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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.controltarjetas.TarjetaViewModel
import com.example.controltarjetas.data.Banco

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaBancos(
    viewModel: TarjetaViewModel = viewModel(),
    onNavigateBack: () -> Unit
) {
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
                        imageVector = Icons.Default.Build,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No hay bancos registrados",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Gray
                    )
                    Text(
                        text = "Toca + para agregar uno",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
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
                    bancoAEliminar?.let { viewModel.eliminarBanco(it) }
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
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Logo
            if (banco.logoUri != null) {
                Image(
                    painter = rememberAsyncImagePainter(banco.logoUri),
                    contentDescription = "Logo",
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray),
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
                        imageVector = Icons.Default.Build,
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
                    fontWeight = FontWeight.Bold
                )
                if (banco.limiteCredito != null) {
                    Text(
                        text = "Límite: ${formatoMoneda(banco.limiteCredito)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
                if (banco.fechaCorte != null) {
                    Text(
                        text = "Corte día: ${banco.fechaCorte}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
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
    var nombreBanco by remember { mutableStateOf(banco?.nombreBanco ?: "") }
    var limiteCredito by remember { mutableStateOf(banco?.limiteCredito?.toString() ?: "") }
    var fechaCorte by remember { mutableStateOf(banco?.fechaCorte?.toString() ?: "") }
    var logoUri by remember { mutableStateOf(banco?.logoUri) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        logoUri = uri?.toString()
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
                        .background(Color.LightGray)
                        .clickable { imagePickerLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (logoUri != null) {
                        Image(
                            painter = rememberAsyncImagePainter(logoUri),
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
                                tint = Color.Gray
                            )
                            Text("Toca para agregar logo", color = Color.Gray)
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
                                logoUri = logoUri,
                                limiteCredito = limiteCredito.toDoubleOrNull(),
                                fechaCorte = fechaCorte.toIntOrNull()
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