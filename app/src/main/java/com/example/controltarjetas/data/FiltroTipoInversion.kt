package com.example.controltarjetas.data

// Enum para los filtros de tipo de inversión
enum class FiltroTipoInversion {
    TODOS,
    TARJETA,
    ACCIONES,
    CRIPTO,
    CETES
}

// Enum para ordenamiento
enum class OrdenAhorro {
    MONTO_DESC,
    MONTO_ASC,
    FECHA_DESC,
    FECHA_ASC,
    NOMBRE_ASC,
    NOMBRE_DESC
}

// Clase para mantener el estado de los filtros
data class EstadoFiltrosAhorro(
    val tipoInversion: FiltroTipoInversion = FiltroTipoInversion.TODOS,
    val institucionId: Int? = null,
    val orden: OrdenAhorro = OrdenAhorro.FECHA_DESC,
    val busqueda: String = ""
)