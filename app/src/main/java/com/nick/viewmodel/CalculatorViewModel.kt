package com.nick.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class CalculatorViewModel : ViewModel() {
    val display = mutableStateOf("0")

    private var currentNumber = ""
    private var previousNumber = ""
    private var currentOperation = ""

    fun onAction(action: CalculatorAction) {
        when (action) {
            is CalculatorAction.Number -> {
                if (currentNumber.length < 10) {
                    currentNumber += action.number
                    display.value = currentNumber
                }
            }
            is CalculatorAction.Operation -> {
                if (currentNumber.isNotEmpty()) {
                    previousNumber = currentNumber
                    currentNumber = ""
                    currentOperation = action.operation
                }
            }
            CalculatorAction.Clear -> {
                currentNumber = ""
                previousNumber = ""
                currentOperation = ""
                display.value = "0"
            }
            CalculatorAction.Equals -> {
                if (currentNumber.isNotEmpty() && previousNumber.isNotEmpty()) {
                    val result = when (currentOperation) {
                        "+" -> previousNumber.toDouble() + currentNumber.toDouble()
                        "-" -> previousNumber.toDouble() - currentNumber.toDouble()
                        "*" -> previousNumber.toDouble() * currentNumber.toDouble()
                        "/" -> previousNumber.toDouble() / currentNumber.toDouble()
                        else -> 0.0
                    }
                    display.value = result.toString()
                    currentNumber = result.toString()
                    previousNumber = ""
                    currentOperation = ""
                }
            }
        }
    }
}

sealed class CalculatorAction {
    data class Number(val number: String) : CalculatorAction()
    data class Operation(val operation: String) : CalculatorAction()
    object Clear : CalculatorAction()
    object Equals : CalculatorAction()
}
