package com.example.controltarjetas.data

import kotlinx.coroutines.flow.Flow

class HistorialRepository(private val historialPagoDao: HistorialPagoDao) {

    val todoHistorial: Flow<List<HistorialPago>> = historialPagoDao.obtenerTodoHistorial()

    suspend fun insertar(historialPago: HistorialPago) {
        historialPagoDao.insertar(historialPago)
    }

    fun obtenerHistorialPorBanco(bancoId: Int): Flow<List<HistorialPago>> {
        return historialPagoDao.obtenerHistorialPorBanco(bancoId)
    }

    suspend fun eliminar(id: Int) {
        historialPagoDao.eliminarPorId(id)
    }
}