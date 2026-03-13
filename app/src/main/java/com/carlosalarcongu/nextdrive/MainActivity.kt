// MainActivity.kt
package com.carlosalarcongu.nextdrive

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.carlosalarcongu.nextdrive.data.AppDatabase
import com.carlosalarcongu.nextdrive.ui.*
import com.carlosalarcongu.nextdrive.ui.theme.NextDriveTheme

sealed class AppScreen {
    object Garage : AppScreen()
    data class AddEditVehicle(val vehicleId: Long? = null) : AppScreen()
    data class Dashboard(val vehicleId: Long) : AppScreen()
    data class ExpensePanel(val vehicleId: Long) : AppScreen()
    data class AddExpense(val vehicleId: Long) : AppScreen()
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val database = AppDatabase.getDatabase(this)
        val viewModel: NextDriveViewModel by viewModels { NextDriveViewModelFactory(database.nextDriveDao()) }

        setContent {
            // ¡Cambiado! Antes ponía MaterialTheme
            NextDriveTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background // Automáticamente cogerá el NightGray
                ) {
                    var currentScreen by remember { mutableStateOf<AppScreen>(AppScreen.Garage) }

                    when (val screen = currentScreen) {
                        is AppScreen.Garage -> GarageScreen(
                            viewModel = viewModel,
                            onNavigateToAddVehicle = { currentScreen = AppScreen.AddEditVehicle() },
                            onVehicleClick = { vehicleId -> currentScreen = AppScreen.Dashboard(vehicleId) }
                        )
                        is AppScreen.AddEditVehicle -> AddVehicleScreen(
                            vehicleId = screen.vehicleId,
                            viewModel = viewModel,
                            onNavigateBack = {
                                currentScreen = if (screen.vehicleId != null) AppScreen.Dashboard(screen.vehicleId) else AppScreen.Garage
                            }
                        )
                        is AppScreen.Dashboard -> VehicleDashboardScreen(
                            vehicleId = screen.vehicleId,
                            viewModel = viewModel,
                            onNavigateBack = { currentScreen = AppScreen.Garage },
                            onNavigateToExpenses = { vId -> currentScreen = AppScreen.ExpensePanel(vId) },
                            onNavigateToEdit = { vId -> currentScreen = AppScreen.AddEditVehicle(vId) }
                        )
                        is AppScreen.ExpensePanel -> ExpensePanelScreen(
                            vehicleId = screen.vehicleId,
                            viewModel = viewModel,
                            onNavigateBack = { currentScreen = AppScreen.Dashboard(screen.vehicleId) },
                            onNavigateToAddExpense = { vId -> currentScreen = AppScreen.AddExpense(vId) }
                        )
                        is AppScreen.AddExpense -> AddExpenseScreen(
                            vehicleId = screen.vehicleId,
                            viewModel = viewModel,
                            onNavigateBack = { currentScreen = AppScreen.ExpensePanel(screen.vehicleId) }
                        )
                    }
                }
            }
        }
    }
}