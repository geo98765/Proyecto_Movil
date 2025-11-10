package com.example.controltarjetas.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.controltarjetas.AhorroViewModel
import com.example.controltarjetas.InstitucionFinancieraViewModel
import com.example.controltarjetas.TarjetaViewModel
import kotlinx.coroutines.launch

// Rutas de navegación
sealed class Pantalla(val ruta: String, val titulo: String, val icono: androidx.compose.ui.graphics.vector.ImageVector) {
    object Lista : Pantalla("lista", "Control de Pagos", Icons.Default.CreditCard)
    object Agregar : Pantalla("agregar", "Agregar Pago", Icons.Default.Add)
    object Editar : Pantalla("editar/{tarjetaId}", "Editar Pago", Icons.Default.Edit) {
        fun crearRuta(tarjetaId: Int) = "editar/$tarjetaId"
    }
    object Bancos : Pantalla("bancos", "Bancos", Icons.Default.AccountBalance)
    object Historial : Pantalla("historial", "Historial", Icons.Default.History)
    object Configuracion : Pantalla("configuracion", "Configuración", Icons.Default.Settings)
    object Estadisticas : Pantalla("estadisticas", "Estadísticas", Icons.Default.BarChart)

    // Rutas para Ahorros
    object Ahorros : Pantalla("ahorros", "Mis Ahorros", Icons.Default.Savings)
    object AgregarAhorro : Pantalla("agregar_ahorro", "Agregar Ahorro", Icons.Default.Add)
    object EditarAhorro : Pantalla("editar_ahorro/{ahorroId}", "Editar Ahorro", Icons.Default.Edit) {
        fun crearRuta(ahorroId: Int) = "editar_ahorro/$ahorroId"
    }
    object InstitucionesFinancieras : Pantalla("instituciones_financieras", "Instituciones", Icons.Default.AccountBalance)

    // NUEVA: Ruta para Rendimientos
    object Rendimientos : Pantalla("rendimientos", "Rendimientos", Icons.Default.TrendingUp)
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavegacionApp(
    navController: NavHostController = rememberNavController(),
    tarjetaViewModel: TarjetaViewModel = viewModel(),
    ahorroViewModel: AhorroViewModel = viewModel(),
    institucionViewModel: InstitucionFinancieraViewModel = viewModel()
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                DrawerContent(
                    currentRoute = currentRoute,
                    onItemClick = { route ->
                        scope.launch {
                            drawerState.close()
                            navController.navigate(route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            }
        }
    ) {
        NavHost(
            navController = navController,
            startDestination = Pantalla.Lista.ruta
        ) {
            // ===== RUTAS DE TARJETAS =====

            // Pantalla principal - Lista de tarjetas
            composable(Pantalla.Lista.ruta) {
                PantallaListaTarjetas(
                    viewModel = tarjetaViewModel,
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
                    },
                    onOpenDrawer = {
                        scope.launch { drawerState.open() }
                    }
                )
            }

            // Pantalla agregar nueva tarjeta
            composable(Pantalla.Agregar.ruta) {
                PantallaAgregarEditarTarjeta(
                    viewModel = tarjetaViewModel,
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
                    viewModel = tarjetaViewModel,
                    tarjetaId = tarjetaId,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            // Pantalla de bancos
            composable(Pantalla.Bancos.ruta) {
                PantallaBancos(
                    viewModel = tarjetaViewModel,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            // Pantalla de historial
            composable(Pantalla.Historial.ruta) {
                PantallaHistorial(
                    viewModel = tarjetaViewModel,
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
                    viewModel = tarjetaViewModel,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            // ===== RUTAS DE AHORROS =====

            // Pantalla principal de ahorros
            composable(Pantalla.Ahorros.ruta) {
                PantallaListaAhorros(
                    viewModel = ahorroViewModel,
                    institucionViewModel = institucionViewModel,
                    onAgregarAhorro = {
                        navController.navigate(Pantalla.AgregarAhorro.ruta)
                    },
                    onEditarAhorro = { ahorro ->
                        navController.navigate(Pantalla.EditarAhorro.crearRuta(ahorro.id))
                    },
                    onNavigateInstituciones = {
                        navController.navigate(Pantalla.InstitucionesFinancieras.ruta)
                    },
                    onOpenDrawer = {
                        scope.launch { drawerState.open() }
                    },
                    // NUEVO: Agregar navegación a rendimientos
                    onNavigateRendimientos = {
                        navController.navigate(Pantalla.Rendimientos.ruta)
                    }
                )
            }

            // Pantalla agregar nuevo ahorro
            composable(Pantalla.AgregarAhorro.ruta) {
                PantallaAgregarEditarAhorro(
                    viewModel = ahorroViewModel,
                    institucionViewModel = institucionViewModel,
                    ahorroId = null,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            // Pantalla editar ahorro existente
            composable(
                route = Pantalla.EditarAhorro.ruta,
                arguments = listOf(
                    navArgument("ahorroId") { type = NavType.IntType }
                )
            ) { backStackEntry ->
                val ahorroId = backStackEntry.arguments?.getInt("ahorroId")
                PantallaAgregarEditarAhorro(
                    viewModel = ahorroViewModel,
                    institucionViewModel = institucionViewModel,
                    ahorroId = ahorroId,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            // Pantalla de instituciones financieras
            composable(Pantalla.InstitucionesFinancieras.ruta) {
                PantallaInstitucionesFinancieras(
                    viewModel = institucionViewModel,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            // NUEVA: Pantalla de rendimientos
            composable(Pantalla.Rendimientos.ruta) {
                PantallaRendimientos(
                    viewModel = ahorroViewModel
                )
            }
        }
    }
}

@Composable
fun DrawerContent(
    currentRoute: String?,
    onItemClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 16.dp)
    ) {
        // Header del drawer
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 24.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = MaterialTheme.shapes.medium
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.AccountBalanceWallet,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Control Financiero",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "Gestiona tus finanzas",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Divider(modifier = Modifier.padding(vertical = 8.dp))

        Text(
            text = "PRINCIPAL",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 28.dp, vertical = 8.dp)
        )

        // Control de Pagos
        DrawerItem(
            icon = Icons.Default.CreditCard,
            label = "Control de Pagos",
            isSelected = currentRoute == Pantalla.Lista.ruta,
            onClick = { onItemClick(Pantalla.Lista.ruta) }
        )

        // Ahorros
        DrawerItem(
            icon = Icons.Default.Savings,
            label = "Mis Ahorros",
            isSelected = currentRoute == Pantalla.Ahorros.ruta,
            onClick = { onItemClick(Pantalla.Ahorros.ruta) }
        )

        // NUEVO: Rendimientos
        DrawerItem(
            icon = Icons.Default.TrendingUp,
            label = "Rendimientos",
            isSelected = currentRoute == Pantalla.Rendimientos.ruta,
            onClick = { onItemClick(Pantalla.Rendimientos.ruta) }
        )

        Divider(modifier = Modifier.padding(vertical = 8.dp))

        Text(
            text = "GESTIÓN",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 28.dp, vertical = 8.dp)
        )

        // Bancos
        DrawerItem(
            icon = Icons.Default.AccountBalance,
            label = "Bancos",
            isSelected = currentRoute == Pantalla.Bancos.ruta,
            onClick = { onItemClick(Pantalla.Bancos.ruta) }
        )

        // Instituciones Financieras
        DrawerItem(
            icon = Icons.Default.AccountBalance,
            label = "Instituciones",
            isSelected = currentRoute == Pantalla.InstitucionesFinancieras.ruta,
            onClick = { onItemClick(Pantalla.InstitucionesFinancieras.ruta) }
        )

        // Historial
        DrawerItem(
            icon = Icons.Default.History,
            label = "Historial",
            isSelected = currentRoute == Pantalla.Historial.ruta,
            onClick = { onItemClick(Pantalla.Historial.ruta) }
        )

        // Estadísticas
        DrawerItem(
            icon = Icons.Default.BarChart,
            label = "Estadísticas",
            isSelected = currentRoute == Pantalla.Estadisticas.ruta,
            onClick = { onItemClick(Pantalla.Estadisticas.ruta) }
        )

        Spacer(modifier = Modifier.weight(1f))

        Divider(modifier = Modifier.padding(vertical = 8.dp))

        // Configuración
        DrawerItem(
            icon = Icons.Default.Settings,
            label = "Configuración",
            isSelected = currentRoute == Pantalla.Configuracion.ruta,
            onClick = { onItemClick(Pantalla.Configuracion.ruta) }
        )
    }
}

@Composable
fun DrawerItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    NavigationDrawerItem(
        icon = { Icon(icon, contentDescription = null) },
        label = { Text(label) },
        selected = isSelected,
        onClick = onClick,
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
    )
}