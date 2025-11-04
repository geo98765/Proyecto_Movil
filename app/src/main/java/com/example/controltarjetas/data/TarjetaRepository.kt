package com.example.controltarjetas.data

import kotlinx.coroutines.flow.Flow

class TarjetaRepository(private val tarjetaDao: TarjetaDao) {

    val todasLasTarjetas: Flow<List<Tarjeta>> = tarjetaDao.obtenerTodasLasTarjetas()
    val tarjetasPendientes: Flow<List<Tarjeta>> = tarjetaDao.obtenerTarjetasPendientes()
    val deudaTotal: Flow<Double?> = tarjetaDao.obtenerDeudaTotal()

    suspend fun insertar(tarjeta: Tarjeta) {
        tarjetaDao.insertar(tarjeta)
    }

    suspend fun actualizar(tarjeta: Tarjeta) {
        tarjetaDao.actualizar(tarjeta)
    }

    suspend fun eliminar(tarjeta: Tarjeta) {
        tarjetaDao.eliminar(tarjeta)
    }

    suspend fun obtenerPorId(id: Int): Tarjeta? {
        return tarjetaDao.obtenerTarjetaPorId(id)
    }

    suspend fun eliminarPorId(id: Int) {
        tarjetaDao.eliminarPorId(id)
    }

    suspend fun obtenerTarjetaPorBancoYMes(bancoId: Int, mesPeriodo: String): Tarjeta? {
        return tarjetaDao.obtenerTarjetaPorBancoYMes(bancoId, mesPeriodo)
    }
}