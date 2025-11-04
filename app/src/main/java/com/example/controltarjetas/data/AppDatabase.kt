package com.example.controltarjetas.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [Tarjeta::class, Banco::class, HistorialPago::class],
    version = 2,  // Incrementamos la versión
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun tarjetaDao(): TarjetaDao
    abstract fun bancoDao(): BancoDao
    abstract fun historialPagoDao(): HistorialPagoDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "tarjetas_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}