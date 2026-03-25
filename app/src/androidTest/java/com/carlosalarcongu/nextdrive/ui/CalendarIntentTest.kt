package com.carlosalarcongu.nextdrive.ui

import android.content.Intent
import android.provider.CalendarContract
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.carlosalarcongu.nextdrive.data.Expense
import com.carlosalarcongu.nextdrive.data.NextDriveDao
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import org.hamcrest.Matchers.allOf
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CalendarIntentTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun setUp() {
        Intents.init()
    }

    @After
    fun tearDown() {
        Intents.release()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun guardarMantenimiento_ConCalendario_LanzaIntentCorrecto() {
        // 1. Preparamos el Mock Dao
        val mockDao = mockk<NextDriveDao>(relaxed = true)

        // 2. Preparamos un Gasto Falso (Mock) que ya tiene el recordatorio configurado
        val fakeExpense = Expense(
            id = 99L,
            vehicleId = 1L,
            title = "Cambio de Ruedas",
            totalCost = 250.0,
            dateMillis = System.currentTimeMillis(),
            category = "Mantenimiento",
            hasReminder = true, // Recordatorio Activo
            reminderType = "CALENDARIO", // Tipo Calendario
            reminderTimePeriod = 6, // 6 meses
            reminderTimeUnit = "Meses"
        )

        // 3. Le decimos al Mock Dao que, si alguien le pide el gasto 99L, devuelva nuestro Gasto Falso
        every { mockDao.getExpenseById(99L) } returns flowOf(fakeExpense)

        val viewModel = NextDriveViewModel(mockDao)

        // 4. Lanzamos la pantalla pasándole el ID 99L. La pantalla se autocompletará.
        composeTestRule.setContent {
            AddMantenimientoScreen(
                vehicleId = 1L,
                expenseId = 99L, // Pasamos el ID del gasto falso
                categoryStr = "Mantenimiento",
                viewModel = viewModel,
                onNavigateBack = {}
            )
        }

        // 5. Esperamos un poco a que el LaunchedEffect de la pantalla cargue el gasto
        composeTestRule.waitForIdle()

        // 6. Solo tenemos que hacer click en GUARDAR
        composeTestRule.onNodeWithText("GUARDAR").performScrollTo().performClick()

        // 7. Verificar que se intentó abrir el calendario
        intended(
            allOf(
                hasAction(Intent.ACTION_INSERT),
                hasData(CalendarContract.Events.CONTENT_URI),
                hasExtra(CalendarContract.Events.TITLE, "Mantenimiento: Cambio de Ruedas")
            )
        )
    }
}