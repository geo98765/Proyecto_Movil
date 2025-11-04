package com.example.controltarjetas.data

import kotlinx.coroutines.flow.Flow

class BancoRepository(private val bancoDao: BancoDao) {

    val todosBancos: Flow<List<Banco>> = bancoDao.obtenerTodosBancos()

    suspend fun insertar(banco: Banco) {
        bancoDao.insertar(banco)
    }

    suspend fun actualizar(banco: Banco) {
        bancoDao.actualizar(banco)
    }

    suspend fun eliminar(banco: Banco) {
        bancoDao.eliminar(banco)
    }

    suspend fun obtenerPorId(id: Int): Banco? {
        return bancoDao.obtenerBancoPorId(id)
    }
}