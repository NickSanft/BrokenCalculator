package com.nick.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class CalculatorViewModel : ViewModel() {
    val display = mutableStateOf("0")

    private var expression = ""
    private var resultJustCalculated = false

    fun onAction(action: CalculatorAction) {
        when (action) {
            is CalculatorAction.Number -> {
                if (resultJustCalculated) {
                    expression = "" // Start new expression
                    resultJustCalculated = false
                }
                expression += action.number
                display.value = expression
            }
            is CalculatorAction.Operation -> {
                // Prevent adding multiple operators or adding an operator at the start
                if (expression.isNotEmpty() && expression.last().isDigit()) {
                    expression += action.operation
                    display.value = expression
                    resultJustCalculated = false
                }
            }
            CalculatorAction.Clear -> {
                expression = ""
                display.value = "0"
                resultJustCalculated = false
            }
            CalculatorAction.Equals -> {
                if (expression.isNotEmpty() && expression.last().isDigit()) {
                    val result = evaluateExpression(expression)
                    // Format result to remove .0 for whole numbers
                    val resultString = if (result.rem(1.0) == 0.0) {
                        result.toLong().toString()
                    } else {
                        result.toString()
                    }
                    display.value = resultString
                    expression = resultString
                    resultJustCalculated = true
                }
            }
        }
    }

    private fun evaluateExpression(expression: String): Double {
        // This evaluation is left-to-right and does not follow standard operator precedence.
        val numbers = expression.split(Regex("[+\\-*/]")).mapNotNull { it.toDoubleOrNull() }.toMutableList()
        val ops = expression.filter { it in "+-*/" }.toMutableList()

        if (numbers.isEmpty()) return 0.0

        var result = numbers.removeAt(0)

        while (ops.isNotEmpty()) {
            val op = ops.removeAt(0)
            if (numbers.isEmpty()) break // Handles trailing operator case e.g. "5+"
            val number = numbers.removeAt(0)
            result = when (op) {
                '+' -> result + number
                '-' -> result - number
                '*' -> result * number
                '/' -> if (number != 0.0) result / number else Double.NaN
                else -> result
            }
        }

        return result
    }
}

sealed class CalculatorAction {
    data class Number(val number: String) : CalculatorAction()
    data class Operation(val operation: String) : CalculatorAction()
    object Clear : CalculatorAction()
    object Equals : CalculatorAction()
}
