package com.example.controltarjetas.data

import kotlinx.coroutines.flow.Flow

class InstitucionFinancieraRepository(private val institucionDao: InstitucionFinancieraDao) {

    val todasInstituciones: Flow<List<InstitucionFinanciera>> = institucionDao.obtenerTodasInstituciones()

    suspend fun insertar(institucion: InstitucionFinanciera) {
        institucionDao.insertar(institucion)
    }

    suspend fun actualizar(institucion: InstitucionFinanciera) {
        institucionDao.actualizar(institucion)
    }

    suspend fun eliminar(institucion: InstitucionFinanciera) {
        institucionDao.eliminar(institucion)
    }

    suspend fun obtenerPorId(id: Int): InstitucionFinanciera? {
        return institucionDao.obtenerInstitucionPorId(id)
    }

    fun obtenerPorTipo(tipo: String): Flow<List<InstitucionFinanciera>> {
        return institucionDao.obtenerInstitucionesPorTipo(tipo)
    }
}
