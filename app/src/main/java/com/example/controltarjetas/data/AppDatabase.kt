package com.example.controltarjetas.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [Tarjeta::class, Banco::class, HistorialPago::class],
    version = 4,  // Incrementamos la versión a 4
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
                database.execSQL("ALTER TABLE bancos ADD COLUMN diaPago INTEGER DEFAULT NULL")
            }
        }

        // Migración de versión 3 a 4 (agregar campos MSI a tarjetas)
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Agregar campos para Meses Sin Intereses (MSI)
                database.execSQL("ALTER TABLE tarjetas ADD COLUMN esMSI INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE tarjetas ADD COLUMN msiGrupoId TEXT DEFAULT NULL")
                database.execSQL("ALTER TABLE tarjetas ADD COLUMN msiDescripcion TEXT DEFAULT NULL")
                database.execSQL("ALTER TABLE tarjetas ADD COLUMN msiMesActual INTEGER DEFAULT NULL")
                database.execSQL("ALTER TABLE tarjetas ADD COLUMN msiMesesTotal INTEGER DEFAULT NULL")
                database.execSQL("ALTER TABLE tarjetas ADD COLUMN msiMontoTotal REAL DEFAULT NULL")
                database.execSQL("ALTER TABLE tarjetas ADD COLUMN msiMontoPorMes REAL DEFAULT NULL")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "tarjetas_database"
                )
                    .addMigrations(MIGRATION_2_3, MIGRATION_3_4)  // Agregar ambas migraciones
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}