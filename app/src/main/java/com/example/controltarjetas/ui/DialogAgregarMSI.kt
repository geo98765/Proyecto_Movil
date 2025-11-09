package com.example.controltarjetas.ui

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
import androidx.compose.ui.window.Dialog
import com.example.controltarjetas.data.Banco
import java.text.NumberFormat
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogAgregarMSI(
    bancos: List<Banco>,
    onDismiss: () -> Unit,
    onConfirmar: (
        bancoId: Int,
        descripcion: String,
        montoTotal: Double,
        meses: Int,
        mesInicio: YearMonth
    ) -> Unit
) {
    var bancoSeleccionadoId by remember { mutableStateOf<Int?>(null) }
    var descripcion by remember { mutableStateOf("") }
    var montoTotal by remember { mutableStateOf("") }
    var mesesSeleccionados by remember { mutableStateOf(3) }
    var mesInicio by remember { mutableStateOf(YearMonth.now()) }

    var expandedBanco by remember { mutableStateOf(false) }
    var expandedMeses by remember { mutableStateOf(false) }
    var expandedMesInicio by remember { mutableStateOf(false) }

    val opcionesMeses = listOf(3, 6, 9, 12, 18, 24)
    val mesesDisponibles = remember {
        (0..11).map { YearMonth.now().plusMonths(it.toLong()) }
    }

    // Calcular pago mensual
    val pagoMensual = if (montoTotal.isNotBlank()) {
        val monto = montoTotal.toDoubleOrNull() ?: 0.0
        monto / mesesSeleccionados
    } else 0.0

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = null,
                            modifier = Modifier
                                .size(48.dp)
                                .padding(12.dp),
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                    }
                    Column {
                        Text(
                            text = "Agregar Compra a MSI",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Meses Sin Intereses",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Divider()

                // Selector de banco
                ExposedDropdownMenuBox(
                    expanded = expandedBanco,
                    onExpandedChange = { expandedBanco = it }
                ) {
                    OutlinedTextField(
                        value = bancos.find { it.id == bancoSeleccionadoId }?.nombreBanco ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Banco *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedBanco) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        leadingIcon = {
                            Icon(Icons.Default.CreditCard, null)
                        }
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

                // Descripción
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción *") },
                    placeholder = { Text("Ej: Laptop Dell, iPhone 15, etc.") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(Icons.Default.ShoppingCart, null)
                    }
                )

                // Monto total
                OutlinedTextField(
                    value = montoTotal,
                    onValueChange = { montoTotal = it },
                    label = { Text("Monto Total *") },
                    placeholder = { Text("0.00") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    prefix = { Text("$") },
                    leadingIcon = {
                        Icon(Icons.Default.AttachMoney, null)
                    }
                )

                // Selector de meses
                ExposedDropdownMenuBox(
                    expanded = expandedMeses,
                    onExpandedChange = { expandedMeses = it }
                ) {
                    OutlinedTextField(
                        value = "$mesesSeleccionados meses",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Número de Meses *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedMeses) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        leadingIcon = {
                            Icon(Icons.Default.DateRange, null)
                        }
                    )
                    ExposedDropdownMenu(
                        expanded = expandedMeses,
                        onDismissRequest = { expandedMeses = false }
                    ) {
                        opcionesMeses.forEach { meses ->
                            DropdownMenuItem(
                                text = { Text("$meses meses") },
                                onClick = {
                                    mesesSeleccionados = meses
                                    expandedMeses = false
                                }
                            )
                        }
                    }
                }

                // Mes de inicio
                ExposedDropdownMenuBox(
                    expanded = expandedMesInicio,
                    onExpandedChange = { expandedMesInicio = it }
                ) {
                    OutlinedTextField(
                        value = "${mesInicio.month.getDisplayName(TextStyle.FULL, Locale("es"))} ${mesInicio.year}",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Mes de Inicio *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedMesInicio) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        leadingIcon = {
                            Icon(Icons.Default.Event, null)
                        }
                    )
                    ExposedDropdownMenu(
                        expanded = expandedMesInicio,
                        onDismissRequest = { expandedMesInicio = false }
                    ) {
                        mesesDisponibles.forEach { mes ->
                            DropdownMenuItem(
                                text = {
                                    Text("${mes.month.getDisplayName(TextStyle.FULL, Locale("es"))} ${mes.year}")
                                },
                                onClick = {
                                    mesInicio = mes
                                    expandedMesInicio = false
                                }
                            )
                        }
                    }
                }

                // Card resumen
                if (montoTotal.isNotBlank() && montoTotal.toDoubleOrNull() != null) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Resumen",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )

                            Divider(color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.2f))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "Pago mensual:",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    formatoMoneda(pagoMensual),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "Total:",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                                )
                                Text(
                                    formatoMoneda(montoTotal.toDoubleOrNull() ?: 0.0),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "Meses:",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                                )
                                Text(
                                    "$mesesSeleccionados meses",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Botones
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancelar")
                    }
                    Button(
                        onClick = {
                            if (bancoSeleccionadoId != null &&
                                descripcion.isNotBlank() &&
                                montoTotal.isNotBlank()
                            ) {
                                onConfirmar(
                                    bancoSeleccionadoId!!,
                                    descripcion,
                                    montoTotal.toDouble(),
                                    mesesSeleccionados,
                                    mesInicio
                                )
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = bancoSeleccionadoId != null &&
                                descripcion.isNotBlank() &&
                                montoTotal.isNotBlank() &&
                                montoTotal.toDoubleOrNull() != null &&
                                montoTotal.toDouble() > 0
                    ) {
                        Icon(Icons.Default.Done, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Crear MSI")
                    }
                }
            }
        }
    }
}

private fun formatoMoneda(monto: Double): String {
    val formato = NumberFormat.getCurrencyInstance(Locale("es", "MX"))
    return formato.format(monto)
}