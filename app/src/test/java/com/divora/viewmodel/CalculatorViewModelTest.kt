package com.divora.viewmodel

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.divora.data.TestUserDataStore
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class CalculatorViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var viewModel: CalculatorViewModel
    private lateinit var userDataStore: TestUserDataStore
    private lateinit var application: Application

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        application = mockk(relaxed = true)
        userDataStore = TestUserDataStore(application)

        viewModel = CalculatorViewModel(application, userDataStore)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `test initial state`() = runTest {
        assertEquals("0", viewModel.display.value)
        assertFalse(viewModel.operationStates["-"]!!)
        assertFalse(viewModel.operationStates["*"]!!)
        assertFalse(viewModel.operationStates["/"]!!)
        assertFalse(viewModel.operationStates["√"]!!)
        assertFalse(viewModel.operationStates["%"]!!)
    }

    @Test
    fun `test number input`() {
        viewModel.onAction(CalculatorAction.Number("5"))
        assertEquals("5", viewModel.display.value)
    }

    @Test
    fun `test operation input - unlocked`() {
        viewModel.onAction(CalculatorAction.Number("5"))
        viewModel.onAction(CalculatorAction.Operation("+"))
        assertEquals("5+", viewModel.display.value)
    }

    @Test
    fun `test operation input - locked`() {
        viewModel.onAction(CalculatorAction.Number("5"))
        viewModel.onAction(CalculatorAction.Operation("-"))
        assertEquals("5", viewModel.display.value)
    }

    @Test
    fun `test clear action`() {
        viewModel.onAction(CalculatorAction.Number("5"))
        viewModel.onAction(CalculatorAction.Clear)
        assertEquals("0", viewModel.display.value)
    }

    @Test
    fun `test backspace action`() {
        viewModel.onAction(CalculatorAction.Number("5"))
        viewModel.onAction(CalculatorAction.Number("3"))
        viewModel.onAction(CalculatorAction.Backspace)
        assertEquals("5", viewModel.display.value)
    }

    @Test
    fun `test equals action - addition`() {
        viewModel.onAction(CalculatorAction.Number("2"))
        viewModel.onAction(CalculatorAction.Operation("+"))
        viewModel.onAction(CalculatorAction.Number("3"))
        viewModel.onAction(CalculatorAction.Equals)
        assertEquals("5", viewModel.display.value)
    }

    @Test
    fun `test unary operation - sqrt`() = runTest {
        viewModel.operationStates["√"] = true
        viewModel.onAction(CalculatorAction.Number("81"))
        viewModel.onAction(CalculatorAction.UnaryOperation("√"))
        assertEquals(9.0.toString(), viewModel.display.value)
    }

    @Test
    fun `test unary operation - percent`() = runTest {
        viewModel.operationStates["%"] = true
        viewModel.onAction(CalculatorAction.Number("10"))
        viewModel.onAction(CalculatorAction.UnaryOperation("%"))
        assertEquals(0.1.toString(), viewModel.display.value)
    }

    @Test
    fun `test unlock subtraction`() {
        viewModel.onAction(CalculatorAction.Number("2"))
        viewModel.onAction(CalculatorAction.Operation("+"))
        viewModel.onAction(CalculatorAction.Number("2"))
        viewModel.onAction(CalculatorAction.Equals)
        assertEquals(true, viewModel.operationStates["-"])
        assertEquals("Congratulations! You've unlocked Subtraction!", viewModel.unlockedOperationMessage.value)
    }
    
    @Test
    fun `test reset action`() = runTest {
        userDataStore.setOperationUnlocked("-", true)
        viewModel.onAction(CalculatorAction.Reset)
        assertFalse(viewModel.operationStates["-"]!!)
    }
    
    @Test
    fun `test left to right evaluation for same precedence`() {
        // 5 + 3 - 2 = 6
        viewModel.operationStates["-"] = true
        viewModel.onAction(CalculatorAction.Number("5"))
        viewModel.onAction(CalculatorAction.Operation("+"))
        viewModel.onAction(CalculatorAction.Number("3"))
        viewModel.onAction(CalculatorAction.Operation("-"))
        viewModel.onAction(CalculatorAction.Number("2"))
        viewModel.onAction(CalculatorAction.Equals)
        assertEquals("6", viewModel.display.value)
    }

    @Test
    fun `test operator precedence`() {
        // 2 + 3 * 4 = 14
        viewModel.operationStates["*"] = true
        viewModel.onAction(CalculatorAction.Number("2"))
        viewModel.onAction(CalculatorAction.Operation("+"))
        viewModel.onAction(CalculatorAction.Number("3"))
        viewModel.onAction(CalculatorAction.Operation("*"))
        viewModel.onAction(CalculatorAction.Number("4"))
        viewModel.onAction(CalculatorAction.Equals)
        assertEquals("14", viewModel.display.value)
    }

    @Test
    fun `test multiple high precedence operators`() {
        // 10 * 2 / 5 = 4
        viewModel.operationStates["*"] = true
        viewModel.operationStates["/"] = true
        viewModel.onAction(CalculatorAction.Number("10"))
        viewModel.onAction(CalculatorAction.Operation("*"))
        viewModel.onAction(CalculatorAction.Number("2"))
        viewModel.onAction(CalculatorAction.Operation("/"))
        viewModel.onAction(CalculatorAction.Number("5"))
        viewModel.onAction(CalculatorAction.Equals)
        assertEquals("4", viewModel.display.value)
    }

    @Test
    fun `test complex expression with precedence`() {
        // 2 + 3 * 4 - 10 / 5 = 12
        viewModel.operationStates["-"] = true
        viewModel.operationStates["*"] = true
        viewModel.operationStates["/"] = true
        viewModel.onAction(CalculatorAction.Number("2"))
        viewModel.onAction(CalculatorAction.Operation("+"))
        viewModel.onAction(CalculatorAction.Number("3"))
        viewModel.onAction(CalculatorAction.Operation("*"))
        viewModel.onAction(CalculatorAction.Number("4"))
        viewModel.onAction(CalculatorAction.Operation("-"))
        viewModel.onAction(CalculatorAction.Number("10"))
        viewModel.onAction(CalculatorAction.Operation("/"))
        viewModel.onAction(CalculatorAction.Number("5"))
        viewModel.onAction(CalculatorAction.Equals)
        assertEquals("12", viewModel.display.value)
    }
}
