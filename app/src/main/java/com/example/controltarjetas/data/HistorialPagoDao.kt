package com.example.controltarjetas.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface HistorialPagoDao {

    @Insert
    suspend fun insertar(historialPago: HistorialPago)

    @Query("SELECT * FROM historial_pagos ORDER BY fechaPago DESC")
    fun obtenerTodoHistorial(): Flow<List<HistorialPago>>

    @Query("SELECT * FROM historial_pagos WHERE bancoId = :bancoId ORDER BY fechaPago DESC")
    fun obtenerHistorialPorBanco(bancoId: Int): Flow<List<HistorialPago>>

    @Query("DELETE FROM historial_pagos WHERE id = :id")
    suspend fun eliminarPorId(id: Int)

    @Query("""
        SELECT fechaPago, SUM(deudaTotal) as total 
        FROM historial_pagos 
        GROUP BY fechaPago 
        ORDER BY fechaPago DESC 
        LIMIT 12
    """)
    fun obtenerPagosPorFecha(): Flow<List<PagoMensual>>
}

data class PagoMensual(
    val fechaPago: String,
    val total: Double
)