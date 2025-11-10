package com.example.controltarjetas.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface InstitucionFinancieraDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(institucion: InstitucionFinanciera)

    @Update
    suspend fun actualizar(institucion: InstitucionFinanciera)

    @Delete
    suspend fun eliminar(institucion: InstitucionFinanciera)

    @Query("SELECT * FROM instituciones_financieras ORDER BY nombreInstitucion ASC")
    fun obtenerTodasInstituciones(): Flow<List<InstitucionFinanciera>>

    @Query("SELECT * FROM instituciones_financieras WHERE id = :id")
    suspend fun obtenerInstitucionPorId(id: Int): InstitucionFinanciera?

    @Query("SELECT * FROM instituciones_financieras WHERE tipoInversion = :tipo ORDER BY nombreInstitucion ASC")
    fun obtenerInstitucionesPorTipo(tipo: String): Flow<List<InstitucionFinanciera>>
}