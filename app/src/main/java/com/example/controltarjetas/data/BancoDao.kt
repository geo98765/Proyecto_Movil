package com.example.controltarjetas.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BancoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(banco: Banco)

    @Update
    suspend fun actualizar(banco: Banco)

    @Delete
    suspend fun eliminar(banco: Banco)

    @Query("SELECT * FROM bancos ORDER BY nombreBanco ASC")
    fun obtenerTodosBancos(): Flow<List<Banco>>

    @Query("SELECT * FROM bancos WHERE id = :id")
    suspend fun obtenerBancoPorId(id: Int): Banco?
}