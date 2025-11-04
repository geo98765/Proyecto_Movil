package com.example.controltarjetas.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.controltarjetas.TarjetaViewModel

// Rutas de navegación
sealed class Pantalla(val ruta: String) {
    object Lista : Pantalla("lista")
    object Agregar : Pantalla("agregar")
    object Editar : Pantalla("editar/{tarjetaId}") {
        fun crearRuta(tarjetaId: Int) = "editar/$tarjetaId"
    }
    object Bancos : Pantalla("bancos")
    object Historial : Pantalla("historial")
    object Configuracion : Pantalla("configuracion")  // NUEVA
    object Estadisticas : Pantalla("estadisticas")
}

@Composable
fun NavegacionApp(
    navController: NavHostController = rememberNavController(),
    viewModel: TarjetaViewModel = viewModel()
) {
    NavHost(
        navController = navController,
        startDestination = Pantalla.Lista.ruta
    ) {
        // Pantalla principal - Lista de tarjetas
        composable(Pantalla.Lista.ruta) {
            PantallaListaTarjetas(
                viewModel = viewModel,
                onAgregarTarjeta = {
                    navController.navigate(Pantalla.Agregar.ruta)
                },
                onEditarTarjeta = { tarjeta ->
                    navController.navigate(Pantalla.Editar.crearRuta(tarjeta.id))
                },
                onNavigateBancos = {
                    navController.navigate(Pantalla.Bancos.ruta)
                },
                onNavigateHistorial = {
                    navController.navigate(Pantalla.Historial.ruta)
                },
                onNavigateEstadisticas = {
                    navController.navigate(Pantalla.Estadisticas.ruta)
                },
                onNavigateConfiguracion = {
                    navController.navigate(Pantalla.Configuracion.ruta)
                }
            )
        }

        // Pantalla agregar nueva tarjeta
        composable(Pantalla.Agregar.ruta) {
            PantallaAgregarEditarTarjeta(
                viewModel = viewModel,
                tarjetaId = null,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Pantalla editar tarjeta existente
        composable(
            route = Pantalla.Editar.ruta,
            arguments = listOf(
                navArgument("tarjetaId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val tarjetaId = backStackEntry.arguments?.getInt("tarjetaId")
            PantallaAgregarEditarTarjeta(
                viewModel = viewModel,
                tarjetaId = tarjetaId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Pantalla de bancos
        composable(Pantalla.Bancos.ruta) {
            PantallaBancos(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Pantalla de historial
        composable(Pantalla.Historial.ruta) {
            PantallaHistorial(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        // Pantalla de configuración
        composable(Pantalla.Configuracion.ruta) {
            PantallaConfiguracion(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Pantalla de estadísticas
        composable(Pantalla.Estadisticas.ruta) {
            PantallaEstadisticas(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }

}
