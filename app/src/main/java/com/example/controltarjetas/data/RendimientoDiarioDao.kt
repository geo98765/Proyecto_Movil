package com.example.controltarjetas.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface RendimientoDiarioDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(rendimiento: RendimientoDiario)

    @Query("SELECT * FROM rendimientos_diarios WHERE ahorroId = :ahorroId ORDER BY fecha DESC")
    fun obtenerPorAhorro(ahorroId: Int): Flow<List<RendimientoDiario>>

    @Query("SELECT * FROM rendimientos_diarios WHERE fecha = :fecha")
    fun obtenerPorFecha(fecha: String): Flow<List<RendimientoDiario>>

    @Query("DELETE FROM rendimientos_diarios WHERE fecha < :fecha")
    suspend fun eliminarAnterioresA(fecha: String)
}