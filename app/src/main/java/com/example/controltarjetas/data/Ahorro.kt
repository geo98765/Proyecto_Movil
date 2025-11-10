package com.example.controltarjetas.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "ahorros",
    foreignKeys = [
        ForeignKey(
            entity = InstitucionFinanciera::class,
            parentColumns = ["id"],
            childColumns = ["institucionId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Ahorro(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    // Referencia a la institución financiera
    val institucionId: Int,

    // Datos generales
    val nombre: String,
    val descripcion: String? = null,
    val fechaCreacion: String,

    // Para Tarjeta de Ahorro
    val montoTarjeta: Double? = null,

    // Para Acciones
    val cantidadAcciones: Double? = null,
    val precioCompraAccion: Double? = null,
    val simboloAccion: String? = null,

    // Para Cripto
    val cantidadCripto: Double? = null,
    val precioCompraCripto: Double? = null,
    val simboloCripto: String? = null,

    // Para CETES
    val montoCetes: Double? = null,
    val plazoCetes: Int? = null, // En días
    val tasaCetes: Double? = null // Porcentaje
) {
    // Función para calcular el valor total actual
    fun calcularValorTotal(tipoInversion: String): Double {
        return when (tipoInversion) {
            "Tarjeta" -> montoTarjeta ?: 0.0
            "Acciones" -> precioCompraAccion ?: 0.0
            "Cripto" -> precioCompraCripto ?: 0.0
            "CETES" -> montoCetes ?: 0.0
            else -> 0.0
        }
    }

}