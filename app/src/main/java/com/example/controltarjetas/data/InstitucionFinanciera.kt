
package com.example.controltarjetas.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "instituciones_financieras")
data class InstitucionFinanciera(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nombreInstitucion: String,
    val logoUri: String? = null,
    val tipoInversion: String, // "Tarjeta", "Acciones", "Cripto", "CETES"
    val rendimientoAnual: Double? = null // Porcentaje opcional
)