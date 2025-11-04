package com.example.controltarjetas.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "historial_pagos")
data class HistorialPago(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val bancoId: Int,                  // Referencia al banco
    val nombreBanco: String,           // Guardamos el nombre por si eliminan el banco
    val deudaTotal: Double,
    val pagoMinimo: Double?,
    val fechaLimitePago: String,
    val fechaPago: String,             // Fecha en que marcó como pagado
    val notas: String? = null
)