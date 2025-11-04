package com.example.controltarjetas.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [Tarjeta::class, Banco::class, HistorialPago::class],
    version = 3,  // Incrementamos la versión a 3
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun tarjetaDao(): TarjetaDao
    abstract fun bancoDao(): BancoDao
    abstract fun historialPagoDao(): HistorialPagoDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Migración de versión 2 a 3 (agregar diaPago a bancos)
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Agregar columna diaPago a la tabla bancos
                database.execSQL("ALTER TABLE bancos ADD COLUMN diaPago INTEGER DEFAULT NULL")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "tarjetas_database"
                )
                    .addMigrations(MIGRATION_2_3)  // Agregar la migración
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}