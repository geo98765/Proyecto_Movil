package com.example.controltarjetas

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.controltarjetas.notifications.NotificationScheduler
import com.example.controltarjetas.preferences.PreferencesManager
import com.example.controltarjetas.ui.NavegacionApp
import com.example.controltarjetas.ui.theme.ControlTarjetasTheme

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permiso concedido, programar notificaciones
            NotificationScheduler(this).scheduleNotifications()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val preferencesManager = PreferencesManager(applicationContext)

        // Solicitar permiso de notificaciones en Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                // Ya tiene permiso, programar notificaciones
                NotificationScheduler(this).scheduleNotifications()
            }
        } else {
            // Android 12 o menor, no necesita permiso
            NotificationScheduler(this).scheduleNotifications()
        }

        setContent {
            val darkMode by preferencesManager.darkModeFlow.collectAsState(initial = false)

            ControlTarjetasTheme(darkTheme = darkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val tarjetaViewModel: TarjetaViewModel = viewModel()
                    val ahorroViewModel: AhorroViewModel = viewModel()
                    val institucionViewModel: InstitucionFinancieraViewModel = viewModel()

                    NavegacionApp(
                        tarjetaViewModel = tarjetaViewModel,
                        ahorroViewModel = ahorroViewModel,
                        institucionViewModel = institucionViewModel
                    )
                }
            }
        }
    }
}