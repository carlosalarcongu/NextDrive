package com.carlosalarcongu.nextdrive

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.carlosalarcongu.nextdrive.data.AppDatabase
import com.carlosalarcongu.nextdrive.ui.*
import com.carlosalarcongu.nextdrive.ui.theme.NextDriveTheme

sealed class AppScreen {
    object Garage : AppScreen()
    object FuelPrices : AppScreen() // ¡NUEVA PANTALLA PRINCIPAL!
    object Settings : AppScreen()
    object UserGuide : AppScreen()
    data class AddEditVehicle(val vehicleId: Long? = null) : AppScreen()
    data class Dashboard(val vehicleId: Long) : AppScreen()
    data class ExpensePanel(val vehicleId: Long) : AppScreen()
    data class AddEditExpense(val vehicleId: Long, val expenseId: Long? = null, val defaultCategory: String = "Pieza") : AppScreen()
    data class AddPieza(val vehicleId: Long, val expenseId: Long? = null) : AppScreen()
    data class AddRepostaje(val vehicleId: Long, val expenseId: Long? = null) : AppScreen()
    data class AddMantenimiento(val vehicleId: Long, val expenseId: Long? = null) : AppScreen()
    data class AddAveria(val vehicleId: Long, val expenseId: Long? = null) : AppScreen()
    data class AddTramite(val vehicleId: Long, val expenseId: Long? = null) : AppScreen()
    data class DocumentPanel(val vehicleId: Long) : AppScreen()
    data class StatisticsPanel(val vehicleId: Long) : AppScreen()
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val database = AppDatabase.getDatabase(this)
        val viewModel: NextDriveViewModel by viewModels { NextDriveViewModelFactory(database.nextDriveDao()) }

        val sharedPrefs = getSharedPreferences("NextDrivePrefs", Context.MODE_PRIVATE)

        setContent {
            var themeMode by remember { mutableStateOf(sharedPrefs.getString("theme", "SYSTEM") ?: "SYSTEM") }

            NextDriveTheme(themeMode = themeMode) {
                var backStack by remember { mutableStateOf(listOf<AppScreen>(AppScreen.Garage)) }
                val currentScreen = backStack.last()

                val navigateTo: (AppScreen) -> Unit = { screen -> backStack = backStack + screen }
                val navigateBack: () -> Unit = { if (backStack.size > 1) backStack = backStack.dropLast(1) else finish() }

                BackHandler(enabled = backStack.size > 1) { navigateBack() }

                Scaffold(
                    bottomBar = {
                        // Mostramos la barra inferior en las TRES pestañas principales
                        if (currentScreen is AppScreen.Garage || currentScreen is AppScreen.Settings || currentScreen is AppScreen.FuelPrices) {
                            NavigationBar {
                                NavigationBarItem(
                                    icon = { Icon(Icons.Default.DirectionsCar, contentDescription = "Garaje") },
                                    label = { Text("Mi Garaje") },
                                    selected = currentScreen is AppScreen.Garage,
                                    onClick = { if (currentScreen !is AppScreen.Garage) navigateTo(AppScreen.Garage) }
                                )
                                NavigationBarItem(
                                    icon = { Icon(Icons.Default.LocalGasStation, contentDescription = "Gasolineras") },
                                    label = { Text("Precios") },
                                    selected = currentScreen is AppScreen.FuelPrices,
                                    onClick = { if (currentScreen !is AppScreen.FuelPrices) navigateTo(AppScreen.FuelPrices) }
                                )
                                NavigationBarItem(
                                    icon = { Icon(Icons.Default.Settings, contentDescription = "Ajustes") },
                                    label = { Text("Ajustes") },
                                    selected = currentScreen is AppScreen.Settings,
                                    onClick = { if (currentScreen !is AppScreen.Settings) navigateTo(AppScreen.Settings) }
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                        when (val screen = currentScreen) {
                            is AppScreen.Garage -> GarageScreen(
                                viewModel = viewModel,
                                onNavigateToAddVehicle = { navigateTo(AppScreen.AddEditVehicle()) },
                                onVehicleClick = { vehicleId -> navigateTo(AppScreen.Dashboard(vehicleId)) },
                                onNavigateToUserGuide = { navigateTo(AppScreen.UserGuide) }
                            )
                            is AppScreen.FuelPrices -> FuelPricesScreen() // ¡NUEVA RUTA!
                            is AppScreen.Settings -> SettingsScreen(
                                viewModel = viewModel,
                                currentTheme = themeMode,
                                onThemeChange = { newTheme ->
                                    themeMode = newTheme
                                    sharedPrefs.edit().putString("theme", newTheme).apply()
                                }
                            )
                            is AppScreen.UserGuide -> UserGuideScreen(onNavigateBack = navigateBack)
                            is AppScreen.AddEditVehicle -> AddVehicleScreen(vehicleId = screen.vehicleId, viewModel = viewModel, onNavigateBack = navigateBack)
                            is AppScreen.Dashboard -> VehicleDashboardScreen(
                                vehicleId = screen.vehicleId, viewModel = viewModel, onNavigateBack = navigateBack,
                                onNavigateToExpenses = { vId -> navigateTo(AppScreen.ExpensePanel(vId)) },
                                onNavigateToEdit = { vId -> navigateTo(AppScreen.AddEditVehicle(vId)) },
                                onNavigateToDocuments = { vId -> navigateTo(AppScreen.DocumentPanel(vId)) },
                                onNavigateToGraphs = { vId -> navigateTo(AppScreen.StatisticsPanel(vId)) },
                                onNavigateToAddRepostaje = { vId -> navigateTo(AppScreen.AddRepostaje(vId)) }
                            )
                            is AppScreen.ExpensePanel -> ExpensePanelScreen(
                                vehicleId = screen.vehicleId, viewModel = viewModel, onNavigateBack = navigateBack,
                                onNavigateToAdd = { vId, cat ->
                                    when(cat) {
                                        "Repostaje" -> navigateTo(AppScreen.AddRepostaje(vId))
                                        "Mantenimiento" -> navigateTo(AppScreen.AddMantenimiento(vId))
                                        "Avería" -> navigateTo(AppScreen.AddAveria(vId))
                                        "Trámites" -> navigateTo(AppScreen.AddTramite(vId))
                                        else -> navigateTo(AppScreen.AddPieza(vId))
                                    }
                                },
                                onNavigateToEdit = { vId, cat, eId ->
                                    when(cat) {
                                        "Repostaje" -> navigateTo(AppScreen.AddRepostaje(vId, eId))
                                        "Mantenimiento" -> navigateTo(AppScreen.AddMantenimiento(vId, eId))
                                        "Avería" -> navigateTo(AppScreen.AddAveria(vId, eId))
                                        "Trámites" -> navigateTo(AppScreen.AddTramite(vId, eId))
                                        else -> navigateTo(AppScreen.AddPieza(vId, eId))
                                    }
                                }
                            )
                            is AppScreen.AddEditExpense -> AddExpenseScreen(vehicleId = screen.vehicleId, expenseId = screen.expenseId, defaultCategory = screen.defaultCategory, viewModel = viewModel, onNavigateBack = navigateBack)
                            is AppScreen.AddPieza -> AddPiezaScreen(screen.vehicleId, screen.expenseId, viewModel, navigateBack)
                            is AppScreen.AddRepostaje -> AddRepostajeScreen(screen.vehicleId, screen.expenseId, viewModel, navigateBack)
                            is AppScreen.AddMantenimiento -> AddMantenimientoScreen(screen.vehicleId, screen.expenseId, "Mantenimiento", viewModel, navigateBack)
                            is AppScreen.AddAveria -> AddMantenimientoScreen(screen.vehicleId, screen.expenseId, "Avería", viewModel, navigateBack)
                            is AppScreen.AddTramite -> AddTramiteScreen(screen.vehicleId, screen.expenseId, viewModel, navigateBack)
                            is AppScreen.DocumentPanel -> DocumentPanelScreen(vehicleId = screen.vehicleId, viewModel = viewModel, onNavigateBack = navigateBack)
                            is AppScreen.StatisticsPanel -> StatisticsPanelScreen(vehicleId = screen.vehicleId, viewModel = viewModel, onNavigateBack = navigateBack)
                        }
                    }
                }
            }
        }
    }
}