package com.example.controltarjetas

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.controltarjetas.data.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class InstitucionFinancieraViewModel(application: Application) : AndroidViewModel(application) {

    private val institucionRepository: InstitucionFinancieraRepository

    val todasInstituciones: Flow<List<InstitucionFinanciera>>

    init {
        val database = AppDatabase.getDatabase(application)
        institucionRepository = InstitucionFinancieraRepository(database.institucionFinancieraDao())

        todasInstituciones = institucionRepository.todasInstituciones
    }

    fun insertarInstitucion(institucion: InstitucionFinanciera) = viewModelScope.launch {
        institucionRepository.insertar(institucion)
    }

    fun actualizarInstitucion(institucion: InstitucionFinanciera) = viewModelScope.launch {
        institucionRepository.actualizar(institucion)
    }

    fun eliminarInstitucion(institucion: InstitucionFinanciera) = viewModelScope.launch {
        institucionRepository.eliminar(institucion)
    }

    suspend fun obtenerInstitucionPorId(id: Int): InstitucionFinanciera? {
        return institucionRepository.obtenerPorId(id)
    }

    fun obtenerInstitucionesPorTipo(tipo: String): Flow<List<InstitucionFinanciera>> {
        return institucionRepository.obtenerPorTipo(tipo)
    }
}