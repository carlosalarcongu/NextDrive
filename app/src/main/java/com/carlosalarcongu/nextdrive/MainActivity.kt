package com.carlosalarcongu.nextdrive

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
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
    object UserGuide : AppScreen()
    data class AddEditVehicle(val vehicleId: Long? = null) : AppScreen()
    data class Dashboard(val vehicleId: Long) : AppScreen()
    data class ExpensePanel(val vehicleId: Long) : AppScreen()
    data class AddEditExpense(val vehicleId: Long, val expenseId: Long? = null) : AppScreen()
    data class DocumentPanel(val vehicleId: Long) : AppScreen()
    data class StatisticsPanel(val vehicleId: Long) : AppScreen()
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val database = AppDatabase.getDatabase(this)
        val viewModel: NextDriveViewModel by viewModels { NextDriveViewModelFactory(database.nextDriveDao()) }

        setContent {
            var isDarkTheme by remember { mutableStateOf(true) } // Estado del Tema

            NextDriveTheme(darkTheme = isDarkTheme) {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    var backStack by remember { mutableStateOf(listOf<AppScreen>(AppScreen.Garage)) }
                    val currentScreen = backStack.last()

                    val navigateTo: (AppScreen) -> Unit = { screen -> backStack = backStack + screen }
                    val navigateBack: () -> Unit = { if (backStack.size > 1) backStack = backStack.dropLast(1) else finish() }

                    BackHandler(enabled = backStack.size > 1) { navigateBack() }

                    when (val screen = currentScreen) {
                        is AppScreen.Garage -> GarageScreen(
                            viewModel = viewModel,
                            isDarkTheme = isDarkTheme,
                            onThemeToggle = { isDarkTheme = !isDarkTheme },
                            onNavigateToAddVehicle = { navigateTo(AppScreen.AddEditVehicle()) },
                            onVehicleClick = { vehicleId -> navigateTo(AppScreen.Dashboard(vehicleId)) },
                            onNavigateToUserGuide = { navigateTo(AppScreen.UserGuide) }
                        )
                        is AppScreen.UserGuide -> UserGuideScreen(onNavigateBack = navigateBack)
                        is AppScreen.AddEditVehicle -> AddVehicleScreen(vehicleId = screen.vehicleId, viewModel = viewModel, onNavigateBack = navigateBack)
                        is AppScreen.Dashboard -> VehicleDashboardScreen(
                            vehicleId = screen.vehicleId, viewModel = viewModel, onNavigateBack = navigateBack,
                            onNavigateToExpenses = { vId -> navigateTo(AppScreen.ExpensePanel(vId)) },
                            onNavigateToEdit = { vId -> navigateTo(AppScreen.AddEditVehicle(vId)) },
                            onNavigateToDocuments = { vId -> navigateTo(AppScreen.DocumentPanel(vId)) },
                            onNavigateToGraphs = { vId -> navigateTo(AppScreen.StatisticsPanel(vId)) }
                        )
                        is AppScreen.ExpensePanel -> ExpensePanelScreen(
                            vehicleId = screen.vehicleId, viewModel = viewModel, onNavigateBack = navigateBack,
                            onNavigateToAddExpense = { vId -> navigateTo(AppScreen.AddEditExpense(vId)) },
                            onNavigateToEditExpense = { vId, eId -> navigateTo(AppScreen.AddEditExpense(vId, eId)) }
                        )
                        is AppScreen.AddEditExpense -> AddExpenseScreen(vehicleId = screen.vehicleId, expenseId = screen.expenseId, viewModel = viewModel, onNavigateBack = navigateBack)
                        is AppScreen.DocumentPanel -> DocumentPanelScreen(vehicleId = screen.vehicleId, viewModel = viewModel, onNavigateBack = navigateBack)
                        is AppScreen.StatisticsPanel -> StatisticsPanelScreen(vehicleId = screen.vehicleId, viewModel = viewModel, onNavigateBack = navigateBack)
                    }
                }
            }
        }
    }
}