package com.example.controltarjetas.data

import kotlinx.coroutines.flow.Flow

// ==================== AHORRO REPOSITORY ====================

class AhorroRepository(private val ahorroDao: AhorroDao) {

    val todosAhorros: Flow<List<Ahorro>> = ahorroDao.obtenerTodosAhorros()
    val totalesPorTipo: Flow<List<TotalPorTipo>> = ahorroDao.obtenerTotalesPorTipo()

    suspend fun insertar(ahorro: Ahorro) {
        ahorroDao.insertar(ahorro)
    }

    suspend fun actualizar(ahorro: Ahorro) {
        ahorroDao.actualizar(ahorro)
    }

    suspend fun eliminar(ahorro: Ahorro) {
        ahorroDao.eliminar(ahorro)
    }

    suspend fun obtenerPorId(id: Int): Ahorro? {
        return ahorroDao.obtenerAhorroPorId(id)
    }

    fun obtenerPorInstitucion(institucionId: Int): Flow<List<Ahorro>> {
        return ahorroDao.obtenerAhorrosPorInstitucion(institucionId)
    }
}

