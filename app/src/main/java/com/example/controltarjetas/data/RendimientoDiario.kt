package com.example.controltarjetas.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "rendimientos_diarios",
    foreignKeys = [
        ForeignKey(
            entity = Ahorro::class,
            parentColumns = ["id"],
            childColumns = ["ahorroId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class RendimientoDiario(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val ahorroId: Int,
    val fecha: String,
    val rendimiento: Double,
    val valorBase: Double,
    val tasaAplicada: Double
)