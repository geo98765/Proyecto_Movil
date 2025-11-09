package com.example.controltarjetas.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "tarjetas",
    foreignKeys = [
        ForeignKey(
            entity = Banco::class,
            parentColumns = ["id"],
            childColumns = ["bancoId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Tarjeta(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val bancoId: Int,                  // Referencia al banco
    val tipoTarjeta: String,           // Ej: "Crédito", "Débito"

    // Montos del mes actual
    val deudaTotal: Double,            // Ej: 5000.00
    val pagoMinimo: Double? = null,    // Ej: 250.00 (opcional)

    // Fechas
    val fechaLimitePago: String,       // Ej: "2025-11-15"
    val mesPeriodo: String,            // Ej: "2025-11" - Para saber a qué mes pertenece

    // Estado
    val estaPagada: Boolean = false,   // true = pagada este mes

    // Notas adicionales
    val notas: String? = null,         // Ej: "Usar para gasolina" (opcional)

    // NUEVO: Meses Sin Intereses (MSI)
    val esMSI: Boolean = false,                // Si es parte de un MSI
    val msiGrupoId: String? = null,            // ID único para agrupar MSI (ej: "msi_123456")
    val msiDescripcion: String? = null,        // Descripción de la compra (ej: "Laptop Dell")
    val msiMesActual: Int? = null,             // Mes actual (ej: 1 de 3)
    val msiMesesTotal: Int? = null,            // Total de meses (ej: 3, 6, 9, 12)
    val msiMontoTotal: Double? = null,         // Monto total de la compra original
    val msiMontoPorMes: Double? = null         // Monto que se paga este mes
)