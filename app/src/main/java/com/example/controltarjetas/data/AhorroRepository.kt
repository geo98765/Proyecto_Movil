package com.example.controltarjetas.data

import kotlinx.coroutines.flow.Flow

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

    suspend fun eliminarPorId(id: Int) {
        ahorroDao.eliminarPorId(id)
    }

    fun obtenerAhorrosPorInstitucion(institucionId: Int): Flow<List<Ahorro>> {
        return ahorroDao.obtenerAhorrosPorInstitucion(institucionId)
    }
}
