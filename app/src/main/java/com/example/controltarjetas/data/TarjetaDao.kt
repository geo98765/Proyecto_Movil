package com.example.controltarjetas.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TarjetaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(tarjeta: Tarjeta)

    @Update
    suspend fun actualizar(tarjeta: Tarjeta)

    @Delete
    suspend fun eliminar(tarjeta: Tarjeta)

    @Query("SELECT * FROM tarjetas ORDER BY fechaLimitePago ASC")
    fun obtenerTodasLasTarjetas(): Flow<List<Tarjeta>>

    @Query("SELECT * FROM tarjetas WHERE estaPagada = 0 ORDER BY fechaLimitePago ASC")
    fun obtenerTarjetasPendientes(): Flow<List<Tarjeta>>

    @Query("SELECT * FROM tarjetas WHERE id = :id")
    suspend fun obtenerTarjetaPorId(id: Int): Tarjeta?

    @Query("SELECT SUM(deudaTotal) FROM tarjetas WHERE estaPagada = 0")
    fun obtenerDeudaTotal(): Flow<Double?>

    @Query("DELETE FROM tarjetas WHERE id = :id")
    suspend fun eliminarPorId(id: Int)

    @Query("SELECT * FROM tarjetas WHERE bancoId = :bancoId AND mesPeriodo = :mesPeriodo")
    suspend fun obtenerTarjetaPorBancoYMes(bancoId: Int, mesPeriodo: String): Tarjeta?

    // Estadísticas
    @Query("""
        SELECT mesPeriodo, SUM(deudaTotal) as total 
        FROM tarjetas 
        GROUP BY mesPeriodo 
        ORDER BY mesPeriodo DESC 
        LIMIT 6
    """)
    fun obtenerDeudasPorMes(): Flow<List<DeudaMensual>>

    @Query("""
        SELECT b.nombreBanco, SUM(t.deudaTotal) as total
        FROM tarjetas t
        INNER JOIN bancos b ON t.bancoId = b.id
        WHERE t.estaPagada = 0
        GROUP BY b.nombreBanco
        ORDER BY total DESC
    """)
    fun obtenerDeudasPorBanco(): Flow<List<DeudaPorBanco>>
}

data class DeudaMensual(
    val mesPeriodo: String,
    val total: Double
)

data class DeudaPorBanco(
    val nombreBanco: String,
    val total: Double
)
