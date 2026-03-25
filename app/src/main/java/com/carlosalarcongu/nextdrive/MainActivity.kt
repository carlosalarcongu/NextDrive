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
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.EventNote
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
    object FuelPrices : AppScreen()
    object StatisticsGlobal : AppScreen()
    object Upcoming : AppScreen()
    object Settings : AppScreen()
    object UserGuide : AppScreen()
    object Forecast : AppScreen() // NUEVA PANTALLA: Estimación Anual

    data class AddEditVehicle(val vehicleId: Long? = null) : AppScreen()
    data class Dashboard(val vehicleId: Long) : AppScreen()
    data class AddPieza(val vehicleId: Long, val expenseId: Long? = null) : AppScreen()
    data class AddRepostaje(val vehicleId: Long, val expenseId: Long? = null) : AppScreen()
    data class AddMantenimiento(val vehicleId: Long, val expenseId: Long? = null, val prefillTitle: String? = null) : AppScreen()
    data class AddAveria(val vehicleId: Long, val expenseId: Long? = null) : AppScreen()
    data class AddTramite(val vehicleId: Long, val expenseId: Long? = null) : AppScreen()
    data class DocumentPanel(val vehicleId: Long) : AppScreen()
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val database = AppDatabase.getDatabase(this)
        val viewModel: NextDriveViewModel by viewModels { NextDriveViewModelFactory(database.nextDriveDao()) }
        val sharedPrefs = getSharedPreferences("NextDrivePrefs", Context.MODE_PRIVATE)

        setContent {
            // ESTADOS DE PREFERENCIAS GLOBALES RECUPERADOS
            var themeMode by remember { mutableStateOf(sharedPrefs.getString("theme", "SYSTEM") ?: "SYSTEM") }
            var colorPalette by remember { mutableStateOf(sharedPrefs.getString("palette", "VAMPIRIC") ?: "VAMPIRIC") }
            var fontSize by remember { mutableStateOf(sharedPrefs.getString("fontSize", "MEDIANO") ?: "MEDIANO") }
            var unitDist by remember { mutableStateOf(sharedPrefs.getString("unitDist", "Kilómetros") ?: "Kilómetros") }
            var unitCurr by remember { mutableStateOf(sharedPrefs.getString("unitCurr", "Euros (€)") ?: "Euros (€)") }
            var unitVol by remember { mutableStateOf(sharedPrefs.getString("unitVol", "Litros") ?: "Litros") }
            var dateFormat by remember { mutableStateOf(sharedPrefs.getString("dateFormat", "Sistema") ?: "Sistema") }

            NextDriveTheme(themeMode = themeMode, palette = colorPalette, fontSizeStr = fontSize) {
                var backStack by remember { mutableStateOf(listOf<AppScreen>(AppScreen.Garage)) }
                val currentScreen = backStack.last()

                val navigateTo: (AppScreen) -> Unit = { screen -> backStack = backStack + screen }
                val navigateBack: () -> Unit = { if (backStack.size > 1) backStack = backStack.dropLast(1) else finish() }

                BackHandler(enabled = backStack.size > 1) { navigateBack() }

                Scaffold(
                    bottomBar = {
                        val mainScreens = listOf(AppScreen.Garage, AppScreen.FuelPrices, AppScreen.StatisticsGlobal, AppScreen.Upcoming, AppScreen.Settings)
                        if (mainScreens.any { it::class == currentScreen::class }) {
                            NavigationBar {
                                NavigationBarItem(icon = { Icon(Icons.Default.DirectionsCar, "Garaje") }, label = { Text("Garaje", maxLines=1) }, selected = currentScreen is AppScreen.Garage, onClick = { navigateTo(AppScreen.Garage) })
                                NavigationBarItem(icon = { Icon(Icons.Default.LocalGasStation, "Gas") }, label = { Text("Precios", maxLines=1) }, selected = currentScreen is AppScreen.FuelPrices, onClick = { navigateTo(AppScreen.FuelPrices) })
                                NavigationBarItem(icon = { Icon(Icons.Default.BarChart, "Stats") }, label = { Text("Gráficas", maxLines=1) }, selected = currentScreen is AppScreen.StatisticsGlobal, onClick = { navigateTo(AppScreen.StatisticsGlobal) })
                                NavigationBarItem(icon = { Icon(Icons.Default.EventNote, "Próximos") }, label = { Text("Próximos", maxLines=1) }, selected = currentScreen is AppScreen.Upcoming, onClick = { navigateTo(AppScreen.Upcoming) })
                                NavigationBarItem(icon = { Icon(Icons.Default.Settings, "Ajustes") }, label = { Text("Ajustes", maxLines=1) }, selected = currentScreen is AppScreen.Settings, onClick = { navigateTo(AppScreen.Settings) })
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                        when (val screen = currentScreen) {
                            is AppScreen.Garage -> GarageScreen(viewModel, { navigateTo(AppScreen.AddEditVehicle()) }, { navigateTo(AppScreen.Dashboard(it)) }, { navigateTo(AppScreen.UserGuide) })
                            is AppScreen.FuelPrices -> FuelPricesScreen()

                            // NUEVA INTEGRACIÓN DE FORECAST
                            is AppScreen.StatisticsGlobal -> StatisticsPanelScreen(
                                vehicleId = null,
                                viewModel = viewModel,
                                onNavigateBack = navigateBack,
                                onNavigateToForecast = { navigateTo(AppScreen.Forecast) }
                            )
                            is AppScreen.Forecast -> ForecastScreen(
                                viewModel = viewModel,
                                onNavigateBack = navigateBack
                            )

                            is AppScreen.Upcoming -> UpcomingScreen(viewModel,
                                onAttend = { vId, title -> navigateTo(AppScreen.AddMantenimiento(vId, null, title)) },
                                onEdit = { vId, eId, cat ->
                                    when(cat) { "Repostaje" -> navigateTo(AppScreen.AddRepostaje(vId, eId)); "Trámites" -> navigateTo(AppScreen.AddTramite(vId, eId)); "Avería" -> navigateTo(AppScreen.AddAveria(vId, eId)); "Pieza" -> navigateTo(AppScreen.AddPieza(vId, eId)); else -> navigateTo(AppScreen.AddMantenimiento(vId, eId)) }
                                }
                            )
                            is AppScreen.Settings -> SettingsScreen(
                                viewModel = viewModel,
                                themeMode = themeMode, colorPalette = colorPalette, fontSize = fontSize,
                                unitDist = unitDist, unitCurr = unitCurr, unitVol = unitVol, dateFormat = dateFormat,
                                onUpdatePref = { key, value ->
                                    sharedPrefs.edit().putString(key, value).apply()
                                    when(key) {
                                        "theme" -> themeMode = value
                                        "palette" -> colorPalette = value
                                        "fontSize" -> fontSize = value
                                        "unitDist" -> unitDist = value
                                        "unitCurr" -> unitCurr = value
                                        "unitVol" -> unitVol = value
                                        "dateFormat" -> dateFormat = value
                                    }
                                }
                            )
                            is AppScreen.UserGuide -> UserGuideScreen(navigateBack)
                            is AppScreen.AddEditVehicle -> AddVehicleScreen(screen.vehicleId, viewModel, navigateBack)
                            is AppScreen.Dashboard -> VehicleDashboardScreen(
                                vehicleId = screen.vehicleId, viewModel = viewModel, onNavigateBack = navigateBack,
                                onNavigateToEdit = { vId -> navigateTo(AppScreen.AddEditVehicle(vId)) },
                                onNavigateToDocuments = { vId -> navigateTo(AppScreen.DocumentPanel(vId)) },
                                onNavigateToAdd = { vId, cat ->
                                    when(cat) { "Repostaje" -> navigateTo(AppScreen.AddRepostaje(vId)); "Mantenimiento" -> navigateTo(AppScreen.AddMantenimiento(vId)); "Avería" -> navigateTo(AppScreen.AddAveria(vId)); "Trámites" -> navigateTo(AppScreen.AddTramite(vId)); else -> navigateTo(AppScreen.AddPieza(vId)) }
                                },
                                onNavigateToEditExpense = { vId, cat, eId ->
                                    when(cat) { "Repostaje" -> navigateTo(AppScreen.AddRepostaje(vId, eId)); "Mantenimiento" -> navigateTo(AppScreen.AddMantenimiento(vId, eId)); "Avería" -> navigateTo(AppScreen.AddAveria(vId, eId)); "Trámites" -> navigateTo(AppScreen.AddTramite(vId, eId)); else -> navigateTo(AppScreen.AddPieza(vId, eId)) }
                                }
                            )
                            is AppScreen.AddPieza -> AddPiezaScreen(screen.vehicleId, screen.expenseId, viewModel, navigateBack)
                            is AppScreen.AddRepostaje -> AddRepostajeScreen(screen.vehicleId, screen.expenseId, viewModel, navigateBack)
                            is AppScreen.AddMantenimiento -> AddMantenimientoScreen(screen.vehicleId, screen.expenseId, "Mantenimiento", screen.prefillTitle, viewModel, navigateBack)
                            is AppScreen.AddAveria -> AddMantenimientoScreen(screen.vehicleId, screen.expenseId, "Avería", null, viewModel, navigateBack)
                            is AppScreen.AddTramite -> AddTramiteScreen(screen.vehicleId, screen.expenseId, viewModel, navigateBack)
                            is AppScreen.DocumentPanel -> DocumentPanelScreen(screen.vehicleId, viewModel, navigateBack)
                        }
                    }
                }
            }
        }
    }
}