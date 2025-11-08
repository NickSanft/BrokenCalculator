package com.nick

import com.nick.viewmodel.CalculatorAction
import com.nick.viewmodel.CalculatorViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CalculatorViewModelTest {

    private lateinit var viewModel: CalculatorViewModel

    @Before
    fun setup() {
        viewModel = CalculatorViewModel()
    }

    @Test
    fun `test initial state`() {
        assertTrue(viewModel.operationStates["+"] == true)
        assertFalse(viewModel.operationStates["-"] == true)
        assertFalse(viewModel.operationStates["*"] == true)
        assertFalse(viewModel.operationStates["/"] == true)
    }

    @Test
    fun `test unlock subtraction`() {
        viewModel.onAction(CalculatorAction.Number("2"))
        viewModel.onAction(CalculatorAction.Operation("+"))
        viewModel.onAction(CalculatorAction.Number("2"))
        viewModel.onAction(CalculatorAction.Equals)
        assertTrue(viewModel.operationStates["-"] == true)
    }

    @Test
    fun `test unlock division`() {
        // First, unlock subtraction
        viewModel.onAction(CalculatorAction.Number("2"))
        viewModel.onAction(CalculatorAction.Operation("+"))
        viewModel.onAction(CalculatorAction.Number("2"))
        viewModel.onAction(CalculatorAction.Equals)

        // Now, unlock division
        viewModel.onAction(CalculatorAction.Number("5"))
        viewModel.onAction(CalculatorAction.Operation("-"))
        viewModel.onAction(CalculatorAction.Number("1"))
        viewModel.onAction(CalculatorAction.Equals)
        assertTrue(viewModel.operationStates["/"] == true)
    }

    @Test
    fun `test unlock multiplication`() {
        // First, unlock subtraction and division
        viewModel.onAction(CalculatorAction.Number("2"))
        viewModel.onAction(CalculatorAction.Operation("+"))
        viewModel.onAction(CalculatorAction.Number("2"))
        viewModel.onAction(CalculatorAction.Equals)
        viewModel.onAction(CalculatorAction.Number("5"))
        viewModel.onAction(CalculatorAction.Operation("-"))
        viewModel.onAction(CalculatorAction.Number("1"))
        viewModel.onAction(CalculatorAction.Equals)

        // Now, unlock multiplication
        viewModel.onAction(CalculatorAction.Number("1"))
        viewModel.onAction(CalculatorAction.Operation("/"))
        viewModel.onAction(CalculatorAction.Number("0"))
        viewModel.onAction(CalculatorAction.Equals)
        assertTrue(viewModel.operationStates["*"] == true)
    }

    @Test
    fun `test reset operations`() {
        // Unlock all operations
        viewModel.onAction(CalculatorAction.Number("2"))
        viewModel.onAction(CalculatorAction.Operation("+"))
        viewModel.onAction(CalculatorAction.Number("2"))
        viewModel.onAction(CalculatorAction.Equals)
        viewModel.onAction(CalculatorAction.Number("5"))
        viewModel.onAction(CalculatorAction.Operation("-"))
        viewModel.onAction(CalculatorAction.Number("1"))
        viewModel.onAction(CalculatorAction.Equals)
        viewModel.onAction(CalculatorAction.Number("1"))
        viewModel.onAction(CalculatorAction.Operation("/"))
        viewModel.onAction(CalculatorAction.Number("0"))
        viewModel.onAction(CalculatorAction.Equals)

        // Reset
        viewModel.onAction(CalculatorAction.Reset)

        // Check that they are all disabled (except for addition)
        assertTrue(viewModel.operationStates["+"] == true)
        assertFalse(viewModel.operationStates["-"] == true)
        assertFalse(viewModel.operationStates["*"] == true)
        assertFalse(viewModel.operationStates["/"] == true)
    }
}
