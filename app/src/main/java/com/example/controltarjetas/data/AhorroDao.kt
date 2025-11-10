package com.example.controltarjetas.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AhorroDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(ahorro: Ahorro)

    @Update
    suspend fun actualizar(ahorro: Ahorro)

    @Delete
    suspend fun eliminar(ahorro: Ahorro)

    @Query("SELECT * FROM ahorros ORDER BY fechaCreacion DESC")
    fun obtenerTodosAhorros(): Flow<List<Ahorro>>

    @Query("SELECT * FROM ahorros WHERE id = :id")
    suspend fun obtenerAhorroPorId(id: Int): Ahorro?

    @Query("SELECT * FROM ahorros WHERE institucionId = :institucionId ORDER BY fechaCreacion DESC")
    fun obtenerAhorrosPorInstitucion(institucionId: Int): Flow<List<Ahorro>>

    @Query("DELETE FROM ahorros WHERE id = :id")
    suspend fun eliminarPorId(id: Int)

    // Obtener total por tipo de inversión
    @Query("""
        SELECT i.tipoInversion, SUM(
            CASE 
                WHEN i.tipoInversion = 'Tarjeta' THEN a.montoTarjeta
                WHEN i.tipoInversion = 'Acciones' THEN a.precioCompraAccion
                WHEN i.tipoInversion = 'Cripto' THEN a.precioCompraCripto
                WHEN i.tipoInversion = 'CETES' THEN a.montoCetes
                ELSE 0
            END
        ) as total
        FROM ahorros a
        INNER JOIN instituciones_financieras i ON a.institucionId = i.id
        GROUP BY i.tipoInversion
        ORDER BY total DESC
    """)
    fun obtenerTotalesPorTipo(): Flow<List<TotalPorTipo>>
}

data class TotalPorTipo(
    val tipoInversion: String,
    val total: Double
)