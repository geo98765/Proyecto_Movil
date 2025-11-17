package com.example.controltarjetas.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [Tarjeta::class,
        Banco::class,
        HistorialPago::class,
        Ahorro::class,
        InstitucionFinanciera::class,
        RendimientoDiario::class  // NUEVO
    ],
    version = 7,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun tarjetaDao(): TarjetaDao
    abstract fun bancoDao(): BancoDao
    abstract fun historialPagoDao(): HistorialPagoDao
    abstract fun ahorroDao(): AhorroDao
    abstract fun institucionFinancieraDao(): InstitucionFinancieraDao

    abstract fun rendimientoDiarioDao(): RendimientoDiarioDao  // NUEVO

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE bancos ADD COLUMN diaPago INTEGER DEFAULT NULL")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE tarjetas ADD COLUMN esMSI INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE tarjetas ADD COLUMN msiGrupoId TEXT DEFAULT NULL")
                database.execSQL("ALTER TABLE tarjetas ADD COLUMN msiDescripcion TEXT DEFAULT NULL")
                database.execSQL("ALTER TABLE tarjetas ADD COLUMN msiMesActual INTEGER DEFAULT NULL")
                database.execSQL("ALTER TABLE tarjetas ADD COLUMN msiMesesTotal INTEGER DEFAULT NULL")
                database.execSQL("ALTER TABLE tarjetas ADD COLUMN msiMontoTotal REAL DEFAULT NULL")
                database.execSQL("ALTER TABLE tarjetas ADD COLUMN msiMontoPorMes REAL DEFAULT NULL")
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Tabla ahorros sin institución (versión antigua)
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS ahorros_old (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        nombre TEXT NOT NULL,
                        fotoUri TEXT,
                        tipoAhorro TEXT NOT NULL,
                        rendimientoAnual REAL,
                        descripcion TEXT,
                        fechaCreacion TEXT NOT NULL,
                        montoTarjeta REAL,
                        cantidadAcciones REAL,
                        precioCompraAccion REAL,
                        simboloAccion TEXT,
                        cantidadCripto REAL,
                        precioCompraCripto REAL,
                        simboloCripto TEXT,
                        montoCetes REAL,
                        plazoCetes INTEGER,
                        tasaCetes REAL
                    )
                """)
            }
        }

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Crear tabla instituciones_financieras
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS instituciones_financieras (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        nombreInstitucion TEXT NOT NULL,
                        logoUri TEXT,
                        tipoInversion TEXT NOT NULL,
                        rendimientoAnual REAL
                    )
                """)

                // Eliminar tabla antigua de ahorros si existe
                database.execSQL("DROP TABLE IF EXISTS ahorros_old")
                database.execSQL("DROP TABLE IF EXISTS ahorros")

                // Crear nueva tabla ahorros con foreign key
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS ahorros (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        institucionId INTEGER NOT NULL,
                        nombre TEXT NOT NULL,
                        descripcion TEXT,
                        fechaCreacion TEXT NOT NULL,
                        montoTarjeta REAL,
                        cantidadAcciones REAL,
                        precioCompraAccion REAL,
                        simboloAccion TEXT,
                        cantidadCripto REAL,
                        precioCompraCripto REAL,
                        simboloCripto TEXT,
                        montoCetes REAL,
                        plazoCetes INTEGER,
                        tasaCetes REAL,
                        FOREIGN KEY(institucionId) REFERENCES instituciones_financieras(id) ON DELETE CASCADE
                    )
                """)
            }
        }


        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
            CREATE TABLE IF NOT EXISTS rendimientos_diarios (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                ahorroId INTEGER NOT NULL,
                fecha TEXT NOT NULL,
                rendimiento REAL NOT NULL,
                valorBase REAL NOT NULL,
                tasaAplicada REAL NOT NULL,
                FOREIGN KEY(ahorroId) REFERENCES ahorros(id) ON DELETE CASCADE
            )
        """)
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "tarjetas_database"
                )
                    .addMigrations(MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }



    }
}