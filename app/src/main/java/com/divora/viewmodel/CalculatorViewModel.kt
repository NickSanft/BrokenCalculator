package com.divora.viewmodel

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

data class HintInfo(
    val description: String,
    val code: String,
    val isUnlocked: () -> Boolean
)

class CalculatorViewModel : ViewModel() {
    val display = mutableStateOf("0")
    val operationStates = mutableStateMapOf("+" to true, "-" to false, "*" to false, "/" to false)
    val showHintsDialog = mutableStateOf(false)
    val unlockedOperationMessage = mutableStateOf<String?>(null)
    val allOperationsUnlocked = mutableStateOf(false)
    private var allOperationsAlreadyUnlocked = false

    val hints: List<HintInfo> = listOf(
        HintInfo(
            description = "Subtraction (-): 2+2",
            code = """
                if (expression == "2+2") {
                    operationStates["-"] = true
                }
            """.trimIndent(),
            isUnlocked = { operationStates["-"] == true }
        ),
        HintInfo(
            description = "Division (/): 5-1",
            code = """
                if (expression == "5-1") {
                    operationStates["/"] = true
                }
            """.trimIndent(),
            isUnlocked = { operationStates["/"] == true }
        ),
        HintInfo(
            description = "Multiplication (*): Attempt to divide by zero",
            code = """
                if (result.isNaN()) {
                   operationStates["*"] = true
                }
            """.trimIndent(),
            isUnlocked = { operationStates["*"] == true }
        )
    )

    private var expression = ""
    private var resultJustCalculated = false

    fun onAction(action: CalculatorAction) {
        when (action) {
            is CalculatorAction.Number -> {
                if (resultJustCalculated) {
                    expression = ""
                    resultJustCalculated = false
                }
                expression += action.number
                display.value = expression
            }
            is CalculatorAction.Operation -> {
                if (operationStates[action.operation] == true) {
                    if (expression.isNotEmpty() && expression.last().isDigit()) {
                        expression += action.operation
                        display.value = expression
                        resultJustCalculated = false
                    }
                }
            }
            CalculatorAction.Clear -> {
                expression = ""
                display.value = "0"
                resultJustCalculated = false
            }
            CalculatorAction.Backspace -> {
                if (resultJustCalculated) {
                    expression = ""
                    display.value = "0"
                    resultJustCalculated = false
                } else if (expression.isNotEmpty()) {
                    expression = expression.dropLast(1)
                    display.value = expression.ifEmpty { "0" }
                }
            }
            CalculatorAction.Equals -> {
                if (expression.isNotEmpty() && expression.last().isDigit()) {
                    // Cheat codes
                    if (expression == "2+2" && operationStates["-"] == false) {
                        operationStates["-"] = true
                        unlockedOperationMessage.value = "Congratulations! You've unlocked Subtraction!"
                    }
                    if (expression == "5-1" && operationStates["-"] == true && operationStates["/"] == false) {
                        operationStates["/"] = true
                        unlockedOperationMessage.value = "Congratulations! You've unlocked Division!"
                    }

                    val result = evaluateExpression(expression)

                    if (result.isNaN()) {
                        if (operationStates["*"] == false) {
                            operationStates["*"] = true
                            unlockedOperationMessage.value = "Congratulations! You've unlocked Multiplication!"
                        }
                        display.value = "Error"
                        expression = ""
                        resultJustCalculated = true
                        checkAllOperationsUnlocked()
                        return
                    }

                    val resultString = if (result.rem(1.0) == 0.0) {
                        result.toLong().toString()
                    } else {
                        result.toString()
                    }
                    display.value = resultString
                    expression = resultString
                    resultJustCalculated = true
                    checkAllOperationsUnlocked()
                }
            }
            CalculatorAction.ShowHints -> showHintsDialog.value = true
            CalculatorAction.HideHints -> showHintsDialog.value = false
            CalculatorAction.Reset -> {
                resetOperations()
                showHintsDialog.value = false
            }
            CalculatorAction.DismissUnlockMessage -> unlockedOperationMessage.value = null
            CalculatorAction.DismissAllOperationsUnlockedDialog -> {
                allOperationsUnlocked.value = false
                allOperationsAlreadyUnlocked = true
            }
        }
    }

    private fun checkAllOperationsUnlocked() {
        if (!allOperationsAlreadyUnlocked && operationStates.values.all { it }) {
            allOperationsUnlocked.value = true
            unlockedOperationMessage.value = null // Prevent double dialog
        }
    }

    private fun resetOperations() {
        operationStates["-"] = false
        operationStates["*"] = false
        operationStates["/"] = false
        allOperationsAlreadyUnlocked = false
    }

    private fun evaluateExpression(expression: String): Double {
        val numbers = expression.split(Regex("[+\\-*/]")).mapNotNull { it.toDoubleOrNull() }.toMutableList()
        val ops = expression.filter { it in "+-*/" }.toMutableList()

        if (numbers.isEmpty()) return 0.0

        var result = numbers.removeAt(0)

        while (ops.isNotEmpty()) {
            val op = ops.removeAt(0)
            if (numbers.isEmpty()) break
            val number = numbers.removeAt(0)
            result = when (op) {
                '+' -> result + number
                '-' -> if (operationStates["-"] == true) result - number else result
                '*' -> if (operationStates["*"] == true) result * number else result
                '/' -> if (operationStates["/"] == true && number != 0.0) result / number else if (number == 0.0) Double.NaN else result
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
    object Backspace : CalculatorAction()
    object Equals : CalculatorAction()
    object ShowHints : CalculatorAction()
    object HideHints : CalculatorAction()
    object Reset : CalculatorAction()
    object DismissUnlockMessage : CalculatorAction()
    object DismissAllOperationsUnlockedDialog : CalculatorAction()
}
