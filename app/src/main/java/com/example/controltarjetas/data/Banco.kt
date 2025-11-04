package com.example.controltarjetas.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bancos")
data class Banco(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nombreBanco: String,           // Ej: "BBVA", "Santander"
    val logoUri: String? = null,       // Ruta de la imagen del logo
    val limiteCredito: Double? = null, // Límite predefinido
    val fechaCorte: Int? = null,       // Día del mes (1-31)
    val diaPago: Int? = null  // Día de pago límite (1-31)
)
