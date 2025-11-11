package com.divora.viewmodel

import android.app.Application
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.divora.data.UserDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.math.sqrt

data class HintInfo(
    val description: String,
    val code: String,
    val isUnlocked: () -> Boolean
)

data class Achievement(
    val title: String,
    val description: String,
    val isUnlocked: () -> Boolean
)

class CalculatorViewModel(application: Application, private val userDataStore: UserDataStore) : AndroidViewModel(application) {

    val display = mutableStateOf("0")
    val operationStates = mutableStateMapOf("+" to true, "-" to false, "*" to false, "/" to false, "√" to false, "%" to false)
    val showHintsDialog = mutableStateOf(false)
    val showAchievementsDialog = mutableStateOf(false)
    val unlockedOperationMessage = mutableStateOf<String?>(null)
    val allOperationsUnlocked = mutableStateOf(false)
    val calculationTrigger = mutableStateOf(0)
    private var allOperationsAlreadyUnlocked = false
    val answerAchievementUnlocked = mutableStateOf(false)

    val achievements: List<Achievement> = listOf(
        Achievement("The First Step", "Unlock the subtraction operation.") { operationStates["-"] == true },
        Achievement("The Second Step", "Unlock the division operation.") { operationStates["/"] == true },
        Achievement("The Third Step", "Unlock the multiplication operation.") { operationStates["*"] == true },
        Achievement("The Whole Calculator", "Unlock all operations.") { allOperationsAlreadyUnlocked },
        Achievement("The Answer", "Calculate the answer to the ultimate question of life, the universe, and everything.") { answerAchievementUnlocked.value },
        Achievement("Root of the Problem", "Unlock the square root operation.") { operationStates["√"] == true },
        Achievement("A Small Percentage", "Unlock the percentage operation.") { operationStates["%"] == true }
    )

    val hints: List<HintInfo> = listOf(
        HintInfo(
            description = "Subtraction (-)",
            code = """
                if (expression == "2+2") {
                    operationStates["-"] = true
                }
            """.trimIndent(),
            isUnlocked = { operationStates["-"] == true }
        ),
        HintInfo(
            description = "Division (/)",
            code = """
                if (expression == "5-1") {
                    operationStates["/"] = true
                }
            """.trimIndent(),
            isUnlocked = { operationStates["/"] == true }
        ),
        HintInfo(
            description = "Multiplication (*)",
            code = """
                if (result.isNaN()) {
                   operationStates["*"] = true
                }
            """.trimIndent(),
            isUnlocked = { operationStates["*"] == true }
        ),
        HintInfo(
            description = "Square Root (√)",
            code = """
                if (expression == "9*9") {
                    operationStates["√"] = true
                }
            """.trimIndent(),
            isUnlocked = { operationStates["√"] == true }
        ),
        HintInfo(
            description = "Percentage (%)",
            code = """
                if (expression == "100/10") {
                    operationStates["%"] = true
                }
            """.trimIndent(),
            isUnlocked = { operationStates["%"] == true }
        )
    )

    private var expression = ""
    private var resultJustCalculated = false

    init {
        viewModelScope.launch {
            operationStates["-"] = userDataStore.subtractionUnlockedFlow.first()
            operationStates["/"] = userDataStore.divisionUnlockedFlow.first()
            operationStates["*"] = userDataStore.multiplicationUnlockedFlow.first()
            operationStates["√"] = userDataStore.sqrtUnlockedFlow.first()
            operationStates["%"] = userDataStore.percentUnlockedFlow.first()
            allOperationsAlreadyUnlocked = userDataStore.allOperationsUnlockedAlreadyFlow.first()
            answerAchievementUnlocked.value = userDataStore.answerAchievementUnlockedFlow.first()
        }
    }

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
                    calculationTrigger.value++
                    // Cheat codes
                    if (expression == "2+2" && operationStates["-"] == false) {
                        operationStates["-"] = true
                        unlockedOperationMessage.value = "Congratulations! You've unlocked Subtraction!"
                        viewModelScope.launch { userDataStore.setOperationUnlocked("-", true) }
                    }
                    if (expression == "5-1" && operationStates["-"] == true && operationStates["/"] == false) {
                        operationStates["/"] = true
                        unlockedOperationMessage.value = "Congratulations! You've unlocked Division!"
                        viewModelScope.launch { userDataStore.setOperationUnlocked("/", true) }
                    }
                    if (expression == "9*9" && operationStates["*"] == true && operationStates["√"] == false) {
                        operationStates["√"] = true
                        unlockedOperationMessage.value = "Congratulations! You've unlocked Square Root!"
                        viewModelScope.launch { userDataStore.setOperationUnlocked("√", true) }
                    }
                    if (expression == "100/10" && operationStates["/"] == true && operationStates["%"] == false) {
                        operationStates["%"] = true
                        unlockedOperationMessage.value = "Congratulations! You've unlocked Percentage!"
                        viewModelScope.launch { userDataStore.setOperationUnlocked("%", true) }
                    }

                    val result = evaluateExpression(expression)

                    if (result.isNaN()) {
                        if (operationStates["*"] == false) {
                            operationStates["*"] = true
                            unlockedOperationMessage.value = "Congratulations! You've unlocked Multiplication!"
                            viewModelScope.launch { userDataStore.setOperationUnlocked("*", true) }
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

                    if (resultString == "42") {
                        answerAchievementUnlocked.value = true
                        viewModelScope.launch { userDataStore.setAnswerAchievementUnlocked(true) }
                    }
                }
            }
            is CalculatorAction.UnaryOperation -> {
                if (expression.isNotEmpty() && expression.last().isDigit()) {
                    val number = expression.toDouble()
                    val result = when (action.operation) {
                        "√" -> sqrt(number)
                        "%" -> number / 100
                        else -> 0.0
                    }
                    display.value = result.toString()
                    expression = result.toString()
                    resultJustCalculated = true
                }
            }
            CalculatorAction.ShowHints -> showHintsDialog.value = true
            CalculatorAction.HideHints -> showHintsDialog.value = false
            CalculatorAction.ShowAchievements -> showAchievementsDialog.value = true
            CalculatorAction.HideAchievements -> showAchievementsDialog.value = false
            CalculatorAction.Reset -> {
                viewModelScope.launch {
                    userDataStore.resetOperations()
                    resetOperations()
                }
                showHintsDialog.value = false
            }
            CalculatorAction.DismissUnlockMessage -> unlockedOperationMessage.value = null
            CalculatorAction.DismissAllOperationsUnlockedDialog -> {
                allOperationsUnlocked.value = false
                allOperationsAlreadyUnlocked = true
                viewModelScope.launch { userDataStore.setAllOperationsAlreadyUnlocked(true) }
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
        operationStates["√"] = false
        operationStates["%"] = false
        allOperationsAlreadyUnlocked = false
        answerAchievementUnlocked.value = false
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
    data class UnaryOperation(val operation: String) : CalculatorAction()
    object Clear : CalculatorAction()
    object Backspace : CalculatorAction()
    object Equals : CalculatorAction()
    object ShowHints : CalculatorAction()
    object HideHints : CalculatorAction()
    object ShowAchievements : CalculatorAction()
    object HideAchievements : CalculatorAction()
    object Reset : CalculatorAction()
    object DismissUnlockMessage : CalculatorAction()
    object DismissAllOperationsUnlockedDialog : CalculatorAction()
}
